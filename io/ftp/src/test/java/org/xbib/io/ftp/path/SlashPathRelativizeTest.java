package org.xbib.io.ftp.path;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public final class SlashPathRelativizeTest {
    public Iterator<Object[]> relativizeData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"/a/b", "/a/b/c/d", "c/d"});
        list.add(new Object[]{"/a/b/c/d", "/a/b", "../.."});
        list.add(new Object[]{"/a/b", "/a/b", ""});
        list.add(new Object[]{"/a/b", "/a/c", "../c"});
        list.add(new Object[]{"a/b", "a/c", "../c"});
        list.add(new Object[]{"a/b", "c", "../../c"});
        list.add(new Object[]{"../a/b", "../c", "../../c"});

        return list.iterator();
    }

    @Test
    public void relativizationWorksAsExpected() {
        relativizeData().forEachRemaining(o -> {
            final String srcpath = (String) o[0];
            final String dstpath = (String) o[1];
            final String relpath = (String) o[2];
            final SlashPath src = SlashPath.fromString(srcpath);
            final SlashPath dst = SlashPath.fromString(dstpath);
            final SlashPath rel = SlashPath.fromString(relpath);

            assertEquals(src.relativize(dst), rel);
            assertEquals(src.relativize(src.resolve(rel)), rel);
        });
    }

    @Test
    public void pathsMustBothBeAbsoluteOrRelative() {
        final SlashPath p1 = SlashPath.fromString("/abs");
        final SlashPath p2 = SlashPath.fromString("rel");

        try {
            p1.relativize(p2);
            fail("No exception thrown!");
        } catch (IllegalArgumentException ignored) {
            assertTrue(true);
        }

        try {
            p2.relativize(p1);
            fail("No exception thrown!");
        } catch (IllegalArgumentException ignored) {
            assertTrue(true);
        }
    }
}
