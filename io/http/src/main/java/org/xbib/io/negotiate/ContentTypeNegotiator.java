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
package org.xbib.io.negotiate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ContentTypeNegotiator {

    private List<VariantSpec> variantSpecs = new ArrayList<VariantSpec>();

    private List<MediaRangeSpec> defaultAcceptRanges = Collections.singletonList(MediaRangeSpec.parseRange("*/*"));

    private Collection<AcceptHeaderOverride> userAgentOverrides = new ArrayList<AcceptHeaderOverride>();

    public VariantSpec addVariant(String mediaType) {
        VariantSpec result = new VariantSpec(mediaType);
        variantSpecs.add(result);
        return result;
    }

    /**
     * Sets an Accept header to be used as the default if a client does
     * not send an Accept header, or if the Accept header cannot be parsed.
     * Defaults to "* / *".
     */
    public void setDefaultAccept(String accept) {
        this.defaultAcceptRanges = MediaRangeSpec.parseAccept(accept);
    }

    /**
     * Overrides the Accept header for certain user agents. This can be
     * used to implement special-case handling for user agents that send
     * faulty Accept headers.
     *
     * @param userAgentString      A pattern to be matched against the User-Agent header;
     *                             <tt>null</tt> means regardless of User-Agent
     * @param originalAcceptHeader Only override the Accept header if the user agent
     *                             sends this header; <tt>null</tt> means always override
     * @param newAcceptHeader      The Accept header to be used instead
     */
    public void addUserAgentOverride(Pattern userAgentString,
                                     String originalAcceptHeader, String newAcceptHeader) {
        this.userAgentOverrides.add(new AcceptHeaderOverride(userAgentString, originalAcceptHeader, newAcceptHeader));
    }

    public MediaRangeSpec getBestMatch(String accept) {
        return getBestMatch(accept, null);
    }

    public MediaRangeSpec getBestMatch(String accept, String userAgent) {
        if (userAgent == null) {
            userAgent = "";
        }
        Iterator it = userAgentOverrides.iterator();
        String overriddenAccept = accept;
        while (it.hasNext()) {
            AcceptHeaderOverride override = (AcceptHeaderOverride) it.next();
            if (override.matches(accept, userAgent)) {
                overriddenAccept = override.getReplacement();
            }
        }
        return new Negotiation(toAcceptRanges(overriddenAccept)).negotiate(variantSpecs);
    }

    private List<MediaRangeSpec> toAcceptRanges(String accept) {
        if (accept == null) {
            return defaultAcceptRanges;
        }
        List<MediaRangeSpec> result = MediaRangeSpec.parseAccept(accept);
        if (result.isEmpty()) {
            return defaultAcceptRanges;
        }
        return result;
    }

}