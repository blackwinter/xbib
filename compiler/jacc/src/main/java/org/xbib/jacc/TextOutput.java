package org.xbib.jacc;

import org.xbib.jacc.compiler.Handler;
import org.xbib.jacc.util.IntSet;

import java.io.IOException;
import java.io.Writer;

class TextOutput extends Output {

    private boolean wantFirst;

    TextOutput(Handler handler, JaccJob jaccjob, boolean flag) {
        super(handler, jaccjob);
        wantFirst = false;
        wantFirst = flag;
        tables.analyzeRows();
    }

    public void write(Writer writer) throws IOException {
        datestamp(writer);
        for (int i = 0; i < numStates; i++) {
            writer.write(resolver.getConflictsAt(i));
            writer.write(describeEntry(i) + "\n");
            IntSet intset = machine.getItemsAt(i);
            int k = intset.size();
            for (int i1 = 0; i1 < k; i1++) {
                indent(writer, 1);
                machine.getItems().getItem(intset.at(i1)).display(writer);
                writer.write("\n");
            }
            writer.write("\n");
            byte abyte0[] = tables.getActionAt(i);
            int ai1[] = tables.getArgAt(i);
            int j1 = tables.getDefaultRowAt(i);
            int ai2[] = tables.indexAt(i);
            for (int k1 = 0; k1 < abyte0.length; k1++) {
                int l1 = ai2[k1];
                if (j1 < 0 || abyte0[l1] != abyte0[j1] || ai1[l1] != ai1[j1]) {
                    indent(writer, 1);
                    writer.write(grammar.getTerminal(l1).getName());
                    writer.write(' ');
                    writer.write(describeAction(i, abyte0[l1], ai1[l1])+ "\n");
                }
            }
            indent(writer, 1);
            if (j1 < 0) {
                writer.write(". error"+ "\n");
            } else {
                writer.write(". ");
                writer.write(describeAction(i, abyte0[j1], ai1[j1])+ "\n");
            }
            writer.write("\n");
            int ai3[] = machine.getGotosAt(i);
            if (ai3.length <= 0)
                continue;
            for (int anAi3 : ai3) {
                int j2 = machine.getEntry(anAi3);
                indent(writer, 1);
                writer.write(grammar.getSymbol(j2).getName());
                writer.write(" " + describeGoto(anAi3) + "\n");
            }
            writer.write("\n");
        }

        if (wantFirst) {
            grammar.getFirst().display(writer);
            writer.write("\n");
            grammar.getFollow().display(writer);
            writer.write("\n");
            grammar.getNullable().display(writer);
            writer.write("\n");
        }
        if (tables.getProdUnused() > 0) {
            for (int j = 0; j < numNTs; j++) {
                boolean aflag[] = tables.getProdsUsedAt(j);
                for (int l = 0; l < aflag.length; l++) {
                    if (!aflag[l]) {
                        int ai[] = grammar.getProds(j)[l].getRhs();
                        writer.write("Rule not reduced: ");
                        writer.write(grammar.getNonterminal(j).getName());
                        writer.write(" : ");
                        writer.write(grammar.displaySymbols(ai, "", " ") + "\n");
                    }
                }
            }
            writer.write("\n");
        }
        writer.write(numTs + " terminals, " + numNTs + " nonterminals;");
        writer.write(grammar.getNumProds() + " grammar rules, " + numStates + " states;");
        writer.write(resolver.getNumSRConflicts() + " shift/reduce and " + resolver.getNumRRConflicts() + " reduce/reduce conflicts reported.");
    }

    protected String describeEntry(int i) {
        return "state " + i + " (entry on " + grammar.getSymbol(machine.getEntry(i)) + ")";
    }

    private String describeAction(int i, int j, int k) {
        if (j == 0) {
            if (k == 0) {
                return "error";
            } else {
                return "error \"" + tables.getError(k - 1) + "\"";
            }
        }
        if (j == 2) {
            return "reduce " + machine.reduceItem(i, k).getSeqNo();
        }
        if (k < 0) {
            return "accept";
        }
        else {
            return describeShift(k);
        }
    }

    protected String describeShift(int i) {
        return "shift " + i;
    }

    protected String describeGoto(int i) {
        return "goto " + i;
    }
}
