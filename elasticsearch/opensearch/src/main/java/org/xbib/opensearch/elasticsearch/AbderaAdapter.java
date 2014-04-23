/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.opensearch.elasticsearch;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;
import org.xbib.atom.AbstractAbderaAdapter;
import org.xbib.atom.AtomFeedFactory;

/**
 * An Abdera managed collection service for ElasticSearch DSL query language.
 *
 */
public class AbderaAdapter extends AbstractAbderaAdapter {

    private final OpenSearchAtomFeedFactory controller = new OpenSearchAtomFeedFactory();
    
    /**
     * Construct Adapter for Abdera
     */
    public AbderaAdapter(Abdera abdera, FeedConfiguration config) {
        super(abdera, config);
    }

    @Override
    protected AtomFeedFactory getFeedFactory() {
        return controller;
    }

}
