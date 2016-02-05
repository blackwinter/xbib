package org.xbib.io.ftp;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public enum BasicFileAttributesEnum {
    CREATION_TIME("creationTime") {
        @Override
        public Object getValue(final BasicFileAttributes attributes) {
            return attributes.creationTime();
        }
    },
    FILE_KEY("fileKey") {
        @Override
        public Object getValue(final BasicFileAttributes attributes) {
            return attributes.fileKey();
        }
    },
    IS_DIRECTORY("isDirectory") {
        @Override
        public Object getValue(final BasicFileAttributes attributes) {
            return attributes.isDirectory();
        }
    },
    IS_REGULAR_FILE("isRegularFile") {
        @Override
        public Object getValue(final BasicFileAttributes attributes) {
            return attributes.isRegularFile();
        }
    },
    IS_SYMBOLIC_LINK("isSymbolicLink") {
        @Override
        public Object getValue(final BasicFileAttributes attributes) {
            return attributes.isSymbolicLink();
        }
    },
    IS_OTHER("isOther") {
        @Override
        public Object getValue(final BasicFileAttributes attributes) {
            return attributes.isOther();
        }
    },
    LAST_ACCESS_TIME("lastAccessTime") {
        @Override
        public Object getValue(final BasicFileAttributes attributes) {
            return attributes.lastAccessTime();
        }
    },
    LAST_MODIFIED_TIME("lastModifiedTime") {
        @Override
        public Object getValue(final BasicFileAttributes attributes) {
            return attributes.lastModifiedTime();
        }
    },
    SIZE("size") {
        @Override
        public Object getValue(final BasicFileAttributes attributes) {
            return attributes.size();
        }

    };

    private static final Map<String, BasicFileAttributesEnum> REVERSE_MAP;

    static {
        REVERSE_MAP = new HashMap<>();

        for (final BasicFileAttributesEnum value : values()) {
            REVERSE_MAP.put(value.name, value);
        }
    }

    private final String name;

    BasicFileAttributesEnum(final String name) {
        this.name = name;
    }

    public static BasicFileAttributesEnum forName(final String name) {
        return REVERSE_MAP.get(name);
    }

    public abstract Object getValue(final BasicFileAttributes attributes);

    @Override
    public String toString() {
        return "basic:" + name;
    }
}
