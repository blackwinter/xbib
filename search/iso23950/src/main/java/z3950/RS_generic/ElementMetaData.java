/*
 * $Source$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 1998, Hoylen Sue.  All Rights Reserved.
 * <h.sue@ieee.org>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  Refer to
 * the supplied license for more details.
 *
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:23 UTC
 */



package z3950.RS_generic;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1External;
import asn1.ASN1OctetString;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;



/**
 * Class for representing a <code>ElementMetaData</code> from <code>RecordSyntax-generic</code>
 * <p/>
 * <pre>
 * ElementMetaData ::=
 * SEQUENCE {
 *   seriesOrder [1] IMPLICIT Order OPTIONAL
 *   usageRight [2] IMPLICIT Usage OPTIONAL
 *   hits [3] IMPLICIT SEQUENCE OF HitVector OPTIONAL
 *   displayName [4] IMPLICIT InternationalString OPTIONAL
 *   supportedVariants [5] IMPLICIT SEQUENCE OF Variant OPTIONAL
 *   message [6] IMPLICIT InternationalString OPTIONAL
 *   elementDescriptor [7] IMPLICIT OCTET STRING OPTIONAL
 *   surrogateFor [8] IMPLICIT TagPath OPTIONAL
 *   surrogateElement [9] IMPLICIT TagPath OPTIONAL
 *   other [99] IMPLICIT EXTERNAL OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class ElementMetaData extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a ElementMetaData.
     */

    public ElementMetaData() {
    }



    /**
     * Constructor for a ElementMetaData from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ElementMetaData(BEREncoding ber, boolean check_tag)
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
        // ElementMetaData should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun ElementMetaData: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_seriesOrder = null;
        s_usageRight = null;
        s_hits = null;
        s_displayName = null;
        s_supportedVariants = null;
        s_message = null;
        s_elementDescriptor = null;
        s_surrogateFor = null;
        s_surrogateElement = null;
        s_other = null;

        // Decoding: seriesOrder [1] IMPLICIT Order OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_seriesOrder = new Order(p, false);
            part++;
        }

        // Decoding: usageRight [2] IMPLICIT Usage OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_usageRight = new Usage(p, false);
            part++;
        }

        // Decoding: hits [3] IMPLICIT SEQUENCE OF HitVector OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_hits = new HitVector[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_hits[n] = new HitVector(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: displayName [4] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_displayName = new InternationalString(p, false);
            part++;
        }

        // Decoding: supportedVariants [5] IMPLICIT SEQUENCE OF Variant OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_supportedVariants = new Variant[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_supportedVariants[n] = new Variant(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: message [6] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_message = new InternationalString(p, false);
            part++;
        }

        // Decoding: elementDescriptor [7] IMPLICIT OCTET STRING OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 7 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_elementDescriptor = new ASN1OctetString(p, false);
            part++;
        }

        // Decoding: surrogateFor [8] IMPLICIT TagPath OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 8 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_surrogateFor = new TagPath(p, false);
            part++;
        }

        // Decoding: surrogateElement [9] IMPLICIT TagPath OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 9 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_surrogateElement = new TagPath(p, false);
            part++;
        }

        // Decoding: other [99] IMPLICIT EXTERNAL OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 99 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_other = new ASN1External(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun ElementMetaData: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the ElementMetaData.
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
     * Returns a BER encoding of ElementMetaData, implicitly tagged.
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

        int num_fields = 0; // number of mandatories
        if (s_seriesOrder != null) {
            num_fields++;
        }
        if (s_usageRight != null) {
            num_fields++;
        }
        if (s_hits != null) {
            num_fields++;
        }
        if (s_displayName != null) {
            num_fields++;
        }
        if (s_supportedVariants != null) {
            num_fields++;
        }
        if (s_message != null) {
            num_fields++;
        }
        if (s_elementDescriptor != null) {
            num_fields++;
        }
        if (s_surrogateFor != null) {
            num_fields++;
        }
        if (s_surrogateElement != null) {
            num_fields++;
        }
        if (s_other != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_seriesOrder: Order OPTIONAL

        if (s_seriesOrder != null) {
            fields[x++] = s_seriesOrder.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding s_usageRight: Usage OPTIONAL

        if (s_usageRight != null) {
            fields[x++] = s_usageRight.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_hits: SEQUENCE OF OPTIONAL

        if (s_hits != null) {
            f2 = new BEREncoding[s_hits.length];

            for (p = 0; p < s_hits.length; p++) {
                f2[p] = s_hits[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, f2);
        }

        // Encoding s_displayName: InternationalString OPTIONAL

        if (s_displayName != null) {
            fields[x++] = s_displayName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_supportedVariants: SEQUENCE OF OPTIONAL

        if (s_supportedVariants != null) {
            f2 = new BEREncoding[s_supportedVariants.length];

            for (p = 0; p < s_supportedVariants.length; p++) {
                f2[p] = s_supportedVariants[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, f2);
        }

        // Encoding s_message: InternationalString OPTIONAL

        if (s_message != null) {
            fields[x++] = s_message.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        // Encoding s_elementDescriptor: OCTET STRING OPTIONAL

        if (s_elementDescriptor != null) {
            fields[x++] = s_elementDescriptor.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Encoding s_surrogateFor: TagPath OPTIONAL

        if (s_surrogateFor != null) {
            fields[x++] = s_surrogateFor.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
        }

        // Encoding s_surrogateElement: TagPath OPTIONAL

        if (s_surrogateElement != null) {
            fields[x++] = s_surrogateElement.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);
        }

        // Encoding s_other: EXTERNAL OPTIONAL

        if (s_other != null) {
            fields[x++] = s_other.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 99);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the ElementMetaData.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_seriesOrder != null) {
            str.append("seriesOrder ");
            str.append(s_seriesOrder);
            outputted++;
        }

        if (s_usageRight != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("usageRight ");
            str.append(s_usageRight);
            outputted++;
        }

        if (s_hits != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("hits ");
            str.append("{");
            for (p = 0; p < s_hits.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_hits[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_displayName != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("displayName ");
            str.append(s_displayName);
            outputted++;
        }

        if (s_supportedVariants != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("supportedVariants ");
            str.append("{");
            for (p = 0; p < s_supportedVariants.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_supportedVariants[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_message != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("message ");
            str.append(s_message);
            outputted++;
        }

        if (s_elementDescriptor != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("elementDescriptor ");
            str.append(s_elementDescriptor);
            outputted++;
        }

        if (s_surrogateFor != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("surrogateFor ");
            str.append(s_surrogateFor);
            outputted++;
        }

        if (s_surrogateElement != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("surrogateElement ");
            str.append(s_surrogateElement);
            outputted++;
        }

        if (s_other != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("other ");
            str.append(s_other);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public Order s_seriesOrder; // optional
    public Usage s_usageRight; // optional
    public HitVector s_hits[]; // optional
    public InternationalString s_displayName; // optional
    public Variant s_supportedVariants[]; // optional
    public InternationalString s_message; // optional
    public ASN1OctetString s_elementDescriptor; // optional
    public TagPath s_surrogateFor; // optional
    public TagPath s_surrogateElement; // optional
    public ASN1External s_other; // optional

} // ElementMetaData


//EOF
