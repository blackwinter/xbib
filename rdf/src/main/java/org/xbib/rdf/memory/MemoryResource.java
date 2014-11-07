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
package org.xbib.rdf.memory;

import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.XSDResourceIdentifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * A resource is a sequence of properties and of associated resources.
 */
public class MemoryResource implements Resource, Comparable<Resource>, XSDResourceIdentifiers {

    private final static AtomicLong nodeID = new AtomicLong();

    private final static String GENID = "genid";

    private final static String PLACEHOLDER = "_:";

    private final MultiMap<IRI, Node> attributes = new LinkedHashMultiMap<IRI, Node>();

    private final Map<IRI, Resource> children = new LinkedHashMap<IRI, Resource>();

    private IRI id;

    private boolean embedded;

    private boolean deleted;

    @Override
    public MemoryResource id(IRI id) {
        this.id = id;
        if (id != null) {
            embedded = GENID.equals(id.getScheme());
        }
        return this;
    }

    @Override
    public IRI id() {
        return id;
    }

    @Override
    public int compareTo(Resource r) {
        return id == null ? -1 : id.toString().compareTo(r.id().toString());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : -1;
    }

    @Override
    public boolean equals(Object obj) {
        return id != null && obj != null && obj instanceof Resource && id.equals(((Resource) obj).id());
    }

    public MemoryResource blank() {
        id(IRI.builder().curie(GENID, "b" + next()).build());
        this.embedded = true;
        return this;
    }

