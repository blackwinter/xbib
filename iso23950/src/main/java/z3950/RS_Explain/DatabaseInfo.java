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
import asn1.ASN1GeneralizedTime;
import asn1.ASN1Integer;
import asn1.ASN1Null;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.DatabaseName;
import z3950.v3.IntUnit;

/**
 * Class for representing a <code>DatabaseInfo</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * DatabaseInfo ::=
 * SEQUENCE {
 *   commonInfo [0] IMPLICIT CommonInfo OPTIONAL
 *   name [1] IMPLICIT DatabaseName
 *   explainDatabase [2] IMPLICIT NULL OPTIONAL
 *   nicknames [3] IMPLICIT SEQUENCE OF DatabaseName OPTIONAL
 *   icon [4] IMPLICIT IconObject OPTIONAL
 *   user-fee [5] IMPLICIT BOOLEAN
 *   available [6] IMPLICIT BOOLEAN
 *   titleString [7] IMPLICIT HumanString OPTIONAL
 *   keywords [8] IMPLICIT SEQUENCE OF HumanString OPTIONAL
 *   description [9] IMPLICIT HumanString OPTIONAL
 *   associatedDbs [10] IMPLICIT DatabaseList OPTIONAL
 *   subDbs [11] IMPLICIT DatabaseList OPTIONAL
 *   disclaimers [12] IMPLICIT HumanString OPTIONAL
 *   news [13] IMPLICIT HumanString OPTIONAL
 *   recordCount [14] EXPLICIT DatabaseInfo_recordCount OPTIONAL
 *   defaultOrder [15] IMPLICIT HumanString OPTIONAL
 *   avRecordSize [16] IMPLICIT INTEGER OPTIONAL
 *   maxRecordSize [17] IMPLICIT INTEGER OPTIONAL
 *   hours [18] IMPLICIT HumanString OPTIONAL
 *   bestTime [19] IMPLICIT HumanString OPTIONAL
 *   lastUpdate [20] IMPLICIT GeneralizedTime OPTIONAL
 *   updateInterval [21] IMPLICIT IntUnit OPTIONAL
 *   coverage [22] IMPLICIT HumanString OPTIONAL
 *   proprietary [23] IMPLICIT BOOLEAN OPTIONAL
 *   copyrightText [24] IMPLICIT HumanString OPTIONAL
 *   copyrightNotice [25] IMPLICIT HumanString OPTIONAL
 *   producerContactInfo [26] IMPLICIT ContactInfo OPTIONAL
 *   supplierContactInfo [27] IMPLICIT ContactInfo OPTIONAL
 *   submissionContactInfo [28] IMPLICIT ContactInfo OPTIONAL
 *   accessInfo [29] IMPLICIT AccessInfo OPTIONAL
 * }
 * </pre>
 *
 */
public final class DatabaseInfo extends ASN1Any {
    /**
     * Default constructor for a DatabaseInfo.
     */

