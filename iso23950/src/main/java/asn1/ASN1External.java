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
 * ASN1 EXTERNAL
 * <p/>
 * The <code>EXTERNAL<code> type represents an external object.
 * <p/>
 * According to clause 34.4 of the ASN.1 standard,
 * the EXTERNAL type can be defined as:
 * <p/>
 * <pre>
 * EXTERNAL := [UNIVERSAL 8] IMPLICIT SEQUENCE {
 *               direct_reference OBJECT IDENTIFIER OPTIONAL,
 *               indirect_reference INTEGER OPTIONAL,
 *               data_value_descriptor ObjectDescriptor OPTIONAL,
 *               encoding CHOICE {
 *                 single_ASN1_type [0] ANY,
 *                 octet_Aligned    [1] IMPLICIT OCTET STRING,
 *                 arbitrary        [2] IMPLICIT BIT STRING
 *               }
 *             }
 * </pre>
 * <p/>
 * This construct has been represented by a class with six
 * variables:
 * s_direct_reference,
 * s_indirect_reference,
 * s_data_value_descriptor,
 * c_single_ASN1_type,
 * c_octet_Aligned,
 * c_arbitrary.
 * The first three should be set to point to the appropriate object
 * if present, or null if not.
 * One of the last three variables should be set to non-null (the choice)
 * and the rest to null.
 *
 */

public final class ASN1External extends ASN1Any {
    /**
     * This constant is the ASN.1 UNIVERSAL tag value for an EXTERNAL.
     */

    public static final int TAG = 0x08;

    /**
     * Constructor for an ASN.1 EXTERNAL object. It sets the tag to the
     * default value of UNIVERSAL 8.
     */

    public ASN1External() {
        // All members automatically set to null
    }

    /**
     * Constructor for an ASN.1 EXTERNAL object from a BER encoding.
     *
     * @param ber       The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1Exception If the BER encoding is incorrect.
     */

