/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.util;

import org.xbib.time.Duration;
import org.xbib.time.PrettyTime;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtil {

    public static String convertFileSize(double size) {
        return convertFileSize(size, Locale.getDefault());
    }

    public static String convertFileSize(double size, Locale locale) {
        String strSize;
        long kb = 1024;
        long mb = 1024 * kb;
        long gb = 1024 * mb;
        long tb = 1024 * gb;

        NumberFormat formatter = NumberFormat.getNumberInstance(locale);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);

        if (size < kb) {
            strSize = size + " bytes";
        } else if (size < mb) {
            strSize = formatter.format(size / kb) + " KB";
        } else if (size < gb) {
            strSize = formatter.format(size / mb) + " MB";
        } else if (size < tb) {
            strSize = formatter.format(size / gb) + " GB";
        } else {
            strSize = formatter.format(size / tb) + " TB";
        }
        return strSize;
    }

    private final static PrettyTime pretty = new PrettyTime();

    public static String formatMillis(long millis) {
        Duration d = pretty.calculateDuration(millis);
        return pretty.format(d);
    }

}
