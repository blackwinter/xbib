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
package org.xbib.marc.dialects.marctag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.field.CarriageReturnLineFeedStreamReader;
import org.xbib.io.field.FieldListener;
import org.xbib.io.field.FieldSeparator;
import org.xbib.marc.label.RecordLabel;

import java.io.Reader;

/**
 * "MAB-Diskette" is an ISO2709 format derivative with custom padding symbold and field delimiters
 * created originally for diskette distribution to PC systems with MS-DOS.
 */
public class MarcTagFieldStreamReader extends CarriageReturnLineFeedStreamReader {

    private final static Logger logger = LogManager.getLogger(MarcTagFieldStreamReader.class);

    private FieldListener listener;

    private String FILE_SEPARATOR = String.valueOf(FieldSeparator.FS);

    private String SUBFIELD_SEPARATOR = String.valueOf(FieldSeparator.US);

    public MarcTagFieldStreamReader(Reader reader, FieldListener listener) {
        super(reader);
        this.listener = listener;
    }

    public void begin() {
    }

    /*
     * Marc 21 "tagged"
     *
     * <tag> <ind1> <ind2> <1f> <subf> <1c> <value> [ <1f> <subf> <1c> <value> ... ] <0d> <0a>
     *
     */
    public void processLine(String line) throws Exception {
        if (line == null || line.isEmpty()) {
            return;
        }
        if (line.length() < 5) {
            return;
        }
        String number = line.substring(0, 3);
        String ind1 = line.substring(3, 4);
        String ind2 = line.substring(4, 5);
        String value = line.substring(5);
        if ("###".equals(number)) { // leader
            listener.mark(FieldSeparator.GS);
            RecordLabel label = new RecordLabel(value.toCharArray())
                    .setIndicatorLength(2)
                    .setSubfieldIdentifierLength(2);
            listener.data(label.getRecordLabel());
        } else if (number.startsWith("00")) {
            listener.mark(FieldSeparator.RS);
            listener.data(number + value);
        } else {
            // split into subfields
            String designator = number + ind1 + ind2;
            listener.mark(FieldSeparator.RS);
            listener.data(designator);
            value = value.replaceAll(FILE_SEPARATOR, "");
            String[] subfields = value.split(SUBFIELD_SEPARATOR);
            for (String s : subfields) {
                if (s.length() > 1) {
                    listener.mark(FieldSeparator.US);
                    listener.data(s); // with subfield code
                }
            }
        }
    }

    public void end() {
        listener.mark(FieldSeparator.GS);
        listener.mark(FieldSeparator.FS);
    }

}
