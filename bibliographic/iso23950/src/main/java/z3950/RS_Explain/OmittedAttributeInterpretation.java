package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.StringOrNumeric;

/**
 * Class for representing a <code>OmittedAttributeInterpretation</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * OmittedAttributeInterpretation ::=
 * SEQUENCE {
 *   defaultValue [0] EXPLICIT StringOrNumeric OPTIONAL
 *   defaultDescription [1] IMPLICIT HumanString OPTIONAL
 * }
 * </pre>
 *
 */



public final class OmittedAttributeInterpretation extends ASN1Any {

    /**
     * Default constructor for a OmittedAttributeInterpretation.
     */
    public OmittedAttributeInterpretation() {
    }
    /**
     * Constructor for a OmittedAttributeInterpretation from a BER encoding.
     *
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public OmittedAttributeInterpretation(BEREncoding ber, boolean check_tag)
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
        // OmittedAttributeInterpretation should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("OmittedAttributeInterpretation: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_defaultValue = null;
        s_defaultDescription = null;

        // Decoding: defaultValue [0] EXPLICIT StringOrNumeric OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("OmittedAttributeInterpretation: bad BER encoding: s_defaultValue tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("OmittedAttributeInterpretation: bad BER encoding: s_defaultValue tag bad\n");
            }

            s_defaultValue = new StringOrNumeric(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: defaultDescription [1] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_defaultDescription = new HumanString(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("OmittedAttributeInterpretation: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the OmittedAttributeInterpretation.
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
     * Returns a BER encoding of OmittedAttributeInterpretation, implicitly tagged.
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
        if (s_defaultValue != null) {
            num_fields++;
        }
        if (s_defaultDescription != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_defaultValue: StringOrNumeric OPTIONAL

        if (s_defaultValue != null) {
            enc = new BEREncoding[1];
            enc[0] = s_defaultValue.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 0, enc);
        }

        // Encoding s_defaultDescription: HumanString OPTIONAL

        if (s_defaultDescription != null) {
            fields[x++] = s_defaultDescription.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the OmittedAttributeInterpretation.
     */

    public String
    toString() {
        StringBuilder str = new StringBuilder("{");
        int outputted = 0;

        if (s_defaultValue != null) {
            str.append("defaultValue ");
            str.append(s_defaultValue);
            outputted++;
        }

        if (s_defaultDescription != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("defaultDescription ");
            str.append(s_defaultDescription);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public StringOrNumeric s_defaultValue; // optional
    public HumanString s_defaultDescription; // optional

}