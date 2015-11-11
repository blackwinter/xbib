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
package org.xbib.marc;

import org.testng.annotations.Test;
import org.xbib.io.field.FieldSortable;
import org.xbib.marc.label.RecordLabel;

import static org.testng.Assert.assertEquals;

public class FieldTest {

    @Test
    public void testFieldData() {
        Field f = new Field().tag("100").indicator("").data("Hello World");
        assertEquals(f.data(), "Hello World");
    }

    @Test
    public void testFieldDataSortable() {
        Field f = new Field().tag("100").indicator("").data("\u0098Hello \u009CWorld");
        assertEquals(f.data(), "\u0098Hello \u009CWorld");
        assertEquals(f.dataSortable(), "World");
    }

    @Test
    public void sortableTest() {
        Field f = new Field().tag("331").indicator("")
                .subfieldId(null)
                .data('\u0098' + "Der" + '\u009c' + " kleine Prinz");

        assertEquals(f.data(), FieldSortable.NON_SORTABLE_BEGIN + "Der" + FieldSortable.NON_SORTABLE_END + " kleine Prinz");
        assertEquals(f.dataSortable(), " kleine Prinz");
    }

    @Test
    public void testLonerField() {
        Field f = new Field();
        FieldList c = new FieldList();
        c.add(f);
        assertEquals(c.toKey(), "$");
    }

    @Test
    public void testSingleTagField() {
        Field f = new Field().tag("100");
        FieldList c = new FieldList();
        c.add(f);
        assertEquals(c.toKey(), "100$");
    }

    @Test
    public void testSingleFieldWithIndicators() {
        Field f = new Field().tag("100").indicator("01");
        FieldList c = new FieldList();
        c.add(f);
        assertEquals(c.toKey(), "100$01$");
    }

    @Test
    public void testSingleFieldWithSubfields() {
        Field f = new Field().tag("100").indicator("01");
        Field f1 = new Field(f).subfieldId("1");
        Field f2 = new Field(f).subfieldId("2");
        FieldList c = new FieldList();
        c.add(f);
        c.add(f1);
        c.add(f2);
        assertEquals(c.toKey(), "100$01$12");
    }

    @Test
    public void testNumericSubfields() {
        Field f = new Field().tag("016");
        Field f1 = new Field(f).subfieldId("1");
        Field f2 = new Field(f).subfieldId("2");
        Field f3 = new Field(f).subfieldId("3");
        FieldList c = new FieldList();
        c.add(f);
        c.add(f1);
        c.add(f2);
        c.add(f3);
        assertEquals(c.toKey(), "016$123");
    }

    @Test
    public void testAlphabeticSubfields() {
        Field f = new Field().tag("016");
        Field f1 = new Field(f).subfieldId("a");
        Field f2 = new Field(f).subfieldId("b");
        Field f3 = new Field(f).subfieldId("c");
        FieldList c = new FieldList();
        c.add(f);
        c.add(f1);
        c.add(f2);
        c.add(f3);
        assertEquals(c.toKey(), "016$abc");
    }

    @Test
    public void testRepeatingSubfields() {
        Field f = new Field().tag("016");
        Field f1 = new Field(f).subfieldId("a");
        Field f2 = new Field(f).subfieldId("a");
        Field f3 = new Field(f).subfieldId("a");
        FieldList c = new FieldList();
        c.add(f);
        c.add(f1);
        c.add(f2);
        c.add(f3);
        assertEquals(c.toKey(), "016$aaa");
    }

    @Test
    public void testEmptyIndicatorWithSubfields() {
        Field f = new Field().tag("016").indicator("").subfieldId(null).data(null);
        Field f1 = new Field(f).subfieldId("1");
        Field f2 = new Field(f).subfieldId("2");
        Field f3 = new Field(f).subfieldId("3");
        FieldList c = new FieldList();
        c.add(f);
        c.add(f1);
        c.add(f2);
        c.add(f3);
        assertEquals(c.toKey(), "016$$123");
    }

    // 901  =, 901  a=98502599, 901  d=0, 901  e=14, 901  =f, 901  =h]
    @Test
    public void testBeginEndFields() {
        Field f = new Field().tag("901").indicator("  ");
        Field f1 = new Field(f).subfieldId("a");
        Field f2 = new Field(f).subfieldId("d");
        Field f3 = new Field(f).subfieldId("e");
        Field f4 = new Field(f);
        Field f5 = new Field(f);
        FieldList c = new FieldList();
        c.add(f);
        c.add(f1);
        c.add(f2);
        c.add(f3);
        c.add(f4);
        c.add(f5);
        assertEquals(c.toKey(), "901$  $ade");
    }

    @Test
    public void testWrongRecordLabel() {
        String s = "123456789";
        RecordLabel label = new RecordLabel(s.toCharArray());
        assertEquals(label.getRecordLabel().length(), RecordLabel.LENGTH);
        s = "123456789012345678901234567890";
        label = new RecordLabel(s.toCharArray());
        assertEquals(label.getRecordLabel().length(), RecordLabel.LENGTH);
    }
}
