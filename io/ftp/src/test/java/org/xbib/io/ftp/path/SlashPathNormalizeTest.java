package org.xbib.io.ftp.path;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class SlashPathNormalizeTest {
    public Iterator<Object[]> resolveData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"foo", "foo"});
        list.add(new Object[]{"foo/.", "foo"});
        list.add(new Object[]{"foo/..", ""});
        list.add(new Object[]{"foo", "foo"});
        list.add(new Object[]{"foo/.", "foo"});
        list.add(new Object[]{"foo/..", ""});
        list.add(new Object[]{"/foo/..", "/"});
        list.add(new Object[]{"/foo/../bar/./baz/.", "/bar/baz"});
        list.add(new Object[]{"a/b/./d/e/../../c/.", "a/b/c"});
        list.add(new Object[]{"/..", "/.."});
        list.add(new Object[]{"..", ".."});
        list.add(new Object[]{"../.", ".."});
        list.add(new Object[]{"../../a", "../../a"});
        list.add(new Object[]{"../a/./b/../c", "../a/c"});

        return list.iterator();
    }

    @Test
    public void pathNormalizeWorks() {
        resolveData().forEachRemaining(o -> {
            final String input = (String) o[0];
            final String result = (String) o[1];
            final SlashPath orig = SlashPath.fromString(input);
            final SlashPath expected
                    = SlashPath.fromString(result);
            assertEquals(orig.normalize(), expected);
            assertTrue(expected.isNormalized());
        });
    }
}
