
package org.xbib.web.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.elasticsearch.search.SearchSupport;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private final static Logger logger = LogManager.getLogger(ApplicationContextListener.class.getName());

    public static SearchSupport searchSupport;

    public static Dispatcher dispatcher;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            searchSupport = new SearchSupport().init();
            logger.info("Elasticsearch search initiated");
            dispatcher = new Dispatcher();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (searchSupport != null) {
            searchSupport.shutdown();
            logger.info("Elasticsearch search shutdown");
        }
    }

}
