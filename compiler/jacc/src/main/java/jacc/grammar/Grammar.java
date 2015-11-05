package jacc.grammar;

import jacc.util.BitSet;
import jacc.util.Interator;
import jacc.util.SCC;

import java.io.PrintWriter;

public class Grammar {
    public static class Prod {

        protected int rhs[];
        private int seqNo;

        public int[] getRhs() {
            return rhs;
        }

        public int getSeqNo() {
            return seqNo;
        }

        public String getLabel() {
            return null;
        }

        public Prod(int ai[], int i) {
            rhs = ai;
            seqNo = i;
        }
    }

    public static class Symbol {

        protected String name;

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        public Symbol(String s) {
            name = s;
        }
    }


    private Symbol symbols[];
    private Prod prods[][];
    private int numSyms;
    private int numNTs;
    private int numTs;
    private int comps[][];
    private int depends[][];
    private int revdeps[][];
    private Nullable nullable;
    private Finitary finitary;
    private Left left;
    private First first;
    private Follow follow;

    public Grammar(Symbol asymbol[], Prod aprod[][])
            throws Exception {
        validate(asymbol, aprod);
        symbols = asymbol;
        numSyms = asymbol.length;
        prods = aprod;
        numNTs = aprod.length;
        numTs = numSyms - numNTs;
        calcDepends();
        comps = SCC.get(depends, revdeps, numNTs);
    }

    public int getNumSyms() {
        return numSyms;
    }

    public int getNumNTs() {
        return numNTs;
    }

    public int getNumTs() {
        return numTs;
    }

    public Symbol getSymbol(int i) {
        return symbols[i];
    }

    public Symbol getStart() {
        return symbols[0];
    }

    public Symbol getEnd() {
        return symbols[numSyms - 1];
    }

    public Symbol getNonterminal(int i) {
        return symbols[i];
    }

    public Symbol getTerminal(int i) {
        return symbols[numNTs + i];
    }

    public boolean isNonterminal(int i) {
        return 0 <= i && i < numNTs;
    }

    public boolean isTerminal(int i) {
        return numNTs <= i && i < numSyms;
    }

    public int getNumProds() {
        int i = 0;
        for (int j = 0; j < prods.length; j++) {
            i += prods[j].length;
        }

        return i;
    }

    public Prod[] getProds(int i) {
        return prods[i];
    }

    public int[][] getComponents() {
        return comps;
    }

    public static void validate(Symbol asymbol[], Prod aprod[][])
            throws Exception {
        if (asymbol == null || asymbol.length == 0) {
            throw new Exception("No symbols specified");
        }
        for (int i = 0; i < asymbol.length; i++) {
            if (asymbol[i] == null) {
                throw new Exception("Symbol " + i + " is null");
            }
        }

        int j = asymbol.length;
        if (aprod == null || aprod.length == 0) {
            throw new Exception("No nonterminals specified");
        }
        if (aprod.length > j) {
            throw new Exception("To many nonterminals specified");
        }
        if (aprod.length == j) {
            throw new Exception("No terminals specified");
        }
        for (int k = 0; k < aprod.length; k++) {
            if (aprod[k] == null || aprod[k].length == 0) {
                throw new Exception("Nonterminal " + asymbol[k] + " (number " + k + ") has no productions");
            }
            for (int l = 0; l < aprod[k].length; l++) {
                int ai[] = aprod[k][l].getRhs();
                if (ai == null) {
                    throw new Exception("Production " + l + " for symbol " + asymbol[k] + " (number " + k + ") is null");
                }
                for (int i1 = 0; i1 < ai.length; i1++) {
                    if (ai[i1] < 0 || ai[i1] >= j - 1) {
                        throw new Exception("Out of range symbol " + ai[i1] + " in production " + l + " for symbol " + asymbol[k] + " (number " + k + ")");
                    }
                }

            }

        }

    }

