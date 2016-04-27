package org.xbib.jacc;

import org.xbib.jacc.compiler.Failure;
import org.xbib.jacc.compiler.Handler;
import org.xbib.jacc.grammar.Grammar;

import java.io.IOException;
import java.io.Writer;

class ParserOutput extends Output {

    private int yyaccept;
    private int yyabort;
    private int stack_overflow;
    private int error_handler;
    private int user_error_handler;
    private int stNumSwitches[];
    private int ntGoto[][];
    private int ntGotoSrc[][];
    private int ntDefault[];
    private int ntDistinct[];
    private int errTok;
    private boolean errMsgs;
    private boolean errUsed;

    ParserOutput(Handler handler, JaccJob jaccjob) {
        super(handler, jaccjob);
        errMsgs = false;
        errUsed = false;
        tables.analyzeRows();
    }

    public void write(Writer writer) throws IOException {
        datestamp(writer);
        String s = settings.getPackageName();
        if (s != null) {
            writer.write("package " + s + ";\n");
        }
        if (settings.getPreText() != null) {
            writer.write(settings.getPreText() + "\n");
        }
        yyaccept = 2 * numStates;
        stack_overflow = 2 * numStates + 1;
        yyabort = 2 * numStates + 2;
        error_handler = 2 * numStates + 3;
        user_error_handler = 2 * numStates + 4;
        int ai[] = new int[numNTs];
        stNumSwitches = new int[numStates];
        for (int i = 0; i < numStates; i++) {
            int ai1[] = machine.getGotosAt(i);
            for (int anAi1 : ai1) {
                ai[machine.getEntry(anAi1)]++;
            }
            byte abyte0[] = tables.getActionAt(i);
            int ai4[] = tables.getArgAt(i);
            int l3 = tables.getDefaultRowAt(i);
            stNumSwitches[i] = 0;
            for (int j4 = 0; j4 < abyte0.length; j4++) {
                if (l3 < 0 || abyte0[j4] != abyte0[l3] || ai4[j4] != ai4[l3]) {
                    stNumSwitches[i]++;
                }
            }
        }
        ntGoto = new int[numNTs][];
        ntGotoSrc = new int[numNTs][];
        ntDefault = new int[numNTs];
        ntDistinct = new int[numNTs];
        for (int j = 0; j < numNTs; j++) {
            ntGoto[j] = new int[ai[j]];
            ntGotoSrc[j] = new int[ai[j]];
        }
        for (int k = 0; k < numStates; k++) {
            int ai2[] = machine.getGotosAt(k);
            for (int anAi2 : ai2) {
                int j3 = machine.getEntry(anAi2);
                ntGoto[j3][--ai[j3]] = anAi2;
                ntGotoSrc[j3][ai[j3]] = k;
            }
        }
        for (int l = 0; l < numNTs; l++) {
            int l1 = -1;
            int k2 = 0;
            int k3 = ntGoto[l].length;
            for (int i4 = 0; i4 + k2 < k3; i4++) {
                int k4 = 1;
                for (int l4 = i4 + 1; l4 < k3; l4++) {
                    if (ntGoto[l][l4] == ntGoto[l][i4]) {
                        k4++;
                    }
                }
                if (k4 > k2) {
                    k2 = k4;
                    l1 = i4;
                }
            }
            ntDefault[l] = l1;
            ntDistinct[l] = ntGoto[l].length - (k2 - 1);
        }
        errMsgs = tables.getNumErrors() > 0;
        errTok = numNTs;
        while (errTok < numSyms && !grammar.getSymbol(errTok).getName().equals("error")) {
            errTok++;
        }
        if (errTok < numSyms) {
            for (int i1 = 0; i1 < numStates && !errUsed; i1++) {
                int ai3[] = machine.getShiftsAt(i1);
                for (int l2 = 0; l2 < ai3.length && !errUsed; l2++) {
                    if (machine.getEntry(ai3[l2]) == errTok) {
                        errUsed = true;
                    }
                }
            }
        }
        writer.write("public class " + settings.getClassName());
        if (settings.getExtendsName() != null) {
            writer.write(" extends " + settings.getExtendsName());
        }
        if (settings.getImplementsNames() != null) {
            writer.write(" implements " + settings.getImplementsNames());
        }
        writer.write(" {\n");
        indent(writer, 1, new String[] {
            "private int yyss = 100;", "private int yytok;", "private int yysp = 0;", "private int[] yyst;", "protected int yyerrno = (-1);"
        });
        if (errUsed) {
            indent(writer, 1, "private int yyerrstatus = 3;");
        }
        indent(writer, 1, "private " + settings.getTypeName() + "[] yysv;");
        indent(writer, 1, "private " + settings.getTypeName() + " yyrv;");
        writer.write("\n");
        defineParse(writer, 1);
        defineExpand(writer, 1);
        defineErrRec(writer, 1);
        for (int j1 = 0; j1 < numStates; j1++) {
            defineState(writer, 1, j1);
        }
        for (int k1 = 0; k1 < numNTs; k1++) {
            Grammar.Prod aprod[] = grammar.getProds(k1);
            for (Grammar.Prod anAprod : aprod) {
                defineReduce(writer, 1, anAprod, k1);
            }
            defineNonterminal(writer, 1, k1);
        }
        defineErrMsgs(writer);
        if (settings.getPostText() != null) {
            writer.write(settings.getPostText() + "\n");
        }
        writer.write("}\n");
    }

