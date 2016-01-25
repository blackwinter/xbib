package org.xbib.tools;

import org.xbib.RuntimeTests;

import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xbib.common.settings.Settings;
import org.xbib.common.settings.loader.SettingsLoader;
import org.xbib.common.settings.loader.SettingsLoaderFactory;
import org.xbib.tools.log.ConsoleConfigurationFactory;
import org.xbib.tools.log.FileLoggerConfigurationFactory;
import org.xbib.tools.log.RollingFileLoggerConfigurationFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.xbib.common.settings.Settings.settingsBuilder;

@Category(RuntimeTests.class)
public class RunnerTest {

    // copied from Runner but without System.exit
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
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("no arguments passed, unable to process");
        }
        try {
            Processor processor = null;
            int i = 0;
            while (i < args.length) {
                try {
                    Class<?> clazz = Class.forName(args[i]);
                    processor = (Processor) clazz.newInstance();
                    i++;
                } catch (Exception e) {
                    // may be Main-Class driven jar execution
                    if (i + 1 < args.length) {
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
                    SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromResource(arg);
                    Settings settings = settingsBuilder()
                            .put(settingsLoader.load(Settings.copyToString(reader)))
                            .replacePropertyPlaceholders()
                            .build();
                    if (processor == null) {
                        if (settings.get("processor.class") != null) {
                            Class<?> clazz = Class.forName(settings.get("processor.class"));
                            processor = (Processor) clazz.newInstance();
                        } else {
                            throw new IllegalArgumentException("no processor class defined");
                        }
                    }
                    exitcode = processor.run(settings);
                }
                if (exitcode != 0) {
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println(1);
        }
        System.err.println(exitcode);
    }

    @Test
    public void testRunner() throws Exception {
        File file = File.createTempFile("runner.", ".json");
        file.deleteOnExit();
        FileWriter writer = new FileWriter(file);
        writer.write("{\"uri\":\"file:src/test/resources/org/xbib/tools/feed/elasticsearch/medline/medline.xml\",\"mock\":true}");
        writer.flush();
        main(new String[]{
                "org.xbib.tools.feed.elasticsearch.medline.Medline",
                "file://" + file.getAbsolutePath()
        });
        writer.close();
        assertTrue(true);
    }

}
