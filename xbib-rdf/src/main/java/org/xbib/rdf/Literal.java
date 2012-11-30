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
package org.xbib.rdf;

import java.io.Serializable;
import java.net.URI;

/**
 *  A literal is a value with a type and/or a language
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public interface Literal<O> extends Serializable {

    URI XSD_BOOLEAN = URI.create("xsd:boolean");
    URI XSD_INT = URI.create("xsd:int");
    URI XSD_DOUBLE = URI.create("xsd:double");
    URI XSD_FLOAT = URI.create("xsd:float");
    URI XSD_STRING = URI.create("xsd:string");
    
    /**
     * Set the value for the literal
     * @param value 
     */
    Literal<O> object(O value);

    /**
     * Get the value
     * @return the value
     */
    O object();

    /**
     * Set type of the literal
     * 
     * @param type 
     */
    Literal<O> type(URI type);
    
    /**
     * Get type of the literal
     * @return the type
     */
    URI type();

    /**
     * Set the lianguage of the literal
     * @param lang 
     */
    Literal<O> language(String lang);
    
    /**
     * Get language of the literal
     * @return the language
     */
    String language();
    
    /**
     * Get native value (Java primitive type)
     * @return 
     */
    Object nativeValue();

}
