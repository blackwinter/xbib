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
package org.xbib.marc.label;

/**
 * Type of record
 *
 *  One-character alphabetic code used to define the characteristics and components of the record.
 *
 *  Used to differentiate MARC records created for various types of content and material and
 *  to determine the appropriateness and validity of certain data elements in the record.
 *
 *  Microforms, whether original or reproductions, are not identified by a distinctive
 *  Type of record code. The type of content characteristics described by the codes
 *  take precedence over the microform characteristics of the item.
 *  Computer files are identified by a distinctive Type of record code only if they belong
 *  to certain categories of electronic resources as specified below; in all other cases
 *  the type of content characteristics described by the other codes take precedence
 *  over the computer file characteristics of the item.
 *
 *  Determination of the code for a multi-item bibliographic entity (types of material
 *  are those specified by values a through t below):
 *
 *  Items are multiple forms of material
 *
 *  o (Kit) - entity is issued as a single unit; no type of material predominates
 *  p (Mixed materials) - entity is a made-up collection; no type of material predominates
 *  other codes - entity is a made-up collection; one type of material predominates
 *
 * Items are all one form of material
 *
 *  any except o or p - all cases
 *  a - Language material
 *  Used for non-manuscript language material. Manuscript language material uses code t.
 *
 *  Includes microforms and electronic resources that are basically textual in nature,
 *  whether they are reproductions from print or originally produced.
 *
 *  c - Notated music
 *  Used for printed, microform, or electronic notated music.
 *
 *  d - Manuscript notated music
 *  Used for manuscript notated music or a microform of manuscript music.
 *
 *  e - Cartographic material
 *  Used for non-manuscript cartographic material or a microform of non-manuscript cartographic material.
 *
 *  Includes maps, atlases, globes, digital maps, and other cartographic items.
 *
 *  f - Manuscript cartographic material
 *  Used for manuscript cartographic material or a microform of manuscript cartographic material.
 *
 *  g - Projected medium
 *  Used for motion pictures, videorecordings (including digital video), filmstrips, slide,
 *  transparencies or material specifically designed for projection.
 *
 *  Material specifically designed for overhead projection is also included in this type of record category.
 *
 *  i - Nonmusical sound recording
 *  Used for a recording of nonmusical sounds (e.g., speech).
 *
 *  j - Musical sound recording
 *  Used for a musical sound recording (e.g., phonodiscs, compact discs, or cassette tapes.
 *
 *  k - Two-dimensional nonprojectable graphic
 *  Used for two-dimensional nonprojectable graphics such as, activity cards, charts, collages,
 *  computer graphics, digital pictures, drawings, duplication masters, flash cards, paintings,
 *  photo CDs, photomechanical reproductions, photonegatives, photoprints, pictures, postcards,
 *  posters, prints, spirit masters, study prints, technical drawings, transparency masters,
 *  and reproductions of any of these.
 *
 *  m - Computer file
 *  Used for the following classes of electronic resources: computer software
 *  (including programs, games, fonts), numeric data, computer-oriented multimedia,
 *  online systems or services. For these classes of materials, if there is a significant
 *  aspect that causes it to fall into another Leader/06 category, the code for that
 *  significant aspect is used instead of code m (e.g., vector data that is cartographic
 *  is not coded as numeric but as cartographic). Other classes of electronic resources are
 *  coded for their most significant aspect (e.g. language material, graphic, cartographic material,
 *  sound, music, moving image). In case of doubt or if the most significant aspect cannot be
 *  determined, consider the item a computer file.
 *
 *  o - Kit
 *  Used for a mixture of various components issued as a unit and intended primarily
 *  for instructional purposes where no one item is the predominant component of the kit.
 *
 *  Examples are packages of assorted materials, such as a set of school social studies
 *  curriculum material (books, workbooks, guides, activities, etc.), or packages of
 *  educational test materials (tests, answer sheets, scoring guides, score charts,
 *  interpretative manuals, etc.).
 *
 *  p - Mixed materials
 *  Used when there are significant materials in two or more forms that are usually related
 *  by virtue of their having been accumulated by or about a person or body.
 *  Includes archival fonds and manuscript collections of mixed forms of materials,
 *  such as text, photographs, and sound recordings.
 *
 *  Intended primary purpose is other than for instructional purposes (i.e., other than
 *  the purpose of those materials coded as o (Kit)).
 *
 *  r - Three-dimensional artifact or naturally occurring object
 *  Includes man-made objects such as models, dioramas, games, puzzles, simulations,
 *  sculptures and other three-dimensional art works, exhibits, machines, clothing, toys,
 *  and stitchery. Also includes naturally occurring objects such as, microscope specimens
 *  (or representations of them) and other specimens mounted for viewing.
 *
 *  t - Manuscript language material
 *  Used for manuscript language material or a microform of manuscript language material.
 *  This category is applied to items for language material in handwriting, typescript, or
 *  computer printout including printed materials completed by hand or by keyboard.
 *  At the time it is created, this material is usually intended, either implicitly or explicitly,
 *  to exist as a single instance. Examples include marked or corrected galley and page proofs,
 *  manuscript books, legal papers, and unpublished theses and dissertations.
 *
 * http://www.ifla.org/files/assets/uca/unimarc_updates/BIBLIOGRAPHIC/u-b_reclabl_update.pdf
 */
public enum TypeOfRecord {

    LANGUAGE_MATERIAL('a'),
    LANGUAGE_MATERIAL_MANUSCRIPT('b'),
    NOTATED_MUSIC('c'),
    NOTATED_MUSIC_MANUSCRIPT('d'),
    CARTOGRAPHIC_MATERIAL('e'),
    CARTOGRAPHIC_MATERIAL_MANUSCRIPT('f'),
    PROJECTED_MEDIUM('g'),
    NONMUSICAL_SOUND_RECORDING('i'),
    MUSICAL_SOUND_RECORDING('j'),
    PICTURE('k'),
    ELECTRONIC_RESOURCE('l'),
    COMPUTER_FILE('m'),
    KIT('o'),
    MIXED_MATERIALS('p'),
    ARTIFACT('r'),
    LANGUAGE_MATERIAL_MANUSCRIPT_MARC21('t')
    ;

    char ch;

    TypeOfRecord(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }

}
