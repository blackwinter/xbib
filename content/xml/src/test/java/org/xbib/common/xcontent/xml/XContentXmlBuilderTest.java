package org.xbib.common.xcontent.xml;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.common.settings.Settings;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.xml.namespace.XmlNamespaceContext;

import javax.xml.namespace.QName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import static org.xbib.common.settings.Settings.settingsBuilder;

public class XContentXmlBuilderTest extends Assert {

    @Test
    public void testEmpty() throws Exception {
        QName root = new QName("root");
        XContentBuilder builder = XmlXContent.contentBuilder(new XmlXParams(root));
        builder.startObject().field("Hello", "World").endObject();
        assertEquals(builder.string(),
                "<root><Hello>World</Hello></root>"
        );
    }

    @Test
    public void testContextNamespace() throws Exception {
        QName root = new QName("root");
        XmlNamespaceContext context = XmlNamespaceContext.newInstance();
        XContentBuilder builder = XmlXContent.contentBuilder(new XmlXParams(root, context));
        builder.startObject().field("Hello", "World").endObject();
        assertEquals(builder.string(),
                "<root><Hello>World</Hello></root>"
        );
    }

    @Test
    public void testXml() throws Exception {
        XContentBuilder builder = XmlXContent.contentBuilder();
        builder.startObject().field("Hello", "World").endObject();
        assertEquals(builder.string(),
                "<root><Hello>World</Hello></root>"
        );
    }

    @Test
    public void testXmlParams() throws Exception {
        XmlXParams params = new XmlXParams();
        XContentBuilder builder = XmlXContent.contentBuilder(params);
        builder.startObject().field("Hello", "World").endObject();
        assertEquals(builder.string(),
                "<root><Hello>World</Hello></root>"
        );
    }

    @Test
    public void testDefaultNamespaces() throws Exception {
        XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();
        XmlXParams params = new XmlXParams(context);
        XContentBuilder builder = XmlXContent.contentBuilder(params);
        builder.startObject()
                .field("dc:creator", "John Doe")
                .endObject();
        assertEquals(builder.string(),
                "<root xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:xalan=\"http://xml.apache.org/xslt\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"><dc:creator>John Doe</dc:creator></root>"
        );
    }

    @Test
    public void testCustomNamespaces() throws Exception {
        QName root = new QName("result");
        XmlNamespaceContext context = XmlNamespaceContext.newInstance();
        context.addNamespace("abc", "http://localhost");
        XmlXParams params = new XmlXParams(root, context);
        XContentBuilder builder = XmlXContent.contentBuilder(params);
        builder.startObject()
                .field("abc:creator", "John Doe")
                .endObject();
        assertEquals(builder.string(),
                "<result xmlns:abc=\"http://localhost\"><abc:creator>John Doe</abc:creator></result>"
        );
    }

    @Test
    public void testRootNamespace() throws Exception {
        QName root = new QName("http://content", "root", "abc");
        XmlNamespaceContext context = XmlNamespaceContext.newInstance();
        context.addNamespace("", "http://localhost");
        context.addNamespace("abc", "http://content");
        XmlXParams params = new XmlXParams(root, context);
        XContentBuilder builder = XmlXContent.contentBuilder(params);
        builder.startObject()
                .field("creator", "John Doe")
                .endObject();
        assertEquals(builder.string(),
                "<abc:root xmlns:abc=\"http://content\" xmlns=\"http://localhost\"><creator>John Doe</creator></abc:root>"
        );
    }

    @Test
    public void testXmlObject() throws Exception {
        QName root = XmlXParams.getDefaultParams().getRoot();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = XmlXContent.contentBuilder(params);
        builder.startObject()
                .startObject("author")
                .field("creator", "John Doe")
                .field("role", "writer")
                .endObject()
                .startObject("author")
                .field("creator", "Joe Smith")
                .field("role", "illustrator")
                .endObject()
                .endObject();
        assertEquals(builder.string(),
            "<root><author><creator>John Doe</creator><role>writer</role></author><author><creator>Joe Smith</creator><role>illustrator</role></author></root>"
        );

    }

    @Test
    public void testXmlAttributes() throws Exception {
        QName root = XmlXParams.getDefaultParams().getRoot();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = XmlXContent.contentBuilder(params);
        builder.startObject()
                .startObject("author")
                .field("@name", "John Doe")
                .field("@id", 1)
                .endObject()
                .endObject();
        assertEquals(builder.string(),
            "<root><author><name>John Doe</name><id>1</id></author></root>"
        );
    }


    @Test
    public void testXmlArrayOfValues() throws Exception {
        QName root = XmlXParams.getDefaultParams().getRoot();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = XmlXContent.contentBuilder(params);
        builder.startObject()
                .array("author", "John Doe", "Joe Smith")
                .endObject();
        assertEquals(builder.string(),
            "<root><author>John Doe</author><author>Joe Smith</author></root>"
        );
    }

    @Test
    public void testXmlArrayOfObjects() throws Exception {
        QName root = XmlXParams.getDefaultParams().getRoot();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = XmlXContent.contentBuilder(params);
        builder.startObject()
                .startArray("author")
                .startObject()
                .field("creator", "John Doe")
                .field("role", "writer")
                .endObject()
                .startObject()
                .field("creator", "Joe Smith")
                .field("role", "illustrator")
                .endObject()
                .endArray()
                .endObject();
        assertEquals(builder.string(),
                "<root><author><creator>John Doe</creator><role>writer</role></author><author><creator>Joe Smith</creator><role>illustrator</role></author></root>"
        );
    }

    @Test
    public void testParseJson() throws Exception {
        XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();
        context.addNamespace("bib","info:srw/cql-context-set/1/bib-v1/");
        context.addNamespace("xbib", "http://xbib.org/");
        context.addNamespace("abc", "http://localhost/");
        context.addNamespace("lia", "http://xbib.org/namespaces/lia/");
        InputStream in = getClass().getResourceAsStream("/org/xbib/common/xcontent/dc.json");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        byte[] buf = out.toByteArray();
    }

    @Test
    public void testParseReader() throws Exception {
        StringReader sr = new StringReader("{\"name\":\"value\"}");
        Settings settings = settingsBuilder()
                .loadFromReader(sr).build();
    }

    @Test
    public void testInvalidWhiteSpaceCharacter() throws Exception {
        QName root = new QName("root");
        XContentBuilder builder = XmlXContent.contentBuilder(new XmlXParams(root));
        builder.startObject().field("Hello", "World\u001b").endObject();
        assertEquals(builder.string(),
                "<root><Hello>World�</Hello></root>"
        );
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }
}
