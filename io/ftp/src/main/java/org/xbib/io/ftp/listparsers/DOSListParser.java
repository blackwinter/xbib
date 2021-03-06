package org.xbib.io.ftp.listparsers;

import org.xbib.io.ftp.FTPEntry;
import org.xbib.io.ftp.FTPException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This parser can handle the MSDOS-style LIST responses.
 */
public class DOSListParser implements FTPListParser {

    private static final Pattern PATTERN = Pattern
            .compile("^(\\d{2})-(\\d{2})-(\\d{2})\\s+(\\d{2}):(\\d{2})(AM|PM)\\s+"
                    + "(<DIR>|\\d+)\\s+([^\\\\/*?\"<>|]+)$");

    @Override
    public List<FTPEntry> parse(List<String> lines, TimeZone timeZone) throws FTPException {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
        List<FTPEntry> ret = new ArrayList<>();
        for (String line : lines) {
            Matcher m = PATTERN.matcher(line);
            if (m.matches()) {
                String month = m.group(1);
                String day = m.group(2);
                String year = m.group(3);
                String hour = m.group(4);
                String minute = m.group(5);
                String ampm = m.group(6);
                String dirOrSize = m.group(7);
                String name = m.group(8);
                FTPEntry ftpEntry = new FTPEntry();
                ftpEntry.setName(name);
                if (dirOrSize.equalsIgnoreCase("<DIR>")) {
                    ftpEntry.setType(FTPEntry.TYPE_DIRECTORY);
                    ftpEntry.setSize(0);
                } else {
                    long fileSize;
                    try {
                        fileSize = Long.parseLong(dirOrSize);
                    } catch (Throwable t) {
                        throw new FTPException("parse");
                    }
                    ftpEntry.setType(FTPEntry.TYPE_FILE);
                    ftpEntry.setSize(fileSize);
                }
                String mdString = month + "/" + day + "/" + year + " " + hour
                        + ":" + minute + " " + ampm;
                Date md;
                try {
                    md = dateFormat.parse(mdString);
                    ftpEntry.setModifiedDate(md);
                } catch (ParseException e) {
                    // ignore
                }
                ret.add(ftpEntry);
            } else {
                throw new FTPException("parse");
            }
        }
        return ret;
    }

}
