package org.xbib.openurl.config;

import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.Service;
import org.xbib.openurl.Transport;
import org.xbib.openurl.descriptors.Identifier;

import java.util.List;
import java.util.Map;

/**
 * General configuration information.
 */
public interface OpenURLConfig {

    /**
     * Get a list of Transport classes defined in this CommunityProfile.
     *
     * @return a list of Transports supported by the CommunityProfile
     * @throws OpenURLException
     */
    List<Transport> getTransports() throws OpenURLException;

    /**
     * Get an instance of an OpenURLRequestProcessor. Different processors may
     * interpret OpenURLRequests differently, so OOM uses the configuration
     * file to identify the one to be used by this application.
     *
     * @return the configured OpenURLRequestProcessor
     * @throws OpenURLException
     */
    OpenURLRequestProcessor getProcessor() throws OpenURLException;

    /**
     * Get an instance of an identified service from the configuration file.
     *
     * @param serviceID an identifier for a configured service
     * @return a Service.
     * @throws OpenURLException
     */
    Service getService(Identifier serviceID) throws OpenURLException;

    /**
     * Get an instance of a Service class identified by a class name from the
     * configuration file. Use this method to construct Service classes
     * because it will automatically include any class-specific information
     * it finds in the configuration file.
     *
     * @param className the name of a Service class
     * @return a Service.
     * @throws OpenURLException
     */
    Service getService(String className) throws OpenURLException;

    /**
     * Get a class configuration property from the OOM configuration file
     * for the specified key.
     * Only use this method if you know there is only one value for the
     * key.
     *
     * @param key
     * @return the value for a key in the configuration file
     * @throws OpenURLException
     */
    String getArg(String key) throws OpenURLException;

    /**
     * Get an array of class configuration properties from the configuration
     * file for the specified key.
     *
     * @param key
     * @return an array of values assigned to this key in the
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
    Map getArgs() throws OpenURLException;
}
