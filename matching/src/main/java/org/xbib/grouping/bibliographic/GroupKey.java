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
import java.util.Collection;

/**
 * A cluster key holds a collection of key components,
 * where the encoded key representations are used to generate a
 * uniform resource identifier.
 * The order of the components is maintained in the order of adding
 * components to the cluster key.
 * A component can be updated or removed.
 *
 */
public interface GroupKey extends Collection<GroupKeyComponent> {

    /**
     * Encode cluster key as string or null
     *
     * @return the encoded cluster key or null
     */
    String encodeToString() throws EncoderException;

    /**
     * Encode cluster key as a Uniform Resource Identifier
     *
     * @param prefix the URI prefix
     * @return the uri
     * @throws URISyntaxException if a cluster key can not be constructed as an URI
     */
    URI encodeToURI(String prefix) throws URISyntaxException, EncoderException;

    /**
     * Get cluster key component for a given domain
     *
     * @param domain the domain
     * @return the cluster key component
     */
    GroupKeyComponent getComponent(GroupDomain domain);

    /**
     * Update cluster key component
     *
     * @param component
     */
    void update(GroupKeyComponent component);

    /**
     * Returns true if at least one of the cluster key components is usable
     *
     * @return
     */
    boolean isUsable();

    /**
     * Set or unset usable flag. This flag can invalidate the key, if the application
     * decides the key should not be used, but retains the components.
     */
    void setUsable(boolean usable);

    /**
     * Get usable flag.
     *
     * @return
     */
    boolean getUsable();
}
