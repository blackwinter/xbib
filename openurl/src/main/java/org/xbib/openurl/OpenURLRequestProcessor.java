package org.xbib.openurl;

import org.w3c.dom.Document;
import org.xbib.openurl.descriptors.ByReferenceMetadata;
import org.xbib.openurl.descriptors.ByValueMetadataKev;
import org.xbib.openurl.descriptors.ByValueMetadataXml;
import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.descriptors.Identifier;
import org.xbib.openurl.entities.Referent;
import org.xbib.openurl.entities.Referrer;
import org.xbib.openurl.entities.ReferringEntity;
import org.xbib.openurl.entities.Requester;
import org.xbib.openurl.entities.Resolver;
import org.xbib.openurl.entities.ServiceType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This abstraction processes web service request(s) represented
 * by the OpenURL model. The OpenURL 1.0 specification doesn't
 * dictate the mode of operation at this level, so implementations
 * should make instantiations of this interface a configuration
 * parameter.
 */
public interface OpenURLRequestProcessor {


    /**
     * Get processor identifier
     *
     * @return the processor identifier
     * @throws URISyntaxException
     */
    URI getProcessorID();

    /**
     * Create a ContextObject for the provided entities.
     *
     * @param referent          descriptions of what the request refers to
     * @param referringEntities descriptions of where the request
     *                          was invoked
     * @param requesters        descriptions of who invoked the request
     * @param serviceTypes      descriptions for why the requesters
     *                          invoked the request
     * @param resolvers         description of viable service providers
     * @param referrers         description of the entities that formulated
     *                          the request
     * @param id
     * @param version
     * @param timestamp
     * @return a ContextObject containing the specified Entities
     */
    ContextObject createContextObject(Referent referent,
                                      List<ReferringEntity> referringEntities,
                                      List<Requester> requesters,
                                      List<ServiceType> serviceTypes,
                                      List<Resolver> resolvers,
                                      List<Referrer> referrers,
                                      String id,
                                      String version,
                                      String timestamp);

    /**
     * This method takes a web service request that has been
     * transformed into the OpenURL model, locates, and invokes
     * the corresponding Service classes to produce a result.
     *
     * @param openURLRequest the entire web service request
     *                       represented in the OpenURL model.
     * @return a single result that the Servlet can easily
     * return to the client.
     * @throws OpenURLException
     */
    OpenURLResponse resolve(OpenURLRequest openURLRequest)
            throws OpenURLException;

    /**
     * This method writes a response to an OutputStream
     *
     * @param out
     * @throws IOException
     */
    void write(OutputStream out) throws IOException;

    /**
     * This method creates a Referent. Note that the descriptor
     * parameter can be anything, including an array of objects
     *
     * @param descriptors descriptors describing the Referent
     * @return a Referent containing the specified descriptor(s)
     */
    Referent createReferent(List<Descriptor> descriptors);

    /**
     * This method creates a ServiceType. Note that the descriptor
     * parameter can be anything, including an array of objects
     *
     * @param serviceMap a map of Services for given identifiers
     * @return a ServiceType containing the specified descriptor(s)
     */
    ServiceType createServiceType(Map<Identifier, Service> serviceMap);

    /**
     * This method creates a Requester. Note that the descriptor
     * parameter can be anything, including an array of objects
     *
     * @param descriptors descriptors describing the Requester
     * @return a Requester containing the specified descriptor(s)
     */
    Requester createRequester(List<Descriptor> descriptors);

    /**
     * This method creates a ReferringEntity.
     *
     * @param descriptors descriptors describing the ReferringEntity
     * @return a ReferringEntity containing the specified descriptor(s)
     */
    ReferringEntity createReferringEntity(List<Descriptor> descriptors);

    /**
     * This method creates a Referrer.
     *
     * @param descriptors descriptors describing the Referrer
     * @return a Referrer containing the specified descriptor(s)
     */
    Referrer createReferrer(List<Descriptor> descriptors);

    /**
     * This factory creates a Resolver.
     *
     * @param descriptors descriptors describing the Resolver
     * @return a Resolver containing the specified descriptor(s)
     */
    Resolver createResolver(List<Descriptor> descriptors);

    /**
     * Obtain a by-reference metadata descriptor.
     *
     * @param ref_fmt
     * @param ref
     * @return a reference to a metadata description
     */
    ByReferenceMetadata createByReferenceMetadata(URI ref_fmt, URL ref);

    /**
     * Obtain a by-value metadata descriptor.
     *
     * @param val_fmt
     * @param prefix
     * @param map
     * @return a metadata description
     */
    ByValueMetadataKev createByValueMetadataKEV(URI val_fmt,
                                                String prefix, Map map);

    /**
     * Obtain a by-value metadata descriptor.
     *
     * @param val_fmt
     * @return a metadata description
     */
    ByValueMetadataXml createByValueMetadataXML(URI val_fmt,
                                                Document document);

    /**
     * Create an OpenURLRequest object containing essential
     * ingredients from the request represented in
     * terms of the OpenURL API.
     *
     * @param contextObject
     * @return an OpenURLRequest container for the
     * specified request components.
     */
    OpenURLRequest createOpenURLRequest(ContextObject contextObject);

}