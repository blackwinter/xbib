package org.xbib.jacc.grammar;

import java.io.IOException;
import java.io.Writer;

public class Parser {
    public static class Stack {

        private int state;
        private int symbol;
        private Stack up;
        private Stack down;

        int getState()
        {
            return state;
        }

        public int getSymbol()
        {
            return symbol;
        }

        Stack pop()
        {
            return down;
        }

        Stack push(int i, int j)
        {
            Stack stack1 = up;
            if (stack1 == null)
                stack1 = up = new Stack(this);
            stack1.state = i;
            stack1.symbol = j;
            return stack1;
        }

        public void display(Writer writer, Grammar grammar1, boolean flag) throws IOException {
            if (down != null) {
                down.display(writer, grammar1, flag);
                if (flag) {
                    writer.write(state);
                    writer.write(" ");
                }
                writer.write(grammar1.getSymbol(symbol).toString());
                writer.write(" ");
            }
        }

        Stack() {
            this(null);
        }

        private Stack(Stack stack) {
            down = stack;
            up = null;
        }
    }

    private Tables tables;
    private int input[];
    private Machine machine;
    private Grammar grammar;
    private int position;
    private int currSymbol;
    private int reducedNT;
    private Stack stack;
    private int state;
    private static final int ACCEPT = 0;
    private static final int ERROR = 1;
    private static final int SHIFT = 2;
    private static final int GOTO = 3;
    private static final int REDUCE = 4;

    public Parser(Tables tables, int ai[]) {
        position = 0;
        currSymbol = -1;
        reducedNT = -1;
        stack = new Stack();
        state = ACCEPT;
        this.tables = tables;
        input = ai;
        machine = tables.getMachine();
        grammar = machine.getGrammar();
    }

    public int getState()
    {
        return state;
    }

    public int getNextSymbol()
    {
        return reducedNT < 0 ? currSymbol : reducedNT;
    }

    public int step() {
        if (state < 0) {
            return ACCEPT;
        }
        if (reducedNT >= 0) {
            shift(reducedNT);
            if (!gotoState(reducedNT)) {
                return ERROR;
            } else {
                reducedNT = -1;
                return GOTO;
            }
        }
        if (currSymbol < 0)
            currSymbol = position < input.length ? input[position++] : grammar.getNumSyms() - 1;
        if (grammar.isNonterminal(currSymbol)) {
            shift(currSymbol);
            if (!gotoState(currSymbol)) {
                return ERROR;
            } else {
                currSymbol = -1;
                return GOTO;
            }
        }
        byte[] b = tables.getActionAt(state);
        int[] ai = tables.getArgAt(state);
        int i = currSymbol - grammar.getNumNTs();
        switch (b[i]) {
        case 1:
            if (ai[i] < 0) {
                return ACCEPT;
            } else {
                shift(currSymbol);
                currSymbol = -1;
                state = ai[i];
                return SHIFT;
            }
        case 2:
            reduce(ai[i]);
            return REDUCE;
        }
        return ERROR;
    }

    private void shift(int i)
    {
        stack = stack.push(state, i);
    }

    private void reduce(int i) {
        LR0Items.Item item = machine.reduceItem(state, i);
        int j = item.getProd().getRhs().length;
        if (j > 0) {
            for (; j > 1; j--) {
                stack = stack.pop();
            }
            state = stack.getState();
            stack = stack.pop();
        }
        reducedNT = item.getLhs();
    }

    private boolean gotoState(int i) {
        int ai[] = machine.getGotosAt(state);
        for (int anAi : ai) {
            if (i == machine.getEntry(anAi)) {
                state = anAi;
                return true;
            }
        }
        return false;
    }

    public void display(Writer writer, boolean flag) throws IOException {
        stack.display(writer, grammar, flag);
        if (flag) {
            writer.write(state);
            writer.write(" ");
        }
        writer.write("_ ");
        if (reducedNT >= 0) {
            writer.write(grammar.getSymbol(reducedNT).toString());
            writer.write(" ");
        }
        if (currSymbol >= 0) {
            writer.write(grammar.getSymbol(currSymbol).toString());
            if (position < input.length)
                writer.write(" ...");
        } else {
            if (position < input.length) {
                writer.write(grammar.getSymbol(input[position]).toString());
                writer.write(" ...");
            }
        }
        writer.write("\n");
    }
}
