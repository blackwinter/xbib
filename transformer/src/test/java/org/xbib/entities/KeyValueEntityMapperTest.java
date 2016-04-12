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
package org.xbib.entities;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.etl.marc.MARCSpecification;
import org.xbib.etl.Entity;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;

import java.util.Map;
import java.util.TreeMap;

public class KeyValueEntityMapperTest extends Assert {

    @Test
    public void testMARCSubfields() throws Exception {
        String value = "100$01$abc";
        Entity entity = new NullEntity();
        Map map = new TreeMap(); // for sorted output in assertEquals matching
        MARCSpecification specification = new MARCSpecification();
        Map m = specification.addKey(value, entity, map);
        value = "100$02$abc";
        entity = new NullEntity();
        m = specification.addKey(value, entity, m);
        value = "100$02$def";
        entity = new NullEntity();
        m = specification.addKey(value, entity, m);
        value = "200$02$abc";
        entity = new NullEntity();
        m = specification.addKey(value, entity, m);
        assertEquals("{100={01={abc=<null>}, 02={abc=<null>, def=<null>}}, 200={02={abc=<null>}}}", m.toString());
        Entity e = specification.getEntity("100$01$abc", m);
        assertEquals("<null>", e.toString());
        e = specification.getEntity("100$01$def", m);
        assertNull(e);
    }

    @Test
    public void testMARCField() throws Exception {
        String value = "100$01$ab";
        Entity entity = new NullEntity();
        Map map = new TreeMap(); // for sorted output in assertEquals matching
        MARCSpecification specification = new MARCSpecification();
        Map m = specification.addKey(value, entity, map);
        Field f = new Field().tag("100").indicator("01");
        Field f1 = new Field(f).subfieldId("a").data("Hello");
        Field f2 = new Field(f).subfieldId("b").data("World");
        FieldList c = new FieldList();
        c.add(f);
        c.add(f1);
        c.add(f2);
        Entity e = specification.getEntity(c.toKey(), m);
        assertNotNull(e);
    }

    @Test
    public void testMARCControlField() throws Exception {
        String value = "001";
        Entity entity = new NullEntity();
        Map map = new TreeMap(); // for sorted output in assertEquals matching
        MARCSpecification specification = new MARCSpecification();
        Map m = specification.addKey(value, entity, map);
        Field f = new Field().tag("001");
        Field f1 = new Field(f).data("123456");
        FieldList c = new FieldList();
        c.add(f);
        c.add(f1);
        Entity e = specification.getEntity(c.toKey(), m);
        assertNotNull(e);
    }

}
