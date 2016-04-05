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
package org.xbib.rdf;

import org.xbib.iri.IRI;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Resource is an ID with a map of predicates associated with objects.
 */
public interface Resource extends Node {

    Resource setId(IRI id);

    IRI id();

    /**
     * Is resource ID local/embedded ("blank node")?
     *
     * @return true if embedded, otherwise false
     */
    boolean isEmbedded();

    /**
     * Add a property to this resource with a string object value
     *
     * @param predicate a predicate identifier
     * @param object    an object in its string representation form
     * @return the new resource with the property added
     */
    Resource add(IRI predicate, Node object);

    /**
     * Add a property to this resource with a string object value
     *
     * @param predicate a predicate identifier
     * @param object    an object in its string representation form
     * @return the new resource with the property added
     */
    Resource add(IRI predicate, String object);

    /**
     * Add a property to this resource
     *
     * @param predicate a predicate identifier
     * @param number    an integer
     * @return the new resource with the property added
     */
    Resource add(IRI predicate, Integer number);

    Resource add(IRI predicate, Boolean number);

    /**
     * Add a property to this resource
     *
     * @param predicate a predicate identifier
     * @param literal   a literal
     * @return the new resource with the property added
     */
    Resource add(IRI predicate, Literal literal);

    /**
     * Add a property to this resource
     *
     * @param predicate a predicate identifier
     * @param resource  external resource IRI
     * @return the new resource with the property added
     */
    Resource add(IRI predicate, IRI resource);

    /**
     * Add a property to this resource
     *
     * @param predicate a predicate identifier
     * @param list      a list of objects
     * @return the new resource with the property added
     */
    Resource add(IRI predicate, List<Object> list);

    Resource add(IRI predicate, Map<Object,Object> map);

    /**
     * Add another resource to this resource
     *
     * @param predicate a predicate identifier
     * @param resource  resource
     * @return the new resource with the resource added
     */
    Resource add(IRI predicate, Resource resource);

    /**
     * Add a property to this resource.
     *
     * @param predicate a predicate identifier in its string representation form
     * @param object    an object in its string representation form
     * @return the new resource with the property added
     */
    Resource add(String predicate, String object);

    /**
     * Add a property to this resource
     *
     * @param predicate a predicate identifier
     * @param number    an integer
     * @return the new resource with the property added
     */
    Resource add(String predicate, Integer number);

    Resource add(String predicate, Boolean number);

    /**
     * Add a property to this resource.
     *
     * @param predicate a predicate identifier in its string representation form
     * @param literal   an object in its string representation form
     * @return the new resource with the property added
     */
    Resource add(String predicate, Literal literal);

    /**
     * Add a property to this resource
     *
     * @param predicate        predicate
     * @param externalResource external resource
     * @return the new resource with the property added
     */
    Resource add(String predicate, IRI externalResource);

    /**
     * Add a property to this resource
     *
     * @param predicate predicate
     * @param list      a list of objects
     * @return the new resource with the property added
     */
    Resource add(String predicate, List<Object> list);

    Resource add(String predicate, Map<Object,Object> map);

    /**
     * Add another resource to this resource
     *
     * @param predicate predicate
     * @param resource  resource
     * @return the new resource with the resource added
     */
    Resource add(String predicate, Resource resource);

    Resource add(Map<Object,Object> map);

    Resource rename(String oldPredicate, String newPredicate);

    Resource rename(IRI oldPredicate, IRI newPredicate);

    /**
     * Setting the type of the resource.
     * This is equivalent to add("rdf:type", externalResource)
     *
     * @param externalResource external resource
     * @return this resource
     */
    Resource a(IRI externalResource);

    /**
     * Return list of resources for this predicate
     *
     * @param predicate the predicate
     * @return list of resources
     */
    List<Resource> resources(IRI predicate);

    /**
     * Create an anonymous resource and associate it with this resource. If the
     * resource under the given resource identifier already exists, the existing
     * resource is returned.
     *
     * @param predicate the predicate ID for the resource
     * @return the new anonymous resource
     */
    Resource newResource(IRI predicate);

    /**
     * Create an anonymous resource and associate it with this resource. If the
     * resource under the given resource identifier already exists, the existing
     * resource is returned.
     *
     * @param predicate the predicate ID for the resource
     * @return the new anonymous resource
     */
    Resource newResource(String predicate);

    Resource newSubject(Object subject);

    IRI newPredicate(Object predicate);

    Node newObject(Object object);

    Literal newLiteral(Object value);

    /**
     * Return the set of predicates
     *
     * @return set of predicates
     */
    Set<IRI> predicates();

    /**
     * Return object list for a given predicate
     *
     * @param predicate predicate
     * @return set of objects
     */
    List<Node> objects(IRI predicate);

    List<Node> objects(String predicate);

    /**
     * Return collection of embedded resources.
     * It is required to get the size of the collection,
     * for determining if a single value or an array must be
     * constructed.
     * @param predicate the predicate
     * @return collection of embedded resources
     */
    Collection<Resource> embeddedResources(IRI predicate);

    List<Node> visibleObjects(IRI predicate);

    /**
     * Add a triple to this resource
     *
     * @param triple triple
     */
    Resource add(Triple triple);

    /**
     * Get iterator over triples
     *
     * @return statements
     */
    List<Triple> triples();

    /**
     * Get iterator over triples thats are properties of this resource
     *
     * @return iterator over triple
     */
    List<Triple> properties();

    /**
     * Compact a predicate. Under the predicate, there is a single blank node
     * object with a single value for the same predicate. In such case, the
     * blank node can be removed and the single value can be promoted to the
     * predicate.
     *
     * @param predicate the predicate
     */
    void compactPredicate(IRI predicate);

    /**
     * Remove all properties and resources from this resource
     */
    void clear();

    /**
     * Check if resource is empty, if it has no properties and no resources
     */
    boolean isEmpty();

    /**
     * The size of the resource. It corresponds to the number of statements in this resource.
     *
     * @return the size
     */
    int size();

    /**
     * Set marker for resource deletion
     */
    Resource setDeleted(boolean delete);

    /**
     * Check if marker for resource deletion is set
     *
     * @return true if the marker ist set
     */
    boolean isDeleted();

    interface Mapper {
        void map(Resource r, String path, String value) throws IOException;
    }
}
