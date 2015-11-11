/**
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
package org.xbib.charset;

import java.lang.ref.SoftReference;

/**
 * Extra bibliographic character sets.
 */
public class CharsetProvider extends AbstractCharsetProvider {

    /**
     * The reference to the character set instance.
     * If there are no remaining references to this instance,
     * the character set will be removed by the garbage collector.
     */
    static volatile SoftReference instance = null;

    /**
     * Constructor
     */
    public CharsetProvider() {
        charset("ANSI-Z39_47", "ANSI_Z39_47",
            new String[] { "ANSI_Z39_47", "ANSI-Z39-47", "Z39_47", "Z39-47", "ANSEL", "Ansel", "ansel"});
        charset("x-MAB", "MabCharset",
            new String[] { "x-mab",  "ISO-5426", "ISO_5426", "ISO_5426:1983", "5426-1983", "MAB2" } );
        charset("PICA", "Pica", 
            new String[] { "Pica", "pica"} );
        charset("x-PICA", "PicaCharset",
            new String[] { "x-pica" } );
        charset("ISO_5428", "ISO_5428",
            new String[]{ "ISO-5428", "ISO-5428:1984", "iso-ir-55" });
        instance = new SoftReference(this);
    }

    /**
     * List all aliases defined for a character set.
     * @param s the name of the character set
     * @return an alias string array
     */
    public static String[] aliasesFor(String s) {
        SoftReference softreference = instance;
        CharsetProvider charsets = null;
        if (softreference != null) {
            charsets = (CharsetProvider) softreference.get();
        }
        if (charsets == null) {
            charsets = new CharsetProvider();
            instance = new SoftReference(charsets);
        }
        return charsets.aliases(s);
    }
}
