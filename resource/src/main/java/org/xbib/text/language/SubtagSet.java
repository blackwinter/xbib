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
package org.xbib.text.language;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

abstract class SubtagSet implements Cloneable, Iterable<Subtag>, Comparable<SubtagSet> {

    protected final Subtag primary;

    protected SubtagSet(Subtag primary) {
        this.primary = primary;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (Subtag subtag : this) {
            if (buf.length() > 0) {
                buf.append('-');
            }
            buf.append(subtag.getName());
        }
        return buf.toString();
    }

    public Iterator<Subtag> iterator() {
        return new SubtagIterator(primary);
    }

    public boolean contains(Subtag subtag) {
        for (Subtag tag : this) {
            if (tag.equals(subtag)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(String tag) {
        return contains(tag, Subtag.Type.SIMPLE);
    }

    public boolean contains(String tag, Subtag.Type type) {
        return contains(new Subtag(type, tag));
    }

    public int length() {
        return toString().length();
    }

    public boolean isValid() {
        for (Subtag subtag : this) {
            if (!subtag.isValid()) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    public int count() {
        int n = 0;
        for (Subtag tag : this) {
            n++;
        }
        return n;
    }

    public Subtag get(int index) {
        if (index < 0 || index > count()) {
            throw new IndexOutOfBoundsException();
        }
        Subtag tag = primary;
        for (int n = 1; n <= index; n++) {
            tag = tag.getNext();
        }
        return tag;
    }

    static class SubtagIterator implements Iterator<Subtag> {
        private Subtag current;

        SubtagIterator(Subtag current) {
            this.current = current;
        }

        public boolean hasNext() {
            return current != null;
        }

        public Subtag next() {
            Subtag tag = current;
            current = tag.getNext();
            return tag;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (Subtag tag : this) {
            result = prime * result + tag.hashCode();
        }
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Lang other = (Lang) obj;
        return hashCode() == other.hashCode();
    }

    public Subtag[] toArray() {
        List<Subtag> tags = new LinkedList<Subtag>();
        for (Subtag tag : this) {
            tags.add(tag);
        }
        return tags.toArray(new Subtag[tags.size()]);
    }

    public List<Subtag> asList() {
        return Arrays.asList(toArray());
    }

    public int compareTo(SubtagSet o) {
        Iterator<Subtag> i = iterator();
        Iterator<Subtag> e = o.iterator();
        for (; i.hasNext() && e.hasNext(); ) {
            Subtag inext = i.next();
            Subtag enext = e.next();
            int c = inext.compareTo(enext);
            if (c != 0) {
                return c;
            }
        }
        if (e.hasNext() && !i.hasNext()) {
            return -1;
        }
        if (i.hasNext() && !e.hasNext()) {
            return 1;
        }
        return 0;
    }

}