    private void defineErrMsgs(Writer writer) throws IOException {
        if (errMsgs) {
            indent(writer, 1, new String[]{
                    "private int yyerr(int e, int n) {", "    yyerrno = e;", "    return n;", "}"
            });
        }
        indent(writer, 1, "protected String[] yyerrmsgs = {");
        int i = tables.getNumErrors();
        if (i > 0) {
            for (int j = 0; j < i - 1; j++) {
                indent(writer, 2, "\"" + tables.getError(j) + "\",");
            }
            indent(writer, 2, "\"" + tables.getError(i - 1) + "\"");
        }
        indent(writer, 1, "};");
    }

    private void defineExpand(Writer writer, int i) throws IOException {
        indent(writer, i, new String[] {
            "protected void yyexpand() {", "    int[] newyyst = new int[2*yyst.length];"
        });
        indent(writer, i + 1, settings.getTypeName() + "[] newyysv = new " + settings.getTypeName() + "[2*yyst.length];");
        indent(writer, i, new String[] {
            "    for (int i=0; i<yyst.length; i++) {", "        newyyst[i] = yyst[i];", "        newyysv[i] = yysv[i];", "    }", "    yyst = newyyst;", "    yysv = newyysv;", "}"
        });
        writer.write("\n");
    }

    private void defineErrRec(Writer writer, int i) throws IOException {
        if (errUsed) {
            indent(writer, i, "public void yyerrok() {");
            indent(writer, i + 1, "yyerrstatus = 3;");
            if (errMsgs) {
                indent(writer, i + 1, "yyerrno     = (-1);");
            }
            indent(writer, i, "}");
            writer.write("\n");
            indent(writer, i, "public void yyclearin() {");
            indent(writer, i + 1, "yytok = (" + settings.getNextToken());
            indent(writer, i + 1, "        );");
            indent(writer, i, "}");
            writer.write("\n");
        }
    }

    private void defineParse(Writer writer, int i) throws IOException {
        indent(writer, i, "public boolean parse() {");
        indent(writer, i + 1, new String[] {
            "int yyn = 0;", "yysp = 0;", "yyst = new int[yyss];"
        });
        if (errUsed) {
            indent(writer, i + 1, "yyerrstatus = 3;");
        }
        if (errMsgs) {
            indent(writer, i + 1, "yyerrno = (-1);");
        }
        indent(writer, i + 1, "yysv = new " + settings.getTypeName() + "[yyss];");
        indent(writer, i + 1, "yytok = (" + settings.getGetToken());
        indent(writer, i + 1, "         );");
        indent(writer, i, new String[] {
            "loop:", "    for (;;) {", "        switch (yyn) {"
        });
        for (int j = 0; j < numStates; j++)
            stateCases(writer, i + 3, j);

        indent(writer, i + 3, "case " + yyaccept + ":");
        indent(writer, i + 4, "return true;");
        indent(writer, i + 3, "case " + stack_overflow + ":");
        indent(writer, i + 4, "yyerror(\"stack overflow\");");
        indent(writer, i + 3, "case " + yyabort + ":");
        indent(writer, i + 4, "return false;");
        errorCases(writer, i + 3);
        indent(writer, i, new String[] {
            "        }", "    }", "}"
        });
        writer.write("\n");
    }

