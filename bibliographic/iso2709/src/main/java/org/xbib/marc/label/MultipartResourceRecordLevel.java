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
 * Multipart resource record level
 * Record level to which a resource pertains and any record dependencies.
 * This information will facilitate processing the record in different situations.
 * For example, the record may describe a set of items, or it may describe a part of a set.
 * The part may only have a dependent title to be used for identification purposes thus
 * requiring use of additional information to understand its context.
 *
 * # - Not specified or not applicable
 * The distinction between record levels is not specified or not applicable for the type of resource.
 *
 * a - Set
 * Record is for a set consisting of multiple items.
 *
 * b - Part with independent title
 * The record is for a resource which is part of a set and has a title that allows it
 * to be independent of the set record.
 *
 * c - Part with dependent title
 * The record is for a resource which is part of a set but has a title that makes it dependent
 * on the set record to understand its context.
 */
public enum MultipartResourceRecordLevel {

    NOT_SPECIFIED(' '),
    SET('a'),
    PART_WITH_INDEPENDENT_TITLE('b'),
    PART_WITH_DEPENDENT_TITLE('c')
    ;

    char ch;
    MultipartResourceRecordLevel(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
