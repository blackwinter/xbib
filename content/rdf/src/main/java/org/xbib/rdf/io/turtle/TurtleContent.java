package org.xbib.rdf.io.turtle;

import org.xbib.rdf.RdfContent;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentGenerator;
import org.xbib.rdf.RdfContentParser;
import org.xbib.rdf.StandardRdfContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TurtleContent implements RdfContent {

    public final static TurtleContent turtleContent = new TurtleContent();

    public static RdfContentBuilder contentBuilder(TurtleContentParams params) throws IOException {
        return new RdfContentBuilder(turtleContent, params);
    }

    public static RdfContentBuilder contentBuilder(OutputStream out, TurtleContentParams params) throws IOException {
        return new RdfContentBuilder(turtleContent, params, out);
    }

    private TurtleContent() {
    }

    @Override
    public StandardRdfContentType type() {
        return StandardRdfContentType.TURTLE;
    }

    @Override
    public RdfContentGenerator createGenerator(OutputStream os) throws IOException {
        return new TurtleContentGenerator(os);
    }

    @Override
    public RdfContentParser createParser(InputStream in) throws IOException {
        return new TurtleContentParser(in);
    }

}
