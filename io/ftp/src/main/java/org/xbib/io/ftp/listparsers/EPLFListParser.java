package org.xbib.io.ftp.listparsers;

import org.xbib.io.ftp.FTPException;
import org.xbib.io.ftp.FTPEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * This parser can handle the EPLF format.
 */
public class EPLFListParser implements FTPListParser {

    @Override
    public List<FTPEntry> parse(List<String> lines, TimeZone timeZone) throws FTPException {
        List<FTPEntry> ret = new ArrayList<>();
        for (String l : lines) {
            // Validate the plus sign.
            if (l.charAt(0) != '+') {
                throw new FTPException("list parse");
            }
            // Split the facts from the filename.
            int a = l.indexOf('\t');
            if (a == -1) {
                throw new FTPException("list parse");
            }
            String facts = l.substring(1, a);
            String name = l.substring(a + 1, l.length());
            // Parse the facts.
            Date md = null;
            boolean dir = false;
            long fileSize = 0;
            StringTokenizer st = new StringTokenizer(facts, ",");
            while (st.hasMoreTokens()) {
                String f = st.nextToken();
                int s = f.length();
                if (s > 0) {
                    if (s == 1) {
                        if (f.equals("/")) {
                            // This is a directory.
                            dir = true;
                        }
                    } else {
                        char c = f.charAt(0);
                        String value = f.substring(1, s);
                        if (c == 's') {
                            // Size parameter.
                            try {
                                fileSize = Long.parseLong(value);
                            } catch (Throwable t) {
                                //
                            }
                        } else if (c == 'm') {
                            try {
                                long m = Long.parseLong(value);
                                md = new Date(m * 1000);
                            } catch (Throwable t) {
                                // ignore
                            }
                        }
                    }
                }
            }
            FTPEntry ftpEntry = new FTPEntry();
            ftpEntry.setName(name);
            ftpEntry.setModifiedDate(md);
            ftpEntry.setSize(fileSize);
            ftpEntry.setType(dir ? FTPEntry.TYPE_DIRECTORY : FTPEntry.TYPE_FILE);
            ret.add(ftpEntry);
        }
        return ret;
    }
}
