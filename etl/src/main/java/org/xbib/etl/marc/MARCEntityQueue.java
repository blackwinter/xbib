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
package org.xbib.etl.marc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.EntityQueue;
import org.xbib.etl.UnmappedKeyListener;
import org.xbib.iri.IRI;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryRdfGraph;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MARCEntityQueue extends EntityQueue<MARCEntityBuilderState, MARCEntity, FieldList, String>
        implements Closeable {

    private final static Logger logger = LogManager.getLogger(MARCEntityQueue.class);

    private final static IRI tempPredicate = IRI.create("tmp");

    private UnmappedKeyListener<FieldList> listener;

    private final String packageName;

    public MARCEntityQueue(String packageName, int workers, String... paths) throws Exception {
        this(packageName, new HashMap<>(), workers,  paths);
    }

    public MARCEntityQueue(String packageName, Map<String, Object> params, int workers,  String... paths)
            throws Exception {
        super(new MARCSpecification(new HashMap<>(), params, MARCEntityQueue.class.getClassLoader(),
                packageName, paths), workers);
        this.packageName = packageName;
        logger.info("specification: {} {} entities", getSpecification(), getSpecification().getEntities().size());
    }

    public MARCEntityQueue setUnmappedKeyListener(UnmappedKeyListener<FieldList> listener) {
        this.listener = listener;
        return this;
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
    public void beforeCompletion(MARCEntityBuilderState state) throws IOException {
        // empty
    }

    @Override
    public void afterCompletion(MARCEntityBuilderState state) throws IOException {
        // empty
    }

    @Override
    public MARCWorker newWorker() {
        return new MARCWorker();
    }

    public class MARCWorker extends EntityWorker {

        @Override
        public MARCEntityBuilderState newState() {
            return new MARCEntityBuilderState(packageName, getSpecification(), new MemoryRdfGraph(), contentBuilderProviders());
        }

        @Override
        public void build(FieldList fields) throws IOException {
            if (fields == null) {
                return;
            }
            String key = fields.toKey();
            MARCEntity entity = getSpecification().getEntity(key, getMap());
            if (entity != null) {
                boolean done = entity.fields(this, fields);
                if (done) {
                    return;
                }
                addToResource(state().getResource(), fields, entity);
            } else {
                if (listener != null) {
                    listener.unknown(state().getRecordIdentifier(), fields);
                }
            }
        }

        @SuppressWarnings("unchecked")
        void addToResource(Resource resource, FieldList fields, MARCEntity entity) throws IOException {
            // setup
            Map<String, Object> defaultSubfields = (Map<String, Object>) entity.getParams().get("subfields");
            if (defaultSubfields == null) {
                return;
            }
            Map<Field, String> fieldNames = new HashMap<>();
            // create another anoymous resource, will be linked late if predicate is determined
            Resource newResource = resource.newResource(tempPredicate);
            // default predicate is the name of the class
            String predicate = entity.getClass().getSimpleName();
            // the _predicate field allows to select a field to name the resource by a coded value
            if (entity.getParams().containsKey("_predicate")) {
                predicate = (String) entity.getParams().get("_predicate");
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
                if (entity.getParams().containsKey("tags")) {
                    Map<String, Object> tags = (Map<String, Object>) entity.getParams().get("tags");
                    if (tags.containsKey(field.tag())) {
                        if (!overridePredicate) {
                            predicate = (String) tags.get(field.tag());
                        }
                        subfields = (Map<String, Object>) entity.getParams().get(predicate);
                        if (subfields == null) {
                            subfields = defaultSubfields;
                        }
                    }
                }
                // indicator-based predicate defined?
                if (entity.getParams().containsKey("indicators")) {
                    Map<String, Object> indicators = (Map<String, Object>) entity.getParams().get("indicators");
                    if (indicators.containsKey(field.tag())) {
                        Map<String, Object> indicatorMap = (Map<String, Object>) indicators.get(field.tag());
                        if (indicatorMap.containsKey(field.indicator())) {
                            if (!overridePredicate) {
                                predicate = (String) indicatorMap.get(field.indicator());
                                fieldNames.put(field, predicate);
                            }
                            subfields = (Map<String, Object>) entity.getParams().get(predicate);
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
                        Map<String, Object> vm = (Map<String, Object>) entity.getParams().get(fieldName);
                        if (vm == null) {
                            // fallback to "subfields"
                            vm = (Map<String, Object>) entity.getParams().get("subfields");
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
                            List<Map<String, String>> patterns = (List<Map<String, String>>) entity.getParams().get(fieldName + "pattern");
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
                        if (entity.getParams().containsKey(fieldName)) {
                            try {
                                Map<String, Object> vm = (Map<String, Object>) entity.getParams().get(fieldName);
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
                                    List<Map<String, String>> patterns = (List<Map<String, String>>) entity.getParams().get(fieldName + "pattern");
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
                                        entity.getParams().get(fieldName),
                                        entity.getParams().get(fieldName).getClass(),
                                        entity.getParams(),
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
                    String property;
                    String subfieldId = field.subfieldId();
                    if (subfieldId == null) {
                        subfieldId = ""; // empty string, for map lookup
                    }
                    property = (String) subfields.get(subfieldId);
                    if (property == null) {
                        // unmapped subfield ID
                        property = subfieldId;
                    }
                    newResource.add(property, entity.data(this, predicate, newResource, property, field.data()));
                }
            }
            // rename, now that we know the predicate
            resource.rename(tempPredicate, IRI.builder().curie(predicate).build());
        }
    }

}
