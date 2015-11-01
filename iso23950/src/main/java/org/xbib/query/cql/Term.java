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
package org.xbib.query.cql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A CQL Term 
 *
 */
public class Term extends AbstractNode {

    private static final TimeZone tz = TimeZone.getTimeZone("GMT");
    private static final String ISO_FORMAT_SECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO_FORMAT_DAYS = "yyyy-MM-dd";

    private String value;
    private Long longvalue;
    private Double doublevalue;
    private Identifier identifier;
    private Date datevalue;
    private SimpleName name;

    public Term(String value) {
        this.value = value;
        try {
            // check for hidden dates. CQL does not support ISO dates.
            this.datevalue = parseDateISO(value);
            this.value = null;
        } catch (Exception e) {
            
        }
    }

    public Term(Identifier identifier) {
        this.identifier = identifier;
    }

    public Term(SimpleName name) {
        this.name = name;
    }

    public Term(Long value) {
        this.longvalue = value;
    }

    public Term(Double value) {
        this.doublevalue = value;
    }

    /**
     * Set value, useful for inline replacements
     * in spellcheck suggestions
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * If the value is a String it is embedded in quotation marks.
     * If its a Integer or a Double it is returned without
     * quotation marks.
     *
     * @return the value as String
     */
    public String getValue() {
        return longvalue != null ? Long.toString(longvalue)
                : doublevalue != null ? Double.toString(doublevalue)
                : value != null ? value
                : identifier != null ? identifier.toString()
                : name != null ? name.toString()
                : null;
    }

    public boolean isLong() {
        return longvalue != null;
    }

    public boolean isFloat() {
        return doublevalue != null;
    }

    public boolean isString() {
        return value != null;
    }

    public boolean isName() {
        return name != null;
    }

    public boolean isIdentifier() {
        return identifier != null;
    }
    
    public boolean isDate() {
        return datevalue != null;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    private Date parseDateISO(String value) {
        if (value == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(ISO_FORMAT_SECONDS);
        sdf.setTimeZone(tz);
        sdf.setLenient(true);
        try {
            return sdf.parse(value);
        } catch (ParseException pe) {
            // skip
        }
        sdf.applyPattern(ISO_FORMAT_DAYS);
        try {
            return sdf.parse(value);
        } catch (ParseException pe) {
            return null;
        }
    }

    private String formatDateISO(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(ISO_FORMAT_SECONDS);
        sdf.setTimeZone(tz);
        return sdf.format(date);
    }

    @Override
    public String toString() {
        return longvalue != null ? Long.toString(longvalue)
                : doublevalue != null ? Double.toString(doublevalue)
                : datevalue != null ? formatDateISO(datevalue)
                : value != null ? value.startsWith("\"") && value.endsWith("\"") ? value
                    : "\"" + value.replaceAll("\"", "\\\\\"") + "\""
                : identifier != null ? identifier.toString()
                : name != null ? name.toString()
                : null;
    }
}
