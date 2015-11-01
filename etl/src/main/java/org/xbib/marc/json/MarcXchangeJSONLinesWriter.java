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
package org.xbib.marc.json;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.marc.MarcException;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.util.LinkedHashMultiMap;
import org.xbib.util.MultiMap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Convert a MarcXchange stream to Elasticsearch XContent lines.
 *
 * This writer is threadsafe.
 *
 */
public class MarcXchangeJSONLinesWriter implements MarcXchangeListener, MarcXchangeConstants {

    private final OutputStream out;

    private XContentBuilder builder;

    private FieldList fields;

    private StringTransformer transformer;

    private MarcXchangeListener marcXchangeListener;

    private String format;

    private String type;

    private final ReentrantLock lock = new ReentrantLock(true);

    public MarcXchangeJSONLinesWriter(OutputStream out) throws IOException {
        this.out = out;
        this.builder = jsonBuilder(out);
    }

    public MarcXchangeJSONLinesWriter setMarcXchangeListener(MarcXchangeListener marcXchangeListener) {
        this.marcXchangeListener = marcXchangeListener;
        return this;
    }

    public MarcXchangeJSONLinesWriter setStringTransformer(StringTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    public void startDocument() throws IOException {
        // empty
    }

    public void endDocument() throws IOException {
        if (lock.isLocked()) {
            lock.unlock();
        }
    }

    public MarcXchangeJSONLinesWriter setFormat(String format) {
        this.format = format;
        return this;
    }

    public MarcXchangeJSONLinesWriter setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public void beginCollection() {
        if (marcXchangeListener != null) {
            marcXchangeListener.beginCollection();
        }
    }

    @Override
    public void endCollection() {
        if (marcXchangeListener != null) {
            marcXchangeListener.endCollection();
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        lock.lock();
        if (this.format != null) {
            format = this.format;
        }
        if (this.type != null) {
            type = this.type;
        }
        try {
            builder.startObject();
            if (format != null) {
                builder.field(FORMAT_TAG, format);
            }
            if (type != null) {
                builder.field(TYPE_TAG, type);
            }
        } catch (IOException e) {
            throw new MarcException(e);
        }
        if (marcXchangeListener != null) {
            marcXchangeListener.beginRecord(format, type);
        }
    }

    @Override
    public void endRecord() {
        try {
            builder.endObject();
            builder.flush();
            out.write('\n');
            if (marcXchangeListener != null) {
                marcXchangeListener.endRecord();
            }
        } catch (IOException e) {
            throw new MarcException(e);
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void leader(String label) {
        try {
            if (label != null) {
                builder.field(LEADER_TAG, label);
            }
        } catch (IOException e) {
            throw new MarcException(e);
        }
        if (marcXchangeListener != null) {
            marcXchangeListener.leader(label);
        }
    }

    @Override
    public void beginControlField(Field field) {
        fields = new FieldList();
        fields.add(field);
        if (marcXchangeListener != null) {
            marcXchangeListener.beginControlField(field);
        }
    }

    @Override
    public void endControlField(Field field) {
        String data = field != null ? field.data() : null;
        if (transformer != null) {
            data = transformer.transform(data);
        }
        try {
            for (Field f : fields) {
                builder.field(f.tag(), data);
            }
        } catch (IOException e) {
            throw new MarcException(e);
        }
        if (marcXchangeListener != null) {
            marcXchangeListener.endControlField(field);
        }
    }

    @Override
    public void beginDataField(Field field) {
        fields = new FieldList();
        fields.add(field);
        if (marcXchangeListener != null) {
            marcXchangeListener.beginDataField(field);
        }
    }

    @Override
    public void endDataField(Field field) {
        String data = field != null ? field.data() : null;
        // if we have no subfields (data is in data field),
        // then move data to a subfield with subfield code "a".
        // Add doubleblank indicators if missing.
        if (field != null && data != null && !data.isEmpty()) {
            if (field.indicator() == null) {
                field.indicator("  ");
            }
            field.subfieldId("a");
            endSubField(field);
        }
        try {
            if (fields != null && !fields.isEmpty()) {
                Iterator<Field> it = fields.iterator();
                Field f = it.next();
                String ind = f.indicator();
                if (ind == null) {
                    ind = "  ";
                }
                builder.startObject(f.tag())
                        .startObject(ind.replace(' ', '_'));
                // subfields may repeat, use multimap to collect them into array
                // if there is only one subfield, this could be optimized to a simple
                // builder.field(...)
                MultiMap<String, String> subfields = new LinkedHashMultiMap<>();
                while (it.hasNext()) {
                    f = it.next();
                    subfields.put(f.subfieldId(), f.data());
                }
                for (String key : subfields.keySet()) {
                    Collection<String> values = subfields.get(key);
                    if (values.size() > 1) {
                        builder.array(key, values);
                    } else {
                        builder.field(key, values.iterator().next());
                    }
                }
                builder.endObject().endObject();
            }
        } catch (IOException e) {
            throw new MarcException(e);
        }
        if (marcXchangeListener != null) {
            marcXchangeListener.endDataField(field);
        }
    }

    @Override
    public void beginSubField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.beginSubField(field);
        }
    }

    @Override
    public void endSubField(Field field) {
        if (field != null) {
            if (transformer != null) {
                field.data(transformer.transform(field.data()));
            }
            fields.add(field);
        }
        if (marcXchangeListener != null) {
            marcXchangeListener.endSubField(field);
        }
    }
}
