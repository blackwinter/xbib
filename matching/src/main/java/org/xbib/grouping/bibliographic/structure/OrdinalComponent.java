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
package org.xbib.grouping.bibliographic.structure;

import org.xbib.grouping.bibliographic.GroupDomain;
import org.xbib.grouping.bibliographic.title.TitleComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ordered part title component for cluster key construction.
 * An ordered part title is a title component that can be naturally ordered in sequence,
 * where it may consist of any alphanumerical content
 *
 */
public class OrdinalComponent extends TitleComponent {

    private final Pattern numericPattern = Pattern.compile("^(\\d+).*");

    /**
     * The domain name
     *
     * @return the domain name
     */
    @Override
    public GroupDomain getDomain() {
        return GroupDomain.ORDERED_PART_TITLE;
    }

    /**
     * Add a component
     *
     * @param value
     */
    @Override
    public boolean add(String value) {
        // check if there is a numeral at the beginning, then it's a numbered part
        Matcher m = numericPattern.matcher(value);
        if (m.matches()) {
            return super.add(m.group());
        } else {
            // TODO count words, check length ...
            return super.add(value);
        }
    }

}
