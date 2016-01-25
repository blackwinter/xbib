package org.xbib.tools.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.net.URI;

public class RollingFileLoggerConfigurationFactory extends ConfigurationFactory {

    static Configuration createConfiguration(final String name, ConfigurationBuilder<BuiltConfiguration> builder) {
        builder.setConfigurationName(name);
        builder.setStatusLevel(Level.OFF);
        String fileName = "logs/xbib.log";
        if (System.getProperty("log4j.filename") != null) {
            fileName = System.getProperty("log4j.filename");
        }
        AppenderComponentBuilder appenderBuilder = builder.newAppender("RollingFile", "RollingFile")
                .addAttribute("fileName", fileName)
                .addAttribute("filePattern", "logs/$${date:yyyy-MM}/%d{yyyy-MM-dd}-%i.gz");
        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "[%d{ABSOLUTE}][%-5p][%-25c][%t] %m%n"));
        appenderBuilder.addComponent(builder.newComponent("Policies")
                .addComponent(builder.newComponent("TimeBasedTriggeringPolicy"))
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100 MB")));
        builder.add(appenderBuilder);
        Level level = System.getProperty("log4j.debug") != null ? Level.DEBUG : Level.INFO;
        builder.add(builder.newRootLogger(level).add(builder.newAppenderRef("RollingFile")));
        return builder.build();
    }

    @Override
    public Configuration getConfiguration(ConfigurationSource source) {
        return getConfiguration(source.toString(), null);
    }

    @Override
    public Configuration getConfiguration(final String name, final URI configLocation) {
        ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
        return createConfiguration(name, builder);
    }

    @Override
    protected String[] getSupportedTypes() {
        return new String[] {"*"};
    }
}
