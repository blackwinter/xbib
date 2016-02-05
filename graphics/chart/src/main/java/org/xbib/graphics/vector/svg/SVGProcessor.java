package org.xbib.graphics.vector.svg;

import org.xbib.graphics.vector.Document;
import org.xbib.graphics.vector.Processor;
import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.filters.FillPaintedShapeAsImageFilter;
import org.xbib.graphics.vector.intermediate.filters.StateChangeGroupingFilter;
import org.xbib.graphics.vector.util.PageSize;

public class SVGProcessor implements Processor {
    public Document process(Iterable<Command<?>> commands, PageSize pageSize) {
        FillPaintedShapeAsImageFilter shapesAsImages = new FillPaintedShapeAsImageFilter(commands);
        Iterable<Command<?>> filtered = new StateChangeGroupingFilter(shapesAsImages);
        SVGDocument doc = new SVGDocument(pageSize);
        for (Command<?> command : filtered) {
            doc.handle(command);
        }
        doc.close();
        return doc;
    }
}
