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
 * Encoding level
 *
 * One-character alphanumeric code that indicates the fullness of the bibliographic information
 * and/or content designation of the MARC record.
 *
 * # - Full level
 * Most complete MARC record created from information derived from an inspection of the physical item.
 *
 * For serials, at least one issue of the serial is inspected.
 *
 * 1 - Full level, material not examined
 * Next most complete MARC record after the full level created from information derived from an extant
 * description of the item (e.g., a printed catalog card or a description in an institutional guide)
 * without reinspection of the physical item. Used primarily in the retrospective conversion of records
 * when all of the information on the extant description is transcribed.
 * Certain control field coding and other data (e.g., field 043 (Geographic Area Code)) are based
 * only on explicit information in the description.
 *
 * 2 - Less-than-full level, material not examined
 * Less-than-full level record (i.e., a record that falls between minimal level and full) created
 * from an extant description of the material (e.g., a printed catalog card) without reinspection of
 * the physical item. Used primarily in the retrospective conversion of records when all of the descriptive access points but only a specified subset of other data elements are transcribed. Authoritative headings may not be current.
 *
 * 3 - Abbreviated level
 * Brief record that does not meet minimal level cataloging specifications. Headings in the records may reflect established forms to the extent that such forms were available at the time the record was created.
 *
 * 4 - Core level
 * Less-than-full but greater-than-minimal level cataloging record that meets core record standards for completeness.
 *
 * 5 - Partial (preliminary) level
 * Preliminary cataloging level record that is not considered final by the creating agency (e.g., the headings may not reflect established forms; the record may not meet national-level cataloging specifications).
 *
 * 7 - Minimal level
 * Record that meets the U.S. National Level Bibliographic Record minimal level cataloging specifications and is considered final by the creating agency. Headings have been checked against an authority file and reflect established forms to the extent that such forms were available at the time the minimal level record was created. The U.S. requirements for minimal-level records can be found in National Level and Minimal Level Record Requirements
 *
 * 8 - Prepublication level
 * Prepublication level record. Includes records created in cataloging in publication programs.
 *
 * u - Unknown
 * Used by an agency receiving or sending data with a local code in Leader/17 cannot adequately determine the appropriate encoding level of the record. Code u thus replaces the local code. Not used in newly input or updated records.
 *
 * For example, code u is used in Dublin Core originated records.
 *
 * z - Not applicable
 * Concept of encoding level does not apply to the record.
 *
 */
public enum EncodingLevel {

    FULL(' '),
    FULL_NOT_EXAMINED('1'),
    LESS_THAN_FULL_NOT_EXAMINED('2'),
    ABBREV('3'),
    CORE('4'),
    PARTIAL('5'),
    MINIMAL('7'),
    PREPUBLICATION('8'),
    UNKNOWN('u'),
    NOT_APPLICABLE('z')
    ;

    char ch;
    EncodingLevel(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
