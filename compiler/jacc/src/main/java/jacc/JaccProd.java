package jacc;

import compiler.Position;
import jacc.grammar.Grammar;

public class JaccProd extends jacc.grammar.Grammar.Prod
{

    private Fixity fixity;
    private JaccSymbol prodSyms[];
    private Position actPos;
    private String action;

    public JaccProd(Fixity fixity1, JaccSymbol ajaccsymbol[], Position position, String s, int i)
    {
        super(new int[ajaccsymbol.length], i);
        fixity = fixity1;
        prodSyms = ajaccsymbol;
        actPos = position;
        action = s;
    }

    public String getLabel()
    {
        return Integer.toString(getSeqNo());
    }

    public void fixup()
    {
        int ai[] = getRhs();
        for (int i = 0; i < prodSyms.length; i++)
            ai[i] = prodSyms[i].getTokenNo();

    }

    public Fixity getFixity()
    {
        if (fixity == null)
        {
            for (int i = prodSyms.length - 1; i >= 0; i--)
            {
                Fixity fixity1 = prodSyms[i].getFixity();
                if (fixity1 != null)
                    return fixity1;
            }

        }
        return fixity;
    }

    public Position getActionPos()
    {
        return actPos;
    }

    public String getAction()
    {
        return action;
    }
}
