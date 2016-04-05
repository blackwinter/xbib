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
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RdfConstants;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.XSDResourceIdentifiers;
import org.xbib.util.LinkedHashMultiMap;
import org.xbib.util.MultiMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A resource is a sequence of properties and of associated resources.
 */
public class MemoryResource implements Resource, Comparable<Resource>, XSDResourceIdentifiers {

    final static String GENID = "genid";

    final static String UNDERSCORE = "_";

    final static String PLACEHOLDER = "_:";

    private IRI id;

    private final MultiMap<IRI, Node> attributes;

    private final Map<IRI, Resource> children;

    private boolean embedded;

    private boolean deleted;

    public static Resource create(String id) {
        return new MemoryResource(IRI.builder().curie(id).build());
    }

    public static Resource create(IRINamespaceContext context, String id) {
        return new MemoryResource(context.expandIRI(IRI.builder().curie(id).build()));
    }

    public MemoryResource(IRI id) {
        this(id, new LinkedHashMultiMap<>(), new LinkedHashMap<>());
    }

    public MemoryResource(MemoryResource resource) {
        this(resource.id(),resource.getAttributes(), resource.getChildren());
        this.deleted = resource.isDeleted();
    }

    public MemoryResource(IRI id, MultiMap<IRI, Node> attributes, Map<IRI, Resource> children) {
        setId(id);
        this.attributes = attributes;
        this.children = children;
    }

    @Override
    public MemoryResource setId(IRI id) {
        this.id = id;
        if (id != null) {
            this.embedded = isBlank(this);
        }
        return this;
    }

    public static boolean isBlank(Resource resource) {
        if (resource == null) {
            return false;
        }
        String scheme = resource.id().getScheme();
        return scheme != null && (GENID.equals(scheme) || UNDERSCORE.equals(scheme));
    }

    @Override
    public IRI id() {
        return id;
    }

    @Override
    public int compareTo(Resource r) {
        return id != null ? id.toString().compareTo(r.id().toString()) : -1;
    }

    @Override
    public int hashCode() {
        return id != null ? id.toString().hashCode() : -1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Resource && id != null && id.toString().equals(((Resource) obj).id().toString());
    }

    @Override
    public boolean isEmbedded() {
        return embedded;
    }

    @Override
    public boolean isVisible() {
        return !embedded;
    }

    public MultiMap<IRI, Node> getAttributes() {
        return attributes;
    }

