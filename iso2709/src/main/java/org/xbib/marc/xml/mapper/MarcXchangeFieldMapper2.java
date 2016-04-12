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
package org.xbib.marc.xml.mapper;

import org.xbib.io.field.MarcField;
import org.xbib.io.field.MarcListener;
import org.xbib.io.field.event.EventListener;
import org.xbib.io.field.event.MarcFieldEvent;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.label.RecordLabel;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MarcXchange field mapper parses MarcXchange fields one by one,
 * with the capability to map fields to other ones, or even remove them.
 */
public abstract class MarcXchangeFieldMapper2 implements MarcXchangeConstants, MarcListener {

    private final static String EMPTY = "";

    // the repeat counter pattern
    private final static Pattern REP = Pattern.compile("\\{r\\}");

    private List<MarcField> controlfields = new LinkedList<>();

    private List<MarcField> datafields = new LinkedList<>();

    private int repeatCounter;

    private String format;

    private String type;

    private String label;

    private Map<String, Map<String, Object>> maps;

    private EventListener<MarcFieldEvent> eventListener;

    public MarcXchangeFieldMapper2 setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public MarcXchangeFieldMapper2 setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    public MarcXchangeFieldMapper2 addFieldMap(String fieldMapName, Map<String, Object> map) {
        if (maps == null) {
            maps = new LinkedHashMap<>();
        }
        maps.put(fieldMapName, map);
        return this;
    }

    public MarcXchangeFieldMapper2 setFieldEventListener(EventListener<MarcFieldEvent> eventListener) {
        this.eventListener = eventListener;
        return this;
    }

    protected void setRecordLabel(String label) {
        RecordLabel recordLabel = new RecordLabel(label.toCharArray());
        this.label = recordLabel.getRecordLabel();
        if (!this.label.equals(label) && eventListener != null) {
            eventListener.receive(MarcFieldEvent.RECORD_LABEL_CHANGED.setChange(label, this.label));
        }
    }

    protected void addControlField(MarcField field) {
        // there is one controlfield rule, only 1 occurence of 001 allowed
        if (RECORD_NUMBER_FIELD.equals(field.getTag())) {
            for (MarcField f : controlfields) {
                if (RECORD_NUMBER_FIELD.equals(f.getTag())) {
                    // already exist, drop this new 001 field
                    if (eventListener != null) {
                        eventListener.receive(MarcFieldEvent.RECORD_NUMBER_MULTIPLE.setField(field));
                    }
                    return;
                }
            }
            if (eventListener != null) {
                eventListener.receive(MarcFieldEvent.RECORD_NUMBER.setField(field));
            }
        }
        this.controlfields.add(field);
    }

    protected void addDataField(MarcField field) {
        this.datafields.add(field);
    }


    protected void emitRecord(String receivedFormat, String receivedtype) {
        if (format == null && receivedFormat != null) {
            format = receivedFormat;
        }
        if (type == null && receivedtype != null) {
            type = receivedtype;
        }
        beginRecord(format, type);
        leader(label);
        controlfields.stream().forEach(this::field);
        datafields.stream().map(this::map).forEach(this::field);
        endRecord();
        // reset all the counters and variables for next record
        repeatCounter = 0;
        controlfields = new LinkedList<>();
        datafields = new LinkedList<>();
    }

