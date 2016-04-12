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

import org.xbib.io.FastByteArrayOutputStream;
import org.xbib.io.field.FieldSeparator;
import org.xbib.marc.label.RecordLabel;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Iso2709Writer implements MarcXchangeListener, Flushable, Closeable {

    private final static Charset LATIN = Charset.forName("ISO-8859-1");

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final Lock lock = new ReentrantLock(true);

    private final ThreadLocal<Record> records = new ThreadLocal<Record>();

    private final OutputStream out;

    private String format;

    private String type;

    public Iso2709Writer(OutputStream out) {
        this.out = out;
    }

    public Iso2709Writer setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public Iso2709Writer setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    @Override
    public void beginCollection() {
    }

    @Override
    public void endCollection() {
    }

    @Override
    public void beginRecord(String format, String type) {
        records.set(new Record());
    }

    @Override
    public void endRecord() {
        try {
            flush();
        } catch (IOException e) {
            // ignore
        } finally {
            records.remove();
        }
    }

    @Override
    public void leader(String label) {
        records.get().setLeader(label);
    }

    @Override
    public void beginControlField(Field field) {
    }

    @Override
    public void endControlField(Field field) {
        records.get().getControlfields().add(field);
    }

    @Override
    public void beginDataField(Field field) {
        records.get().newFieldCollection();
    }

    @Override
    public void endDataField(Field field) {
        records.get().getDatafields().add(records.get().getCurrent());
    }

    @Override
    public void beginSubField(Field field) {
    }

    @Override
    public void endSubField(Field field) {
        records.get().getCurrent().add(field);
    }

    public void flush() throws IOException {
        lock.lock();
        try {
            Record record = records.get();
            buildRecord(record);
            out.write(record.getLabel().getRecordLabel().getBytes(LATIN));
            record.getDirectoryStream().writeTo(out);
            record.getFieldStream().writeTo(out);
            out.write(FieldSeparator.GS);
        } finally {
            lock.unlock();
        }
    }

    public void buildRecord(Record record) throws IOException {
        FastByteArrayOutputStream directory = record.getDirectoryStream();
        FastByteArrayOutputStream fields = record.getFieldStream();
        int pos = 0;
        for (Field controlfield : record.getControlfields()) {
            fields.write(controlfield.data().getBytes(UTF8));
            fields.write(FieldSeparator.RS);
            directory.write(makeEntry(controlfield.tag(), pos, fields.size() - pos).getBytes(LATIN));
            pos = fields.size();
        }
        for (FieldList datafield : record.getDatafields()) {
            Field f = datafield.getFirst();
            fields.write(f.indicator().getBytes(LATIN));
            for (Field subfield : datafield) {
                if (subfield.isSubField()) {
                    fields.write(FieldSeparator.US);
                    fields.write(subfield.subfieldId().getBytes(LATIN));
                    fields.write(subfield.data().getBytes(UTF8));
                }
            }
            fields.write(FieldSeparator.RS);
            directory.write(makeEntry(f.tag(), pos, fields.size() - pos).getBytes(LATIN));
            pos = fields.size();
        }
        int base = RecordLabel.LENGTH + directory.size();
        int recordLength = base + fields.size() + 1;
        buildRecordLabel(record, recordLength, base);
    }

    public void buildRecordLabel(Record record, int recordLength, int base) {
        RecordLabel recordLabel = record.getLeader() != null ?
                new RecordLabel(record.getLeader().toCharArray()) : new RecordLabel();
        recordLabel.setBaseAddressOfData(base)
                .setRecordLength(recordLength);
        // TODO more recordlabel defs

        record.setLabel(recordLabel);
    }



    public void close() throws IOException {
        try {
            out.close();
        } finally {
            records.get().cleanup();
            records.remove();
        }
    }

    private String makeEntry(String tag, int offset, int size) throws IOException {
        if (offset > 99999) {
            throw new IOException("offset overflow: " + offset + " for tag " + tag);
        }
        if (size > 99999) {
            throw new IOException("size overflow: " + size + " for tag " + tag);
        }
        String offsetStr = Integer.toString(offset);
        String sizeStr = Integer.toString(size);
        StringBuilder sb = new StringBuilder()
                .append(tag)
                .append("0000".substring(0, 4 - sizeStr.length()))
                .append(sizeStr)
                .append("00000".substring(0, 5 - offsetStr.length()))
                .append(offsetStr);
        return sb.toString();
    }

}
