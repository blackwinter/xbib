
package org.xbib.graphics.barcode.tools;

import java.util.Map;

/**
 * Holds information on the page a barcode is painted on.
 */
public class PageInfo {

    private int pageNumber;
    private String pageNumberString;

    /**
     * Creates a new object.
     * @param pageNumber the page number
     * @param pageNumberString the string representation of the page number (ex. "12" or "XII")
     */
    public PageInfo(int pageNumber, String pageNumberString) {
        this.pageNumber = pageNumber;
        this.pageNumberString = pageNumberString;
    }

    /**
     * Creates a {@link PageInfo} from a {@link Map} containing processing hints.
     * @param hints the processing hints
     * @return the page info object or null if no such information is available
     */
    public static PageInfo fromProcessingHints(Map hints) {
        if (hints.containsKey("page-number")) {
            int pageNumber = ((Number)hints.get("page-number")).intValue();
            String pageName = (String)hints.get("page-name");
            return new PageInfo(pageNumber, pageName);
        }
        return null;
    }

    /**
     * Returns the page number
     * @return the page number
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * Returns the string representation of the page number (ex. "12" or "XII").
     * @return the page number as a string
     */
    public String getPageNumberString() {
        return this.pageNumberString;
    }

}
