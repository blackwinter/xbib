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
package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1Boolean;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1External;
import asn1.ASN1Integer;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;



/**
 * Class for representing a <code>ExtendedServicesInfo</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * ExtendedServicesInfo ::=
 * SEQUENCE {
 *   commonInfo [0] IMPLICIT CommonInfo OPTIONAL
 *   type [1] IMPLICIT OBJECT IDENTIFIER
 *   name [2] IMPLICIT InternationalString OPTIONAL
 *   privateType [3] IMPLICIT BOOLEAN
 *   restrictionsApply [5] IMPLICIT BOOLEAN
 *   feeApply [6] IMPLICIT BOOLEAN
 *   available [7] IMPLICIT BOOLEAN
 *   retentionSupported [8] IMPLICIT BOOLEAN
 *   waitAction [9] IMPLICIT INTEGER
 *   description [10] IMPLICIT HumanString OPTIONAL
 *   specificExplain [11] IMPLICIT EXTERNAL OPTIONAL
 *   esASN [12] IMPLICIT InternationalString OPTIONAL
 * }
 * </pre>
 *
 */
public final class ExtendedServicesInfo extends ASN1Any {
    /**
     * Default constructor for a ExtendedServicesInfo.
     */

    public ExtendedServicesInfo() {
    }



    /**
     * Constructor for a ExtendedServicesInfo from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ExtendedServicesInfo(BEREncoding ber, boolean check_tag)
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
        // ExtendedServicesInfo should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("ExtendedServicesInfo: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: commonInfo [0] IMPLICIT CommonInfo OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_commonInfo = new CommonInfo(p, false);
            part++;
        }

        // Decoding: type [1] IMPLICIT OBJECT IDENTIFIER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ExtendedServicesInfo: bad tag in s_type\n");
        }

        s_type = new ASN1ObjectIdentifier(p, false);
        part++;

        // Decoding: name [2] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_name = new InternationalString(p, false);
            part++;
        }

        // Decoding: privateType [3] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 3 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ExtendedServicesInfo: bad tag in s_privateType\n");
        }

        s_privateType = new ASN1Boolean(p, false);
        part++;

        // Decoding: restrictionsApply [5] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 5 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ExtendedServicesInfo: bad tag in s_restrictionsApply\n");
        }

        s_restrictionsApply = new ASN1Boolean(p, false);
        part++;

        // Decoding: feeApply [6] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 6 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ExtendedServicesInfo: bad tag in s_feeApply\n");
        }

        s_feeApply = new ASN1Boolean(p, false);
        part++;

        // Decoding: available [7] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 7 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ExtendedServicesInfo: bad tag in s_available\n");
        }

        s_available = new ASN1Boolean(p, false);
        part++;

        // Decoding: retentionSupported [8] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 8 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ExtendedServicesInfo: bad tag in s_retentionSupported\n");
        }

        s_retentionSupported = new ASN1Boolean(p, false);
        part++;

        // Decoding: waitAction [9] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 9 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ExtendedServicesInfo: bad tag in s_waitAction\n");
        }

        s_waitAction = new ASN1Integer(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_description = null;
        s_specificExplain = null;
        s_esASN = null;

        // Decoding: description [10] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 10 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_description = new HumanString(p, false);
            part++;
        }

        // Decoding: specificExplain [11] IMPLICIT EXTERNAL OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 11 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_specificExplain = new ASN1External(p, false);
            part++;
        }

        // Decoding: esASN [12] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 12 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_esASN = new InternationalString(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("ExtendedServicesInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the ExtendedServicesInfo.
     *
     * @exception ASN1Exception Invalid or cannot be encoded.
     * @return The BER encoding.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
    }



    /**
     * Returns a BER encoding of ExtendedServicesInfo, implicitly tagged.
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
        // Calculate the number of fields in the encoding

        int num_fields = 7; // number of mandatories
        if (s_commonInfo != null) {
            num_fields++;
        }
        if (s_name != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }
        if (s_specificExplain != null) {
            num_fields++;
        }
        if (s_esASN != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;

        // Encoding s_commonInfo: CommonInfo OPTIONAL

        if (s_commonInfo != null) {
            fields[x++] = s_commonInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_type: OBJECT IDENTIFIER

        fields[x++] = s_type.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_name: InternationalString OPTIONAL

        if (s_name != null) {
            fields[x++] = s_name.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_privateType: BOOLEAN

        fields[x++] = s_privateType.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);

        // Encoding s_restrictionsApply: BOOLEAN

        fields[x++] = s_restrictionsApply.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);

        // Encoding s_feeApply: BOOLEAN

        fields[x++] = s_feeApply.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);

        // Encoding s_available: BOOLEAN

        fields[x++] = s_available.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);

        // Encoding s_retentionSupported: BOOLEAN

        fields[x++] = s_retentionSupported.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);

        // Encoding s_waitAction: INTEGER

        fields[x++] = s_waitAction.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);

        // Encoding s_description: HumanString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 10);
        }

        // Encoding s_specificExplain: EXTERNAL OPTIONAL

        if (s_specificExplain != null) {
            fields[x++] = s_specificExplain.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 11);
        }

        // Encoding s_esASN: InternationalString OPTIONAL

        if (s_esASN != null) {
            fields[x++] = s_esASN.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 12);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the ExtendedServicesInfo.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_commonInfo != null) {
            str.append("commonInfo ");
            str.append(s_commonInfo);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("type ");
        str.append(s_type);
        outputted++;

        if (s_name != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("name ");
            str.append(s_name);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("privateType ");
        str.append(s_privateType);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("restrictionsApply ");
        str.append(s_restrictionsApply);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("feeApply ");
        str.append(s_feeApply);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("available ");
        str.append(s_available);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("retentionSupported ");
        str.append(s_retentionSupported);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("waitAction ");
        str.append(s_waitAction);
        outputted++;

        if (s_description != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("description ");
            str.append(s_description);
            outputted++;
        }

        if (s_specificExplain != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("specificExplain ");
            str.append(s_specificExplain);
            outputted++;
        }

        if (s_esASN != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("esASN ");
            str.append(s_esASN);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public CommonInfo s_commonInfo; // optional
    public ASN1ObjectIdentifier s_type;
    public InternationalString s_name; // optional
    public ASN1Boolean s_privateType;
    public ASN1Boolean s_restrictionsApply;
    public ASN1Boolean s_feeApply;
    public ASN1Boolean s_available;
    public ASN1Boolean s_retentionSupported;
    public ASN1Integer s_waitAction;
    public HumanString s_description; // optional
    public ASN1External s_specificExplain; // optional
    public InternationalString s_esASN; // optional


/*
 * Enumerated constants for class.
 */

    // Enumerated constants for waitAction
    public static final int E_waitSupported = 1;
    public static final int E_waitAlways = 2;
    public static final int E_waitNotSupported = 3;
    public static final int E_depends = 4;
    public static final int E_notSaying = 5;

}