    public Map<IRI, Resource> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return embedded ? PLACEHOLDER + (id != null ? id.getSchemeSpecificPart() : "<null>") :
                (id != null ? id.toString() : "<null>");
    }

    @Override
    public Resource add(Triple triple) {
        if (triple == null) {
            return this;
        }
        IRI otherId = triple.subject().id();
        if (otherId == null || otherId.equals(id())) {
            add(triple.predicate(), triple.object());
        } else {
            Resource child = children.get(otherId);
            if (child != null) {
                return child.add(triple);
            } else {
                // nothing found, continue with a new resource with new subject
                return new MemoryResource(otherId).add(triple);
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
        return add(predicate, new MemoryResource(iri));
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
            resource.setId(id()); // side effect, transfer our ID to other resource
            Resource r = newResource(predicate);
            resource.triples().stream().forEach(r::add);
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
    @SuppressWarnings("unchecked")
    public Resource add(IRI predicate, List<Object> list) {
        list.stream().forEach(object -> {
            if (object instanceof Map) {
                add(predicate, (Map) object);
            } else if (object instanceof List) {
                add(predicate, ((List) object));
            } else if (object instanceof Resource) {
                add(predicate, (Resource) object);
            } else {
                add(predicate, newLiteral(object));
            }
        });
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Resource add(IRI predicate, Map<Object,Object> map) {
        Resource r = newResource(predicate);
        for (Map.Entry<Object,Object> entry : map.entrySet()) {
            Object pred = entry.getKey();
            Object obj = entry.getValue();
            if (obj instanceof Map) {
                r.add(newPredicate(pred), (Map<Object,Object>) obj);
            } else if (obj instanceof List) {
                r.add(newPredicate(pred), ((List) obj));
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
    public Resource add(String predicate, Map<Object,Object> map) {
        return add(newPredicate(predicate), map);
    }

    @Override
    public Resource add(String predicate, List<Object> list) {
        return add(newPredicate(predicate), list);
    }

    @Override
    public Resource add(Map<Object,Object> map) {
        for (Map.Entry<Object,Object> entry : map.entrySet()) {
            Object pred = entry.getKey();
            Object obj =  entry.getValue();
            if (obj instanceof Map) {
                Resource r = newResource(newPredicate(pred));
                r.add((Map) obj);
            } else if (obj instanceof List) {
                add(newPredicate(pred), ((List) obj));
            } else if (obj instanceof Resource) {
                add(newPredicate(pred), (Resource) obj);
            } else {
                add(newPredicate(pred), newLiteral(obj));
            }
        }
        return this;
    }

    @Override
    public Resource rename(IRI oldPredicate, IRI newPredicate) {
        Collection<Node> node = attributes.remove(oldPredicate);
        if (node != null) {
            node.forEach(n -> attributes.put(newPredicate, n));
        }
        Resource resource = children.remove(oldPredicate);
        if (resource != null) {
            children.put(newPredicate, resource);
        }
        return this;
    }

    @Override
    public Resource rename(String oldPredicate, String newPredicate) {
        rename(newPredicate(oldPredicate), newPredicate(newPredicate));
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
    public List<Node> objects(IRI predicate) {
        return attributes.containsKey(predicate) ? new ArrayList<>(attributes.get(predicate)) : Collections.EMPTY_LIST;
    }

    @Override
    public List<Node> objects(String predicate) {
        return objects(newPredicate(predicate));
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
            Resource r = resources.iterator().next();
            attributes.remove(predicate, r);
            r.objects(predicate).stream().forEach(object -> attributes.put(predicate, object));
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
        Resource r = new BlankMemoryResource();
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
                        subject instanceof IRI ? new MemoryResource((IRI) subject) :
                                new MemoryResource(IRI.builder().curie(subject.toString()).build());
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
                        object instanceof IRI ? new MemoryResource((IRI) object) :
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

    public static Resource from(Map<String, Object> map, Mapper mapper) throws IOException {
        Resource r = new BlankMemoryResource();
        map(mapper, r, null, map);
        return r;
    }

    @SuppressWarnings("unchecked")
    private static void map(Mapper mapper, Resource r, String prefix, Map<String, Object> map) throws IOException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String p = prefix != null ? prefix + "." + key : key;
            Object value = entry.getValue();
            if (value instanceof Map) {
                map(mapper, r, p, (Map<String, Object>) value);
            } else if (value instanceof List) {
                for (Object o : (List) value) {
                    if (o instanceof Map) {
                        map(mapper, r, p, (Map<String, Object>) o);
                    } else {
                        mapper.map(r, p, o.toString());
                    }
                }
            } else {
                if (value != null) {
                    mapper.map(r, p, value.toString());
                }
            }
        }
    }

    public List<Triple> find(IRI predicate, Literal literal) {
        return new Triples(this, predicate, literal).list();
    }

    private static class Triples {

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

        List<Triple> list() {
            return triples;
        }

        private List<Triple> unfold(Resource resource) {
            final List<Triple> list = new ArrayList<>();
            if (resource == null) {
                return list;
            }
            for (IRI pred : resource.predicates()) {
                resource.objects(pred).stream().forEach(node -> {
                    MemoryTriple memoryTriple = new MemoryTriple(resource, pred, node);
                    list.add(memoryTriple);
                    if (recursive && node instanceof Resource) {
                        list.addAll(unfold((Resource) node));
                    }
                });
            }
            return list;
        }

        private List<Triple> find(Resource resource, IRI predicate, Literal literal) {
            final List<Triple> list = new ArrayList<>();
            if (resource == null) {
                return list;
            }
            if (resource.predicates().contains(predicate)) {
                resource.objects(predicate).stream().forEach(node -> {
                    if (literal.equals(node)) {
                        list.add(new MemoryTriple(resource, predicate, node));
                    }
                });
                if (!list.isEmpty()) {
                    return list;
                }
            } else {
                for (IRI pred : resource.predicates()) {
                    resource.objects(pred).stream().forEach(node -> {
                        if (node instanceof Resource) {
                            list.addAll(find((Resource) node, predicate, literal));
                        }
                    });
                }
            }
            return list;
        }
    }

}
