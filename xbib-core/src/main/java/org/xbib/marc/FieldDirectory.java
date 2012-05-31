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

import java.util.TreeMap;

public class FieldDirectory extends TreeMap<Integer, FieldDesignator> {

    public FieldDirectory(RecordLabel label, String segment) throws FieldDirectoryException {
        super();
        int directoryLength = label.getBaseAddressOfData() - (RecordLabel.LENGTH + 1);
        // assume that negative values means prohibiting directory access
        if (directoryLength > 0
                && label.getDataFieldLength() > 0
                && label.getStartingCharacterPositionLength() > 0
                && label.getSegmentIdentifierLength() >= 0) {
            int keylength = 3;
            // directory entry size = key length (fixed at 3)
            // plus data field length
            // plus starting character position length
            // plus segment identifier length
            int entrysize = keylength + label.getDataFieldLength()
                    + label.getStartingCharacterPositionLength()
                    + label.getSegmentIdentifierLength();
            if (directoryLength % entrysize != 0) {
                throw new FieldDirectoryException("invalid ISO 2709 directory length: "
                        + directoryLength + ", definitions in record label: "
                        + " data field length = " + label.getDataFieldLength()
                        + " starting character position length = " + label.getStartingCharacterPositionLength()
                        + " segment identifier length = " + label.getSegmentIdentifierLength());
            }
            for (int i = RecordLabel.LENGTH; i < RecordLabel.LENGTH + directoryLength; i += entrysize) {
                String key = segment.substring(i, i + keylength);
                try {
                    int length = Integer.parseInt(
                            segment.substring(i + keylength, i + keylength + label.getDataFieldLength()));
                    int position = label.getBaseAddressOfData() + Integer.parseInt(
                            segment.substring(i + keylength + label.getDataFieldLength(),
                            i + keylength + label.getDataFieldLength() + label.getStartingCharacterPositionLength()));
                    put(position, new FieldDesignator(label, key, position, length));
                } catch (NumberFormatException e) {
                    throw new FieldDirectoryException("directory corrupt? key = " + key + " length = " + directoryLength);
                }
            }
        }
    }
}
