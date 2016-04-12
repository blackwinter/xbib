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
 *
 *  Record status
 *
 *  One-character alphabetic code that indicates the relationship of the record to a file
 *  for file maintenance purposes.
 *
 *  a - Increase in encoding level
 *  Encoding level (Leader/17) of the record has been changed to a higher encoding level.
 *
 *  Indicates an increase in the level of cataloging (e.g., code a is used when a preliminary
 *  cataloging record (code 5 in Leader/17) is raised to full cataloging level (code # in Leader/17)).
 *
 *  c - Corrected or revised
 *  Addition/change other than in the Encoding level code has been made to the record.
 *
 *  d - Deleted
 *  Record has been deleted.
 *
 *  n - New
 *  Record is newly input.
 *
 *  p - Increase in encoding level from prepublication
 *  Prepublication record has had a change in cataloging level resulting from the availability
 *  of the published item.
 *
 *  Example: a CIP record (code 8 in Leader/17)) upgraded to a full record (code # or 1 in Leader/17.)
 */
public enum RecordStatus {

    INCREASE_IN_ENCODING_LEVEL('a'),
    CORRECTED_OR_REVISED('c'),
    DELETED('d'),
    NEW('n'),
    INCREASE_IN_ENCODING_LEVEL_FROM_PREPUBLICATION('p')
    ;

    char ch;
    RecordStatus(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
