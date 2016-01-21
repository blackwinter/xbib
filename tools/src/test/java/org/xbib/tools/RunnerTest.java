package org.xbib.tools;

import org.xbib.RuntimeTests;

import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xbib.tools.log.ConsoleConfigurationFactory;
import org.xbib.tools.log.FileLoggerConfigurationFactory;
import org.xbib.tools.log.RollingFileLoggerConfigurationFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import static org.junit.Assert.assertTrue;

@Category(RuntimeTests.class)
public class RunnerTest {

    // copied from Runner but without System.exit
    public static void main(String[] args) {
        if (System.getProperty("log4j.configurationFile") == null && System.getProperty("log4j.configurationFactory") == null) {
            boolean hasConsole = System.console() != null;
            if (hasConsole) {
                ConfigurationFactory.setConfigurationFactory(new ConsoleConfigurationFactory());
            } else {
                if (System.getProperty("log.rollingfile") != null) {
                    ConfigurationFactory.setConfigurationFactory(new RollingFileLoggerConfigurationFactory());
                } else {
                    ConfigurationFactory.setConfigurationFactory(new FileLoggerConfigurationFactory());
                }
            }
        }
        int exitcode = 0;
        try {
            if (args != null && args.length > 0) {
                if (System.in.available() > 0) {
                    Class<?> clazz = Class.forName(args[0]);
                    Processor processor = (Processor) clazz.newInstance();
                    exitcode = processor.from(".json", new InputStreamReader(System.in));
                } else {
                    for (int i = 0; i < args.length; i+=2) {
                        Class<?> clazz = Class.forName(args[i]);
                        Processor processor = (Processor) clazz.newInstance();
                        exitcode = processor.from(args[i+1]);
                        if (exitcode != 0) {
                            break;
                        }
                    }
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
