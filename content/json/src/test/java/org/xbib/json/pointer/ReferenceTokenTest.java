
package org.xbib.json.pointer;

import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

public final class ReferenceTokenTest extends Assert {

    @Test
    public void nullCookedRaisesError()
            throws JsonPointerException {
        try {
            ReferenceToken.fromCooked(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            //assertEquals(e.getMessage(), BUNDLE.getMessage("nullInput"));
        }
    }

    @Test
    public void nullRawRaisesError() {
        try {
            ReferenceToken.fromRaw(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            //assertEquals(e.getMessage(), BUNDLE.getMessage("nullInput"));
        }
    }

    @Test
    public void emptyEscapeRaisesTheAppropriateException() {
        try {
            ReferenceToken.fromCooked("whatever~");
            fail("No exception thrown!!");
        } catch (JsonPointerException e) {
            //assertEquals(e.getMessage(), BUNDLE.getMessage("emptyEscape"));
        }
    }

    @Test
    public void illegalEscapeRaisesTheAppropriateException() {
        try {
            ReferenceToken.fromCooked("~a");
            fail("No exception thrown!!");
        } catch (JsonPointerException e) {
            //assertEquals(e.getMessage(), BUNDLE.getMessage("illegalEscape"));
        }
    }

    @Test
    public void fromCookedOrFromRawYieldsSameResults()
            throws JsonPointerException {
        List<Object[]> list = Arrays.asList(
                new Object[]{"~0", "~"},
                new Object[]{"~1", "/"},
                new Object[]{"", ""},
                new Object[]{"~0user", "~user"},
                new Object[]{"foobar", "foobar"},
                new Object[]{"~1var~1lib~1mysql", "/var/lib/mysql"}
        );
        for (Object[] o : list) {
            String cooked = (String) o[0];
            String raw = (String) o[1];
            final ReferenceToken token1 = ReferenceToken.fromCooked(cooked);
            final ReferenceToken token2 = ReferenceToken.fromRaw(raw);

            assertTrue(token1.equals(token2));
            assertEquals(token2.toString(), cooked);
        }
    }

    @Test
    public void fromIndexOrStringYieldsSameResults()
            throws JsonPointerException {
        List<Object[]> list = Arrays.asList(
                new Object[]{0, "0"},
                new Object[]{-1, "-1"},
                new Object[]{13, "13"}
        );
        for (Object[] o : list) {
            int index = (int) o[0];
            String asString = (String) o[1];
            final ReferenceToken fromInt = ReferenceToken.fromInt(index);
            final ReferenceToken cooked = ReferenceToken.fromCooked(asString);
            final ReferenceToken raw = ReferenceToken.fromRaw(asString);
            assertTrue(fromInt.equals(cooked));
            assertTrue(cooked.equals(raw));
            assertTrue(raw.equals(fromInt));
            assertEquals(fromInt.toString(), asString);
        }
    }

    @Test
    public void zeroAndZeroZeroAreNotTheSame()
            throws JsonPointerException {
        final ReferenceToken zero = ReferenceToken.fromCooked("0");
        final ReferenceToken zerozero = ReferenceToken.fromCooked("00");

        assertFalse(zero.equals(zerozero));
    }
}
