package compiler;

public class DelegatingHandler extends Handler
{

    private Handler handler;

    public DelegatingHandler(Handler handler1)
    {
        handler = handler1;
    }

    protected void respondTo(Diagnostic diagnostic)
    {
        handler.report(diagnostic);
    }
}
