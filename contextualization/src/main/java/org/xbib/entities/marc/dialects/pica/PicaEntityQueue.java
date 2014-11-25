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
package org.xbib.entities.marc.dialects.pica;

import org.xbib.entities.EntityQueue;
import org.xbib.entities.UnmappedKeyListener;
import org.xbib.entities.marc.SubfieldValueMapper;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Resource;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PicaEntityQueue extends EntityQueue<PicaEntityBuilderState, PicaEntity, FieldList, String> {

    private UnmappedKeyListener<FieldList> listener;

    public PicaEntityQueue(String format) {
        super(format, new PicaSpecification(), 1);
    }

    public PicaEntityQueue(String format, int workers) {
        super(format, new PicaSpecification(), workers);
    }

    public PicaEntityQueue setUnmappedKeyListener(UnmappedKeyListener<FieldList> listener) {
        this.listener = listener;
        return this;
    }

    public void close() throws IOException {
        try {
            finish(60L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public PicaKeyValueWorker newWorker() {
        return new PicaKeyValueWorker();
    }

    public class PicaKeyValueWorker extends EntityWorker {
        @Override
        public void build(FieldList fields, String value) throws IOException {
            if (fields == null) {
                return;
            }
            //logger.debug("pica fields = {} value = {}", fields, value);
            String key = fields.toKey();
            PicaEntity entity = (PicaEntity) specification().getEntity(key, map());
            if (entity != null) {
                // mapping?
                /*if (element.map(fields)) {
                    key = fields.toKey();
                    element = (PicaEntity) specification().getEntity(key, map());
                }*/
                // entity-based processing
                entity.fields(this, fields, value);
                // optional indicator configuration
                Map<String, Object> indicators = (Map<String, Object>) entity.getSettings().get("indicators");
                // optional subfield configuration
                Map<String, Object> subfields = (Map<String, Object>) entity.getSettings().get("subfields");
                if (subfields != null) {
                    // get current resource and create new anoymous resource
                    Resource resource = state().getResource();
                    Resource newResource = state().getResource().newResource();
                    // default predicate is the name of the class
                    String predicate = entity.getClass().getSimpleName();

                    // the _predicate field allows to select a field to name the resource by a coded value
                    if (entity.getSettings().containsKey("_predicate")) {
                        predicate = (String)entity.getSettings().get("_predicate");
                    }
                    // put all found fields with configured subfield names to this resource
                    for (Field field : fields) {
                        // is there a subield value decoder?
                        Map.Entry<String, Object> me = SubfieldValueMapper.map(subfields, field);
                        if (me.getKey() != null) {
                            String v = me.getValue().toString();
                            if (entity.getSettings().containsKey(me.getKey())) {
                                Map<String,Object> vm = (Map<String,Object>)entity.getSettings().get(me.getKey());
                                v = vm.containsKey(v) ? vm.get(v).toString() : v;
                            }
                            // is this the "resource type" field or a simple value?
                            if (me.getKey().equals(predicate)) {
                                predicate = v;
                            } else {
                                newResource.add(me.getKey(), v);
                            }
                        } else {
                            // no decoder, simple add field data
                            String property = (String)subfields.get(field.subfieldId());
                            if (property == null) {
                                property = field.subfieldId(); // unmapped subfield ID
                            }
                            newResource.add(property, field.data());
                        }
                        entity.field(builder(), field, value);
                    }
                    // add child resource
                    resource.add(predicate, newResource);
                    state().setResource(resource); // switch back to old resource
                }
            } else {
                if (listener != null) {
                    listener.unknown(state().getRecordNumber(), fields);
                }
            }
            builder().build(state(), entity, fields, value);
        }
    }
}
