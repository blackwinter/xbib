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

import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.event.FieldEvent;
import org.xbib.marc.event.EventListener;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MarcXchange field mapper parses MarcXchange fields one by one,
 * with the capability to map fields to other ones, or even remove them.
 */
public abstract class MarcXchangeFieldMapper implements MarcXchangeConstants, MarcXchangeListener {

    private final static String EMPTY = "";

    // the repeat counter pattern
    private final static Pattern REP = Pattern.compile("\\{r\\}");

    private FieldList record = new FieldList();

    private FieldList controlfields = new FieldList();

    private FieldList datafields = new FieldList();

    private Field previousField;

    private int repeatCounter;

    private String format;

    private String type;

    private String label;

    private Map<String, Map<String, Object>> maps;

    private EventListener<FieldEvent> eventListener;

    public MarcXchangeFieldMapper setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public MarcXchangeFieldMapper setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    public MarcXchangeFieldMapper addFieldMap(String fieldMapName, Map<String, Object> map) {
        if (maps == null) {
            maps = new LinkedHashMap<String, Map<String, Object>>();
        }
        maps.put(fieldMapName, map);
        return this;
    }

    public Map<String, Object> getFieldMap(String fieldMapName) {
        return maps.get(fieldMapName);
    }

    public MarcXchangeFieldMapper setFieldEventListener(EventListener<FieldEvent> eventListener) {
        this.eventListener = eventListener;
        return this;
    }

    protected void setRecordLabel(String label) {
        RecordLabel recordLabel = new RecordLabel(label.toCharArray());
        this.label = recordLabel.getRecordLabel();
        if (!this.label.equals(label) && eventListener != null) {
            eventListener.receive(FieldEvent.RECORD_LABEL_CHANGED.setChange(label, this.label));
        }
    }

    protected void addControlField(Field field) {
        // there is one controlfield rule, only 1 occurence of 001 allowed
        if (RECORD_NUMBER_FIELD.equals(field.tag())) {
            for (Field f : controlfields) {
                if (RECORD_NUMBER_FIELD.equals(f.tag())) {
                    // already exist, drop this new 001 field
                    if (eventListener != null) {
                        eventListener.receive(FieldEvent.RECORD_NUMBER_MULTIPLE.setField(field));
                    }
                    return;
                }
            }
            if (eventListener != null) {
                eventListener.receive(FieldEvent.RECORD_NUMBER.setField(field));
            }
        }
        this.controlfields.add(field);
    }

    protected void addField(Field field) {
        this.datafields.add(field);
    }

    /**
     * Flush a data field by taking the designator
     * and push it to the destination record field.
     * This must be called after a data field is closed.
     * If mapping exist, set up mapping process,
     * and iterate over subfield mappings.
     */
    protected void flushField() {
        if (datafields == null || datafields.isEmpty()) {
            return;
        }
        // optimization: if no mappings exist, simply add all fields as they are
        if (maps == null || maps.isEmpty()) {
            for (Field field : datafields) {
                record.add(field);
            }
            datafields = new FieldList();
            return;
        }
        // data fields
        Iterator<Field> it = datafields.iterator();
        if (!it.hasNext()) {
            return;
        }
        Field datafield = it.next();
        // if field is empty, advance to next non-empty field
        while (datafield.equals(Field.EMPTY_FIELD) && it.hasNext()) {
            datafield = it.next();
        }
        if (isRepeat(previousField, datafield)) {
            repeatCounter++;
        } else {
            repeatCounter = 0;
        }
        // save field
        previousField = new Field(datafield);
        // the heavy lifting, map the field
        Operation op = map(datafield);
        if (op.equals(Operation.OPEN)) {
            record.add(Field.EMPTY_FIELD);
        }
        if (op.equals(Operation.KEEP)) {
            // inject datafield "open" event if subfield tag has changed
            // we accept only subfield data
            if (!record.isEmpty() && record.getLast().isSubField() && datafield.isSubField()
                    && !datafield.tag().equals(record.getLast().tag())) {
                record.add(new Field(datafield).subfieldId(null));
            }
        }
        if (!op.equals(Operation.SKIP)) {
            record.add(datafield);
        }
        // loop over subfields and map them all
        Field subfield;
        while (it.hasNext()) {
            subfield = it.next();
            if (Field.EMPTY_FIELD.equals(subfield)) {
                record.add(subfield);
            } else {
                Operation subfieldOp = map(subfield);
                if (subfieldOp != Operation.SKIP) {
                    if (op.equals(Operation.OPEN) || op.equals(Operation.APPEND)) {
                        // when mapped, there can be "close" datafield events, skip them
                        if (subfield.isSubField()) {
                            record.add(subfield);
                        }
                    } else {
                        record.add(subfield);
                    }
                }
            }
        }
        // reset datafields
        datafields = new FieldList();
    }

