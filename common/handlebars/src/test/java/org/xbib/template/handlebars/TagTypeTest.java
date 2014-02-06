
package org.xbib.template.handlebars;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TagTypeTest extends AbstractTest {

    Hash helpers = $("tag", new Helper<Object>() {
        @Override
        public CharSequence apply(final Object context, final Options options) throws IOException {
            return options.tagType.name();
        }
    });

    @Test
    public void varTag() throws IOException {
        shouldCompileTo("{{tag}}", $, helpers, "VAR");
    }

    @Test
    public void unescapeVarTag() throws IOException {
        shouldCompileTo("{{&tag}}", $, helpers, "AMP_VAR");
    }

    @Test
    public void tripleVarTag() throws IOException {
        shouldCompileTo("{{{tag}}}", $, helpers, "TRIPLE_VAR");
    }

    @Test
    public void sectionTag() throws IOException {
        shouldCompileTo("{{#tag}}{{/tag}}", $, helpers, "SECTION");
    }

    @Test
    public void inline() {
        assertTrue(TagType.VAR.inline());

        assertTrue(TagType.AMP_VAR.inline());

        assertTrue(TagType.TRIPLE_VAR.inline());
    }

    @Test
    public void block() {
        assertTrue(!TagType.SECTION.inline());
    }

    @Test
    public void collectVar() throws IOException {
        assertEquals(Arrays.asList("a", "z", "k"), compile("{{#hello}}{{a}}{{&b}}{{z}}{{/hello}}{{k}}")
                .collect(TagType.VAR));
    }

    @Test
    public void collectAmpVar() throws IOException {
        assertEquals(Arrays.asList("b"), compile("{{#hello}}{{a}}{{&b}}{{z}}{{/hello}}{{k}}")
                .collect(TagType.AMP_VAR));
    }

    @Test
    public void collectTripleVar() throws IOException {
        assertEquals(Arrays.asList("tvar"),
                compile("{{{tvar}}}{{#hello}}{{a}}{{&b}}{{z}}{{/hello}}{{k}}")
                        .collect(TagType.TRIPLE_VAR));
    }

    @Test
    public void collectSection() throws IOException {
        assertEquals(Arrays.asList("hello"), compile("{{#hello}}{{a}}{{&b}}{{z}}{{/hello}}{{k}}")
                .collect(TagType.SECTION));
    }

    @Test
    public void collectSectionAndVars() throws IOException {
        assertEquals(Arrays.asList("hello", "a", "b", "z", "k"),
                compile("{{#hello}}{{a}}{{&b}}{{z}}{{/hello}}{{k}}")
                        .collect(TagType.SECTION, TagType.VAR, TagType.TRIPLE_VAR, TagType.AMP_VAR));
    }
}
