package org.grobid.core.document;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.layout.*;
import org.grobid.core.utilities.TextUtilities;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for building basic structures in a document item.
 *
 * @author Patrice Lopez
 */
public class BasicStructureBuilder {
//    final static public Pattern CLEANER = Pattern.compile("[&%\\$#%@\\+=\\?\\*\\)\\(\\[\\]\\^]");

    static public Pattern introduction =
            Pattern.compile("^\\b*(Introduction?|Einleitung|INTRODUCTION|Acknowledge?ments?|Acknowledge?ment?|Background?|Content?|Contents?|Motivations?|1\\.\\sPROBLEMS?|1\\.(\\n)?\\sIntroduction?|1\\.\\sINTRODUCTION|I\\.(\\s)+Introduction|1\\.\\sProblems?|I\\.\\sEinleitung?|1\\.\\sEinleitung?|1\\sEinleitung?|1\\sIntroduction?)",
                    Pattern.CASE_INSENSITIVE);
    static public Pattern introductionStrict =
            Pattern.compile("^\\b*(1\\.\\sPROBLEMS?|1\\.(\\n)?\\sIntroduction?|1\\.(\\n)?\\sContent?|1\\.\\sINTRODUCTION|I\\.(\\s)+Introduction|1\\.\\sProblems?|I\\.\\sEinleitung?|1\\.\\sEinleitung?|1\\sEinleitung?|1\\sIntroduction?)",
                    Pattern.CASE_INSENSITIVE);

    //public Pattern introductionZFN2 = Pattern.compile("\\b(Introduction?|Einleitung|EINLEITUNG|INTRODUCTION|INTRODUCTION?|Acknowledge?ments?|Acknowledge?ment?|Background?|BACKGROUND?|Content?|Contents?|CONTENT?|CONTENTS?|Motivations?|MOTIVATIONS?|1\\. PROBLEMS?|1\\. Introduction?|I\\. Introduction|1\\. Problems?|I\\. Einleitung|I\\. EINLEITUNG|1\\. Einleitung|1\\. EINLEITUNG)");

    static public Pattern abstract_ = Pattern.compile("^\\b*\\.?(abstract?|résumé?|summary?|zusammenfassung?)",
            Pattern.CASE_INSENSITIVE);
    static public Pattern keywords = Pattern.compile("^\\b*\\.?(keyword?|key\\s*word?|mots\\s*clefs?)",
            Pattern.CASE_INSENSITIVE);

    static public Pattern references =
            Pattern.compile("^\\b*(References?|REFERENCES?|Bibliography|BIBLIOGRAPHY|References?\\s+and\\s+Notes?|References?\\s+Cited|REFERENCE?\\s+CITED|REFERENCES?\\s+AND\\s+NOTES?|Références)", Pattern.CASE_INSENSITIVE);
    static public Pattern header = Pattern.compile("^((\\d\\d?)|([A-Z](I|V|X)*))(\\.(\\d)*)*\\s(\\D+)");
//    static public Pattern header2 = Pattern.compile("^\\d\\s\\D+");
    static public Pattern figure = Pattern.compile("(figure\\s|fig\\.|sch?ma)", Pattern.CASE_INSENSITIVE);
    static public Pattern table = Pattern.compile("^(T|t)able\\s|tab|tableau", Pattern.CASE_INSENSITIVE);
    static public Pattern equation = Pattern.compile("^(E|e)quation\\s");
    static public Pattern acknowledgement = Pattern.compile("(acknowledge?ments?|acknowledge?ment?)",
            Pattern.CASE_INSENSITIVE);
    static public Pattern headerNumbering1 = Pattern.compile("^(\\d+)\\.?\\s");
    static public Pattern headerNumbering2 = Pattern.compile("^((\\d+)\\.)+(\\d+)\\s");
    static public Pattern headerNumbering3 = Pattern.compile("^((\\d+)\\.)+\\s");
    static public Pattern headerNumbering4 = Pattern.compile("^([A-Z](I|V|X)*(\\.(\\d)*)*\\s)");
//    static public Pattern enumeratedList = Pattern.compile("^|\\s(\\d+)\\.?\\s");

    public static Pattern startNum = Pattern.compile("^(\\d)+\\s");
    public static Pattern endNum = Pattern.compile("\\s(\\d)+$");

