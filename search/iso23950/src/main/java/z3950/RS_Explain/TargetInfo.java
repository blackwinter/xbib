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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:21 UTC
 */



package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1Boolean;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.IntUnit;
import z3950.v3.InternationalString;



/**
 * Class for representing a <code>TargetInfo</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * TargetInfo ::=
 * SEQUENCE {
 *   commonInfo [0] IMPLICIT CommonInfo OPTIONAL
 *   name [1] IMPLICIT InternationalString
 *   recent-news [2] IMPLICIT HumanString OPTIONAL
 *   icon [3] IMPLICIT IconObject OPTIONAL
 *   namedResultSets [4] IMPLICIT BOOLEAN
 *   multipleDBsearch [5] IMPLICIT BOOLEAN
 *   maxResultSets [6] IMPLICIT INTEGER OPTIONAL
 *   maxResultSize [7] IMPLICIT INTEGER OPTIONAL
 *   maxTerms [8] IMPLICIT INTEGER OPTIONAL
 *   timeoutInterval [9] IMPLICIT IntUnit OPTIONAL
 *   welcomeMessage [10] IMPLICIT HumanString OPTIONAL
 *   contactInfo [11] IMPLICIT ContactInfo OPTIONAL
 *   description [12] IMPLICIT HumanString OPTIONAL
 *   nicknames [13] IMPLICIT SEQUENCE OF InternationalString OPTIONAL
 *   usage-restrictions [14] IMPLICIT HumanString OPTIONAL
 *   paymentAddr [15] IMPLICIT HumanString OPTIONAL
 *   hours [16] IMPLICIT HumanString OPTIONAL
 *   dbCombinations [17] IMPLICIT SEQUENCE OF DatabaseList OPTIONAL
 *   addresses [18] IMPLICIT SEQUENCE OF NetworkAddress OPTIONAL
 *   languages [101] IMPLICIT SEQUENCE OF InternationalString OPTIONAL
 *   commonAccessInfo [19] IMPLICIT AccessInfo OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class TargetInfo extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a TargetInfo.
     */

    public TargetInfo() {
    }



    /**
     * Constructor for a TargetInfo from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public TargetInfo(BEREncoding ber, boolean check_tag)
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
        // TargetInfo should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun TargetInfo: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: commonInfo [0] IMPLICIT CommonInfo OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TargetInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_commonInfo = new CommonInfo(p, false);
            part++;
        }

        // Decoding: name [1] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TargetInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun TargetInfo: bad tag in s_name\n");
        }

        s_name = new InternationalString(p, false);
        part++;

        // Decoding: recent-news [2] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TargetInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_recent_news = new HumanString(p, false);
            part++;
        }

        // Decoding: icon [3] IMPLICIT IconObject OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TargetInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_icon = new IconObject(p, false);
            part++;
        }

        // Decoding: namedResultSets [4] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TargetInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 4 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun TargetInfo: bad tag in s_namedResultSets\n");
        }

        s_namedResultSets = new ASN1Boolean(p, false);
        part++;

        // Decoding: multipleDBsearch [5] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TargetInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 5 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun TargetInfo: bad tag in s_multipleDBsearch\n");
        }

        s_multipleDBsearch = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_maxResultSets = null;
        s_maxResultSize = null;
        s_maxTerms = null;
        s_timeoutInterval = null;
        s_welcomeMessage = null;
        s_contactInfo = null;
        s_description = null;
        s_nicknames = null;
        s_usage_restrictions = null;
        s_paymentAddr = null;
        s_hours = null;
        s_dbCombinations = null;
        s_addresses = null;
        s_languages = null;
        s_commonAccessInfo = null;

        // Decoding: maxResultSets [6] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_maxResultSets = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: maxResultSize [7] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 7 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_maxResultSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: maxTerms [8] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 8 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_maxTerms = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: timeoutInterval [9] IMPLICIT IntUnit OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 9 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_timeoutInterval = new IntUnit(p, false);
            part++;
        }

        // Decoding: welcomeMessage [10] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 10 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_welcomeMessage = new HumanString(p, false);
            part++;
        }

        // Decoding: contactInfo [11] IMPLICIT ContactInfo OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 11 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_contactInfo = new ContactInfo(p, false);
            part++;
        }

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

        // Decoding: nicknames [13] IMPLICIT SEQUENCE OF InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 13 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_nicknames = new InternationalString[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_nicknames[n] = new InternationalString(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: usage-restrictions [14] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 14 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_usage_restrictions = new HumanString(p, false);
            part++;
        }

        // Decoding: paymentAddr [15] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 15 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_paymentAddr = new HumanString(p, false);
            part++;
        }

        // Decoding: hours [16] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 16 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_hours = new HumanString(p, false);
            part++;
        }

        // Decoding: dbCombinations [17] IMPLICIT SEQUENCE OF DatabaseList OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 17 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_dbCombinations = new DatabaseList[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_dbCombinations[n] = new DatabaseList(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: addresses [18] IMPLICIT SEQUENCE OF NetworkAddress OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 18 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_addresses = new NetworkAddress[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_addresses[n] = new NetworkAddress(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: languages [101] IMPLICIT SEQUENCE OF InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 101 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_languages = new InternationalString[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_languages[n] = new InternationalString(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: commonAccessInfo [19] IMPLICIT AccessInfo OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 19 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_commonAccessInfo = new AccessInfo(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun TargetInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the TargetInfo.
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
     * Returns a BER encoding of TargetInfo, implicitly tagged.
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

        int num_fields = 3; // number of mandatories
        if (s_commonInfo != null) {
            num_fields++;
        }
        if (s_recent_news != null) {
            num_fields++;
        }
        if (s_icon != null) {
            num_fields++;
        }
        if (s_maxResultSets != null) {
            num_fields++;
        }
        if (s_maxResultSize != null) {
            num_fields++;
        }
        if (s_maxTerms != null) {
            num_fields++;
        }
        if (s_timeoutInterval != null) {
            num_fields++;
        }
        if (s_welcomeMessage != null) {
            num_fields++;
        }
        if (s_contactInfo != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }
        if (s_nicknames != null) {
            num_fields++;
        }
        if (s_usage_restrictions != null) {
            num_fields++;
        }
        if (s_paymentAddr != null) {
            num_fields++;
        }
        if (s_hours != null) {
            num_fields++;
        }
        if (s_dbCombinations != null) {
            num_fields++;
        }
        if (s_addresses != null) {
            num_fields++;
        }
        if (s_languages != null) {
            num_fields++;
        }
        if (s_commonAccessInfo != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_commonInfo: CommonInfo OPTIONAL

        if (s_commonInfo != null) {
            fields[x++] = s_commonInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_name: InternationalString

        fields[x++] = s_name.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_recent_news: HumanString OPTIONAL

        if (s_recent_news != null) {
            fields[x++] = s_recent_news.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_icon: IconObject OPTIONAL

        if (s_icon != null) {
            fields[x++] = s_icon.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding s_namedResultSets: BOOLEAN

        fields[x++] = s_namedResultSets.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);

        // Encoding s_multipleDBsearch: BOOLEAN

        fields[x++] = s_multipleDBsearch.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);

        // Encoding s_maxResultSets: INTEGER OPTIONAL

        if (s_maxResultSets != null) {
            fields[x++] = s_maxResultSets.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        // Encoding s_maxResultSize: INTEGER OPTIONAL

        if (s_maxResultSize != null) {
            fields[x++] = s_maxResultSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Encoding s_maxTerms: INTEGER OPTIONAL

        if (s_maxTerms != null) {
            fields[x++] = s_maxTerms.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
        }

        // Encoding s_timeoutInterval: IntUnit OPTIONAL

        if (s_timeoutInterval != null) {
            fields[x++] = s_timeoutInterval.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);
        }

        // Encoding s_welcomeMessage: HumanString OPTIONAL

        if (s_welcomeMessage != null) {
            fields[x++] = s_welcomeMessage.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 10);
        }

        // Encoding s_contactInfo: ContactInfo OPTIONAL

        if (s_contactInfo != null) {
            fields[x++] = s_contactInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 11);
        }

        // Encoding s_description: HumanString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 12);
        }

        // Encoding s_nicknames: SEQUENCE OF OPTIONAL

        if (s_nicknames != null) {
            f2 = new BEREncoding[s_nicknames.length];

            for (p = 0; p < s_nicknames.length; p++) {
                f2[p] = s_nicknames[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 13, f2);
        }

        // Encoding s_usage_restrictions: HumanString OPTIONAL

        if (s_usage_restrictions != null) {
            fields[x++] = s_usage_restrictions.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 14);
        }

        // Encoding s_paymentAddr: HumanString OPTIONAL

        if (s_paymentAddr != null) {
            fields[x++] = s_paymentAddr.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 15);
        }

        // Encoding s_hours: HumanString OPTIONAL

        if (s_hours != null) {
            fields[x++] = s_hours.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 16);
        }

        // Encoding s_dbCombinations: SEQUENCE OF OPTIONAL

        if (s_dbCombinations != null) {
            f2 = new BEREncoding[s_dbCombinations.length];

            for (p = 0; p < s_dbCombinations.length; p++) {
                f2[p] = s_dbCombinations[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 17, f2);
        }

        // Encoding s_addresses: SEQUENCE OF OPTIONAL

        if (s_addresses != null) {
            f2 = new BEREncoding[s_addresses.length];

            for (p = 0; p < s_addresses.length; p++) {
                f2[p] = s_addresses[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 18, f2);
        }

        // Encoding s_languages: SEQUENCE OF OPTIONAL

        if (s_languages != null) {
            f2 = new BEREncoding[s_languages.length];

            for (p = 0; p < s_languages.length; p++) {
                f2[p] = s_languages[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 101, f2);
        }

        // Encoding s_commonAccessInfo: AccessInfo OPTIONAL

        if (s_commonAccessInfo != null) {
            fields[x++] = s_commonAccessInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 19);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the TargetInfo.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_commonInfo != null) {
            str.append("commonInfo ");
            str.append(s_commonInfo);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("name ");
        str.append(s_name);
        outputted++;

        if (s_recent_news != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("recent-news ");
            str.append(s_recent_news);
            outputted++;
        }

        if (s_icon != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("icon ");
            str.append(s_icon);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("namedResultSets ");
        str.append(s_namedResultSets);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("multipleDBsearch ");
        str.append(s_multipleDBsearch);
        outputted++;

        if (s_maxResultSets != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("maxResultSets ");
            str.append(s_maxResultSets);
            outputted++;
        }

        if (s_maxResultSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("maxResultSize ");
            str.append(s_maxResultSize);
            outputted++;
        }

        if (s_maxTerms != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("maxTerms ");
            str.append(s_maxTerms);
            outputted++;
        }

        if (s_timeoutInterval != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("timeoutInterval ");
            str.append(s_timeoutInterval);
            outputted++;
        }

        if (s_welcomeMessage != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("welcomeMessage ");
            str.append(s_welcomeMessage);
            outputted++;
        }

        if (s_contactInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("contactInfo ");
            str.append(s_contactInfo);
            outputted++;
        }

        if (s_description != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("description ");
            str.append(s_description);
            outputted++;
        }

        if (s_nicknames != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("nicknames ");
            str.append("{");
            for (p = 0; p < s_nicknames.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_nicknames[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_usage_restrictions != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("usage-restrictions ");
            str.append(s_usage_restrictions);
            outputted++;
        }

        if (s_paymentAddr != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("paymentAddr ");
            str.append(s_paymentAddr);
            outputted++;
        }

        if (s_hours != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("hours ");
            str.append(s_hours);
            outputted++;
        }

        if (s_dbCombinations != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("dbCombinations ");
            str.append("{");
            for (p = 0; p < s_dbCombinations.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_dbCombinations[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_addresses != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("addresses ");
            str.append("{");
            for (p = 0; p < s_addresses.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_addresses[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_languages != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("languages ");
            str.append("{");
            for (p = 0; p < s_languages.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_languages[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_commonAccessInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("commonAccessInfo ");
            str.append(s_commonAccessInfo);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public CommonInfo s_commonInfo; // optional
    public InternationalString s_name;
    public HumanString s_recent_news; // optional
    public IconObject s_icon; // optional
    public ASN1Boolean s_namedResultSets;
    public ASN1Boolean s_multipleDBsearch;
    public ASN1Integer s_maxResultSets; // optional
    public ASN1Integer s_maxResultSize; // optional
    public ASN1Integer s_maxTerms; // optional
    public IntUnit s_timeoutInterval; // optional
    public HumanString s_welcomeMessage; // optional
    public ContactInfo s_contactInfo; // optional
    public HumanString s_description; // optional
    public InternationalString s_nicknames[]; // optional
    public HumanString s_usage_restrictions; // optional
    public HumanString s_paymentAddr; // optional
    public HumanString s_hours; // optional
    public DatabaseList s_dbCombinations[]; // optional
    public NetworkAddress s_addresses[]; // optional
    public InternationalString s_languages[]; // optional
    public AccessInfo s_commonAccessInfo; // optional

} // TargetInfo


//EOF
