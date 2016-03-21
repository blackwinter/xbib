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
package org.xbib.tools.merge.holdingslicenses.entities;

import org.xbib.common.xcontent.ToXContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.tools.merge.holdingslicenses.support.StatCounter;

import java.io.IOException;
import java.util.Map;

public class Expression {

    private TitleRecord titleRecord;

    private TitleRecordCluster titleRecordCluster;

    private StatCounter statCounter;

    public Expression(TitleRecord titleRecord, TitleRecordCluster titleRecordCluster) {
        this.titleRecord = titleRecord;
        this.titleRecordCluster = titleRecordCluster;
    }

    public TitleRecord getTitleRecord() {
        return titleRecord;
    }

    public void setStatCounter(StatCounter statCounter) {
        this.statCounter = statCounter;
    }

    public StatCounter getStatCounter() {
        return statCounter;
    }

    public void toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        builder.field("identifierForTheExpression", titleRecord.externalID);
        builder.field("title", titleRecord.getTitleComponents());
        builder.field("firstdate", titleRecordCluster.getFirstDate());
        builder.field("lastdate", titleRecordCluster.getLastDate());
        // struct counter
        builder.startObject("count");
        for (Map.Entry<String,Map<String,Integer>> entry : statCounter.entrySet()) {
            String key = entry.getKey();
            Map<String,Integer> map = entry.getValue();
            builder.startObject(key);
            for (Map.Entry<String,Integer> entry2 : map.entrySet()) {
                String key2 = entry2.getKey();
                Integer value2 = entry2.getValue();
                builder.field(key2, value2);
            }
            builder.endObject();
        }
        builder.endObject();
        // list all titles
        builder.startArray("titles");
        for (TitleRecord titleRecord : titleRecordCluster.getMain()) {
            builder.startObject();
            builder.field("identifierForTheTitle", titleRecord.externalID());
            builder.field("firstdate", titleRecord.firstDate());
            builder.field("lastdate", titleRecord.lastDate());
            builder.array("title", titleRecord.getTitleComponents());
            builder.endObject();
            // list all monographic volumes of this title record
            synchronized (titleRecord.getMonographVolumes()) {
                for (MonographVolume monographVolume : titleRecord.getMonographVolumes()) {
                    builder.startObject();
                    builder.field("identifierForTheTitle", monographVolume.externalID());
                    builder.field("firstdate", monographVolume.firstDate());
                    builder.field("lastdate", monographVolume.lastDate());
                    builder.array("title", monographVolume.getTitleComponents());
                    builder.endObject();
                }
            }
        }
        for (TitleRecord titleRecord : titleRecordCluster.getOther()) {
            builder.startObject();
            builder.field("identifierForTheTitle", titleRecord.externalID());
            builder.field("main", false);
            builder.field("firstdate", titleRecord.firstDate());
            builder.field("lastdate", titleRecord.lastDate());
            builder.array("title", titleRecord.getTitleComponents());
            builder.endObject();
            // list all monographic volumes of this title record
            synchronized (titleRecord.getMonographVolumes()) {
                for (MonographVolume monographVolume : titleRecord.getMonographVolumes()) {
                    builder.startObject();
                    builder.field("identifierForTheTitle", monographVolume.externalID());
                    builder.field("firstdate", monographVolume.firstDate());
                    builder.field("lastdate", monographVolume.lastDate());
                    builder.array("title", monographVolume.getTitleComponents());
                    builder.endObject();
                }
            }
        }
        builder.endArray();
        builder.endObject();
    }

    @Override
    public String toString() {
        return titleRecord.externalID();
    }
}
