package jacc.grammar;

import java.io.PrintWriter;

public class Parser
{
    public static class Stack
    {

        private int state;
        private int symbol;
        private Stack up;
        private Stack down;

        public boolean empty()
        {
            return down == null;
        }

        public int getState()
        {
            return state;
        }

        public int getSymbol()
        {
            return symbol;
        }

        public Stack pop()
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

        public void display(PrintWriter printwriter, Grammar grammar1, boolean flag)
        {
            if (down != null)
            {
                down.display(printwriter, grammar1, flag);
                if (flag)
                {
                    printwriter.print(state);
                    printwriter.print(" ");
                }
                printwriter.print(grammar1.getSymbol(symbol).toString());
                printwriter.print(" ");
            }
        }

        public Stack()
        {
            this(null);
        }

        private Stack(Stack stack1)
        {
            down = stack1;
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
    public static final int ACCEPT = 0;
    public static final int ERROR = 1;
    public static final int SHIFT = 2;
    public static final int GOTO = 3;
    public static final int REDUCE = 4;

    public Parser(Tables tables1, int ai[])
    {
        position = 0;
        currSymbol = -1;
        reducedNT = -1;
        stack = new Stack();
        state = 0;
        tables = tables1;
        input = ai;
        machine = tables1.getMachine();
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

    public int step()
    {
        if (state < 0)
            return 0;
        if (reducedNT >= 0)
        {
            shift(reducedNT);
            if (!gotoState(reducedNT))
            {
                return 1;
            } else
            {
                reducedNT = -1;
                return 3;
            }
        }
        if (currSymbol < 0)
            currSymbol = position < input.length ? input[position++] : grammar.getNumSyms() - 1;
        if (grammar.isNonterminal(currSymbol))
        {
            shift(currSymbol);
            if (!gotoState(currSymbol))
            {
                return 1;
            } else
            {
                currSymbol = -1;
                return 3;
            }
        }
        byte abyte0[] = tables.getActionAt(state);
        int ai[] = tables.getArgAt(state);
        int i = currSymbol - grammar.getNumNTs();
        switch (abyte0[i])
        {
        case 1: // '\001'
            if (ai[i] < 0)
            {
                return 0;
            } else
            {
                shift(currSymbol);
                currSymbol = -1;
                state = ai[i];
                return 2;
            }

        case 2: // '\002'
            reduce(ai[i]);
            return 4;
        }
        return 1;
    }

    private void shift(int i)
    {
        stack = stack.push(state, i);
    }

    private void reduce(int i)
    {
        LR0Items.Item item = machine.reduceItem(state, i);
        int j = item.getProd().getRhs().length;
        if (j > 0)
        {
            for (; j > 1; j--)
                stack = stack.pop();

            state = stack.getState();
            stack = stack.pop();
        }
        reducedNT = item.getLhs();
    }

    private boolean gotoState(int i)
    {
        int ai[] = machine.getGotosAt(state);
        for (int j = 0; j < ai.length; j++)
            if (i == machine.getEntry(ai[j]))
            {
                state = ai[j];
                return true;
            }

        return false;
    }

    public void display(PrintWriter printwriter, boolean flag)
    {
        stack.display(printwriter, grammar, flag);
        if (flag)
        {
            printwriter.print(state);
            printwriter.print(" ");
        }
        printwriter.print("_ ");
        if (reducedNT >= 0)
        {
            printwriter.print(grammar.getSymbol(reducedNT).toString());
            printwriter.print(" ");
        }
        if (currSymbol >= 0)
        {
            printwriter.print(grammar.getSymbol(currSymbol).toString());
            if (position < input.length)
                printwriter.print(" ...");
        } else
        if (position < input.length)
        {
            printwriter.print(grammar.getSymbol(input[position]).toString());
            printwriter.print(" ...");
        }
        printwriter.println();
    }
}