    public DatabaseInfo() {
    }
    /**
     * Constructor for a DatabaseInfo from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DatabaseInfo(BEREncoding ber, boolean check_tag)
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
        // DatabaseInfo should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    (" DatabaseInfo: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: commonInfo [0] IMPLICIT CommonInfo OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" DatabaseInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_commonInfo = new CommonInfo(p, false);
            part++;
        }

        // Decoding: name [1] IMPLICIT DatabaseName

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" DatabaseInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    (" DatabaseInfo: bad tag in s_name\n");
        }

        s_name = new DatabaseName(p, false);
        part++;

        // Decoding: explainDatabase [2] IMPLICIT NULL OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" DatabaseInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_explainDatabase = new ASN1Null(p, false);
            part++;
        }

        // Decoding: nicknames [3] IMPLICIT SEQUENCE OF DatabaseName OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" DatabaseInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_nicknames = new DatabaseName[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_nicknames[n] = new DatabaseName(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: icon [4] IMPLICIT IconObject OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" DatabaseInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_icon = new IconObject(p, false);
            part++;
        }

        // Decoding: user-fee [5] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" DatabaseInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 5 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    (" DatabaseInfo: bad tag in s_user_fee\n");
        }

        s_user_fee = new ASN1Boolean(p, false);
        part++;

        // Decoding: available [6] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception(" DatabaseInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 6 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    (" DatabaseInfo: bad tag in s_available\n");
        }

        s_available = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_titleString = null;
        s_keywords = null;
        s_description = null;
        s_associatedDbs = null;
        s_subDbs = null;
        s_disclaimers = null;
        s_news = null;
        s_recordCount = null;
        s_defaultOrder = null;
        s_avRecordSize = null;
        s_maxRecordSize = null;
        s_hours = null;
        s_bestTime = null;
        s_lastUpdate = null;
        s_updateInterval = null;
        s_coverage = null;
        s_proprietary = null;
        s_copyrightText = null;
        s_copyrightNotice = null;
        s_producerContactInfo = null;
        s_supplierContactInfo = null;
        s_submissionContactInfo = null;
        s_accessInfo = null;

        // Decoding: titleString [7] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 7 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_titleString = new HumanString(p, false);
            part++;
        }

        // Decoding: keywords [8] IMPLICIT SEQUENCE OF HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 8 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_keywords = new HumanString[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_keywords[n] = new HumanString(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: description [9] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 9 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_description = new HumanString(p, false);
            part++;
        }

        // Decoding: associatedDbs [10] IMPLICIT DatabaseList OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 10 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_associatedDbs = new DatabaseList(p, false);
            part++;
        }

        // Decoding: subDbs [11] IMPLICIT DatabaseList OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 11 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_subDbs = new DatabaseList(p, false);
            part++;
        }

        // Decoding: disclaimers [12] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 12 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_disclaimers = new HumanString(p, false);
            part++;
        }

        // Decoding: news [13] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 13 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_news = new HumanString(p, false);
            part++;
        }

        // Decoding: recordCount [14] EXPLICIT DatabaseInfo_recordCount OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 14 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("DatabaseInfo: bad BER encoding: s_recordCount tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("DatabaseInfo: bad BER encoding: s_recordCount tag bad\n");
            }

            s_recordCount = new DatabaseInfo_recordCount(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: defaultOrder [15] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 15 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_defaultOrder = new HumanString(p, false);
            part++;
        }

        // Decoding: avRecordSize [16] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 16 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_avRecordSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: maxRecordSize [17] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 17 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_maxRecordSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: hours [18] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 18 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_hours = new HumanString(p, false);
            part++;
        }

        // Decoding: bestTime [19] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 19 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_bestTime = new HumanString(p, false);
            part++;
        }

        // Decoding: lastUpdate [20] IMPLICIT GeneralizedTime OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 20 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_lastUpdate = new ASN1GeneralizedTime(p, false);
            part++;
        }

        // Decoding: updateInterval [21] IMPLICIT IntUnit OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 21 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_updateInterval = new IntUnit(p, false);
            part++;
        }

        // Decoding: coverage [22] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 22 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_coverage = new HumanString(p, false);
            part++;
        }

        // Decoding: proprietary [23] IMPLICIT BOOLEAN OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 23 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_proprietary = new ASN1Boolean(p, false);
            part++;
        }

        // Decoding: copyrightText [24] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 24 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_copyrightText = new HumanString(p, false);
            part++;
        }

        // Decoding: copyrightNotice [25] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 25 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_copyrightNotice = new HumanString(p, false);
            part++;
        }

        // Decoding: producerContactInfo [26] IMPLICIT ContactInfo OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 26 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_producerContactInfo = new ContactInfo(p, false);
            part++;
        }

        // Decoding: supplierContactInfo [27] IMPLICIT ContactInfo OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 27 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_supplierContactInfo = new ContactInfo(p, false);
            part++;
        }

        // Decoding: submissionContactInfo [28] IMPLICIT ContactInfo OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 28 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_submissionContactInfo = new ContactInfo(p, false);
            part++;
        }

        // Decoding: accessInfo [29] IMPLICIT AccessInfo OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 29 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_accessInfo = new AccessInfo(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception(" DatabaseInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the DatabaseInfo.
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
     * Returns a BER encoding of DatabaseInfo, implicitly tagged.
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
        if (s_explainDatabase != null) {
            num_fields++;
        }
        if (s_nicknames != null) {
            num_fields++;
        }
        if (s_icon != null) {
            num_fields++;
        }
        if (s_titleString != null) {
            num_fields++;
        }
        if (s_keywords != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }
        if (s_associatedDbs != null) {
            num_fields++;
        }
        if (s_subDbs != null) {
            num_fields++;
        }
        if (s_disclaimers != null) {
            num_fields++;
        }
        if (s_news != null) {
            num_fields++;
        }
        if (s_recordCount != null) {
            num_fields++;
        }
        if (s_defaultOrder != null) {
            num_fields++;
        }
        if (s_avRecordSize != null) {
            num_fields++;
        }
        if (s_maxRecordSize != null) {
            num_fields++;
        }
        if (s_hours != null) {
            num_fields++;
        }
        if (s_bestTime != null) {
            num_fields++;
        }
        if (s_lastUpdate != null) {
            num_fields++;
        }
        if (s_updateInterval != null) {
            num_fields++;
        }
        if (s_coverage != null) {
            num_fields++;
        }
        if (s_proprietary != null) {
            num_fields++;
        }
        if (s_copyrightText != null) {
            num_fields++;
        }
        if (s_copyrightNotice != null) {
            num_fields++;
        }
        if (s_producerContactInfo != null) {
            num_fields++;
        }
        if (s_supplierContactInfo != null) {
            num_fields++;
        }
        if (s_submissionContactInfo != null) {
            num_fields++;
        }
        if (s_accessInfo != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;
        BEREncoding enc[];

        // Encoding s_commonInfo: CommonInfo OPTIONAL

        if (s_commonInfo != null) {
            fields[x++] = s_commonInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_name: DatabaseName

        fields[x++] = s_name.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_explainDatabase: NULL OPTIONAL

        if (s_explainDatabase != null) {
            fields[x++] = s_explainDatabase.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_nicknames: SEQUENCE OF OPTIONAL

        if (s_nicknames != null) {
            f2 = new BEREncoding[s_nicknames.length];

            for (p = 0; p < s_nicknames.length; p++) {
                f2[p] = s_nicknames[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, f2);
        }

        // Encoding s_icon: IconObject OPTIONAL

        if (s_icon != null) {
            fields[x++] = s_icon.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_user_fee: BOOLEAN

        fields[x++] = s_user_fee.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);

        // Encoding s_available: BOOLEAN

        fields[x++] = s_available.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);

        // Encoding s_titleString: HumanString OPTIONAL

        if (s_titleString != null) {
            fields[x++] = s_titleString.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Encoding s_keywords: SEQUENCE OF OPTIONAL

        if (s_keywords != null) {
            f2 = new BEREncoding[s_keywords.length];

            for (p = 0; p < s_keywords.length; p++) {
                f2[p] = s_keywords[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 8, f2);
        }

        // Encoding s_description: HumanString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);
        }

        // Encoding s_associatedDbs: DatabaseList OPTIONAL

        if (s_associatedDbs != null) {
            fields[x++] = s_associatedDbs.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 10);
        }

        // Encoding s_subDbs: DatabaseList OPTIONAL

        if (s_subDbs != null) {
            fields[x++] = s_subDbs.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 11);
        }

        // Encoding s_disclaimers: HumanString OPTIONAL

        if (s_disclaimers != null) {
            fields[x++] = s_disclaimers.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 12);
        }

        // Encoding s_news: HumanString OPTIONAL

        if (s_news != null) {
            fields[x++] = s_news.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 13);
        }

        // Encoding s_recordCount: DatabaseInfo_recordCount OPTIONAL

        if (s_recordCount != null) {
            enc = new BEREncoding[1];
            enc[0] = s_recordCount.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 14, enc);
        }

        // Encoding s_defaultOrder: HumanString OPTIONAL

        if (s_defaultOrder != null) {
            fields[x++] = s_defaultOrder.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 15);
        }

        // Encoding s_avRecordSize: INTEGER OPTIONAL

        if (s_avRecordSize != null) {
            fields[x++] = s_avRecordSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 16);
        }

        // Encoding s_maxRecordSize: INTEGER OPTIONAL

        if (s_maxRecordSize != null) {
            fields[x++] = s_maxRecordSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 17);
        }

        // Encoding s_hours: HumanString OPTIONAL

        if (s_hours != null) {
            fields[x++] = s_hours.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 18);
        }

        // Encoding s_bestTime: HumanString OPTIONAL

        if (s_bestTime != null) {
            fields[x++] = s_bestTime.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 19);
        }

        // Encoding s_lastUpdate: GeneralizedTime OPTIONAL

        if (s_lastUpdate != null) {
            fields[x++] = s_lastUpdate.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 20);
        }

        // Encoding s_updateInterval: IntUnit OPTIONAL

        if (s_updateInterval != null) {
            fields[x++] = s_updateInterval.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 21);
        }

        // Encoding s_coverage: HumanString OPTIONAL

        if (s_coverage != null) {
            fields[x++] = s_coverage.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 22);
        }

        // Encoding s_proprietary: BOOLEAN OPTIONAL

        if (s_proprietary != null) {
            fields[x++] = s_proprietary.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 23);
        }

        // Encoding s_copyrightText: HumanString OPTIONAL

        if (s_copyrightText != null) {
            fields[x++] = s_copyrightText.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 24);
        }

        // Encoding s_copyrightNotice: HumanString OPTIONAL

        if (s_copyrightNotice != null) {
            fields[x++] = s_copyrightNotice.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 25);
        }

        // Encoding s_producerContactInfo: ContactInfo OPTIONAL

        if (s_producerContactInfo != null) {
            fields[x++] = s_producerContactInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 26);
        }

        // Encoding s_supplierContactInfo: ContactInfo OPTIONAL

        if (s_supplierContactInfo != null) {
            fields[x++] = s_supplierContactInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 27);
        }

        // Encoding s_submissionContactInfo: ContactInfo OPTIONAL

        if (s_submissionContactInfo != null) {
            fields[x++] = s_submissionContactInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 28);
        }

        // Encoding s_accessInfo: AccessInfo OPTIONAL

        if (s_accessInfo != null) {
            fields[x++] = s_accessInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 29);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the DatabaseInfo.
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

        if (s_explainDatabase != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("explainDatabase ");
            str.append(s_explainDatabase);
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
        str.append("user-fee ");
        str.append(s_user_fee);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("available ");
        str.append(s_available);
        outputted++;

        if (s_titleString != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("titleString ");
            str.append(s_titleString);
            outputted++;
        }

        if (s_keywords != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("keywords ");
            str.append("{");
            for (p = 0; p < s_keywords.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_keywords[p]);
            }
            str.append("}");
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

        if (s_associatedDbs != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("associatedDbs ");
            str.append(s_associatedDbs);
            outputted++;
        }

        if (s_subDbs != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("subDbs ");
            str.append(s_subDbs);
            outputted++;
        }

        if (s_disclaimers != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("disclaimers ");
            str.append(s_disclaimers);
            outputted++;
        }

        if (s_news != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("news ");
            str.append(s_news);
            outputted++;
        }

        if (s_recordCount != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("recordCount ");
            str.append(s_recordCount);
            outputted++;
        }

        if (s_defaultOrder != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("defaultOrder ");
            str.append(s_defaultOrder);
            outputted++;
        }

        if (s_avRecordSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("avRecordSize ");
            str.append(s_avRecordSize);
            outputted++;
        }

        if (s_maxRecordSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("maxRecordSize ");
            str.append(s_maxRecordSize);
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

        if (s_bestTime != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("bestTime ");
            str.append(s_bestTime);
            outputted++;
        }

        if (s_lastUpdate != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("lastUpdate ");
            str.append(s_lastUpdate);
            outputted++;
        }

        if (s_updateInterval != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("updateInterval ");
            str.append(s_updateInterval);
            outputted++;
        }

        if (s_coverage != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("coverage ");
            str.append(s_coverage);
            outputted++;
        }

        if (s_proprietary != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("proprietary ");
            str.append(s_proprietary);
            outputted++;
        }

        if (s_copyrightText != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("copyrightText ");
            str.append(s_copyrightText);
            outputted++;
        }

        if (s_copyrightNotice != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("copyrightNotice ");
            str.append(s_copyrightNotice);
            outputted++;
        }

        if (s_producerContactInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("producerContactInfo ");
            str.append(s_producerContactInfo);
            outputted++;
        }

        if (s_supplierContactInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("supplierContactInfo ");
            str.append(s_supplierContactInfo);
            outputted++;
        }

        if (s_submissionContactInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("submissionContactInfo ");
            str.append(s_submissionContactInfo);
            outputted++;
        }

        if (s_accessInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("accessInfo ");
            str.append(s_accessInfo);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public CommonInfo s_commonInfo; // optional
    public DatabaseName s_name;
    public ASN1Null s_explainDatabase; // optional
    public DatabaseName s_nicknames[]; // optional
    public IconObject s_icon; // optional
    public ASN1Boolean s_user_fee;
    public ASN1Boolean s_available;
    public HumanString s_titleString; // optional
    public HumanString s_keywords[]; // optional
    public HumanString s_description; // optional
    public DatabaseList s_associatedDbs; // optional
    public DatabaseList s_subDbs; // optional
    public HumanString s_disclaimers; // optional
    public HumanString s_news; // optional
    public DatabaseInfo_recordCount s_recordCount; // optional
    public HumanString s_defaultOrder; // optional
    public ASN1Integer s_avRecordSize; // optional
    public ASN1Integer s_maxRecordSize; // optional
    public HumanString s_hours; // optional
    public HumanString s_bestTime; // optional
    public ASN1GeneralizedTime s_lastUpdate; // optional
    public IntUnit s_updateInterval; // optional
    public HumanString s_coverage; // optional
    public ASN1Boolean s_proprietary; // optional
    public HumanString s_copyrightText; // optional
    public HumanString s_copyrightNotice; // optional
    public ContactInfo s_producerContactInfo; // optional
    public ContactInfo s_supplierContactInfo; // optional
    public ContactInfo s_submissionContactInfo; // optional
    public AccessInfo s_accessInfo; // optional

} // DatabaseInfo


//EOF
