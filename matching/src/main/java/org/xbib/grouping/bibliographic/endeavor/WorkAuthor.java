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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.strings.encode.BaseformEncoder;
import org.xbib.strings.encode.EncoderException;
import org.xbib.strings.encode.WordBoundaryEntropyEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * An identifiable endeavor for a work created by an author
 */
public class WorkAuthor implements IdentifiableEndeavor {

    private final static Logger logger = LogManager.getLogger(WorkAuthor.class);

    private StringBuilder workName;

    private StringBuilder authorName;

    private StringBuilder chronology;

    private final WordBoundaryEntropyEncoder encoder = new WordBoundaryEntropyEncoder();

    /* These work titles can not be work titles and are blacklisted */
    private final static Set<String> blacklist = readResource("org/xbib/grouping/bibliographic/endeavor/work-blacklist.txt");

    public WorkAuthor() {
    }

    public WorkAuthor workName(CharSequence workName) {
        if (workName != null) {
            this.workName = new StringBuilder(workName);
        }
        return this;
    }


    public WorkAuthor authorName(Collection<String> authorNames) {
        authorNames.forEach(this::authorName);
        return this;
    }

    /**
     * "Forename Givenname" or "Givenname, Forname"
     *
     * @param authorName author name
     * @return this
     */
    public WorkAuthor authorName(String authorName) {
        if (authorName == null) {
            return this;
        }
        // check if this is the work name
        if (workName != null && !authorName.isEmpty() && authorName.equals(workName.toString())) {
            logger.warn("work name is equal to author name: {}", authorName);
            return this;
        }
        // check if there is a comma, then it's "Givenname, Forname"
        if (authorName.indexOf(',') > 0) {
            this.authorName = new StringBuilder(authorName.replaceAll("[.,]",""));
            return this;
        }
        // get last author name part first
        String[] s = authorName.split("\\s+");
        if (s.length > 0) {
            String lastName = s[s.length - 1];
            if (this.authorName == null) {
                this.authorName = new StringBuilder();
            }
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
        return this;
    }


    public WorkAuthor authorNameWithForeNames(String lastName, String foreName) {
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
    public WorkAuthor authorNameWithInitials(String lastName, String initials) {
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

    public WorkAuthor chronology(String chronology) {
        if (chronology != null) {
            if (this.chronology == null) {
                this.chronology = new StringBuilder();
            }
            this.chronology.append(".").append(chronology);
        }
        return this;
    }

    public String createIdentifier() {
        if (workName == null || workName.length() == 0) {
            return null;
        }
        if (!isValidWork()) {
            return null;
        }
        String wName = BaseformEncoder.normalizedFromUTF8(workName.toString())
                .replaceAll("aeiou", ""); // TODO Unicode vocal category?
        try {
            wName = encoder.encode(wName);
        } catch (EncoderException e) {
            // ignore
        }
        if (isBlacklisted(workName)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("w").append(wName);
        if (authorName != null) {
            String aName = BaseformEncoder.normalizedFromUTF8(authorName.toString())
                    .replaceAll("aeiou", ""); // TODO Unicode vocal category?
            try {
                aName = encoder.encode(aName);
            } catch (EncoderException e) {
                //ignore
            }
            sb.append(".a").append(aName);
        }
        if (chronology != null) {
            sb.append(chronology);
        }
        return sb.toString();
    }

    public boolean isValidWork() {
        // only a single word in work name and no author name is not valid
        if (authorName == null) {
            int pos = workName.toString().indexOf(' ');
            if (pos < 0) {
                return false;
            }
        }
        return true;
    }

    private final static Pattern p1 = Pattern.compile(".*Cover and Back matter.*", Pattern.CASE_INSENSITIVE);

    public Set<String> blacklist() {
        return blacklist;
    }

    public boolean isBlacklisted(CharSequence work) {
        return blacklist.contains(work.toString()) || p1.matcher(work).matches();
    }

    private static Set<String> readResource(String resource) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        Set<String> set = new HashSet<>();
        if (url != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName("UTF-8")))) {
                reader.lines().filter(line -> !line.startsWith("#")).forEach(set::add);
            } catch (IOException e) {
                // do nothing
            }
        }
        return set;
    }
}
