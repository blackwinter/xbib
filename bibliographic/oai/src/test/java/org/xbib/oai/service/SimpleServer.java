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
package org.xbib.oai.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import org.xbib.oai.client.DefaultOAIClient;
import org.xbib.oai.OAISession;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.oai.server.getrecord.GetRecordServerRequest;
import org.xbib.oai.server.getrecord.GetRecordServerResponse;
import org.xbib.oai.server.identify.IdentifyServerRequest;
import org.xbib.oai.server.identify.IdentifyServerResponse;
import org.xbib.oai.server.listidentifiers.ListIdentifiersServerRequest;
import org.xbib.oai.server.listidentifiers.ListIdentifiersServerResponse;
import org.xbib.oai.server.listmetadataformats.ListMetadataFormatsServerRequest;
import org.xbib.oai.server.listmetadataformats.ListMetadataFormatsServerResponse;
import org.xbib.oai.server.listrecords.ListRecordsServerRequest;
import org.xbib.oai.server.OAIServer;
import org.xbib.oai.server.listrecords.ListRecordsServerResponse;
import org.xbib.oai.server.listsets.ListSetsServerRequest;
import org.xbib.oai.server.listsets.ListSetsServerResponse;
import org.xbib.oai.server.verb.Identify;

public class SimpleServer implements OAIServer {

    @Override
    public void identify(IdentifyServerRequest request, IdentifyServerResponse response)
            throws OAIException {
        new Identify(request, response).execute(this);        
    }

    @Override
    public void listMetadataFormats(ListMetadataFormatsServerRequest request, ListMetadataFormatsServerResponse response)
            throws OAIException {
    }

    @Override
    public void listSets(ListSetsServerRequest request, ListSetsServerResponse response)
            throws OAIException {
    }

    @Override
    public void listIdentifiers(ListIdentifiersServerRequest request, ListIdentifiersServerResponse response)
            throws OAIException {
    }

    @Override
    public void listRecords(ListRecordsServerRequest request, ListRecordsServerResponse response)
            throws OAIException {
    }

    @Override
    public void getRecord(GetRecordServerRequest request, GetRecordServerResponse response)
            throws OAIException {
    }

    @Override
    public URI getURI() {
        return URI.create("http://localhost:8080/oai");
    }

    @Override
    public OAISession newSession() {
        return new DefaultOAIClient()
                .setURL(getURI());
    }

    @Override
    public Date getLastModified() {
        return new Date();
    }

    @Override
    public String getRepositoryName() {
        return "Test Repository Name";
    }

    @Override
    public URL getBaseURL() {
        try {
            return getURI().toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @Override
    public String getProtocolVersion() {
        return "2.0";
    }

    @Override
    public String getAdminEmail() {
        return "joergprante@gmail.com";
    }

    @Override
    public String getEarliestDatestamp() {
        return "2012-01-01T00:00:00Z";
    }

    @Override
    public String getDeletedRecord() {
        return "no";
    }

    @Override
    public String getGranularity() {
        return "YYYY-MM-DDThh:mm:ssZ";
    }
        
}