    protected void flushRecord(String receivedFormat, String receivedtype) {
        // skip empty record
        if (record == null || record.isEmpty()) {
            return;
        }
        if (format == null && receivedFormat != null) {
            format = receivedFormat;
        }
        if (type == null && receivedtype != null) {
            type = receivedtype;
        }
        beginRecord(format, type);
        leader(label);
        for (Field field : controlfields) {
            beginControlField(field);
            endControlField(field);
        }
        // Sequence of data fields in "record" is:
        // 1. datafield (open) - optional
        // 2. subfield list of datafield
        // 3. datafield (close)
        // Control fields are skipped.
        boolean inData = false;
        for (Field field : record) {
            if (field.equals(Field.EMPTY_FIELD)) {
                // close tag if tag is open
                if (inData) {
                    // ensure no data field has content (for XML)
                    endDataField(field.data(null));
                    inData = false;
                }
            } else if (field.isSubField()) {
                if (!inData) {
                    // ensure that emission of data field does not contain subfield ID or data
                    beginDataField(new Field(field).subfieldId(null).data(null));
                    inData = true;
                }
                beginSubField(field);
                endSubField(field);
            } else if (!field.isControlField()) {
                if (inData) {
                    // ensure no data field has content (for XML)
                    endDataField(field.data(null));
                    inData = false;
                } else {
                    beginDataField(new Field(field).subfieldId(null).data(null));
                    inData = true;
                }
            }
        }
        // forgotten close data field?
        if (inData) {
            endDataField(new Field(record.getLast()).subfieldId(null).data(null));
        }
        endRecord();
        // reset all the counters and variables for next record
        repeatCounter = 0;
        previousField = null;
        datafields = new FieldList();
        controlfields = new FieldList();
        record = new FieldList();
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
    @SuppressWarnings("unchecked")
    protected Operation map(Field field) {
        if (field == null) {
            return null;
        }
        // safe guard
        if (maps == null) {
            return Operation.KEEP;
        }
        for (String fieldMapName : maps.keySet()) {
            Map<String,Object> map = maps.get(fieldMapName);
            if (map == null) {
                continue;
            }
            if (map.containsKey(field.tag())) {
                Object o = map.get(field.tag());
                if (o == null) {
                    // value null means remove this field
                    if (eventListener != null) {
                        eventListener.receive(FieldEvent.FIELD_DROPPED.setField(field).setCause(fieldMapName));
                    }
                    field.clear();
                    return Operation.SKIP;
                }
                if (o instanceof Map) {
                    if (field.isControlField()) {
                        Map<String, Object> subf = (Map<String, Object>) o;
                        if (subf.containsKey(EMPTY)) {
                            o = subf.get(EMPTY);
                            if (o != null) {
                                if (eventListener != null) {
                                    eventListener.receive(FieldEvent.FIELD_MAPPED.setField(field).setCause(fieldMapName));
                                }
                                Operation op = ">".equals(o.toString().substring(0,1)) ? Operation.OPEN :
                                        "<".equals(o.toString().substring(0,1)) ? Operation.CLOSE : Operation.APPEND;
                                String[] s = o.toString().substring(1).split("\\$");
                                if (s.length >= 2) {
                                    s[1] = interpolate(s[1]);
                                    field.tag(s[0]).indicator(s[1]);
                                } else if (s.length == 1) {
                                    field.tag(s[0]);
                                }
                                return op;
                            }
                        }
                    } else {
                        Map<String, Object> ind = (Map<String, Object>) o;
                        if (ind.containsKey(field.indicator())) {
                            o = ind.get(field.indicator());
                            if (o == null) {
                                field.clear();
                                return Operation.SKIP;
                            }
                            if (o instanceof Map) {
                                Map<String, Object> subf = (Map<String, Object>) o;
                                // datafield uses same config as subfield "a" (for convenience) if there is no config for empty string
                                String subfieldId = field.isSubField() ? field.subfieldId() : subf.containsKey(EMPTY) ? EMPTY : "a";
                                if (subf.containsKey(subfieldId)) {
                                    o = subf.get(subfieldId);
                                    if (o == null) {
                                        if (eventListener != null) {
                                            eventListener.receive(FieldEvent.FIELD_DROPPED.setField(field).setCause(fieldMapName));
                                        }
                                        field.clear();
                                        return Operation.SKIP;
                                    } else {
                                        if (eventListener != null) {
                                            eventListener.receive(FieldEvent.FIELD_MAPPED.setField(field).setCause(fieldMapName));
                                        }
                                        Operation op = ">".equals(o.toString().substring(0,1)) ? Operation.OPEN :
                                                "<".equals(o.toString().substring(0,1)) ? Operation.CLOSE : Operation.APPEND;
                                        String[] s = o.toString().substring(1).split("\\$");
                                        if (s.length >= 2) {
                                            s[1] = interpolate(s[1]);
                                            // subfield -> subfield, data field -> data field
                                            if (field.isSubField()) {
                                                field.tag(s[0]).indicator(s[1]).subfieldId(s[2]);
                                            } else {
                                                field.tag(s[0]).indicator(s[1]);
                                            }
                                        } else if (s.length == 1) {
                                            field.tag(s[0]);
                                        }
                                        return op;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return Operation.KEEP;
    }

    /**
     * Checks if a field repeats another.
     * @param previous the previous field
     * @param next the next field
     * @return true if field is repeated
     */
    protected boolean isRepeat(Field previous, Field next) {
        return previous != null && next != null && previous.tag() != null && previous.tag().equals(next.tag());
    }


    /**
     * Interpolate variables.
     * @param value the input value
     * @return the interpolated string
     */
    protected String interpolate(String value) {
        Matcher m = REP.matcher(value);
        if (m.find()) {
            return m.replaceAll(Integer.toString(repeatCounter));
        } else {
            return value;
        }
    }

    enum Operation {
        KEEP, APPEND, OPEN, CLOSE, SKIP
    }

}
