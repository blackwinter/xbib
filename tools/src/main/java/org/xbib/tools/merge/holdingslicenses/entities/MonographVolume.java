/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2015 Jörg Prante and xbib
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
package org.xbib.tools.merge.holdingslicenses.entities;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MonographVolume extends Monograph {

    private final TitleRecord titleRecord;

    private final List<String> parents = new LinkedList<>();

    public MonographVolume(Map<String, Object> map, TitleRecord titleRecord) {
        super(map);
        this.titleRecord = titleRecord;
        titleRecord.addRelated("hasMonographVolume", this);
    }

    public TitleRecord getTitleRecord() {
        return titleRecord;
    }

    public void addParent(String parent) {
        parents.add(parent);
    }

    public  List<String> getParents() {
        return parents;
    }

    @Override
    protected void build() {
        makeIdentity();
        makeCorporate();
        makeConference();
        makeTitle();
        makePublisher();
        makeDate();
        makeGenre();
        makeIdentifiers();
    }

    @Override
    protected void makeIdentity() {
        String s = getString("RecordIdentifier.identifierForTheRecord");
        this.identifier = s != null ? s : "undefined";
        s = getString("IdentifierZDB.identifierZDB");
        this.externalID = s != null ? s : identifier;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TitleRecord && externalID.equals(((TitleRecord)other).externalID);
    }

    @Override
    public int hashCode() {
        return externalID.hashCode();
    }

    @Override
    public int compareTo(TitleRecord m) {
        return externalID.compareTo(m.getExternalID());
    }

}
