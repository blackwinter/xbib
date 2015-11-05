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
package org.xbib.marc.event;

import org.xbib.marc.Field;

public enum FieldEvent implements Event {

    TAG_CLEANED,
    DATA_SCRUBBED,
    DATA_TRANSFORMED,
    RECORD_NUMBER,
    RECORD_NUMBER_MULTIPLE,
    FIELD_DROPPED {
        private String cause;
        public FieldEvent setCause(String cause) {
            this.cause = cause;
            return this;
        }

        public String getCause() {
            return cause;
        }
    },
    FIELD_MAPPED {
        private String cause;
        public FieldEvent setCause(String cause) {
            this.cause = cause;
            return this;
        }

        public String getCause() {
            return cause;
        }
    },
    RECORD_LABEL_CHANGED {
        private Object prev;
        private Object next;
        public FieldEvent setChange(Object prev, Object next) {
            this.prev = prev;
            this.next = next;
            return this;
        }

        public Object getPrev() {
            return prev;
        }

        public Object getNext() {
            return next;
        }

    }
    ;

    private String recordIdentifier;

    private Field field;

    FieldEvent() {
    }

    public FieldEvent setField(Field field) {
        this.field = field;
        return this;
    }

    public Field getField() {
        return field;
    }

    public FieldEvent setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
        return this;
    }

    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public FieldEvent setCause(String cause) {
        return this;
    }

    public String getCause() {
        return null;
    }

    public FieldEvent setChange(Object prev, Object next) {
        return this;
    }

    public Object getPrev() {
        return null;
    }

    public Object getNext() {
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Event:").append(this.name());
        if (getField() != null) {
            sb.append(':').append(getField());
        }
        if (getCause() != null) {
            sb.append(":cause=").append(getCause());
        }
        if (getPrev() != null) {
            sb.append(":prev=").append(getPrev());
        }
        if (getNext() != null) {
            sb.append(":next=").append(getNext());
        }
        sb.append(']');
        return sb.toString();
    }

    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"event\":\"").append(this.name()).append("\"");
        if (getRecordIdentifier() != null) {
            sb.append(",\"rec\":\"").append(getRecordIdentifier()).append("\"");
        }
        if (getField() != null) {
            sb.append(",\"field\":\"").append(getField()).append("\"");
        }
        if (getCause() != null) {
            sb.append(",\"cause\":\"").append(getCause()).append("\"");
        }
        if (getPrev() != null) {
            sb.append(",\"prev\":\"").append(getPrev()).append("\"");
        }
        if (getNext() != null) {
            sb.append(",\"next\":\"").append(getNext()).append("\"");
        }
        sb.append('}');
        return sb.toString();
    }

    public String toTSV() {
        return this.name() + '\t'
                + (getRecordIdentifier() != null ? getRecordIdentifier() : "") + '\t'
                + (getField() != null ? getField() : "") + '\t'
                + (getCause() != null ? getCause() : "") + '\t'
                + (getPrev() != null ? getPrev() : "") + '\t'
                + (getNext() != null ? getNext() : "");
    }
}
