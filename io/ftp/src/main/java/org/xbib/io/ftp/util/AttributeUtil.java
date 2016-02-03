package org.xbib.io.ftp.util;

import java.util.EnumSet;
import java.util.regex.Pattern;

public final class AttributeUtil {
    private static final Pattern COMMA = Pattern.compile(",");
    private static final String ALL = "*";
    private static final String BASIC = "basic";
    private static final char COLON = ':';

    private AttributeUtil() {
    }

    public static EnumSet<BasicFileAttributesEnum> getAttributes(
            final String input) {
        final String[] attrs = COMMA.split(input);
        final EnumSet<BasicFileAttributesEnum> set
                = EnumSet.noneOf(BasicFileAttributesEnum.class);

        String type, name;
        int index;
        BasicFileAttributesEnum attr;

        for (final String attrName : attrs) {
            if (ALL.equals(attrName)) {
                return EnumSet.allOf(BasicFileAttributesEnum.class);
            }
            index = attrName.indexOf(COLON);
            if (index == -1) {
                type = BASIC;
                name = attrName;
            } else {
                type = attrName.substring(0, index);
                name = attrName.substring(index + 1, attrName.length());
            }
            if (!BASIC.equals(type)) {
                throw new UnsupportedOperationException();
            }
            if (ALL.equals(name)) {
                return EnumSet.allOf(BasicFileAttributesEnum.class);
            }
            attr = BasicFileAttributesEnum.forName(name);
            if (attr == null) {
                throw new UnsupportedOperationException();
            }
            set.add(attr);
        }

        return EnumSet.copyOf(set);
    }
}
