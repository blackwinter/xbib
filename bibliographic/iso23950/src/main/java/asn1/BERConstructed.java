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
package asn1;

import java.io.OutputStream;

/**
 * BERConstructed
 * <p/>
 * This class represents a BER encoded ASN.1 object which is
 * constructed from component BER encodings.
 * <p/>
 * <p/>
 * Generally it is used to store the BER encoding of constructed types
 * (i.e. SEQUENCE, SEQUENCE OF, SET, and SET OF) The end-of-content
 * octets, if required, must be added to the end of the elements by
 * the creator.
 *
 */

public class BERConstructed extends BEREncoding {

    /**
     * Constructor for a non-primitive BEREncoding.
     *
     * @param asn1_class The tag type.
     * @param tag        The tag number.
     * @param elements   The components making up the constructed BER.
     * @throws ASN1Exception If tag or tag type is invalid
     * @see asn1.BEREncoding#UNIVERSAL_TAG
     * @see asn1.BEREncoding#APPLICATION_TAG
     * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
     * @see asn1.BEREncoding#PRIVATE_TAG
     */

    public BERConstructed(int asn1_class, int tag, BEREncoding elements[])
            throws ASN1Exception {
        int content_length = 0;
        for (int index = 0; index < elements.length; index++) {
            content_length += elements[index].i_total_length;
        }

        init(asn1_class, /* constructed */ true, tag, content_length);
        content_elements = elements;
    }

  /*
   * This method outputs the encoded octets for this object
   * to the output stream.
   *
   * Note: the output is not flushed, so you <strong>must</strong>  explicitly
   * flush the output stream after calling this method to ensure that
   * the data has been written out.
   *
   * @param	dest - OutputStream to write encoding to.
   */

    public void output(OutputStream dest)
            throws java.io.IOException {
        output_head(dest);

        for (int index = 0; index < content_elements.length; index++) {
            content_elements[index].output(dest);
        }
    }

    /**
     * This method returns the number of BER encoded elements that this
     * object is made up of to be returned.
     */

    public int
    number_components() {
        return content_elements.length;
    }

    /**
     * This method allows the elements of the BER encoding to be examined.
     *
     * @param    index - the index of the BER object required,
     * it must be in the range, [0, number_components() - 1]
     */

    public BEREncoding
    elementAt(int index) {
        return content_elements[index];
    }

    /**
     * Returns a new String object representing this ASN.1 object's value.
     */

    public String
    toString() {
        StringBuilder str = new StringBuilder("[");
        switch (i_tag_type) {
            case BEREncoding.UNIVERSAL_TAG:
                str.append("UNIVERSAL ");
                break;
            case BEREncoding.APPLICATION_TAG:
                str.append("APPLICATION ");
                break;
            case BEREncoding.CONTEXT_SPECIFIC_TAG:
                str.append("CONTEXT SPECIFIC ");
                break;
            case BEREncoding.PRIVATE_TAG:
                str.append("PRIVATE ");
                break;
        }
        str.append(String.valueOf(i_tag) + "]{");

        for (int x = 0; x < content_elements.length; x++) {
            if (x != 0) {
                str.append(',');
            }

            str.append(content_elements[x].toString());
        }

        str.append('}');

        return new String(str);
    }

    /**
     * This protected method is used to implement the "get_encoding" method.
     */
    protected int i_encoding_get(int offset, byte data[]) {
        offset = i_get_head(offset, data);

        for (int index = 0; index < content_elements.length; index++) {
            offset = content_elements[index].i_encoding_get(offset, data);
        }

        return offset;
    }

    private BEREncoding content_elements[];
}
