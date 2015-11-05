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
package org.xbib.oai.server.identify;

import org.xbib.oai.server.ServerOAIResponse;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IdentifyServerResponse extends ServerOAIResponse {

    private String repositoryName;

    private URL baseURL;

    private String protocolVersion;

    private List<String> adminEmails = new ArrayList();

    private Date earliestDatestamp;

    private String deletedRecord;

    private String granularity;

    private String compression;

    @Override
    public IdentifyServerResponse to(Writer writer) throws IOException {
        return this;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    
    public String getRepositoryName() {
        return repositoryName;
    }
    
    public void setBaseURL(URL url) {
        this.baseURL = url;
    }
    
    public URL getBaseURL() {
        return baseURL;
    }
    
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    public void addAdminEmail(String email) {
        adminEmails.add(email);
    }
    
    public List<String> getAdminEmails() {
        return adminEmails;
    }
    
    public void setEarliestDatestamp(Date earliestDatestamp) {
        this.earliestDatestamp = earliestDatestamp;
    }
    
    public Date getEarliestDatestamp() {
        return earliestDatestamp;
    }
    
    public void setDeletedRecord(String deletedRecord) {
        this.deletedRecord = deletedRecord;
    }
    
    public String getDeleteRecord() {
        return deletedRecord;
    }
    
    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }
    
    public String getGranularity() {
        return granularity;
    }
    
    public void setCompression(String compression) {
        this.compression = compression;
    }
    
    public String getCompression() {
        return compression;
    }

}
