package compiler;

public abstract class Handler
{

    private int numDiagnostics;
    private int numFailures;

    public Handler()
    {
        numDiagnostics = 0;
        numFailures = 0;
    }

    public int getNumDiagnostics()
    {
        return numDiagnostics;
    }

    public int getNumFailures()
    {
        return numFailures;
    }

    public void report(Diagnostic diagnostic)
    {
        numDiagnostics++;
        if (diagnostic instanceof Failure)
            numFailures++;
        respondTo(diagnostic);
    }

    protected abstract void respondTo(Diagnostic diagnostic);

    public void reset()
    {
        numDiagnostics = 0;
        numFailures = 0;
    }
}
