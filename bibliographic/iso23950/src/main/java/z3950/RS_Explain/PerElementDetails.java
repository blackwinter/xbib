package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1Boolean;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;

/**
 * Class for representing a <code>PerElementDetails</code> from <code>RecordSyntax-explain</code>
 * <pre>
 * PerElementDetails ::=
 * SEQUENCE {
 *   name [0] IMPLICIT InternationalString OPTIONAL
 *   recordTag [1] IMPLICIT RecordTag OPTIONAL
 *   schemaTags [2] IMPLICIT SEQUENCE OF Path OPTIONAL
 *   maxSize [3] IMPLICIT INTEGER OPTIONAL
 *   minSize [4] IMPLICIT INTEGER OPTIONAL
 *   avgSize [5] IMPLICIT INTEGER OPTIONAL
 *   fixedSize [6] IMPLICIT INTEGER OPTIONAL
 *   repeatable [8] IMPLICIT BOOLEAN
 *   required [9] IMPLICIT BOOLEAN
 *   description [12] IMPLICIT HumanString OPTIONAL
 *   contents [13] IMPLICIT HumanString OPTIONAL
 *   billingInfo [14] IMPLICIT HumanString OPTIONAL
 *   restrictions [15] IMPLICIT HumanString OPTIONAL
 *   alternateNames [16] IMPLICIT SEQUENCE OF InternationalString OPTIONAL
 *   genericNames [17] IMPLICIT SEQUENCE OF InternationalString OPTIONAL
 *   searchAccess [18] IMPLICIT AttributeCombinations OPTIONAL
 * }
 * </pre>
 *
 */



public final class PerElementDetails extends ASN1Any {

    /**
     * Default constructor for a PerElementDetails.
     */

    public PerElementDetails() {
    }



