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
package org.xbib.io.field;

import org.junit.Assert;
import org.junit.Test;

public class MarcFieldTest extends Assert {

    @Test
    public void testFieldData() {
        MarcField marcField = MarcField.builder().tag("100").indicator("").value("Hello World").build();
        assertEquals(marcField.getValue(), "Hello World");
    }

    @Test
    public void testLonerField() {
        MarcField marcField = MarcField.builder().build();
        assertEquals("$$", marcField.toKey());
    }

    @Test
    public void testSingleTagField() {
        MarcField marcField = MarcField.builder().tag("100").build();
        assertEquals("100$$", marcField.toKey());
    }

    @Test
    public void testSingleFieldWithIndicators() {
        MarcField marcField = MarcField.builder()
                .tag("100")
                .indicator("01")
                .build();
        assertEquals("100$01$", marcField.toKey());
    }

    @Test
    public void testSingleFieldWithSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("100")
                .indicator("01")
                .subfield("1", null)
                .subfield("2", null)
                .build();
        assertEquals("100$01$12", marcField.toKey());
    }

    @Test
    public void testNumericSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("016")
                .subfield("1", null)
                .subfield("2", null)
                .subfield("3", null)
                .build();
        assertEquals("016$$123", marcField.toKey());
    }

    @Test
    public void testAlphabeticSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("016")
                .subfield("a", null)
                .subfield("b", null)
                .subfield("c", null)
                .build();
        assertEquals("016$$abc", marcField.toKey());
    }

    @Test
    public void testRepeatingSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("016")
                .subfield("a", null)
                .subfield("a", null)
                .subfield("a", null)
                .build();
        assertEquals("016$$aaa", marcField.toKey());
    }

    @Test
    public void testEmptyIndicatorWithSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("016")
                .subfield("1", null)
                .subfield("2", null)
                .subfield("3", null)
                .build();
        assertEquals("016$$123", marcField.toKey());
    }

    // 901  =, 901  a=98502599, 901  d=0, 901  e=14, 901  =f, 901  =h]
    @Test
    public void testBeginEndFields() {
        MarcField marcField = MarcField.builder()
                .tag("901")
                .indicator("  ")
                .subfield("a", null)
                .subfield("d", null)
                .subfield("e", null)
                .build();
        assertEquals("901$  $ade", marcField.toKey());
    }

}
