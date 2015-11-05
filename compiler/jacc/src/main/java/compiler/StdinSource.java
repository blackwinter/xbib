package compiler;

import java.io.InputStream;

public class StdinSource extends Source
{

    private boolean foundEOF;
    private int lineNumber;
    private StringBuffer buf;

    public StdinSource(Handler handler)
    {
        super(handler);
        foundEOF = false;
        lineNumber = 0;
        buf = new StringBuffer();
    }

    public String describe()
    {
        return "standard input";
    }

    public String readLine()
    {
        if (foundEOF)
            return null;
        lineNumber++;
        buf.setLength(0);
        do
        {
            int i = 0;
            try
            {
                i = System.in.read();
            }
            catch (Exception exception)
            {
                report(new Failure("Error in input stream"));
            }
            if (i == 10)
                break;
            if (i < 0)
            {
                foundEOF = true;
                break;
            }
            buf.append((char)i);
        } while (true);
        return buf.toString();
    }

    public int getLineNo()
    {
        return lineNumber;
    }
}
