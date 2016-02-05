package org.xbib.graphics.vector.intermediate.filters;

import org.junit.Test;
import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.commands.DrawShapeCommand;
import org.xbib.graphics.vector.intermediate.commands.SetColorCommand;
import org.xbib.graphics.vector.intermediate.commands.SetStrokeCommand;
import org.xbib.graphics.vector.intermediate.commands.SetTransformCommand;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterTest {
    @Test
    public void filterNone() {
        List<Command<?>> stream = new LinkedList<Command<?>>();
        stream.add(new SetColorCommand(Color.BLACK));
        stream.add(new SetStrokeCommand(new BasicStroke(1f)));
        stream.add(new DrawShapeCommand(new Line2D.Double(0.0, 1.0, 10.0, 11.0)));
        stream.add(new SetTransformCommand(AffineTransform.getTranslateInstance(5.0, 5.0)));
        stream.add(new DrawShapeCommand(new Line2D.Double(0.0, 1.0, 5.0, 6.0)));

        Iterator<Command<?>> unfiltered = stream.iterator();

        Filter filtered = new Filter(stream) {
            @Override
            protected List<Command<?>> filter(Command<?> command) {
                return Arrays.<Command<?>>asList(command);
            }
        };

        while (filtered.hasNext() || unfiltered.hasNext()) {
            Command<?> expected = unfiltered.next();
            Command<?> result = filtered.next();
            assertEquals(expected, result);
        }
    }

    @Test
    public void filterAll() {
        List<Command<?>> stream = new LinkedList<Command<?>>();
        stream.add(new SetColorCommand(Color.BLACK));
        stream.add(new SetStrokeCommand(new BasicStroke(1f)));
        stream.add(new DrawShapeCommand(new Line2D.Double(0.0, 1.0, 10.0, 11.0)));
        stream.add(new SetTransformCommand(AffineTransform.getTranslateInstance(5.0, 5.0)));
        stream.add(new DrawShapeCommand(new Line2D.Double(0.0, 1.0, 5.0, 6.0)));

        Iterator<Command<?>> unfiltered = stream.iterator();

        Filter filtered = new Filter(stream) {
            @Override
            protected List<Command<?>> filter(Command<?> command) {
                return null;
            }
        };

        assertTrue(unfiltered.hasNext());
        assertFalse(filtered.hasNext());
    }

    @Test
    public void duplicate() {
        List<Command<?>> stream = new LinkedList<Command<?>>();
        stream.add(new SetColorCommand(Color.BLACK));
        stream.add(new SetStrokeCommand(new BasicStroke(1f)));
        stream.add(new DrawShapeCommand(new Line2D.Double(0.0, 1.0, 10.0, 11.0)));
        stream.add(new SetTransformCommand(AffineTransform.getTranslateInstance(5.0, 5.0)));
        stream.add(new DrawShapeCommand(new Line2D.Double(0.0, 1.0, 5.0, 6.0)));

        Iterator<Command<?>> unfiltered = stream.iterator();

        Filter filtered = new Filter(stream) {
            @Override
            protected List<Command<?>> filter(Command<?> command) {
                return Arrays.asList(command, command);
            }
        };

        while (filtered.hasNext() || unfiltered.hasNext()) {
            Command<?> expected = unfiltered.next();
            Command<?> result1 = filtered.next();
            Command<?> result2 = filtered.next();
            assertEquals(expected, result1);
            assertEquals(expected, result2);
        }
    }
}

