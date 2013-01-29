package org.grobid.core.engines;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.Affiliation;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.data.Person;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.batch.GrobidMainArgs;
import org.grobid.core.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessEngine {

	/**
	 * The logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessEngine.class);

	/**
	 * The engine.
	 */
	private static Engine engine;

	/**
	 * @return the engine instance.
	 */
	protected Engine getEngine() {
		if (engine == null) {
			engine = GrobidFactory.getInstance().createEngine();
		}
		return engine;
	}

	/**
	 * Process the headers using pGbdArgs parameters.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 * @throws Exception
	 */
	public void processHeader(final GrobidMainArgs pGbdArgs) throws Exception {
		inferPdfInputPath(pGbdArgs);
		inferOutputPath(pGbdArgs);
		final File pdfDirectory = new File(pGbdArgs.getPath2Input());
		String result = StringUtils.EMPTY;
		for (final File currPdf : pdfDirectory.listFiles()) {
			try {
				if (currPdf.getName().toLowerCase().endsWith(".pdf")) {
					result = getEngine().processHeader(currPdf.getAbsolutePath(), false, null);
					Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator
							+ new File(currPdf.getAbsolutePath()).getName().replace(".pdf", ".tei.xml"), result.toString());
				}
			} catch (final Exception exp) {
				LOGGER.error("An error occured while processing the file " + currPdf.getAbsolutePath()
						+ ". Continuing the process for the other files");
			}
		}
	}

	/**
	 * Process the full text using pGbdArgs parameters.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 * @throws Exception
	 */
	public void processFullText(final GrobidMainArgs pGbdArgs) throws Exception {
		inferPdfInputPath(pGbdArgs);
		inferOutputPath(pGbdArgs);
		final File pdfDirectory = new File(pGbdArgs.getPath2Input());
		String result = StringUtils.EMPTY;
		for (final File currPdf : pdfDirectory.listFiles()) {
			try {
				if (currPdf.getName().toLowerCase().endsWith(".pdf")) {
					result = getEngine().fullTextToTEI(currPdf.getAbsolutePath(), false, false);
					Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator
							+ new File(currPdf.getAbsolutePath()).getName().replace(".pdf", ".tei.xml"), result.toString());
				}
			} catch (final Exception exp) {
				LOGGER.error("An error occured while processing the file " + currPdf.getAbsolutePath()
						+ ". Continuing the process for the other files");
			}
		}
	}

	/**
	 * Process the date using pGbdArgs parameters.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 * @throws Exception
	 */
	public void processDate(final GrobidMainArgs pGbdArgs) throws Exception {
		inferOutputPath(pGbdArgs);
		final List<Date> result = getEngine().processDate(pGbdArgs.getInput());
		Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.get(0).toTEI());
		LOGGER.info(result.get(0).toTEI());
	}

	/**
	 * Process the author header using pGbdArgs parameters.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 * @throws Exception
	 */
	public void processAuthorsHeader(final GrobidMainArgs pGbdArgs) throws Exception {
		inferOutputPath(pGbdArgs);
		final List<Person> result = getEngine().processAuthorsHeader(pGbdArgs.getInput());
		Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.get(0).toTEI());
		LOGGER.info(result.get(0).toTEI());
	}

	/**
	 * Process the author citation using pGbdArgs parameters.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 * @throws Exception
	 */
	public void processAuthorsCitation(final GrobidMainArgs pGbdArgs) throws Exception {
		inferOutputPath(pGbdArgs);
		final List<Person> result = getEngine().processAuthorsCitation(pGbdArgs.getInput());
		Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.get(0).toTEI());
		LOGGER.info(result.get(0).toTEI());
	}

	/**
	 * Process the affiliation using pGbdArgs parameters.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 * @throws Exception
	 */
	public void processAffiliation(final GrobidMainArgs pGbdArgs) throws Exception {
		inferOutputPath(pGbdArgs);
		final List<Affiliation> result = getEngine().processAffiliation(pGbdArgs.getInput());
		Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.get(0).toTEI());
		LOGGER.info(result.get(0).toTEI());
	}

	/**
	 * Process the raw reference using pGbdArgs parameters.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 * @throws Exception
	 */
	public void processRawReference(final GrobidMainArgs pGbdArgs) throws Exception {
		inferOutputPath(pGbdArgs);
		final BiblioItem result = getEngine().processRawReference(pGbdArgs.getInput(), false);
		Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.toTEI(-1));
		LOGGER.info(result.toTEI(-1));
	}

	/**
	 * Train the header.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 */
	public void createTrainingHeader(final GrobidMainArgs pGbdArgs) {
		inferPdfInputPath(pGbdArgs);
		inferOutputPath(pGbdArgs);
		int result = getEngine().batchCreateTrainingHeader(pGbdArgs.getPath2Input(), pGbdArgs.getPath2Output(), -1);
		LOGGER.info(result + " files processed.");
	}

	/**
	 * Train the full text.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 */
	public void createTrainingFulltext(final GrobidMainArgs pGbdArgs) {
		inferPdfInputPath(pGbdArgs);
		inferOutputPath(pGbdArgs);
		int result = getEngine().batchCreateTrainingFulltext(pGbdArgs.getPath2Input(), pGbdArgs.getPath2Output(), -1);
		LOGGER.info(result + " files processed.");
	}

	/**
	 * Train the full text.
	 * 
	 * @param pGbdArgs
	 *            The parameters.
	 * @throws Exception
	 */
	public void createTrainingPatentcitations(final GrobidMainArgs pGbdArgs) throws Exception {
		inferPdfInputPath(pGbdArgs);
		inferOutputPath(pGbdArgs);
		int result = getEngine().batchCreateTrainingPatentcitations(pGbdArgs.getPath2Input(), pGbdArgs.getPath2Output());
		LOGGER.info(result + " files processed.");
	}

	/**
	 * List the engine methods that can be called.
	 * 
	 * @return List<String> containing the list of the methods.
	 */
	public final static List<String> getUsableMethods() {
		final Class<?> pClass = new ProcessEngine().getClass();
		final List<String> availableMethods = new ArrayList<String>();
		for (final Method method : pClass.getMethods()) {
			if (isUsableMethod(method.getName())) {
				availableMethods.add(method.getName());
			}
		}
		return availableMethods;
	}

	/**
	 * Check if the method is usable.
	 * 
	 * @param pMethod
	 *            method name.
	 * @return if it is usable
	 */
	protected final static boolean isUsableMethod(final String pMethod) {
		boolean isUsable = StringUtils.equals("wait", pMethod);
		isUsable |= StringUtils.equals("equals", pMethod);
		isUsable |= StringUtils.equals("toString", pMethod);
		isUsable |= StringUtils.equals("hashCode", pMethod);
		isUsable |= StringUtils.equals("getClass", pMethod);
		isUsable |= StringUtils.equals("notify", pMethod);
		isUsable |= StringUtils.equals("notifyAll", pMethod);
		isUsable |= StringUtils.equals("isUsableMethod", pMethod);
		isUsable |= StringUtils.equals("getUsableMethods", pMethod);
		isUsable |= StringUtils.equals("inferPdfInputPath", pMethod);
		isUsable |= StringUtils.equals("inferOutputPath", pMethod);
		return !isUsable;
	}

	/**
	 * Infer the input path for pdfs if not given in arguments.
	 * 
	 * @param pGbdArgs
	 *            The GrobidArgs.
	 */
	protected final static void inferPdfInputPath(final GrobidMainArgs pGbdArgs) {
		String tmpFilePath;
		if (pGbdArgs.getPath2Input() == null) {
			tmpFilePath = new File(".").getAbsolutePath();
			LOGGER.info("No path set for the input directory. Using: " + tmpFilePath);
			pGbdArgs.setPath2Input(tmpFilePath);
		}
	}

	/**
	 * Infer the output path if not given in arguments.
	 * 
	 * @param pGbdArgs
	 *            The GrobidArgs.
	 */
	protected final static void inferOutputPath(final GrobidMainArgs pGbdArgs) {
		String tmpFilePath;
		if (pGbdArgs.getPath2Output() == null) {
			tmpFilePath = new File(".").getAbsolutePath();
			LOGGER.info("No path set for the output directory. Using: " + tmpFilePath);
			pGbdArgs.setPath2Output(tmpFilePath);
		}
	}

}
