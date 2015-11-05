package compiler;

public class SimpleHandler extends Handler
{

    public SimpleHandler()
    {
    }

    protected void respondTo(Diagnostic diagnostic)
    {
        Position position = diagnostic.getPos();
        if (diagnostic instanceof Warning)
            System.err.print("WARNING: ");
        else
            System.err.print("ERROR: ");
        if (position != null)
            System.err.println(position.describe());
        String s = diagnostic.getText();
        if (s != null)
            System.err.println(s);
        System.err.println();
    }
}
