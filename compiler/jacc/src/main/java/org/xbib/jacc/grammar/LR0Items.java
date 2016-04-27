package org.xbib.jacc.grammar;

import java.io.IOException;
import java.io.Writer;

public class LR0Items {

    public class Item {
        private int itemNo;
        private int lhs;
        private int prodNo;
        private int pos;

        int getLhs() {
            return lhs;
        }

        int getProdNo() {
            return prodNo;
        }

        public int getSeqNo() {
            return getProd().getSeqNo();
        }

        public Grammar.Prod getProd() {
            return grammar.getProds(lhs)[prodNo];
        }

        int getPos() {
            return pos;
        }

        boolean canGoto() {
            if (lhs < 0) {
                return pos == 0;
            }
            else {
                return pos != getProd().getRhs().length;
            }
        }

        boolean canReduce()
        {
            return lhs >= 0 && pos == getProd().getRhs().length;
        }

        boolean canAccept()
        {
            return lhs < 0 && pos == 1;
        }

        int getNextItem() {
            if (lhs >= 0) {
                return itemNo + 1;
            } else {
                return 1;
            }
        }

        int getNextSym() {
            if (lhs >= 0) {
                return grammar.getProds(lhs)[prodNo].getRhs()[pos];
            }
            else {
                return 0;
            }
        }

        public void display(Writer writer) throws IOException {
            if (lhs < 0) {
                if (pos == 0) {
                    writer.write("$accept : _" + grammar.getStart() + " " + grammar.getEnd());
                }
                else {
                    writer.write("$accept : " + grammar.getStart() + "_" + grammar.getEnd());
                }
                return;
            }
            writer.write(grammar.getSymbol(lhs).toString());
            writer.write(" : ");
            Grammar.Prod prod = grammar.getProds(lhs)[prodNo];
            int[] ai = prod.getRhs();
            writer.write(grammar.displaySymbols(ai, 0, pos, "", " "));
            writer.write("_");
            if (pos < ai.length) {
                writer.write(grammar.displaySymbols(ai, pos, ai.length, "", " "));
            }
            String s = prod.getLabel();
            if (s != null) {
                writer.write("    (");
                writer.write(s);
                writer.write(')');
            }
        }

        private Item(int i, int j, int k) {
            itemNo = numItems;
            lhs = i;
            prodNo = j;
            pos = k;
            items[numItems++] = this;
        }

    }


    private Grammar grammar;
    private int numItems;
    private Item[] items;
    private int[][] firstKernel;

    LR0Items(Grammar grammar) {
        this.grammar = grammar;
        int i = grammar.getNumNTs();
        numItems = 2;
        firstKernel = new int[i][];
        for (int j = 0; j < i; j++) {
            Grammar.Prod[] aprod = grammar.getProds(j);
            firstKernel[j] = new int[aprod.length];
            for (int l = 0; l < aprod.length; l++) {
                int j1 = aprod[l].getRhs().length;
                firstKernel[j][l] = numItems;
                numItems += j1 != 0 ? j1 : 1;
            }

        }
        items = new Item[numItems];
        numItems = 0;
        new Item(-1, 0, 0);
        new Item(-1, 0, 1);
        for (int k = 0; k < i; k++) {
            Grammar.Prod[] aprod1 = grammar.getProds(k);
            for (int i1 = 0; i1 < aprod1.length; i1++) {
                int[] ai = aprod1[i1].getRhs();
                for (int k1 = 1; k1 < ai.length; k1++) {
                    new Item(k, i1, k1);
                }
                new Item(k, i1, ai.length);
            }
        }
    }

    public Item getItem(int i) {
        return items[i];
    }

    int getStartItem() {
        return 0;
    }

    int getEndItem() {
        return 1;
    }

    int getFirstKernel(int i, int j)
    {
        return firstKernel[i][j];
    }
}
