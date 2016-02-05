package org.xbib.io.ftp.path;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class SlashPathTest {
    private static final SlashPath TESTPATH = SlashPath.fromString("/a/b/c");

    @Test
    public void constructorRefusesNullArguments() {
        try {
            SlashPath.fromString(null);
            fail("No exception thrown!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), "null argument is not allowed");
        }
    }

    public Iterator<Object[]> pathInputs() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"", ""});
        list.add(new Object[]{"/", "/"});
        list.add(new Object[]{"//", "/"});
        list.add(new Object[]{"foo", "foo"});
        list.add(new Object[]{"foo/", "foo"});
        list.add(new Object[]{"foo//", "foo"});
        list.add(new Object[]{"foo/.", "foo/."});
        list.add(new Object[]{"foo//.", "foo/."});
        list.add(new Object[]{"//foo/", "/foo"});
        list.add(new Object[]{"/foo//bar/..//", "/foo/bar/.."});

        return list.iterator();
    }

    @Test
    public void constructorRemovesExtraSlashes() {
        pathInputs().forEachRemaining(o -> {
            final String input = (String) o[0];
            final String expected = (String) o[1];
            final SlashPath path = SlashPath.fromString(input);
            assertEquals(path.toString(), expected);
        });
    }

    @Test
    public void hashCodeAndEqualsWork() {
        pathInputs().forEachRemaining(o -> {
            final String first = (String) o[0];
            final String second = (String) o[1];
            final SlashPath p1 = SlashPath.fromString(first);
            final SlashPath p2 = SlashPath.fromString(second);
            assertTrue(p1.equals(p2));
            assertTrue(p2.equals(p1));
            assertEquals(p1.hashCode(), p2.hashCode());
        });
    }

    @Test
    public void basicEqualsHashCodeContractIsRespected() {
        final SlashPath path = SlashPath.fromString("foo");
        assertNotNull(path);
        assertFalse(path.equals(new Object()));
    }

    public Iterator<Object[]> absoluteAndNormalizedTests() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"", false, true});
        list.add(new Object[]{"/", true, true});
        list.add(new Object[]{".", false, false});
        list.add(new Object[]{"/foo/bar", true, true});
        list.add(new Object[]{"/foo/..", true, false});
        list.add(new Object[]{"/foo/.", true, false});
        list.add(new Object[]{"foo", false, true});
        return list.iterator();
    }

    @Test
    public void absoluteAndNormalizedPathsAreDetectedAccurately(

    ) {
        absoluteAndNormalizedTests().forEachRemaining(o -> {
            final String input = (String) o[0];
            final boolean absolute = (boolean) o[1];
            final boolean normalized = (boolean) o[2];
            final SlashPath path = SlashPath.fromString(input);
            assertEquals(path.isAbsolute(), absolute);
            assertEquals(path.isNormalized(), normalized);
        });
    }

    @Test
    public void getNameThrowsIAEOnEmptyPath() {
        try {
            SlashPath.fromString("").getName(0);
            fail("No exception thrown!");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "path has no elements");
        }
    }

    public Iterator<Object[]> getNameData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"/a/b/c/d", 4, 2, "c", "d"});
        list.add(new Object[]{"/a", 1, 0, "a", "a"});
        list.add(new Object[]{"/a/..", 2, 0, "a", ".."});
        list.add(new Object[]{"/a/../.", 3, 0, "a", "."});

        return list.iterator();
    }

    @Test
    public void getNameAndLastNameWorkCorrectly() {
        getNameData().forEachRemaining(o -> {
            final String input = (String) o[0];
            final int nameCount = (int) o[1];
            final int index = (int) o[2];
            final String name = (String) o[3];
            final String lastName = (String) o[4];
            final SlashPath path = SlashPath.fromString(input);
            final SlashPath component = SlashPath.fromString(name);
            final SlashPath last = SlashPath.fromString(lastName);

            assertEquals(path.getNameCount(), nameCount);
            assertEquals(path.getName(index), component);
            assertEquals(path.getLastName(), last);

        });
    }

    public Iterator<Object[]> getIllegalNameData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"/1/2/3", -1, "invalid index -1"});
        list.add(new Object[]{"/1/2/3", 3, "invalid index 3"});
        list.add(new Object[]{"/", 0, "path has no elements"});
        list.add(new Object[]{"/", -1, "path has no elements"});
        list.add(new Object[]{"", 0, "path has no elements"});
        return list.iterator();
    }

    @Test
    public void getNameWithIllegalArgumentsThrowsIAE() {
        getIllegalNameData().forEachRemaining(o -> {
            final String input = (String) o[0];
            final int index = (int) o[1];
            final String message = (String) o[2];
            final SlashPath path = SlashPath.fromString(input);
            try {
                path.getName(index);
                fail("No exception thrown!");
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), message);
            }
        });
    }

    public Iterator<Object[]> getParentData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"", null});
        list.add(new Object[]{"/", null});
        list.add(new Object[]{"/a", "/"});
        list.add(new Object[]{"a", null});
        list.add(new Object[]{"/a/b", "/a"});
        list.add(new Object[]{"/a/b/..", "/a/b"});

        return list.iterator();
    }

    @Test
    public void getParentWorks() {
        getParentData().forEachRemaining(o -> {
            final String input = (String) o[0];
            final String output = (String) o[1];
            final SlashPath path = SlashPath.fromString(input);
            final SlashPath expected = output == null ? null
                    : SlashPath.fromString(output);
            assertEquals(path.getParent(), expected);
        });
    }

    public Iterator<Object[]> getIllegalSubpathData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{-1, 2, "start index (-1) must not be negative"});
        list.add(new Object[]{3, 2, "end index (2) must not be less than start index (3)"});
        list.add(new Object[]{0, -1, "end index (-1) must not be less than start index (0)"});
        list.add(new Object[]{0, 4, "end index (4) must not be greater than size (3)"});
        return list.iterator();
    }

    @Test
    public void illegalSubpathIndicesThrowIAE() {
        getIllegalSubpathData().forEachRemaining(o -> {
            final int start = (int) o[0];
            final int end = (int) o[1];
            final String message = (String) o[2];
            try {
                TESTPATH.subpath(start, end);
                fail("No exception thrown!");
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), message);
            }
        });
    }

    public Iterator<Object[]> getSubpathData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"x", 0, 1, "x"});
        list.add(new Object[]{"/x", 0, 1, "x"});
        list.add(new Object[]{"/a/b/c/d/e/f", 2, 6, "c/d/e/f"});
        list.add(new Object[]{"/a/b/c/d/e/f", 0, 6, "a/b/c/d/e/f"});
        list.add(new Object[]{"/a/b/c/d/e/f", 1, 4, "b/c/d"});
        list.add(new Object[]{"/a/b/c/d/e/f", 2, 2, ""});
        return list.iterator();
    }

    @Test
    public void subpathWorksAsIntended() {
        getSubpathData().forEachRemaining(o -> {
            final String orig = (String) o[0];
            final int start = (int) o[1];
            final int end = (int) o[2];
            final String ret = (String) o[3];
            final SlashPath path = SlashPath.fromString(orig);
            final SlashPath expected = SlashPath.fromString(ret);
            assertEquals(path.subpath(start, end), expected);
        });
    }

    public Iterator<Object[]> getStartsWithData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"/foo", "", false});
        list.add(new Object[]{"foo", "", false});
        list.add(new Object[]{"/foo", "foo", false});
        list.add(new Object[]{"foo", "/foo", false});
        list.add(new Object[]{"/foo", "/foo", true});
        list.add(new Object[]{"foo", "foo", true});
        list.add(new Object[]{"foo/bar/baz", "foo", true});
        list.add(new Object[]{"foo", "foo/bar/baz", false});
        list.add(new Object[]{"foo/..", "foo", true});
        list.add(new Object[]{"/", "/", true});
        return list.iterator();
    }

    @Test
    public void startsWithWorksCorrectly() {
        getStartsWithData().forEachRemaining(o -> {
            final String orig = (String) o[0];
            final String against = (String) o[1];
            final boolean expected = (boolean) o[2];
            final SlashPath me = SlashPath.fromString(orig);
            final SlashPath him = SlashPath.fromString(against);
            assertEquals(me.startsWith(him), expected);
        });
    }

    public Iterator<Object[]> getEndsWithData() {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{"foo", "/foo", false});
        list.add(new Object[]{"/foo", "foo", true});
        list.add(new Object[]{"foo", "foo", true});
        list.add(new Object[]{"/foo", "/foo", true});
        list.add(new Object[]{"/foo/bar", "/bar", false});
        list.add(new Object[]{"/foo/bar", "bar", true});
        list.add(new Object[]{"foo/bar", "bar", true});
        list.add(new Object[]{"foo/bar", "/bar", false});
        list.add(new Object[]{"/a/b/c/d/e/f", "e/f", true});
        list.add(new Object[]{"/a/b/c/d/e/f", "e/fg", false});
        return list.iterator();
    }

    @Test
    public void endsWithWorksCorrectly() {
        getEndsWithData().forEachRemaining(o -> {
            final String orig = (String) o[0];
            final String against = (String) o[1];
            final boolean expected = (boolean) o[2];
            final SlashPath me = SlashPath.fromString(orig);
            final SlashPath him = SlashPath.fromString(against);
            assertEquals(me.endsWith(him), expected);
        });
    }
}
