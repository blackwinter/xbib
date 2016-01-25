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
package org.xbib.tools;

import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.xbib.common.settings.Settings;
import org.xbib.common.settings.loader.SettingsLoader;
import org.xbib.common.settings.loader.SettingsLoaderFactory;
import org.xbib.io.ClasspathURLStreamHandlerFactory;
import org.xbib.tools.log.ConsoleConfigurationFactory;
import org.xbib.tools.log.FileLoggerConfigurationFactory;
import org.xbib.tools.log.RollingFileLoggerConfigurationFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import static org.xbib.common.settings.Settings.settingsBuilder;

public class Runner {

    /**
     * The Runner is a tricky class. It must contain only one method  main()
     * to get started flawlessly by the java command. No statics,
     * no constructors, no subclasses, nothing.
     *
     * Also, the start from an interactive console is automatically
     * detected, and a console log is activated. Otherwise, logging to a file
     * is enabled. Rolling file can be controlled by system property
     * <code>log4j.rolllingfile</code>
     *
     * After that, we load our real class from here and start it.
     * We can specifiy the class name on the command line or in a JSON
     * specifcation file. This is only possible as long as the Settings
     * machinery is not using log4j initialization or logging - otherwise, our
     * ConfigurationFactory construction will break here at import time.
     *
     * As a special, multiple JSON specifications can be executed one after another in a
     * chain, such as
     *
     * java {jsondef} {jsondef} {jsondef} ...
     *
     * or pairwise like
     *
     * java {{class} {jsondef}} {{class} {jsondef}} ...
     *
     * @param args the command line args
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("no arguments passed, unable to process");
        }
        if (System.getProperty("log4j.configurationFile") == null && System.getProperty("log4j.configurationFactory") == null) {
            boolean hasConsole = System.console() != null;
            if (hasConsole) {
                ConfigurationFactory.setConfigurationFactory(new ConsoleConfigurationFactory());
            } else {
                if (System.getProperty("log4j.rollingfile") != null) {
                    ConfigurationFactory.setConfigurationFactory(new RollingFileLoggerConfigurationFactory());
                } else {
                    ConfigurationFactory.setConfigurationFactory(new FileLoggerConfigurationFactory());
                }
            }
        }
        URL.setURLStreamHandlerFactory(new ClasspathURLStreamHandlerFactory());
        int exitcode = 0;
        try {
            Processor processor = null;
            int i = 0;
            while (i < args.length) {
                try {
                    Class<?> clazz = Class.forName(args[i]);
                    processor = (Processor) clazz.newInstance();
                    i++;
                } catch (Exception e) {
                    // try again to load class, skip main class from java -cp execution
                    if (i + 1 < args.length && !args[i].endsWith(".json")) {
                        i++;
                    }
                    try {
                        Class<?> clazz = Class.forName(args[i]);
                        processor = (Processor) clazz.newInstance();
                        i++;
                    } catch (Exception e2) {
                        // ignore, may be json file
                    }
                }
                // now the json specification
                String arg = ".json";
                InputStream in = System.in;
                if (i < args.length) {
                    arg = args[i++];
                    try {
                        URL url = new URL(arg);
                        in = url.openStream();
                    } catch (MalformedURLException e) {
                        in = new FileInputStream(arg);
                    }
                }
                try (Reader reader = new InputStreamReader(in, "UTF-8")) {
                    // load settings from JSON
                    SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromResource(arg);
                    Settings settings = settingsBuilder()
                            .put(settingsLoader.load(Settings.copyToString(reader)))
                            .replacePropertyPlaceholders()
                            .build();
                    // set up processor from JSON if not already set
                    if (settings.containsSetting("processor.class")) {
                        Class<?> clazz = Class.forName(settings.get("processor.class"));
                        processor = (Processor) clazz.newInstance();
                    }
                    // run processor
                    if (processor != null) {
                        exitcode = processor.run(settings);
                    }
                }
                if (exitcode != 0) {
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(exitcode);
    }
}
