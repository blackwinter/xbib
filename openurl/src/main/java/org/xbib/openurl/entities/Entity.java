package org.xbib.openurl.entities;

import org.xbib.openurl.descriptors.Descriptor;

import java.util.Collection;

/**
 * This interface is an abstraction for the categories of information
 * represented in a web service request. In layman's terms, these
 * categories are synonymous with who, what, where, why, when, etc.
 */
public interface Entity<D extends Descriptor> {

    /**
     * Get a sequence of descriptors for this entity. Use
     * the instanceof operator to select desired descriptor
     * types from the list.
     * <p>
     * The OpenURL specification defines a set of
     * abstractions to represent descriptors: Identifier (URI),
     * By Value Metadata, By Reference Metadata, and PrivateData
     * (everything else).
     * </p>
     * <p>
     * Identifier is modeled in OpenURL API with a getURI() method
     * because it is functionally equivalent to the URI class.
     * </p>
     * <p>
     * Interfaces are provided for ByValueMetadata
     * and ByReferenceMetadata because they are generally
     * useful.
     * </p>
     * <p>
     * PrivateData is modeled with a getPrivateData() method because it is
     * functionally equivalent to Java's Object class.
     * </p>
     *
     * @return an array of Descriptors that describe this entity
     */
    Collection<D> getDescriptors();

    /**
     * Add descriptor to entity
     *
     * @param descriptor the descriptor to add
     */
    void addDescriptor(D descriptor);
}
