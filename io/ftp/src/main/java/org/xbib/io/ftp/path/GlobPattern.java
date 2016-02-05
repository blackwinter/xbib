package org.xbib.io.ftp.path;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A class for POSIX glob pattern with brace expansions.
 */
public class GlobPattern {

    private static final char BACKSLASH = '\\';

    private Pattern compiled;

    /**
     * Construct the glob pattern object with a glob pattern string
     *
     * @param globPattern the glob pattern string
     */
    public GlobPattern(String globPattern) {
        set(globPattern);
    }

    /**
     * @return the compiled pattern
     */
    public Pattern compiled() {
        return compiled;
    }

    /**
     * Compile glob pattern string
     *
     * @param globPattern the glob pattern
     * @return the pattern object
     */
    public static Pattern compile(String globPattern) {
        return new GlobPattern(globPattern).compiled();
    }

    /**
     * Match input against the compiled glob pattern
     *
     * @param s input chars
     * @return true for successful matches
     */
    public boolean matches(CharSequence s) {
        return compiled.matcher(s).matches();
    }

    /**
     * Set and compile a glob pattern
     *
     * @param glob the glob pattern string
     */
    private void set(String glob) {
        StringBuilder regex = new StringBuilder();
        int setOpen = 0;
        int curlyOpen = 0;
        int len = glob.length();
        for (int i = 0; i < len; i++) {
            char c = glob.charAt(i);
            switch (c) {
                case BACKSLASH:
                    if (++i >= len) {
                        error("Missing escaped character", glob, i);
                    }
                    regex.append(c).append(glob.charAt(i));
                    continue;
                case '.':
                case '$':
                case '(':
                case ')':
                case '|':
                case '+':
                    // escape regex special chars that are not glob special chars
                    regex.append(BACKSLASH);
                    break;
                case '*':
                    regex.append('.');
                    break;
                case '?':
                    regex.append('.');
                    continue;
                case '{': // start of a group
                    regex.append("(?:"); // non-capturing
                    curlyOpen++;
                    continue;
                case ',':
                    regex.append(curlyOpen > 0 ? '|' : c);
                    continue;
                case '}':
                    if (curlyOpen > 0) {
                        // end of a group
                        curlyOpen--;
                        regex.append(")");
                        continue;
                    }
                    break;
                case '[':
                    if (setOpen > 0) {
                        error("Unclosed character class", glob, i);
                    }
                    setOpen++;
                    break;
                case '^': // ^ inside [...] can be unescaped
                    if (setOpen == 0) {
                        regex.append(BACKSLASH);
                    }
                    break;
                case '!': // [! needs to be translated to [^
                    regex.append(setOpen > 0 && '[' == glob.charAt(i - 1) ? '^' : '!');
                    continue;
                case ']':
                    // Many set errors like [][] could not be easily detected here,
                    // as []], []-] and [-] are all valid POSIX glob and java regex.
                    // We'll just let the regex compiler do the real work.
                    setOpen = 0;
                    break;
                default:
            }
            regex.append(c);
        }
        if (setOpen > 0) {
            error("Unclosed character class", glob, len);
        }
        if (curlyOpen > 0) {
            error("Unclosed group", glob, len);
        }
        compiled = Pattern.compile(regex.toString());
    }

    private static void error(String message, String pattern, int pos) {
        throw new PatternSyntaxException(message, pattern, pos);
    }
}