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
 * ASN1Decoder
 * <p/>
 * This is an ASN1 decoder which can handle all the standard ASN.1 types
 * (those with UNIVERSAL tag types).
 * It is used for decoding generic BER encodings into ASN.1 objects.
 *
 */
public class ASN1Decoder {
    private static String foo = "foo";

    public static ASN1Any
    toASN1(BEREncoding ber)
        // Converts the BER encoding to an ASN.1 object.
            throws ASN1Exception {
        // Alternative implementation:
        //
        // Store an associative array of "Class" objects, indexed
        // by tag number and tag type. Use lookup to find the corresponding
        // class, say "matched_class". Then:
        //   ((ASN1Any) matched_class.newInstance()).ber_decode(ber, checkflag);
        //
        // The class table can be populated using strings:
        //   table[?] = Class.forName("ASN1Integer");

        // However this implementation just uses a big switch statememt.

        if (ber.tag_type_get() == BEREncoding.UNIVERSAL_TAG) {
            // UNIVERSAL tag

            switch (ber.tag_get()) {
                case ASN1Boolean.TAG: // 0x01
                    return new ASN1Boolean(ber, true);

                case ASN1Integer.TAG: // 0x02
                    return new ASN1Integer(ber, true);

                case ASN1BitString.TAG: // 0x03
                    return new ASN1BitString(ber, true);

                case ASN1OctetString.TAG: // 0x04
                    return new ASN1OctetString(ber, true);

                case ASN1Null.TAG: // 0x05
                    return new ASN1Null(ber, true);

                case ASN1ObjectIdentifier.TAG: // 0x06
                    return new ASN1ObjectIdentifier(ber, true);

                case ASN1ObjectDescriptor.TAG: // 0x07
                    return new ASN1ObjectDescriptor(ber, true);

                case ASN1External.TAG: // 0x08
                    return new ASN1External(ber, true);

                // 0x09 Real

                case ASN1Enumerated.TAG: // 0x0A
                    return new ASN1Enumerated(ber, true);

                // 0x0B - 0x0F reserved

                case ASN1Sequence.TAG: // 0x10 (16)
                    return new ASN1Sequence(ber, true);

                case ASN1Set.TAG: // 0x11 (17)
                    return new ASN1Set(ber, true);

                case ASN1NumericString.TAG: // 0x12 (18)
                    return new ASN1NumericString(ber, true);

                case ASN1PrintableString.TAG: // 0x13 (19)
                    return new ASN1PrintableString(ber, true);

                case ASN1T61String.TAG: // 0x14 (20)
                    return new ASN1T61String(ber, true);

                case ASN1VideotexString.TAG: // 0x15 (21)
                    return new ASN1VideotexString(ber, true);

                case ASN1IA5String.TAG: // 0x16 (22)
                    return new ASN1IA5String(ber, true);

                case ASN1UTCTime.TAG: // 0x17 (23)
                    return new ASN1UTCTime(ber, true);

                case ASN1GeneralizedTime.TAG: // 0x18 (24)
                    return new ASN1GeneralizedTime(ber, true);

                case ASN1GraphicString.TAG: // 0x19 (25)
                    return new ASN1GraphicString(ber, true);

                case ASN1VisibleString.TAG: // 0x1A (26)
                    return new ASN1VisibleString(ber, true);

                case ASN1GeneralString.TAG: // 0x1B (27)
                    return new ASN1GeneralString(ber, true);
            } // switch
        }

        // Failed to match

        return new ASN1Any(ber, true);
    }
}