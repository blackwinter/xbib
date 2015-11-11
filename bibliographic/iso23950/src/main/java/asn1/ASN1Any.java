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
 * ASN1 ANY
 * <p/>
 * <p/>
 * The ANY type denotes an arbitary value of an arbitary type.
 * This class also serves as the base class for all ASN.1 classes.
 * <p/>
 * <p/>
 * The ASN.1 syntax is defined in
 * <em>Information Technology - Open Systems Interconnection -
 * Specification of Abstract Syntax Notation One (ASN.1)</em>
 * AS 3625-1991
 * ISO/IEC 8824:1990
 * <p/>
 * <p/>
 * The current implementation assumes values are limited to 32-bit
 * signed integers for tags, lengths, etc.
 */

public class ASN1Any {

    /**
     * Constructor for an ASN.1 ANY object.
     */

    public ASN1Any() {
    }

    /**
     * Constructor for an ASN.1 ANY object from a BER encoding.
     *
     * @param ber       The BER encoding to use.
     * @param check_tag If true, it checks the tag. Does nothing for ASN1Any.
     * @exception ASN1Exception if the BER encoding is incorrect.
     */

    public ASN1Any(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        // tag type and number will be set by ber_decode, which is a
        // virtual method.

        ber_decode(ber, check_tag);
    }


    /**
     * Method for initializing the object from a BER encoding.
     * All classes derived from this one must implement a version of this.
     * <p/>
     * This method will be overridden by derived types.
     *
     * @param ber_enc   The BER encoding to use.
     * @param check_tag If true, it checks the tag. Does nothing for ASN1Any.
     * @exception ASN1Exception If the BER encoding is incorrect.
     * Never occurs for ASN1Any.
     */

    public void
    ber_decode(BEREncoding ber_enc, boolean check_tag)
            throws ASN1Exception {
        asn1any_ber = ber_enc;
    }


    //----------------------------------------------------------------

    /**
     * Constructs a BER encoding for this ASN.1 object.
     * This method is usually overridden by a subclass method.
     *
     * @exception ASN1Exception If the object cannot be BER encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        if (asn1any_ber == null) {
            throw new ASN1EncodingException("ASN.1 ANY: uninitialised");
        }

        return asn1any_ber;
    }

    //----------------------------------------------------------------

    /**
     * Returns a BER encoding of ASN1Any, implicitly tagged.
     *
     * @param    tag_type The type of the implicit tag.
     * @param    tag The implicit tag number.
     * @return The BER encoding of the object.
     * @exception ASN1Exception when invalid or cannot be encoded.
     */

    public BEREncoding ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        if (asn1any_ber == null) {
            throw new ASN1EncodingException("ASN.1 ANY: uninitialised");
        }

        // Can't really do it, this method is really for overriding
        // in the subclasses.

        throw new ASN1EncodingException("ASN.1 ANY: cannot implicitly tag");
    }

    //----------------------------------------------------------------

    /**
     * Returns a new String object representing this ASN.1 object's value.
     *
     * @return A text string representation.
     */

    public String toString() {
        if (asn1any_ber == null) {
            return "<empty ASN.1 ANY>";
        }

        return asn1any_ber.toString();
    }

  /* Hack to support creation of ASN1 ANY types from a BER and have
     it behave normally.  This is not used by any other ASN.1 subclasses.
     It is a waste of space in that respect. */

    private BEREncoding asn1any_ber;

}