    /**
     * Filter out line numbering possibly present in the document. This can be frequent for
     * document in a review/submission format and degrades strongly the machine learning
     * extraction results.
     * @param doc a document
     * @return if found numbering
     */
    public boolean filterLineNumber(Document doc) {
        // we first test if we have a line numbering by checking if we have an increasing integer
        // at the begin or the end of each block
        boolean numberBeginLine = false;
        boolean numberEndLine = false;

        boolean foundNumbering = false;

        int currentNumber = -1;
        int lastNumber = -1;
        int i = 0;
        for (Block block : doc.blocks) {
//            Integer ii = i;

            String localText = block.getText();
            ArrayList<LayoutToken> tokens = block.tokens;

            if ((localText != null) && (tokens != null)) {
                if (tokens.size() > 0) {
                    // we get the first and last token iof the block
                    //String tok1 = tokens.get(0).getText();
                    //String tok2 = tokens.get(tokens.size()).getText();
                    localText = localText.trim();

                    Matcher ma1 = startNum.matcher(localText);
                    Matcher ma2 = endNum.matcher(localText);

                    if (ma1.find()) {
                        String groupStr = ma1.group(0);
                        try {
                            currentNumber = Integer.parseInt(groupStr);
                            numberBeginLine = true;
                        } catch (NumberFormatException e) {
                            currentNumber = -1;
                        }
                    } else if (ma2.find()) {
                        String groupStr = ma2.group(0);
                        try {
                            currentNumber = Integer.parseInt(groupStr);
                            numberEndLine = true;
                        } catch (NumberFormatException e) {
                            currentNumber = -1;
                        }
                    }

                    if (lastNumber != -1) {
                        if (currentNumber == lastNumber + 1) {
                            foundNumbering = true;
                            break;
                        }
                    } else
                        lastNumber = currentNumber;
                }
            }
            i++;

            if (i > 5) {
                break;
            }
        }

        i = 0;
        if (foundNumbering) {
            // we have a line numbering, so we filter them
            int counter = 1; // we start at 1, if the actual start is 0,
            // it will remain (as it is negligeable)

            for (Block block : doc.blocks) {

                String localText = block.getText();
                ArrayList<LayoutToken> tokens = block.tokens;

                if ((localText != null) && (tokens.size() > 0)) {

                    if (numberEndLine) {
                        Matcher ma2 = endNum.matcher(localText);

                        if (ma2.find()) {
                            String groupStr = ma2.group(0);
                            if (groupStr.trim().equals("" + counter)) {
                                localText = localText.substring(0, localText.length() - groupStr.length());
                                block.setText(localText);
                                tokens.remove(tokens.size() - 1);
                                counter++;
                            }
                        }

                    } else if (numberBeginLine) {
                        Matcher ma1 = endNum.matcher(localText);

                        if (ma1.find()) {
                            String groupStr = ma1.group(0);
                            if (groupStr.trim().equals("" + counter)) {
                                localText = localText.substring(groupStr.length(), localText.length() - 1);
                                block.setText(localText);
                                tokens.remove(0);
                                counter++;
                            }
                        }

                    }
                }
                i++;
            }
        }

        return foundNumbering;
    }

