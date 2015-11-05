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
package org.xbib.oai.util;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ResumptionToken<T> {
    
    private final static int DEFAULT_INTERVAL_SIZE = 1000;

    private final static ConcurrentHashMap<UUID, ResumptionToken> cache = new ConcurrentHashMap<>();

    private final UUID uuid;

    public final int interval;
    
    private int position;
    
    private T value;
    
    private Date expirationDate;
    
    private int completeListSize;
    
    private int cursor;
    
    private String metadataPrefix;
    
    private String set;
    
    private Date from;
    
    private Date until;

    private boolean completed;
    
    private ResumptionToken() {
        this(DEFAULT_INTERVAL_SIZE);
        this.completed = false;
    }
    
    private ResumptionToken(int interval) {
        this.uuid = UUID.randomUUID();
        this.position = 0;
        this.interval = interval;
        this.value = null;
        cache.put(uuid, this);
    }
    
    public static ResumptionToken newToken(String value) {
        return new ResumptionToken().setValue(value);
    }
    
    public static ResumptionToken get(UUID token) {
        return cache.get(token);
    }
    
    public UUID getKey() {
        return uuid;
    }
    
    public ResumptionToken setPosition(int position) {
        this.position = position;
        return this;
    }
    
    public int getPosition() {
        return position;
    }
    
    public int advancePosition() {
        setPosition(position + interval);
        return getPosition();
    }
    
    public int getInterval() {
        return interval;
    }
    
    public ResumptionToken setValue(T value) {
        this.value = value;
        return this;
    }
    
    public T getValue() {
        return value;
    }
    
    public ResumptionToken setExpirationDate(Date date) {
        this.expirationDate = date;
        return this;
    }
    
    public Date getExpirationDate() {
        return expirationDate;
    }
    
    public ResumptionToken setCompleteListSize(int size) {
        this.completeListSize = size;
        completed = size < interval;
        return this;
    }
    
    public int getCompleteListSize() {
        return completeListSize;
    }
    
    public ResumptionToken setCursor(int cursor) {
        this.cursor = cursor;
        return this;
    }
    
    public int getCursor() {
        return cursor;
    }

    public ResumptionToken setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
        return this;
    }
    
    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public ResumptionToken setSet(String set) {
        this.set = set;
        return this;
    }
    
    public String getSet() {
        return set;
    }
    
    public ResumptionToken setFrom(Date from) {
        this.from = from;
        return this;
    }
    
    public Date getFrom() {
        return from;
    }
    
    public ResumptionToken setUntil(Date until) {
        this.until = until;
        return this;
    }
    
    public Date getUntil() {
        return until;
    }
    
    public void update(int completeListSize, int pageSize, int currentPage) {
        this.completeListSize = completeListSize;
        this.cursor = pageSize * currentPage;
    }

    public boolean isComplete() {
        return completed;
    }
    
    @Override
    public String toString() {
        return value != null ? value.toString() : null;
    }
}

