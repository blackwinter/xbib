package org.asynchttpclient.multipart;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is an adaptation of the Apache HttpClient implementation
 * 
 * @link http://hc.apache.org/httpclient-3.x/
 */
public class FilePartSource implements PartSource {

    /**
     * File part file.
     */
    private File file = null;

    /**
     * File part file name.
     */
    private String fileName = null;

    /**
     * Constructor for FilePartSource.
     * 
     * @param file the FilePart source File.
     * @throws java.io.FileNotFoundException if the file does not exist or cannot be read
     */
    public FilePartSource(File file) throws FileNotFoundException {
        this.file = file;
        if (file != null) {
            if (!file.isFile()) {
                final String errorMessage = String.format("File is not a normal file (%s).", file.getAbsolutePath());
                throw new FileNotFoundException(errorMessage);
            }
            if (!file.canRead()) {
                final String errorMessage = String.format("File is not readable (%s).", file.getAbsolutePath());
                throw new FileNotFoundException(errorMessage);
            }
            this.fileName = file.getName();
        }
    }

    /**
     * Constructor for FilePartSource.
     * 
     * @param fileName the file name of the FilePart
     * @param file the source File for the FilePart
     * @throws java.io.FileNotFoundException if the file does not exist or cannot be read
     */
    public FilePartSource(String fileName, File file) throws FileNotFoundException {
        this(file);
        this.fileName = fileName;
    }

    /**
     * Return the length of the file
     * 
     * @return the length of the file.
     * @see org.asynchttpclient.multipart.PartSource#getLength()
     */
    public long getLength() {
        if (this.file != null) {
            return this.file.length();
        } else {
            return 0;
        }
    }

    /**
     * Return the current filename
     * 
     * @return the filename.
     * @see org.asynchttpclient.multipart.PartSource#getFileName()
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Return a new {@link java.io.FileInputStream} for the current filename.
     * 
     * @return the new input stream.
     * @throws java.io.IOException If an IO problem occurs.
     * @see org.asynchttpclient.multipart.PartSource#createInputStream()
     */
    public InputStream createInputStream() throws IOException {
        if (this.file != null) {
            return new FileInputStream(this.file);
        } else {
            return new ByteArrayInputStream(new byte[] {});
        }
    }

    public File getFile() {
        return file;
    }

}
