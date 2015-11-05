package z3950.DiagFormat;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Null;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.AttributeList;
import z3950.v3.InternationalString;



/**
 * Class for representing a <code>DiagFormat_proximity</code> from <code>DiagnosticFormatDiag1</code>
 * <p/>
 * <pre>
 * DiagFormat_proximity ::=
 * CHOICE {
 *   resultSets [1] IMPLICIT NULL
 *   badSet [2] IMPLICIT InternationalString
 *   relation [3] IMPLICIT INTEGER
 *   unit [4] IMPLICIT INTEGER
 *   distance [5] IMPLICIT INTEGER
 *   attributes [6] EXPLICIT AttributeList
 *   ordered [7] IMPLICIT NULL
 *   exclusion [8] IMPLICIT NULL
 * }
 * </pre>
 *
 */



public final class DiagFormat_proximity extends ASN1Any {



    /**
     * Default constructor for a DiagFormat_proximity.
     */

    public DiagFormat_proximity() {
    }



    /**
     * Constructor for a DiagFormat_proximity from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DiagFormat_proximity(BEREncoding ber, boolean check_tag)
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

        c_resultSets = null;
        c_badSet = null;
        c_relation = null;
        c_unit = null;
        c_distance = null;
        c_attributes = null;
        c_ordered = null;
        c_exclusion = null;

        // Try choice resultSets
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_resultSets = new ASN1Null(ber, false);
            return;
        }

        // Try choice badSet
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_badSet = new InternationalString(ber, false);
            return;
        }

        // Try choice relation
        if (ber.tag_get() == 3 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_relation = new ASN1Integer(ber, false);
            return;
        }

        // Try choice unit
        if (ber.tag_get() == 4 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_unit = new ASN1Integer(ber, false);
            return;
        }

        // Try choice distance
        if (ber.tag_get() == 5 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_distance = new ASN1Integer(ber, false);
            return;
        }

        // Try choice attributes
        if (ber.tag_get() == 6 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun DiagFormat_proximity: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun DiagFormat_proximity: bad BER form\n");
            }
            c_attributes = new AttributeList(tagwrapper.elementAt(0), true);
            return;
        }

        // Try choice ordered
        if (ber.tag_get() == 7 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_ordered = new ASN1Null(ber, false);
            return;
        }

        // Try choice exclusion
        if (ber.tag_get() == 8 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_exclusion = new ASN1Null(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun DiagFormat_proximity: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of DiagFormat_proximity.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        BEREncoding enc[];

        // Encoding choice: c_resultSets
        if (c_resultSets != null) {
            chosen = c_resultSets.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_badSet
        if (c_badSet != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_badSet.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding choice: c_relation
        if (c_relation != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_relation.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding choice: c_unit
        if (c_unit != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_unit.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding choice: c_distance
        if (c_distance != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_distance.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding choice: c_attributes
        if (c_attributes != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_attributes.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 6, enc);
        }

        // Encoding choice: c_ordered
        if (c_ordered != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_ordered.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Encoding choice: c_exclusion
        if (c_exclusion != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_exclusion.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
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

        throw new ASN1EncodingException("Zebulun DiagFormat_proximity: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the DiagFormat_proximity.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_resultSets != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: resultSets> ");
            }
            found = true;
            str.append("resultSets ");
            str.append(c_resultSets);
        }

        if (c_badSet != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: badSet> ");
            }
            found = true;
            str.append("badSet ");
            str.append(c_badSet);
        }

        if (c_relation != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: relation> ");
            }
            found = true;
            str.append("relation ");
            str.append(c_relation);
        }

        if (c_unit != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: unit> ");
            }
            found = true;
            str.append("unit ");
            str.append(c_unit);
        }

        if (c_distance != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: distance> ");
            }
            found = true;
            str.append("distance ");
            str.append(c_distance);
        }

        if (c_attributes != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: attributes> ");
            }
            found = true;
            str.append("attributes ");
            str.append(c_attributes);
        }

        if (c_ordered != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: ordered> ");
            }
            found = true;
            str.append("ordered ");
            str.append(c_ordered);
        }

        if (c_exclusion != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: exclusion> ");
            }
            found = true;
            str.append("exclusion ");
            str.append(c_exclusion);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Null c_resultSets;
    public InternationalString c_badSet;
    public ASN1Integer c_relation;
    public ASN1Integer c_unit;
    public ASN1Integer c_distance;
    public AttributeList c_attributes;
    public ASN1Null c_ordered;
    public ASN1Null c_exclusion;

} // DiagFormat_proximity


//EOF
