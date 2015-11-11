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
package org.xbib.marc.stream;

import org.testng.annotations.Test;
import org.xbib.io.field.Separable;
import org.xbib.marc.Iso2709Reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;

public class StreamTest {

    @Test
    public void testZDB() throws IOException {
        InputStream in = getClass().getResource("zdblokutf8.mrc").openStream();
        Iso2709Reader reader = new Iso2709Reader(in, "UTF-8");
        long count = reader.stream()
                    .fields()
                    .count();
        assertEquals(10171L, count);
        in = getClass().getResource("zdblokutf8.mrc").openStream();
        reader = new Iso2709Reader(in, "UTF-8");
        reader.stream()
                .fields()
                .forEach(new Consumer<Separable>() {
                    @Override
                    public void accept(Separable s) {
                        // TODO
                    }
                });
    }
}
