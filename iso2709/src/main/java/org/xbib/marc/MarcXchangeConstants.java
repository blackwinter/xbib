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

import java.util.regex.Pattern;

/**
 * ISO/DIS 25577 MarcXchange constants
 */
public interface MarcXchangeConstants {

    String MARCXCHANGE = "MarcXchange";

    String MARCXCHANGE_NS_PREFIX = "mx";

    String MARCXCHANGE_V1_NS_URI = "info:lc/xmlns/marcxchange-v1";

    String MARCXCHANGE_V2_NS_URI = "info:lc/xmlns/marcxchange-v2";

    String MARCXCHANGE_V1_1_SCHEMALOCATION = "http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd";

    String MARCXCHANGE_V2_0_SCHEMALOCATION = "http://www.loc.gov/standards/iso25577/marcxchange-2-0.xsd";

    // related (strict superset)

    String MARC21_NS_URI = "http://www.loc.gov/MARC21/slim";

    String MARC21_SCHEMALOCATION = "http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd";

    // element names

    String COLLECTION = "collection";

    String RECORD = "record";

    String LEADER = "leader";

    String CONTROLFIELD = "controlfield";

    String DATAFIELD = "datafield";

    String SUBFIELD = "subfield";

    // attribute names

    String TAG = "tag";

    String IND = "ind";

    String CODE = "code";

    String FORMAT = "format";

    String TYPE = "type";

    String MARC21 = "MARC21";

    String BIBLIOGRAPHIC = "Bibliographic";

    String HOLDINGS = "Holdings";

    Pattern TAG_PATTERN = Pattern.compile("(00[1-9A-Za-z]|0[1-9A-Za-z][0-9A-Za-z]|[1-9A-Za-z][0-9A-Za-z]{2})");

    String FORMAT_TAG = "__FORMAT";

    String TYPE_TAG = "__TYPE";

    String LEADER_TAG = "__LEADER";

    String RECORD_NUMBER_FIELD = "001";

}
