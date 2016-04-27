package org.xbib.jacc;

import org.xbib.jacc.grammar.Grammar;

public class JaccSymbol extends Grammar.Symbol {

    private int num;
    private int tokenNo;
    private JaccProd[] jaccProds;
    private int pused;
    private Fixity fixity;
    private String type;

    public JaccSymbol(String s, int i) {
        super(s);
        tokenNo = -1;
        jaccProds = null;
        pused = 0;
        num = i;
    }

    public JaccSymbol(String s) {
        this(s, -1);
    }

    int getNum() {
        return num;
    }

    void setNum(int i) {
        if (num < 0)
            num = i;
    }

    int getTokenNo()
    {
        return tokenNo;
    }

    void setTokenNo(int i) {
        if (tokenNo < 0)
            tokenNo = i;
    }

    void addProduction(JaccProd jaccprod) {
        if (jaccProds == null) {
            jaccProds = new JaccProd[1];
        } else {
            if (pused >= jaccProds.length) {
                JaccProd ajaccprod[] = new JaccProd[2 * jaccProds.length];
                System.arraycopy(jaccProds, 0, ajaccprod, 0, jaccProds.length);

                jaccProds = ajaccprod;
            }
        }
        jaccProds[pused++] = jaccprod;
    }

    public JaccProd[] getProds() {
        JaccProd ajaccprod[] = new JaccProd[pused];
        for (int i = 0; i < pused; i++) {
            ajaccprod[i] = jaccProds[i];
            ajaccprod[i].fixup();
        }
        return ajaccprod;
    }

    boolean setFixity(Fixity fixity1) {
        if (fixity == null) {
            fixity = fixity1;
            return true;
        } else {
            return fixity1.equalsFixity(fixity);
        }
    }

    Fixity getFixity()
    {
        return fixity;
    }

    boolean setType(String s) {
        if (type == null) {
            type = s;
            return true;
        } else {
            return s.compareTo(type) == 0;
        }
    }

    String getType() {
        return type;
    }
}
