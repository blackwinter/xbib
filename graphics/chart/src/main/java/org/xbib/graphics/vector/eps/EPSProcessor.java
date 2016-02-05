package org.xbib.graphics.vector.eps;

import org.xbib.graphics.vector.Document;
import org.xbib.graphics.vector.Processor;
import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.filters.FillPaintedShapeAsImageFilter;
import org.xbib.graphics.vector.util.PageSize;

public class EPSProcessor implements Processor {
    public Document process(Iterable<Command<?>> commands, PageSize pageSize) {
        // TODO Apply rotate(theta,x,y) => translate-rotate-translate filter
        // TODO Apply image transparency => image mask filter
        // TODO Apply optimization filter
        FillPaintedShapeAsImageFilter paintedShapeAsImageFilter = new FillPaintedShapeAsImageFilter(commands);
        EPSDocument doc = new EPSDocument(pageSize);
        for (Command<?> command : paintedShapeAsImageFilter) {
            doc.handle(command);
        }
        doc.close();
        return doc;
    }
}

