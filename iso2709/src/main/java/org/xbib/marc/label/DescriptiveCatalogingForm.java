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
package org.xbib.marc.label;

/**
 * Descriptive cataloging form
 *
 * One-character alphanumeric code that indicates characteristics of the descriptive data in the record through reference to cataloging norms. Subfield $e (Description conventions) of field 040 (Cataloging Source) also contains information on the cataloging conventions used.
 *
 * # - Non-ISBD
 * Descriptive portion of the record does not follow International Standard Bibliographic Description (ISBD) cataloging and punctuation provisions.
 *
 * a - AACR 2
 * Descriptive portion of the record is formulated according to the description and punctuation provisions as incorporated into the Anglo-American Cataloging Rules, 2nd Edition (AACR 2) and its manuals.
 *
 * c - ISBD punctuation omitted
 * Descriptive portion of the record contains the punctuation provisions of ISBD, except ISBD punctuation is not present at the end of a subfield.
 *
 * i - ISBD punctuation included
 * Descriptive portion of the record contains the punctuation provisions of ISBD.
 *
 * u - Unknown
 * Institution receiving or sending data in Leader/18 cannot adequately determine the appropriate descriptive cataloging form used in the record. May be used in records converted from another metadata format.
 *
 */
public enum DescriptiveCatalogingForm {

    NON_ISBD(' '),
    AACR2('a'),
    ISBD_PUNCTUATION_OMITTED('c'),
    ISBD_PUNCTUATION_INCLUDED('i'),
    UNKNOWN('u')
    ;

    char ch;
    DescriptiveCatalogingForm(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
