package org.xbib.openurl;

import org.xbib.openurl.descriptors.Identifier;
import org.xbib.openurl.entities.ServiceType;

import java.io.IOException;
import java.io.OutputStream;

/**
 * If you think of a service in terms of pure business logic,
 * this class provides a simple bridge providing access to it
 * from the web. To use another analogy, if the business logic
 * is a coat, and OpenURL is a coatrack, this class represents
 * a loop of cloth sewn into the collar of the coat.
 * Installing new Services can be as easy as dropping them on
 * the classpath, if your Transport is smart enough to deduce
 * the package and classname from information in the
 * HttpServletRequest. If the Transport can't deduce everything
 * it needs from the HTTP request, it could use a config
 * file to locate and configure Service classes instead.
 */
public interface Service {

    /**
     * This is a unique identifier assigned to a service.
     * It is easy to imagine a Service being used in
     * multiple CommunityProfiles, so it might be nice to
     * keep track of them in a registry someday. These
     * IDs could be used to represent the service in a
     * language independent way.
     *
     * @return the Service identifier
     */
    Identifier getServiceID();

    /**
     * This method is responsible for pulling what (Referent),
     * why (ServiceType), who (Requester), etc. information
     * out of the ContextObject and using it to call any Java
     * classes and methods needed to produce a result. Having
     * obtained a result of some sort from the business logic,
     * this method is then responsible for transforming it
     * into an OpenURLResponse that acts as a proxy for
     * HttpServletResponse.
     *
     * @param serviceType    the current ServiceType in sequence as
     *                       enumerated in the ContextObject
     * @param contextObject  the current ContextObject in sequence
     *                       as enumerated in the OpenURLRequest
     * @param openURLRequest the entire request from the client,
     *                       represented according to the OpenURL Object Model
     * @return null to have the ServiceType processing loop move
     * on to the next ServiceType or non-null to abort the
     * ServiceType processing loop and return a result.
     * @throws OpenURLException
     */
    OpenURLResponse resolve(ServiceType serviceType,
                            ContextObject contextObject,
                            OpenURLRequest openURLRequest,
                            OpenURLRequestProcessor openURLProcessor)
            throws OpenURLException;

    /**
     * Write response to output stream
     *
     * @param out the output stream
     * @return true if a response was written, otherwise false
     * @throws IOException
     */
    boolean write(OutputStream out) throws IOException;
}
