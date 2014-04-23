
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for pseudo-vars.
 */
public class PseudoVarsTest {

    @Test
    public void list() throws IOException {
        String input =
                "{{#list}}i={{@index}}\neven={{@even}}\nodd={{@odd}}\nfirst={{@first}}\nlast={{@last}}\n{{/list}}";
        Handlebars handlebars = new Handlebars();

        assertEquals("i=0\n" +
                "even=even\n" +
                "odd=\n" +
                "first=first\n" +
                "last=\n" +
                "i=1\n" +
                "even=\n" +
                "odd=odd\n" +
                "first=\n" +
                "last=\n" +
                "i=2\n" +
                "even=even\n" +
                "odd=\n" +
                "first=\n" +
                "last=last\n",
                handlebars.compileInline(input).apply(new Object() {
                    @SuppressWarnings("unused")
                    public List<String> getList() {
                        return Arrays.asList("a", "b", "c");
                    }
                }));
    }

    @Test
    public void lostParent$51() throws IOException {
        String input =
                "{{#parent}}{{#list}}{{@index}}. {{name}} {{/list}}{{/parent}}";
        Handlebars handlebars = new Handlebars();

        Map<String, Object> parent = new HashMap<String, Object>();
        parent.put("name", "px");
        parent.put("list", Arrays.asList("a", "b"));

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("parent", parent);

        assertEquals("0. px 1. px ", handlebars.compileInline(input).apply(context));
    }
}
