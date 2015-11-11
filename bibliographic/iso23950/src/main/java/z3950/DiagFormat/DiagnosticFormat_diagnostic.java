package z3950.DiagFormat;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.DefaultDiagFormat;



/**
 * Class for representing a <code>DiagnosticFormat_diagnostic</code> from <code>DiagnosticFormatDiag1</code>
 * <p/>
 * <pre>
 * DiagnosticFormat_diagnostic ::=
 * CHOICE {
 *   defaultDiagRec [1] IMPLICIT DefaultDiagFormat
 *   explicitDiagnostic [2] EXPLICIT DiagFormat
 * }
 * </pre>
 *
 */
public final class DiagnosticFormat_diagnostic extends ASN1Any {


    /**
     * Default constructor for a DiagnosticFormat_diagnostic.
     */

    public DiagnosticFormat_diagnostic() {
    }



    /**
     * Constructor for a DiagnosticFormat_diagnostic from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DiagnosticFormat_diagnostic(BEREncoding ber, boolean check_tag)
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
        BERConstructed tagwrapper;

        // Null out all choices

        c_defaultDiagRec = null;
        c_explicitDiagnostic = null;

        // Try choice defaultDiagRec
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_defaultDiagRec = new DefaultDiagFormat(ber, false);
            return;
        }

        // Try choice explicitDiagnostic
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun DiagnosticFormat_diagnostic: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun DiagnosticFormat_diagnostic: bad BER form\n");
            }
            c_explicitDiagnostic = new DiagFormat(tagwrapper.elementAt(0), true);
            return;
        }

        throw new ASN1Exception("Zebulun DiagnosticFormat_diagnostic: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of DiagnosticFormat_diagnostic.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        BEREncoding enc[];

        // Encoding choice: c_defaultDiagRec
        if (c_defaultDiagRec != null) {
            chosen = c_defaultDiagRec.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_explicitDiagnostic
        if (c_explicitDiagnostic != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_explicitDiagnostic.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);
        }

        // Check for error of having none of the choices set
        if (chosen == null) {
            throw new ASN1Exception("CHOICE not set");
        }

        return chosen;
    }



    /**
     * Generating a BER encoding of the object
     * and implicitly tagging it.
     * <p/>
     * This method is for internal use only. You should use
     * the ber_encode method that does not take a parameter.
     * <p/>
     * This function should never be used, because this
     * production is a CHOICE.
     * It must never have an implicit tag.
     * <p/>
     * An exception will be thrown if it is called.
     *
     * @param tag_type the type of the tag.
     * @param tag      the tag.
     * @throws ASN1Exception if it cannot be BER encoded.
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // This method must not be called!

        // Method is not available because this is a basic CHOICE
        // which does not have an explicit tag on it. So it is not
        // permitted to allow something else to apply an implicit
        // tag on it, otherwise the tag identifying which CHOICE
        // it is will be overwritten and lost.

        throw new ASN1EncodingException("DiagnosticFormat_diagnostic: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the DiagnosticFormat_diagnostic.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_defaultDiagRec != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: defaultDiagRec> ");
            }
            found = true;
            str.append("defaultDiagRec ");
            str.append(c_defaultDiagRec);
        }

        if (c_explicitDiagnostic != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: explicitDiagnostic> ");
            }
            found = true;
            str.append("explicitDiagnostic ");
            str.append(c_explicitDiagnostic);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public DefaultDiagFormat c_defaultDiagRec;
    public DiagFormat c_explicitDiagnostic;

}