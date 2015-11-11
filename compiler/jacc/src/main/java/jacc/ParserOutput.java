package jacc;

import compiler.Failure;
import compiler.Handler;
import jacc.grammar.Grammar;
import jacc.grammar.LR0Items;
import jacc.grammar.Machine;
import java.io.PrintWriter;

public class ParserOutput extends Output
{

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

    public ParserOutput(Handler handler, JaccJob jaccjob)
    {
        super(handler, jaccjob);
        errMsgs = false;
        errUsed = false;
        tables.analyzeRows();
    }

    public void write(PrintWriter printwriter)
    {
        datestamp(printwriter);
        String s = settings.getPackageName();
        if (s != null)
            printwriter.println("package " + s + ";");
        if (settings.getPreText() != null)
            printwriter.println(settings.getPreText());
        yyaccept = 2 * numStates;
        stack_overflow = 2 * numStates + 1;
        yyabort = 2 * numStates + 2;
        error_handler = 2 * numStates + 3;
        user_error_handler = 2 * numStates + 4;
        int ai[] = new int[numNTs];
        stNumSwitches = new int[numStates];
        for (int i = 0; i < numStates; i++)
        {
            int ai1[] = machine.getGotosAt(i);
            for (int i2 = 0; i2 < ai1.length; i2++)
                ai[machine.getEntry(ai1[i2])]++;

            byte abyte0[] = tables.getActionAt(i);
            int ai4[] = tables.getArgAt(i);
            int l3 = tables.getDefaultRowAt(i);
            stNumSwitches[i] = 0;
            for (int j4 = 0; j4 < abyte0.length; j4++)
                if (l3 < 0 || abyte0[j4] != abyte0[l3] || ai4[j4] != ai4[l3])
                    stNumSwitches[i]++;

        }

        ntGoto = new int[numNTs][];
        ntGotoSrc = new int[numNTs][];
        ntDefault = new int[numNTs];
        ntDistinct = new int[numNTs];
        for (int j = 0; j < numNTs; j++)
        {
            ntGoto[j] = new int[ai[j]];
            ntGotoSrc[j] = new int[ai[j]];
        }

        for (int k = 0; k < numStates; k++)
        {
            int ai2[] = machine.getGotosAt(k);
            for (int j2 = 0; j2 < ai2.length; j2++)
            {
                int j3 = machine.getEntry(ai2[j2]);
                ntGoto[j3][--ai[j3]] = ai2[j2];
                ntGotoSrc[j3][ai[j3]] = k;
            }

        }

        for (int l = 0; l < numNTs; l++)
        {
            int l1 = -1;
            int k2 = 0;
            int k3 = ntGoto[l].length;
            for (int i4 = 0; i4 + k2 < k3; i4++)
            {
                int k4 = 1;
                for (int l4 = i4 + 1; l4 < k3; l4++)
                    if (ntGoto[l][l4] == ntGoto[l][i4])
                        k4++;

                if (k4 > k2)
                {
                    k2 = k4;
                    l1 = i4;
                }
            }

            ntDefault[l] = l1;
            ntDistinct[l] = ntGoto[l].length - (k2 - 1);
        }

        errMsgs = tables.getNumErrors() > 0;
        for (errTok = numNTs; errTok < numSyms && !grammar.getSymbol(errTok).getName().equals("error"); errTok++);
        if (errTok < numSyms)
        {
            for (int i1 = 0; i1 < numStates && !errUsed; i1++)
            {
                int ai3[] = machine.getShiftsAt(i1);
                for (int l2 = 0; l2 < ai3.length && !errUsed; l2++)
                    if (machine.getEntry(ai3[l2]) == errTok)
                        errUsed = true;

            }

        }
        printwriter.print("public class " + settings.getClassName());
        if (settings.getExtendsName() != null)
            printwriter.print(" extends " + settings.getExtendsName());
        if (settings.getImplementsNames() != null)
            printwriter.print(" implements " + settings.getImplementsNames());
        printwriter.println(" {");
        indent(printwriter, 1, new String[] {
            "private int yyss = 100;", "private int yytok;", "private int yysp = 0;", "private int[] yyst;", "protected int yyerrno = (-1);"
        });
        if (errUsed)
            indent(printwriter, 1, "private int yyerrstatus = 3;");
        indent(printwriter, 1, "private " + settings.getTypeName() + "[] yysv;");
        indent(printwriter, 1, "private " + settings.getTypeName() + " yyrv;");
        printwriter.println();
        defineParse(printwriter, 1);
        defineExpand(printwriter, 1);
        defineErrRec(printwriter, 1);
        for (int j1 = 0; j1 < numStates; j1++)
            defineState(printwriter, 1, j1);

        for (int k1 = 0; k1 < numNTs; k1++)
        {
            jacc.grammar.Grammar.Prod aprod[] = grammar.getProds(k1);
            for (int i3 = 0; i3 < aprod.length; i3++)
                defineReduce(printwriter, 1, aprod[i3], k1);

            defineNonterminal(printwriter, 1, k1);
        }

        defineErrMsgs(printwriter);
        if (settings.getPostText() != null)
            printwriter.println(settings.getPostText());
        printwriter.println("}");
    }

