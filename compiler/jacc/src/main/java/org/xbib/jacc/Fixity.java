package org.xbib.jacc;

class Fixity {

    private static final int LEFT = 1;
    private static final int NONASS = 2;
    private static final int RIGHT = 3;
    private int assoc;
    private int prec;

    private Fixity(int i, int j) {
        assoc = i;
        prec = j;
    }

    public static Fixity left(int i)
    {
        return new Fixity(LEFT, i);
    }

    static Fixity nonass(int i)
    {
        return new Fixity(NONASS, i);
    }

    static Fixity right(int i)
    {
        return new Fixity(RIGHT, i);
    }

    static int which(Fixity fixity, Fixity fixity1) {
        if (fixity != null && fixity1 != null) {
            if (fixity.prec > fixity1.prec)
                return LEFT;
            if (fixity.prec < fixity1.prec)
                return RIGHT;
            if (fixity.assoc == LEFT && fixity1.assoc == LEFT)
                return LEFT;
            if (fixity.assoc == RIGHT && fixity1.assoc == RIGHT)
                return RIGHT;
        }
        return NONASS;
    }

    boolean equalsFixity(Fixity fixity)
    {
        return assoc == fixity.assoc && prec == fixity.prec;
    }

    public boolean equals(Object obj) {
        return obj instanceof Fixity && equalsFixity((Fixity) obj);
    }
}
