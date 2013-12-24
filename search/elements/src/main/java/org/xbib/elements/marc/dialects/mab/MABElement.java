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
package org.xbib.elements.marc.dialects.mab;

import org.xbib.elements.Element;
import org.xbib.elements.ElementBuilder;
import org.xbib.analyzer.dublincore.DublinCoreProperties;
import org.xbib.analyzer.dublincore.DublinCoreTerms;
import org.xbib.analyzer.dublincore.DublinCoreTermsProperties;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.rdf.Resource;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public abstract class MABElement
        implements Element<FieldCollection, String, MABElementBuilder>,
        DublinCoreProperties, 
        DublinCoreTerms,
        DublinCoreTermsProperties,
        MarcXchangeConstants {

    protected static final Logger logger = LoggerFactory.getLogger(MABElement.class.getName());

    protected Map params;
    
    @Override
    public MABElement setSettings(Map params) {
        this.params = params;
        return this;
    }
    
   @Override
    public Map<String, Object> getSettings() {
        return params;
    }
   
   @Override
    public MABElement begin() {
       return this;
    }

    @Override
    public MABElement build(MABElementBuilder builder, FieldCollection key, String value) {
        return this;
    }

    @Override
    public MABElement end() {
        return this;
    }

    /**
     * Process mapped element with fields. Empty by default.
     *
     * @param builder
     * @param fields
     * @param value
     */
    public boolean fields(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder, FieldCollection fields, String value) {
        // overridden
        return false;
    }

    /**
     * Process mapped element with subfield. Empty by default.
     *
     * @param builder
     * @param field
     * @param subfieldType
     */
    public boolean field(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder, Field field, String subfieldType) {
        // overridden
        return false;
    }

    /**
     * Transform field data
     * @param value
     * @return
     */
    public String data(String resourcePredicate, Resource resource, String property, String value) {
        return value;
    }


    /*protected String cleanDate(String value) {
        String dateStr = "0001";
        String s = value.replaceAll("[^\\p{Digit}]", "");
        if (s.length() > 4) {
            s = s.substring(0, 4);
        }
        if (s.length() > 0) {
            Integer year = Integer.valueOf(s);
            if ((year >= 1000) && (year <= (currentYear + 1))) {
                dateStr = s;
            }
        }
        return dateStr;
    }*/
    
    /** current year for sanity checks */
    private static int currentYear;

    static {
        GregorianCalendar calender = new GregorianCalendar();
        calender.setTime(new Date());
        currentYear = calender.get(GregorianCalendar.YEAR);
    }    
    
    
}
