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
package org.xbib.grouping.bibliographic.endeavor;

import org.xbib.strings.encode.BaseformEncoder;
import org.xbib.strings.encode.EncoderException;
import org.xbib.strings.encode.WordBoundaryEntropyEncoder;

import java.util.Collection;

/**
 * An identifiable endeavor for an author
 */
public class AuthorKey implements IdentifiableEndeavor {

    private StringBuilder authorName;

    private final WordBoundaryEntropyEncoder encoder = new WordBoundaryEntropyEncoder();

    public AuthorKey() {
    }

    public AuthorKey authorName(Collection<String> authorNames) {
        authorNames.forEach(this::authorName);
        return this;
    }

    /**
     * "Forename Givenname" or "Givenname, Forname"
     *
     * @param authorName author name
     * @return this
     */
    public AuthorKey authorName(String authorName) {
        if (authorName == null) {
            return this;
        }
        if (this.authorName == null) {
            this.authorName = new StringBuilder();
        }
        String[] s = authorName.split("\\s+");
        if (s.length > 0) {
            // check if there is a comma, then it's "Givenname, Forname"
            if (s[0].indexOf(',') > 0) {
                String lastname = s[0];
                this.authorName.append(lastname);
                if (s.length > 1) {
                    this.authorName.append(' ');
                }
                for (int i = 1; i < s.length; i++) {
                    if (s[i].length() > 0) {
                        this.authorName.append(s[i].charAt(0));
                    }
                }
            } else {
                // get last author name part first
                String lastName = s[s.length - 1];
                this.authorName.append(lastName);
                if (s.length > 1) {
                    this.authorName.append(' ');
                }
                for (int i = 0; i < s.length - 1; i++) {
                    if (s[i].length() > 0) {
                        this.authorName.append(s[i].charAt(0));
                    }
                }
            }
        }
        return this;
    }

    public AuthorKey authorNameWithForeNames(String lastName, String foreName) {
        if (foreName == null) {
            return authorName(lastName);
        }
        StringBuilder sb = new StringBuilder();
        for (String s : foreName.split("\\s+")) {
            if (s.length() > 0) {
                sb.append(s.charAt(0));
            }
        }
        if (lastName != null) {
            if (this.authorName == null) {
                this.authorName = new StringBuilder(lastName);
                if (sb.length() > 0) {
                    this.authorName.append(' ').append(sb);
                }
            } else {
                this.authorName.append(lastName);
                if (sb.length() > 0) {
                    this.authorName.append(' ').append(sb);
                }
            }
        }
        return this;
    }

    /**
     * "Smith J"
     * @param lastName last name
     * @param initials initials
     * @return work author key
     */
    public AuthorKey authorNameWithInitials(String lastName, String initials) {
        if (initials != null) {
            initials = initials.replaceAll("\\s+", "");
        }
        if (lastName != null) {
            if (this.authorName == null) {
                this.authorName = new StringBuilder(lastName);
                if (initials != null && initials.length() > 0) {
                    this.authorName.append(' ').append(initials);
                }
            } else {
                this.authorName.append(lastName);
                if (initials != null && initials.length() > 0) {
                    this.authorName.append(' ').append(initials);
                }
            }
        }
        return this;
    }

    public String createIdentifier() {
        StringBuilder sb = new StringBuilder();
        if (authorName != null) {
            String aName = BaseformEncoder.normalizedFromUTF8(authorName.toString())
                    .replaceAll("aeiou", ""); // TODO Unicode vocal category?
            try {
                aName = encoder.encode(aName);
            } catch (EncoderException e) {
                //ignore
            }
            sb.append(aName);
        }
        return sb.toString();
    }
}
