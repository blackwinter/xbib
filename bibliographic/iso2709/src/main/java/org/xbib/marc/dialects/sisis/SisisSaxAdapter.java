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
package org.xbib.marc.dialects.sisis;

import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.event.EventListener;
import org.xbib.marc.event.FieldEvent;
import org.xbib.marc.xml.MarcXchangeSaxAdapter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class SisisSaxAdapter extends MarcXchangeSaxAdapter {

    public SisisSaxAdapter setBuffersize(int buffersize) {
        super.setBuffersize(buffersize);
        return this;
    }

    public SisisSaxAdapter setReader(Reader reader) {
        this.reader = reader;
        return this;
    }

    public SisisSaxAdapter setInputStream(InputStream in) throws IOException {
        this.reader = new InputStreamReader(in, "UTF-8");
        return this;
    }

    public SisisSaxAdapter setInputSource(final InputSource source) throws IOException {
        if (source.getByteStream() != null) {
            String encoding = source.getEncoding() != null ? source.getEncoding() : "ANSEL";
            this.reader = new InputStreamReader(source.getByteStream(), encoding);
        } else {
            this.reader = source.getCharacterStream();
        }
        return this;
    }

    public SisisSaxAdapter setContentHandler(ContentHandler handler) {
        super.setContentHandler(handler);
        return this;
    }

    public SisisSaxAdapter setMarcXchangeListener(String type, MarcXchangeListener listener) {
        super.setMarcXchangeListener(type, listener);
        return this;
    }

    public SisisSaxAdapter setMarcXchangeListener(MarcXchangeListener listener) {
        super.setMarcXchangeListener(BIBLIOGRAPHIC, listener);
        return this;
    }

    public SisisSaxAdapter setFieldEventListener(EventListener<FieldEvent> fieldEventListener) {
        super.setFieldEventListener(fieldEventListener);
        return this;
    }

    public SisisSaxAdapter setSchema(String schema) {
        super.setSchema(schema);
        return this;
    }

    public SisisSaxAdapter setFormat(String format) {
        super.setFormat(format);
        return this;
    }

    public SisisSaxAdapter setType(String type) {
        super.setType(type);
        return this;
    }

    public SisisSaxAdapter setFatalErrors(Boolean fatalerrors) {
        super.setFatalErrors(fatalerrors);
        return this;
    }

    public SisisSaxAdapter setCleanTags(Boolean cleanTags) {
        super.setCleanTags(cleanTags);
        return this;
    }

    public SisisSaxAdapter setScrubData(Boolean scrub) {
        super.setScrubData(scrub);
        return this;
    }

    public SisisSaxAdapter setTransformData(Boolean transformData) {
        super.setTransformData(transformData);
        return this;
    }

    public SisisSaxAdapter addFieldMap(String fieldMapName, Map<String, Object> map) {
        super.addFieldMap(fieldMapName, map);
        return this;
    }

    public SisisFieldStreamReader sisisFieldStream() {
        return new SisisFieldStreamReader(reader, new DirectListener());
    }

    public SisisFieldStreamReader sisisMappedFieldStream() {
        return new SisisFieldStreamReader(reader,  new MappedStreamListener());
    }

    public void parseCollection(SisisFieldStreamReader reader) throws Exception {
        beginCollection();
        String s;
        reader.begin();
        while ((s = reader.readLine ()) != null) {
            reader.processLine(s);
        }
        reader.end();
        reader.close();
        endCollection();
    }

}
