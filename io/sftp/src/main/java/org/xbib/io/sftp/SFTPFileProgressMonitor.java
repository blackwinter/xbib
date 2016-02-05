package org.xbib.io.sftp;

import com.jcraft.jsch.SftpProgressMonitor;

public class SFTPFileProgressMonitor implements SftpProgressMonitor {

    private long max; // the final count (i.e. length of file to transfer).

    private  String destinationFileName;

    private long count; // the number of bytes transferred so far

    public void init(int i, String src, String destinationFileName, long max) {
        this.destinationFileName = destinationFileName;
        this.max = max;
    }

    // Called periodically as more data is transfered.
    public boolean count(long count) {

        this.count = count;
        // true if the transfer should go on, false if the transfer should be cancelled.
        return true;
    }

    // called when the transfer ended, either because all the data was transferred,
    // or because the transfer was cancelled.
    public void end() {
    }

    public long getCount() {
        return count;
    }

}
