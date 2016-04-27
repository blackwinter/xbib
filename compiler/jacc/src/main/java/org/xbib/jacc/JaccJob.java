package org.xbib.jacc;

import org.xbib.jacc.compiler.Failure;
import org.xbib.jacc.compiler.Handler;
import org.xbib.jacc.compiler.JavaSource;
import org.xbib.jacc.compiler.Phase;
import org.xbib.jacc.compiler.Position;
import org.xbib.jacc.compiler.Warning;
import org.xbib.jacc.grammar.Finitary;
import org.xbib.jacc.grammar.Grammar;
import org.xbib.jacc.grammar.LookaheadMachine;
import org.xbib.jacc.grammar.Parser;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

class JaccJob extends Phase {

    private Settings settings;

    private JaccParser parser;

    private JaccTables tables;

    private JaccResolver resolver;

    private Writer out;

    JaccJob(Handler handler, Writer out, Settings settings) {
        super(handler);
        this.out = out;
        this.settings = settings;
        this.parser = new JaccParser(handler, settings);
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

    private JaccLexer lexerFromFile(String s) {
        JaccLexer jacclexer;
        try {
            FileReader filereader = new FileReader(s);
            jacclexer = new JaccLexer(getHandler(), new JavaSource(getHandler(), s, filereader));
            jacclexer.nextToken();
            return jacclexer;
        } catch (IOException e) {
            report(new Failure("Could not open file \"" + s + "\""));
            return null;
        }
    }

    void parseGrammarFile(String s) throws IOException {
        JaccLexer jacclexer = lexerFromFile(s);
        if (jacclexer != null) {
            parser.parse(jacclexer);
        }
    }

    void buildTables() {
        Grammar grammar = parser.getGrammar();
        if (grammar == null || !allDeriveFinite(grammar)) {
            return;
        }
        LookaheadMachine lookaheadmachine = settings.makeMachine(grammar);
        resolver = new JaccResolver(lookaheadmachine);
        tables = new JaccTables(lookaheadmachine, resolver);
        if (tables.getProdUnused() > 0)
            report(new Warning(tables.getProdUnused() + " rules never reduced"));
        if (resolver.getNumSRConflicts() > 0 || resolver.getNumRRConflicts() > 0)
            report(new Warning("conflicts: " + resolver.getNumSRConflicts() + " shift/reduce, " + resolver.getNumRRConflicts() + " reduce/reduce"));
    }

    private boolean allDeriveFinite(Grammar grammar) {
        Finitary finitary = grammar.getFinitary();
        boolean flag = true;
        for (int i = 0; i < grammar.getNumNTs(); i++) {
            if (!finitary.at(i)) {
                flag = false;
                report(new Failure("No finite strings can be derived for " + grammar.getNonterminal(i)));
            }
        }
        return flag;
    }

    void readRunExample(String s, boolean flag) throws IOException {
        out.write("Running example from \"" + s + "\"]\n");
        JaccLexer jacclexer = lexerFromFile(s);
        if (jacclexer != null) {
            runExample(parser.parseSymbols(jacclexer), flag);
        }
    }

    private void runExample(int ai[], boolean flag) throws IOException{
        Grammar grammar = parser.getGrammar();
        Parser parser1 = new Parser(tables, ai);
        out.write("start ");
        do {
            out.write(" :  ");
            parser1.display(out, flag);
            switch (parser1.step()) {
            case 0:
                out.write("Accept!\n");
                return;
            case 1:
                out.write("error in state ");
                out.write(parser1.getState());
                out.write(", next symbol ");
                out.write(grammar.getSymbol(parser1.getNextSymbol()).toString());
                return;
            case 3:
                out.write("goto  ");
                break;
            case 2:
                out.write("shift ");
                break;
            case 4:
                out.write("reduce");
                break;
            }
        } while (true);
    }

    void readErrorExamples(String s) throws IOException {
        out.write("Reading error examples from \"" + s + "\"");
        JaccLexer jacclexer = lexerFromFile(s);
        if (jacclexer != null) {
            parser.parseErrorExamples(jacclexer, this);
        }
    }

    void errorExample(Position position, String s, int ai[]) {
        Parser parser1 = new Parser(tables, ai);
        int i;
        do {
            i = parser1.step();
        } while (i != 0 && i != 1);
        if (i == 0) {
            report(new Warning(position, "Example for \"" + s + "\" does not produce an error"));
        } else {
            Grammar grammar = tables.getMachine().getGrammar();
            int j = parser1.getNextSymbol();
            if (grammar.isNonterminal(j)) {
                report(new Warning(position, "Example for \"" + s + "\" reaches an error at the nonterminal " + grammar.getSymbol(j)));
            } else {
                int k = parser1.getState();
                if (!tables.errorAt(k, j)) {
                    report(new Failure(position, "Error example results in internal error"));
                } else {
                    String s1 = tables.errorSet(k, j, s);
                    if (s1 != null) {
                        report(new Warning(position, "Multiple errors are possible in state " + k + " on terminal " + grammar.getSymbol(j) + ":\n - " + s1 + "\n - " + s));
                    }
                }
            }
        }
    }
}
