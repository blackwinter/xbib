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
package org.xbib.marc.dialects.aleph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Iterate over Aleph Records with sys numbers from file
 */
public class AlephSysNumberFileIterator implements Closeable, Iterator<Integer> {

    private final static Logger logger = LogManager.getLogger(AlephSysNumberFileIterator.class.getName());
    private BufferedReader reader;
    private boolean error;
    private String filename;
    private String id;
    private int count;

    public AlephSysNumberFileIterator() {
    }

    public AlephSysNumberFileIterator setFilename(String filename) {
        this.filename = filename;
        return this;
    }
    
    private void createReader() {
        this.error = false;
        this.count = 0;
        if (filename == null) {
            logger.error("no iterator file given");
            return;
        }
        try {
            this.reader = new BufferedReader(new FileReader(filename));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasNext() {
        if (reader == null) {
            createReader();
        }
        if (reader == null) {
            return false;
        }
        try {
            String line = reader.readLine();
            if (line != null) {
                this.id = line;
                return true;
            } else {
                reader.close();
                reader = null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            try {
                reader.close();
                reader = null;
                error = true;
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return false;
    }

    @Override
    public Integer next() {
        if (reader == null) {
            return null;
        }
        if (id != null) {
            count++;
            return Integer.valueOf(id);
        } else {
            return null;
        }
    }

    /**
     * Remove operation is not supported
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        logger.info("iterator closed after {} elements {}", count, error ? "with error" : "");
    }
}
