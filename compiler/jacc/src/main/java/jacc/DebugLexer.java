package jacc;

import compiler.Handler;
import compiler.Source;
import java.io.PrintStream;

public class DebugLexer extends JaccLexer
{

    public DebugLexer(Handler handler, Source source)
    {
        super(handler, source);
    }

    public int nextToken()
    {
        int i = super.nextToken();
        System.out.println("Token " + i + " >" + getLexeme() + "<");
        return i;
    }
}
