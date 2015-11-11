package compiler;

import java.io.IOException;
import java.io.Reader;

public class JavaSource extends Source
{

    private Reader input;
    private int tabwidth;
    private String description;
    private static final int DEFAULT_TABWIDTH = 8;
    private int c0;
    private int c1;
    private int lineNumber;
    private StringBuffer buf;

    public JavaSource(Handler handler, String s, Reader reader, int i)
    {
        super(handler);
        c1 = 0;
        lineNumber = 0;
        description = s;
        input = reader;
        tabwidth = i;
    }

    public JavaSource(Handler handler, String s, Reader reader)
    {
        this(handler, s, reader, 8);
    }

    public JavaSource(String s, Reader reader)
    {
        this(null, s, reader);
    }

    public String describe()
    {
        return description;
    }

    private void skip()
        throws IOException
    {
        c0 = c1;
        if (c0 != -1)
        {
            c1 = input.read();
            if (c0 == 26 && c1 == -1)
                c0 = c1;
        }
    }

    public String readLine()
    {
        try
        {
            if (input == null)
                return null;
            if (buf == null)
                buf = new StringBuffer();
            else
                buf.setLength(0);
            if (lineNumber++ == 0)
            {
                skip();
                skip();
            }
            if (c0 == -1)
                return null;
            do
            {
                if (c0 == -1 || c0 == 10 || c0 == 13)
                    break;
                if (c0 == 92)
                {
                    skip();
                    if (c0 == 117)
                    {
                        do
                            skip();
                        while (c0 == 117);
                        int i = 0;
                        int k = 0;
                        for (int l = 0; k < 4 && c0 != -1 && (l = Character.digit((char)c0, 16)) >= 0;)
                        {
                            i = (i << 4) + l;
                            k++;
                            skip();
                        }

                        if (k != 4)
                            report(new Warning("Error in Unicode escape sequence"));
                        else
                            buf.append((char)i);
                        continue;
                    }
                    buf.append('\\');
                    if (c0 == -1)
                        break;
                    buf.append((char)c0);
                    skip();
                } else
                if (c0 == 9 && tabwidth > 0)
                {
                    for (int j = tabwidth - buf.length() % tabwidth; j > 0; j--)
                        buf.append(' ');

                    skip();
                } else
                {
                    buf.append((char)c0);
                    skip();
                }
            } while (true);
            if (c0 == 13)
                skip();
            if (c0 == 10)
                skip();
        }
        catch (IOException ioexception)
        {
            close();
        }
        return buf.toString();
    }

    public int getLineNo()
    {
        return lineNumber;
    }

    public void close()
    {
        if (input != null)
        {
            try
            {
                input.close();
            }
            catch (IOException ioexception) { }
            input = null;
            buf = null;
        }
    }
}