    private void defineErrMsgs(PrintWriter printwriter)
    {
        if (errMsgs)
            indent(printwriter, 1, new String[] {
                "private int yyerr(int e, int n) {", "    yyerrno = e;", "    return n;", "}"
            });
        indent(printwriter, 1, "protected String[] yyerrmsgs = {");
        int i = tables.getNumErrors();
        if (i > 0)
        {
            for (int j = 0; j < i - 1; j++)
                indent(printwriter, 2, "\"" + tables.getError(j) + "\",");

            indent(printwriter, 2, "\"" + tables.getError(i - 1) + "\"");
        }
        indent(printwriter, 1, "};");
    }

    private void defineExpand(PrintWriter printwriter, int i)
    {
        indent(printwriter, i, new String[] {
            "protected void yyexpand() {", "    int[] newyyst = new int[2*yyst.length];"
        });
        indent(printwriter, i + 1, settings.getTypeName() + "[] newyysv = new " + settings.getTypeName() + "[2*yyst.length];");
        indent(printwriter, i, new String[] {
            "    for (int i=0; i<yyst.length; i++) {", "        newyyst[i] = yyst[i];", "        newyysv[i] = yysv[i];", "    }", "    yyst = newyyst;", "    yysv = newyysv;", "}"
        });
        printwriter.println();
    }

    private void defineErrRec(PrintWriter printwriter, int i)
    {
        if (errUsed)
        {
            indent(printwriter, i, "public void yyerrok() {");
            indent(printwriter, i + 1, "yyerrstatus = 3;");
            if (errMsgs)
                indent(printwriter, i + 1, "yyerrno     = (-1);");
            indent(printwriter, i, "}");
            printwriter.println();
            indent(printwriter, i, "public void yyclearin() {");
            indent(printwriter, i + 1, "yytok = (" + settings.getNextToken());
            indent(printwriter, i + 1, "        );");
            indent(printwriter, i, "}");
            printwriter.println();
        }
    }

    private void defineParse(PrintWriter printwriter, int i)
    {
        indent(printwriter, i, "public boolean parse() {");
        indent(printwriter, i + 1, new String[] {
            "int yyn = 0;", "yysp = 0;", "yyst = new int[yyss];"
        });
        if (errUsed)
            indent(printwriter, i + 1, "yyerrstatus = 3;");
        if (errMsgs)
            indent(printwriter, i + 1, "yyerrno = (-1);");
        indent(printwriter, i + 1, "yysv = new " + settings.getTypeName() + "[yyss];");
        indent(printwriter, i + 1, "yytok = (" + settings.getGetToken());
        indent(printwriter, i + 1, "         );");
        indent(printwriter, i, new String[] {
            "loop:", "    for (;;) {", "        switch (yyn) {"
        });
        for (int j = 0; j < numStates; j++)
            stateCases(printwriter, i + 3, j);

        indent(printwriter, i + 3, "case " + yyaccept + ":");
        indent(printwriter, i + 4, "return true;");
        indent(printwriter, i + 3, "case " + stack_overflow + ":");
        indent(printwriter, i + 4, "yyerror(\"stack overflow\");");
        indent(printwriter, i + 3, "case " + yyabort + ":");
        indent(printwriter, i + 4, "return false;");
        errorCases(printwriter, i + 3);
        indent(printwriter, i, new String[] {
            "        }", "    }", "}"
        });
        printwriter.println();
    }

    private void stateCases(PrintWriter printwriter, int i, int j)
    {
        indent(printwriter, i, "case " + j + ":");
        indent(printwriter, i + 1, "yyst[yysp] = " + j + ";");
        if (grammar.isTerminal(machine.getEntry(j)))
        {
            indent(printwriter, i + 1, "yysv[yysp] = (" + settings.getGetSemantic());
            indent(printwriter, i + 1, "             );");
            indent(printwriter, i + 1, "yytok = (" + settings.getNextToken());
            indent(printwriter, i + 1, "        );");
            if (errUsed)
                indent(printwriter, i + 1, "yyerrstatus++;");
        }
        indent(printwriter, i + 1, new String[] {
            "if (++yysp>=yyst.length) {", "    yyexpand();", "}"
        });
        indent(printwriter, i, "case " + (j + numStates) + ":");
        if (stNumSwitches[j] > 5)
            continueTo(printwriter, i + 1, "yys" + j + "()", true);
        else
            switchState(printwriter, i + 1, j, true);
        printwriter.println();
    }

