package org.xbib.util;

import java.util.Locale;

public class LocaleUtil {

    public static Locale findLocale(String languages) {
        Locale locale = Locale.getDefault();
        if (languages != null) {
            for (String lang : languages.split(",")) {
                int pos = lang.indexOf(';');
                if (pos != -1) {
                    lang = lang.substring(0, pos);
                }
                lang = lang.trim();
                pos = lang.indexOf('-');
                if (pos  == -1) {
                    return new Locale(lang, "");
                } else {
                    return new Locale(lang.substring(0, pos), lang.substring(pos + 1));
                }
            }
        }
        return locale;
    }
}
