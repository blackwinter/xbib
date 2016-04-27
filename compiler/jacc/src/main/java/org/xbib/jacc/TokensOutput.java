package org.xbib.jacc;

import org.xbib.jacc.compiler.Handler;
import org.xbib.jacc.grammar.Grammar;

import java.io.IOException;
import java.io.Writer;

class TokensOutput extends Output {

    TokensOutput(Handler handler, JaccJob jaccjob) {
        super(handler, jaccjob);
    }

    @Override
    public void write(Writer writer) throws IOException {
        datestamp(writer);
        String s = settings.getPackageName();
        if (s != null) {
            writer.write("package " + s + ";\n\n");
        }
        writer.write("interface " + settings.getInterfaceName() + " {\n");
        indent(writer, 1);
        writer.write("int ENDINPUT = 0;\n");
        for (int i = 0; i < numTs - 1; i++) {
            Grammar.Symbol symbol = grammar.getTerminal(i);
            if (!(symbol instanceof JaccSymbol)) {
                continue;
            }
            JaccSymbol jaccsymbol = (JaccSymbol)symbol;
            String s1 = jaccsymbol.getName();
            indent(writer, 1);
            if (s1.startsWith("'")) {
                writer.write("// " + s1 + " (code=" + jaccsymbol.getNum() + ")\n");
            } else {
                writer.write("int " + s1 + " = " + jaccsymbol.getNum() + ";\n");
            }
        }
        writer.write("}\n");
    }
}
