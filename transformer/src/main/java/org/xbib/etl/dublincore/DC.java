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
package org.xbib.etl.dublincore;

public interface DC {
    String NS_PREFIX = "dc";

    String NS_URI = "http://purl.org/dc/elements/1.1/";

    String CREATOR = NS_URI + "creator";
    String CONTRIBUTOR = NS_URI + "contributor";
    String COVERAGE = NS_URI + "coverage";
    String DATE = NS_URI  + "date";
    String DESCRIPTION = NS_URI + "description";
    String FORMAT = NS_URI + "format";
    String IDENTIFIER = NS_URI + "identifier";
    String LANGUAGE = NS_URI + "language";
    String PUBLISHER = NS_URI + "publisher";
    String SOURCE = NS_URI + "source";
    String SUBJECT = NS_URI + "subject";
    String RELATION = NS_URI + "relation";
    String RIGHTS = NS_URI + "rights";
    String TITLE = NS_URI + "title";
    String TYPE = NS_URI + "type";
}
