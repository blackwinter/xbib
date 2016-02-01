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
package org.xbib.tools.convert.zdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.tools.input.FileInput;
import org.xbib.util.KeyValueStreamAdapter;
import org.xbib.marc.FieldList;
import org.xbib.marc.Field;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.tools.convert.Converter;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cnnverting Zeitschriftendatenbank (ZDB) MARC ISO2709 files
 */
public final class MARC extends Converter {

    private final static Logger logger = LogManager.getLogger(MARC.class.getName());

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new MARC().setPipeline(p);
    }

    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = FileInput.getInputStream(uri)) {
            Reader r = new InputStreamReader(in, ISO88591);
            final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<>());
            final MARCEntityQueue queue = new MARCEntityQueue(settings.get("elements"), settings.getAsInt("pipelines", 1));
            queue.setUnmappedKeyListener((id, key) -> {
                if ((settings.getAsBoolean("detect-unknown", false))) {
                    logger.warn("unmapped field {}", key);
                    unmapped.add("\"" + key + "\"");
                }
            });
            queue.execute();

            final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    .setStringTransformer(value -> Normalizer.normalize(new String(value.getBytes(ISO88591), UTF8), Normalizer.Form.NFKC))
                    .addListener(queue)
                    .addListener(new LoggingAdapter());

            final Iso2709Reader reader = new Iso2709Reader(r)
                    .setMarcXchangeListener(kv);
            // setting the properties is just informational and not used for any purpose.
            reader.setProperty(Iso2709Reader.FORMAT, "MARC21");
            reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
            if ("marc/hol".equals(settings.get("elements")) || "marc/zdb/hol".equals(settings.get("elements"))) {
                reader.setProperty(Iso2709Reader.TYPE, "Holdings");
            }
            reader.setProperty(Iso2709Reader.FATAL_ERRORS, false);
            reader.parse();
            r.close();
            queue.close();
            if (settings.getAsBoolean("detect", false)) {
                logger.info("unknown keys={}", unmapped);
            }
        }
    }

    static class LoggingAdapter extends KeyValueStreamAdapter<FieldList, String> {
        @Override
        public KeyValueStreamAdapter<FieldList, String> begin() {
            logger.debug("start");
            return this;
        }

        @Override
        public KeyValueStreamAdapter<FieldList, String> keyValue(FieldList key, String value) {
            if (logger.isDebugEnabled()) {
                for (Field f : key) {
                    logger.debug("tag={} ind={} subf={} data={}",
                            f.tag(), f.indicator(), f.subfieldId(), f.data());
                }
            }
            return this;
        }

        @Override
        public KeyValueStreamAdapter<FieldList, String> end() {
            logger.debug("end");
            return this;
        }
    }

}
