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
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.BEREncoding;



/**
 * Class for representing a <code>Explain_Record</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * Explain_Record ::=
 * CHOICE {
 *   targetInfo [0] IMPLICIT TargetInfo
 *   databaseInfo [1] IMPLICIT DatabaseInfo
 *   schemaInfo [2] IMPLICIT SchemaInfo
 *   tagSetInfo [3] IMPLICIT TagSetInfo
 *   recordSyntaxInfo [4] IMPLICIT RecordSyntaxInfo
 *   attributeSetInfo [5] IMPLICIT AttributeSetInfo
 *   termListInfo [6] IMPLICIT TermListInfo
 *   extendedServicesInfo [7] IMPLICIT ExtendedServicesInfo
 *   attributeDetails [8] IMPLICIT AttributeDetails
 *   termListDetails [9] IMPLICIT TermListDetails
 *   elementSetDetails [10] IMPLICIT ElementSetDetails
 *   retrievalRecordDetails [11] IMPLICIT RetrievalRecordDetails
 *   sortDetails [12] IMPLICIT SortDetails
 *   processing [13] IMPLICIT ProcessingInformation
 *   variants [14] IMPLICIT VariantSetInfo
 *   units [15] IMPLICIT UnitInfo
 *   categoryList [100] IMPLICIT CategoryList
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class Explain_Record extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a Explain_Record.
     */

    public Explain_Record() {
    }



    /**
     * Constructor for a Explain_Record from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Explain_Record(BEREncoding ber, boolean check_tag)
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
        // Null out all choices

        c_targetInfo = null;
        c_databaseInfo = null;
        c_schemaInfo = null;
        c_tagSetInfo = null;
        c_recordSyntaxInfo = null;
        c_attributeSetInfo = null;
        c_termListInfo = null;
        c_extendedServicesInfo = null;
        c_attributeDetails = null;
        c_termListDetails = null;
        c_elementSetDetails = null;
        c_retrievalRecordDetails = null;
        c_sortDetails = null;
        c_processing = null;
        c_variants = null;
        c_units = null;
        c_categoryList = null;

        // Try choice targetInfo
        if (ber.tag_get() == 0 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_targetInfo = new TargetInfo(ber, false);
            return;
        }

        // Try choice databaseInfo
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_databaseInfo = new DatabaseInfo(ber, false);
            return;
        }

        // Try choice schemaInfo
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_schemaInfo = new SchemaInfo(ber, false);
            return;
        }

        // Try choice tagSetInfo
        if (ber.tag_get() == 3 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_tagSetInfo = new TagSetInfo(ber, false);
            return;
        }

        // Try choice recordSyntaxInfo
        if (ber.tag_get() == 4 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_recordSyntaxInfo = new RecordSyntaxInfo(ber, false);
            return;
        }

        // Try choice attributeSetInfo
        if (ber.tag_get() == 5 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_attributeSetInfo = new AttributeSetInfo(ber, false);
            return;
        }

        // Try choice termListInfo
        if (ber.tag_get() == 6 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_termListInfo = new TermListInfo(ber, false);
            return;
        }

        // Try choice extendedServicesInfo
        if (ber.tag_get() == 7 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_extendedServicesInfo = new ExtendedServicesInfo(ber, false);
            return;
        }

        // Try choice attributeDetails
        if (ber.tag_get() == 8 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_attributeDetails = new AttributeDetails(ber, false);
            return;
        }

        // Try choice termListDetails
        if (ber.tag_get() == 9 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_termListDetails = new TermListDetails(ber, false);
            return;
        }

        // Try choice elementSetDetails
        if (ber.tag_get() == 10 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_elementSetDetails = new ElementSetDetails(ber, false);
            return;
        }

        // Try choice retrievalRecordDetails
        if (ber.tag_get() == 11 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_retrievalRecordDetails = new RetrievalRecordDetails(ber, false);
            return;
        }

        // Try choice sortDetails
        if (ber.tag_get() == 12 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_sortDetails = new SortDetails(ber, false);
            return;
        }

        // Try choice processing
        if (ber.tag_get() == 13 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_processing = new ProcessingInformation(ber, false);
            return;
        }

        // Try choice variants
        if (ber.tag_get() == 14 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_variants = new VariantSetInfo(ber, false);
            return;
        }

        // Try choice units
        if (ber.tag_get() == 15 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_units = new UnitInfo(ber, false);
            return;
        }

        // Try choice categoryList
        if (ber.tag_get() == 100 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_categoryList = new CategoryList(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun Explain_Record: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of Explain_Record.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_targetInfo
        if (c_targetInfo != null) {
            chosen = c_targetInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding choice: c_databaseInfo
        if (c_databaseInfo != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_databaseInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_schemaInfo
        if (c_schemaInfo != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_schemaInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding choice: c_tagSetInfo
        if (c_tagSetInfo != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_tagSetInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding choice: c_recordSyntaxInfo
        if (c_recordSyntaxInfo != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_recordSyntaxInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding choice: c_attributeSetInfo
        if (c_attributeSetInfo != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_attributeSetInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding choice: c_termListInfo
        if (c_termListInfo != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_termListInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        // Encoding choice: c_extendedServicesInfo
        if (c_extendedServicesInfo != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_extendedServicesInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Encoding choice: c_attributeDetails
        if (c_attributeDetails != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_attributeDetails.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
        }

        // Encoding choice: c_termListDetails
        if (c_termListDetails != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_termListDetails.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);
        }

        // Encoding choice: c_elementSetDetails
        if (c_elementSetDetails != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_elementSetDetails.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 10);
        }

        // Encoding choice: c_retrievalRecordDetails
        if (c_retrievalRecordDetails != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_retrievalRecordDetails.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 11);
        }

        // Encoding choice: c_sortDetails
        if (c_sortDetails != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_sortDetails.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 12);
        }

        // Encoding choice: c_processing
        if (c_processing != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_processing.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 13);
        }

        // Encoding choice: c_variants
        if (c_variants != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_variants.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 14);
        }

        // Encoding choice: c_units
        if (c_units != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_units.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 15);
        }

        // Encoding choice: c_categoryList
        if (c_categoryList != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_categoryList.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 100);
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

        throw new ASN1EncodingException("Zebulun Explain_Record: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the Explain_Record.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_targetInfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: targetInfo> ");
            }
            found = true;
            str.append("targetInfo ");
            str.append(c_targetInfo);
        }

        if (c_databaseInfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: databaseInfo> ");
            }
            found = true;
            str.append("databaseInfo ");
            str.append(c_databaseInfo);
        }

        if (c_schemaInfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: schemaInfo> ");
            }
            found = true;
            str.append("schemaInfo ");
            str.append(c_schemaInfo);
        }

        if (c_tagSetInfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: tagSetInfo> ");
            }
            found = true;
            str.append("tagSetInfo ");
            str.append(c_tagSetInfo);
        }

        if (c_recordSyntaxInfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: recordSyntaxInfo> ");
            }
            found = true;
            str.append("recordSyntaxInfo ");
            str.append(c_recordSyntaxInfo);
        }

        if (c_attributeSetInfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: attributeSetInfo> ");
            }
            found = true;
            str.append("attributeSetInfo ");
            str.append(c_attributeSetInfo);
        }

        if (c_termListInfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: termListInfo> ");
            }
            found = true;
            str.append("termListInfo ");
            str.append(c_termListInfo);
        }

        if (c_extendedServicesInfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: extendedServicesInfo> ");
            }
            found = true;
            str.append("extendedServicesInfo ");
            str.append(c_extendedServicesInfo);
        }

        if (c_attributeDetails != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: attributeDetails> ");
            }
            found = true;
            str.append("attributeDetails ");
            str.append(c_attributeDetails);
        }

        if (c_termListDetails != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: termListDetails> ");
            }
            found = true;
            str.append("termListDetails ");
            str.append(c_termListDetails);
        }

        if (c_elementSetDetails != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: elementSetDetails> ");
            }
            found = true;
            str.append("elementSetDetails ");
            str.append(c_elementSetDetails);
        }

        if (c_retrievalRecordDetails != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: retrievalRecordDetails> ");
            }
            found = true;
            str.append("retrievalRecordDetails ");
            str.append(c_retrievalRecordDetails);
        }

        if (c_sortDetails != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: sortDetails> ");
            }
            found = true;
            str.append("sortDetails ");
            str.append(c_sortDetails);
        }

        if (c_processing != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: processing> ");
            }
            found = true;
            str.append("processing ");
            str.append(c_processing);
        }

        if (c_variants != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: variants> ");
            }
            found = true;
            str.append("variants ");
            str.append(c_variants);
        }

        if (c_units != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: units> ");
            }
            found = true;
            str.append("units ");
            str.append(c_units);
        }

        if (c_categoryList != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: categoryList> ");
            }
            found = true;
            str.append("categoryList ");
            str.append(c_categoryList);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public TargetInfo c_targetInfo;
    public DatabaseInfo c_databaseInfo;
    public SchemaInfo c_schemaInfo;
    public TagSetInfo c_tagSetInfo;
    public RecordSyntaxInfo c_recordSyntaxInfo;
    public AttributeSetInfo c_attributeSetInfo;
    public TermListInfo c_termListInfo;
    public ExtendedServicesInfo c_extendedServicesInfo;
    public AttributeDetails c_attributeDetails;
    public TermListDetails c_termListDetails;
    public ElementSetDetails c_elementSetDetails;
    public RetrievalRecordDetails c_retrievalRecordDetails;
    public SortDetails c_sortDetails;
    public ProcessingInformation c_processing;
    public VariantSetInfo c_variants;
    public UnitInfo c_units;
    public CategoryList c_categoryList;

} // Explain_Record


//EOF
