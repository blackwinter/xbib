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
import asn1.ASN1Boolean;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1External;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;




/**
 * Class for representing a <code>InitializeResponse</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * InitializeResponse ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   protocolVersion ProtocolVersion
 *   options Options
 *   preferredMessageSize [5] IMPLICIT INTEGER
 *   exceptionalRecordSize [6] IMPLICIT INTEGER
 *   result [12] IMPLICIT BOOLEAN
 *   implementationId [110] IMPLICIT InternationalString OPTIONAL
 *   implementationName [111] IMPLICIT InternationalString OPTIONAL
 *   implementationVersion [112] IMPLICIT InternationalString OPTIONAL
 *   userInformationField [11] EXPLICIT EXTERNAL OPTIONAL
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class InitializeResponse extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a InitializeResponse.
     */

    public InitializeResponse() {
    }



    /**
     * Constructor for a InitializeResponse from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public InitializeResponse(BEREncoding ber, boolean check_tag)
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
        // InitializeResponse should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun InitializeResponse: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: referenceId ReferenceId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        try {
            s_referenceId = new ReferenceId(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_referenceId = null; // no, not present
        }

        // Decoding: protocolVersion ProtocolVersion

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        s_protocolVersion = new ProtocolVersion(p, true);
        part++;

        // Decoding: options Options

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        s_options = new Options(p, true);
        part++;

        // Decoding: preferredMessageSize [5] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 5 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun InitializeResponse: bad tag in s_preferredMessageSize\n");
        }

        s_preferredMessageSize = new ASN1Integer(p, false);
        part++;

        // Decoding: exceptionalRecordSize [6] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 6 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun InitializeResponse: bad tag in s_exceptionalRecordSize\n");
        }

        s_exceptionalRecordSize = new ASN1Integer(p, false);
        part++;

        // Decoding: result [12] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun InitializeResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 12 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun InitializeResponse: bad tag in s_result\n");
        }

        s_result = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_implementationId = null;
        s_implementationName = null;
        s_implementationVersion = null;
        s_userInformationField = null;
        s_otherInfo = null;

        // Decoding: implementationId [110] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 110 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_implementationId = new InternationalString(p, false);
            part++;
        }

        // Decoding: implementationName [111] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 111 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_implementationName = new InternationalString(p, false);
            part++;
        }

        // Decoding: implementationVersion [112] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 112 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_implementationVersion = new InternationalString(p, false);
            part++;
        }

        // Decoding: userInformationField [11] EXPLICIT EXTERNAL OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 11 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun InitializeResponse: bad BER encoding: s_userInformationField tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun InitializeResponse: bad BER encoding: s_userInformationField tag bad\n");
            }

            s_userInformationField = new ASN1External(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: otherInfo OtherInformation OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_otherInfo = new OtherInformation(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_otherInfo = null; // no, not present
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun InitializeResponse: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the InitializeResponse.
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
     * Returns a BER encoding of InitializeResponse, implicitly tagged.
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

        int num_fields = 5; // number of mandatories
        if (s_referenceId != null) {
            num_fields++;
        }
        if (s_implementationId != null) {
            num_fields++;
        }
        if (s_implementationName != null) {
            num_fields++;
        }
        if (s_implementationVersion != null) {
            num_fields++;
        }
        if (s_userInformationField != null) {
            num_fields++;
        }
        if (s_otherInfo != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_referenceId: ReferenceId OPTIONAL

        if (s_referenceId != null) {
            fields[x++] = s_referenceId.ber_encode();
        }

        // Encoding s_protocolVersion: ProtocolVersion

        fields[x++] = s_protocolVersion.ber_encode();

        // Encoding s_options: Options

        fields[x++] = s_options.ber_encode();

        // Encoding s_preferredMessageSize: INTEGER

        fields[x++] = s_preferredMessageSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);

        // Encoding s_exceptionalRecordSize: INTEGER

        fields[x++] = s_exceptionalRecordSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);

        // Encoding s_result: BOOLEAN

        fields[x++] = s_result.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 12);

        // Encoding s_implementationId: InternationalString OPTIONAL

        if (s_implementationId != null) {
            fields[x++] = s_implementationId.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 110);
        }

        // Encoding s_implementationName: InternationalString OPTIONAL

        if (s_implementationName != null) {
            fields[x++] = s_implementationName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 111);
        }

        // Encoding s_implementationVersion: InternationalString OPTIONAL

        if (s_implementationVersion != null) {
            fields[x++] = s_implementationVersion.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 112);
        }

        // Encoding s_userInformationField: EXTERNAL OPTIONAL

        if (s_userInformationField != null) {
            enc = new BEREncoding[1];
            enc[0] = s_userInformationField.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 11, enc);
        }

        // Encoding s_otherInfo: OtherInformation OPTIONAL

        if (s_otherInfo != null) {
            fields[x++] = s_otherInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the InitializeResponse.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_referenceId != null) {
            str.append("referenceId ");
            str.append(s_referenceId);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("protocolVersion ");
        str.append(s_protocolVersion);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("options ");
        str.append(s_options);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("preferredMessageSize ");
        str.append(s_preferredMessageSize);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("exceptionalRecordSize ");
        str.append(s_exceptionalRecordSize);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("result ");
        str.append(s_result);
        outputted++;

        if (s_implementationId != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("implementationId ");
            str.append(s_implementationId);
            outputted++;
        }

        if (s_implementationName != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("implementationName ");
            str.append(s_implementationName);
            outputted++;
        }

        if (s_implementationVersion != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("implementationVersion ");
            str.append(s_implementationVersion);
            outputted++;
        }

        if (s_userInformationField != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("userInformationField ");
            str.append(s_userInformationField);
            outputted++;
        }

        if (s_otherInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("otherInfo ");
            str.append(s_otherInfo);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ReferenceId s_referenceId; // optional
    public ProtocolVersion s_protocolVersion;
    public Options s_options;
    public ASN1Integer s_preferredMessageSize;
    public ASN1Integer s_exceptionalRecordSize;
    public ASN1Boolean s_result;
    public InternationalString s_implementationId; // optional
    public InternationalString s_implementationName; // optional
    public InternationalString s_implementationVersion; // optional
    public ASN1External s_userInformationField; // optional
    public OtherInformation s_otherInfo; // optional

} // InitializeResponse


//EOF
