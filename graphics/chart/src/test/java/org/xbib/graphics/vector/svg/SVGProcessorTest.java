package org.xbib.graphics.vector.svg;

import org.junit.Test;
import org.xbib.graphics.vector.Document;
import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.commands.DrawShapeCommand;
import org.xbib.graphics.vector.intermediate.commands.FillShapeCommand;
import org.xbib.graphics.vector.util.PageSize;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.xbib.graphics.vector.TestUtils.assertXMLEquals;

public class SVGProcessorTest {
    private static final String EOL = "\n";
    private static final String HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + EOL +
                    "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">" + EOL +
                    "<svg height=\"10.583332098611255mm\" version=\"1.1\" viewBox=\"0 10 20 30\" width=\"7.0555547324075025mm\" x=\"0mm\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" y=\"3.5277773662037513mm\">" + EOL;
    private static final String FOOTER = "</svg>";
    private static final PageSize PAGE_SIZE = new PageSize(0.0, 10.0, 20.0, 30.0);

    private final SVGProcessor processor = new SVGProcessor();
    private final List<Command<?>> commands = new LinkedList<Command<?>>();
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    private String process(Command<?>... commands) throws IOException {
        Collections.addAll(this.commands, commands);
        Document processed = processor.process(this.commands, PAGE_SIZE);
        processed.write(bytes);
        return bytes.toString("UTF-8");
    }

    @Test
    public void envelopeForEmptyDocument() throws Exception {
        String result = process();
        String expected = HEADER.replaceAll(">$", "/>");
        assertXMLEquals(expected, result);
    }

    @Test
    public void drawShapeBlack() throws Exception {
        String result = process(
                new DrawShapeCommand(new Rectangle2D.Double(1, 2, 3, 4))
        );
        String expected =
                HEADER + EOL +
                        "  <rect height=\"4\" style=\"fill:none;stroke:rgb(255,255,255);stroke-miterlimit:10;stroke-linecap:square;\" width=\"3\" x=\"1\" y=\"2\"/>" + EOL +
                        FOOTER;
        assertXMLEquals(expected, result);
    }

    @Test
    public void fillShapeBlack() throws Exception {
        String result = process(
                new FillShapeCommand(new Rectangle2D.Double(1, 2, 3, 4))
        );
        String expected =
                HEADER + EOL +
                        "  <rect height=\"4\" style=\"fill:rgb(255,255,255);stroke:none;\" width=\"3\" x=\"1\" y=\"2\"/>" + EOL +
                        FOOTER;
        assertXMLEquals(expected, result);
    }
}
