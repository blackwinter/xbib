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
package org.xbib.marc.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xbib.io.field.BufferedFieldStreamReader;
import org.xbib.io.field.FieldListener;
import org.xbib.io.field.FieldSeparator;
import org.xbib.io.field.Separable;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldDirectory;
import org.xbib.marc.InvalidFieldDirectoryException;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.event.FieldEvent;
import org.xbib.marc.event.EventListener;
import org.xbib.marc.transformer.IdentityTransformer;
import org.xbib.marc.transformer.StringTransformer;
import org.xbib.marc.xml.mapper.MarcXchangeFieldMapper;
import org.xbib.xml.XMLNS;
import org.xbib.xml.XMLUtil;
import org.xbib.xml.XSI;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The MarcXchange Sax adapter converts ISO 2709 stream events to MarcXchange events.
 * It can also be used to map fields.
 */
public class MarcXchangeSaxAdapter extends MarcXchangeFieldMapper
        implements MarcXchangeConstants, MarcXchangeListener {

    private static final Logger logger = LoggerFactory.getLogger(MarcXchangeSaxAdapter.class.getName());

    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

    private static final String CDATA = "CDATA";

    private StringTransformer transformer = new IdentityTransformer();

    protected Reader reader;

    private FieldDirectory directory;

    private Field designator;

    private RecordLabel recordLabel;

    private boolean datafieldOpen;

    private boolean recordOpen;

    private String schema;

    private String id;

    private String nsUri = MARCXCHANGE_V2_NS_URI;

    private ContentHandler contentHandler;

    private Map<String,MarcXchangeListener> listeners = new HashMap<String,MarcXchangeListener>();

    private MarcXchangeListener listener;

    private EventListener<FieldEvent> fieldEventListener;

    private boolean fatalerrors = false;

    private boolean silenterrors = false;

    private boolean cleanTags = true;

    private boolean scrub = false;

    private int buffersize = 65536;

    private String subfieldDelimiter = null;

    private Integer subfieldIdLength;

    private Field lastDataField;

    public MarcXchangeSaxAdapter() {
    }

    public MarcXchangeSaxAdapter setBuffersize(int buffersize) {
        this.buffersize = buffersize;
        return this;
    }

    public MarcXchangeSaxAdapter setReader(Reader reader) {
        this.reader = reader;
        return this;
    }

    public MarcXchangeSaxAdapter setInputStream(InputStream in) throws IOException {
        this.reader = new InputStreamReader(in, "UTF-8");
        return this;
    }

    public MarcXchangeSaxAdapter setInputSource(final InputSource source) throws IOException {
        if (source.getByteStream() != null) {
            String encoding = source.getEncoding() != null ? source.getEncoding() : "ANSEL";
            this.reader = new InputStreamReader(source.getByteStream(), encoding);
        } else {
            this.reader = source.getCharacterStream();
        }
        return this;
    }

    public MarcXchangeSaxAdapter setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
        return this;
    }

    public MarcXchangeSaxAdapter setMarcXchangeListener(String type, MarcXchangeListener listener) {
        this.listeners.put(type, listener);
        return this;
    }

    public MarcXchangeSaxAdapter setMarcXchangeListener(MarcXchangeListener listener) {
        this.listeners.put(BIBLIOGRAPHIC, listener);
        return this;
    }

    public MarcXchangeSaxAdapter setFieldEventListener(EventListener<FieldEvent> fieldEventListener) {
        super.setFieldEventListener(fieldEventListener);
        this.fieldEventListener = fieldEventListener;
        return this;
    }

    public MarcXchangeSaxAdapter setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public MarcXchangeSaxAdapter setFormat(String format) {
        super.setFormat(format);
        return this;
    }

    public MarcXchangeSaxAdapter setType(String type) {
        super.setType(type);
        return this;
    }
    
    public MarcXchangeSaxAdapter setFatalErrors(Boolean fatalerrors) {
        this.fatalerrors = fatalerrors;
        return this;
    }

    public MarcXchangeSaxAdapter setSilentErrors(Boolean silenterrors) {
        this.silenterrors = silenterrors;
        return this;
    }

    public MarcXchangeSaxAdapter setCleanTags(Boolean cleanTags) {
        this.cleanTags = cleanTags;
        return this;
    }

    public MarcXchangeSaxAdapter setScrubData(Boolean scrub) {
        this.scrub = scrub;
        return this;
    }

    public MarcXchangeSaxAdapter setTransformer(StringTransformer transformer) {
        if (transformer != null) {
            this.transformer = transformer;
        }
        return this;
    }

    public MarcXchangeSaxAdapter setSubfieldDelimiter(String subfieldDelimiter) {
        this.subfieldDelimiter = subfieldDelimiter;
        return this;
    }

    public MarcXchangeSaxAdapter setSubfieldIdLength(Integer subfieldIdLength) {
        this.subfieldIdLength = subfieldIdLength;
        return this;
    }

    public String getIdentifier() {
        return id;
    }

    public MarcXchangeSaxAdapter addFieldMap(String fieldMapName, Map<String, Object> map) {
        super.addFieldMap(fieldMapName, map);
        return this;
    }

    public BufferedFieldStreamReader fieldStream() {
        return new BufferedFieldStreamReader(reader, buffersize, new DirectListener());
    }

    public BufferedFieldStreamReader mappedFieldStream() {
        return new BufferedFieldStreamReader(reader, buffersize,  new MappedStreamListener());
    }

    /**
     * Parse ISO 2709 collection and emit SAX events.
     */
    public void parseCollection(BufferedFieldStreamReader stream) throws IOException, SAXException {
        beginCollection();
        Separable separable;
        do {
            separable = stream.readField();
        } while (separable != null);
        stream.close();
        endCollection();
    }

    @Override
    public void beginCollection() {
        if (contentHandler == null) {
            return;
        }
        try {
            contentHandler.startDocument();
            // write schema info
            AttributesImpl attrs = new AttributesImpl();
            if (MARC21.equalsIgnoreCase(schema)) {
                this.nsUri = MARC21_NS_URI;
                attrs.addAttribute(XMLNS.NS_URI, XSI.NS_PREFIX,
                        XMLNS.NS_PREFIX + ":" + XSI.NS_PREFIX, CDATA, XSI.NS_URI);
                attrs.addAttribute(XSI.NS_URI, "schemaLocation",
                        XSI.NS_PREFIX + ":schemaLocation", CDATA, MARC21_NS_URI + " " + MARC21_SCHEMALOCATION);

            } else {
                this.nsUri = MARCXCHANGE_V2_NS_URI;
                attrs.addAttribute(XMLNS.NS_URI, XSI.NS_PREFIX,
                        XMLNS.NS_PREFIX + ":" + XSI.NS_PREFIX, CDATA, XSI.NS_URI);
                attrs.addAttribute(XSI.NS_URI, "schemaLocation",
                        XSI.NS_PREFIX + ":schemaLocation", CDATA, MARCXCHANGE_V2_NS_URI + " " + MARCXCHANGE_V2_0_SCHEMALOCATION);
            }
            contentHandler.startPrefixMapping("", nsUri);
            contentHandler.startElement(nsUri, COLLECTION, COLLECTION, attrs);
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endCollection() {
        if (contentHandler == null) {
            return;
        }
        try {
            contentHandler.endElement(nsUri, COLLECTION, COLLECTION);
            contentHandler.endDocument();
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        this.listener = listeners.get(type != null ? type : BIBLIOGRAPHIC);
        if (recordOpen) {
            return;
        }
        try {
            AttributesImpl attrs = new AttributesImpl();
            if (format != null && !MARC21.equalsIgnoreCase(schema)) {
                attrs.addAttribute(nsUri, FORMAT, FORMAT, CDATA, format);
            }
            if (type != null) {
                attrs.addAttribute(nsUri, TYPE, TYPE, CDATA, type);
            }
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, RECORD, RECORD, attrs);
            }
            if (listener != null) {
                listener.beginRecord(format, type);
            }
            this.recordOpen = true;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endRecord() {
        if (!recordOpen) {
            return;
        }
        try {
            if (contentHandler != null) {
                contentHandler.endElement(nsUri, RECORD, RECORD);
            }
            if (listener != null) {
                listener.endRecord();
            }
            this.recordOpen = false;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void leader(String value) {
        if (value == null) {
            return;
        }
        try {
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, LEADER, LEADER, EMPTY_ATTRIBUTES);
                contentHandler.characters(value.toCharArray(), 0, value.length());
                contentHandler.endElement(nsUri, LEADER, LEADER);
            }
            if (listener != null) {
                listener.leader(value);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginControlField(Field designator) {
        if (designator == null) {
            return;
        }
        this.lastDataField = new Field(designator);
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(nsUri, TAG, TAG, CDATA, designator.tag());
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, CONTROLFIELD, CONTROLFIELD, attrs);
            }
            if (listener != null) {
                listener.beginControlField(designator);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endControlField(Field designator) {
        try {
            if (listener != null) {
                listener.endControlField(designator);
            }
            String value = designator != null ? designator.data() : lastDataField != null ? lastDataField.data() : null;
            if (value != null && !value.isEmpty()) {
                if (designator != null) {
                    switch (designator.tag()) {
                        case "001":
                            this.id = value;
                            break;
                        case "006":
                        case "007":
                        case "008":
                            // fix fill characters here
                            value = value.replace('^', '|');
                            break;
                    }
                }
                if (contentHandler != null) {
                    contentHandler.characters(value.toCharArray(), 0, value.length());
                }
            }
            if (contentHandler != null) {
                contentHandler.endElement(nsUri, CONTROLFIELD, CONTROLFIELD);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginDataField(Field designator) {
        if (designator == null) {
            return;
        }
        this.lastDataField = new Field(designator);
        try {
            if (designator.isControlField()) {
                beginControlField(designator);
                endControlField(designator);
                return;
            }
            if (datafieldOpen) {
                return;
            }
            AttributesImpl attrs = new AttributesImpl();
            String tag = designator.tag();
            if (tag == null || tag.length() == 0) {
                tag = Field.NULL_TAG; // fallback
                designator.tag(tag);
            }
            attrs.addAttribute(nsUri, TAG, TAG, CDATA, tag);
            int ind = designator.indicator() != null
                    ? designator.indicator().length() : 0;
            // force at least two default blank indicators if schema is MARC21
            if (MARC21.equalsIgnoreCase(schema)) {
                for (int i = (ind == 0 ? 1 : ind); i <= 2; i++) {
                    attrs.addAttribute(null, IND + i, IND + i, CDATA, " ");
                }
            }
            // set indicators
            for (int i = 1; i <= ind; i++) {
                attrs.addAttribute(null, IND + i,
                        IND + i, CDATA, designator.indicator().substring(i - 1, i));
            }
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, DATAFIELD, DATAFIELD, attrs);
            }
            if (listener != null) {
                listener.beginDataField(designator);
            }
            datafieldOpen = true;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endDataField(Field designator) {
        try {
            if (!datafieldOpen) {
                return;
            }
            String value = designator != null ? designator.data() :
                    lastDataField != null && !lastDataField.isSubField() ?
                    lastDataField.data() : null;
            if (value != null && !value.isEmpty() && subfieldDelimiter == null) {
                // datafield carries data. Write as subfield with code a.
                value = transformer.transform(value);
                // write data field per default into a subfield with code 'a'
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute(nsUri, CODE, CODE, CDATA, "a");
                if (contentHandler != null) {
                    contentHandler.startElement(nsUri, SUBFIELD, SUBFIELD, attrs);
                    contentHandler.characters(value.toCharArray(), 0, value.length());
                    contentHandler.endElement(nsUri, SUBFIELD, SUBFIELD);
                }
            }
            if (listener != null) {
                listener.endDataField(designator != null ? designator :
                        lastDataField != null && !lastDataField.isSubField() ?
                        lastDataField : null);
            }
            if (contentHandler != null) {
                contentHandler.endElement(nsUri, DATAFIELD, DATAFIELD);
            }
            datafieldOpen = false;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginSubField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            AttributesImpl attrs = new AttributesImpl();
            String subfieldId = designator.subfieldId();
            if (subfieldId == null || subfieldId.length() == 0) {
                subfieldId = "a"; // fallback
            }
            attrs.addAttribute(nsUri, CODE, CODE, CDATA, subfieldId);
            if (listener != null) {
                listener.beginSubField(designator);
            }
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, SUBFIELD, SUBFIELD, attrs);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endSubField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            if (contentHandler != null) {
                String value = designator.data();
                if (value != null && !value.isEmpty()) {
                    value = transformer.transform(value);
                    contentHandler.characters(value.toCharArray(), 0, value.length());
                }
            }
            if (listener != null) {
                listener.endSubField(designator);
            }
            if (contentHandler != null) {
                contentHandler.endElement(nsUri, SUBFIELD, SUBFIELD);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    public class DirectListener implements FieldListener {

        private char mark = '\u0000';

        private int position = 0;

        private boolean recordOpen = false;

        private boolean datafieldOpen = false;

        @Override
        public void mark(char separator) {
            mark = separator;
            position++;
            if (mark == FieldSeparator.FS) {
                if (datafieldOpen) {
                    datafieldOpen = false;
                    endDataField(null);
                }
                if (recordOpen) {
                    endRecord();
                }
            }
        }

        @SuppressWarnings("fallthrough")
        @Override
        public void data(String data) {
            String fieldContent = data;
            try {
                switch (mark) {
                    case FieldSeparator.FS: // start/end file

                        break;
                    case FieldSeparator.GS: {
                        // start/end of group within a stream
                        if (datafieldOpen) {
                            datafieldOpen = false;
                            endDataField(null);
                        }
                        if (recordOpen) {
                            endRecord(); // close record
                            recordOpen = false;
                        }
                        // fall through is ok!
                    }
                    case '\u0000': {
                        // start of stream
                        position = 0;
                        // skip line-feed (OCLC PICA quirk)
                        if (data.charAt(0) == '\n') {
                            fieldContent = data.substring(1);
                        }
                        if (fieldContent.length() >= RecordLabel.LENGTH) {
                            if (!recordOpen) {
                                beginRecord(getFormat(), getType());
                                recordOpen = true;
                            }
                            String labelStr = fieldContent.substring(0, RecordLabel.LENGTH);
                            recordLabel = new RecordLabel(labelStr.toCharArray());
                            // fix: if subfield delimiter length is null, but a subfield delimiter is configured, set it
                            // the length is plus one for the datafield code
                            if (subfieldDelimiter != null) {
                                recordLabel.setSubfieldIdentifierLength(subfieldDelimiter.length() + 1);
                            }
                            // fix to override wrong subfield ID lengths
                            if (subfieldIdLength != null) {
                                recordLabel.setSubfieldIdentifierLength(subfieldIdLength);
                            }
                            // auto-repair label
                            leader(recordLabel.getRecordLabel());
                            directory = new FieldDirectory(recordLabel, fieldContent);
                            if (directory.isEmpty()) {
                                designator = new Field(recordLabel, fieldContent.substring(RecordLabel.LENGTH));
                                if (designator.tag() != null && !Field.ERROR_TAG.equals(designator.tag())) {
                                    if (subfieldDelimiter != null) {
                                        // skip tag if custom subfield delimiter
                                        designator.data(fieldContent.substring(RecordLabel.LENGTH + 3));
                                    }
                                    if (cleanTags) {
                                        // just one tag cleaning match for the first appearance of the designator. Should be enough for
                                        // all subsequent subfields.
                                        Matcher m = TAG_PATTERN.matcher(designator.tag());
                                        if (!m.matches()) {
                                            if (fieldEventListener != null) {
                                                fieldEventListener.receive(FieldEvent.TAG_CLEANED.setField(designator));
                                            }
                                            // switch invalid tag to error tag
                                            designator.tag(Field.ERROR_TAG);
                                        }
                                    }
                                    beginDataField(designator);
                                    datafieldOpen = true;
                                }
                            }
                        } else {
                            directory = new FieldDirectory(recordLabel, fieldContent);
                            if (directory.isEmpty()) {
                                if (!recordOpen) {
                                    beginRecord(getFormat(), getType());
                                    recordOpen = true;
                                    leader(recordLabel.getRecordLabel());
                                }
                                designator = new Field(recordLabel, fieldContent);
                                beginDataField(designator);
                                datafieldOpen = true;
                            } else {
                                designator = new Field();
                            }
                        }
                        break;
                    }
                    case FieldSeparator.RS: {
                        if (datafieldOpen) {
                            datafieldOpen = false;
                            endDataField(null);
                        }
                        if (directory == null || directory.isEmpty()) {
                            designator = new Field(recordLabel, fieldContent);
                        } else if (directory.containsKey(position)) {
                            designator = new Field(recordLabel, directory.get(position), fieldContent, false);
                        } else {
                            throw new InvalidFieldDirectoryException("byte position not found in directory: "
                                    + position + " - is this stream reading using an 8-bit wide encoding?");
                        }
                        // custom subfield delimiter? Can be useful if source does not split subfields
                        // with FieldSeparator.US but with pseudo delimiters like "$$"
                        if (subfieldDelimiter != null) {
                            // skip control field
                            if (!designator.isControlField()) {
                                if (cleanTags) {
                                    Matcher m = TAG_PATTERN.matcher(designator.tag());
                                    if (!m.matches()) {
                                        if (fieldEventListener != null) {
                                            fieldEventListener.receive(FieldEvent.TAG_CLEANED.setField(designator));
                                        }
                                        // switch invalid tag to error tag
                                        designator.tag(Field.ERROR_TAG);
                                    }
                                }
                                beginDataField(designator.data(""));
                                datafieldOpen = true;
                                // tricky: first field has no subfield ID. We set it to blank.
                                // tag len = 3 ind len = 1
                                fieldContent = fieldContent.length() > 4 ? " " + fieldContent.substring(4) : "";
                                for (String subfield : fieldContent.split(Pattern.quote(subfieldDelimiter))) {
                                    subfield = transformer.transform(subfield);
                                    designator = new Field(recordLabel, designator, subfield, true);
                                    if (scrub) {
                                        String old = designator.data();
                                        designator.data(XMLUtil.sanitizeXml10(designator.data()).toString());
                                        if (!old.equals(designator.data())) {
                                            if (fieldEventListener != null) {
                                                fieldEventListener.receive(FieldEvent.DATA_SCRUBBED.setField(designator));
                                            }
                                        }
                                    }
                                    beginSubField(designator);
                                    endSubField(designator);
                                }
                            }
                        } else {
                            if (cleanTags) {
                                Matcher m = TAG_PATTERN.matcher(designator.tag());
                                if (!m.matches()) {
                                    if (fieldEventListener != null) {
                                        fieldEventListener.receive(FieldEvent.TAG_CLEANED.setField(designator));
                                    }
                                    // switch invalid tag to error tag
                                    designator.tag(Field.ERROR_TAG);
                                }
                            }
                            beginDataField(designator);
                            datafieldOpen = true;
                        }
                        break;
                    }
                    case FieldSeparator.US: {
                        if (!datafieldOpen) {
                            if (cleanTags) {
                                Matcher m = TAG_PATTERN.matcher(designator.tag());
                                if (!m.matches()) {
                                    if (fieldEventListener != null) {
                                        fieldEventListener.receive(FieldEvent.TAG_CLEANED.setField(designator));
                                    }
                                    // switch invalid tag to error tag
                                    designator.tag(Field.ERROR_TAG);
                                }
                            }
                            beginDataField(designator);
                            datafieldOpen = true;
                        }
                        if (designator != null) {
                            fieldContent = transformer.transform(fieldContent);
                            designator = new Field(recordLabel, designator, fieldContent, true);
                            if (scrub) {
                                String old = designator.data();
                                designator.data(XMLUtil.sanitizeXml10(designator.data()).toString());
                                if (!old.equals(designator.data())) {
                                    if (fieldEventListener != null) {
                                        fieldEventListener.receive(FieldEvent.DATA_SCRUBBED.setField(designator));
                                    }
                                }
                            }
                            beginSubField(designator);
                            endSubField(designator);
                        }
                        break;
                    }
                }
            } catch (InvalidFieldDirectoryException ex) {
                // we reciver from invalid field directories
                logger.warn(ex.getMessage());
            } finally {
                position += data.length();
            }
        }

    }

    /**
     * This field listener can map fields according to a given map and organize them into records.
     * Incoming fields (the subfield list) are processed one by one. At the end of record, the fields are
     * flushed out to the listener with the mapped version of data fields and subfields.
     * Data field opening and closing is controlled by the mapped fields.
     * A repeat counter <code>{r}</code> counts if source fields tag do repeat. The repeat counter
     * can be interpolated into the mapped field designator.
     */
    public class MappedStreamListener implements FieldListener {

        private char mark = '\u0000';

        private int position = 0;

        private boolean datafieldOpen = false;

        @Override
        public void mark(char separator) {
            mark = separator;
            position++;
            if (mark == FieldSeparator.FS) {
                flushRecord();
            }
        }

        @SuppressWarnings("fallthrough")
        @Override
        public void data(String data) {
            String fieldContent = data;
            try {
                switch (mark) {
                    case FieldSeparator.FS: // start/end file
                        break;
                    case FieldSeparator.GS: {
                        // start/end of group within a stream
                        if (datafieldOpen) {
                            datafieldOpen = false;
                            addDataField(Field.EMPTY); // is this required?
                        }
                        flushRecord();
                        // fall through is ok!
                    }
                    case '\u0000': {
                        // start of stream
                        position = 0;
                        // skip line-feed (OCLC PICA quirk)
                        if (data.charAt(0) == '\n') {
                            fieldContent = data.substring(1);
                        }
                        if (fieldContent.length() >= RecordLabel.LENGTH) {
                            String labelStr = fieldContent.substring(0, RecordLabel.LENGTH);
                            recordLabel = new RecordLabel(labelStr.toCharArray());
                            // this also repairs/recreates the leader according to the record format
                            setRecordLabel(recordLabel.getRecordLabel());
                            // fix: if subfield delimiter length is null, but a subfield delimiter is configured, set it
                            // the length is plus one for the datafield code
                            if (subfieldDelimiter != null) {
                                recordLabel.setSubfieldIdentifierLength(subfieldDelimiter.length() + 1);
                            }
                            // fix to override wrong subfield ID lengths
                            if (subfieldIdLength != null) {
                                recordLabel.setSubfieldIdentifierLength(subfieldIdLength);
                            }
                            // auto-repair label
                            directory = new FieldDirectory(recordLabel, fieldContent);
                            if (directory.isEmpty()) {
                                designator = new Field(recordLabel, fieldContent.substring(RecordLabel.LENGTH));
                                if (designator.tag() != null) {
                                    if (subfieldDelimiter != null) {
                                        // skip tag if custom subfield delimiter
                                        designator.data(fieldContent.substring(RecordLabel.LENGTH + 3));
                                    }
                                    if (designator.isControlField()) {
                                        addControlField(designator);
                                    } else {
                                        addDataField(designator);
                                    }
                                }
                            }
                        } else {
                            directory = new FieldDirectory(recordLabel, fieldContent);
                            designator = new Field();
                        }
                        break;
                    }
                    case FieldSeparator.RS: {
                        if (datafieldOpen) {
                            datafieldOpen = false;
                            addDataField(Field.EMPTY); // is this required?
                        }
                        if (directory == null || directory.isEmpty()) {
                            designator = new Field(recordLabel, fieldContent);
                        } else if (directory.containsKey(position)) {
                            designator = new Field(recordLabel, directory.get(position), fieldContent, false);
                        } else {
                            throw new InvalidFieldDirectoryException("byte position not found in directory: "
                                    + position + " - is this stream reading using an 8-bit wide encoding?");
                        }
                        // Is custom subfield delimiter set? Can be useful if source does not split subfields
                        // with FieldSeparator.US but with pseudo delimiters like "$$"
                        if (subfieldDelimiter != null) {
                            if (!designator.isControlField()) {
                                addDataField(designator);
                                datafieldOpen = true;
                                // tricky: first field has no subfield ID. We set it to blank.
                                // tag len = 3 ind len = 1
                                fieldContent = fieldContent.length() > 4 ? " " + fieldContent.substring(4) : "";
                                for (String subfield : fieldContent.split(Pattern.quote(subfieldDelimiter))) {
                                    subfield = transformer.transform(subfield);
                                    designator = new Field(recordLabel, designator, subfield, true);
                                    if (scrub) {
                                        String old = designator.data();
                                        designator.data(XMLUtil.sanitizeXml10(designator.data()).toString());
                                        if (!old.equals(designator.data())) {
                                            if (fieldEventListener != null) {
                                                fieldEventListener.receive(FieldEvent.DATA_SCRUBBED.setField(designator));
                                            }
                                        }
                                    }
                                    addDataField(designator);
                                }
                                flushField();
                            }
                        } else {
                            if (designator.isControlField()) {
                                addControlField(designator);
                            } else {
                                addDataField(designator);
                                // do not flush, we opened the datafield
                            }
                            datafieldOpen = true;
                        }
                        break;
                    }
                    case FieldSeparator.US: {
                        if (!datafieldOpen) {
                            datafieldOpen = true;
                            addDataField(designator);
                        }
                        if (designator != null) {
                            fieldContent = transformer.transform(fieldContent);
                            designator = new Field(recordLabel, designator, fieldContent, true);
                            addDataField(designator);
                        }
                        break;
                    }
                }
            } catch (InvalidFieldDirectoryException ex) {
                // we recover from invalid field directories here
                logger.warn(ex.getMessage());
            } finally {
                position += data.length();
            }
        }
    }
}
