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
 * Bibliographic level
 *
 * One-character alphabetic code indicating the bibliographic level of the record.
 *
 * a - Monographic component part
 * Monographic bibliographic unit that is physically attached to or contained in another unit
 * such that the retrieval of the component part is dependent on the identification and location
 * of the host item or container. Contains fields that describe the component part and data that
 * identify the host, field 773 (Host Item Entry).
 *
 * Examples of monographic component parts with corresponding host items include an article in a
 * single issue of a periodical, a chapter in a book, a band on a phonodisc, and a map on a single
 * sheet that contains several maps.
 *
 * b - Serial component part
 * Serial bibliographic unit that is physically attached to or contained in another unit such
 * that the retrieval of the component part is dependent on the identification and location
 * of the host item or container. Contains fields that describe the component part and data that
 * identify the host, field 773 (Host Item Entry).
 *
 * Example of a serial component part with corresponding host item is a regularly appearing column
 * or feature in a periodical.
 *
 * c - Collection
 * Made-up multipart group of items that were not originally published, distributed, or produced
 * together. The record describes units defined by common provenance or administrative convenience
 * for which the record is intended as the most comprehensive in the system.
 *
 * d - Subunit
 * Part of collection, especially an archival unit described collectively elsewhere in the system.
 * Contains fields that describe the subunit and data that identify the host item.
 *
 * Subunits may be items, folders, boxes, archival series, subgroups, or subcollections.
 *
 * i - Integrating resource
 * Bibliographic resource that is added to or changed by means of updates that do not remain
 * discrete and are integrated into the whole. Examples include updating loose-leafs and
 * updating Web sites.
 *
 * Integrating resources may be finite or continuing.
 *
 * m - Monograph/Item
 * Item either complete in one part (e.g., a single monograph, a single map, a single manuscript,
 * etc.) or intended to be completed, in a finite number of separate parts (e.g., a multivolume
 * monograph, a sound recording with multiple tracks, etc.).
 *
 * s - Serial
 * Bibliographic item issued in successive parts bearing numerical or chronological designations
 * and intended to be continued indefinitely. Includes periodicals; newspapers; annuals (reports,
 * yearbooks, etc.); the journals, memoirs, proceedings, transactions, etc., of societies; and
 * numbered monographic series, etc.
 */
public enum BibliographicLevel {

    MONOGRPAHIC_COMPONENT_PART('a'),
    SERIAL_COMPONENT_PART('b'),
    COLLECTION('c'),
    SUBUNIT('d'),
    INTEGRATING_RESOURCE('i'),
    MONOGRAPH('m'),
    SERIAL('s')
    ;

    char ch;
    BibliographicLevel(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }
}
