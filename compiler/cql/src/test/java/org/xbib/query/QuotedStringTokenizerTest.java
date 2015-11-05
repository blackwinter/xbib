package org.xbib.query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuotedStringTokenizerTest {

    @Test
    public void testTokenizer() throws Exception {
        String s = "Linux is \"pinguin's best friend\", not Windows";
        QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(s);
        assertEquals("Linux", tokenizer.nextToken());
        assertEquals("is", tokenizer.nextToken());
        assertEquals("pinguin's best friend,", tokenizer.nextToken());
        assertEquals("not", tokenizer.nextToken());
        assertEquals("Windows", tokenizer.nextToken());
    }
}
