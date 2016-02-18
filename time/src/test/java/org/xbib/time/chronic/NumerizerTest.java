package org.xbib.time.chronic;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.time.chronic.numerizer.Numerizer;

import java.util.LinkedHashMap;
import java.util.Map;

public class NumerizerTest extends Assert {

    @Test
    public void testStraightParsing() {
        Map<Integer, String> strings = new LinkedHashMap<>();
        strings.put(1, "one");
        strings.put(5, "five");
        strings.put(10, "ten");
        strings.put(11, "eleven");
        strings.put(12, "twelve");
        strings.put(13, "thirteen");
        strings.put(14, "fourteen");
        strings.put(15, "fifteen");
        strings.put(16, "sixteen");
        strings.put(17, "seventeen");
        strings.put(18, "eighteen");
        strings.put(19, "nineteen");
        strings.put(20, "twenty");
        strings.put(27, "twenty seven");
        strings.put(31, "thirty-one");
        strings.put(59, "fifty nine");
        strings.put(100, "a hundred");
        strings.put(100, "one hundred");
        strings.put(150, "one hundred and fifty");
        //   strings.put(Integer.valueOf(150), "one fifty");
        strings.put(200, "two-hundred");
        strings.put(500, "5 hundred");
        strings.put(999, "nine hundred and ninety nine");
        strings.put(1000, "one thousand");
        strings.put(1200, "twelve hundred");
        strings.put(1200, "one thousand two hundred");
        strings.put(17000, "seventeen thousand");
        strings.put(21473, "twentyone-thousand-four-hundred-and-seventy-three");
        strings.put(74002, "seventy four thousand and two");
        strings.put(99999, "ninety nine thousand nine hundred ninety nine");
        strings.put(100000, "100 thousand");
        strings.put(250000, "two hundred fifty thousand");
        strings.put(1000000, "one million");
        strings.put(1250007, "one million two hundred fifty thousand and seven");
        strings.put(1000000000, "one billion");
        strings.put(1000000001, "one billion and one");

        for (Integer value : strings.keySet()) {
            String str = strings.get(value);
            assertEquals(value.intValue(), Integer.parseInt(Numerizer.numerize(str)));
        }
    }
}
