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
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;



/**
 * Class for representing a <code>ElementInfo</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * ElementInfo ::=
 * SEQUENCE {
 *   elementName [1] IMPLICIT InternationalString
 *   elementTagPath [2] IMPLICIT Path
 *   dataType [3] EXPLICIT ElementDataType OPTIONAL
 *   required [4] IMPLICIT BOOLEAN
 *   repeatable [5] IMPLICIT BOOLEAN
 *   description [6] IMPLICIT HumanString OPTIONAL
 * }
 * </pre>
 */
public final class ElementInfo extends ASN1Any {
    /**
     * Default constructor for a ElementInfo.
     */
    public ElementInfo() {
    }
    /**
     * Constructor for a ElementInfo from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ElementInfo(BEREncoding ber, boolean check_tag)
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
        // ElementInfo should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    (" ElementInfo: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: elementName [1] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" ElementInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    (" ElementInfo: bad tag in s_elementName\n");
        }

        s_elementName = new InternationalString(p, false);
        part++;

        // Decoding: elementTagPath [2] IMPLICIT Path

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" ElementInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 2 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    (" ElementInfo: bad tag in s_elementTagPath\n");
        }

        s_elementTagPath = new Path(p, false);
        part++;

        // Decoding: dataType [3] EXPLICIT ElementDataType OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" ElementInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        (" ElementInfo: bad BER encoding: s_dataType tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        (" ElementInfo: bad BER encoding: s_dataType tag bad\n");
            }

            s_dataType = new ElementDataType(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: required [4] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" ElementInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 4 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    (" ElementInfo: bad tag in s_required\n");
        }

        s_required = new ASN1Boolean(p, false);
        part++;

        // Decoding: repeatable [5] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" ElementInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 5 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    (" ElementInfo: bad tag in s_repeatable\n");
        }

        s_repeatable = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_description = null;

        // Decoding: description [6] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_description = new HumanString(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception(" ElementInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the ElementInfo.
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
     * Returns a BER encoding of ElementInfo, implicitly tagged.
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

        int num_fields = 4; // number of mandatories
        if (s_dataType != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_elementName: InternationalString

        fields[x++] = s_elementName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_elementTagPath: Path

        fields[x++] = s_elementTagPath.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);

        // Encoding s_dataType: ElementDataType OPTIONAL

        if (s_dataType != null) {
            enc = new BEREncoding[1];
            enc[0] = s_dataType.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
        }

        // Encoding s_required: BOOLEAN

        fields[x++] = s_required.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);

        // Encoding s_repeatable: BOOLEAN

        fields[x++] = s_repeatable.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);

        // Encoding s_description: HumanString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the ElementInfo.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("elementName ");
        str.append(s_elementName);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("elementTagPath ");
        str.append(s_elementTagPath);
        outputted++;

        if (s_dataType != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("dataType ");
            str.append(s_dataType);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("required ");
        str.append(s_required);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("repeatable ");
        str.append(s_repeatable);
        outputted++;

        if (s_description != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("description ");
            str.append(s_description);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public InternationalString s_elementName;
    public Path s_elementTagPath;
    public ElementDataType s_dataType; // optional
    public ASN1Boolean s_required;
    public ASN1Boolean s_repeatable;
    public HumanString s_description; // optional

} // ElementInfo


//EOF
