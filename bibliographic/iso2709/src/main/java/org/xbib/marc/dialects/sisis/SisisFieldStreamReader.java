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

import org.xbib.io.field.FieldListener;
import org.xbib.io.field.FieldSeparator;
import org.xbib.io.field.LineFeedStreamReader;
import org.xbib.marc.label.RecordLabel;

import java.io.Reader;

/**
 * SISIS EXPORT is a derivative of ISO2709, with field iteration counter placed into indicator positions.
 *
 */
public class SisisFieldStreamReader extends LineFeedStreamReader {

    private FieldListener listener;

    private RecordLabel label;

    public SisisFieldStreamReader(Reader reader, FieldListener listener) {
        super(reader);
        this.label = new RecordLabel().setIndicatorLength(2).setSubfieldIdentifierLength(1);
        this.listener = listener;
    }

    public void begin() {
        listener.data(label.getRecordLabel());
    }

    public void processLine(String line) throws Exception {
        if (line == null || line.isEmpty()) {
            return;
        }
        // find first colon
        int pos = line.indexOf(':');
        if (pos > 0) {
            String number = line.substring(0, pos);
            String value = line.substring(pos + 1);
            String ind2 = " ";
            // number can have a counter for field repetitions
            pos = number.indexOf('.');
            if (pos > 0) {
                ind2 = number.substring(pos + 3, pos + 4); // drop pos+1, pos+2 (always "0"?)
                number = number.substring(0, pos);
            }
            // number is always four characters, take the last three and make the first character to "indicator 1"
            String ind1 = number.substring(0,1);
            number = number.substring(1,4);
            String data;
            String designator;
            // special field 9999 means "end of record" (group delimiter)
            if ("999".equals(number)) {
                listener.mark(FieldSeparator.GS);
                listener.data(label.getRecordLabel());
            } else {
                if ("000".equals(number)) {
                    number = "001";
                    designator = number;
                } else if (number.startsWith("00")) {
                    if (!" ".equals(ind2)) {
                        // move fields out of controlfield area "000"-"009" to "900-909" plus ind2="9"
                        // if there is a numbering, to avoid validation errors
                        designator = "9" + number.substring(1,3) + "9" + ind2 + "a";
                    } else {
                        designator = number;
                    }
                } else {
                    designator = number + ind1 + ind2 + "a";
                }
                data = designator + value;
                listener.mark(FieldSeparator.RS);
                listener.data(data);
            }
        }
    }

    public void end() {
        listener.mark(FieldSeparator.GS);
        listener.mark(FieldSeparator.FS);
    }

}
