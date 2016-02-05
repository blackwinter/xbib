package org.xbib.io.ftp;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.regex.Pattern;

class FTPPathMatcher implements PathMatcher {

    private final Pattern pattern;

    public FTPPathMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matches(Path path) {
        return path != null && pattern.matcher(path.toString()).matches();
    }
}