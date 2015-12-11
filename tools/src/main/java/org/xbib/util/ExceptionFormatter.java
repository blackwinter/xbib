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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

/**
 * Format java exception messages and stack traces.
 */
public final class ExceptionFormatter {

    private ExceptionFormatter() {
    }

    /**
     * Append Exception to string builder
     */
    @SuppressWarnings("rawtypes")
    public static void append(StringBuilder buf, Throwable t,
                              int level, boolean details) {
        try {
            if (((t != null) && (t.getMessage() != null))
                    && (t.getMessage().length() > 0)) {
                if (details && (level > 0)) {
                    buf.append("\n\nCaused by\n");
                }
                buf.append(t.getMessage());
            }
            if (details) {
                if (t != null) {
                    if ((t.getMessage() != null)
                            && (t.getMessage().length() == 0)) {
                        buf.append("\n\nCaused by ");
                    } else {
                        buf.append("\n\n");
                    }
                }
                StringWriter sw = new StringWriter();
                if (t != null) {
                    t.printStackTrace(new PrintWriter(sw));
                }
                buf.append(sw.toString());
            }
            if (t != null) {
                Method method = t.getClass().getMethod("getCause",
                        new Class[]{});
                Throwable cause = (Throwable) method.invoke(t,
                        (Object) null);
                if (cause != null) {
                    append(buf, cause, level + 1, details);
                }
            }
        } catch (Exception ex) {
            //
        }
    }

    /**
     * Format exception with stack trace
     *
     * @param t the thrown object
     * @return the formatted exception
     */
    public static String format(Throwable t) {
        StringBuilder sb = new StringBuilder();
        append(sb, t, 0, true);
        return sb.toString();
    }
}
