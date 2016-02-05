package org.xbib.io.sftp;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.regex.Pattern;

class SFTPPathMatcher implements PathMatcher {

    private final Pattern pattern;

    public SFTPPathMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matches(Path path) {
        return path != null && pattern.matcher(path.toString()).matches();
    }
}