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
package org.xbib.tools.analyze;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.common.settings.loader.JsonSettingsLoader;
import org.xbib.common.settings.loader.SettingsLoader;
import org.xbib.tools.Program;

import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;

import static org.xbib.common.settings.Settings.settingsBuilder;

abstract class Analyzer implements Program {

    private final static Logger logger = LogManager.getLogger(Analyzer.class.getSimpleName());

    @Override
    public int daemon(String[] args) throws Exception {
        if (args.length != 2) {
            return 1;
        }
        try (FileReader reader = new FileReader(args[1])) {
            return from(args, reader);
        }
    }

    @Override
    public int read(Reader reader) throws Exception {
        return from(null, reader);
    }

    @Override
    public int from(String[] args, Reader reader) throws Exception {
        try {
            SettingsLoader settingsLoader = new JsonSettingsLoader();
            Settings settings = settingsBuilder().put(settingsLoader.load(Settings.copyToString(reader))).build();
            run(settings);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return 1;
        }
        return 0;
    }


    protected abstract void run(Settings settings) throws Exception;

}
