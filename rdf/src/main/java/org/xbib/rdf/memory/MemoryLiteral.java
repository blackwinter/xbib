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
package org.xbib.rdf.memory;

import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;

/**
 * A simple Literal is a value of object type
 */
public class MemoryLiteral implements Literal, Comparable<Literal> {

    private Object value;

    private IRI type;

    private String lang;

    public MemoryLiteral(Object value) {
        this.value = value;
    }

    public MemoryLiteral(Object value, String lang) {
        this.value = value;
        this.lang = lang;
    }

    @Override
    public MemoryLiteral object(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public MemoryLiteral type(IRI type) {
        this.type = type;
        return this;
    }

    @Override
    public IRI type() {
        return type;
    }

    @Override
    public MemoryLiteral language(String lang) {
        this.lang = lang;
        return this;
    }

    @Override
    public String language() {
        return lang;
    }

    @Override
    public int compareTo(Literal that) {
        if (this == that) {
            return 0;
        }
        if (that == null) {
            return 1;
        }
        return toString().compareTo(that.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Literal)) {
            return false;
        }
        final Literal that = (Literal) obj;
        return equal(this.value, that.object())
                && equal(this.lang, that.language())
                && equal(this.type, that.type());
    }

    @Override
    public int hashCode() {
        return (value + lang + type).hashCode();
    }

    @Override
    public String toString() {
        return lexicalValue();
    }

    public String lexicalValue() {
        return (value != null ? value : "")
                + (lang != null ? "@" + lang : "")
                + (type != null ? "^^" + type : "");
    }

    @Override
    public Object object() {
        if (type == null) {
            return value;
        }
        if (value == null) {
            return null;
        }
        String s = value.toString();
        switch (type.toString()) {
            case "xsd:long":
                return Long.parseLong(s);
            case "xsd:int":
                return Integer.parseInt(s);
            case "xsd:boolean":
                return Boolean.parseBoolean(s);
            case "xsd:float":
                return Float.parseFloat(s);
            case "xsd:double":
                return Double.parseDouble(s);
            case "xsd:gYear":
                return Integer.parseInt(s);
            // add more xsd here ...
            default:
                return s;
        }
    }

    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

}
