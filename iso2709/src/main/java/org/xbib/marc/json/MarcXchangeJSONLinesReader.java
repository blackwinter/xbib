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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

/**
 * Read MARC from JSON lines format
 */
public class MarcXchangeJSONLinesReader {

    private final MarcXchangeListener listener;

    private Integer bufferSize = 16 * 1024;

    private JsonParser jsonParser;

    private String format;

    private String type;

    private String leader;

    private String tag;

    private String indicator;

    private String subfieldId;

    public MarcXchangeJSONLinesReader(MarcXchangeListener listener) {
        this.listener = listener;
    }

    public MarcXchangeJSONLinesReader setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public void parse(InputStream in) throws IOException {
        parse(new InputStreamReader(in, "UTF-8"));
    }

    public void parse(Reader reader) throws IOException {
        JsonFactory factory = new JsonFactory();
        BufferedReader bufferedReader = new BufferedReader(reader, bufferSize);
        String line;
        listener.beginCollection();
        while ((line = bufferedReader.readLine()) != null) {
            this.jsonParser = factory.createParser(line);
            List<Field> fields = new ArrayList<Field>(8);
            jsonParser.nextToken();
            parseObject(fields, 0);
            emitFields(fields);
            listener.endRecord();
        }
        listener.endCollection();
        bufferedReader.close();
    }

    private void parseObject(List<Field> fields, int level) throws IOException {
        while (jsonParser.nextToken() != null && jsonParser.getCurrentToken() != END_OBJECT) {
            if (FIELD_NAME.equals(jsonParser.getCurrentToken())) {
                jsonParser.nextToken();
                parseInner(jsonParser.getCurrentName(), fields, level);
            } else {
                throw new JsonParseException("expected field name, but got " + jsonParser.getCurrentToken(),
                        jsonParser.getCurrentLocation());
            }
        }
        emitFields(fields);
    }

    private void parseInner(String name, List<Field> fields, int level) throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (START_OBJECT.equals(currentToken)) {
            switch (level) {
                case 0: {
                    tag = name;
                    break;
                }
                case 1: {
                    indicator = name.replace('_', ' ');
                    break;
                }
            }
            parseObject(fields, level + 1);
        } else if (currentToken.isScalarValue()) {
            if (MarcXchangeConstants.FORMAT_TAG.equals(name)) {
                format = jsonParser.getText();
            } else if (MarcXchangeConstants.TYPE_TAG.equals(name)) {
                type = jsonParser.getText();
            } else if (MarcXchangeConstants.LEADER_TAG.equals(name)) {
                leader = jsonParser.getText();
            } else {
                switch (level) {
                    case 0: {
                        tag = name;
                        break;
                    }
                    case 1: {
                        indicator = name.replace('_', ' ');
                        break;
                    }
                    case 2: {
                        subfieldId = name;
                        break;
                    }
                }
                Field field = new Field().tag(tag).indicator(indicator).subfieldId(subfieldId).data(jsonParser.getText());
                fields.add(field);
                // need to emit control field here because it is complete
                if (field.isControlField()) {
                    emitFields(fields);
                }
            }
        }
    }

    private void emitFields(List<Field> fields) {
        if (fields.isEmpty()) {
            return;
        }
        if (format != null) {
            listener.beginRecord(format, type);
            listener.leader(leader);
            format = null;
            type = null;
            leader = null;
        }
        Field f = fields.get(0);
        if (f.isControlField()) {
            listener.beginControlField(f);
            listener.endControlField(f);
        } else {
            Field dataField = new Field().tag(f.tag()).indicator(f.indicator());
            listener.beginDataField(dataField);
            for (Field field : fields) {
                listener.beginSubField(field);
                listener.endSubField(field);
            }
            listener.endDataField(dataField);
        }
        fields.clear();
        tag = null;
        indicator = null;
        subfieldId = null;
    }

}