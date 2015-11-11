package jacc.grammar;

import java.io.PrintWriter;

public class LR0Items
{
    public class Item
    {

        private int itemNo;
        private int lhs;
        private int prodNo;
        private int pos;

        public int getNo()
        {
            return itemNo;
        }

        public int getLhs()
        {
            return lhs;
        }

        public int getProdNo()
        {
            return prodNo;
        }

        public int getSeqNo()
        {
            return getProd().getSeqNo();
        }

        public Grammar.Prod getProd()
        {
            return grammar.getProds(lhs)[prodNo];
        }

        public int getPos()
        {
            return pos;
        }

        public boolean canGoto()
        {
            if (lhs < 0)
                return pos == 0;
            else
                return pos != getProd().getRhs().length;
        }

        public boolean canReduce()
        {
            return lhs >= 0 && pos == getProd().getRhs().length;
        }

        public boolean canAccept()
        {
            return lhs < 0 && pos == 1;
        }

        public int getNextItem()
        {
            if (lhs >= 0)
                return itemNo + 1;
            else
                return 1;
        }

        public int getNextSym()
        {
            if (lhs >= 0)
                return grammar.getProds(lhs)[prodNo].getRhs()[pos];
            else
                return 0;
        }

        public void display(PrintWriter printwriter)
        {
            if (lhs < 0)
            {
                if (pos == 0)
                    printwriter.print("$accept : _" + grammar.getStart() + " " + grammar.getEnd());
                else
                    printwriter.print("$accept : " + grammar.getStart() + "_" + grammar.getEnd());
                return;
            }
            printwriter.print(grammar.getSymbol(lhs));
            printwriter.print(" : ");
            Grammar.Prod prod = grammar.getProds(lhs)[prodNo];
            int ai[] = prod.getRhs();
            printwriter.print(grammar.displaySymbols(ai, 0, pos, "", " "));
            printwriter.print("_");
            if (pos < ai.length)
                printwriter.print(grammar.displaySymbols(ai, pos, ai.length, "", " "));
            String s = prod.getLabel();
            if (s != null)
            {
                printwriter.print("    (");
                printwriter.print(s);
                printwriter.print(')');
            }
        }

        private Item(int i, int j, int k)
        {
            itemNo = numItems;
            lhs = i;
            prodNo = j;
            pos = k;
            items[numItems++] = this;
        }

    }


    private Grammar grammar;
    private int numItems;
    private Item items[];
    private int firstKernel[][];

    public LR0Items(Grammar grammar1)
    {
        grammar = grammar1;
        int i = grammar1.getNumNTs();
        numItems = 2;
        firstKernel = new int[i][];
        for (int j = 0; j < i; j++)
        {
            Grammar.Prod aprod[] = grammar1.getProds(j);
            firstKernel[j] = new int[aprod.length];
            for (int l = 0; l < aprod.length; l++)
            {
                int j1 = aprod[l].getRhs().length;
                firstKernel[j][l] = numItems;
                numItems += j1 != 0 ? j1 : 1;
            }

        }

        items = new Item[numItems];
        numItems = 0;
        new Item(-1, 0, 0);
        new Item(-1, 0, 1);
        for (int k = 0; k < i; k++)
        {
            Grammar.Prod aprod1[] = grammar1.getProds(k);
            for (int i1 = 0; i1 < aprod1.length; i1++)
            {
                int ai[] = aprod1[i1].getRhs();
                for (int k1 = 1; k1 < ai.length; k1++)
                    new Item(k, i1, k1);

                new Item(k, i1, ai.length);
            }

        }

    }

    public int getNumItems()
    {
        return numItems;
    }

    public Item getItem(int i)
    {
        return items[i];
    }

    public int getStartItem()
    {
        return 0;
    }

    public int getEndItem()
    {
        return 1;
    }

    public int getFirstKernel(int i, int j)
    {
        return firstKernel[i][j];
    }

    public void displayAllItems(PrintWriter printwriter)
    {
        printwriter.println("Items:");
        for (int i = 0; i < items.length; i++)
        {
            printwriter.print(i + ": ");
            items[i].display(printwriter);
            printwriter.println();
        }

    }




}