    private void calcDepends() {
        int ai[][] = new int[numNTs][];
        int ai1[] = BitSet.make(numNTs);
        depends = new int[numNTs][];
        for (int i = 0; i < numNTs; i++) {
            ai[i] = BitSet.make(numNTs);
        }

        for (int j = 0; j < numNTs; j++) {
            BitSet.clear(ai1);
            for (int l = 0; l < prods[j].length; l++) {
                int ai2[] = prods[j][l].getRhs();
                for (int i1 = 0; i1 < ai2.length; i1++) {
                    if (isNonterminal(ai2[i1])) {
                        BitSet.set(ai[ai2[i1]], j);
                        BitSet.set(ai1, ai2[i1]);
                    }
                }

            }

            depends[j] = BitSet.members(ai1);
        }

        revdeps = new int[numNTs][];
        for (int k = 0; k < numNTs; k++) {
            revdeps[k] = BitSet.members(ai[k]);
        }

    }

    public Nullable getNullable() {
        if (nullable == null) {
            nullable = new Nullable(this);
        }
        return nullable;
    }

    public Finitary getFinitary() {
        if (finitary == null) {
            finitary = new Finitary(this);
        }
        return finitary;
    }

    public Left getLeft() {
        if (left == null) {
            left = new Left(this);
        }
        return left;
    }

    public First getFirst() {
        if (first == null) {
            first = new First(this, getNullable());
        }
        return first;
    }

    public Follow getFollow() {
        if (follow == null) {
            follow = new Follow(this, getNullable(), getFirst());
        }
        return follow;
    }

    public void display(PrintWriter printwriter) {
        for (int i = 0; i < numNTs; i++) {
            printwriter.println(symbols[i].getName());
            String s = " = ";
            for (int j = 0; j < prods[i].length; j++) {
                int ai[] = prods[i][j].getRhs();
                printwriter.print(s);
                printwriter.print(displaySymbols(ai, "/* empty */", " "));
                printwriter.println();
                s = " | ";
            }

            printwriter.println(" ;");
        }

    }

    public void displayAnalyses(PrintWriter printwriter) {
        if (nullable == null) {
            printwriter.println("No nullable analysis");
        } else {
            nullable.display(printwriter);
        }
        if (finitary == null) {
            printwriter.println("No finitary analysis");
        } else {
            finitary.display(printwriter);
        }
        if (left == null) {
            printwriter.println("No left analysis");
        } else {
            left.display(printwriter);
        }
        if (first == null) {
            printwriter.println("No first analysis");
        } else {
            first.display(printwriter);
        }
        if (follow == null) {
            printwriter.println("No follow analysis");
        } else {
            follow.display(printwriter);
        }
    }

    public void displayDepends(PrintWriter printwriter) {
        printwriter.println("Dependency information:");
        for (int i = 0; i < numNTs; i++) {
            printwriter.print(" " + symbols[i] + ": calls {");
            printwriter.print(displaySymbols(depends[i], "", ", "));
            printwriter.print("}, called from {");
            printwriter.print(displaySymbols(revdeps[i], "", ", "));
            printwriter.println("}");
        }

    }

    public String displaySymbols(int ai[], String s, String s1) {
        return displaySymbols(ai, 0, ai.length, s, s1);
    }

    public String displaySymbols(int ai[], int i, int j, String s, String s1) {
        if (ai == null || i >= j) {
            return s;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(symbols[ai[i]].getName());
        for (int k = i + 1; k < j; k++) {
            sb.append(s1);
            sb.append(symbols[ai[k]].getName());
        }

        return sb.toString();
    }

    public String displaySymbolSet(int ai[], int i) {
        StringBuffer stringbuffer = new StringBuffer();
        int j = 0;
        for (Interator interator = BitSet.interator(ai, i); interator.hasNext(); stringbuffer.append(symbols[interator.next()].getName())) {
            if (j++ != 0) {
                stringbuffer.append(", ");
            }
        }

        return stringbuffer.toString();
    }
}
