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

import java.util.Arrays;

/**
 * Record label of ISO 2709 records
 */
public class RecordLabel {

    /**
     * The length of a record label is fixed at 24 characters.
     */
    public final static int LENGTH = 24;

    private final char[] cfix = new char[LENGTH];

    private int recordLength;

    private char recordStatus;

    private char bibliographicLevel;

    private int indicatorLength;

    private int subfieldIdentifierLength;

    private int baseAddressOfData;

    private int dataFieldLength;

    private int startingCharacterPositionLength;

    private int segmentIdentifierLength;

    /**
     * Construct a record label
     *
     */
    public RecordLabel() {
        this(new char[]{});
    }

    /**
     * Construct a record label
     *
     * @param label the label
     */
    public RecordLabel(char[] label) {
        if (label.length > LENGTH) {
            label = Arrays.copyOf(label, LENGTH);
        } else if (label.length < LENGTH) {
            // fill with blanks
            char[] ch = new char[LENGTH - label.length];
            for (int i = 0; i < ch.length; i++) {
                ch[i] = ' ';
            }
            char[] newLabel = new char[LENGTH];
            System.arraycopy(label, 0, newLabel, 0, label.length);
            System.arraycopy(ch, 0, newLabel, label.length, ch.length);
            label = newLabel;
        }
        System.arraycopy(label, 0, cfix, 0, LENGTH);
        // repair digits if missing
        for (int i = 0; i < 5; i++) {
            if (label[i] < '0' || label[i] > '9') {
                cfix[i] = '0';
            }
        }
        // replace ^
        for (int i = 5; i < 10; i++) {
            if (label[i] == '^') {
                cfix[i] = ' ';
            }
        }
        for (int i = 10; i < 17; i++) {
            if (label[i] < '0' || label[i] > '9') {
                cfix[i] = '0';
            }
        }
        for (int i = 17; i < 20; i++) {
            if (label[i] == '^') {
                cfix[i] = ' ';
            }
        }
        for (int i = 20; i < 23; i++) {
            if (label[i] < '0' || label[i] > '9') {
                cfix[i] = '0';
            }
        }
        this.recordLength = Integer.parseInt(new String(new char[]{cfix[0], cfix[1], cfix[2], cfix[3], cfix[4]}));
        this.recordStatus = cfix[5];
        this.bibliographicLevel = cfix[7]; // should be enum to be typesafe
        this.indicatorLength = cfix[10] - '0';

        // 00588cM2.01200024   000h
        // 012345678901

        this.subfieldIdentifierLength = cfix[11] - '0';
        this.baseAddressOfData = Integer.parseInt(new String(new char[]{cfix[12], cfix[13], cfix[14], cfix[15], cfix[16]}));
        this.dataFieldLength = cfix[20] - '0';
        this.startingCharacterPositionLength = cfix[21] - '0';
        this.segmentIdentifierLength = cfix[22] - '0';
    }

    public String getRecordLabel() {
        return new String(cfix);
    }

    /**
     * Five decimal digits, right justified, with zero fill where necessary,
     * representing the number of characters in the entire record, including the
     * label itself, the directory, and the variable fields. This data element
     * is normally calculated automatically when the total record is assembled
     * for exchange.
     */
    public RecordLabel setRecordLength(int length) {
        this.recordLength = length;
        String s = Integer.toString(recordLength);
        String fs = "00000".substring(5 - s.length()) + s;
        for (int i = 0; i < 5; i++) {
            cfix[i] = fs.charAt(i);
        }
        return this;
    }


    public int getRecordLength() {
        return recordLength;
    }

    /**
     * A single character, denoting the processing status of the record.
     *
     * c corrected record
     *
     * A record to which changes have been made to correct errors, one which has
     * been amended to bring it up to date, or one where fields have been
     * deleted. However, if the previous record was a prepublication record
     * (e.g.; CIP) and a full record replacement is now being issued, code 'p'
     * should be used instead of 'c'. A record labelled 'n', 'o' or 'p' on which
     * a correction is made is coded as 'c'.
     *
     * d deleted record
     *
     * A record which is exchanged in order to indicate that a record bearing
     * this control number is no longer valid. The record may contain only the
     * label, directory; and 001 (record control number) field, or it may
     * contain all the fields in the record as issued; in either case GENERAL
     * NOTE 300 field may be used to explain why the record is deleted.
     *
     * n new record
     *
     * A new record (including a pre-publication record, e.g., CIP). If code 'o'
     * applies, it is used in preference to ' n '.
     *
     * o previously issued higher level record
     *
     * A new record at a hierarchical level below the highest level for which a
     * higher level record has already been issued (see also character position
     * 8).
     *
     * p previously issued as an incomplete, pre-publication record
     *
     * A record for a published item replacing a pre-publication record, e.g.,
     * CIP.
     */
    public RecordLabel setRecordStatus(char recordStatus) {
        this.recordStatus = recordStatus;
        cfix[5] = recordStatus;
        return this;
    }

    public char getRecordStatus() {
        return recordStatus;
    }

