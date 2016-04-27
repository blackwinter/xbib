package org.xbib.jacc.compiler;

public abstract class Handler {

    private int numFailures;

    public Handler() {
        numFailures = 0;
    }

    public int getNumFailures() {
        return numFailures;
    }

    void report(Diagnostic diagnostic) {
        if (diagnostic instanceof Failure) {
            numFailures++;
        }
        respondTo(diagnostic);
    }

    protected abstract void respondTo(Diagnostic diagnostic);

}
