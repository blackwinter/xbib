package org.xbib.openurl.config;

import org.xbib.openurl.OpenURLException;

import java.util.List;
import java.util.Map;

/**
 * Configuration information for OpenURL classes.
 */
public interface ClassConfig {

    /**
     * The Java classname that instantiates the implied Class
     *
     * @return the fully-qualified Java class name
     * @throws OpenURLException
     */
    String getClassName() throws OpenURLException;

    /**
     * Get a class configuration property from the configuration file
     * for the specified key.
     * Only use this method if you know there is only one value for the
     * key.
     *
     * @param key key
     * @return the value for a key in the configuration file
     * @throws OpenURLException
     */
    String getArg(String key) throws OpenURLException;

    /**
     * Get an array of class configuration properties from the configuration
     * file for the specified key.
     *
     * @param key key
     * @return an array of values assigned to this key in the OOM
     * configuration file.
     * @throws OpenURLException
     */
    List<String> getArgs(String key) throws OpenURLException;

    /**
     * Get a Map of the args in the configuration file for this class
     *
     * @return a Map of args
     * @throws OpenURLException
     */
    Map<String, String> getArgs() throws OpenURLException;
}
