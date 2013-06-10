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
package org.xbib.sru.scan;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

import org.xbib.io.OutputFormat;
import org.xbib.sru.DefaultSRUResponse;

/**
 * Scan response (not yet implemented)
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ScanResponse extends DefaultSRUResponse {

    private final ScanRequest request;

    public ScanResponse(ScanRequest request) {
        this.request = request;
    }

    @Override
    public ScanResponse setOutputFormat(OutputFormat format) {
        return this;
    }

    @Override
    public ScanResponse to(Writer writer) throws IOException {
        // TODO XMLEvents
        InputStreamReader stream = new InputStreamReader(
                getClass().getResourceAsStream("/org/xbib/sru/explain.xml"), "UTF-8");
        final char[] chars = new char[4096];
        while (stream.ready()) {
            int n = stream.read(chars);
            writer.write(chars, 0, n);
        }
        writer.flush();
        return this;
    }

}
