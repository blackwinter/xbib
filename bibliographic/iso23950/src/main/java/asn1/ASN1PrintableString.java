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
package asn1;

/**
 * Representation for ASN.1 PrintableString.
 * <p/>
 * The <code>PrintableString<code> type denotes an arbitary string
 * of printable characters from the following character set:
 * <p/>
 * <table>
 * <tr><td>Capital letters<td>A, B, ... , Z
 * <tr><td>Small letters<td>a, b, ..., z
 * <tr><td>Digits<td>0, 1, ..., 9
 * <tr><td>Space<td>
 * <tr><td>Apostrophe<td>'
 * <tr><td>Left partnthesis<td>(
 * <tr><td>Right partnthesis<td>)
 * <tr><td>Plus<td>+
 * <tr><td>comma<td>,
 * <tr><td>hyphen<td>-
 * <tr><td>Full stop<td>.
 * <tr><td>Solidus<td>/
 * <tr><td>Colon<td>:
 * <tr><td>Equal sign<td>=
 * <tr><td>Question mark<td>?
 * </table>
 * <p/>
 * This type is a string type.
 */

public final class ASN1PrintableString extends ASN1OctetString {
    /**
     * This constant is the ASN.1 UNIVERSAL tag value for PrintableString.
     */

    public static final int TAG = 0x13;

    /**
     * Constructor for a PrintableString object. It sets the tag to the
     * default value of UNIVERSAL 19 (0x13).
     */

    public ASN1PrintableString(String text) {
        super(text);
    }

    /**
     * Constructor for a PrintableString object from a primitive BER encoding.
     *
     * @param ber       The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1Exception If the BER encoding is incorrect.
     */

    public ASN1PrintableString(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, false);

        if (check_tag) {
            if (ber.tag_get() != TAG ||
                    ber.tag_type_get() != BEREncoding.UNIVERSAL_TAG) {
                throw new ASN1EncodingException
                        ("ASN.1 PrintableString: bad BER: tag=" + ber.tag_get() +
                                " expected " + TAG + "\n");
            }
        }
    }

}