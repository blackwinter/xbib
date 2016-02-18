package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.chronic.tags.Scalar;
import org.xbib.time.chronic.tags.StringTag;

public class TokenTest extends Assert {

    @Test
    public void testToken() {
        Token token = new Token("foo");
        assertEquals(0, token.getTags().size());
        assertFalse(token.isTagged());
        token.tag(new StringTag("mytag"));
        assertEquals(1, token.getTags().size());
        assertTrue(token.isTagged());
        assertEquals(StringTag.class, token.getTag(StringTag.class).getClass());
        token.tag(new Scalar(5));
        assertEquals(2, token.getTags().size());
        token.untag(StringTag.class);
        assertEquals(1, token.getTags().size());
        assertEquals("foo", token.getWord());
    }
}
