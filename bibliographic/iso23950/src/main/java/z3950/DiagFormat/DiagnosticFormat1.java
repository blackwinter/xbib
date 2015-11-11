package z3950.DiagFormat;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;



/**
 * Class for representing a <code>DiagnosticFormat1</code> from <code>DiagnosticFormatDiag1</code>
 * <p/>
 * <pre>
 * DiagnosticFormat1 ::=
 * SEQUENCE {
 *   diagnostic [1] EXPLICIT DiagnosticFormat_diagnostic OPTIONAL
 *   message [2] IMPLICIT InternationalString OPTIONAL
 * }
 * </pre>
 *
 */



public final class DiagnosticFormat1 extends ASN1Any {


    /**
     * Default constructor for a DiagnosticFormat1.
     */

    public DiagnosticFormat1() {
    }



    /**
     * Constructor for a DiagnosticFormat1 from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DiagnosticFormat1(BEREncoding ber, boolean check_tag)
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
        // DiagnosticFormat1 should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun DiagnosticFormat1: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_diagnostic = null;
        s_message = null;

        // Decoding: diagnostic [1] EXPLICIT DiagnosticFormat_diagnostic OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("DiagnosticFormat1: bad BER encoding: s_diagnostic tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("DiagnosticFormat1: bad BER encoding: s_diagnostic tag bad\n");
            }

            s_diagnostic = new DiagnosticFormat_diagnostic(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: message [2] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_message = new InternationalString(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("DiagnosticFormat1: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the DiagnosticFormat1.
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
     * Returns a BER encoding of DiagnosticFormat1, implicitly tagged.
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
        if (s_diagnostic != null) {
            num_fields++;
        }
        if (s_message != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_diagnostic: DiagnosticFormat_diagnostic OPTIONAL

        if (s_diagnostic != null) {
            enc = new BEREncoding[1];
            enc[0] = s_diagnostic.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);
        }

        // Encoding s_message: InternationalString OPTIONAL

        if (s_message != null) {
            fields[x++] = s_message.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the DiagnosticFormat1.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_diagnostic != null) {
            str.append("diagnostic ");
            str.append(s_diagnostic);
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

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public DiagnosticFormat_diagnostic s_diagnostic; // optional
    public InternationalString s_message; // optional

}