    private void continueTo(PrintWriter printwriter, int i, String s, boolean flag)
    {
        if (flag)
        {
            indent(printwriter, i, "yyn = " + s + ";");
            indent(printwriter, i, "continue;");
        } else
        {
            indent(printwriter, i, "return " + s + ";");
        }
    }

    private void defineState(PrintWriter printwriter, int i, int j)
    {
        if (stNumSwitches[j] > 5)
        {
            indent(printwriter, i, "private int yys" + j + "() {");
            switchState(printwriter, i + 1, j, false);
            indent(printwriter, i, "}");
            printwriter.println();
        }
    }

    private void switchState(PrintWriter printwriter, int i, int j, boolean flag)
    {
        byte abyte0[] = tables.getActionAt(j);
        int ai[] = tables.getArgAt(j);
        int k = tables.getDefaultRowAt(j);
        if (stNumSwitches[j] > 0)
        {
            indent(printwriter, i, "switch (yytok) {");
            int ai1[] = tables.indexAt(j);
            int k1;
            for (int l = 0; l < ai1.length; l = k1)
            {
                int i1 = ai1[l];
                byte byte0 = abyte0[i1];
                int j1 = ai[i1];
                for (k1 = l; ++k1 < ai1.length && abyte0[ai1[k1]] == byte0 && ai[ai1[k1]] == j1;);
                if (k >= 0 && byte0 == abyte0[k] && j1 == ai[k])
                    continue;
                for (int l1 = l; l1 < k1; l1++)
                {
                    indent(printwriter, i + 1);
                    printwriter.print("case ");
                    if (ai1[l1] == numTs - 1)
                        printwriter.print("ENDINPUT");
                    else
                        printwriter.print(grammar.getTerminal(ai1[l1]).getName());
                    printwriter.println(":");
                }

                continueTo(printwriter, i + 2, codeAction(j, byte0, j1), flag);
            }

            indent(printwriter, i, "}");
        }
        if (k < 0)
            continueTo(printwriter, i, Integer.toString(error_handler), flag);
        else
            continueTo(printwriter, i, codeAction(j, abyte0[k], ai[k]), flag);
    }

    private String codeAction(int i, int j, int k)
    {
        if (j == 0)
        {
            String s = Integer.toString(error_handler);
            return k != 0 ? "yyerr(" + (k - 1) + ", " + s + ")" : s;
        }
        if (j == 2)
            return "yyr" + machine.reduceItem(i, k).getSeqNo() + "()";
        else
            return Integer.toString(k >= 0 ? k : yyaccept);
    }

    private void gotoReduce(PrintWriter printwriter, int i, int j, int k)
    {
        indent(printwriter, i, "return yyr" + machine.reduceItem(j, k).getSeqNo() + "();");
    }

    private void defineReduce(PrintWriter printwriter, int i, jacc.grammar.Grammar.Prod prod, int j)
    {
        if ((prod instanceof JaccProd) && ntDefault[j] >= 0)
        {
            JaccProd jaccprod = (JaccProd)prod;
            indent(printwriter, i);
            printwriter.print("private int yyr" + jaccprod.getSeqNo() + "() { // ");
            printwriter.print(grammar.getSymbol(j).getName() + " : ");
            printwriter.println(grammar.displaySymbols(jaccprod.getRhs(), "/* empty */", " "));
            String s = jaccprod.getAction();
            int k = jaccprod.getRhs().length;
            if (s != null)
            {
                indent(printwriter, i + 1);
                translateAction(printwriter, jaccprod, s);
                indent(printwriter, i + 1, "yysv[yysp-=" + k + "] = yyrv;");
            } else
            if (k > 0)
                indent(printwriter, i + 1, "yysp -= " + k + ";");
            gotoNonterminal(printwriter, i + 1, j);
            indent(printwriter, i, "}");
            printwriter.println();
        }
    }

