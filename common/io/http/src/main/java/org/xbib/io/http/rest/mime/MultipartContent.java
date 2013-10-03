package org.xbib.io.http.rest.mime;

import org.xbib.io.http.rest.AbstractContent;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Content handler for multipart data of any shape (form, mixed, alternative)
 */
public class MultipartContent extends AbstractContent {
    private String subType;
    private List<AbstractContent> parts;
    private String boundary = "jb" + UUID.randomUUID().toString();

    public MultipartContent(String aSubtype, AbstractContent... content) {
        subType = aSubtype;
        parts = new ArrayList<>(Arrays.asList(content));
    }

    protected void addContent(URLConnection con) throws IOException {
        con.setDoOutput(true);
        con.addRequestProperty("Content-Type", "multipart/" + subType + "; boundary=" + boundary);
        OutputStream os = con.getOutputStream();
        writeContent(os);
        os.close();
    }

    @Override
    public void writeContent(OutputStream os) throws IOException {
        if (parts.isEmpty()) {
            return;
        }
        for (AbstractContent c : parts) {
            os.write(ascii("--"));
            os.write(ascii(boundary));
            os.write(CRLF);
            c.writeHeader(os);
            os.write(CRLF);
            c.writeContent(os);
            os.write(CRLF);
        }
        os.write(ascii("--"));
        os.write(ascii(boundary));
        os.write(ascii("--"));
        os.write(CRLF);
    }


    @Override
    public void writeHeader(OutputStream os) throws IOException {
        os.write(ascii("Content-Type: multipart/" + subType + "; boundary=" + boundary + "\r\n"));
    }

}
