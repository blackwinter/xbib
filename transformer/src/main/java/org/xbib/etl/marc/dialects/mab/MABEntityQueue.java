/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.etl.marc.dialects.mab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.EntityQueue;
import org.xbib.etl.Specification;
import org.xbib.etl.UnmappedKeyListener;
import org.xbib.etl.marc.SubfieldValueMapper;
import org.xbib.etl.support.*;
import org.xbib.iri.IRI;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryRdfGraph;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MABEntityQueue extends EntityQueue<MABEntityBuilderState, MABEntity, FieldList, String>
        implements Closeable {

    private final static Logger logger = LogManager.getLogger(MABEntityQueue.class.getName());

    private final static IRI tempPredicate = IRI.create("tmp");

    private final String packageName;

    private final IdentifierMapper identifierMapper;

    private final StatusCodeMapper statusMapper;

    private Map<IRI,RdfContentBuilderProvider> providers;

    private UnmappedKeyListener<FieldList> listener;

    private ConfigurableClassifier classifier;

    private List<Supplement> supplements;

    public MABEntityQueue(String packageName, URL path) throws Exception{
        this(packageName, new HashMap<>(), 1, path);
    }

    public MABEntityQueue(String packageName, int workers, URL path) throws Exception {
        this(packageName, new HashMap<>(), workers, path);
    }

    public MABEntityQueue(String packageName, Map<String,Object> params, int workers, URL path) throws Exception {
        super(new MABSpecification(path != null ? path.openStream() : null, new HashMap<>(), params, packageName), workers);
        this.packageName = packageName;
        this.identifierMapper = setupIdentifierMapper(params);
        this.statusMapper = setupStatusMapper(params);
        logger.info("package: {}", packageName);
        logger.info("specification: {} {} entities", getSpecification(), getSpecification().getEntities().size());
        logger.info("identifier mapper: {} entries", identifierMapper.getMap().size());
        logger.info("status mapper: {} entries", statusMapper.getMap().size());
    }

    public MABEntityQueue setUnmappedKeyListener(UnmappedKeyListener<FieldList> listener) {
        this.listener = listener;
        return this;
    }

    public MABEntityQueue setContentBuilderProviders(Map<IRI,RdfContentBuilderProvider> providers) {
        this.providers = providers;
        return this;
    }

    protected IdentifierMapper setupIdentifierMapper(Map<String,Object> params) {
        IdentifierMapper identifierMapper = new IdentifierMapper();
        // configured sigel
        try {
            ValueMaps valueMaps = new ValueMaps();
            Map<String,String> sigel2isil = valueMaps.getAssocStringMap("org/xbib/analyzer/mab/sigel2isil.json", "sigel2isil");
            identifierMapper.add(sigel2isil);
            if (params != null && params.containsKey("tab_sigel_url")) {
                // current sigel
                URL url = new URL((String) params.get("tab_sigel_url"));
                logger.info("loading tab_sigel from {}", url);
                identifierMapper.load(url.openStream());
                logger.info("sigel2isil size = {}, plus tab_sigel = {}",
                        sigel2isil.size(), identifierMapper.getMap().size());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return identifierMapper;
    }

    @SuppressWarnings("unchecked")
    protected StatusCodeMapper setupStatusMapper(Map<String,Object> params) {
        StatusCodeMapper statusMapper = new StatusCodeMapper();
        try {
            ValueMaps valueMaps = new ValueMaps();
            Map<String,Object> statuscodes = valueMaps.getMap("org/xbib/analyzer/mab/status.json", "status");
            statusMapper.add(statuscodes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return statusMapper;
    }

    public MABEntityQueue addIdentifierMapper(String path) throws IOException {
        identifierMapper.load(getClass().getResource(path).openStream());
        return this;
    }

    public MABEntityQueue addStatusMapper(String path) throws IOException {
        statusMapper.load(path);
        return this;
    }

    public MABEntityQueue addClassifier(String prefix, String isil, String classifierPath) throws IOException {
        if (classifier == null) {
            classifier = new ConfigurableClassifier();
        }
        URL url = new URL(classifierPath);
        InputStream in = url.openStream();
        if (in == null) {
            in = getClass().getResource(classifierPath).openStream();
        }
        classifier.load(in, isil, prefix);
        logger.info("added classifications for {} with size of {}", isil, classifier.getMap().size());
        return this;
    }

    public MABEntityQueue addSupplement(Class<? extends Supplement> clazz, String idKey, String prefix, String isil, String supplementPath) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (supplements == null) {
            supplements = new ArrayList<>();
        }
        URL url = new URL(supplementPath);
        InputStream in = url.openStream();
        if (in == null) {
            in = getClass().getResource(supplementPath).openStream();
        }
        Constructor<?> conztructor = clazz.getConstructor(String.class);
        Supplement supplement = (Supplement) conztructor.newInstance(new Object[] { idKey });
        supplement.load(in, isil, prefix);
        supplements.add(supplement);
        logger.info("added supplements for {} with size of {}", isil, supplement.getMap().size());
        return this;
    }


    @Override
    public Map<IRI,RdfContentBuilderProvider> contentBuilderProviders() {
        return providers;
    }

    @Override
    public void close() throws IOException {
        try {
            super.finish(60L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void beforeCompletion(MABEntityBuilderState state) throws IOException {
        // empty
    }

    @Override
    public void afterCompletion(MABEntityBuilderState state) throws IOException {
        //empty
    }

    @Override
    public MABWorker newWorker() {
        return new MABWorker();
    }

    public class MABWorker extends EntityWorker {

        @Override
        public MABEntityBuilderState newState() {
            return new MABEntityBuilderState(packageName, getSpecification(),
                    new MemoryRdfGraph(), contentBuilderProviders());
        }

        @Override
        public void build(FieldList fields) throws IOException {
            if (fields == null) {
                return;
            }
            MABEntity entity;
            try {
                Specification<MABEntity> spec = getSpecification();
                entity = spec.getEntity(fields.toKey(), getMap());
            } catch (ClassCastException e) {
                logger.error("can't convert fields {}, entity map problem", fields);
                return;
            }
            if (entity != null) {
                logger.debug("processing entity {} for {}", entity, fields.toKey());
                boolean done = entity.fields(this, fields);
                if (done) {
                    return;
                }
                addToResource(getWorkerState().getResource(), fields, entity);
                // build facets and classify
                String value = fields.getLast().data();
                if (value != null && !value.isEmpty()) {
                    entity.facetize(this, fields.getFirst().data(value));
                } else {
                    for (Field field : fields) {
                        entity.facetize(this, field);
                    }
                }
            } else {
                if (listener != null) {
                    listener.unknown(getWorkerState().getIdentifier(), fields);
                }
            }
        }

        @SuppressWarnings("unchecked")
        public Resource addToResource(Resource resource, FieldList fields, MABEntity entity) throws IOException {
            Map<String, Object> params = entity.getParams();
            // setup
            Map<String, Object> defaultSubfields = (Map<String, Object>) params.get("subfields");
            if (defaultSubfields == null) {
                return resource;
            }
            Map<Field, String> fieldNames = new HashMap<>();
            // create another anoymous resource
            Resource newResource = resource.newResource(tempPredicate);
            // default predicate is the name of the element class
            String predicate = entity.getClass().getSimpleName();
            // the _predicate field allows to select a field to name the resource by a coded value
            if (params.containsKey("_predicate")) {
                predicate = (String) params.get("_predicate");
            }
            boolean overridePredicate = false;
            // put all found fields with configured subfield names to this resource
            for (Field field : fields) {
                // skip all data fields without subfield ID (but not control fields)
                // skip fields that have no data (invalid / degraded)
                if (!field.isControlField() && (field.subfieldId() == null || field.data() == null || field.data().isEmpty())) {
                    continue;
                }
                Map<String, Object> subfields = defaultSubfields;
                // tag predicates defined?
                if (params.containsKey("tags")) {
                    Map<String, Object> tags = (Map<String, Object>) params.get("tags");
                    if (tags.containsKey(field.tag())) {
                        if (!overridePredicate) {
                            predicate = (String) tags.get(field.tag());
                        }
                        subfields = (Map<String, Object>) params.get(predicate);
                        if (subfields == null) {
                            subfields = defaultSubfields;
                        }
                    }
                }
                // indicator-based predicate defined?
                if (params.containsKey("indicators")) {
                    Map<String, Object> indicators = (Map<String, Object>) params.get("indicators");
                    if (indicators.containsKey(field.tag())) {
                        Map<String, Object> indicatorMap = (Map<String, Object>) indicators.get(field.tag());
                        if (indicatorMap.containsKey(field.indicator())) {
                            if (!overridePredicate) {
                                predicate = (String) indicatorMap.get(field.indicator());
                                fieldNames.put(field, predicate);
                            }
                            subfields = (Map<String, Object>) params.get(predicate);
                            if (subfields == null) {
                                subfields = defaultSubfields;
                            }
                        }
                    }
                }
                // is there a subfield value decoder?
                Map.Entry<String, Object> me = SubfieldValueMapper.map(subfields, field);
                if (me.getKey() != null && me.getValue() != null) {
                    String v = me.getValue().toString();
                    if (fieldNames.containsKey(field)) {
                        // field-specific subfield map
                        String fieldName = fieldNames.get(field);
                        Map<String, Object> vm = (Map<String, Object>) params.get(fieldName);
                        if (vm == null) {
                            // fallback to "subfields"
                            vm = (Map<String, Object>) params.get("subfields");
                        }
                        // is value containing a blank?
                        int pos = v.indexOf(' ');
                        // move after blank
                        String vv = pos > 0 ? v.substring(0, pos) : v;
                        // code table lookup
                        if (vm.containsKey(v)) {
                            newResource.add(me.getKey() + "Source", v);
                            v = (String) vm.get(v);
                        } else if (vm.containsKey(vv)) {
                            newResource.add(me.getKey() + "Source", v);
                            v = (String) vm.get(vv);
                        } else {
                            // relation by pattern?
                            List<Map<String, String>> patterns = (List<Map<String, String>>) params.get(fieldName + "pattern");
                            if (patterns != null) {
                                for (Map<String, String> pattern : patterns) {
                                    Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                                    String p = mme.getKey();
                                    String rel = mme.getValue();
                                    Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                                    if (m.matches()) {
                                        newResource.add(me.getKey() + "Source", v);
                                        v = rel;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // default subfield map
                        String fieldName = me.getKey();
                        if (params.containsKey(fieldName)) {
                            try {
                                Map<String, Object> vm = (Map<String, Object>) params.get(fieldName);
                                int pos = v.indexOf(' ');
                                String vv = pos > 0 ? v.substring(0, pos) : v;
                                if (vm.containsKey(v)) {
                                    newResource.add(fieldName + "Source", v);
                                    v = (String) vm.get(v);
                                } else if (vm.containsKey(vv)) {
                                    newResource.add(fieldName + "Source", v);
                                    v = (String) vm.get(vv);
                                } else {
                                    // relation by pattern?
                                    List<Map<String, String>> patterns = (List<Map<String, String>>) params.get(fieldName + "pattern");
                                    if (patterns != null) {
                                        for (Map<String, String> pattern : patterns) {
                                            Map.Entry<String, String> mme = pattern.entrySet().iterator().next();
                                            String p = mme.getKey();
                                            String rel = mme.getValue();
                                            Matcher m = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(v);
                                            if (m.matches()) {
                                                newResource.add(fieldName + "Source", v);
                                                v = rel;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (ClassCastException e) {
                                logger.warn("entity {}: found {} of class {} in entity settings {} for key {} but must be a map",
                                        entity.getClass(),
                                        params.get(fieldName),
                                        params.get(fieldName).getClass(),
                                        params,
                                        fieldName);
                            }
                        }
                    }
                    // transform value v
                    v = entity.data(this, predicate, newResource, me.getKey(), v);
                    // is this the predicate field or a value?
                    if (me.getKey().equals(predicate)) {
                        predicate = v;
                        overridePredicate = true;
                    } else {
                        newResource.add(me.getKey(), v);
                    }
                } else {
                    // no decoder, simple add field data
                    String property = null;
                    String subfieldId = field.subfieldId();
                    if (subfieldId == null) {
                        subfieldId = ""; // empty string, for map lookup
                    }
                    try {
                        property = (String) subfields.get(subfieldId);
                    } catch (ClassCastException e) {
                        logger.error("cannot use string property of '" + subfieldId + "' for field " + field);
                    }
                    if (property != null) {
                        newResource.add(property, entity.data(this, predicate, newResource, property, field.data()));
                    }
                }
            }
            resource.rename(tempPredicate, IRI.builder().curie(predicate).build());
            return newResource;
        }

        public ConfigurableClassifier classifier() {
            return classifier;
        }

        public IdentifierMapper identifierMapper() {
            return identifierMapper;
        }

        public StatusCodeMapper statusCodeMapper() {
            return statusMapper;
        }

        public List<Supplement> supplements() {
            return supplements;
        }
    }

}
