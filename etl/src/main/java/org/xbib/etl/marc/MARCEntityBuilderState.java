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
package org.xbib.etl.marc;

import org.xbib.etl.DefaultEntityBuilderState;
import org.xbib.iri.IRI;
import org.xbib.rdf.RdfContentBuilderProvider;
import org.xbib.rdf.RdfGraph;
import org.xbib.rdf.RdfGraphParams;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.MemoryResource;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class MARCEntityBuilderState extends DefaultEntityBuilderState {

    private Resource root;

    private String label;

    private String recordNumber;

    public MARCEntityBuilderState(RdfGraph<RdfGraphParams> graph,
                                  Map<IRI,RdfContentBuilderProvider> providers) {
        super(graph, providers);
    }

    public MARCEntityBuilderState setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public MARCEntityBuilderState setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
        return this;
    }

    public String getRecordNumber() {
        return recordNumber;
    }

    public Resource getResource() throws IOException {
        if (!graph().getResources().hasNext()) {
            root = new MemoryResource().blank();
            graph().receive(root);
        }
        return root;
    }

    @Override
    public void complete() throws IOException {
        Iterator<Resource> it = graph().getResources();
        while (it.hasNext()) {
            Resource resource = it.next();
            if (recordNumber != null) {
                resource.id(IRI.builder().fragment(recordNumber).build());
            }
        }
        super.complete();
    }

}
