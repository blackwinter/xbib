package jacc;


public class Fixity
{

    public static final int LEFT = 1;
    public static final int NONASS = 2;
    public static final int RIGHT = 3;
    private int assoc;
    private int prec;

    private Fixity(int i, int j)
    {
        assoc = i;
        prec = j;
    }

    public static Fixity left(int i)
    {
        return new Fixity(1, i);
    }

    public static Fixity nonass(int i)
    {
        return new Fixity(2, i);
    }

    public static Fixity right(int i)
    {
        return new Fixity(3, i);
    }

    public int getAssoc()
    {
        return assoc;
    }

    public int getPrec()
    {
        return prec;
    }

    public static int which(Fixity fixity, Fixity fixity1)
    {
        if (fixity != null && fixity1 != null)
        {
            if (fixity.prec > fixity1.prec)
                return 1;
            if (fixity.prec < fixity1.prec)
                return 3;
            if (fixity.assoc == 1 && fixity1.assoc == 1)
                return 1;
            if (fixity.assoc == 3 && fixity1.assoc == 3)
                return 3;
        }
        return 2;
    }

    public boolean equalsFixity(Fixity fixity)
    {
        return assoc == fixity.assoc && prec == fixity.prec;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof Fixity)
            return equalsFixity((Fixity)obj);
        else
            return false;
    }
}
