package org.xbib.jacc;

import org.xbib.jacc.compiler.Position;
import org.xbib.jacc.grammar.Grammar;

class JaccProd extends Grammar.Prod {

    private Fixity fixity;
    private JaccSymbol[] prodSyms;
    private Position actPos;
    private String action;

    JaccProd(Fixity fixity, JaccSymbol[] jaccsymbol, Position position, String s, int i) {
        super(new int[jaccsymbol.length], i);
        this.fixity = fixity;
        prodSyms = jaccsymbol;
        actPos = position;
        action = s;
    }

    public String getLabel() {
        return Integer.toString(getSeqNo());
    }

    void fixup() {
        int[] ai = getRhs();
        for (int i = 0; i < prodSyms.length; i++) {
            ai[i] = prodSyms[i].getTokenNo();
        }
    }

    Fixity getFixity() {
        if (fixity == null) {
            for (int i = prodSyms.length - 1; i >= 0; i--) {
                Fixity fixity1 = prodSyms[i].getFixity();
                if (fixity1 != null) {
                    return fixity1;
                }
            }
        }
        return fixity;
    }

    Position getActionPos()
    {
        return actPos;
    }

    String getAction()
    {
        return action;
    }
}
