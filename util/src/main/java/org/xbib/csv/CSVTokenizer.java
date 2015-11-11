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
package org.xbib.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

public class CSVTokenizer extends StreamTokenizer {

    public static final int TT_DELIMITER = -5;

    public static final int TT_QUOTED = -6;

    private char delimChar;

    public CSVTokenizer(Reader r) {
        this(r, '#', ',');
    }

    public CSVTokenizer(Reader r, char cc, char delim) {
        super(r);
        commentChar(cc);
        ordinaryChar('/');
        //if (delim != '\t') {
            ordinaryChar('\t');
        //}
        eolIsSignificant(true);
        delimChar = delim;
    }

    @Override
    public int nextToken() throws IOException {
        // read next token from superclass
        // prepare special csv-tokens
        // types:
        // TT_EOF       = -1
        // TT_EOL       = \n
        // TT_NUMBER    = -2
        // TT_WORD      = -3
        // TT_NOTHING   = -4
        // TT_DELIMITER = -5
        // TT_QUOTED    = -6
        ttype = super.nextToken();
        if (ttype == delimChar) {
            ttype = TT_DELIMITER;
        } else if (ttype == '"') {
            ttype = TT_QUOTED;
        }
        return ttype;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Token[");
        switch (ttype) {
            case TT_DELIMITER:
                sb.append("DELIMITER").append("], line ").append(lineno());
                break;
            default:
                sb.append(super.toString().substring(6));
                break;
        }
        return sb.toString();
    }

}
