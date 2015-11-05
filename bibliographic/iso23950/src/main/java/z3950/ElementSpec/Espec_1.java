package z3950.ElementSpec;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.RS_generic.Variant;
import z3950.v3.InternationalString;



/**
 * Class for representing a <code>Espec_1</code> from <code>ElementSpecificationFormat-eSpec-1</code>
 * <pre>
 * Espec_1 ::=
 * SEQUENCE {
 *   elementSetNames [1] IMPLICIT SEQUENCE OF InternationalString OPTIONAL
 *   defaultVariantSetId [2] IMPLICIT OBJECT IDENTIFIER OPTIONAL
 *   defaultVariantRequest [3] IMPLICIT Variant OPTIONAL
 *   defaultTagType [4] IMPLICIT INTEGER OPTIONAL
 *   elements [5] IMPLICIT SEQUENCE OF ElementRequest OPTIONAL
 * }
 * </pre>
 *
 */



public final class Espec_1 extends ASN1Any {

    /**
     * Default constructor for a Espec_1.
     */

    public Espec_1() {
    }



    /**
     * Constructor for a Espec_1 from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Espec_1(BEREncoding ber, boolean check_tag)
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
        // Espec_1 should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Espec_1: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_elementSetNames = null;
        s_defaultVariantSetId = null;
        s_defaultVariantRequest = null;
        s_defaultTagType = null;
        s_elements = null;

        // Decoding: elementSetNames [1] IMPLICIT SEQUENCE OF InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_elementSetNames = new InternationalString[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_elementSetNames[n] = new InternationalString(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: defaultVariantSetId [2] IMPLICIT OBJECT IDENTIFIER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_defaultVariantSetId = new ASN1ObjectIdentifier(p, false);
            part++;
        }

        // Decoding: defaultVariantRequest [3] IMPLICIT Variant OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_defaultVariantRequest = new Variant(p, false);
            part++;
        }

        // Decoding: defaultTagType [4] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_defaultTagType = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: elements [5] IMPLICIT SEQUENCE OF ElementRequest OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_elements = new ElementRequest[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_elements[n] = new ElementRequest(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Espec_1: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the Espec_1.
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
     * Returns a BER encoding of Espec_1, implicitly tagged.
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
        if (s_elementSetNames != null) {
            num_fields++;
        }
        if (s_defaultVariantSetId != null) {
            num_fields++;
        }
        if (s_defaultVariantRequest != null) {
            num_fields++;
        }
        if (s_defaultTagType != null) {
            num_fields++;
        }
        if (s_elements != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_elementSetNames: SEQUENCE OF OPTIONAL

        if (s_elementSetNames != null) {
            f2 = new BEREncoding[s_elementSetNames.length];

            for (p = 0; p < s_elementSetNames.length; p++) {
                f2[p] = s_elementSetNames[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, f2);
        }

        // Encoding s_defaultVariantSetId: OBJECT IDENTIFIER OPTIONAL

        if (s_defaultVariantSetId != null) {
            fields[x++] = s_defaultVariantSetId.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_defaultVariantRequest: Variant OPTIONAL

        if (s_defaultVariantRequest != null) {
            fields[x++] = s_defaultVariantRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding s_defaultTagType: INTEGER OPTIONAL

        if (s_defaultTagType != null) {
            fields[x++] = s_defaultTagType.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_elements: SEQUENCE OF OPTIONAL

        if (s_elements != null) {
            f2 = new BEREncoding[s_elements.length];

            for (p = 0; p < s_elements.length; p++) {
                f2[p] = s_elements[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, f2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the Espec_1.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_elementSetNames != null) {
            str.append("elementSetNames ");
            str.append("{");
            for (p = 0; p < s_elementSetNames.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_elementSetNames[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_defaultVariantSetId != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("defaultVariantSetId ");
            str.append(s_defaultVariantSetId);
            outputted++;
        }

        if (s_defaultVariantRequest != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("defaultVariantRequest ");
            str.append(s_defaultVariantRequest);
            outputted++;
        }

        if (s_defaultTagType != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("defaultTagType ");
            str.append(s_defaultTagType);
            outputted++;
        }

        if (s_elements != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("elements ");
            str.append("{");
            for (p = 0; p < s_elements.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_elements[p]);
            }
            str.append("}");
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public InternationalString s_elementSetNames[]; // optional
    public ASN1ObjectIdentifier s_defaultVariantSetId; // optional
    public Variant s_defaultVariantRequest; // optional
    public ASN1Integer s_defaultTagType; // optional
    public ElementRequest s_elements[]; // optional

} // Espec_1


//EOF
