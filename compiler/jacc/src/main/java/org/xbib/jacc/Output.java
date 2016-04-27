package org.xbib.jacc;

import org.xbib.jacc.compiler.Failure;
import org.xbib.jacc.compiler.Handler;
import org.xbib.jacc.compiler.Phase;
import org.xbib.jacc.grammar.Grammar;
import org.xbib.jacc.grammar.Machine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;

public abstract class Output extends Phase {

    protected Grammar grammar;
    protected int numTs;
    protected int numNTs;
    int numSyms;
    protected Machine machine;
    int numStates;
    JaccTables tables;
    JaccResolver resolver;
    Settings settings;

    Output(Handler handler, JaccJob jaccjob) {
        super(handler);
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

    public void write(String s) throws IOException {
        Writer writer = null;
        try {
            File file = new File(s);
            File parent = file.getParentFile();
            if (!parent.exists()) {
                boolean mkdirs = parent.mkdirs();
            }
            writer = new FileWriter(s);
            write(writer);
        }
        catch (IOException ioexception) {
            report(new Failure("Cannot write to file \"" + s + "\""));
        }
        if (writer != null) {
            writer.close();
        }
    }

    public abstract void write(Writer writer) throws IOException;

    static void indent(Writer writer, int i, String[] as) throws IOException {
        for (String a : as) {
            indent(writer, i, a);
        }
    }

    static void indent(Writer writer, int i) throws IOException {
        for (int j = 0; j < i; j++) {
            writer.write("    ");
        }
    }

    static void indent(Writer writer, int i, String s) throws IOException {
        indent(writer, i);
        writer.write(s + "\n");
    }

    static void datestamp(Writer writer) throws IOException {
        writer.write("// Output created by jacc 2.0.0-SNAPSHOT on " + LocalDateTime.now() + "\n");
    }
}
