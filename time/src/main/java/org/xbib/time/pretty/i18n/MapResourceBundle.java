package org.xbib.time.pretty.i18n;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;

public abstract class MapResourceBundle extends ResourceBundle {

    private Map<String,Object> lookup;

    public MapResourceBundle() {
    }

    public final Object handleGetObject(String key) {
        if (lookup == null) {
            loadLookup();
        }
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    public Enumeration<String> getKeys() {
        if (lookup == null) {
            loadLookup();
        }
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(), parent != null ? parent.getKeys() : null);
    }

    protected Set<String> handleKeySet() {
        if (lookup == null) {
            loadLookup();
        }
        return lookup.keySet();
    }

    abstract protected Map<String,Object> getContents();

    private synchronized void loadLookup() {
        if (lookup != null) {
            return;
        }
        Map<String,Object> contents = getContents();
        Map<String,Object> temp = new HashMap<>();
        for (Map.Entry<String,Object> entry : contents.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            temp.put(key, value);
        }
        lookup = temp;
    }

    static class ResourceBundleEnumeration implements Enumeration<String> {
        private Set<String> set;
        private Iterator<String> iterator;
        private Enumeration<String> enumeration;
        private String next = null;

        public ResourceBundleEnumeration(Set<String> var1, Enumeration<String> var2) {
            this.set = var1;
            this.iterator = var1.iterator();
            this.enumeration = var2;
        }

        public boolean hasMoreElements() {
            if (next == null) {
                if (iterator.hasNext()) {
                    next = iterator.next();
                } else if (enumeration != null) {
                    while (next == null && enumeration.hasMoreElements()) {
                        next = enumeration.nextElement();
                        if (set.contains(next)) {
                            next = null;
                        }
                    }
                }
            }
            return next != null;
        }

        public String nextElement() {
            if (this.hasMoreElements()) {
                String var1 = this.next;
                this.next = null;
                return var1;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
