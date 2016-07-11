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
package org.xbib.etl.marc.dialects.nlz;

import org.xbib.etl.UnmappedKeyListener;
import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryRdfGraph;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NlzEntityQueue extends MARCEntityQueue<NlzEntityBuilderState, NlzEntity> {

    private UnmappedKeyListener<FieldList> listener;

    private final Map<String, Resource> serialsMap;

    private final Map<String,Boolean> missingSerials;

    public NlzEntityQueue(Map<String, Resource> serialsMap, String packageName, int workers, URL path) throws Exception {
        super(packageName, workers, path);
        this.serialsMap = serialsMap;
        this.missingSerials = new ConcurrentHashMap<>();
    }

    public NlzEntityQueue setUnmappedKeyListener(UnmappedKeyListener<FieldList> listener) {
        this.listener = listener;
        return this;
    }

    public Map<String,Boolean> getMissingSerials() {
        return missingSerials;
    }

    @Override
    public void beforeCompletion(NlzEntityBuilderState state) throws IOException {
        // empty
    }

    @Override
    public void afterCompletion(NlzEntityBuilderState state) throws IOException {
        // empty
    }

    @Override
    public NlzWorker newWorker() {
        return new NlzWorker();
    }

    public class NlzWorker extends MARCWorker {

        @Override
        public NlzEntityBuilderState newState() {
            return new NlzEntityBuilderState(new MemoryRdfGraph(), contentBuilderProviders(), serialsMap, missingSerials);
        }

        @Override
        public void build(FieldList fields) throws IOException {
            if (fields == null) {
                return;
            }
            String key = fields.toKey();
            NlzEntity entity = getSpecification().getEntity(key, getMap());
            if (entity != null) {
                boolean done = entity.fields(this, fields);
                if (done) {
                    return;
                }
                addToResource(fields, entity);
            } else {
                if (listener != null) {
                    listener.unknown(getWorkerState().getRecordIdentifier(), fields);
                }
            }
        }

        @SuppressWarnings("unchecked")
        void addToResource(FieldList fields, MARCEntity entity) throws IOException {
            // setup
            Map<String, Object> defaultSubfields = (Map<String, Object>) entity.getParams().get("subfields");
            if (defaultSubfields == null) {
                return;
            }
            String predicate = entity.getClass().getSimpleName();
            if (entity.getParams().containsKey("_predicate")) {
                predicate = (String) entity.getParams().get("_predicate");
            }
            for (Field field : fields) {
                // skip all data fields without subfield ID (but not control fields)
                // skip fields that have no data (invalid / degraded)
                if (!field.isControlField() && (field.subfieldId() == null || field.data() == null || field.data().isEmpty())) {
                    continue;
                }
                Map<String, Object> subfields = defaultSubfields;
                if (entity.getParams().containsKey("tags")) {
                    Map<String, Object> tags = (Map<String, Object>) entity.getParams().get("tags");
                    if (tags.containsKey(field.tag())) {
                        predicate = (String) tags.get(field.tag());
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
                            predicate = (String) indicatorMap.get(field.indicator());
                            subfields = (Map<String, Object>) entity.getParams().get(predicate);
                            if (subfields == null) {
                                subfields = defaultSubfields;
                            }
                        }
                    }
                }
                String subfieldId = field.subfieldId();
                if (subfieldId == null) {
                    subfieldId = "";
                }
                String property = (String) subfields.get(subfieldId);
                if (property == null) {
                    property = subfieldId;
                }
                entity.data(this, predicate, null, property, field.data());
            }
        }
    }

}
