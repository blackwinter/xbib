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
package z3950.v3;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.BEREncoding;

/**
 * Class for representing a <code>CloseReason</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * CloseReason ::=
 * [211] IMPLICIT INTEGER
 * </pre>
 *
 */

public final class CloseReason extends ASN1Any {

    /**
     * Default constructor for a CloseReason.
     */

    public CloseReason() {
    }

    /**
     * Constructor for a CloseReason from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public CloseReason(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag);
    }

    /**
     * Initializing object from a BER encoding.
     * This method is for internal use only. You should use
     * the constructor that takes a BEREncoding.
     *
     * @param ber       the BER to decode.
     * @param check_tag if the tag should be checked.
     * @throws ASN1Exception if the BER encoding is bad.
     */

    public void
    ber_decode(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        // Check tag matches

        if (check_tag) {
            if (ber.tag_get() != 211 ||
                    ber.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
                throw new ASN1EncodingException
                        ("Zebulun: CloseReason: bad BER: tag=" + ber.tag_get() + " expected 211\n");
            }
        }

        value = new ASN1Integer(ber, false);
    }

    /**
     * Returns a BER encoding of the CloseReason.
     *
     * @exception ASN1Exception Invalid or cannot be encoded.
     * @return The BER encoding.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 211);
    }

    /**
     * Returns a BER encoding of CloseReason, implicitly tagged.
     *
     * @param tag_type The type of the implicit tag.
     * @param tag      The implicit tag.
     * @return The BER encoding of the object.
     * @exception ASN1Exception When invalid or cannot be encoded.
     * @see asn1.BEREncoding#UNIVERSAL_TAG
     * @see asn1.BEREncoding#APPLICATION_TAG
     * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
     * @see asn1.BEREncoding#PRIVATE_TAG
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        return value.ber_encode(tag_type, tag);
    }

    /**
     * Returns a new String object containing a text representing
     * of the CloseReason.
     */

    public String
    toString() {
        return value.toString();
    }

/*
 * Internal variables for class.
 */

    public ASN1Integer value;

/*
 * Enumerated constants for class.
 */

    public static final int E_finished = 0;
    public static final int E_shutdown = 1;
    public static final int E_systemProblem = 2;
    public static final int E_costLimit = 3;
    public static final int E_resources = 4;
    public static final int E_securityViolation = 5;
    public static final int E_protocolError = 6;
    public static final int E_lackOfActivity = 7;
    public static final int E_peerAbort = 8;
    public static final int E_unspecified = 9;

}