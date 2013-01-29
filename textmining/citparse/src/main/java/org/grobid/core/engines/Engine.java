/**
 * Copyright 2008-2011 P. Lopez and the authors 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grobid.core.engines;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.grobid.core.data.Affiliation;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.BiblioSet;
import org.grobid.core.data.ChemicalEntity;
import org.grobid.core.data.PatentItem;
import org.grobid.core.data.Person;
import org.grobid.core.document.Document;
import org.grobid.core.engines.entities.ChemicalParser;
import org.grobid.core.engines.patent.ReferenceExtractor;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.lang.Language;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing the extraction of bibliographical informations from PDF
 * documents or raw text.
 * 
 * @author Patrice Lopez
 */
public class Engine implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);

	// path where the pdf file is stored
	public String path = null;

	// Name of the pdf file
	public String fileName = null;

	private AuthorParser authorParser = null;
	private AffiliationAddressParser affiliationAddressParser = null;
	private HeaderParser headerParser = null;
	private DateParser dateParser = null;
	private CitationParser citationParser = null;
	private FullTextParser fullTextParser = null;
	private ReferenceExtractor referenceExtractor = null;
	private ChemicalParser chemicalParser = null;

	// Identified parsed bibliographical items and related information
	public List<org.grobid.core.data.BibDataSet> resBib;

	// Identified parsed bibliographical item from raw text
	public BiblioItem resRef;

	// identified parsed bibliographical data header
	public BiblioItem resHeader;

	// The list of accepted languages
	// the languages are encoded in ISO 3166
	// if null, all languages are accepted.
	private List<String> acceptedLanguages = null;

	// The document representation, including layout information
	private Document doc = null;

	// return the implemented representation of the currently processed document
	public Document getDocument() {
		return doc;
	}

	/**
	 * Parse a sequence of authors from a header, i.e. containing possibly
	 * reference markers.
	 * 
	 * @param authorSequence
	 *            - the string corresponding to a raw sequence of names
	 * @return the list of structured author object
	 */
	public List<Person> processAuthorsHeader(String authorSequence) throws Exception {
		List<String> inputs = new ArrayList<String>();
		inputs.add(authorSequence);
		if (authorParser == null) {
			authorParser = new AuthorParser();
		}
		List<Person> result = authorParser.processingHeader(inputs);
		close();
		return result;
	}

	/**
	 * Parse a sequence of authors from a citation, i.e. containing no reference
	 * markers.
	 * 
	 * @param authorSequence
	 *            - the string corresponding to a raw sequence of names
	 * @return the list of structured author object
	 */
	public List<Person> processAuthorsCitation(String authorSequence) throws Exception {
		List<String> inputs = new ArrayList<String>();
		inputs.add(authorSequence);
		if (authorParser == null) {
			authorParser = new AuthorParser();
		}
		List<Person> result = authorParser.processingCitation(inputs);
		close();
		return result;
	}

	/**
	 * Parse a list of independent sequences of authors from citations.
	 * 
	 * @param authorSequences
	 *            - the list of strings corresponding each to a raw sequence of
	 *            names.
	 * @return the list of all recognized structured author objects for each
	 *         sequence of authors.
	 */
	public List<List<Person>> processAuthorsCitationLists(List<String> authorSequences) throws Exception {
		return null;
	}

	/**
	 * Parse a text block corresponding to an affiliation+address.
	 * 
	 * @param addressBlock
	 *            - the string corresponding to a raw affiliation+address
	 * @return the list of all recognized structured affiliation objects.
	 * @throws IOException
	 */
	public List<Affiliation> processAffiliation(String addressBlock) throws IOException {
		if (affiliationAddressParser == null) {
			affiliationAddressParser = new AffiliationAddressParser();
		}
		List<Affiliation> result = affiliationAddressParser.processing(addressBlock);
		return result;
	}

	/**
	 * Parse a list of text blocks corresponding to an affiliation+address.
	 * 
	 * @param addressBlocks
	 *            - the list of strings corresponding each to a raw
	 *            affiliation+address.
	 * @return the list of all recognized structured affiliation objects for
	 *         each sequence of affiliation + address block.
	 */
	public List<List<Affiliation>> processAffiliations(List<String> addressBlocks) throws Exception {
		if (affiliationAddressParser == null) {
			affiliationAddressParser = new AffiliationAddressParser();
		}
		List<List<Affiliation>> results = null;
		for (String addressBlock : addressBlocks) {
			List<Affiliation> localRes = affiliationAddressParser.processing(addressBlock);
			if (results == null) {
				results = new ArrayList<List<Affiliation>>();
			}
			results.add(localRes);
		}
		return results;
	}

	/**
	 * Parse a raw string containing dates.
	 * 
	 * @param dateBlock
	 *            - the string containing raw dates.
	 * @return the list of all structured date objects recognized in the string.
	 * @throws IOException
	 */
	public List<org.grobid.core.data.Date> processDate(String dateBlock) throws IOException {
		if (dateParser == null) {
			dateParser = new DateParser();
		}
		List<org.grobid.core.data.Date> result = dateParser.processing(dateBlock);
		close();
		return result;
	}

	/**
	 * Parse a list of raw dates.
	 * 
	 * @param dateBlocks
	 *            - the list of strings each containing raw dates.
	 * @return the list of all structured date objects recognized in the string
	 *         for each inputed string.
	 */
	public List<List<org.grobid.core.data.Date>> processDates(List<String> dateBlocks) {
		return null;
	}

	/**
	 * Apply a parsing model for a given single raw reference string based on
	 * CRF
	 * 
	 * @param reference
	 *            : the reference string to be processed
	 * @param consolidate
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving header information
	 * @return the recognized bibliographical object
	 */
	public BiblioItem processRawReference(String reference, boolean consolidate) {
		if (reference != null) {
			reference = reference.replaceAll("\\\\", "");
		}
		if (citationParser == null) {
			citationParser = new CitationParser();
		}
		return citationParser.processing(reference, consolidate);
	}

	/**
	 * Apply a parsing model for a set of raw reference text based on CRF
	 * 
	 * @param references
	 *            : the list of raw reference string to be processed
	 * @param consolidate
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving header information
	 * @return the list of recognized bibliographical objects
	 */
	public List<BiblioItem> processRawReferences(List<String> references, boolean consolidate) throws Exception {
		if (references == null)
			return null;
		if (references.size() == 0)
			return null;
		if (citationParser == null) {
			citationParser = new CitationParser();
		}
		List<BiblioItem> results = new ArrayList<BiblioItem>();
		for (String reference : references) {
			BiblioItem bit = citationParser.processing(reference, consolidate);
			results.add(bit);
		}
		return results;
	}

	/**
	 * Return an object representing the bibliographical information of the
	 * header of the current document. The extraction and parsing of the header
	 * of the document must have been done to get an instanciated object.
	 * 
	 * @return BiblioItem representing the cibliographical information of the
	 *         header of the current document.
	 */
	public BiblioItem getResHeader() {
		return resHeader;
	}

	/**
	 * Set the path of the current document to be processed.
	 */
	public void setDocumentPath(String dirName) {
		path = dirName;
	}

	/**
	 * Set the name of the current document file to be processed.
	 */
	public void setDocumentFile(String fName) {
		fileName = fName;
	}

	/**
	 * Constructor for the Grobid engine instance.
	 */
	public Engine() {
		/*
		 * Runtime.getRuntime().addShutdownHook(new Thread() {
		 * 
		 * @Override public void run() { try { close(); } catch (IOException e)
		 * { LOGGER.error("Failed to close all resources: " + e); } } });
		 */
	}

	/**
	 * Apply a parsing model to the reference block of a PDF file based on CRF
	 * 
	 * @param inputFile
	 *            : the path of the PDF file to be processed
	 * @param consolidate
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving header information
	 * @return the list of parsed references as bibliographical objects enriched
	 *         with citation contexts
	 */
	public List<BibDataSet> processReferences(String inputFile, boolean consolidate) throws Exception {
		if (citationParser == null) {
			citationParser = new CitationParser();
		}
		List<org.grobid.core.data.BibDataSet> bits = citationParser.processingReferenceSection(inputFile, consolidate);
		return bits;
	}

	/**
	 * Download a PDF file.
	 * 
	 * @param url
	 *            URL of the PDF to download
	 * @param dirName
	 *            directory where to store the downloaded PDF
	 * @param name
	 *            name of the file to save the downloaded PDF
	 */
	public void downloadPDF(String url, String dirName, String name) {
		path = dirName;
		fileName = Utilities.uploadFile(url, path, name);
	}

	/**
	 * Give the list of languages for which an extraction is allowed. If null,
	 * any languages will be processed
	 * 
	 * @return the list of languages to be processed coded in ISO 3166.
	 */
	public List<String> getAcceptedLanguages() {
		return acceptedLanguages;
	}

	/**
	 * Add a language to the list of accepted languages.
	 * 
	 * @param lang
	 *            the language in ISO 3166 to be added
	 */
	public void addAcceptedLanguages(String lang) {
		if (acceptedLanguages == null) {
			acceptedLanguages = new ArrayList<String>();
		}
		acceptedLanguages.add(lang);
	}

	/**
	 * Perform a language identification
	 * 
	 * @param ext
	 *            part
	 * @return language
	 */
	public Language runLanguageId(String ext) {
		try {
			// we just skip the 50 first lines and get the next approx. 5000
			// first characters,
			// which should give a ~100% accuracy for the supported languages
			String text = "";
			FileInputStream fileIn = new FileInputStream(path + fileName.substring(0, fileName.length() - 3) + ext);
			InputStreamReader reader = new InputStreamReader(fileIn, "UTF-8");
			BufferedReader bufReader = new BufferedReader(reader);
			String line;
			// int nbLine = 0;
			int nbChar = 0;
			while (((line = bufReader.readLine()) != null) && (nbChar < 5000)) {
				if (line.length() == 0)
					continue;
				text += " " + line;
				nbChar += line.length();
			}
			bufReader.close();
			LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
			return languageUtilities.runLanguageId(text);
		} catch (IOException e) {
			throw new GrobidException("An exception occurred while running Grobid.", e);
		}
	}

	/**
	 * Basic run for language identification, default is on the body of the
	 * current document.
	 * 
	 * @return language id
	 */
	public Language runLanguageId() {
		return runLanguageId("body");
	}

	/**
	 * Apply a parsing model for the header of a PDF file based on CRF, using
	 * first three pages of the PDF
	 * 
	 * @param inputFile
	 *            : the path of the PDF file to be processed
	 * @param consolidate
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving header information
	 * @param result
	 *            bib result
	 * @return the TEI representation of the extracted bibliographical
	 *         information
	 * @throws Exception
	 *             if sth went wrong
	 */
	public String processHeader(String inputFile, boolean consolidate, BiblioItem result) throws Exception {
		return processHeader(inputFile, consolidate, 0, 2, result);
	}

	/**
	 * Apply a parsing model for the header of a PDF file based on CRF, using
	 * dynamic range of pages as header
	 * 
	 * @param inputFile
	 *            : the path of the PDF file to be processed
	 * @param consolidate
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving header information
	 * @param startPage
	 *            : start page of range to use (0-based)
	 * @param endPage
	 *            : stop page of range to use (0-based)
	 * @param result
	 *            bib result
	 * @return the TEI representation of the extracted bibliographical
	 *         information
	 * @throws Exception
	 *             if sth went wrong
	 */
	public String processHeader(String inputFile, boolean consolidate, int startPage, int endPage, BiblioItem result) throws Exception {
		if (headerParser == null) {
			headerParser = new HeaderParser();
		}
		// normally the BiblioItem reference must not be null, but if it is the
		// case, we still continue
		// with a new instance, so that the resulting TEI string is still
		// delivered
		if (result == null) {
			result = new BiblioItem();
		}

		String resultTEI = headerParser.processing(inputFile, consolidate, result, startPage, endPage);
		doc = headerParser.getDoc();
		close();
		return resultTEI;
	}

	/**
	 * Create training data for the header model based on the application of the
	 * current header model on a new PDF
	 * 
	 * @param inputFile
	 *            : the path of the PDF file to be processed
	 * @param pathHeader
	 *            : the path where to put the header with layout features
	 * @param pathTEI
	 *            : the path where to put the annotated TEI representation (the
	 *            file to be corrected for gold-level training data)
	 * @param id
	 *            : an optional ID to be used in the TEI file and the header
	 *            file
	 */
	public void createTrainingHeader(String inputFile, String pathHeader, String pathTEI, int id) {
		if (headerParser == null) {
			headerParser = new HeaderParser();
		}
		headerParser.createTrainingHeader(inputFile, pathHeader, pathTEI);
		doc = headerParser.getDoc();
	}

	/**
	 * Create training data for the full text model based on the application of
	 * the current full text model on a new PDF
	 * 
	 * @param inputFile
	 *            : the path of the PDF file to be processed
	 * @param pathFullText
	 *            : the path where to put the full text with layout features
	 * @param pathTEI
	 *            : the path where to put the annotated TEI representation (the
	 *            file to be corrected for gold-level training data)
	 * @param id
	 *            : an optional ID to be used in the TEI file and the full text
	 *            file
	 */
	public void createTrainingFullText(String inputFile, String pathFullText, String pathTEI, int id) {
		if (fullTextParser == null) {
			fullTextParser = new FullTextParser();
		}
		fullTextParser.createTrainingFullText(inputFile, pathFullText, pathTEI, id);
		doc = fullTextParser.getDoc();
	}

	/**
	 * Parse and convert the current article into TEI, this method performs the
	 * whole parsing and conversion process. If onlyHeader is true, tean only
	 * the tei header data will be created.
	 * 
	 * @param inputFile
	 *            - absolute path to the pdf to be processed
	 * @param consolidateHeader
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving header information
	 * @param consolidateCitations
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving citations information
	 */
	public String fullTextToTEI(String inputFile, boolean consolidateHeader, boolean consolidateCitations) throws Exception {
		return fullTextToTEI(inputFile, consolidateHeader, consolidateCitations, 0);
	}

	/**
	 * Parse and convert the current article into TEI, this method performs the
	 * whole parsing and conversion process. If onlyHeader is true, than only
	 * the tei header data will be created.
	 * 
	 * @param inputFile
	 *            - absolute path to the pdf to be processed
	 * @param consolidateHeader
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving header information
	 * @param consolidateCitations
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving citations information
	 * @param method
	 *            - if method is 0, a rule-based is used for the full text part,
	 *            otherwise a machine learning approach is used.
	 */
	public String fullTextToTEI(String inputFile, boolean consolidateHeader, boolean consolidateCitations, int method) throws Exception {
		if (fullTextParser == null) {
			fullTextParser = new FullTextParser();
		}
		// replace by the commented version for the new full ML text parser
		String resultTEI = null;
		LOGGER.debug("Starting processing fullTextToTEI on " + inputFile);
		long time = System.currentTimeMillis();
		if (method == 0) {
			resultTEI = fullTextParser.processing(inputFile, consolidateHeader, consolidateCitations);
		} else {
			resultTEI = fullTextParser.processing2(inputFile, consolidateHeader, consolidateCitations);
		}
		LOGGER.debug("Ending processing fullTextToTEI on " + inputFile + ". Time to process: " + (System.currentTimeMillis() - time) + "ms");
		doc = fullTextParser.getDoc();
		resHeader = fullTextParser.getResHeader();
		return resultTEI;
	}

	/**
	 * Process all the PDF in a given directory with a header extraction and
	 * produce the corresponding training data format files for manual
	 * correction. The goal of this method is to help to produce additional
	 * traning data based on an existing model.
	 * 
	 * @param directoryPath
	 *            - the path to the directory containing PDF to be processed.
	 * @param resultPath
	 *            - the path to the directory where the results as XML files
	 *            shall be written.
	 * @param ind
	 *            - identifier integer to be included in the resulting files to
	 *            identify the training case. This is optional: no identifier
	 *            will be included if ind = -1
	 * @return the number of processed files.
	 */
	public int batchCreateTrainingHeader(String directoryPath, String resultPath, int ind) {
		return batchCreateTraining(directoryPath, resultPath, ind, 0);
	}

	/**
	 * Process all the PDF in a given directory with a fulltext extraction and
	 * produce the corresponding training data format files for manual
	 * correction. The goal of this method is to help to produce additional
	 * traning data based on an existing model.
	 * 
	 * @param directoryPath
	 *            - the path to the directory containing PDF to be processed.
	 * @param resultPath
	 *            - the path to the directory where the results as XML files
	 *            shall be written.
	 * @param ind
	 *            - identifier integer to be included in the resulting files to
	 *            identify the training case. This is optional: no identifier
	 *            will be included if ind = -1
	 * @return the number of processed files.
	 */
	public int batchCreateTrainingFulltext(String directoryPath, String resultPath, int ind) {
		return batchCreateTraining(directoryPath, resultPath, ind, 1);
	}

	private int batchCreateTraining(String directoryPath, String resultPath, int ind, int type) {
		try {
			File path = new File(directoryPath);
			// we process all pdf files in the directory
			File[] refFiles = path.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".pdf") || name.endsWith(".PDF"))
						return true;
					else
						return false;
				}
			});

			if (refFiles == null)
				return 0;

			// System.out.println(refFiles.length + " files to be processed.");

			int n = 0;
			// for (; n < refFiles.length; n++) {
			for (final File pdfFile : refFiles) {
				// File pdfFile = refFiles[n];
				// if (pdfFile.getAbsolutePath().endsWith(".pdf")) {
					if (type == 0) {
						createTrainingHeader(pdfFile.getPath(), resultPath, resultPath, ind + n);
					} else if (type == 1) {
						createTrainingFullText(pdfFile.getPath(), resultPath, resultPath, ind + n);
					}
					/*
					 * else if (type == 2) {
					 * createTrainingCitations(pdfFile.getPath(), resultPath,
					 * resultPath, ind+n); }
					 */
				// }

			}

			return refFiles.length;
		} catch (final Exception exp) {
			throw new GrobidException("An exception occured while running Grobid batch.", exp);
		}
	}

	/**
	 * Extract the headers for all PDF files in a given directory and produce
	 * the results as an XML file TEI conformant.
	 * 
	 * @param directoryPath
	 *            - the path to the directory containing PDF to be processed.
	 * @param resultPath
	 *            - the path to the directory where the results as XML files
	 *            shall be written.
	 * @param consolidate
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving header information
	 * @return the number of processed files.
	 */
	public int batchProcessHeader(String directoryPath, String resultPath, boolean consolidate) throws Exception {
		return batchProcess(directoryPath, resultPath, consolidate, consolidate, 0);
	}

	/**
	 * Extract the fulltext for all PDF files in a given directory and produce
	 * the results as an XML file TEI conformant.
	 * 
	 * @param directoryPath
	 *            - the path to the directory containing PDF to be processed.
	 * @param resultPath
	 *            - the path to the directory where the results as XML files
	 *            shall be written.
	 * @param consolidateHeader
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving header information
	 * @param consolidateCitations
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving citations information
	 * @return the number of processed files.
	 */
	public int batchProcessFulltext(String directoryPath, String resultPath, boolean consolidateHeader, boolean consolidateCitations) {
		return batchProcess(directoryPath, resultPath, consolidateHeader, consolidateCitations, 1);
	}

	/**
	 * @param directoryPath
	 *            input path, folder where the pdf files are supposed to be
	 *            located
	 * @param resultPath
	 *            output path, folder where the tei files pdfs are written to
	 * @param consolidateHeader
	 * @param consolidateCitations
	 * @param type
	 * @return
	 */
	private int batchProcess(String directoryPath, String resultPath, boolean consolidateHeader, boolean consolidateCitations, int type) {
		if (directoryPath == null) {
			throw new GrobidResourceException("Cannot start parsing, because the input path, "
					+ "where the pdf files are supposed to be located is null.");
		}
		if (resultPath == null) {
			throw new GrobidResourceException("Cannot start parsing, because the output path, "
					+ "where the tei files will be written to is null.");
		}
		File path = new File(directoryPath);
		if (!path.exists()) {
			throw new GrobidResourceException("Cannot start parsing, because the input path, "
					+ "where the pdf files are supposed to be located '" + path.getAbsolutePath() + "' does not exists.");
		}
		File resultPathFile = new File(resultPath);
		if (!resultPathFile.exists()) {
			if (!resultPathFile.mkdirs()) {
				throw new GrobidResourceException("Cannot start parsing, because cannot create "
						+ "output path for tei files on location '" + resultPathFile.getAbsolutePath() + "'.");
			}
		}

		try {
			// we process all pdf files in the directory
			File[] refFiles = path.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".pdf") || name.endsWith(".PDF"))
						return true;
					else
						return false;
				}
			});

			if (refFiles == null)
				return 0;

			// System.out.println(refFiles.length + " files to be processed.");

			int n = 0;
			for (; n < refFiles.length; n++) {
				File pdfFile = refFiles[n];
				if (!pdfFile.exists()) {
					throw new GrobidResourceException("A problem occurs in reading pdf file '" + pdfFile.getAbsolutePath()
							+ "'. The file does not exists. ");
				}
				if (type == 0) {
					// BiblioItem res = processHeader(pdfFile.getPath(),
					// consolidateHeader);
					BiblioItem res = new BiblioItem();
					String tei = processHeader(pdfFile.getPath(), consolidateHeader, res);
					// if (res!= null) {
					if (tei != null) {
						String outPath = resultPath + "/" + pdfFile.getName().replace(".pdf", GrobidProperties.FILE_ENDING_TEI_HEADER);
						Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPath), false), "UTF-8");
						// writer.write(res.toTEI(0) + "\n");
						writer.write(tei + "\n");
						writer.close();
					}
				} else if (type == 1) {
					String tei = fullTextToTEI(pdfFile.getPath(), consolidateHeader, consolidateCitations, 0);
					if (tei != null) {
						String outPath = resultPath + "/" + pdfFile.getName().replace(".pdf", GrobidProperties.FILE_ENDING_TEI_FULLTEXT);
						Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPath), false), "UTF-8");
						writer.write(tei + "\n");
						writer.close();
					}
				}
				/*
				 * else if (type == 2) { processCitations(pdfFile.getPath(),
				 * resultPath, resultPath); }
				 */
			}

			return refFiles.length;
		} catch (Exception e) {
			throw new GrobidException("An exception occured while running Grobid.", e);
		}
	}

	/**
	 * Get the TEI XML string corresponding to the recognized raw text citation
	 */
	public String rawCitation2TEI() {
		resRef.setPath(path);
		return resRef.toTEI(0);
	}

	/**
	 * Get the TEI XML string corresponding to the recognized raw text citation
	 * with pointers
	 */
	public String rawCitation2TEI2() {
		StringBuffer result = new StringBuffer();
		result.append("<tei>\n");

		BiblioSet bs = new BiblioSet();
		resRef.buildBiblioSet(bs, path);
		result.append(bs.toTEI());
		result.append("<listbibl>\n\n" + resRef.toTEI2(bs) + "\n</listbibl>\n</tei>\n");

		return result.toString();
	}

	/**
	 * Get the BibTeX string corresponding to the recognized raw text citation
	 */
	public String rawCitation2BibTeX() {
		resRef.setPath(path);
		return resRef.toBibTeX();
	}

	/**
	 * Get the TEI XML string corresponding to the recognized header text
	 */
	public String header2TEI() {
		return resHeader.toTEI(0);
	}

	/**
	 * Get the BibTeX string corresponding to the recognized header text
	 */
	public String header2BibTeX() {
		return resHeader.toBibTeX();
	}

	/**
	 * Get the TEI XML string corresponding to the recognized citation section
	 */
	public String references2TEI2() {
		StringBuffer result = new StringBuffer();
		result.append("<tei>\n");

		BiblioSet bs = new BiblioSet();

		for (BibDataSet bib : resBib) {
			BiblioItem bit = bib.getResBib();
			bit.buildBiblioSet(bs, path);
		}

		result.append(bs.toTEI());
		result.append("<listbibl>\n");

		for (BibDataSet bib : resBib) {
			BiblioItem bit = bib.getResBib();
			result.append("\n" + bit.toTEI2(bs));
		}
		result.append("\n</listbibl>\n</tei>\n");

		return result.toString();
	}

	/**
	 * Get the TEI XML string corresponding to the recognized citation section,
	 * with pointers and advanced structuring
	 */
	public String references2TEI() {
		StringBuffer result = new StringBuffer();
		result.append("<listbibl>\n");

		int p = 0;
		for (BibDataSet bib : resBib) {
			BiblioItem bit = bib.getResBib();
			bit.setPath(path);
			result.append("\n" + bit.toTEI(p));
			p++;
		}
		result.append("\n</listbibl>\n");
		return result.toString();
	}

	/**
	 * Get the BibTeX string corresponding to the recognized citation section
	 */
	public String references2BibTeX() {
		StringBuffer result = new StringBuffer();

		for (BibDataSet bib : resBib) {
			BiblioItem bit = bib.getResBib();
			bit.setPath(path);
			result.append("\n" + bit.toBibTeX());
		}

		return result.toString();
	}

	/**
	 * Get the TEI XML string corresponding to the recognized citation section
	 * for a particular citation
	 */
	public String reference2TEI(int i) {
		StringBuffer result = new StringBuffer();

		if (resBib != null) {
			if (i <= resBib.size()) {
				BibDataSet bib = resBib.get(i);
				BiblioItem bit = bib.getResBib();
				bit.setPath(path);
				result.append(bit.toTEI(i));
			}
		}

		return result.toString();
	}

	/**
	 * Get the BibTeX string corresponding to the recognized citation section
	 * for a given citation
	 */
	public String reference2BibTeX(int i) {
		StringBuffer result = new StringBuffer();

		if (resBib != null) {
			if (i <= resBib.size()) {
				BibDataSet bib = resBib.get(i);
				BiblioItem bit = bib.getResBib();
				bit.setPath(path);
				result.append(bit.toBibTeX());
			}
		}

		return result.toString();
	}

	public void OCRMetadataCorrection() {
		// utilities.OCRMetadataCorrection(resHeader);
	}

	/**
	 * Extract and parse patent references within a patent. Result are provided
	 * as PatentItem containing both "WISIWIG" results (the patent reference
	 * attributes as they appear in the text) and the attributes in DOCDB format
	 * (format according to WIPO and ISO standards). Offset positions are given
	 * in the PatentItem object.
	 * 
	 * @param text
	 *            - the string corresponding to the text body of the patent.
	 * @return the list of extracted and parserd patent references as PatentItem
	 *         object.
	 */
	public List<PatentItem> processPatentCitationsInPatent(String text) throws Exception {
		if (referenceExtractor == null) {
			referenceExtractor = new ReferenceExtractor();
		}
		List<PatentItem> patents = new ArrayList<PatentItem>();
		// we initialize the attribute individually for readability...
		boolean filterDuplicate = false;
		boolean consolidate = false;
		referenceExtractor.extractAllReferencesString(text, filterDuplicate, consolidate, patents, null);
		return patents;
	}

	/**
	 * Extract and parse non patent references within a patent. Result are
	 * provided as a BibDataSet with offset position instanciated relative to
	 * input text.
	 * 
	 * @param text
	 *            - the string corresponding to the text body of the patent.
	 * @param consolidateCitations
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving citations information
	 * @return the list of extracted and parserd non patent references as
	 *         BiblioItem object.
	 */
	public List<BibDataSet> processNPLCitationsInPatent(String text, boolean consolidateCitations) throws Exception {
		if (referenceExtractor == null) {
			referenceExtractor = new ReferenceExtractor();
		}
		List<BibDataSet> articles = new ArrayList<BibDataSet>();
		// we initialize the attribute individually for readability...
		boolean filterDuplicate = false;
		referenceExtractor.extractAllReferencesString(text, filterDuplicate, consolidateCitations, null, articles);
		return articles;
	}

	/**
	 * Extract and parse both patent and non patent references within a patent
	 * text. Result are provided as a BibDataSet with offset position
	 * instanciated relative to input text and as PatentItem containing both
	 * "WISIWIG" results (the patent reference attributes as they appear in the
	 * text) and the attributes in DOCDB format (format according to WIPO and
	 * ISO standards). Patent references' offset positions are also given in the
	 * PatentItem object.
	 * 
	 * @param text
	 *            - the string corresponding to the text body of the patent.
	 * @param nplResults
	 *            - the list of extracted and parsed non patent references as
	 *            BiblioItem object. This list must be instanciated before
	 *            calling the method for receiving the results.
	 * @param patentResults
	 *            - the list of extracted and parsed patent references as
	 *            PatentItem object. This list must be instanciated before
	 *            calling the method for receiving the results.
	 * @param consolidateCitations
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving citations information
	 */
	public void processAllCitationsInPatent(String text, List<BibDataSet> nplResults, List<PatentItem> patentResults,
			boolean consolidateCitations) throws Exception {
		if ((nplResults == null) && (patentResults == null)) {
			return;
		}
		if (referenceExtractor == null) {
			referenceExtractor = new ReferenceExtractor();
		}
		// we initialize the attribute individually for readability...
		boolean filterDuplicate = false;
		referenceExtractor.extractAllReferencesString(text, filterDuplicate, consolidateCitations, patentResults, nplResults);
	}

	/**
	 * Extract and parse both patent and non patent references within a patent
	 * in ST.36 format. Result are provided as a BibDataSet with offset position
	 * instanciated relative to input text and as PatentItem containing both
	 * "WISIWIG" results (the patent reference attributes as they appear in the
	 * text) and the attributes in DOCDB format (format according to WIPO and
	 * ISO standards). Patent references' offset positions are also given in the
	 * PatentItem object.
	 * 
	 * @param xmlPath
	 *            xml path
	 * @param nplResults
	 *            - the list of extracted and parsed non patent references as
	 *            BiblioItem object. This list must be instanciated before
	 *            calling the method for receiving the results.
	 * @param patentResults
	 *            - the list of extracted and parsed patent references as
	 *            PatentItem object. This list must be instanciated before
	 *            calling the method for receiving the results.
	 * @param consolidateCitations
	 *            - the consolidation option allows GROBID to exploit Crossref
	 *            web services for improving citations information
	 * @throws Exception
	 *             if sth. went wrong
	 */
	public void processAllCitationsInXMLPatent(String xmlPath, List<BibDataSet> nplResults, List<PatentItem> patentResults,
			boolean consolidateCitations) throws Exception {
		if ((nplResults == null) && (patentResults == null)) {
			return;
		}
		if (referenceExtractor == null) {
			referenceExtractor = new ReferenceExtractor();
		}
		// we initialize the attribute individually for readability...
		boolean filterDuplicate = false;
		referenceExtractor.extractAllReferencesXMLFile(xmlPath, filterDuplicate, consolidateCitations, patentResults, nplResults);
	}

	/**
	 * Process an XML patent document with a patent citation extraction and
	 * produce the corresponding training data format files for manual
	 * correction. The goal of this method is to help to produce additional
	 * traning data based on an existing model.
	 * 
	 * @param pathXML
	 *            - the path to the XML patent document to be processed.
	 * @param resultPath
	 *            - the path to the directory where the results as XML files
	 *            shall be written.
	 */
	public void createTrainingPatentCitations(String pathXML, String resultPath) throws Exception {
		if (referenceExtractor == null) {
			referenceExtractor = new ReferenceExtractor();
		}
		referenceExtractor.generateTrainingData(pathXML, resultPath);
	}

	/**
	 * Process all the XML patent documents in a given directory with a patent
	 * citation extraction and produce the corresponding training data format
	 * files for manual correction. The goal of this method is to help to
	 * produce additional traning data based on an existing model.
	 * 
	 * @param directoryPath
	 *            - the path to the directory containing XML files to be
	 *            processed.
	 * @param resultPath
	 *            - the path to the directory where the results as XML files
	 *            shall be written.
	 * @return the number of processed files.
	 */
	public int batchCreateTrainingPatentcitations(String directoryPath, String resultPath) throws Exception {
		try {
			File path = new File(directoryPath);
			// we process all pdf files in the directory
			File[] refFiles = path.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".xml") || name.endsWith(".XML"))
						return true;
					else
						return false;
				}
			});

			if (refFiles == null)
				return 0;

			// System.out.println(refFiles.length + " files to be processed.");

			int n = 0;
			for (; n < refFiles.length; n++) {
				File xmlFile = refFiles[n];
				createTrainingPatentCitations(xmlFile.getPath(), resultPath);
			}

			return refFiles.length;
		} catch (Exception e) {
			throw new GrobidException("An exception occured while running Grobid.", e);
		}
	}

	/**
	 * Extract chemical names from text.
	 * 
	 * @param text
	 *            - text to be processed.
	 * @return List of chemical entites as POJO.
	 */
	public List<ChemicalEntity> extractChemicalEntities(String text) throws Exception {
		if (chemicalParser == null) {
			chemicalParser = new ChemicalParser();
		}
		return chemicalParser.extractChemicalEntities(text);
	}

	/**
	 * Return all textual content except metadata. Useful for term extraction
	 */
	public String getAllBody(boolean withBookTitle) throws Exception {
		return doc.getAllBody(this, resHeader, resBib, withBookTitle);
	}

	/**
	 * Return all textual content without requiring a segmentation. Ignore the
	 * toIgnore1 th blocks (default is 0) and the blocks after toIgnore2 th
	 * (included, default is -1)
	 */
	public String getAllBlocksClean(int toIgnore1, int toIgnore2) throws Exception {
		return doc.getAllBlocksClean(toIgnore1, toIgnore2);
	}

	public String getAllBlocksClean(int toIgnore1) throws Exception {
		return doc.getAllBlocksClean(toIgnore1, -1);
	}

	public String getAllBlocksClean() throws Exception {
		return doc.getAllBlocksClean(0, -1);
	}

	/**
	 * Print the abstract content. Useful for term extraction.
	 */
	public String getAbstract() throws Exception {
		String abstr = resHeader.getAbstract();
		abstr = abstr.replace("@BULLET", " • ");
		return abstr;
	}

	/**
	 * Return all the reference titles. Maybe useful for term extraction.
	 */
	public String printRefTitles() throws Exception {
		StringBuffer accumulated = new StringBuffer();
		for (BibDataSet bib : resBib) {
			BiblioItem bit = bib.getResBib();

			if (bit.getTitle() != null) {
				accumulated.append(bit.getTitle() + "\n");
			}
		}

		return accumulated.toString();
	}

	/**
	 * Return all the reference book titles. Maybe useful for term extraction.
	 */
	public String printRefBookTitles() throws Exception {
		StringBuffer accumulated = new StringBuffer();
		for (BibDataSet bib : resBib) {
			BiblioItem bit = bib.getResBib();

			if (bit.getJournal() != null) {
				accumulated.append(bit.getJournal() + "\n");
			}

			if (bit.getBookTitle() != null) {
				accumulated.append(bit.getBookTitle() + "\n");
			}
		}

		return accumulated.toString();
	}

	/**
	 * Return the introduction.
	 * 
	 * @return introduction
	 */
	public String getIntroduction() throws Exception {
		return doc.getIntroduction(this);
	}

	/**
	 * 
	 * @return conclusion.
	 */
	public String getConclusion() throws Exception {
		return doc.getConclusion(this);
	}

	/**
	 * Return all the section titles.
	 */
	public String getSectionTitles() throws Exception {
		return doc.getSectionTitles();
	}

	public AffiliationAddressParser getAffiliationAddressParser() {
		return affiliationAddressParser;
	}

	public AuthorParser getAuthorParser() {
		return authorParser;
	}

	public HeaderParser getHeaderParser() {
		return headerParser;
	}

	public DateParser getDateParser() {
		return dateParser;
	}

	public CitationParser getCitationParser() {
		return citationParser;
	}

	public FullTextParser getFullTextParser() {
		return fullTextParser;
	}

	@Override
	public synchronized void close() throws IOException {
		LOGGER.debug("==> Closing all resources...");
		if (authorParser != null) {
			authorParser.close();
			authorParser = null;
			LOGGER.debug("CLOSING authorParser");
		}
		if (affiliationAddressParser != null) {
			affiliationAddressParser.close();
			affiliationAddressParser = null;
			LOGGER.debug("CLOSING affiliationAddressParser");
		}

		if (headerParser != null) {
			headerParser.close();
			headerParser = null;
			LOGGER.debug("CLOSING headerParser");
		}

		if (dateParser != null) {
			dateParser.close();
			dateParser = null;
			LOGGER.debug("CLOSING dateParser");
		}

		if (citationParser != null) {
			citationParser.close();
			citationParser = null;
			LOGGER.debug("CLOSING citationParser");
		}

		if (fullTextParser != null) {
			fullTextParser.close();
			fullTextParser = null;
			LOGGER.debug("CLOSING fullTextParser");
		}

		if (referenceExtractor != null) {
			referenceExtractor.close();
			referenceExtractor = null;
			LOGGER.debug("CLOSING referenceExtractor");
		}

		if (chemicalParser != null) {
			chemicalParser.close();
			chemicalParser = null;
			LOGGER.debug("CLOSING chemicalParser");
		}

		LOGGER.debug("==>All resources closed");
	}
}