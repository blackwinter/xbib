package org.xbib.jacc.compiler;

public abstract class Phase {

    private final Handler handler;

    protected Phase(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        return handler;
    }

    protected void report(Diagnostic diagnostic) {
        if (handler != null)
            handler.report(diagnostic);
    }
}
