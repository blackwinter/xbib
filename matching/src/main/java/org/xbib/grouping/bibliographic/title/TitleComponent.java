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
package org.xbib.grouping.bibliographic.title;

import org.xbib.grouping.bibliographic.GroupDomain;
import org.xbib.grouping.bibliographic.GroupKeyComponent;
import org.xbib.strings.encode.EncoderException;
import org.xbib.strings.encode.SimpleEntropyEncoder;

import java.text.Normalizer;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Title component for cluster key construction
 *
 */
public class TitleComponent extends AbstractCollection<String>
        implements GroupKeyComponent<String> {

    private final LinkedList<String> list = new LinkedList<>();

    /**
     * We use an entropy-based encoder for titles in cluster keys
     */
    private final SimpleEntropyEncoder enc = new SimpleEntropyEncoder();

    private char delimiter = '/';

    /**
     * The name of this component class.
     *
     * @return the name
     */
    public GroupDomain getDomain() {
        return GroupDomain.TITLE;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public boolean isUsable() {
        return !isEmpty();
    }

    @Override
    public Iterator<String> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    /**
     * Add a component of title word.
     * Keep each title word in sequential order.
     * Normalize title word.
     * Remove librarian comments in brackets.
     * Remove all punctuation and non-visible characters.
     * Add the title word only to components if the number of components
     * will not exceed 5 or title word length is greater than 4
     * and number of components is greater than 1.
     * That is, at most one short title word is added
     * (for very compact and short titles) at the first component position.
     *
     * @param value the value
     */
    @Override
    public boolean add(String value) {
        String normalized = normalize(value);
        int n = size();
        return !(n > 5 || (normalized.length() < 4 && n > 1)) && list.add(normalized);
    }

    /**
     * Encode the titles
     *
     * @return the encoded title form
     */
    public String encode() throws EncoderException {
        StringBuilder sb = new StringBuilder();
        for (String s : this) {
            sb.append(enc.encode(s)).append(delimiter);
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    }

    protected String normalize(String value) {
        String s = value.replaceAll("\\[.+\\]", ""); // remove comments
        s = Normalizer.normalize(s, Normalizer.Form.NFKD);
        return s.replaceAll("[^\\p{L}\\p{N}]", "");
    }

}