    private void stateCases(Writer writer, int i, int j) throws IOException {
        indent(writer, i, "case " + j + ":");
        indent(writer, i + 1, "yyst[yysp] = " + j + ";");
        if (grammar.isTerminal(machine.getEntry(j))) {
            indent(writer, i + 1, "yysv[yysp] = (" + settings.getGetSemantic());
            indent(writer, i + 1, "             );");
            indent(writer, i + 1, "yytok = (" + settings.getNextToken());
            indent(writer, i + 1, "        );");
            if (errUsed)
                indent(writer, i + 1, "yyerrstatus++;");
        }
        indent(writer, i + 1, new String[] {
            "if (++yysp>=yyst.length) {", "    yyexpand();", "}"
        });
        indent(writer, i, "case " + (j + numStates) + ":");
        if (stNumSwitches[j] > 5) {
            continueTo(writer, i + 1, "yys" + j + "()", true);
        } else {
            switchState(writer, i + 1, j, true);
        }
        writer.write("\n");
    }

    private void continueTo(Writer writer, int i, String s, boolean flag) throws IOException {
        if (flag) {
            indent(writer, i, "yyn = " + s + ";");
            indent(writer, i, "continue;");
        } else {
            indent(writer, i, "return " + s + ";");
        }
    }

    private void defineState(Writer writer, int i, int j) throws IOException {
        if (stNumSwitches[j] > 5) {
            indent(writer, i, "private int yys" + j + "() {");
            switchState(writer, i + 1, j, false);
            indent(writer, i, "}");
            writer.write("\n");
        }
    }

    private void switchState(Writer writer, int i, int j, boolean flag) throws IOException {
        byte[] abyte0 = tables.getActionAt(j);
        int[] ai = tables.getArgAt(j);
        int k = tables.getDefaultRowAt(j);
        if (stNumSwitches[j] > 0) {
            indent(writer, i, "switch (yytok) {");
            int ai1[] = tables.indexAt(j);
            int k1;
            for (int l = 0; l < ai1.length; l = k1) {
                int i1 = ai1[l];
                byte byte0 = abyte0[i1];
                int j1 = ai[i1];
                k1 = l;
                ++k1;
                while (k1 < ai1.length && abyte0[ai1[k1]] == byte0 && ai[ai1[k1]] == j1) {
                    k1++;
                }
                if (k >= 0 && byte0 == abyte0[k] && j1 == ai[k]) {
                    continue;
                }
                for (int l1 = l; l1 < k1; l1++) {
                    indent(writer, i + 1);
                    writer.write("case ");
                    if (ai1[l1] == numTs - 1) {
                        writer.write("ENDINPUT");
                    }
                    else {
                        writer.write(grammar.getTerminal(ai1[l1]).getName());
                    }
                    writer.write(":\n");
                }
                continueTo(writer, i + 2, codeAction(j, byte0, j1), flag);
            }
            indent(writer, i, "}");
        }
        if (k < 0) {
            continueTo(writer, i, Integer.toString(error_handler), flag);
        } else {
            continueTo(writer, i, codeAction(j, abyte0[k], ai[k]), flag);
        }
    }

    private String codeAction(int i, int j, int k) {
        if (j == 0) {
            String s = Integer.toString(error_handler);
            return k != 0 ? "yyerr(" + (k - 1) + ", " + s + ")" : s;
        }
        if (j == 2) {
            return "yyr" + machine.reduceItem(i, k).getSeqNo() + "()";
        }
        else {
            return Integer.toString(k >= 0 ? k : yyaccept);
        }
    }

    private void defineReduce(Writer writer, int i, Grammar.Prod prod, int j) throws IOException {
        if ((prod instanceof JaccProd) && ntDefault[j] >= 0) {
            JaccProd jaccprod = (JaccProd)prod;
            indent(writer, i);
            writer.write("private int yyr" + jaccprod.getSeqNo() + "() { // ");
            writer.write(grammar.getSymbol(j).getName() + " : ");
            writer.write(grammar.displaySymbols(jaccprod.getRhs(), "/* empty */", " ") + "\n");
            String s = jaccprod.getAction();
            int k = jaccprod.getRhs().length;
            if (s != null) {
                indent(writer, i + 1);
                translateAction(writer, jaccprod, s);
                indent(writer, i + 1, "yysv[yysp-=" + k + "] = yyrv;");
            } else
            if (k > 0) {
                indent(writer, i + 1, "yysp -= " + k + ";");
            }
            gotoNonterminal(writer, i + 1, j);
            indent(writer, i, "}");
            writer.write("\n");
        }
    }

