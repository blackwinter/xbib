package jacc;


abstract class JaccSymbols
{
    protected static class Node
    {

        Node left;
        JaccSymbol data;
        Node right;

        Node(JaccSymbol jaccsymbol)
        {
            data = jaccsymbol;
        }
    }


    protected Node root;
    protected int size;

    protected JaccSymbols()
    {
        root = null;
        size = 0;
    }

    public int getSize()
    {
        return size;
    }

    public int fill(JaccSymbol ajaccsymbol[], int i)
    {
        return fill(ajaccsymbol, i, root);
    }

    private static int fill(JaccSymbol ajaccsymbol[], int i, Node node)
    {
        if (node != null)
        {
            i = fill(ajaccsymbol, i, node.left);
            ajaccsymbol[i++] = node.data;
            i = fill(ajaccsymbol, i, node.right);
        }
        return i;
    }
}
