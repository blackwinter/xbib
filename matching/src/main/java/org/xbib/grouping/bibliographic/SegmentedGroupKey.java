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
package org.xbib.grouping.bibliographic;

import org.xbib.strings.encode.EncoderException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A segmented cluster key
 */
public class SegmentedGroupKey extends AbstractCollection<GroupKeyComponent>
        implements GroupKey {

    private final List<GroupKeyComponent> list = new LinkedList<>();

    /**
     * delimiter
     */
    private char delimiter;
    /**
     * component delimiter
     */
    private char componentDelimiter;
    /**
     * the key code
     */
    private String key;
    /**
     * usable flag
     */
    private boolean usable;

    public SegmentedGroupKey() {
        this(':', '/');
    }

    @Override
    public boolean add(GroupKeyComponent component) {
        return list.add(component);
    }

    @Override
    public Iterator<GroupKeyComponent> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    public SegmentedGroupKey(char componentDelimiter, char delimiter) {
        this.componentDelimiter = componentDelimiter;
        this.delimiter = delimiter;
    }

    /**
     * Get URI of this key. Create key if not already encoded.
     *
     * @param prefix the prefix
     * @return uri
     * @throws URISyntaxException
     * @throws EncoderException
     */
    public URI encodeToURI(String prefix) throws URISyntaxException, EncoderException {
        if (key == null) {
            key = encodeKey(new StringBuilder(prefix));
        }
        return URI.create(key);
    }

    public String encodeToString() throws EncoderException {
        if (key == null) {
            key = encodeKey(new StringBuilder());
        }
        return key;
    }

    public void update(GroupKeyComponent component) {
        for (int i = 0; i < size(); i++) {
            GroupKeyComponent segment = list.get(i);
            if (component.getDomain().equals(segment.getDomain())) {
                list.set(i, component);
            }
        }
    }

    public void remove(GroupKeyComponent component) {
        super.remove(component);
    }

    public GroupKeyComponent getComponent(GroupDomain domain) {
        for (GroupKeyComponent segment : this) {
            if (domain.equals(segment.getDomain())) {
                return segment;
            }
        }
        return null;
    }

    public boolean isUsable() {
        boolean anyusable = false;
        for (GroupKeyComponent segment : this) {
            anyusable = anyusable || segment.isUsable();
        }
        return anyusable;
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }

    public boolean getUsable() {
        return usable;
    }

    public static SegmentedGroupKey parse(URI key, char componentDelimiter, char delimiter) {
        SegmentedGroupKey k = new SegmentedGroupKey(componentDelimiter, delimiter);
        for (String s : key.getSchemeSpecificPart().split(String.valueOf(componentDelimiter))) {
            if (s.length() > 0) {
                String domain = s.substring(0, 1);
                String value = s.substring(1);
                try {
                    k.add(new SimpleComponent(GroupDomain.getDomain(domain), value));
                } catch (InvalidGroupDomainException e) {
                    // silently ignore invalid cluster domains
                }
            }
        }
        return k;
    }

    @Override
    public String toString() {
        try {
            return encodeToString();
        } catch (EncoderException e) {
            return "EncoderException: " + e.getMessage();
        }
    }

    private String encodeKey(StringBuilder sb) throws EncoderException {
        for (GroupKeyComponent segment : this) {
            if (segment.isUsable()) {
                char segmentDelimiter = segment.getDelimiter();
                segment.setDelimiter(delimiter);
                sb.append(segment.getDomain()).append(segment.encode()).append(componentDelimiter);
                segment.setDelimiter(segmentDelimiter);
            }
        }
        int len = sb.length();
        if (len > 1) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }
}