    private void translateAction(Writer writer, JaccProd jaccprod, String s) throws IOException {
        int ai[] = jaccprod.getRhs();
        int i = s.length();
        for (int j = 0; j < i; j++) {
            char c = s.charAt(j);
            if (c == '$') {
                c = s.charAt(j + 1);
                if (c == '$') {
                    j++;
                    writer.write("yyrv");
                    continue;
                }
                if (Character.isDigit(c)) {
                    int k = 0;
                    do {
                        k = k * 10 + Character.digit(c, 10);
                        j++;
                        c = s.charAt(j + 1);
                    } while (Character.isDigit(c));
                    if (k < 1 || k > ai.length) {
                        report(new Failure(jaccprod.getActionPos(), "$" + k + " cannot be used in this action."));
                        continue;
                    }
                    int l = (1 + ai.length) - k;
                    String s1 = null;
                    if (grammar.getSymbol(ai[k - 1]) instanceof JaccSymbol) {
                        JaccSymbol jaccsymbol = (JaccSymbol)grammar.getSymbol(ai[k - 1]);
                        s1 = jaccsymbol.getType();
                    }
                    if (s1 != null) {
                        writer.write("((" + s1 + ")");
                    }
                    writer.write("yysv[yysp-" + l + "]");
                    if (s1 != null) {
                        writer.write(")");
                    }
                } else {
                    writer.write('$');
                }
                continue;
            }
            if (c == '\n') {
                writer.write("\n");
            } else {
                writer.write(c);
            }
        }
        writer.write("\n");
    }

    private void gotoNonterminal(Writer writer, int i, int j) throws IOException {
        if (ntDefault[j] < 0) {
            return;
        }
        if (ntDistinct[j] == 1) {
            indent(writer, i, "return " + ntGoto[j][0] + ";");
        } else {
            if (grammar.getProds(j).length == 1) {
                nonterminalSwitch(writer, i, j);
            } else {
                indent(writer, i, "return " + ntName(j) + "();");
            }
        }
    }

    private void defineNonterminal(Writer writer, int i, int j) throws IOException {
        if (ntDefault[j] >= 0 && ntDistinct[j] != 1 && grammar.getProds(j).length != 1) {
            indent(writer, i, "private int " + ntName(j) + "() {");
            nonterminalSwitch(writer, i + 1, j);
            indent(writer, i, "}");
            writer.write("\n");
        }
    }

    private void nonterminalSwitch(Writer writer, int i, int j) throws IOException {
        int k = ntGoto[j][ntDefault[j]];
        indent(writer, i);
        writer.write("switch (yyst[yysp-1]) {\n");
        for (int l = 0; l < ntGoto[j].length; l++) {
            int i1 = ntGoto[j][l];
            if (i1 != k) {
                indent(writer, i + 1);
                writer.write("case " + ntGotoSrc[j][l]);
                writer.write(": return " + i1 + ";\n");
            }
        }
        indent(writer, i + 1);
        writer.write("default: return " + k + ";\n");
        indent(writer, i);
        writer.write("}\n");
    }

    private String ntName(int i) {
        return "yyp" + grammar.getSymbol(i).getName();
    }

    private void errorCases(Writer writer, int i) throws IOException {
        indent(writer, i, "case " + error_handler + ":");
        if (!errUsed) {
            indent(writer, i + 1, new String[] {
                "yyerror(\"syntax error\");", "return false;"
            });
            return;
        }
        indent(writer, i + 1, new String[] {
            "if (yyerrstatus>2) {", "    yyerror(\"syntax error\");", "}"
        });
        indent(writer, i, "case " + user_error_handler + " :");
        indent(writer, i + 1, new String[] {
            "if (yyerrstatus==0) {", "    if ((" + settings.getGetToken(), "         )==ENDINPUT) {", "        return false;", "    }", "    " + settings.getNextToken(), "    ;"
        });
        indent(writer, i + 2, "yyn = " + numStates + " + yyst[yysp-1];");
        indent(writer, i + 1, new String[] {
            "    continue;", "} else {", "    yyerrstatus = 0;", "    while (yysp>0) {", "        switch(yyst[yysp-1]) {"
        });
        for (int j = 0; j < numStates; j++) {
            int[] ai = machine.getShiftsAt(j);
            for (int anAi : ai) {
                if (machine.getEntry(anAi) == errTok) {
                    indent(writer, i + 4, "case " + j + ":");
                    indent(writer, i + 5, "yyn = " + anAi + ";");
                    indent(writer, i + 5, "continue loop;");
                }
            }
        }
        indent(writer, i + 1, new String[] {
            "        }", "        yysp--;", "    }", "    return false;", "}"
        });
    }
}
