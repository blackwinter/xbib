package org.xbib.jacc;

import org.xbib.jacc.grammar.LookaheadMachine;
import org.xbib.jacc.grammar.Resolver;
import org.xbib.jacc.grammar.Tables;

import java.io.IOException;
import java.io.Writer;

class JaccTables extends Tables {

    private class RowAnalysis {
        private byte a[];
        private int b[];
        private int size;
        private int idx[];

        void analyze(int i) {
            a = action[i];
            b = arg[i]; 
            size = numTs;
            idx = new int[size];
            for (int j = 0; j < numTs; j++) {
                idx[j] = j;
            }
            for (int k = size / 2; k >= 0; k--) {
                heapify(k);
            }
            for (int l = size - 1; l > 0; l--) {
                int i1 = idx[l];
                idx[l] = idx[0];
                idx[0] = i1;
                size--;
                heapify(0);
            }
            index[i] = idx;
            defaultRow[i] = findDefault();
        }

        private void heapify(int i) {
            int j = i;
            int k = idx[j];
            do {
                int l = 2 * i + 1;
                int i1 = l + 1;
                if (l < size) {
                    int j1 = idx[l];
                    if (a[j1] > a[k] || a[j1] == a[k] && b[j1] > b[k]) {
                        j = l;
                        k = j1;
                    }
                    if (i1 < size) {
                        int k1 = idx[i1];
                        if (a[k1] > a[k] || a[k1] == a[k] && b[k1] > b[k]) {
                            j = i1;
                            k = k1;
                        }
                    }
                }
                if (j == i) {
                    return;
                }
                idx[j] = idx[i];
                idx[i] = k;
                i = j;
                k = idx[j];
            } while (true);
        }

        int findDefault() {
            int i = 1;
            int j = -1;
            int k = 0;
            do {
                if (k >= a.length) {
                    break;
                }
                int l = idx[k];
                byte byte0 = a[l];
                if (byte0 == 1) {
                    k++;
                } else {
                    int j1 = 1;
                    for (int k1 = b[l]; ++k < a.length && a[idx[k]] == byte0 && b[idx[k]] == k1;) {
                        j1++;
                    }
                    if (j1 > i) {
                        j = l;
                        i = j1;
                    }
                }
            } while (true);
            return j;
        }
    }

    private String[] errors;
    private int numErrors;
    private int[][] index;
    private int[] defaultRow;

    JaccTables(LookaheadMachine lookaheadmachine, Resolver resolver) {
        super(lookaheadmachine, resolver);
        errors = null;
        numErrors = 0;
    }

    int getNumErrors()
    {
        return numErrors;
    }

    String getError(int i)
    {
        return errors[i];
    }

    boolean errorAt(int i, int j)
    {
        return action[i][j - numNTs] == 0;
    }

    String errorSet(int i, int j, String s) {
        if (arg[i][j - numNTs] != 0) {
            return errors[arg[i][j - numNTs] - 1];
        } else {
            arg[i][j - numNTs] = errorNo(s) + 1;
            return null;
        }
    }

    private int errorNo(String s) {
        for (int i = 0; i < numErrors; i++) {
            if (errors[i].equals(s)) {
                return i;
            }
        }
        String as[] = new String[numErrors != 0 ? 2 * numErrors : 1];
        System.arraycopy(errors, 0, as, 0, numErrors);
        errors = as;
        errors[numErrors] = s;
        return numErrors++;
    }

    void analyzeRows() {
        if (index == null) {
            RowAnalysis rowanalysis = new RowAnalysis();
            int i = machine.getNumStates();
            index = new int[i][];
            defaultRow = new int[i];
            for (int j = 0; j < i; j++) {
                rowanalysis.analyze(j);
            }
        }
    }

    int[] indexAt(int i)
    {
        return index[i];
    }

    int getDefaultRowAt(int i)
    {
        return defaultRow[i];
    }

    public void display(Writer writer) throws IOException {
        int i = machine.getNumStates();
        for (int j = 0; j < i; j++) {
            writer.write("state " + j + ":\n");
            for (int k = 0; k < numTs; k++) {
                switch (action[j][k]) {
                case 0:
                    writer.write(" E");
                    break;
                case 1:
                    writer.write(" S");
                    break;
                case 2:
                    writer.write(" R");
                    break;
                }
                writer.write(arg[j][k]);
            }
            writer.write("\n");
        }
    }
}
