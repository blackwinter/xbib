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
package org.xbib.marc;

import org.xbib.util.KeyValueStreamListener;
import org.xbib.marc.transformer.StringTransformer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Convert a MarcXchange stream to a key/value stream
 */
public class MarcXchange2KeyValue implements MarcXchangeListener, KeyValueStreamListener<FieldList, String>, MarcXchangeConstants {

    private FieldList fields;

    private StringTransformer transformer;

    private MarcXchangeListener marcXchangeListener;

    private List<KeyValueStreamListener<FieldList, String>> listeners = new LinkedList<>();

    public MarcXchange2KeyValue addListener(KeyValueStreamListener<FieldList, String> listener) {
        this.listeners.add(listener);
        return this;
    }

    public MarcXchange2KeyValue setMarcXchangeListener(MarcXchangeListener marcXchangeListener) {
        this.marcXchangeListener = marcXchangeListener;
        return this;
    }

    public MarcXchange2KeyValue setStringTransformer(StringTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldList, String> begin() throws IOException {
        for (KeyValueStreamListener<FieldList, String> listener : listeners) {
            listener.begin();
        }
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldList, String> keyValue(FieldList key, String value) throws IOException {
        for (KeyValueStreamListener<FieldList, String> listener : listeners) {
            // we allow null value, but null keys are not passed to the listeners
            if (key != null) {
                listener.keyValue(key, value);
            }
        }
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldList, String> keys(List<FieldList> keys) throws IOException {
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldList, String> values(List<String> values) throws IOException {
        return this;
    }

    @Override
    public KeyValueStreamListener<FieldList, String> end() throws IOException {
        for (KeyValueStreamListener<FieldList, String> listener : listeners) {
            listener.end();
        }
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
        if (marcXchangeListener != null) {
            marcXchangeListener.beginRecord(format, type);
        }
        try {
            begin();
            if (format != null) {
                FieldList field = new FieldList();
                field.add(new Field().tag(FORMAT_TAG).data(format));
                keyValue(field, format);
            }
            if (type != null) {
                FieldList field = new FieldList();
                field.add(new Field().tag(TYPE_TAG).data(type));
                keyValue(field, type);
            }
        } catch (IOException e) {
            throw new MarcException(e);
        }
    }

    @Override
    public void endRecord() {
        if (marcXchangeListener != null) {
            marcXchangeListener.endRecord();
        }
        try {
            end();
        } catch (IOException e) {
            throw new MarcException(e);
        }
    }

    @Override
    public void leader(String label) {
        if (marcXchangeListener != null) {
            marcXchangeListener.leader(label);
        }
        try {
            if (label != null) {
                FieldList field = new FieldList();
                field.add(new Field().tag(LEADER_TAG).data(label));
                keyValue(field, label);
            }
        } catch (IOException e) {
            throw new MarcException(e);
        }
    }

    @Override
    public void beginControlField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.beginControlField(field);
        }
        fields = new FieldList();
        fields.add(field);
    }

    @Override
    public void endControlField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.endControlField(field);
        }
        String data = field != null ? field.data() : null;
        if (transformer != null) {
            data = transformer.transform(data);
        }
        try {
            keyValue(fields, data);
        } catch (IOException e) {
            throw new MarcException(e);
        }
    }

    @Override
    public void beginDataField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.beginDataField(field);
        }
        fields = new FieldList();
        fields.add(field);
    }

    @Override
    public void endDataField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.endDataField(field);
        }
        String data = field != null ? field.data() : null;
        // if we have data in a data field, move them to a subfield with subfield ID "a"
        if (field != null && data != null && !data.isEmpty()) {
            field.subfieldId("a");
            endSubField(field);
        }
        try {
            keyValue(fields, data);
        } catch (IOException e) {
            throw new MarcException(e);
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
        if (marcXchangeListener != null) {
            marcXchangeListener.endSubField(field);
        }
        if (field == null) {
            return;
        }
        if (transformer != null) {
            field.data(transformer.transform(field.data()));
        }
        fields.add(field);
    }
}