    private void translateAction(PrintWriter printwriter, JaccProd jaccprod, String s)
    {
        int ai[] = jaccprod.getRhs();
        int i = s.length();
        for (int j = 0; j < i; j++)
        {
            char c = s.charAt(j);
            if (c == '$')
            {
                c = s.charAt(j + 1);
                if (c == '$')
                {
                    j++;
                    printwriter.print("yyrv");
                    continue;
                }
                if (Character.isDigit(c))
                {
                    int k = 0;
                    do
                    {
                        k = k * 10 + Character.digit(c, 10);
                        j++;
                        c = s.charAt(j + 1);
                    } while (Character.isDigit(c));
                    if (k < 1 || k > ai.length)
                    {
                        report(new Failure(jaccprod.getActionPos(), "$" + k + " cannot be used in this action."));
                        continue;
                    }
                    int l = (1 + ai.length) - k;
                    String s1 = null;
                    if (grammar.getSymbol(ai[k - 1]) instanceof JaccSymbol)
                    {
                        JaccSymbol jaccsymbol = (JaccSymbol)grammar.getSymbol(ai[k - 1]);
                        s1 = jaccsymbol.getType();
                    }
                    if (s1 != null)
                        printwriter.print("((" + s1 + ")");
                    printwriter.print("yysv[yysp-" + l + "]");
                    if (s1 != null)
                        printwriter.print(")");
                } else
                {
                    printwriter.print('$');
                }
                continue;
            }
            if (c == '\n')
                printwriter.println();
            else
                printwriter.print(c);
        }

        printwriter.println();
    }

    private void gotoNonterminal(PrintWriter printwriter, int i, int j)
    {
        if (ntDefault[j] < 0)
            return;
        if (ntDistinct[j] == 1)
            indent(printwriter, i, "return " + ntGoto[j][0] + ";");
        else
        if (grammar.getProds(j).length == 1)
            nonterminalSwitch(printwriter, i, j);
        else
            indent(printwriter, i, "return " + ntName(j) + "();");
    }

    private void defineNonterminal(PrintWriter printwriter, int i, int j)
    {
        if (ntDefault[j] >= 0 && ntDistinct[j] != 1 && grammar.getProds(j).length != 1)
        {
            indent(printwriter, i, "private int " + ntName(j) + "() {");
            nonterminalSwitch(printwriter, i + 1, j);
            indent(printwriter, i, "}");
            printwriter.println();
        }
    }

    private void nonterminalSwitch(PrintWriter printwriter, int i, int j)
    {
        int k = ntGoto[j][ntDefault[j]];
        indent(printwriter, i);
        printwriter.println("switch (yyst[yysp-1]) {");
        for (int l = 0; l < ntGoto[j].length; l++)
        {
            int i1 = ntGoto[j][l];
            if (i1 != k)
            {
                indent(printwriter, i + 1);
                printwriter.print("case " + ntGotoSrc[j][l]);
                printwriter.println(": return " + i1 + ";");
            }
        }

        indent(printwriter, i + 1);
        printwriter.println("default: return " + k + ";");
        indent(printwriter, i);
        printwriter.println("}");
    }

    private String ntName(int i)
    {
        return "yyp" + grammar.getSymbol(i).getName();
    }

    private void errorCases(PrintWriter printwriter, int i)
    {
        indent(printwriter, i, "case " + error_handler + ":");
        if (!errUsed)
        {
            indent(printwriter, i + 1, new String[] {
                "yyerror(\"syntax error\");", "return false;"
            });
            return;
        }
        indent(printwriter, i + 1, new String[] {
            "if (yyerrstatus>2) {", "    yyerror(\"syntax error\");", "}"
        });
        indent(printwriter, i, "case " + user_error_handler + " :");
        indent(printwriter, i + 1, new String[] {
            "if (yyerrstatus==0) {", "    if ((" + settings.getGetToken(), "         )==ENDINPUT) {", "        return false;", "    }", "    " + settings.getNextToken(), "    ;"
        });
        indent(printwriter, i + 2, "yyn = " + numStates + " + yyst[yysp-1];");
        indent(printwriter, i + 1, new String[] {
            "    continue;", "} else {", "    yyerrstatus = 0;", "    while (yysp>0) {", "        switch(yyst[yysp-1]) {"
        });
        for (int j = 0; j < numStates; j++)
        {
            int ai[] = machine.getShiftsAt(j);
            for (int k = 0; k < ai.length; k++)
                if (machine.getEntry(ai[k]) == errTok)
                {
                    indent(printwriter, i + 4, "case " + j + ":");
                    indent(printwriter, i + 5, "yyn = " + ai[k] + ";");
                    indent(printwriter, i + 5, "continue loop;");
                }

        }

        indent(printwriter, i + 1, new String[] {
            "        }", "        yysp--;", "    }", "    return false;", "}"
        });
    }
}
