// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) fieldsfirst space 

package jacc;


// Referenced classes of package jacc:
//            JaccSymbols, JaccSymbol

class NumJaccSymbols extends JaccSymbols
{

    NumJaccSymbols()
    {
    }

    public JaccSymbol find(int i)
    {
        for (JaccSymbols.Node node = root; node != null;)
        {
            int j = node.data.getNum();
            if (i < j)
                node = node.left;
            else
            if (i > j)
                node = node.right;
            else
                return node.data;
        }

        return null;
    }

    public JaccSymbol findOrAdd(String s, int i)
    {
        if (root == null)
        {
            JaccSymbol jaccsymbol = new JaccSymbol(s, i);
            root = new JaccSymbols.Node(jaccsymbol);
            size++;
            return jaccsymbol;
        }
        JaccSymbols.Node node = root;
        do
        {
            int j = node.data.getNum();
            if (i < j)
            {
                if (node.left == null)
                {
                    JaccSymbol jaccsymbol1 = new JaccSymbol(s, i);
                    node.left = new JaccSymbols.Node(jaccsymbol1);
                    size++;
                    return jaccsymbol1;
                }
                node = node.left;
            } else
            if (i > j)
            {
                if (node.right == null)
                {
                    JaccSymbol jaccsymbol2 = new JaccSymbol(s, i);
                    node.right = new JaccSymbols.Node(jaccsymbol2);
                    size++;
                    return jaccsymbol2;
                }
                node = node.right;
            } else
            {
                return node.data;
            }
        } while (true);
    }
}
