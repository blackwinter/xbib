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
package org.xbib.io.iso23950.service;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.iso23950.ZSession;
import org.xbib.io.iso23950.client.ZClient;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveResponse;
import org.xbib.io.iso23950.util.ZContentTypeNegotiator;
import org.xbib.io.negotiate.ContentTypeNegotiator;
import org.xbib.io.negotiate.MediaRangeSpec;
import org.xbib.xml.transform.StylesheetTransformer;

public class ZServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(ZServlet.class.getName());

    private final ConnectionService<ZSession> service = ConnectionService.getInstance();

    private final Map<String, String> mediaTypes = new HashMap();

    private final ContentTypeNegotiator ctn = new ZContentTypeNegotiator();

    private String address;

    private String database;

    private String resultSetName;

    private String query;

    private String elementSetName;

    private String preferredRecordSyntax;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.address = config.getInitParameter("address");
        this.database = config.getInitParameter("database");
        this.resultSetName = config.getInitParameter("resultSetName");
        this.query = config.getInitParameter("query");
        this.elementSetName = config.getInitParameter("elementSetName");
        this.preferredRecordSyntax = config.getInitParameter("preferredRecordSyntax");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaType = getMediaType(request);

        resultSetName = request.getParameter("resultSetName") != null
                ? request.getParameter("resultSetName") : "default";
        query = request.getParameter("query");
        elementSetName = request.getParameter("elementSetName") != null
                ? request.getParameter("elementSetName") : "F";
        int from = Integer.parseInt(
                request.getParameter("from") != null
                        ? request.getParameter("from") : "1");
        int size = Integer.parseInt(
                request.getParameter("size") != null
                        ? request.getParameter("size") : "10");

        URI uri = URI.create(address);

        Connection<ZSession> connection = service
                .getConnectionFactory(uri)
                .getConnection(uri);
        ZSession session = connection.createSession();
        ZClient client = session.newZClient();
        try {
            ZSearchRetrieveRequest searchRetrieve = client.newPQFSearchRetrieveRequest();
            searchRetrieve.setDatabase(Collections.singletonList(database))
                    .setQuery(query)
                    .setResultSetName(resultSetName)
                    .setElementSetName(elementSetName)
                    .setPreferredRecordSyntax(preferredRecordSyntax)
                    .setFrom(from)
                    .setSize(size);
            ZSearchRetrieveResponse zResponse = searchRetrieve.execute();
            StylesheetTransformer transformer = new StylesheetTransformer().setPath("/xsl");
            response.setContentType(mediaType);
            response.setHeader("Server", "Java");
            response.setHeader("X-Powered-By", getClass().getName());
            zResponse.setStylesheetTransformer(transformer)
                    .to(response.getWriter());
            transformer.close();
        } finally {
            client.close();
            session.close();
            connection.close();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private String getMediaType(HttpServletRequest req) {
        String useragent = req.getHeader("User-Agent");
        String mediaType, mimeType = req.getParameter("http:accept");
        if (mimeType == null) {
            mimeType = req.getParameter("httpAccept");
        }
        if (mimeType == null) {
            mimeType = req.getHeader("accept");
        }
        mediaType = mediaTypes.get(mimeType);
        if (mediaType == null) {
            MediaRangeSpec mrs = useragent != null
                    ? ctn.getBestMatch(mimeType, useragent) : ctn.getBestMatch(mimeType);
            if (mrs != null) {
                mediaType = mrs.getMediaType();
            } else {
                mediaType = "";
            }
            mediaTypes.put(mimeType, mediaType);
        }
        logger.trace("mimeType = {} -> mediaType = {}", mimeType, mediaType);
        return mediaType;
    }

}