    /**
     * The bibliographic level of a record relates to the main part of the
     * record. Some cataloguing codes may not make a clear distinction between a
     * multipart item (multivolume monograph) and a monographic series. In such
     * cases an agency should use whichever of the values is more appropriate in
     * the majority of cases. Where such a distinction is made, but cannot be
     * determined in a particular instance, the item should be coded as a
     * serial.
     *
     * 'a' analytic (component part)
     *
     * bibliographic item that is physically contained in another item such that
     * the location of the component part is dependent upon the physical
     * identification and location of the containing item. A component part may
     * itself be either monographic or serial.
     *
     * The following are examples of materials that are coded 'a': an article in
     * a journal; a continuing column or feature within a journal; a single
     * paper in a collection of conference proceedings.
     *
     * 'c' collection
     *
     * bibliographic item that is a made-up collection.
     *
     * The following are examples of materials which are coded 'c': a collection
     * of pamphlets housed in a box; a set of memorabilia in various formats
     * kept together as a collection; all the manuscripts of an individual
     * author.
     *
     * This code is used only for made-up collections.
     *
     * 'm' monographic
     *
     * bibliographic item complete in one physical part or intended to be
     * completed in a finite number of parts.
     *
     * The following are examples of materials which are coded 'm': a single
     * part item (monograph); a multipart item (multivolume monograph); a
     * separately catalogued single part of a multipart item; a book in a
     * series; a separately catalogued special issue of a newspaper; a sheet map
     * in a series; a complete series of maps, assuming the series was intended
     * to be completed in a finite number of parts; a single globe.
     *
     * 's' serial
     *
     * bibliographic item issued in successive parts and intended to be
     * continued indefinitely.
     *
     * The following are examples of materials which are coded 's': a journal
     * that is still being published; a complete run of a journal that has
     * ceased publication; a newspaper; a monographic series.
     */
    public RecordLabel setBibliographicLevel(char level) {
        this.bibliographicLevel = level;
        cfix[7] = level;
        return this;
    }

    public char getBibliographicLevel() {
        return bibliographicLevel;
    }

    /**
     * Indicator length is a numeric digit giving the length of the indicators.
     */
    public RecordLabel setIndicatorLength(int length) {
        this.indicatorLength = length;
        cfix[10] = (char)('0' + length);
        return this;
    }

    public int getIndicatorLength() {
        return indicatorLength;
    }

    /**
     * A numeric digit giving the length of the subfield identifier
     */
    public RecordLabel setSubfieldIdentifierLength(int subfieldIdentifierLength) {
        this.subfieldIdentifierLength = subfieldIdentifierLength;
        cfix[11] = (char)('0' + subfieldIdentifierLength);
        return this;
    }

    public int getSubfieldIdentifierLength() {
        return subfieldIdentifierLength;
    }

    /**
     * Base address of data. The location within the record at which the first
     * datafield begins, relative to the first character in the record, which is
     * designated character position `0' (zero).
     *
     * Five numeric digits, right justified with leading zeros, indicating the
     * starting character position of the first data field relative to the
     * beginning of the record. Since the first character of the record is
     * numbered 0 (zero), the number entered as the base address of data will be
     * equal to the total number of characters in the label and directory
     * including the field separator that terminates the directory. In the
     * directory, the starting character position for each field is given
     * relative to the first character of the first data field which will be
     * field 001, rather than the beginning of the record. The base address thus
     * gives the base from which the position of each field is calculated.
     */
    public RecordLabel setBaseAddressOfData(int baseAddressOfData) {
        this.baseAddressOfData = baseAddressOfData;
        String s = Integer.toString(baseAddressOfData);
        String fs = "00000".substring(5 - s.length()) + s;
        for (int i = 12; i < 17; i++) {
            cfix[i] = fs.charAt(i-12);
        }
        return this;
    }

    public int getBaseAddressOfData() {
        return baseAddressOfData;
    }

    /**
     * Length of data field A four-digit number showing how many characters are
     * occupied the datafield, including indicators and datafield separator but
     * excluding the record separator code if the datafield is the last field in
     * the record. The use of 4 characters permits datafields as long as 9999
     * characters.
     */
    public RecordLabel setDataFieldLength(int length) {
        this.dataFieldLength = length;
        cfix[20] = (char)('0' + length);
        return this;
    }

    public int getDataFieldLength() {
        return dataFieldLength;
    }

    /**
     * Length of the starting-character-position portion of each entry
     */
    public RecordLabel setStartingCharacterPositionLength(int length) {
        this.startingCharacterPositionLength = length;
        cfix[21] = (char)('0' + length);
        return this;
    }

    public int getStartingCharacterPositionLength() {
        return startingCharacterPositionLength;
    }

    /**
     * The length of implementation-defined section of each entry in the
     * directory. Of the two characters, one is used for the segment identifier,
     * the other for the occurrence identifier.
     *
     * @return the segment identifier
     */
    public RecordLabel setSegmentIdentifierLength(int length) {
        this.segmentIdentifierLength = length;
        cfix[22] = (char)('0' + length);
        return this;
    }

    public int getSegmentIdentifierLength() {
        return segmentIdentifierLength;
    }

    @Override
    public String toString() {
        return new String(cfix);
    }
}
