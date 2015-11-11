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
package org.xbib.marc.event.reporter;

import org.xbib.marc.event.EventListener;
import org.xbib.marc.event.FieldEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class TSVFieldEventWriter implements EventListener<FieldEvent> {

    private final static int MAX_EVENTS = 100;

    private final Writer writer;

    private FieldEvent lastEvent;

    List<String> events;

    public TSVFieldEventWriter(OutputStream out) throws IOException {
        this.writer = new OutputStreamWriter(out, "UTF-8");
        reset();
    }

    public TSVFieldEventWriter(Writer writer) throws IOException {
        this.writer = writer;
        reset();
    }

    private void reset() {
        events = new ArrayList<String>();
    }

    @Override
    public void receive(FieldEvent event) {
        if (event != null && event.getRecordIdentifier() != null
                && lastEvent != null
                && event.getRecordIdentifier().equals(lastEvent.getRecordIdentifier())
                && events.size() < MAX_EVENTS) {
            events.add(event.toTSV());
        } else {
            try {
                flush();
                reset();
            } catch (IOException e) {
                // ignore
            }
        }
        lastEvent = event;
    }

    public void flush() throws IOException {
        if (events == null || events.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : events) {
            sb.append(s).append('\n');
        }
        writer.write(sb.toString());
    }

    public void close() throws IOException {
        flush();
        writer.close();
    }
}