    public ASN1External(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag);
    }

    /**
     * Method for initializing the object from a BER encoding.
     *
     * @param ber_enc   The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1Exception If the BER encoding is incorrect.
     */

    public void
    ber_decode(BEREncoding ber_enc, boolean check_tag)
            throws ASN1Exception {
        if (check_tag) {
            if (ber_enc.tag_get() != TAG ||
                    ber_enc.tag_type_get() != BEREncoding.UNIVERSAL_TAG) {
                throw new ASN1EncodingException
                        ("ASN.1 EXTERNAL: bad BER: tag=" + ber_enc.tag_get() +
                                " expected " + TAG + "\n");
            }
        }

        if (ber_enc instanceof BERPrimitive) {
            throw new ASN1EncodingException
                    ("ASN.1 EXTERNAL: incorrect form, primitive encoding");
        }

        BERConstructed ber = (BERConstructed) ber_enc;

        s_direct_reference = null;
        s_indirect_reference = null;
        s_data_value_descriptor = null;
        c_singleASN1type = null;
        c_octetAligned = null;
        c_arbitrary = null;

        int num_parts = ber.number_components();
        if (num_parts < 1) {
            throw new ASN1EncodingException("ASN.1 EXTERNAL: incomplete");
        }

        int part = 0;

        BEREncoding p = ber.elementAt(part);

        if (p.tag_get() == ASN1ObjectIdentifier.TAG &&
                p.tag_type_get() == BEREncoding.UNIVERSAL_TAG) {
            s_direct_reference = new ASN1ObjectIdentifier(p, true);

            if (num_parts <= ++part) {
                throw new ASN1EncodingException("ASN.1 EXTERNAL: incomplete");
            }
            p = ber.elementAt(part);
        }

        if (p.tag_get() == ASN1Integer.TAG &&
                p.tag_type_get() == BEREncoding.UNIVERSAL_TAG) {
            s_indirect_reference = new ASN1Integer(p, true);

            if (num_parts <= ++part) {
                throw new ASN1EncodingException("ASN.1 EXTERNAL: incomplete");
            }
            p = ber.elementAt(part);
        }

        if (p.tag_get() == ASN1ObjectDescriptor.TAG &&
                p.tag_type_get() == BEREncoding.UNIVERSAL_TAG) {
            s_data_value_descriptor = new ASN1ObjectDescriptor(p, true);

            if (num_parts <= ++part) {
                throw new ASN1EncodingException("ASN.1 EXTERNAL: incomplete");
            }
            p = ber.elementAt(part);
        }

        // decoding IMPLICIT

        switch (p.tag_get()) {
            case 0:

                if (!(p instanceof BERConstructed)) {
                    throw new ASN1EncodingException
                            ("ASN.1 EXTERNAL: singleASN1type: bad form, primitive");
                }

                if (((BERConstructed) p).number_components() != 1) {
                    throw new ASN1EncodingException
                            ("ASN.1 EXTERNAL: singleASN1type: bad form, no explicit tag");
                }

                c_singleASN1type = ASN1Decoder.toASN1(((BERConstructed) p).elementAt(0));
                break;

            case 1:
                // octet_Aligned [1] IMPLICIT OCTET STRING

                if (p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
                    throw new ASN1EncodingException
                            ("ASN.1 EXTERNAL: encoding: bad tag type " + p);
                }

                c_octetAligned = new ASN1OctetString(p, false);
                break;

            case 2:
                // arbitrary [2] IMPLICIT BIT STRING

                if (p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
                    throw new ASN1EncodingException
                            ("ASN.1 EXTERNAL: encoding: bad tag type " + p);
                }

                c_arbitrary = new ASN1BitString(p, false);
                break;

            default:
                throw new ASN1EncodingException
                        ("ASN.1 EXTERNAL: encoding: tag = " + p.tag_get());
        }

        if (part != (num_parts - 1)) {
            throw new ASN1Exception("ASN.1 EXTERNAL: extra element(s)");
        }
    }

    /**
     * Returns a BER encoding of the EXTERNAL.
     *
     * @return The BER encoding of the EXTERNAL
     * @exception ASN1Exception when the EXTERNAL is invalid
     * and cannot be encoded.
     */

    public BEREncoding ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
    }

    /**
     * Returns a BER encoding of the EXTERNAL.
     *
     * @return The BER encoding of the EXTERNAL
     * @exception ASN1Exception when the EXTERNAL is invalid
     * and cannot be encoded.
     */

    public BEREncoding ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // Calculate length of encoding

        int num_parts = 0;

        if (c_singleASN1type != null) {
            num_parts++;
        }
        if (c_octetAligned != null) {
            num_parts++;
        }
        if (c_arbitrary != null) {
            num_parts++;
        }

        if (num_parts < 1) {
            throw new ASN1Exception("ASN1 EXTERNAL: no encoding has been set");
        }
        if (1 < num_parts) {
            throw new ASN1Exception("ASN1 EXTERNAL: more than one encoding set");
        }

        if (s_direct_reference != null) {
            num_parts++;
        }
        if (s_indirect_reference != null) {
            num_parts++;
        }
        if (s_data_value_descriptor != null) {
            num_parts++;
        }

        // Encode it

        BEREncoding parts[] = new BEREncoding[num_parts];

        int part = 0;

        if (s_direct_reference != null) {
            parts[part++] = s_direct_reference.ber_encode();
        }
        if (s_indirect_reference != null) {
            parts[part++] = s_indirect_reference.ber_encode();
        }
        if (s_data_value_descriptor != null) {
            parts[part++] = s_data_value_descriptor.ber_encode();
        }

        // Encode the choice

        if (c_singleASN1type != null) {
            // explicit tag
            BEREncoding contents[] = new BEREncoding[1];
            contents[0] = c_singleASN1type.ber_encode();
            parts[part] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 0,
                    contents);

        } else if (c_octetAligned != null) {
            parts[part] = c_octetAligned.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG,
                    1);

        } else if (c_arbitrary != null) {
            parts[part] = c_arbitrary.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG,
                    2);
        }

        return new BERConstructed(tag_type, tag, parts);
    }

    /**
     * Returns a new String object representing this ASN.1 object's value.
     */

    public String toString() {
        StringBuffer str = new StringBuffer("{");
        boolean has_element = false;

        if (s_direct_reference != null) {
            str.append("directReference ");
            str.append(s_direct_reference);
            has_element = true;
        }

        if (s_indirect_reference != null) {
            if (has_element) {
                str.append(", ");
            }
            str.append("indirectReference ");
            str.append(s_indirect_reference);
            has_element = true;
        }

        if (s_data_value_descriptor != null) {
            if (has_element) {
                str.append(", ");
            }
            str.append("dataValueDescriptor ");
            str.append(s_data_value_descriptor);
            has_element = true;
        }

        if (has_element) {
            str.append(", ");
        }
        str.append("encoding {");

        if (c_singleASN1type != null) {
            str.append("singleASN1type ");
            str.append(c_singleASN1type);
        }

        if (c_octetAligned != null) {
            str.append("octetAligned ");
            str.append(c_octetAligned);
        }

        if (c_arbitrary != null) {
            str.append("arbitrary ");
            str.append(c_arbitrary);
        }

        str.append("}}");

        return str.toString();
    }

  /*
   * The values are stored in these variables.
   */

    public ASN1ObjectIdentifier s_direct_reference;
    public ASN1Integer s_indirect_reference;
    public ASN1ObjectDescriptor s_data_value_descriptor;

    public ASN1Any c_singleASN1type;
    public ASN1OctetString c_octetAligned;
    public ASN1BitString c_arbitrary;

}