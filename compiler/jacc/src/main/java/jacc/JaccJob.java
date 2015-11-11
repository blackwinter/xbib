package jacc;

import compiler.*;
import jacc.grammar.Finitary;
import jacc.grammar.Grammar;
import jacc.grammar.LookaheadMachine;
import jacc.grammar.Parser;
import java.io.*;


public class JaccJob extends Phase
{

    private Settings settings;
    private JaccParser parser;
    private JaccTables tables;
    private JaccResolver resolver;
    private PrintWriter out;

    public JaccJob(Handler handler, PrintWriter printwriter, Settings settings1)
    {
        super(handler);
        out = printwriter;
        settings = settings1;
        parser = new JaccParser(handler, settings1);
    }

    Settings getSettings()
    {
        return settings;
    }

    JaccTables getTables()
    {
        return tables;
    }

    JaccResolver getResolver()
    {
        return resolver;
    }

    private JaccLexer lexerFromFile(String s)
    {
        JaccLexer jacclexer;
        try
        {
            FileReader filereader = new FileReader(s);
            jacclexer = new JaccLexer(getHandler(), new JavaSource(getHandler(), s, filereader));
            jacclexer.nextToken();
            return jacclexer;
        }
        catch (FileNotFoundException filenotfoundexception)
        {
            report(new Failure("Could not open file \"" + s + "\""));
            return null;
        }
    }

    public void parseGrammarFile(String s)
    {
        JaccLexer jacclexer = lexerFromFile(s);
        if (jacclexer != null)
            parser.parse(jacclexer);
    }

    public void buildTables()
    {
        Grammar grammar = parser.getGrammar();
        if (grammar == null || !allDeriveFinite(grammar))
            return;
        LookaheadMachine lookaheadmachine = settings.makeMachine(grammar);
        resolver = new JaccResolver(lookaheadmachine);
        tables = new JaccTables(lookaheadmachine, resolver);
        if (tables.getProdUnused() > 0)
            report(new Warning(tables.getProdUnused() + " rules never reduced"));
        if (resolver.getNumSRConflicts() > 0 || resolver.getNumRRConflicts() > 0)
            report(new Warning("conflicts: " + resolver.getNumSRConflicts() + " shift/reduce, " + resolver.getNumRRConflicts() + " reduce/reduce"));
    }

    private boolean allDeriveFinite(Grammar grammar)
    {
        Finitary finitary = grammar.getFinitary();
        boolean flag = true;
        for (int i = 0; i < grammar.getNumNTs(); i++)
            if (!finitary.at(i))
            {
                flag = false;
                report(new Failure("No finite strings can be derived for " + grammar.getNonterminal(i)));
            }

        return flag;
    }

    public void readRunExample(String s, boolean flag)
    {
        out.println("Running example from \"" + s + "\"");
        JaccLexer jacclexer = lexerFromFile(s);
        if (jacclexer != null)
            runExample(parser.parseSymbols(jacclexer), flag);
    }

    public void runExample(int ai[], boolean flag)
    {
        Grammar grammar = parser.getGrammar();
        Parser parser1 = new Parser(tables, ai);
        out.print("start ");
        do
        {
            out.print(" :  ");
            parser1.display(out, flag);
            switch (parser1.step())
            {
            case 0: // '\0'
                out.println("Accept!");
                return;

            case 1: // '\001'
                out.print("error in state ");
                out.print(parser1.getState());
                out.print(", next symbol ");
                out.println(grammar.getSymbol(parser1.getNextSymbol()));
                return;

            case 3: // '\003'
                out.print("goto  ");
                break;

            case 2: // '\002'
                out.print("shift ");
                break;

            case 4: // '\004'
                out.print("reduce");
                break;
            }
        } while (true);
    }

    public void readErrorExamples(String s)
    {
        out.println("Reading error examples from \"" + s + "\"");
        JaccLexer jacclexer = lexerFromFile(s);
        if (jacclexer != null)
            parser.parseErrorExamples(jacclexer, this);
    }

    public void errorExample(Position position, String s, int ai[])
    {
        Parser parser1 = new Parser(tables, ai);
        int i;
        do
            i = parser1.step();
        while (i != 0 && i != 1);
        if (i == 0)
        {
            report(new Warning(position, "Example for \"" + s + "\" does not produce an error"));
        } else
        {
            Grammar grammar = tables.getMachine().getGrammar();
            int j = parser1.getNextSymbol();
            if (grammar.isNonterminal(j))
            {
                report(new Warning(position, "Example for \"" + s + "\" reaches an error at the nonterminal " + grammar.getSymbol(j)));
            } else
            {
                int k = parser1.getState();
                if (!tables.errorAt(k, j))
                {
                    report(new Failure(position, "Error example results in internal error"));
                } else
                {
                    String s1 = tables.errorSet(k, j, s);
                    if (s1 != null)
                        report(new Warning(position, "Multiple errors are possible in state " + k + " on terminal " + grammar.getSymbol(j) + ":\n - " + s1 + "\n - " + s));
                }
            }
        }
    }
}