    /**
     * First pass to detect basic structures: remove page header/footer, identify section numbering,
     * identify Figure and table blocks.
     * @param doc a document
     */
    static public void firstPass(Document doc)  {
        if (doc == null) {
            throw new NullPointerException();
        }
        if (doc.blocks == null) {
            throw new NullPointerException();
        }

        int i = 0;
//        boolean first = true;
        ArrayList<Integer> blockHeaders = new ArrayList<Integer>();
        ArrayList<Integer> blockFooters = new ArrayList<Integer>();
        ArrayList<Integer> blockSectionTitles = new ArrayList<Integer>();
        ArrayList<Integer> acknowledgementBlocks = new ArrayList<Integer>();
        ArrayList<Integer> blockTables = new ArrayList<Integer>();
        ArrayList<Integer> blockFigures = new ArrayList<Integer>();
        ArrayList<Integer> blockHeadTables = new ArrayList<Integer>();
        ArrayList<Integer> blockHeadFigures = new ArrayList<Integer>();
        ArrayList<Integer> blockDocumentHeaders = new ArrayList<Integer>();

        doc.titleMatchNum = false;

        try {
            for (Block block : doc.blocks) {
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();

                Matcher ma1 = BasicStructureBuilder.introduction.matcher(localText);
                Matcher ma2 = BasicStructureBuilder.references.matcher(localText);

                if ((ma1.find()) || (ma2.find())) {
                    if (((localText.startsWith("1.")) || (localText.startsWith("1 "))) ||
                            ((localText.startsWith("2.")) || (localText.startsWith("2 "))) ||
                            (localText.startsWith("Contents")))
                        doc.titleMatchNum = true;
                    //System.out.println("Title section identified: block " + i + ", " + localText);
                    blockSectionTitles.add(i);
                } else {
                    StringTokenizer st = new StringTokenizer(localText, "\n");
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();

                        if (token.startsWith("@PAGE")) {
                            // current block should give the header/footors
                            if (i > 4) {
                                if (doc.blocks.get(i - 5).getNbTokens() < 20) {
                                    Integer i2 = i - 5;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            if (i > 3) {
                                if (doc.blocks.get(i - 4).getNbTokens() < 20) {
                                    Integer i2 = i - 4;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            if (i > 2) {
                                if (doc.blocks.get(i - 3).getNbTokens() < 20) {
                                    Integer i2 = i - 3;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            if (i > 1) {
                                if (doc.blocks.get(i - 2).getNbTokens() < 20) {
                                    Integer i2 = i - 2;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            if (i > 0) {
                                if (doc.blocks.get(i - 1).getNbTokens() < 20) {
                                    Integer i2 = i - 1;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            blockFooters.add(i);

                            // page header candidates
                            blockHeaders.add(i);
                            if (i < doc.blocks.size() - 1) {
                                if (doc.blocks.get(i + 1).getNbTokens() < 20) {
                                    Integer i2 = i + 1;
                                    if (!blockHeaders.contains(i2))
                                        blockHeaders.add(i + 1);
                                }
                            }
                            if (i < doc.blocks.size() - 2) {
                                if (doc.blocks.get(i + 2).getNbTokens() < 20) {
                                    Integer i2 = i + 2;
                                    if (!blockHeaders.contains(i2))
                                        blockHeaders.add(i + 2);
                                }
                            }
                            if (i < doc.blocks.size() - 3) {
                                if (doc.blocks.get(i + 3).getNbTokens() < 20) {
                                    Integer i2 = i + 3;
                                    if (!blockHeaders.contains(i2))
                                        blockHeaders.add(i + 3);
                                }
                            }
                            if (i < doc.blocks.size() - 4) {
                                if (doc.blocks.get(i + 4).getNbTokens() < 20) {
                                    Integer i2 = i + 4;
                                    if (!blockHeaders.contains(i2))
                                        blockHeaders.add(i + 4);
                                }
                            }
                            // more ??
                        }

                    }
                }

                // clustering of blocks per font (for section header and figure/table detections)
                addBlockToCluster(i, doc);

                i++;
            }

            // try to find the cluster of section titles
            Cluster candidateCluster = null;
            //System.out.println("nb clusters: " + clusters.size());
            for (Cluster cluster : doc.clusters) {
                if ((cluster.getNbBlocks() < (doc.blocks.size() / 5)) && (cluster.getNbBlocks() < 20)) {
                    ArrayList<Integer> blo = cluster.getBlocks2();
                    for (Integer b : blo) {
                        if (blockSectionTitles.contains(b)) {
                            if (candidateCluster == null) {
                                candidateCluster = cluster;
                                break;
                            }
                            //else if (cluster.getFontSize() >= candidateCluster.getFontSize())
                            //	candidateCluster = cluster;
                        }
                    }
                }
            }
            if (candidateCluster != null) {
                ArrayList<Integer> newBlockSectionTitles = new ArrayList<Integer>();
                for (Integer bl : blockSectionTitles) {
                    if (!newBlockSectionTitles.contains(bl))
                        newBlockSectionTitles.add(bl);
                }

                ArrayList<Integer> blockClusterTitles = candidateCluster.getBlocks2();
                if (blockClusterTitles.size() < 20) {
                    for (Integer bl : blockClusterTitles) {
                        if (!newBlockSectionTitles.contains(bl))
                            newBlockSectionTitles.add(bl);
                    }
                }

                blockSectionTitles = newBlockSectionTitles;
            }

            // aknowledgement section recognition
            boolean ackn = false;
            i = 0;
            for (Block block : doc.blocks) {
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();

                //System.out.println(i + ": " + localText+"\n");

                Integer iii = i;
                Matcher m3 = BasicStructureBuilder.acknowledgement.matcher(localText);
                if ((m3.find()) && (blockSectionTitles.contains(iii))) {
                    acknowledgementBlocks.add(iii);
                    ackn = true;
                    //int index = blockSectionTitles.indexOf(iii);
                    //blockSectionTitles.remove(index);
                } else if ((ackn) && (blockSectionTitles.contains(iii))) {
                    ackn = false;
                    break;
                } else if (ackn) {
                    Matcher m4 = BasicStructureBuilder.references.matcher(localText);
                    if ((ackn) && (!blockFooters.contains(iii)) && (!m4.find())) {
                        acknowledgementBlocks.add(iii);
                    } else if (m4.find()) {
                        ackn = false;
                        break;
                    }
                }
                i++;
            }

            // we remove references headers in blockSectionTitles
            int index = -1;
            for (Integer ii : blockSectionTitles) {
                Block block = doc.blocks.get(ii.intValue());
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();
                Matcher m4 = BasicStructureBuilder.references.matcher(localText);
                if (m4.find()) {
                    index = blockSectionTitles.indexOf(ii);
                    break;
                }
            }
            if (index != -1) {
                blockSectionTitles.remove(index);
            }

            // we check headers repetition from page to page to decide if it is an header or not
            ArrayList<Integer> toRemove = new ArrayList<Integer>();
            for (Integer ii : blockHeaders) {
                String localText = (doc.blocks.get(ii.intValue())).getText().trim();
                localText = TextUtilities.shadowNumbers(localText);
                int length = localText.length();
                if (length > 160)
                    toRemove.add(ii);
                else {
                    //System.out.println("header candidate: " + localText);
                    // evaluate distance with other potential headers
                    boolean valid = false;
                    for (Integer ii2 : blockHeaders) {
                        if (ii.intValue() != ii2.intValue()) {
                            String localText2 = doc.blocks.get(ii2.intValue()).getText().trim();
                            if (localText2.length() < 160) {
                                localText2 = TextUtilities.shadowNumbers(localText2);
                                double dist = (double) TextUtilities.getLevenshteinDistance(localText, localText2) / length;
                                //System.out.println("dist with " + localText2 + " : " + dist);
                                if (dist < 0.25) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!valid) {
                        toRemove.add(ii);
                    }
                }
            }

            for (Integer ii : toRemove) {
                blockHeaders.remove(ii);
            }

            // same for footers
            toRemove = new ArrayList<Integer>();
            for (Integer ii : blockFooters) {
                String localText = (doc.blocks.get(ii.intValue())).getText().trim();
                localText = TextUtilities.shadowNumbers(localText);
                int length = localText.length();
                if (length > 160)
                    toRemove.add(ii);
                else {
                    //System.out.println("footer candidate: " + localText);
                    // evaluate distance with other potential headers
                    boolean valid = false;
                    for (Integer ii2 : blockFooters) {
                        if (ii.intValue() != ii2.intValue()) {
                            String localText2 = doc.blocks.get(ii2.intValue()).getText().trim();
                            if (localText2.length() < 160) {
                                localText2 = TextUtilities.shadowNumbers(localText2);
                                double dist = (double) TextUtilities.getLevenshteinDistance(localText, localText2) / length;
                                if (dist < 0.25) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!valid) {
                        toRemove.add(ii);
                    }
                }
            }

            for (Integer ii : toRemove) {
                blockFooters.remove(ii);
            }

            // a special step for added banner repositoryies such HAL
            i = 0;
            for (Block block : doc.blocks) {
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();

                //HAL
                if (localText.startsWith("Author manuscript, published in")) {
                    Double y = block.getY();
                    //System.out.println("HAL banner found, " + "block " + i + ", y = " + y);
                    if (Math.abs(y - 12.538) < 2) { // reference position
                        //blockHeaders.add(new Integer(i));
                        blockDocumentHeaders.add(i);
                        //System.out.println("HAL banner added as header block");
                        break;
                    }
                }

                // ACM publications
                //System.out.println("test ACM " + i);
                //System.out.println(localText);
                if (localText.startsWith("Permission to make digital or hard copies")) {
                    blockFooters.add(i);
                    break;
                }

                // arXiv, etc. put here
                // IOP

                if (localText.startsWith("Confidential: ") && (localText.contains("IOP"))) {
                    blockDocumentHeaders.add(i);
                    //System.out.println("IOP banner added as header block");
                    break;
                }
                i++;
            }

            // we try to recognize here table and figure blocks
            // the idea is that the textual elements are not located as the normal text blocks
            // this is recognized by exploiting the cluster of blocks starting up and down front the block
            // containing a table or a figure marker
            // two different runs, one for figures and one for tables (everything could be done in one step)
            i = 0;
            for (Block block : doc.blocks) {
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();

                Matcher m = BasicStructureBuilder.figure.matcher(localText);
                Matcher m2 = BasicStructureBuilder.table.matcher(localText);

                double width = block.getWidth();
                boolean bold = block.getBold();

                // table
                //if ( (m2.find()) && (localText.length() < 200) ) {
                if ((m2.find()) && ((bold) || (localText.length() < 200))) {
                    if (!blockHeadTables.contains(i)) {
                        blockHeadTables.add(i);
                    }
                    // we also put all the small blocks before and after the marker
                    int j = i - 1;
                    while ((j > i - 15) && (j > 0)) {
                        Block b = doc.blocks.get(j);
                        if (b.getText() != null) {
                            if ((b.getText().length() < 160) || (width < 50)) {
                                if ((!blockTables.contains(j)) && (!blockSectionTitles.contains(j)) &&
                                        (!blockHeaders.contains(j)) && (!blockFooters.contains(j))
                                        )
                                    blockTables.add(j);
                            } else
                                j = 0;
                        }
                        j--;
                    }

                    j = i + 1;
                    while ((j < i + 15) && (j < doc.blocks.size())) {
                        Block b = doc.blocks.get(j);
                        if (b.getText() != null) {
                            if ((b.getText().length() < 160) || (width < 50)) {
                                if ((!blockTables.contains(j)) && (!blockSectionTitles.contains(j)) &&
                                        (!blockHeaders.contains(j)) && (!blockFooters.contains(j))
                                        )
                                    blockTables.add(j);
                            } else
                                j = doc.blocks.size();
                        }
                        j++;
                    }
                }
                // figure
                //else if ( (m.find()) && (localText.length() < 200) ) {
                else if ((m.find()) && ((bold) || (localText.length() < 200))) {
                    if (!blockHeadFigures.contains(i))
                        blockHeadFigures.add(i);
                    // we also put all the small blocks before and after the marker
                    int j = i - 1;
                    boolean imageFound = false;
                    while ((j > i - 15) && (j > 0)) {
                        Block b = doc.blocks.get(j);

                        if (b.getText() != null) {
                            String localText2 = b.getText().trim();
                            //localText = localText.replace("\n", " ");
                            localText2 = localText2.replace("  ", " ");
                            localText2 = localText2.trim();

                            if ((localText2.startsWith("@IMAGE")) && (!imageFound)) {
                                //System.out.println(localText2);
                                block.setText(block.getText() + " " + localText2);
                                //System.out.println(block.getText());
                                imageFound = true;
                            }

                            if ((localText2.length() < 160) || (width < 50)) {
                                if ((!blockFigures.contains(j)) && (!blockSectionTitles.contains(j)) &&
                                        (!blockHeaders.contains(j)) && (!blockFooters.contains(j))
                                        )
                                    blockFigures.add(j);
                            } else
                                j = 0;
                        }
                        j--;
                    }

                    j = i + 1;
                    while ((j < i + 15) && (j < doc.blocks.size())) {
                        Block b = doc.blocks.get(j);
                        if (b.getText() != null) {
                            if ((b.getText().trim().length() < 160) || (width < 50)) {
                                if ((!blockFigures.contains(j)) && (!blockSectionTitles.contains(j)) &&
                                        (!blockHeaders.contains(j)) && (!blockFooters.contains(j))
                                        )
                                    blockFigures.add(j);
                            } else
                                j = doc.blocks.size();
                        }
                        j++;
                    }
                }
                i++;
            }
        } finally {
            doc.blockHeaders = blockHeaders;
            doc.blockFooters = blockFooters;
            doc.blockSectionTitles = blockSectionTitles;
            doc.acknowledgementBlocks = acknowledgementBlocks;
            doc.blockTables = blockTables;
            doc.blockFigures = blockFigures;
            doc.blockHeadTables = blockHeadTables;
            doc.blockHeadFigures = blockHeadFigures;
            doc.blockDocumentHeaders = blockDocumentHeaders;
        }
    }

    /**
     * Cluster the blocks following the font, style and size aspects
     * @param b integer
     * @param doc a document
     */
    static public void addBlockToCluster(Integer b, Document doc) {
        // get block features
        Block block = doc.blocks.get(b.intValue());
        String font = block.getFont();
        boolean bold = block.getBold();
        boolean italic = block.getItalic();
        double fontSize = block.getFontSize();
        boolean found = false;

        if (font == null) {
            font = "unknown";
        }
        //System.out.println(font + " " + bold + " " + italic + " " + fontSize );

        if (doc.clusters == null) {
            doc.clusters = new ArrayList<Cluster>();
        } else {
            for (Cluster cluster : doc.clusters) {
                String font2 = cluster.getFont();
                if (font2 == null)
                    font2 = "unknown";
                if (font.equals(font2) &&
                        (bold == cluster.getBold()) &
                                (italic == cluster.getItalic()) &
                                (fontSize == cluster.getFontSize())) {
                    cluster.addBlock2(b);
                    found = true;
                }
            }
        }

        if (!found) {
            Cluster cluster = new Cluster();
            cluster.setFont(font);
            cluster.setBold(bold);
            cluster.setItalic(italic);
            cluster.setFontSize(fontSize);
            cluster.addBlock2(b);
            doc.clusters.add(cluster);
        }

    }

    /**
     * Set the main segments of the document based on the full text parsing results
     * @param doc a document
     * @param rese string
     * @param tokenizations tokens
     * @return a document
     */
    static public Document resultSegmentation(Document doc,
                                              String rese,
                                              ArrayList<String> tokenizations) {
        if (doc == null) {
            throw new NullPointerException();
        }
        if (doc.blocks == null) {
            throw new NullPointerException();
        }
        //System.out.println(tokenizations.toString());
//        int i = 0;
//        boolean first = true;
        ArrayList<Integer> blockHeaders = new ArrayList<Integer>();
        ArrayList<Integer> blockFooters = new ArrayList<Integer>();
        ArrayList<Integer> blockDocumentHeaders = new ArrayList<Integer>();
        ArrayList<Integer> blockReferences = new ArrayList<Integer>();
        ArrayList<Integer> blockSectionTitles = new ArrayList<Integer>();

        doc.bibDataSets = new ArrayList<BibDataSet>();

        StringTokenizer st = new StringTokenizer(rese, "\n");
        String s1 = null;
        String s2 = null;
        String lastTag = null;
        int p = 0; // index in the results' tokenization (st)
        int blockIndex = 0;

        BibDataSet bib = null;

        while (st.hasMoreTokens()) {

            for (; blockIndex < doc.blocks.size() - 1; blockIndex++) {
//                int startTok = doc.blocks.get(blockIndex).getStartToken();
                int endTok = doc.blocks.get(blockIndex).getEndToken();

                if (endTok >= p) {
                    break;
                }
            }

            boolean addSpace = false;
            String tok = st.nextToken().trim();

            StringTokenizer stt = new StringTokenizer(tok, " \t");
            ArrayList<String> localFeatures = new ArrayList<String>();
            int j = 0;

            boolean newLine = false;
            int ll = stt.countTokens();
            while (stt.hasMoreTokens()) {
                String s = stt.nextToken().trim();
                if (j == 0) {
                    //s2 = TextUtilities.HTMLEncode(s); // lexical token
                    s2 = s;

                    boolean strop = false;
                    while ((!strop) && (p < tokenizations.size())) {
                        String tokOriginal = tokenizations.get(p);
                        if (tokOriginal.equals(" ")
                                | tokOriginal.equals("\n")
                                | tokOriginal.equals("\r")
                                | tokOriginal.equals("\t")) {
                            addSpace = true;
                            p++;
                        } else if (tokOriginal.equals("")) {
                            p++;
                        } else //if (tokOriginal.equals(s))
                        {
                            strop = true;
                        }

                    }
                } else if (j == ll - 1) {
                    s1 = s; // current tag
                } else {
                    if (s.equals("LINESTART"))
                        newLine = true;
                    localFeatures.add(s);
                }
                j++;
            }
            String lastTag0 = null;
            if (lastTag != null) {
                if (lastTag.startsWith("I-")) {
                    lastTag0 = lastTag.substring(2, lastTag.length());
                } else {
                    lastTag0 = lastTag;
                }
            }
            String currentTag0 = null;
            if (s1 != null) {
                if (s1.startsWith("I-")) {
                    currentTag0 = s1.substring(2, s1.length());
                } else {
                    currentTag0 = s1;
                }
            }

            if (currentTag0.equals("<header>")) {
                if (!blockDocumentHeaders.contains(blockIndex)) {
                    blockDocumentHeaders.add(blockIndex);
                    //System.out.println("add block header: " + blockIndexInteger.intValue());
                }
            } else if (currentTag0.equals("<reference>")) {
                if (!blockReferences.contains(blockIndex)) {
                    blockReferences.add(blockIndex);
                    //System.out.println("add block reference: " + blockIndexInteger.intValue());
                }

                if (s1.equals("I-<reference>")) {
                    if (bib != null) {
                        if (bib.getRawBib() != null) {
                            doc.bibDataSets.add(bib);
                            bib = new BibDataSet();
                        }
                    } else {
                        bib = new BibDataSet();
                    }
                    bib.setRawBib(s2);
                } else {
                    if (addSpace) {
                        if (bib == null) {
                            bib = new BibDataSet();
                            bib.setRawBib(" " + s2);
                        } else {
                            bib.setRawBib(bib.getRawBib() + " " + s2);
                        }
                    } else {
                        if (bib == null) {
                            bib = new BibDataSet();
                            bib.setRawBib(s2);
                        } else {
                            bib.setRawBib(bib.getRawBib() + s2);
                        }
                    }
                }
            } else if (currentTag0.equals("<reference_marker>")) {
				if (!blockReferences.contains(blockIndex)) {
                    blockReferences.add(blockIndex);
                    //System.out.println("add block reference: " + blockIndexInteger.intValue());
                }
	
                if (s1.equals("I-<reference_marker>")) {
                    if (bib != null) {
                        if (bib.getRefSymbol() != null) {
                            doc.bibDataSets.add(bib);
                            bib = new BibDataSet();
                        }
                    } else {
                        bib = new BibDataSet();
                    }
                    bib.setRefSymbol(s2);
                } else {
                    if (addSpace) {
                        if (bib == null) {
                            bib = new BibDataSet();
                            bib.setRefSymbol(s2);
                        } else {
                            bib.setRefSymbol(bib.getRefSymbol() + " " + s2);
                        }
                    } else {
                        if (bib == null) {
                            bib = new BibDataSet();
                            bib.setRefSymbol(s2);
                        } else {
                            bib.setRefSymbol(bib.getRefSymbol() + s2);
                        }
                    }
                }
            } else if (currentTag0.equals("<page_footnote>")) {
                if (!blockFooters.contains(blockIndex)) {
                    blockFooters.add(blockIndex);
                    //System.out.println("add block foot note: " + blockIndexInteger.intValue());
                }
            } else if (currentTag0.equals("<page_header>")) {
                if (!blockHeaders.contains(blockIndex)) {
                    blockHeaders.add(blockIndex);
                    //System.out.println("add block page header: " + blockIndexInteger.intValue());
                }
            } else if (currentTag0.equals("<section>")) {
                if (!blockSectionTitles.contains(blockIndex)) {
                    blockSectionTitles.add(blockIndex);
                    //System.out.println("add block page header: " + blockIndexInteger.intValue());
                }
            }

            lastTag = s1;
            p++;
        }

        if (bib != null) {
            doc.bibDataSets.add(bib);
        }

        doc.blockHeaders = blockHeaders;
        doc.blockFooters = blockFooters;
        doc.blockDocumentHeaders = blockDocumentHeaders;
        doc.blockReferences = blockReferences;
        doc.blockSectionTitles = blockSectionTitles;

        return doc;
    }

}