    /**
     * The mapper. Maps a field by the following convention:
     *
     * tag : {
     *   ind : {
     *       subf : "totag$toind$tosubf"
     *   }
     * }
     *
     * where <code>toind</code> can be interpolated by repeat counter.
     *
     * If a null value is configured, the field is removed.
     *
     * @param field the field to map from
     * @return the mpped field
     */
    protected MarcField map(MarcField field) {
        if (field == null) {
            return null;
        }
        MarcField.Builder builder = MarcField.builder();
        // safe guard
        if (maps == null) {
            builder.marcField(field).operation(MarcField.Operation.KEEP);
            return builder.build();
        }
        for (Map.Entry<String,Map<String,Object>> entry: maps.entrySet()) {
            String fieldMapName = entry.getKey();
            Map<String,Object> map = entry.getValue();
            if (map == null) {
                continue;
            }
            if (map.containsKey(field.getTag())) {
                Object o = map.get(field.getTag());
                if (o == null) {
                    // value null means remove this field
                    if (eventListener != null) {
                        eventListener.receive(MarcFieldEvent.FIELD_DROPPED.setField(field).setCause(fieldMapName));
                    }
                    //field.clear();
                    builder.operation(MarcField.Operation.SKIP);
                    return builder.build();
                }
                if (o instanceof Map) {
                    if (field.isControl()) {
                        Map<String, Object> subf = (Map<String, Object>) o;
                        if (subf.containsKey(EMPTY)) {
                            o = subf.get(EMPTY);
                            if (o != null) {
                                if (eventListener != null) {
                                    eventListener.receive(MarcFieldEvent.FIELD_MAPPED.setField(field).setCause(fieldMapName));
                                }
                                MarcField.Operation op = ">".equals(o.toString().substring(0,1)) ? MarcField.Operation.OPEN :
                                        "<".equals(o.toString().substring(0,1)) ? MarcField.Operation.CLOSE : MarcField.Operation.APPEND;
                                String[] s = o.toString().substring(1).split("\\$");
                                if (s.length >= 2) {
                                    s[1] = interpolate(s[1]);
                                    builder.tag(s[0]).indicator(s[1]);
                                } else if (s.length == 1) {
                                    builder.tag(s[0]);
                                }
                                return builder.operation(op).build();
                            }
                        }
                    } else {
                        Map<String, Object> ind = (Map<String, Object>) o;
                        if (ind.containsKey(field.getIndicator())) {
                            o = ind.get(field.getIndicator());
                            if (o == null) {
                                //field.clear();
                                return builder.operation(MarcField.Operation.SKIP).build();
                            }
                            if (o instanceof Map) {
                                Map<String, Object> subf = (Map<String, Object>) o;
                                for (MarcField.Subfield subfield : field.getSubfields()) {
                                    String subfieldId = subfield.getId();
                                    if (subf.containsKey(subfieldId)) {
                                        o = subf.get(subfieldId);
                                        if (o == null) {
                                            if (eventListener != null) {
                                                eventListener.receive(MarcFieldEvent.FIELD_DROPPED.setField(field).setCause(fieldMapName));
                                            }
                                            //field.clear();
                                            //return Operation.SKIP;
                                        } else {
                                            if (eventListener != null) {
                                                eventListener.receive(MarcFieldEvent.FIELD_MAPPED.setField(field).setCause(fieldMapName));
                                            }
                                            MarcField.Operation op = ">".equals(o.toString().substring(0, 1)) ? MarcField.Operation.OPEN :
                                                    "<".equals(o.toString().substring(0, 1)) ? MarcField.Operation.CLOSE : MarcField.Operation.APPEND;
                                            String[] s = o.toString().substring(1).split("\\$");
                                            if (s.length >= 2) {
                                                s[1] = interpolate(s[1]);
                                                builder.tag(s[0]).indicator(s[1]).subfield(s[2], subfield.getValue(), op);
                                            } else if (s.length == 1) {
                                                builder.tag(s[0]);
                                            }
                                        }
                                    }
                                }
                                return builder.build();
                            }
                        }
                    }
                }
            }
        }
        return builder.operation(MarcField.Operation.KEEP).build();
    }

    /**
     * Checks if a field repeats another.
     * @param previous the previous field
     * @param next the next field
     * @return true if field is repeated
     */
    private boolean isRepeat(MarcField previous, MarcField next) {
        return previous != null && next != null && previous.getTag() != null && previous.getTag().equals(next.getTag());
    }

    /**
     * Interpolate variables.
     * @param value the input value
     * @return the interpolated string
     */
    private String interpolate(String value) {
        Matcher m = REP.matcher(value);
        return m.find() ? m.replaceAll(Integer.toString(repeatCounter)) : value;
    }
}
