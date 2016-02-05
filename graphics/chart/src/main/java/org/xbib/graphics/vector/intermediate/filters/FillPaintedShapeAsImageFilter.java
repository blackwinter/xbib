package org.xbib.graphics.vector.intermediate.filters;

import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.commands.DisposeCommand;
import org.xbib.graphics.vector.intermediate.commands.DrawImageCommand;
import org.xbib.graphics.vector.intermediate.commands.FillShapeCommand;
import org.xbib.graphics.vector.intermediate.commands.SetPaintCommand;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class FillPaintedShapeAsImageFilter extends Filter {
    private SetPaintCommand lastSetPaintCommand;

    public FillPaintedShapeAsImageFilter(Iterable<Command<?>> stream) {
        super(stream);
    }

    @Override
    public Command<?> next() {
        Command<?> nextCommand = super.next();

        if (nextCommand instanceof SetPaintCommand) {
            lastSetPaintCommand = (SetPaintCommand) nextCommand;
        } else if (nextCommand instanceof DisposeCommand) {
            lastSetPaintCommand = null;
        }

        return nextCommand;
    }

    private DrawImageCommand getDrawImageCommand(FillShapeCommand shapeCommand, SetPaintCommand paintCommand) {
        Shape shape = shapeCommand.getValue();
        Rectangle2D shapeBounds = shape.getBounds2D();
        double x = shapeBounds.getX();
        double y = shapeBounds.getY();
        double width = shapeBounds.getWidth();
        double height = shapeBounds.getHeight();
        int imageWidth = (int) Math.round(width);
        int imageHeight = (int) Math.round(height);
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = (Graphics2D) image.getGraphics();
        imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        imageGraphics.scale(imageWidth / width, imageHeight / height);
        imageGraphics.translate(-shapeBounds.getX(), -shapeBounds.getY());
        imageGraphics.setPaint(paintCommand.getValue());
        imageGraphics.fill(shape);
        imageGraphics.dispose();

        DrawImageCommand drawImageCommand = new DrawImageCommand(image, imageWidth, imageHeight, x, y, width, height);
        return drawImageCommand;
    }

    @Override
    protected List<Command<?>> filter(Command<?> command) {
        if (lastSetPaintCommand != null && command instanceof FillShapeCommand) {
            FillShapeCommand fillShapeCommand = (FillShapeCommand) command;
            DrawImageCommand drawImageCommand = getDrawImageCommand(fillShapeCommand, lastSetPaintCommand);
            return Arrays.<Command<?>>asList(drawImageCommand);
        }

        return Arrays.<Command<?>>asList(command);
    }
}

