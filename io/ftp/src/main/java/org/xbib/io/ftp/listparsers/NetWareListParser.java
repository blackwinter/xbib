package org.xbib.io.ftp.listparsers;

import org.xbib.io.ftp.FTPException;
import org.xbib.io.ftp.FTPEntry;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This parser can handle NetWare list responses
 */
public class NetWareListParser implements FTPListParser {

    private static final Pattern PATTERN = Pattern
            .compile("^(d|-)\\s+\\[.{8}\\]\\s+\\S+\\s+(\\d+)\\s+"
                    + "(?:(\\w{3})\\s+(\\d{1,2}))\\s+(?:(\\d{4})|(?:(\\d{1,2}):(\\d{1,2})))\\s+"
                    + "([^\\\\/*?\"<>|]+)$");

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "MMM dd yyyy HH:mm", Locale.US);

    public List<FTPEntry> parse(List<String> lines) throws FTPException {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        List<FTPEntry> ret = new ArrayList<FTPEntry>();
        for (String line : lines) {
            Matcher m = PATTERN.matcher(line);
            if (m.matches()) {
                String typeString = m.group(1);
                String sizeString = m.group(2);
                String monthString = m.group(3);
                String dayString = m.group(4);
                String yearString = m.group(5);
                String hourString = m.group(6);
                String minuteString = m.group(7);
                String nameString = m.group(8);
                // Parse the data.
                FTPEntry ftpEntry = new FTPEntry();
                if (typeString.equals("-")) {
                    ftpEntry.setType(FTPEntry.TYPE_FILE);
                } else if (typeString.equals("d")) {
                    ftpEntry.setType(FTPEntry.TYPE_DIRECTORY);
                } else {
                    throw new FTPException("list parse");
                }
                long fileSize;
                try {
                    fileSize = Long.parseLong(sizeString);
                } catch (Throwable t) {
                    throw new FTPException("list parse");
                }
                ftpEntry.setSize(fileSize);
                if (dayString.length() == 1) {
                    dayString = "0" + dayString;
                }
                StringBuilder mdString = new StringBuilder();
                mdString.append(monthString);
                mdString.append(' ');
                mdString.append(dayString);
                mdString.append(' ');
                boolean checkYear = false;
                if (yearString == null) {
                    mdString.append(currentYear);
                    checkYear = true;
                } else {
                    mdString.append(yearString);
                    checkYear = false;
                }
                mdString.append(' ');
                if (hourString != null && minuteString != null) {
                    if (hourString.length() == 1) {
                        hourString = "0" + hourString;
                    }
                    if (minuteString.length() == 1) {
                        minuteString = "0" + minuteString;
                    }
                    mdString.append(hourString);
                    mdString.append(':');
                    mdString.append(minuteString);
                } else {
                    mdString.append("00:00");
                }
                Date md;
                try {
                    synchronized (DATE_FORMAT) {
                        md = DATE_FORMAT.parse(mdString.toString());
                    }
                } catch (ParseException e) {
                    throw new FTPException("date parse");
                }
                if (checkYear) {
                    Calendar mc = Calendar.getInstance();
                    mc.setTime(md);
                    if (mc.after(now) && mc.getTimeInMillis() - now.getTimeInMillis() > 24L * 60L * 60L * 1000L) {
                        mc.set(Calendar.YEAR, currentYear - 1);
                        md = mc.getTime();
                    }
                }
                ftpEntry.setModifiedDate(md);
                ftpEntry.setName(nameString);
                ret.add(ftpEntry);
            } else {
                throw new FTPException("list parse");
            }
        }
        return ret;
    }

}
