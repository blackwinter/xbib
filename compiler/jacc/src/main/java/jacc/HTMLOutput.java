package jacc;

import compiler.Handler;
import java.io.PrintWriter;

public class HTMLOutput extends TextOutput
{

    public HTMLOutput(Handler handler, JaccJob jaccjob, boolean flag)
    {
        super(handler, jaccjob, flag);
    }

    public void write(PrintWriter printwriter)
    {
        printwriter.println("<html>");
        printwriter.println("<title>Generated machine for " + settings.getClassName() + "</title>");
        printwriter.println("<body>");
        printwriter.println("<h1>Generated machine for " + settings.getClassName() + "</h1>");
        printwriter.println("<pre>");
        super.write(printwriter);
        printwriter.println("</pre>");
        printwriter.println("</body>");
        printwriter.println("</html>");
    }

    protected String describeEntry(int i)
    {
        return "<hr><a name=\"st" + i + "\"><b>" + super.describeEntry(i) + "</b></a>";
    }

    protected String describeShift(int i)
    {
        return "<a href=\"#st" + i + "\">" + super.describeShift(i) + "</a>";
    }

    protected String describeGoto(int i)
    {
        return "<a href=\"#st" + i + "\">" + super.describeGoto(i) + "</a>";
    }
}
