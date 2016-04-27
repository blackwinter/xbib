package org.xbib.wikidata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor;
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor.TimeoutException;
import org.wikidata.wdtk.dumpfiles.MwDumpFile;

public class DataExtractionProcessor implements EntityDocumentProcessor {

    private final static Logger logger = LogManager.getLogger(DataExtractionProcessor.class);

	private static final String extractPropertyId = "P227"; // "GND identifier"
	private static final String filterPropertyId = "P31"; // "instance of"
	private static final Value filterValue = Datamodel.makeWikidataItemIdValue("Q5"); // "human"

	private int itemsWithPropertyCount = 0;
	private int itemCount = 0;

	/**
	 * If set to true, all example programs will run in offline mode. Only data
	 * dumps that have been downloaded in previous runs will be used.
	 */
	//private static final boolean OFFLINE_MODE = false;

    private BufferedWriter out;

	public DataExtractionProcessor() throws IOException {
		// open file for writing results:
        File file = new File("extracted-data.csv");

		try (BufferedWriter out = Files.newBufferedWriter(file.toPath())){
            this.out = out;
            // write CSV header:
            out.write("ID,Label (en),Label (de),Value,Wikipedia (en),Wikipedia (de)");
            processEntitiesFromWikidataDump(this);
            System.err.println("Found " + this.itemsWithPropertyCount
                    + " matching items after scanning " + this.itemCount
                    + " items.");
        }
	}

    private void processEntitiesFromWikidataDump(EntityDocumentProcessor entityDocumentProcessor) {
		DumpProcessingController dumpProcessingController = new DumpProcessingController("wikidatawiki");
		//dumpProcessingController.setOfflineMode(OFFLINE_MODE);

		// // Optional: Use another download directory:
		// dumpProcessingController.setDownloadDirectory(System.getProperty("user.dir"));

		// Should we process historic revisions or only current ones?
		boolean onlyCurrentRevisions = true;

		// Subscribe to the most recent entity documents of type wikibase item:
		dumpProcessingController.registerEntityDocumentProcessor(
				entityDocumentProcessor, null, onlyCurrentRevisions);

		// Also add a timer that reports some basic progress information:
		EntityTimerProcessor entityTimerProcessor = new EntityTimerProcessor(0);
		dumpProcessingController.registerEntityDocumentProcessor(
				entityTimerProcessor, null, onlyCurrentRevisions);

		MwDumpFile dumpFile = null;
		try {
					dumpFile = dumpProcessingController
							.getMostRecentDump(DumpContentType.JSON);

			if (dumpFile != null) {
				dumpProcessingController.processDump(dumpFile);
			}
		} catch (TimeoutException e) {
			// The timer caused a time out. Continue and finish normally.
		}

		// Print final timer results:
		entityTimerProcessor.close();
	}


	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		this.itemCount++;

		// Check if the item matches our filter conditions:
		if (!itemDocument.hasStatementValue(filterPropertyId, filterValue)) {
			return;
		}

        Iterator<Statement> it = itemDocument.getAllStatements();

		// Find the first value for this property, if any:
		StringValue stringValue = itemDocument.findStatementStringValue(extractPropertyId);

		// If a value was found, write the data:
		if (stringValue != null) {
			this.itemsWithPropertyCount++;
            try {
                out.write(itemDocument.getItemId().getId());
                out.write(",");
                out.write(csvEscape(itemDocument.findLabel("en")));
                out.write(",");
                out.write(csvEscape(itemDocument.findLabel("de")));
                out.write(",");
                out.write(csvEscape(stringValue.getString()));
                out.write(",");
                SiteLink enwiki = itemDocument.getSiteLinks().get("enwiki");
                if (enwiki != null) {
                    out.write(csvEscape(enwiki.getPageTitle()));
                } else {
                    out.write("\"\"");
                }
                out.write(",");
                SiteLink dewiki = itemDocument.getSiteLinks().get("dewiki");
                if (dewiki != null) {
                    out.write(csvEscape(dewiki.getPageTitle()));
                } else {
                    out.write("\"\"");
                }
                out.write("\n");
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
		}
	}

	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		// Nothing to do
	}

	/**
	 * Escapes a string for use in CSV. In particular, the string is quoted and
	 * quotation marks are escaped.
	 *
	 * @param string
	 *            the string to escape
	 * @return the escaped string
	 */
	private String csvEscape(String string) {
		if (string == null) {
			return "\"\"";
		} else {
			return "\"" + string.replace("\"", "\"\"") + "\"";
		}
	}

}
