package jacc;

import jacc.grammar.Grammar;

public class JaccSymbol extends jacc.grammar.Grammar.Symbol
{

    private int num;
    private int tokenNo;
    private JaccProd jaccProds[];
    private int pused;
    public Fixity fixity;
    private String type;

    public JaccSymbol(String s, int i)
    {
        super(s);
        tokenNo = -1;
        jaccProds = null;
        pused = 0;
        num = i;
    }

    public JaccSymbol(String s)
    {
        this(s, -1);
    }

    public int getNum()
    {
        return num;
    }

    public void setNum(int i)
    {
        if (num < 0)
            num = i;
    }

    public int getTokenNo()
    {
        return tokenNo;
    }

    public void setTokenNo(int i)
    {
        if (tokenNo < 0)
            tokenNo = i;
    }

    public void addProduction(JaccProd jaccprod)
    {
        if (jaccProds == null)
            jaccProds = new JaccProd[1];
        else
        if (pused >= jaccProds.length)
        {
            JaccProd ajaccprod[] = new JaccProd[2 * jaccProds.length];
            for (int i = 0; i < jaccProds.length; i++)
                ajaccprod[i] = jaccProds[i];

            jaccProds = ajaccprod;
        }
        jaccProds[pused++] = jaccprod;
    }

    public JaccProd[] getProds()
    {
        JaccProd ajaccprod[] = new JaccProd[pused];
        for (int i = 0; i < pused; i++)
        {
            ajaccprod[i] = jaccProds[i];
            ajaccprod[i].fixup();
        }

        return ajaccprod;
    }

    public boolean setFixity(Fixity fixity1)
    {
        if (fixity == null)
        {
            fixity = fixity1;
            return true;
        } else
        {
            return fixity1.equalsFixity(fixity);
        }
    }

    public Fixity getFixity()
    {
        return fixity;
    }

    public boolean setType(String s)
    {
        if (type == null)
        {
            type = s;
            return true;
        } else
        {
            return s.compareTo(type) == 0;
        }
    }

    public String getType()
    {
        return type;
    }
}
