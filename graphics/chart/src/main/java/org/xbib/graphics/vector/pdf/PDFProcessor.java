package org.xbib.graphics.vector.pdf;

import org.xbib.graphics.vector.Document;
import org.xbib.graphics.vector.Processor;
import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.filters.AbsoluteToRelativeTransformsFilter;
import org.xbib.graphics.vector.intermediate.filters.FillPaintedShapeAsImageFilter;
import org.xbib.graphics.vector.intermediate.filters.StateChangeGroupingFilter;
import org.xbib.graphics.vector.util.PageSize;

public class PDFProcessor implements Processor {

    public Document process(Iterable<Command<?>> commands, PageSize pageSize) {
        AbsoluteToRelativeTransformsFilter absoluteToRelativeTransformsFilter = new AbsoluteToRelativeTransformsFilter(commands);
        FillPaintedShapeAsImageFilter paintedShapeAsImageFilter = new FillPaintedShapeAsImageFilter(absoluteToRelativeTransformsFilter);
        Iterable<Command<?>> filtered = new StateChangeGroupingFilter(paintedShapeAsImageFilter);
        PDFDocument doc = new PDFDocument(pageSize);
        for (Command<?> command : filtered) {
            doc.handle(command);
        }
        doc.close();
        return doc;
    }
}

