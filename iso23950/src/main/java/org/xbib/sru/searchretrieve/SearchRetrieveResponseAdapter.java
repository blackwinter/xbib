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
package org.xbib.sru.searchretrieve;

import java.util.Collection;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.xbib.io.Request;
import org.xbib.io.http.HttpResponse;

/**
 *  An adapter class for SearchRetrieve responses
 *
 */
public class SearchRetrieveResponseAdapter implements SearchRetrieveListener {

    @Override
    public void onConnect(Request request) {
    }

    @Override
    public void onDisconnect(Request request) {
    }

    @Override
    public void onReceive(Request request, CharSequence message) {
    }

    @Override
    public void onError(Request request, CharSequence errorMessage) {
    }

    @Override
    public void version(String version) {
    }

    @Override
    public void numberOfRecords(long numberOfRecords) {
    }

    @Override
    public void beginRecord() {
    }

    @Override
    public void recordSchema(String recordSchema) {
    }

    @Override
    public void recordPacking(String recordPacking) {
    }

    @Override
    public void recordIdentifier(String recordIdentifier) {
    }

    @Override
    public void recordPosition(int recordPosition) {
    }

    @Override
    public XMLEventConsumer recordData() {
        return null;
    }

    @Override
    public XMLEventConsumer extraRecordData() {
        return null;
    }

    @Override
    public void facetedResults(Collection<XMLEvent> facets) {
    }

    @Override
    public void endRecord() {
    }

    @Override
    public void receivedResponse(HttpResponse response) {
    }
}
