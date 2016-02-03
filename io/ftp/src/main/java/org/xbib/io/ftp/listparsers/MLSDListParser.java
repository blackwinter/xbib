package org.xbib.io.ftp.listparsers;

import org.xbib.io.ftp.FTPEntry;
import org.xbib.io.ftp.FTPException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * This parser can handle the standard MLST/MLSD responses (RFC 3659).
 */
public class MLSDListParser implements FTPListParser {

    /**
     * Date format 1 for MLSD date facts (supports millis).
     */
    //private static final

    /**
     * Date format 2 for MLSD date facts (doesn't support millis).
     */
    //private static final
    @Override
    public List<FTPEntry> parse(List<String> lines, TimeZone timeZone) throws FTPException {
        List<FTPEntry> list = new ArrayList<>();
        for (String line : lines) {
            FTPEntry file = parseLine(line, timeZone);
            if (file != null) {
                list.add(file);
            }
        }
        return list;
    }

    /**
     * Parses a line ad a MLSD response element.
     *
     * @param line The line.
     * @return The file, or null if the line has to be ignored.
     * @throws FTPException If the line is not a valid MLSD entry.
     */
    private FTPEntry parseLine(String line, TimeZone timeZone) throws FTPException {
        DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMddhhmmss.SSS Z");
        dateFormat1.setTimeZone(timeZone);
        DateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss Z");
        dateFormat2.setTimeZone(timeZone);
        List<String> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(line, ";");
        while (st.hasMoreElements()) {
            String aux = st.nextToken().trim();
            if (aux.length() > 0) {
                list.add(aux);
            }
        }
        if (list.size() == 0) {
            throw new FTPException("list size 0");
        }
        // Extracts the file name.
        String name = list.remove(list.size() - 1);
        // Parses the facts.
        Properties facts = new Properties();
        for (String aux : list) {
            int sep = aux.indexOf('=');
            if (sep == -1) {
                throw new FTPException("list parse");
            }
            String key = aux.substring(0, sep).trim();
            String value = aux.substring(sep + 1, aux.length()).trim();
            if (key.length() == 0 || value.length() == 0) {
                throw new FTPException("list parse");
            }
            facts.setProperty(key, value);
        }
        // Type.
        int type;
        String typeString = facts.getProperty("type");
        if (typeString == null) {
            throw new FTPException("list parse");
        } else if ("file".equalsIgnoreCase(typeString)) {
            type = FTPEntry.TYPE_FILE;
        } else if ("dir".equalsIgnoreCase(typeString)) {
            type = FTPEntry.TYPE_DIRECTORY;
        } else if ("cdir".equalsIgnoreCase(typeString)) {
            // Current directory. Skips...
            return null;
        } else if ("pdir".equalsIgnoreCase(typeString)) {
            // Parent directory. Skips...
            return null;
        } else {
            // Unknown... (link?)... Skips...
            return null;
        }
        // Last modification date.
        Date modifiedDate = null;
        String modifyString = facts.getProperty("modify");
        if (modifyString != null) {
            modifyString += " +0000";
            try {
                modifiedDate = dateFormat1.parse(modifyString);
            } catch (ParseException e1) {
                try {
                    modifiedDate = dateFormat2.parse(modifyString);
                } catch (ParseException e2) {
                    //
                }
            }
        }
        // Size.
        long size = 0;
        String sizeString = facts.getProperty("size");
        if (sizeString != null) {
            try {
                size = Long.parseLong(sizeString);
            } catch (NumberFormatException e) {
                //
            }
            if (size < 0) {
                size = 0;
            }
        }
        // Done!
        FTPEntry ret = new FTPEntry();
        ret.setType(type);
        ret.setModifiedDate(modifiedDate);
        ret.setSize(size);
        ret.setName(name);
        return ret;
    }

}
