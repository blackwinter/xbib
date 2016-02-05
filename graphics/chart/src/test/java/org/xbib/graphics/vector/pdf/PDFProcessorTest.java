package org.xbib.graphics.vector.pdf;

import org.junit.Test;
import org.xbib.graphics.vector.Document;
import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.util.PageSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static org.xbib.graphics.vector.TestUtils.Template;
import static org.xbib.graphics.vector.TestUtils.assertTemplateEquals;

public class PDFProcessorTest {
    private static final String EOL = "\n";
    private static final String HEADER = "%PDF-1.4";
    private static final String FOOTER = "%%EOF";
    private static final PageSize PAGE_SIZE = new PageSize(0.0, 10.0, 20.0, 30.0);

    private final PDFProcessor processor = new PDFProcessor();
    private final List<Command<?>> commands = new LinkedList<Command<?>>();
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    private String process(Command<?>... commands) throws IOException {
        Collections.addAll(this.commands, commands);
        Document processed = processor.process(this.commands, PAGE_SIZE);
        processed.write(bytes);
        return bytes.toString("ISO-8859-1");
    }

    @Test
    public void envelopeForEmptyDocument() throws IOException {
        String result = process();
        Template actual = new Template(result.split(EOL));
        Template expected = new Template(
                HEADER,
                "1 0 obj",
                "<<",
                "/Type /Catalog",
                "/Pages 2 0 R",
                ">>",
                "endobj",
                "2 0 obj",
                "<<",
                "/Type /Pages",
                "/Kids [3 0 R]",
                "/Count 1",
                ">>",
                "endobj",
                "3 0 obj",
                "<<",
                "/Type /Page",
                "/Parent 2 0 R",
                "/MediaBox [0 28.34645669291339 56.69291338582678 85.03937007874016]",
                "/Contents 4 0 R",
                "/Resources 6 0 R",
                ">>",
                "endobj",
                "4 0 obj",
                "<<",
                "/Length 5 0 R",
                ">>",
                "stream",
                "q",
                "1 1 1 rg 1 1 1 RG",
                "2.834645669291339 0 0 -2.834645669291339 0 85.03937007874016 cm",
                "/Fnt0 12.0 Tf",
                "Q",
                "endstream",
                "endobj",
                "5 0 obj",
                "100",
                "endobj",
                "6 0 obj",
                "<<",
                "/ProcSet [/PDF /Text /ImageB /ImageC /ImageI]",
                "/Font <<",
                "/Fnt0 <<",
                "/Type /Font",
                "/Subtype /TrueType",
                "/Encoding /WinAnsiEncoding",
                Pattern.compile("/BaseFont /\\S+"),
                ">>",
                ">>",
                ">>",
                "endobj",
                "xref",
                "0 7",
                "0000000000 65535 f ",
                Pattern.compile("\\d{10} 00000 n "),
                Pattern.compile("\\d{10} 00000 n "),
                Pattern.compile("\\d{10} 00000 n "),
                Pattern.compile("\\d{10} 00000 n "),
                Pattern.compile("\\d{10} 00000 n "),
                Pattern.compile("\\d{10} 00000 n "),
                "trailer",
                "<<",
                "/Size 7",
                "/Root 1 0 R",
                ">>",
                "startxref",
                Pattern.compile("[1-9]\\d*"),
                FOOTER
        );

        assertTemplateEquals(expected, actual);
    }
}

