
package org.xbib.io.ftp.listparsers;

import org.xbib.io.ftp.FTPException;
import org.xbib.io.ftp.FTPEntry;

import java.util.List;
import java.util.TimeZone;

/**
 * Implement this interface to build a new LIST parser. List parsers are called
 * to parse the result of a FTP LIST command send to the server in the list()
 * method. You can add a custom parser to your instance of FTPClient calling on
 * it the method addListParser.
 */
public interface FTPListParser {

    /**
     * Parses a LIST command response and builds an array of FTPFile objects.
     *
     * @param lines The response to parse, splitted by line.
     * @return An array of FTPFile objects representing the result of the
     * operation.
     * @throws FTPException If this parser cannot parse the given response.
     */
    List<FTPEntry> parse(List<String> lines, TimeZone tz) throws FTPException;

}
