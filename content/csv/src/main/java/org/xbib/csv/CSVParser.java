package org.xbib.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class CSVParser {

    private final CSVLexer lexer;

    private final List<String> row;

    private final Token reusableToken;

    public CSVParser(Reader reader) throws IOException {
        lexer = new CSVLexer(new LookAheadReader(reader), ',', '\\', '"', '#', true, true);
        row = new LinkedList<>();
        reusableToken = new Token();
    }

    public void close() throws IOException {
        lexer.close();
    }

    public long getCurrentLineNumber() {
        return lexer.getCurrentLineNumber();
    }

    public Iterator<List<String>> iterator() {
        return new Iterator<List<String>>() {
            private List<String> current;

            private List<String> getNextRow() throws IOException {
                    return CSVParser.this.nextRow();
            }

            public boolean hasNext() {
                if (current == null) {
                    try {
                        current = getNextRow();
                    } catch (IOException e) {
                        throw new NoSuchElementException(e.getMessage());
                    }
                }
                return current != null && !current.isEmpty();
            }

            public List<String> next() {
                List<String> next = current;
                current = null;
                if (next == null) {
                    try {
                        next = getNextRow();
                    } catch (IOException e) {
                        throw new NoSuchElementException(e.getMessage());
                    }
                    if (next.isEmpty()) {
                        throw new NoSuchElementException("no more rows");
                    }
                }
                return next;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    protected List<String> nextRow() throws IOException {
        row.clear();
        StringBuilder sb = null;
        do {
            reusableToken.reset();
            lexer.nextToken(reusableToken);
            String s = reusableToken.content.toString();
            switch (reusableToken.type) {
                case TOKEN:
                    row.add(s);
                    break;
                case EORECORD:
                    row.add(s);
                    break;
                case EOF:
                    if (reusableToken.isReady) {
                        row.add(s);
                    }
                    break;
                case INVALID:
                    throw new IOException("(line " + getCurrentLineNumber() + ") invalid parse sequence");
                case COMMENT:
                    if (sb == null) {
                        sb = new StringBuilder();
                    } else {
                        sb.append(Constants.LF);
                    }
                    sb.append(reusableToken.content);
                    reusableToken.type = Token.Type.TOKEN;
                    break;
                default:
                    throw new IllegalStateException("unexpected token type: " + reusableToken.type);
            }
        } while (reusableToken.type == Token.Type.TOKEN);
        return row;
    }

}
