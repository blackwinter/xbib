package jacc;

import compiler.*;
import jacc.grammar.Grammar;

public abstract class JaccAbstractParser extends Phase
{

    protected NamedJaccSymbols terminals;
    protected NamedJaccSymbols nonterms;
    protected NumJaccSymbols literals;
    protected JaccSymbol start;

    public JaccAbstractParser(Handler handler)
    {
        super(handler);
        terminals = new NamedJaccSymbols();
        nonterms = new NamedJaccSymbols();
        literals = new NumJaccSymbols();
        start = null;
    }

    public Grammar getGrammar()
    {
        try {
        JaccSymbol ajaccsymbol[];
        JaccProd ajaccprod[][];
        int i = nonterms.getSize();
        int j = terminals.getSize() + literals.getSize() + 1;
        if (i == 0 || start == null)
        {
            report(new Failure("No nonterminals defined"));
            return null;
        }
        ajaccsymbol = new JaccSymbol[i + j];
        literals.fill(ajaccsymbol, terminals.fill(ajaccsymbol, nonterms.fill(ajaccsymbol, 0)));
        ajaccsymbol[(i + j) - 1] = new JaccSymbol("$end");
        ajaccsymbol[(i + j) - 1].setNum(0);
        int k = 1;
        for (int l = 0; l < j - 1; l++)
        {
            if (ajaccsymbol[i + l].getNum() >= 0)
                continue;
            for (; literals.find(k) != null; k++);
            ajaccsymbol[i + l].setNum(k++);
        }

        int i1 = 0;
        do
        {
            if (i1 >= i)
                break;
            if (ajaccsymbol[i1] == start)
            {
                if (i1 > 0)
                {
                    JaccSymbol jaccsymbol = ajaccsymbol[0];
                    ajaccsymbol[0] = ajaccsymbol[i1];
                    ajaccsymbol[i1] = jaccsymbol;
                }
                break;
            }
            i1++;
        } while (true);
        for (int j1 = 0; j1 < ajaccsymbol.length; j1++)
            ajaccsymbol[j1].setTokenNo(j1);

        ajaccprod = new JaccProd[nonterms.getSize()][];
        boolean flag = false;
        for (int k1 = 0; k1 < ajaccprod.length; k1++)
        {
            ajaccprod[k1] = ajaccsymbol[k1].getProds();
            if (ajaccprod[k1] == null || ajaccprod[k1].length == 0)
            {
                report(new Failure("No productions for " + ajaccsymbol[k1].getName()));
                flag = true;
            }
        }

        //if (flag)
            //break MISSING_BLOCK_LABEL_402;
            return new Grammar(ajaccsymbol, ajaccprod);
        }
        catch (Exception exception)
        {
            report(new Failure("Internal problem " + exception));
            return null;
        }
    }
}
