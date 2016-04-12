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

import org.xbib.io.StreamListener;
import org.xbib.marc.transformer.StringTransformer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * MarcXchange stream
 */
public class MarcXchangeStream implements MarcXchangeListener, StreamListener<FieldList>, FieldListObservable, MarcXchangeConstants {

    private FieldList fields;

    private StringTransformer transformer;

    private MarcXchangeListener marcXchangeListener;

    private List<StreamListener<FieldList>> listeners = new LinkedList<>();

    @Override
    public MarcXchangeStream add(StreamListener<FieldList> listener) {
        this.listeners.add(listener);
        return this;
    }

    @Override
    public MarcXchangeStream remove(StreamListener<FieldList> listener) {
        this.listeners.remove(listener);
        return this;
    }

    public MarcXchangeStream setMarcXchangeListener(MarcXchangeListener marcXchangeListener) {
        this.marcXchangeListener = marcXchangeListener;
        return this;
    }

    public MarcXchangeStream setStringTransformer(StringTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public void onBegin() throws IOException {
        for (StreamListener<FieldList> listener : listeners) {
            listener.onBegin();
        }
    }

    public void onObject(FieldList fieldList) throws IOException {
        if (fieldList == null) {
            return;
        }
        for (StreamListener<FieldList> listener : listeners) {
            listener.onObject(fieldList);
        }
    }

    @Override
    public void onEnd() throws IOException {
        for (StreamListener<FieldList> listener : listeners) {
            listener.onEnd();
        }
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
            onBegin();
            if (format != null) {
                FieldList fields = new FieldList();
                fields.add(new Field().tag(FORMAT_TAG).data(format));
                onObject(fields);
            }
            if (type != null) {
                FieldList fields = new FieldList();
                fields.add(new Field().tag(TYPE_TAG).data(type));
                onObject(fields);
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
            onEnd();
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
                FieldList fields = new FieldList();
                fields.add(new Field().tag(LEADER_TAG).data(label));
                onObject(fields);
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
        if (field == null) {
            return;
        }
        fields = new FieldList();
        fields.add(field);
    }

    @Override
    public void endControlField(Field field) {
        if (marcXchangeListener != null) {
            marcXchangeListener.endControlField(field);
        }
        if (field == null) {
            return;
        }
        if (transformer != null) {
            field.data(transformer.transform(field.data()));
        }
        try {
            onObject(fields);
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
        // tricky: if we have data in a data field, move them to a subfield with subfield ID "a"
        if (field != null && data != null && !data.isEmpty()) {
            field.subfieldId("a");
            endSubField(field);
        }
        try {
            onObject(fields);
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
