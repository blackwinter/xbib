package compiler;

public class TableHandler extends Handler
{

    private Diagnostic diags[];
    private int used;
    private int maxCapacity;

    public TableHandler(int i)
    {
        maxCapacity = i;
        diags = new Diagnostic[i <= 0 ? 10 : i];
        used = 0;
    }

    public TableHandler()
    {
        this(0);
    }

    public Diagnostic getDiagnostic(int i)
    {
        return i < 0 || i >= used ? null : diags[i];
    }

    protected void respondTo(Diagnostic diagnostic)
    {
        if (maxCapacity == 0 || used < maxCapacity)
        {
            if (used >= diags.length)
            {
                Diagnostic adiagnostic[] = new Diagnostic[2 * diags.length];
                for (int i = 0; i < diags.length; i++)
                    adiagnostic[i] = diags[i];

                diags = adiagnostic;
            }
            diags[used++] = diagnostic;
        }
    }

    public void reset()
    {
        super.reset();
        for (int i = 0; i < used; i++)
            diags[i] = null;

        used = 0;
    }
}
