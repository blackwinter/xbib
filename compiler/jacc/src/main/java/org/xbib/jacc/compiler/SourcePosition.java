package org.xbib.jacc.compiler;

class SourcePosition extends Position {

    private final Source source;

    private int row;

    private int column;

    SourcePosition(Source source) {
        this(source, 0, 0);
    }

    private SourcePosition(Source source, int i, int j) {
        this.source = source;
        row = i;
        column = j;
    }

    void updateCoords(int i, int j) {
        row = i;
        column = j;
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        if (source != null) {
            sb.append('"');
            sb.append(source.describe());
            sb.append('"');
            if (row > 0)
                sb.append(", ");
            if (row > 0) {
                sb.append("line ");
                sb.append(row);
            }
            String s = source.getLine(row);
            if (s != null) {
                sb.append('\n');
                sb.append(s);
                sb.append('\n');
                for (int i = 0; i < column; i++) {
                    sb.append(' ');
                }
                sb.append('^');
            }
        }
        return sb.length() > 0 ? sb.toString() : "input";
    }

    @Override
    public Position copy() {
        return new SourcePosition(source, row, column);
    }
}