    public MemoryResource blank(String id) {
        id(IRI.builder().curie(GENID, id).build());
        this.embedded = true;
        return this;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public boolean isVisible() {
        return !embedded;
    }

    public static void reset() {
        nodeID.set(0L);
    }

    public static long next() {
        return nodeID.incrementAndGet();
    }

    @Override
    public String toString() {
        if (id() == null) {
            blank();
        }
        return embedded ? PLACEHOLDER + id().getSchemeSpecificPart() : id().toString();
    }

    @Override
    public Resource add(Triple triple) {
        if (triple == null) {
            return this;
        }
        IRI id = triple.subject().id();
        if (id == null || id.equals(id())) {
            add(triple.predicate(), triple.object());
        } else {
            Resource r = children.get(id);
            if (r != null) {
                return r.add(triple);
            } else {
                // nothing found, continue with a new resource with new subject
                return new MemoryResource().id(id).add(triple);
            }
        }
        return this;
    }

    @Override
    public Resource add(IRI predicate, Node object) {
        attributes.put(predicate, object);
        if (object instanceof Resource) {
            Resource r = (Resource) object;
            children.put(r.id(), r);
        }
        return this;
    }

    @Override
    public Resource add(IRI predicate, IRI iri) {
        return add(predicate, new MemoryResource().id(iri));
    }

    @Override
    public Resource add(IRI predicate, Literal literal) {
        if (predicate != null && literal != null) {
            attributes.put(predicate, literal);
        }
        return this;
    }

    @Override
    public Resource add(IRI predicate, Resource resource) {
        if (resource == null) {
            return this;
        }
        if (resource.id() == null) {
            resource.id(id());
            Resource r = newResource(predicate);
            resource.triples().forEach(r::add);
        } else {
            attributes.put(predicate, resource);
        }
        return this;
    }

    @Override
    public Resource add(IRI predicate, String value) {
        return add(predicate, newLiteral(value));
    }

    @Override
    public Resource add(IRI predicate, Integer value) {
        return add(predicate, newLiteral(value));
    }

    @Override
    public Resource add(IRI predicate, Boolean value) {
        return add(predicate, newLiteral(value));
    }

    @Override
    public Resource add(IRI predicate, List list) {
        for (Object object : list) {
            if (object instanceof Map) {
                add(predicate, (Map) object);
            } else if (object instanceof List) {
                add(predicate, (List) object);
            } else if (object instanceof Resource) {
                add(predicate, (Resource) object);
            } else {
                add(predicate, newLiteral(object));
            }
        }
        return this;
    }

    @Override
    public Resource add(IRI predicate, Map map) {
        Resource r = newResource(predicate);
        for (Object pred : map.keySet()) {
            Object obj = map.get(pred);
            if (obj instanceof Map) {
                r.add(newPredicate(pred), (Map) obj);
            } else if (obj instanceof List) {
                r.add(newPredicate(pred), (List) obj);
            } else if (obj instanceof Resource) {
                r.add(newPredicate(pred), (Resource) obj);
            } else {
                r.add(newPredicate(pred), newLiteral(obj));
            }
        }
        return this;
    }

    @Override
    public Resource add(String predicate, String value) {
        return add(newPredicate(predicate), value);
    }

    @Override
    public Resource add(String predicate, Integer value) {
        return add(newPredicate(predicate), value);
    }

    @Override
    public Resource add(String predicate, Boolean value) {
        return add(newPredicate(predicate), value);
    }

    @Override
    public Resource add(String predicate, Literal value) {
        return add(newPredicate(predicate), value);
    }

    @Override
    public Resource add(String predicate, IRI externalResource) {
        return add(newPredicate(predicate), externalResource);
    }

    @Override
    public Resource add(String predicate, Resource resource) {
        return add(newPredicate(predicate), resource);
    }

    @Override
    public Resource add(String predicate, Map map) {
        return add(newPredicate(predicate), map);
    }

    @Override
    public Resource add(String predicate, List list) {
        return add(newPredicate(predicate), list);
    }

    @Override
    public Resource add(Map map) {
        for (Object pred : map.keySet()) {
            Object obj = map.get(pred);
            if (obj instanceof Map) {
                Resource r = newResource(newPredicate(pred));
                r.add((Map) obj);
            } else if (obj instanceof List) {
                add(newPredicate(pred), (List) obj);
            } else if (obj instanceof Resource) {
                add(newPredicate(pred), (Resource) obj);
            } else {
                add(newPredicate(pred), newLiteral(obj));
            }
        }
        return this;
    }

    public Resource remove(IRI predicate) {
        if (predicate == null) {
            return this;
        }
        // check if child resource exists for any of the objects under this predicate and remove it
        embeddedResources(predicate).forEach(resource -> children.remove(resource.id()));
        attributes.remove(predicate);
        return this;
    }

    public Resource remove(IRI predicate, Node object) {
        if (predicate == null) {
            return this;
        }
        attributes.remove(predicate, object);
        return this;
    }

    @Override
    public Resource a(IRI externalResource) {
        add(newPredicate(RdfConstants.RDF_TYPE), externalResource);
        return this;
    }

    @Override
    public Set<IRI> predicates() {
        return attributes.keySet();
    }

    @Override
    public Collection<Node> objects(IRI predicate) {
        return attributes.get(predicate);
    }

    @Override
    public Collection<Node> objects(String predicate) {
        return attributes.get(newPredicate(predicate));
    }

    @Override
    public List<Literal> literals(IRI predicate) {
        return attributes.get(predicate).stream()
                .filter(n -> n instanceof Literal)
                .map(Literal.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> resources(IRI predicate) {
        return attributes.get(predicate).stream()
                .filter(n -> n instanceof Resource)
                .map(Resource.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> embeddedResources(IRI predicate) {
        return attributes.get(predicate).stream()
                .filter(n -> n instanceof Resource)
                .map(Resource.class::cast)
                .filter(Resource::isEmbedded)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> linkedResources(IRI predicate) {
        return attributes.get(predicate).stream()
                .filter(n -> n instanceof Resource)
                .map(Resource.class::cast)
                .filter(n -> !n.isEmbedded())
                .collect(Collectors.toList());
    }

    @Override
    public List<Node> visibleObjects(IRI predicate) {
        return attributes.get(predicate).stream()
                .filter(Node::isVisible)
                .collect(Collectors.toList());
    }

    /**
     * Compact a predicate with a single blank node object.
     * If there is a single blank node object with values for the same predicate, the
     * blank node can be dropped and the values can be promoted to the predicate.
     *
     * @param predicate the predicate
     */
    @Override
    public void compactPredicate(IRI predicate) {
        List<Resource> resources = embeddedResources(predicate);
        if (resources.size() == 1) {
            Resource r = resources.get(0);
            attributes.remove(predicate, r);
            attributes.putAll(predicate, r.objects(predicate));
        }
    }

    @Override
    public void clear() {
        attributes.clear();
    }

    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public Resource setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public Resource newResource(IRI predicate) {
        Resource r = new MemoryResource().blank();
        children.put(r.id(), r);
        attributes.put(predicate, r);
        return r;
    }

    @Override
    public Resource newResource(String predicate) {
        return newResource(newPredicate(predicate));
    }


    @Override
    public List<Triple> triples() {
        return new Triples(this, true).list();
    }

    @Override
    public List<Triple> properties() {
        return new Triples(this, false).list();
    }

    @Override
    public Resource newSubject(Object subject) {
        return subject == null ? null :
                subject instanceof Resource ? (Resource) subject :
                        subject instanceof IRI ? new MemoryResource().id((IRI) subject) :
                                new MemoryResource().id(IRI.builder().curie(subject.toString()).build());
    }

    @Override
    public IRI newPredicate(Object predicate) {
        return predicate == null ? null :
                predicate instanceof IRI ? (IRI) predicate :
                        IRI.builder().curie(predicate.toString()).build();
    }

    @Override
    public Node newObject(Object object) {
        return object == null ? null :
                object instanceof Literal ? (Literal) object :
                        object instanceof IRI ? new MemoryResource().id((IRI) object) :
                                new MemoryLiteral(object);
    }

    @Override
    public Literal newLiteral(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Literal) {
            return (Literal) value;
        }
        if (value instanceof Double) {
            return new MemoryLiteral(value).type(DOUBLE);
        }
        if (value instanceof Float) {
            return new MemoryLiteral(value).type(FLOAT);
        }
        if (value instanceof Long) {
            return new MemoryLiteral(value).type(LONG);
        }
        if (value instanceof Integer) {
            return new MemoryLiteral(value).type(INT);
        }
        if (value instanceof Boolean) {
            return new MemoryLiteral(value).type(BOOLEAN);
        }
        // untyped
        return new MemoryLiteral(value);
    }

    public List<Triple> find(IRI predicate, Literal literal) {
        return new Triples(this, predicate, literal).list();
    }

    class Triples {

        private final List<Triple> triples;

        private final boolean recursive;

        Triples(Resource resource, boolean recursive) {
            this.recursive = recursive;
            this.triples = unfold(resource);
        }

        Triples(Resource resource, IRI predicate, Literal literal) {
            this.recursive = true;
            this.triples = find(resource, predicate, literal);
        }

        public List<Triple> list() {
            return triples;
        }

        private List<Triple> unfold(Resource resource) {
            List<Triple> list = new ArrayList<Triple>(32);
            if (resource == null) {
                return list;
            }
            for (IRI pred : resource.predicates()) {
                for (Node obj : resource.objects(pred)) {
                    list.add(new MemoryTriple(resource, pred, obj));
                    if (recursive && obj instanceof Resource) {
                        list.addAll(unfold((Resource) obj));
                    }
                }
            }
            return list;
        }

        private List<Triple> find(Resource resource, IRI predicate, Literal literal) {
            List<Triple> list = new ArrayList<Triple>();
            if (resource == null) {
                return list;
            }
            if (resource.predicates().contains(predicate)) {
                for (Node node : resource.objects(predicate)) {
                    if (literal.equals(node)) {
                        list.add(new MemoryTriple(resource, predicate, node));
                        return list;
                    }
                }
            } else {
                for (IRI pred : resource.predicates()) {
                    for (Node obj : resource.objects(pred)) {
                        if (obj instanceof Resource) {
                            list.addAll(find((Resource) obj, predicate, literal));
                        }
                    }
                }
            }
            return list;
        }
    }

}
