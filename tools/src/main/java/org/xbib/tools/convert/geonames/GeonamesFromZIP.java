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
package org.xbib.tools.convert.geonames;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.InputService;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.tools.Converter;
import org.xbib.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Import geonames, UTF-8 tab-separated in ZIP
 */
public class GeonamesFromZIP extends Converter {

    private final static Logger logger = LogManager.getLogger(GeonamesFromZIP.class);

    @Override
    public String getName() {
        return "geonames-tsv";
    }

    protected PipelineProvider<Pipeline> pipelineProvider() {
        return GeonamesFromZIP::new;
    }

    @Override
    public void process(URI uri) throws Exception {
        logger.info("start of processing {}", uri);
        InputStream in = InputService.getInputStream(uri);
        ZipInputStream zin = new ZipInputStream(in);
        for (ZipEntry zipEntry; (zipEntry = zin.getNextEntry()) != null; ) {
            logger.info("reading zip entry {}", zipEntry.getName());
            Scanner sc = new Scanner(zin);
            while (sc.hasNextLine()) {
                int i = 0;
                String[] line = sc.nextLine().split("\t");
                String geonameid = line[i++];
                String name = line[i++];
                String asciiname = line[i++];
                String[] alternatenames = line[i++].split(",");
                Double latitude = Double.parseDouble(line[i++]);
                Double longitude = Double.parseDouble(line[i++]);
                String featureClass = line[i++];
                String featureCode = line[i++];
                String country = line[i++];
                String cc2 = line[i++];
                String admin1 = line[i++];
                String admin2 = line[i++];
                String admin3 = line[i++];
                String admin4 = line[i++];
                Long population = Long.parseLong(line[i++]);
                String elevationStr = line[i++];
                Integer elevation = Strings.isNullOrEmpty(elevationStr) ? 0 : Integer.parseInt(line[i++]);
                String dem = line[i++];
                String timezone = line[i];
            }
        }
        zin.close();
        logger.info("end of processing {}", uri);
    }

}
