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
package org.xbib.text;

/**
 * Base implementation of a CodepointIterator that filters the output of another CodpointIterator
 */
public abstract class DelegatingCodepointIterator extends CodepointIterator {

    private CodepointIterator internal;

    protected DelegatingCodepointIterator(CodepointIterator internal) {
        this.internal = internal;
    }

    @Override
    protected char get() {
        return internal.get();
    }

    @Override
    protected char get(int index) {
        return internal.get(index);
    }

    @Override
    public boolean hasNext() {
        return internal.hasNext();
    }

    @Override
    public boolean isHigh(int index) {
        return internal.isHigh(index);
    }

    @Override
    public boolean isLow(int index) {
        return internal.isLow(index);
    }

    @Override
    public int limit() {
        return internal.limit();
    }

    @Override
    public Codepoint next() {
        return internal.next();
    }

    @Override
    public char[] nextChars() {
        return internal.nextChars();
    }

    @Override
    public Codepoint peek() {
        return internal.peek();
    }

    @Override
    public Codepoint peek(int index) {
        return internal.peek(index);
    }

    @Override
    public char[] peekChars() {
        return internal.peekChars();
    }

    @Override
    public int position() {
        return internal.position();
    }

    @Override
    public int remaining() {
        return internal.remaining();
    }

    @Override
    public void position(int position) {
        internal.position(position);
    }

}
