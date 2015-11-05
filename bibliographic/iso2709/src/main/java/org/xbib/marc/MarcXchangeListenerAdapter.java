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

/**
 * The MarcXchange listener is an interface for catching events while
 * reading from ISO 2709 / MARC format family streams.
 * 
 * Each record is framed by a leader and a trailer event. The leader
 * event fires directly after the begin of a record when a leader element
 * is found, the trailer (which is not defined in ISO 2709/MarcXchange) is fired 
 * just before the record ends. The trailer event is useful for post-processing
 * fields before the record end event is fired.
 * 
 * Data field events are fired in the sequence they are found in a record.
 * Sub fields can be nested in data fields, but at most for one nesting level.
 * 
 * Control fields are defined as data fields in the tag range from 000 to 009.
 * They do not have any indicators or sub fields.
 * 
 * Field data is carried only in the end events, where begin events carry
 * information about field indicators and subfield identifiers.
 */
public class MarcXchangeListenerAdapter implements MarcXchangeListener {

    /**
     * Begin of a collection
     */
    public void beginCollection() {}

    /**
     * End of a collection
     */
    public void endCollection() {}

    /**
     * Begin of a record
     *
     * @param format the record format
     * @param type   the record type
     */
    public void beginRecord(String format, String type) {}

    /**
     * End of a record
     */
    public void endRecord() {}

    /**
     * The leader (or label) of a record
     *
     * @param label the label
     */
    public void leader(String label) {}

    /**
     * A control field begins.
     *
     * @param field the field
     */
    public void beginControlField(Field field) {}

    /**
     * A control field ends.
     *
     * @param field the field
     */
    public void endControlField(Field field) {}

    /**
     * A data field begins
     *
     * @param field the field
     */
    public void beginDataField(Field field) {}

    /**
     * A data field ends
     *
     * @param field the field
     */
    public void endDataField(Field field) {}

    /**
     * A sub field begins
     *
     * @param field the field
     */
    public void beginSubField(Field field) {}

    /**
     * A sub field ends
     *
     * @param field the field
     */
    public void endSubField(Field field) {}

}