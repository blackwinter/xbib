package compiler;

public abstract class Phase
{

    private Handler handler;

    protected Phase(Handler handler1)
    {
        handler = handler1;
    }

    public Handler getHandler()
    {
        return handler;
    }

    public void report(Diagnostic diagnostic)
    {
        if (handler != null)
            handler.report(diagnostic);
    }
}
