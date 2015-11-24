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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CSVGenerator implements Closeable, Flushable {

    private final static char comma = ',';

    private final static char quote = '\"';

    private final static char escapeCharacter = '\"';

    private final static char tab = '\t';

    private final static String lf = System.getProperty("line.separator");

    private Writer writer;

    private int col;

    private int row;

    private String[] keys;

    public CSVGenerator(Writer writer) {
        this.writer = writer;
        this.col = 0;
        this.keys = new String[]{};
    }

    public CSVGenerator keys(List<String> keys) {
        this.keys = keys.toArray(new String[keys.size()]);
        return this;
    }

    public CSVGenerator keys(String[] keys) {
        this.keys = keys;
        return this;
    }

    public CSVGenerator writeKeys() throws IOException {
        for (String k : keys) {
            write(k);
        }
        return this;
    }

    public CSVGenerator write(String value) throws IOException {
        if (col > 0) {
            writer.write(comma);
        }
        if (value != null) {
            writer.write(escape(value));
        }
        col++;
        if (col > keys.length) {
            writer.write(lf);
            row++;
            col = 0;
        }
        return this;
    }

    public int getColumn() {
        return col;
    }

    public int getRow() {
        return row;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    private String escape(String value) {
        if (value.indexOf(quote) < 0 && value.indexOf(escapeCharacter) < 0
                && value.indexOf(comma) < 0 && value.indexOf(tab) < 0  && !value.contains(lf)) {
           return value;
        }
        int length = value.length();
        StringBuilder sb = new StringBuilder(length + 2);
        sb.append(quote);
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(i);
            if (ch == quote) {
                sb.append(quote);
            }
            sb.append(ch);
        }
        sb.append(quote);
        return sb.toString();
    }

}
