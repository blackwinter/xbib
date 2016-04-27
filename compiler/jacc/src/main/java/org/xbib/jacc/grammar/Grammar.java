package org.xbib.jacc.grammar;

import org.xbib.jacc.util.BitSet;
import org.xbib.jacc.util.Interator;
import org.xbib.jacc.util.SCC;

import java.io.IOException;
import java.io.Writer;

public class Grammar {
    public static class Prod {

        int[] rhs;
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

        public Prod(int[] ai, int i) {
            rhs = ai;
            seqNo = i;
        }
    }

    public static class Symbol {

        String name;

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


    private final Symbol[] symbols;
    private final Prod[][] prods;
    private int numSyms;
    private int numNTs;
    private int numTs;
    private int[][] comps;
    private int[][] depends;
    private int[][] revdeps;
    private Nullable nullable;
    private Finitary finitary;
    private Left left;
    private First first;
    private Follow follow;

    public Grammar(Symbol[] symbols, Prod[][] prods) throws Exception {
        validate(symbols, prods);
        this.symbols = symbols;
        numSyms = symbols.length;
        this.prods = prods;
        numNTs = prods.length;
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

    Symbol getStart() {
        return symbols[0];
    }

    Symbol getEnd() {
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
        for (Prod[] prod : prods) {
            i += prod.length;
        }
        return i;
    }

    public Prod[] getProds(int i) {
        return prods[i];
    }

    int[][] getComponents() {
        return comps;
    }

    private static void validate(Symbol[] symbol, Prod[][] prod) throws Exception {
        if (symbol == null || symbol.length == 0) {
            throw new Exception("No symbols specified");
        }
        for (int i = 0; i < symbol.length; i++) {
            if (symbol[i] == null) {
                throw new Exception("Symbol " + i + " is null");
            }
        }
        int j = symbol.length;
        if (prod == null || prod.length == 0) {
            throw new Exception("No nonterminals specified");
        }
        if (prod.length > j) {
            throw new Exception("To many nonterminals specified");
        }
        if (prod.length == j) {
            throw new Exception("No terminals specified");
        }
        for (int k = 0; k < prod.length; k++) {
            if (prod[k] == null || prod[k].length == 0) {
                throw new Exception("Nonterminal " + symbol[k] + " (number " + k + ") has no productions");
            }
            for (int l = 0; l < prod[k].length; l++) {
                int[] ai = prod[k][l].getRhs();
                if (ai == null) {
                    throw new Exception("Production " + l + " for symbol " + symbol[k] + " (number " + k + ") is null");
                }
                for (int m : ai) {
                    if (m < 0 || m >= j - 1) {
                        throw new Exception("Out of range symbol " + m + " in production " + l + " for symbol " + symbol[k] + " (number " + k + ")");
                    }
                }
            }
        }
    }

    private void calcDepends() {
        int[][] ai = new int[numNTs][];
        int[] ai1 = BitSet.make(numNTs);
        depends = new int[numNTs][];
        for (int i = 0; i < numNTs; i++) {
            ai[i] = BitSet.make(numNTs);
        }
        for (int j = 0; j < numNTs; j++) {
            BitSet.clear(ai1);
            for (int l = 0; l < prods[j].length; l++) {
                int[] ai2 = prods[j][l].getRhs();
                for (int anAi2 : ai2) {
                    if (isNonterminal(anAi2)) {
                        BitSet.set(ai[anAi2], j);
                        BitSet.set(ai1, anAi2);
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

    public void display(Writer writer) throws IOException {
        for (int i = 0; i < numNTs; i++) {
            writer.write(symbols[i].getName() + "\n");
            String s = " = ";
            for (int j = 0; j < prods[i].length; j++) {
                int[] ai = prods[i][j].getRhs();
                writer.write(s);
                writer.write(displaySymbols(ai, "/* empty */", " ") + "\n");
                s = " | ";
            }
            writer.write(" ;\n");
        }
    }

    public String displaySymbols(int[] ai, String s, String s1) {
        return displaySymbols(ai, 0, ai.length, s, s1);
    }

    String displaySymbols(int[] ai, int i, int j, String s, String s1) {
        if (ai == null || i >= j) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(symbols[ai[i]].getName());
        for (int k = i + 1; k < j; k++) {
            sb.append(s1);
            sb.append(symbols[ai[k]].getName());
        }
        return sb.toString();
    }

    String displaySymbolSet(int[] ai, int i) {
        StringBuilder sb = new StringBuilder();
        int j = 0;
        for (Interator interator = BitSet.interator(ai, i); interator.hasNext(); sb.append(symbols[interator.next()].getName())) {
            if (j++ != 0) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
