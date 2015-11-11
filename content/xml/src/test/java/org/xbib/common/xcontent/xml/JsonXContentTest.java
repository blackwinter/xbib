package org.xbib.common.xcontent.xml;

import org.xbib.xml.namespace.XmlNamespaceContext;

import javax.xml.namespace.QName;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class JsonXContentTest {

    public void testJSONXmlXContent(String path) throws Exception {
        Reader r = getInput(path);
        StringWriter w = new StringWriter();
        copy(r, w);
        String json = w.toString();
        byte[] buf = json.getBytes("UTF-8");
        XmlXParams params = new XmlXParams(root(), context());
        String xml = XmlXContentHelper.convertToXml(params, buf, 0, buf.length, false);
        Writer out = getOutput("test-xmlxcontent-" + path + ".xml");
        out.write(xml);
        out.close();
        r.close();
    }

    private Reader getInput(String path) throws IOException {
        InputStream in = getClass().getResourceAsStream("/org/xbib/json/" + path + ".json");
        if (in == null) {
            throw new IOException("resource not found: " + path);
        }
        return new InputStreamReader(in, "UTF-8");
    }

    private Writer getOutput(String path) throws IOException {
        return new OutputStreamWriter(new FileOutputStream("target/" + path),"UTF-8");
    }

    private QName root() {
        return new QName("http://elasticsearch.org/ns/1.0/", "result", "es");
    }

    private XmlNamespaceContext context() {
        XmlNamespaceContext nsContext = XmlNamespaceContext.getDefaultInstance();
        nsContext.addNamespace("bib","info:srw/cql-context-set/1/bib-v1/");
        nsContext.addNamespace("xbib", "http://xbib.org/");
        nsContext.addNamespace("abc", "http://localhost/");
        nsContext.addNamespace("lia", "http://xbib.org/namespaces/lia/");
        return nsContext;
    }

    private void copy(Reader reader, Writer writer) throws IOException {
        char[] buffer = new char[1024];
        int len;
        while ((len = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, len);
        }
    }
}
