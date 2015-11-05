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
package org.xbib.io.iso23950;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Diagnostics for Z server
 *
 */
public class Diagnostics extends IOException {

    private static final ResourceBundle bundle =
            ResourceBundle.getBundle("org.xbib.io.iso23950.diagnostics");

    private int diagCode;

    private String message;

    private String details;

    public Diagnostics(int diagCode) {
        super("" + diagCode);
        this.diagCode = diagCode;
    }

    public Diagnostics(int diagCode, String message) {
        super("" + diagCode + " " + message);
        this.diagCode = diagCode;
        this.message = message;
    }

    public Diagnostics(int diagCode, String message, String details) {
        super("" + diagCode + " " + message + " " + details);
        this.diagCode = diagCode;
        this.message = message;
        this.details = details;
    }

    public String getPlainText() {
        String[] s = bundle.getString(Integer.toString(diagCode)).split("\\|");
        if (s != null) {
        if (message == null) {
            message = s.length > 0 ? s[1] : "<undefined>";
        }
        if (details == null) {
            details = s.length > 1 ? s[2] : "<undefined>";
        }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("diag code=").append(diagCode)
                .append(" message=").append(message)
                .append(" details=").append(details);
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getPlainText();
    }
}
