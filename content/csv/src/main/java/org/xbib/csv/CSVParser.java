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

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

public class CSVParser {

    private static final int STATE_LITERAL = 1;

    private static final int STATE_DELIMITER = 2;

    private CSVTokenizer lexer;

    private int curState;

    private boolean beginOfLine = true;

    public CSVParser(Reader reader) {
        this.lexer = new CSVTokenizer(reader);
        this.curState = STATE_LITERAL;
    }

    public CSVParser(CSVTokenizer tokenizer) {
        this.lexer = tokenizer;
        this.curState = STATE_LITERAL;
    }

    /**
     * Reads next token from tokenizer
     *
     * @return next token
     */
    public String nextToken() throws IOException {
        int ttype = lexer.nextToken();
        if (ttype == CSVTokenizer.TT_EOF) {
            throw new EOFException();
        }
        switch (curState) {
            case STATE_LITERAL:
                switch (ttype) {
                    case CSVTokenizer.TT_NUMBER:
                        beginOfLine = false;
                        curState = STATE_DELIMITER;
                        return String.valueOf(lexer.nval);
                    case CSVTokenizer.TT_WORD:
                        beginOfLine = false;
                        curState = STATE_DELIMITER;
                        return lexer.sval;
                    case CSVTokenizer.TT_QUOTED:
                        beginOfLine = false;
                        curState = STATE_DELIMITER;
                        return lexer.sval;
                    case CSVTokenizer.TT_EOL:
                        if (beginOfLine) {
                            return nextToken();
                        } else {
                            beginOfLine = true;
                            return "";
                        }
                    case CSVTokenizer.TT_DELIMITER:
                        return "";
                }
                break;
            case STATE_DELIMITER:
                switch (ttype) {
                    case CSVTokenizer.TT_DELIMITER:
                        curState = STATE_LITERAL;
                        return nextToken();
                    case CSVTokenizer.TT_EOL:
                        beginOfLine = true;
                        curState = STATE_LITERAL;
                        return nextToken();
                }
                break;
        }
        throw new IOException("malformed CSV: curState=" + curState + " ttype=" + ttype + " lineno=" + lexer.lineno());
    }
}
