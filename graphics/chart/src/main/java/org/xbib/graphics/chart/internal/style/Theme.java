package org.xbib.graphics.chart.internal.style;

import org.xbib.graphics.chart.StylerPie.AnnotationType;
import org.xbib.graphics.chart.internal.style.Styler.LegendPosition;
import org.xbib.graphics.chart.internal.style.colors.SeriesColors;
import org.xbib.graphics.chart.internal.style.lines.SeriesLines;
import org.xbib.graphics.chart.internal.style.markers.SeriesMarkers;

import java.awt.*;

public interface Theme extends SeriesMarkers, SeriesLines, SeriesColors {

    // Chart Style ///////////////////////////////

    public Color getChartBackgroundColor();

    public Color getChartFontColor();

    public int getChartPadding();

    // Chart Title ///////////////////////////////

    public Font getChartTitleFont();

    public boolean isChartTitleVisible();

    public boolean isChartTitleBoxVisible();

    public Color getChartTitleBoxBackgroundColor();

    public Color getChartTitleBoxBorderColor();

    public int getChartTitlePadding();

    // Chart Legend ///////////////////////////////

    public Font getLegendFont();

    public boolean isLegendVisible();

    public Color getLegendBackgroundColor();

    public Color getLegendBorderColor();

    public int getLegendPadding();

    public int getLegendSeriesLineLength();

    public LegendPosition getLegendPosition();

    // Chart Axes ///////////////////////////////

    public boolean isXAxisTitleVisible();

    public boolean isYAxisTitleVisible();

    public Font getAxisTitleFont();

    public boolean isXAxisTicksVisible();

    public boolean isYAxisTicksVisible();

    public Font getAxisTickLabelsFont();

    public int getAxisTickMarkLength();

    public int getAxisTickPadding();

    public Color getAxisTickMarksColor();

    public Stroke getAxisTickMarksStroke();

    public Color getAxisTickLabelsColor();

    public boolean isAxisTicksLineVisible();

    public boolean isAxisTicksMarksVisible();

    public int getAxisTitlePadding();

    public int getXAxisTickMarkSpacingHint();

    public int getYAxisTickMarkSpacingHint();

    // Chart Plot Area ///////////////////////////////

    public boolean isPlotGridLinesVisible();

    public boolean isPlotGridVerticalLinesVisible();

    public boolean isPlotGridHorizontalLinesVisible();

    public Color getPlotBackgroundColor();

    public Color getPlotBorderColor();

    public boolean isPlotBorderVisible();

    public Color getPlotGridLinesColor();

    public Stroke getPlotGridLinesStroke();

    public boolean isPlotTicksMarksVisible();

    public double getPlotContentSize();

    public int getPlotMargin();

    // Bar Charts ///////////////////////////////

    public double getBarWidthPercentage();

    public boolean isBarsOverlapped();

    // Pie Charts ///////////////////////////////

    public boolean isCircular();

    public double getStartAngleInDegrees();

    public Font getPieFont();

    public double getAnnotationDistance();

    AnnotationType getAnnotationType();

    // Line, Scatter, Area Charts ///////////////////////////////

    public int getMarkerSize();

    // Error Bars ///////////////////////////////

    public Color getErrorBarsColor();

    public boolean isErrorBarsColorSeriesColor();

}
