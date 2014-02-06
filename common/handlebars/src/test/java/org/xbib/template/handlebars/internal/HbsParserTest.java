package org.xbib.template.handlebars.internal;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

public class HbsParserTest {

    @Test
    public void hello() {
        parse("Hello {{who}}\n!");
    }

    @Test
    public void text() {
        parse("Hello world!");
    }

    @Test
    public void newlines() {
        parse("Alan's\rTest");
    }

    @Test
    public void var() {
        parse("{{variable 678}}");

        parse("{{array.[10]}}");

        parse("{{array.[1foo]}}");

        parse("{{array.['foo']}}");

        parse("{{array.['foo or bar}}]}}");

        parse("{{variable \"string\"}}");

        parse("{{variable \"true\"}}");

        parse("{{variable \"string\" 78}}");
    }

    @Test
    public void comments() {
        parse("12345{{! Comment Block! }}67890");
    }

    @Test
    public void partial() {
        parse("[ {{>include}} ]");
    }

    @Test
    public void ampvar() {
        parse("{{&variable 678}}");

        parse("{{&variable \"string\"}}");

        parse("{{&variable \"true\"}}");

        parse("{{&variable \"string\" 78}}");
    }

    @Test
    public void tvar() {
        parse("{{{variable 678}}}");

        parse("{{{variable \"string\"}}}");

        parse("{{{variable \"true\"}}}");

        parse("{{{variable \"string\" 78}}}");
    }

    @Test
    public void block() {
        parse("{{#block 678}}{{var}}{{/block}}");
        parse("{{#block 678}}then{{^}}else{{/block}}");
        parse("{{#block 678}}then{{^}}else{{/block}}");
    }

    @Test
    public void unless() {
        parse("{{^block}}{{var}}{{/block}}");
    }

    @Test
    public void hash() {
        parse("{{variable int=678}}");

        parse("{{variable string='string'}}");
    }

    @Test
    public void setDelim() {
        parse("{{=<% %>=}}<%hello%><%={{ }}=%>{{reset}}");
        parse("{{= | | =}}<|#lambda|-|/lambda|>");
        parse("{{=+-+ -+-=}}+-+test-+-");
    }

    private ParseTree parse(final String input) {
        return parse(input, "{{", "}}");
    }

    private ParseTree parse(final String input, final String start, final String delim) {
        final HbsLexer lexer = new HbsLexer(new ANTLRInputStream(input), start, delim);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        HbsParser parser = new HbsParser(tokens) {
            @Override
            void setStart(final String start) {
                lexer.start = start;
            }

            @Override
            void setEnd(final String end) {
                lexer.end = end;
            }
        };
        ParseTree tree = parser.template();
        return tree;
    }
}
