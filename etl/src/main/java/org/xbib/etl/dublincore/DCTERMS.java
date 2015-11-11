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

public interface DCTERMS {
    String NS_PREFIX = "dcterms";

    String NS_URI = "http://purl.org/dc/terms/";

    String ABSTRACT = NS_URI + "abstract";
    String ACCESSRIGHTS = NS_URI + "accessRights";
    String ACCRUALMETHOD = NS_URI + "accrualMethod";
    String ACCRUALPERIODICITY = NS_URI + "accrualPeriodicity";
    String ACCRUALPOLICY = NS_URI + "accrualPolicy";
    String ALTERNATIVE = NS_URI + "alternative";
    String AUDIENCE = NS_URI + "audience";
    String AVAILABLE = NS_URI + "available";
    String BIBLIOGRAPHICCITATION = NS_URI + "bibliographicCitation";
    String CONFORMSTO = NS_URI + "conformsTo";
    String CONTRIBUTOR = NS_URI + "contributor";
    String COVERAGE = NS_URI + "coverage";
    String CREATED = NS_URI + "created";
    String CREATOR = NS_URI + "creator";
    String DATE = NS_URI + "date";
    String DATEACCEPTED = NS_URI + "dateAccepted";
    String DATECOPYRIGHTED = NS_URI + "dateCopyrighted";
    String DATESUBMITTED = NS_URI + "dateSubmitted";
    String DESCRIPTION = NS_URI + "description";
    String EDUCATIONLEVEL = NS_URI + "educationLevel";
    String EXTENT = NS_URI + "extent";
    String FORMAT = NS_URI + "format";
    String HASFORMAT = NS_URI + "hasFormat";
    String HASPART = NS_URI + "hasPart";
    String HASVERSION = NS_URI + "hasVersion";
    String IDENTIFIER = NS_URI + "identifier";
    String INSTRUCTIONALMETHOD = NS_URI + "instructionalMethod";
    String ISFORMATOF = NS_URI + "isFormatOf";
    String ISPARTOF = NS_URI + "isPartOf";
    String ISREFERENCEDBY = NS_URI + "isReferencedBy";
    String ISREQUIREDBY = NS_URI + "isRequiredBy";
    String ISSUED = NS_URI + "issued";
    String ISVERSIONOF = NS_URI + "isVersionOf";
    String LANGUAGE = NS_URI + "language";
    String LICENSE = NS_URI + "license";
    String MEDIATOR = NS_URI + "mediator";
    String MEDIUM = NS_URI + "medium";
    String MODIFIED = NS_URI + "modified";
    String PROVENANCE = NS_URI + "provenance";
    String PUBLISHER = NS_URI + "publisher";
    String REFERENCES= NS_URI + "references";
    String RELATION = NS_URI + "relation";
    String REPLACES = NS_URI + "replaces";
    String REQUIRES = NS_URI + "requires";
    String RIGHTS = NS_URI + "rights";
    String RIGHTSHOLDER= NS_URI + "rightsHolder";
    String SOURCE = NS_URI + "source";
    String SPATIAL = NS_URI + "spatial";
    String SUBJECT = NS_URI + "subject";
    String TABLEOFCONTENTS = NS_URI + "tableOfContents";
    String TEMPORAL = NS_URI + "temporal";
    String TITLE = NS_URI + "title";
    String TYPE = NS_URI + "type";
    String VALID = NS_URI + "valid";
}