    /**
     * Constructor for a PerElementDetails from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public PerElementDetails(BEREncoding ber, boolean check_tag)
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
        // PerElementDetails should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("PerElementDetails: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: name [0] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("PerElementDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_name = new InternationalString(p, false);
            part++;
        }

        // Decoding: recordTag [1] IMPLICIT RecordTag OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("PerElementDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_recordTag = new RecordTag(p, false);
            part++;
        }

        // Decoding: schemaTags [2] IMPLICIT SEQUENCE OF Path OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("PerElementDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_schemaTags = new Path[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_schemaTags[n] = new Path(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: maxSize [3] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("PerElementDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_maxSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: minSize [4] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("PerElementDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_minSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: avgSize [5] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("PerElementDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_avgSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: fixedSize [6] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("PerElementDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_fixedSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: repeatable [8] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("PerElementDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 8 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("PerElementDetails: bad tag in s_repeatable\n");
        }

        s_repeatable = new ASN1Boolean(p, false);
        part++;

        // Decoding: required [9] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("PerElementDetails: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 9 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("PerElementDetails: bad tag in s_required\n");
        }

        s_required = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_description = null;
        s_contents = null;
        s_billingInfo = null;
        s_restrictions = null;
        s_alternateNames = null;
        s_genericNames = null;
        s_searchAccess = null;

        // Decoding: description [12] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 12 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_description = new HumanString(p, false);
            part++;
        }

        // Decoding: contents [13] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 13 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_contents = new HumanString(p, false);
            part++;
        }

        // Decoding: billingInfo [14] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 14 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_billingInfo = new HumanString(p, false);
            part++;
        }

        // Decoding: restrictions [15] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 15 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_restrictions = new HumanString(p, false);
            part++;
        }

        // Decoding: alternateNames [16] IMPLICIT SEQUENCE OF InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 16 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_alternateNames = new InternationalString[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_alternateNames[n] = new InternationalString(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: genericNames [17] IMPLICIT SEQUENCE OF InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 17 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_genericNames = new InternationalString[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_genericNames[n] = new InternationalString(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: searchAccess [18] IMPLICIT AttributeCombinations OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 18 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_searchAccess = new AttributeCombinations(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("PerElementDetails: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the PerElementDetails.
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
     * Returns a BER encoding of PerElementDetails, implicitly tagged.
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

        int num_fields = 2; // number of mandatories
        if (s_name != null) {
            num_fields++;
        }
        if (s_recordTag != null) {
            num_fields++;
        }
        if (s_schemaTags != null) {
            num_fields++;
        }
        if (s_maxSize != null) {
            num_fields++;
        }
        if (s_minSize != null) {
            num_fields++;
        }
        if (s_avgSize != null) {
            num_fields++;
        }
        if (s_fixedSize != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }
        if (s_contents != null) {
            num_fields++;
        }
        if (s_billingInfo != null) {
            num_fields++;
        }
        if (s_restrictions != null) {
            num_fields++;
        }
        if (s_alternateNames != null) {
            num_fields++;
        }
        if (s_genericNames != null) {
            num_fields++;
        }
        if (s_searchAccess != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_name: InternationalString OPTIONAL

        if (s_name != null) {
            fields[x++] = s_name.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_recordTag: RecordTag OPTIONAL

        if (s_recordTag != null) {
            fields[x++] = s_recordTag.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding s_schemaTags: SEQUENCE OF OPTIONAL

        if (s_schemaTags != null) {
            f2 = new BEREncoding[s_schemaTags.length];

            for (p = 0; p < s_schemaTags.length; p++) {
                f2[p] = s_schemaTags[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
        }

        // Encoding s_maxSize: INTEGER OPTIONAL

        if (s_maxSize != null) {
            fields[x++] = s_maxSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding s_minSize: INTEGER OPTIONAL

        if (s_minSize != null) {
            fields[x++] = s_minSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_avgSize: INTEGER OPTIONAL

        if (s_avgSize != null) {
            fields[x++] = s_avgSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding s_fixedSize: INTEGER OPTIONAL

        if (s_fixedSize != null) {
            fields[x++] = s_fixedSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        // Encoding s_repeatable: BOOLEAN

        fields[x++] = s_repeatable.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);

        // Encoding s_required: BOOLEAN

        fields[x++] = s_required.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);

        // Encoding s_description: HumanString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 12);
        }

        // Encoding s_contents: HumanString OPTIONAL

        if (s_contents != null) {
            fields[x++] = s_contents.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 13);
        }

        // Encoding s_billingInfo: HumanString OPTIONAL

        if (s_billingInfo != null) {
            fields[x++] = s_billingInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 14);
        }

        // Encoding s_restrictions: HumanString OPTIONAL

        if (s_restrictions != null) {
            fields[x++] = s_restrictions.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 15);
        }

        // Encoding s_alternateNames: SEQUENCE OF OPTIONAL

        if (s_alternateNames != null) {
            f2 = new BEREncoding[s_alternateNames.length];

            for (p = 0; p < s_alternateNames.length; p++) {
                f2[p] = s_alternateNames[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 16, f2);
        }

        // Encoding s_genericNames: SEQUENCE OF OPTIONAL

        if (s_genericNames != null) {
            f2 = new BEREncoding[s_genericNames.length];

            for (p = 0; p < s_genericNames.length; p++) {
                f2[p] = s_genericNames[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 17, f2);
        }

        // Encoding s_searchAccess: AttributeCombinations OPTIONAL

        if (s_searchAccess != null) {
            fields[x++] = s_searchAccess.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 18);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the PerElementDetails.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_name != null) {
            str.append("name ");
            str.append(s_name);
            outputted++;
        }

        if (s_recordTag != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("recordTag ");
            str.append(s_recordTag);
            outputted++;
        }

        if (s_schemaTags != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("schemaTags ");
            str.append("{");
            for (p = 0; p < s_schemaTags.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_schemaTags[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_maxSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("maxSize ");
            str.append(s_maxSize);
            outputted++;
        }

        if (s_minSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("minSize ");
            str.append(s_minSize);
            outputted++;
        }

        if (s_avgSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("avgSize ");
            str.append(s_avgSize);
            outputted++;
        }

        if (s_fixedSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("fixedSize ");
            str.append(s_fixedSize);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("repeatable ");
        str.append(s_repeatable);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("required ");
        str.append(s_required);
        outputted++;

        if (s_description != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("description ");
            str.append(s_description);
            outputted++;
        }

        if (s_contents != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("contents ");
            str.append(s_contents);
            outputted++;
        }

        if (s_billingInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("billingInfo ");
            str.append(s_billingInfo);
            outputted++;
        }

        if (s_restrictions != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("restrictions ");
            str.append(s_restrictions);
            outputted++;
        }

        if (s_alternateNames != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("alternateNames ");
            str.append("{");
            for (p = 0; p < s_alternateNames.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_alternateNames[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_genericNames != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("genericNames ");
            str.append("{");
            for (p = 0; p < s_genericNames.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_genericNames[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_searchAccess != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("searchAccess ");
            str.append(s_searchAccess);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public InternationalString s_name; // optional
    public RecordTag s_recordTag; // optional
    public Path s_schemaTags[]; // optional
    public ASN1Integer s_maxSize; // optional
    public ASN1Integer s_minSize; // optional
    public ASN1Integer s_avgSize; // optional
    public ASN1Integer s_fixedSize; // optional
    public ASN1Boolean s_repeatable;
    public ASN1Boolean s_required;
    public HumanString s_description; // optional
    public HumanString s_contents; // optional
    public HumanString s_billingInfo; // optional
    public HumanString s_restrictions; // optional
    public InternationalString s_alternateNames[]; // optional
    public InternationalString s_genericNames[]; // optional
    public AttributeCombinations s_searchAccess; // optional

}