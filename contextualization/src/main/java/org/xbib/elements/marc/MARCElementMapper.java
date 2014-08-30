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
package org.xbib.elements.marc;

import org.xbib.elements.AbstractElementMapper;
import org.xbib.elements.ElementBuilderFactory;
import org.xbib.elements.KeyValueElementPipeline;
import org.xbib.marc.FieldCollection;

import java.util.Map;

/**
 * A MARC element mapper
 */
public class MARCElementMapper extends AbstractElementMapper<FieldCollection, String, MARCElement, MARCContext> {

    /**
     * Instantiate a MARC element mapper.
     *
     * @param format name of the configuration to be loaded
     */
    public MARCElementMapper(String format) {
        super("/org/xbib/analyzer/", format, new MARCSpecification());
    }

    public MARCElementMapper(String format, Map<String,Object> params) {
        super("/org/xbib/analyzer/", format, new MARCSpecification().setParameters(params));
    }

    @Override
    public MARCElementMapper pipelines(int pipelines) {
        super.pipelines(pipelines);
        return this;
    }

    @Override
    public MARCElementMapper detectUnknownKeys(boolean enabled) {
        super.detectUnknownKeys(enabled);
        return this;
    }

    public MARCElementMapper start() {
        super.start(new MARCElementBuilderFactory());
        return this;
    }

    @Override
    public MARCElementMapper start(ElementBuilderFactory<FieldCollection, String, MARCElement, MARCContext> factory) {
        super.start(factory);
        return this;
    }

    @Override
    protected KeyValueElementPipeline createPipeline(int i) {
        MARCElementPipeline pipeline = new MARCElementPipeline(i);
        pipeline.setElementBuilder(factory.newBuilder())
                .setSpecification(specification)
                .setQueue(queue)
                .setMap(map);
        return pipeline;
    }

}