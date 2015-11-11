package jacc;


class NamedJaccSymbols extends JaccSymbols
{

    NamedJaccSymbols()
    {
    }

    public JaccSymbol find(String s)
    {
        for (JaccSymbols.Node node = root; node != null;)
        {
            int i = s.compareTo(node.data.getName());
            if (i < 0)
                node = node.left;
            else
            if (i > 0)
                node = node.right;
            else
                return node.data;
        }

        return null;
    }

    public JaccSymbol findOrAdd(String s)
    {
        if (root == null)
        {
            JaccSymbol jaccsymbol = new JaccSymbol(s);
            root = new JaccSymbols.Node(jaccsymbol);
            size++;
            return jaccsymbol;
        }
        JaccSymbols.Node node = root;
        do
        {
            int i = s.compareTo(node.data.getName());
            if (i < 0)
            {
                if (node.left == null)
                {
                    JaccSymbol jaccsymbol1 = new JaccSymbol(s);
                    node.left = new JaccSymbols.Node(jaccsymbol1);
                    size++;
                    return jaccsymbol1;
                }
                node = node.left;
            } else
            if (i > 0)
            {
                if (node.right == null)
                {
                    JaccSymbol jaccsymbol2 = new JaccSymbol(s);
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
