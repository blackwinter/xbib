package org.xbib.io.ftp.listparsers;

import org.xbib.io.ftp.FTPEntry;
import org.xbib.io.ftp.FTPException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This parser can handle the result of a list ftp command as it is a UNIX "ls
 * -l" command response.
 */
public class UnixListParser implements FTPListParser {

    private static final Pattern PATTERN = Pattern
            .compile("^([dl\\-])[r\\-][w\\-][xSs\\-][r\\-][w\\-][xSs\\-][r\\-][w\\-][xTt\\-]\\s+"
                    + "(?:\\d+\\s+)?\\S+\\s*\\S+\\s+(\\d+)\\s+(?:(\\w{3})\\s+(\\d{1,2}))\\s+"
                    + "(?:(\\d{4})|(?:(\\d{1,2}):(\\d{1,2})))\\s+"
                    + "([^\\\\*?\"<>|]+)(?: -> ([^\\\\*?\"<>|]+))?$");

    @Override
    public List<FTPEntry> parse(List<String> lines, TimeZone timeZone) throws FTPException {
        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm", Locale.US);
        dateFormat.setTimeZone(timeZone);
        List<FTPEntry> ret = new ArrayList<>();
        int size = lines.size();
        if (size == 0) {
            return ret;
        }
        // Removes the "total" line used in MAC style.
        if (lines.get(0).startsWith("total")) {
            size--;
            List<String> lines2 = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                lines2.add(lines.get(i + 1));
            }
            lines = lines2;
        }
        // What's the date today?
        Calendar now = Calendar.getInstance();
        // Ok, starts parsing.
        int currentYear = now.get(Calendar.YEAR);

        for (int i = 0; i < size; i++) {
            Matcher m = PATTERN.matcher(lines.get(i));
            if (m.matches()) {
                FTPEntry ftpFiles = new FTPEntry();
                // Retrieve the data.
                String typeString = m.group(1);
                String sizeString = m.group(2);
                String monthString = m.group(3);
                String dayString = m.group(4);
                String yearString = m.group(5);
                String hourString = m.group(6);
                String minuteString = m.group(7);
                String nameString = m.group(8);
                String linkedString = m.group(9);
                // Parse the data.
                switch (typeString) {
                    case "-":
                        ftpFiles.setType(FTPEntry.TYPE_FILE);
                        break;
                    case "d":
                        ftpFiles.setType(FTPEntry.TYPE_DIRECTORY);
                        break;
                    case "l":
                        ftpFiles.setType(FTPEntry.TYPE_LINK);
                        ftpFiles.setLink(linkedString);
                        break;
                    default:
                        throw new FTPException("list parse");
                }
                long fileSize;
                try {
                    fileSize = Long.parseLong(sizeString);
                } catch (Throwable t) {
                    throw new FTPException("list parse");
                }
                ftpFiles.setSize(fileSize);
                if (dayString.length() == 1) {
                    dayString = "0" + dayString;
                }
                StringBuilder mdString = new StringBuilder();
                mdString.append(monthString);
                mdString.append(' ');
                mdString.append(dayString);
                mdString.append(' ');
                boolean checkYear;
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
                    md = dateFormat.parse(mdString.toString());
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
                ftpFiles.setModifiedDate(md);
                ftpFiles.setName(nameString);
                ret.add(ftpFiles);
            } else {
                throw new FTPException("parse list");
            }
        }
        return ret;
    }

}
