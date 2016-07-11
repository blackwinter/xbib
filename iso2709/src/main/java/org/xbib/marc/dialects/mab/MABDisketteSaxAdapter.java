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
package org.xbib.marc.dialects.mab;

import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.event.EventListener;
import org.xbib.marc.event.FieldEvent;
import org.xbib.marc.xml.sax.MarcXchangeSaxAdapter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;

public class MABDisketteSaxAdapter extends MarcXchangeSaxAdapter {

    @Override
    public MABDisketteSaxAdapter setBuffersize(int buffersize) {
        super.setBuffersize(buffersize);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setReader(Reader reader) {
        this.reader = reader;
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setInputSource(final InputSource source) throws IOException {
        if (source.getByteStream() != null) {
            Charset encoding = Charset.forName(source.getEncoding() != null ? source.getEncoding() : "cp850");
            this.reader = new InputStreamReader(source.getByteStream(), encoding);
        } else {
            this.reader = source.getCharacterStream();
        }
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setContentHandler(ContentHandler handler) {
        super.setContentHandler(handler);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setMarcXchangeListener(String type, MarcXchangeListener listener) {
        super.setMarcXchangeListener(type, listener);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setMarcXchangeListener(MarcXchangeListener listener) {
        super.setMarcXchangeListener(BIBLIOGRAPHIC, listener);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setFieldEventListener(EventListener<FieldEvent> fieldEventListener) {
        super.setFieldEventListener(fieldEventListener);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setSchema(String schema) {
        super.setSchema(schema);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setFormat(String format) {
        super.setFormat(format);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setType(String type) {
        super.setType(type);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setFatalErrors(Boolean fatalerrors) {
        super.setFatalErrors(fatalerrors);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setCleanTags(Boolean cleanTags) {
        super.setCleanTags(cleanTags);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setScrubData(Boolean scrub) {
        super.setScrubData(scrub);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter setTransformData(Boolean transformData) {
        super.setTransformData(transformData);
        return this;
    }

    @Override
    public MABDisketteSaxAdapter addFieldMap(String fieldMapName, Map<String, Object> map) {
        super.addFieldMap(fieldMapName, map);
        return this;
    }

    public MABDisketteLineFeedStreamReader mabDisketteFieldStream() {
        return new MABDisketteLineFeedStreamReader(reader, new DirectListener());
    }

    public MABDisketteLineFeedStreamReader mabDisketteMappedFieldStream() {
        return new MABDisketteLineFeedStreamReader(reader, new MappedStreamListener());
    }

    public MABDisketteCarriageReturnLineFeedStreamReader mabDisketteCarriageReturnFieldStream() {
        return new MABDisketteCarriageReturnLineFeedStreamReader(reader, new DirectListener());
    }

    public MABDisketteCarriageReturnLineFeedStreamReader mabDisketteCarriageReturnMappedFieldStream() {
        return new MABDisketteCarriageReturnLineFeedStreamReader(reader, new MappedStreamListener());
    }

    public void parseCollection(MABDisketteLineFeedStreamReader reader) throws Exception {
        beginCollection();
        reader.begin();
        String s;
        while ((s = reader.readLine ()) != null) {
            reader.processLine(s);
        }
        reader.end();
        reader.close();
        endCollection();
    }

    public void parseCollection(MABDisketteCarriageReturnLineFeedStreamReader reader) throws Exception {
        beginCollection();
        reader.begin();
        String s;
        while ((s = reader.readLine ()) != null) {
            reader.processLine(s);
        }
        reader.end();
        reader.close();
        endCollection();
    }
}
