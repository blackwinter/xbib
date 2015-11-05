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
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;


/**
 * Class for representing a <code>Close</code> from <code>Z39-50-APDU-1995</code>
 *
 * <pre>
 * Close ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   closeReason CloseReason
 *   diagnosticInformation [3] IMPLICIT InternationalString OPTIONAL
 *   resourceReportFormat [4] IMPLICIT ResourceReportId OPTIONAL
 *   resourceReport [5] EXPLICIT ResourceReport OPTIONAL
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 */

public final class Close extends ASN1Any {

    /**
     * Default constructor for a Close.
     */

    public Close() {
    }

    /**
     * Constructor for a Close from a BER encoding.
     *
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Close(BEREncoding ber, boolean check_tag)
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
        // Close should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun Close: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: referenceId ReferenceId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun Close: incomplete");
        }
        p = ber_cons.elementAt(part);

        try {
            s_referenceId = new ReferenceId(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_referenceId = null; // no, not present
        }

        // Decoding: closeReason CloseReason

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun Close: incomplete");
        }
        p = ber_cons.elementAt(part);

        s_closeReason = new CloseReason(p, true);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_diagnosticInformation = null;
        s_resourceReportFormat = null;
        s_resourceReport = null;
        s_otherInfo = null;

        // Decoding: diagnosticInformation [3] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_diagnosticInformation = new InternationalString(p, false);
            part++;
        }

        // Decoding: resourceReportFormat [4] IMPLICIT ResourceReportId OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_resourceReportFormat = new ResourceReportId(p, false);
            part++;
        }

        // Decoding: resourceReport [5] EXPLICIT ResourceReport OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Close: bad BER encoding: s_resourceReport tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun Close: bad BER encoding: s_resourceReport tag bad\n");
            }

            s_resourceReport = new ResourceReport(tagged.elementAt(0), true);
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
            throw new ASN1Exception("Close: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }


    /**
     * Returns a BER encoding of the Close.
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
     * Returns a BER encoding of Close, implicitly tagged.
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

        int num_fields = 1; // number of mandatories
        if (s_referenceId != null) {
            num_fields++;
        }
        if (s_diagnosticInformation != null) {
            num_fields++;
        }
        if (s_resourceReportFormat != null) {
            num_fields++;
        }
        if (s_resourceReport != null) {
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

        // Encoding s_closeReason: CloseReason

        fields[x++] = s_closeReason.ber_encode();

        // Encoding s_diagnosticInformation: InternationalString OPTIONAL

        if (s_diagnosticInformation != null) {
            fields[x++] = s_diagnosticInformation.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding s_resourceReportFormat: ResourceReportId OPTIONAL

        if (s_resourceReportFormat != null) {
            fields[x++] = s_resourceReportFormat.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_resourceReport: ResourceReport OPTIONAL

        if (s_resourceReport != null) {
            enc = new BEREncoding[1];
            enc[0] = s_resourceReport.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, enc);
        }

        // Encoding s_otherInfo: OtherInformation OPTIONAL

        if (s_otherInfo != null) {
            fields[x++] = s_otherInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }


    /**
     * Returns a new String object containing a text representing
     * of the Close.
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


        str.append("closeReason ");
        str.append(s_closeReason);
        outputted++;

        if (s_diagnosticInformation != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("diagnosticInformation ");
            str.append(s_diagnosticInformation);
            outputted++;
        }

        if (s_resourceReportFormat != null) {
            if (0 < outputted) {
             str.append(", ");
            }


            str.append("resourceReportFormat ");
            str.append(s_resourceReportFormat);
            outputted++;
        }

        if (s_resourceReport != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("resourceReport ");
            str.append(s_resourceReport);
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
    public CloseReason s_closeReason;
    public InternationalString s_diagnosticInformation; // optional
    public ResourceReportId s_resourceReportFormat; // optional
    public ResourceReport s_resourceReport; // optional
    public OtherInformation s_otherInfo; // optional

}