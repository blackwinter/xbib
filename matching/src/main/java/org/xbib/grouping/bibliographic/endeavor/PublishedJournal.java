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

public class PublishedJournal implements IdentifiableEndeavor {

    private String journalName;

    private String publisherName;

    public PublishedJournal() {
    }

    public PublishedJournal journalName(String journalName) {
        this.journalName = journalName;
        return this;
    }

    public PublishedJournal publisherName(String publisherName) {
        this.publisherName = publisherName;
        return this;
    }

    public String createIdentifier() {
        if (journalName == null) {
            return null;
        }
        // remove punctuation
        journalName = journalName.replaceAll("\\p{P}","");
        // remove "... series"
        journalName = journalName.replaceAll(" [sS]eries$", "");
        WordBoundaryEntropyEncoder encoder = new WordBoundaryEntropyEncoder();
        StringBuilder sb = new StringBuilder();
        sb.append("s");
        String shortJournalName = BaseformEncoder.normalizedFromUTF8(journalName);
        int l = shortJournalName.length();
        if (l == 0) {
            shortJournalName = journalName; // restore non-latin-script titles
        } else {
            try {
                shortJournalName = encoder.encode(shortJournalName);
            } catch (EncoderException e) {
                // ignore
            }
        }
        shortJournalName = shortJournalName.replaceAll("\\s","");
        sb.append(shortJournalName);
        if (publisherName != null) {
            publisherName = publisherName.replaceAll("\\p{P}", "");
            String shortPublisherName = BaseformEncoder.normalizedFromUTF8(publisherName);
            l = shortPublisherName.length();
            if (l == 0) {
                shortPublisherName = publisherName; // restore
            } else {
                try {
                    shortPublisherName = encoder.encode(shortPublisherName);
                } catch (EncoderException e) {
                    // ignore
                }
            }
            shortPublisherName = shortPublisherName.replaceAll("\\s", "");
            sb.append(shortPublisherName);
        }
        return sb.toString();
    }
}
