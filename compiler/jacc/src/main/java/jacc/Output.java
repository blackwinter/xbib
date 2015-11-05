package jacc;

import compiler.*;
import jacc.grammar.Grammar;
import jacc.grammar.Machine;
import java.io.*;
import java.util.Date;

public abstract class Output extends Phase
{

    protected JaccJob job;
    protected Grammar grammar;
    protected int numTs;
    protected int numNTs;
    protected int numSyms;
    protected Machine machine;
    protected int numStates;
    protected JaccTables tables;
    protected JaccResolver resolver;
    protected Settings settings;

    protected Output(Handler handler, JaccJob jaccjob)
    {
        super(handler);
        job = jaccjob;
        tables = jaccjob.getTables();
        machine = tables.getMachine();
        grammar = machine.getGrammar();
        numTs = grammar.getNumTs();
        numNTs = grammar.getNumNTs();
        numSyms = grammar.getNumSyms();
        numStates = machine.getNumStates();
        resolver = jaccjob.getResolver();
        settings = jaccjob.getSettings();
    }

    public void write(String s)
    {
        PrintWriter printwriter = null;
        try
        {
            printwriter = new PrintWriter(new FileWriter(s));
            write(printwriter);
        }
        catch (IOException ioexception)
        {
            report(new Failure("Cannot write to file \"" + s + "\""));
        }
        if (printwriter != null)
            printwriter.close();
    }

    public abstract void write(PrintWriter printwriter);

    protected static void indent(PrintWriter printwriter, int i, String as[])
    {
        for (int j = 0; j < as.length; j++)
            indent(printwriter, i, as[j]);

    }

    protected static void indent(PrintWriter printwriter, int i)
    {
        for (int j = 0; j < i; j++)
            printwriter.print("    ");

    }

    protected static void indent(PrintWriter printwriter, int i, String s)
    {
        indent(printwriter, i);
        printwriter.println(s);
    }

    protected static void datestamp(PrintWriter printwriter)
    {
        printwriter.println("// Output created by jacc on " + new Date());
        printwriter.println();
    }
}
