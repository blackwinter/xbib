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
import org.xbib.tools.log.ConsoleConfigurationFactory;
import org.xbib.tools.log.FileLoggerConfigurationFactory;
import org.xbib.tools.log.RollingFileLoggerConfigurationFactory;

import java.io.InputStreamReader;

public class Runner {

    public static void main(String[] args) {
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
        int exitcode = 0;
        try {
            if (args != null && args.length > 0) {
                Processor processor;
                if (System.in.available() > 0) {
                    try {
                        Class<?> clazz = Class.forName(args[0]);
                        processor = (Processor) clazz.newInstance();
                    } catch (Exception e) {
                        Class<?> clazz = Class.forName(args[1]);
                        processor = (Processor) clazz.newInstance();
                    }
                    if (processor != null) {
                        exitcode = processor.from(".json", new InputStreamReader(System.in, "UTF-8"));
                    }
                } else {
                    for (int i = 0; i < args.length; i+=2) {
                        try {
                            Class<?> clazz = Class.forName(args[i]);
                            processor = (Processor) clazz.newInstance();
                        } catch (Exception e) {
                            i++;
                            Class<?> clazz = Class.forName(args[i]);
                            processor = (Processor) clazz.newInstance();
                        }
                        if (processor != null) {
                            if (i < args.length - 1) {
                                exitcode = processor.from(args[i + 1]);
                            } else {
                                // System.in is not available = no pipe, but maybe it works from terminal
                                exitcode = processor.from(".json", new InputStreamReader(System.in, "UTF-8"));
                            }
                            if (exitcode != 0) {
                                break;
                            }
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("no arguments passed, unable to run");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(exitcode);
    }
}
