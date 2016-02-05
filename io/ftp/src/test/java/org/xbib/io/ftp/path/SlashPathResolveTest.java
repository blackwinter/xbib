package org.xbib.io.ftp.path;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class SlashPathResolveTest {
    public Iterator<Object[]> resolveData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"/foo", "/bar", "/bar"});
        list.add(new Object[]{"/foo", "/", "/"});
        list.add(new Object[]{"foo", "/", "/"});
        list.add(new Object[]{"/foo", "/bar", "/bar"});
        list.add(new Object[]{"", "/bar", "/bar"});
        list.add(new Object[]{"", "/..", "/.."});
        list.add(new Object[]{".", "..", "./.."});
        list.add(new Object[]{"", "", ""});
        list.add(new Object[]{"", "a/b", "a/b"});
        list.add(new Object[]{"./d", "a/b", "./d/a/b"});
        list.add(new Object[]{"d", "/a/b", "/a/b"});

        return list.iterator();
    }

    @Test
    public void pathResolveWorks() {
        resolveData().forEachRemaining(o -> {
            final String first = (String) o[0];
            final String second = (String) o[1];
            final String result = (String) o[2];
            final SlashPath p1 = SlashPath.fromString(first);
            final SlashPath p2 = SlashPath.fromString(second);
            final SlashPath expected
                    = SlashPath.fromString(result);
            assertEquals(p1.resolve(p2), expected);
        });
    }
}
