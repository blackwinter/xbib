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
package org.xbib.sru.service;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.MimeUtil;
import org.xbib.io.negotiate.ContentTypeNegotiator;
import org.xbib.io.negotiate.MediaRangeSpec;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SRUVersion;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.SRUConstants;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.sru.util.SRUContentTypeNegotiator;
import org.xbib.xml.transform.StylesheetTransformer;

/**
 * SRU servlet
 */
public class SRUServlet extends HttpServlet implements SRUConstants {

    private final static Logger logger = LogManager.getLogger(SRUServlet.class.getName());

    private ServletConfig config;

    private SRUService service;

    private SRUClient client;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.config = config;
        String serviceName = config.getInitParameter("name");
        String serviceURI = config.getInitParameter("uri");
        this.service = serviceName != null ?
                SRUServiceFactory.getService(serviceName) :
                serviceURI != null ?
                        SRUServiceFactory.getService(serviceURI) :
                        SRUServiceFactory.getDefaultService();
        try {
            this.client = service.newClient();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (service != null) {
            try {
                service.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String mediaType = getMediaType(request);
        response.setContentType(mediaType);
        response.setHeader("Server", "Java");
        response.setHeader("X-Powered-By", getClass().getName());
        try {
            String operation = request.getParameter(OPERATION_PARAMETER);
            if (SEARCH_RETRIEVE_COMMAND.equals(operation)) {
                SearchRetrieveRequest sruRequest = createRequest(request);

                SRUVersion version = SRUVersion.fromString(sruRequest.getVersion());

                // validate SRU parameters against our SRU service
                if (sruRequest.getRecordSchema() != null && !service.getRecordSchema().equals(sruRequest.getRecordSchema())) {
                    throw new Diagnostics(66, sruRequest.getRecordSchema() + " != " + service.getRecordSchema());
                }
                if (sruRequest.getRecordPacking() != null && !service.getRecordPacking().equals(sruRequest.getRecordPacking())) {
                    throw new Diagnostics(6, sruRequest.getRecordPacking() + " != " + service.getRecordPacking());
                }
                SearchRetrieveResponse sruResponse = client.searchRetrieve(sruRequest);

                String contentType = version.equals(SRUVersion.VERSION_2_0) ? "sru" : "xml";

                response.setStatus(sruResponse.isEmpty() ? 404 : 200);
                response.setContentType(contentType);
                response.setCharacterEncoding("UTF-8");
                response.addHeader("X-SRU-origin",
                        sruRequest.getURI() != null ? sruRequest.getURI().toASCIIString() : "undefined");

                // get stylesheets for version
                String s = config.getInitParameter(version.name().toLowerCase());
                String[] stylesheets = s != null ? s.split(",") : null;

                StylesheetTransformer transformer = new StylesheetTransformer().setPath("/xsl");
                sruResponse.setOutputFormat("sru")
                        .setStylesheetTransformer(transformer)
                        .setStylesheets(version, stylesheets)
                        .to(response.getWriter());
                transformer.close();
                logger.debug("SRU servlet response sent");
            }
        } catch (Diagnostics diag) {
            logger.warn(diag.getMessage(), diag);
            //response.setStatus(500); SRU does not use 500 HTTP errors :(
            response.setStatus(200);
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MimeUtil.guessMimeTypeFromExtension("sru"));
            String responseEncoding = "UTF-8";
            response.getOutputStream().write(diag.getXML().getBytes(responseEncoding));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.setStatus(500);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private SearchRetrieveRequest createRequest(HttpServletRequest request) {
        SearchRetrieveRequest sruRequest =
                client.newSearchRetrieveRequest()
                        .setURI(getBaseURI(request))
                        .setPath(request.getPathInfo())
                        .setVersion(request.getParameter(VERSION_PARAMETER))
                        .setQuery(request.getParameter(QUERY_PARAMETER))
                        .setFilter(request.getParameter(FILTER_PARAMETER));
        int startRecord = Integer.parseInt(
                request.getParameter(START_RECORD_PARAMETER) != null
                        ? request.getParameter(START_RECORD_PARAMETER) : "1");
        sruRequest.setStartRecord(startRecord);
        int maxRecords = Integer.parseInt(
                request.getParameter(MAXIMUM_RECORDS_PARAMETER) != null
                        ? request.getParameter(MAXIMUM_RECORDS_PARAMETER) : "10");
        sruRequest.setMaximumRecords(maxRecords);
        String recordPacking = request.getParameter(RECORD_PACKING_PARAMETER) != null
                ? request.getParameter(RECORD_PACKING_PARAMETER) : "xml";
        sruRequest.setRecordPacking(recordPacking);
        String recordSchema = request.getParameter(RECORD_SCHEMA_PARAMETER) != null
                ? request.getParameter(RECORD_SCHEMA_PARAMETER) : "mods";
        sruRequest.setRecordSchema(recordSchema);
        int ttl = Integer.parseInt(
                request.getParameter(RESULT_SET_TTL_PARAMETER) != null
                        ? request.getParameter(RESULT_SET_TTL_PARAMETER) : "0");
        sruRequest.setResultSetTTL(ttl);
        sruRequest.setSortKeys(request.getParameter(SORT_KEYS_PARAMETER));

        sruRequest.setFacetLimit(request.getParameter(FACET_LIMIT_PARAMETER));
        sruRequest.setFacetCount(request.getParameter(FACET_COUNT_PARAMETER));
        sruRequest.setFacetStart(request.getParameter(FACET_START_PARAMETER));
        sruRequest.setFacetSort(request.getParameter(FACET_SORT_PARAMETER));

        sruRequest.setExtraRequestData(request.getParameter(EXTRA_REQUEST_DATA_PARAMETER));
        return sruRequest;
    }

    private final Map<String, String> mediaTypes = new HashMap<String,String>();

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
            final ContentTypeNegotiator ctn = new SRUContentTypeNegotiator();
            MediaRangeSpec mrs = useragent != null
                    ? ctn.getBestMatch(mimeType, useragent) : ctn.getBestMatch(mimeType);
            if (mrs != null) {
                mediaType = mrs.getMediaType();
            } else {
                mediaType = "";
            }
            mediaTypes.put(mimeType, mediaType);
        }
        logger.debug("mimeType = {} -> mediaType = {}", mimeType, mediaType);
        return mediaType;
    }

    private URI getBaseURI(HttpServletRequest request) {
        String uri = request.getRequestURL().toString();
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && forwardedHost.length() > 0) {
            uri = uri.replaceAll("://[^/]*", "://" + forwardedHost);
        }
        return URI.create(uri);
    }

}
