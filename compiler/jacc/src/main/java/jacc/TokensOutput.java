package jacc;

import compiler.Handler;
import jacc.grammar.Grammar;
import java.io.PrintWriter;

public class TokensOutput extends Output
{

    public TokensOutput(Handler handler, JaccJob jaccjob)
    {
        super(handler, jaccjob);
    }

    public void write(PrintWriter printwriter)
    {
        datestamp(printwriter);
        String s = settings.getPackageName();
        if (s != null)
        {
            printwriter.println("package " + s + ";");
            printwriter.println();
        }
        printwriter.println("interface " + settings.getInterfaceName() + " {");
        indent(printwriter, 1);
        printwriter.println("int ENDINPUT = 0;");
        for (int i = 0; i < numTs - 1; i++)
        {
            jacc.grammar.Grammar.Symbol symbol = grammar.getTerminal(i);
            if (!(symbol instanceof JaccSymbol))
                continue;
            JaccSymbol jaccsymbol = (JaccSymbol)symbol;
            String s1 = jaccsymbol.getName();
            indent(printwriter, 1);
            if (s1.startsWith("'"))
                printwriter.println("// " + s1 + " (code=" + jaccsymbol.getNum() + ")");
            else
                printwriter.println("int " + s1 + " = " + jaccsymbol.getNum() + ";");
        }

        printwriter.println("}");
    }
}
