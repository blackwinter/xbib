package compiler;

public class SourcePosition extends Position
{

    private Source source;
    private int row;
    private int column;

    public SourcePosition(Source source1, int i, int j)
    {
        source = source1;
        row = i;
        column = j;
    }

    public SourcePosition(Source source1)
    {
        this(source1, 0, 0);
    }

    public Source getSource()
    {
        return source;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return column;
    }

    public void updateCoords(int i, int j)
    {
        row = i;
        column = j;
    }

    public String describe()
    {
        StringBuffer stringbuffer = new StringBuffer();
        if (source != null)
        {
            stringbuffer.append('"');
            stringbuffer.append(source.describe());
            stringbuffer.append('"');
            if (row > 0)
                stringbuffer.append(", ");
        }
        if (row > 0)
        {
            stringbuffer.append("line ");
            stringbuffer.append(row);
        }
        String s = source.getLine(row);
        if (s != null)
        {
            stringbuffer.append('\n');
            stringbuffer.append(s);
            stringbuffer.append('\n');
            for (int i = 0; i < column; i++)
                stringbuffer.append(' ');

            stringbuffer.append('^');
        }
        return stringbuffer.length() != 0 ? stringbuffer.toString() : "input";
    }

    public Position copy()
    {
        return new SourcePosition(source, row, column);
    }
}
