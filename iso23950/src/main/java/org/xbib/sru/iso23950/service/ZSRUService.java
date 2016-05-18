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
package org.xbib.sru.iso23950.service;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.xbib.io.iso23950.ZConstants;
import org.xbib.io.iso23950.client.ZClient;
import org.xbib.io.iso23950.client.ZClientFactory;
import org.xbib.io.iso23950.searchretrieve.CQLSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.PQFSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveResponse;
import org.xbib.io.iso23950.service.ZService;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SRUConstants;
import org.xbib.sru.SRUVersion;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.iso23950.client.ZSRUClientFactory;
import org.xbib.sru.service.SRUService;
import org.xbib.xml.transform.StylesheetTransformer;

/**
 *  A SRU service on a Z service
 */
public class ZSRUService implements SRUService<ZSearchRetrieveRequest, ZSearchRetrieveResponse>, SRUConstants, ZService, ZConstants {

    private final Properties properties;

    public ZSRUService(Properties properties) throws IOException {
        this.properties = properties;
    }

    @Override
    public ZClient newZClient() throws IOException {
        return ZClientFactory.getInstance().newZClient(properties);
    }

    @Override
    public URI getURI() {
        return URI.create(properties.getProperty("uri"));
    }

    @Override
    public SRUClient newClient() throws IOException {
        return ZSRUClientFactory.getInstance().newClient(properties);
    }

    @Override
    public void close(ZClient client) throws IOException {
        client.close();
    }

    public List<String> getDatabase() {
        return Arrays.asList(properties.getProperty(DATABASE_PROPERTY,"").split(","));
    }

    public String getPreferredRecordSyntax() {
        return properties.getProperty(PREFERRED_RECORD_SYNTAX_PROPERTY, "marc21");
    }

    public String getResultSetName() {
        return properties.getProperty(RESULT_SET_NAME_PROPERTY, "default");
    }

    public String getElementSetName() {
        return properties.getProperty(ELEMENT_SET_NAME_PROPERTY, "F");
    }

    public String getVersion() {
        return properties.getProperty(VERSION_PROPERTY, "2.0");
    }

    @Override
    public String getRecordSchema() {
        return properties.getProperty(RECORD_SCHEMA_PARAMETER, "mods");
    }

    @Override
    public String getRecordPacking() {
        return properties.getProperty(RECORD_PACKING_PARAMETER, "xml");
    }

    public String getFormat() {
        return properties.getProperty(SRUConstants.FORMAT_PROPERTY, "MARC21");
    }

    public String getType() {
        return properties.getProperty(SRUConstants.TYPE_PROPERTY, "Bibliographic");
    }

    public String getEncoding() {
        return properties.getProperty(SRUConstants.ENCODING_PROPERTY, "UTF-8");
    }

    public String getStylesheet(SRUVersion version) {
        return properties.getProperty("stylesheet." + version.name().toLowerCase(), null);
    }

    public void searchRetrieve(CQLSearchRetrieveRequest request, Writer writer)
            throws IOException {
        /*
        // sanity check for record schema
        if (request.getRecordSchema() != null && !getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        // sanity check for record packing
        if (request.getRecordPacking() != null && !getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        */

        String cql = request.getQuery();
        int from = request.getStartRecord();
        int size = request.getMaximumRecords();
        try {
            ZSearchRetrieveResponse zResponse = request
                    .setDatabase(getDatabase())
                    .setPreferredRecordSyntax(getPreferredRecordSyntax())
                    .setResultSetName(getResultSetName())
                    .setElementSetName(getElementSetName())
                    .setQuery(cql)
                    .setFrom(from)
                    .setSize(size)
                    .execute();
            StylesheetTransformer transformer = new StylesheetTransformer().setPath("/xsl");
            transformer.addParameter("version", getVersion());
            transformer.addParameter("format", getVersion());
            transformer.addParameter("type", getType());
            SRUVersion version = SRUVersion.fromString(request.getVersion());
            zResponse.setStylesheetTransformer(transformer)
                    .setStylesheets(version, getStylesheet(version))
                    .to(writer);
            transformer.close();
        } catch (org.xbib.io.iso23950.Diagnostics d) {
            throw new Diagnostics(1, d.getPlainText());
        }
    }

    /**
     * PQF search for internal usage. Sending PQF queries is not SRU standard.
     * But Z servers in a federated environment may react differently to
     * queries, CQL is not possible to be translated to each member in most
     * cases, and we fall back to a list of PQF queries to each Z server. As a
     * side effect, from/size must be passed separately , since PQF is not able
     * to carry this information (startRecord/maximumRecords in SRU). The result
     * is fully adhering to SRU response.
     *
     * @param request request
     * @param writer writer
     * @throws Diagnostics
     * @throws IOException
     */
    public void searchRetrieve(PQFSearchRetrieveRequest request, Writer writer)
            throws IOException {
        /*
        // sanity check for record schema
        if (request.getRecordSchema() != null && !getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        // sanity check for record packing
        if (request.getRecordPacking() != null && !getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        */
        String pqf = request.getQuery();
        int from = request.getStartRecord();
        int size = request.getMaximumRecords();
        try {
            ZSearchRetrieveResponse zResponse = request
                    .setDatabase(getDatabase())
                    .setPreferredRecordSyntax(getPreferredRecordSyntax())
                    .setResultSetName(getResultSetName())
                    .setElementSetName(getElementSetName())
                    .setQuery(pqf)
                    .setFrom(from)
                    .setSize(size)
                    .execute();
            StylesheetTransformer transformer = new StylesheetTransformer().setPath("/xsl");
            transformer.addParameter("version", getVersion());
            transformer.addParameter("format", getFormat());
            transformer.addParameter("type", getType());
            SRUVersion version = SRUVersion.fromString(request.getVersion());
            zResponse.setStylesheetTransformer(transformer)
                    .setStylesheets(version, getStylesheet(version))
                    .to(writer);

        } catch (org.xbib.io.iso23950.Diagnostics d) {
            throw new Diagnostics(1, d.getPlainText());
        }
    }

    @Override
    public void close() throws IOException {
    }
}
