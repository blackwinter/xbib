package org.xbib.chart;

import org.junit.Test;
import org.xbib.graphics.chart.ChartBuilderXY;
import org.xbib.graphics.chart.ChartXY;
import org.xbib.graphics.chart.SwingWrapper;
import org.xbib.graphics.chart.VectorGraphicsEncoder;
import org.xbib.graphics.chart.internal.style.Styler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MatlabTest {

    @Test
    public void testMatlabInstants() throws IOException, ParseException {
        ChartXY chart = new ChartBuilderXY().width(800).height(600)
                .theme(Styler.ChartTheme.Matlab)
                .title("Matlab Theme")
                .xAxisTitle("X")
                .yAxisTitle("Y")
                .build();
        chart.getStyler().setPlotGridLinesVisible(false);
        chart.getStyler().setXAxisTickMarkSpacingHint(100);
        chart.getStyler().setDatePattern("HH:mm:ss");
        List<Instant> xData = new ArrayList<>();
        List<Double> y1Data = new ArrayList<>();
        List<Double> y2Data = new ArrayList<>();

        Instant instant;
        instant = Instant.parse("2012-08-01T00:00:00Z");
        xData.add(instant);
        y1Data.add(120d);
        y2Data.add(15d);

        instant = Instant.parse("2012-08-01T01:00:00Z");
        xData.add(instant);
        y1Data.add(165d);
        y2Data.add(15d);

        instant = Instant.parse("2012-08-01T02:00:00Z");
        xData.add(instant);
        y1Data.add(210d);
        y2Data.add(20d);

        instant = Instant.parse("2012-08-01T03:00:00Z");
        xData.add(instant);
        y1Data.add(400d);
        y2Data.add(30d);

        instant = Instant.parse("2012-08-01T04:00:00Z");
        xData.add(instant);
        y1Data.add(800d);
        y2Data.add(100d);

        instant = Instant.parse("2012-08-01T05:00:00Z");
        xData.add(instant);
        y1Data.add(2000d);
        y2Data.add(120d);

        instant = Instant.parse("2012-08-01T06:00:00Z");
        xData.add(instant);
        y1Data.add(3000d);
        y2Data.add(150d);

        chart.addSeries("downloads", xData, y1Data);
        chart.addSeries("price", xData, y2Data);

        /*VectorGraphicsEncoder.write(chart, Files.newOutputStream(Paths.get("test.svg")),
                VectorGraphicsEncoder.VectorGraphicsFormat.SVG );
        new SwingWrapper(chart).displayChart();
        */
    }
}
