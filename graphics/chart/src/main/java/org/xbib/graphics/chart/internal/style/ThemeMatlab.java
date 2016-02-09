package org.xbib.graphics.chart.internal.style;

import org.xbib.graphics.chart.StylerPie.AnnotationType;
import org.xbib.graphics.chart.internal.style.Styler.LegendPosition;
import org.xbib.graphics.chart.internal.style.colors.ChartColor;
import org.xbib.graphics.chart.internal.style.colors.MatlabSeriesColors;
import org.xbib.graphics.chart.internal.style.lines.MatlabSeriesLines;
import org.xbib.graphics.chart.internal.style.markers.Marker;
import org.xbib.graphics.chart.internal.style.markers.MatlabSeriesMarkers;

import java.awt.*;

public class ThemeMatlab implements Theme {

    @Override
    public Color getChartBackgroundColor() {

        return ChartColor.getAWTColor(ChartColor.WHITE);
    }

    @Override
    public Color getChartFontColor() {

        return ChartColor.getAWTColor(ChartColor.BLACK);
    }

    @Override
    public int getChartPadding() {

        return 10;
    }

    @Override
    public Marker[] getSeriesMarkers() {

        return new MatlabSeriesMarkers().getSeriesMarkers();

    }

    @Override
    public BasicStroke[] getSeriesLines() {

        return new MatlabSeriesLines().getSeriesLines();

    }

    @Override
    public Color[] getSeriesColors() {

        return new MatlabSeriesColors().getSeriesColors();

    }

    // Chart Title

    @Override
    public Font getChartTitleFont() {

        return new Font(Font.SANS_SERIF, Font.BOLD, 14);
    }

    @Override
    public boolean isChartTitleVisible() {

        return true;
    }

    @Override
    public boolean isChartTitleBoxVisible() {

        return false;
    }

    @Override
    public Color getChartTitleBoxBackgroundColor() {

        return ChartColor.getAWTColor(ChartColor.WHITE);
    }

    @Override
    public Color getChartTitleBoxBorderColor() {

        return ChartColor.getAWTColor(ChartColor.WHITE);
    }

    @Override
    public int getChartTitlePadding() {

        return 5;
    }

    // Chart Legend

    @Override
    public Font getLegendFont() {

        return new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    }

    @Override
    public boolean isLegendVisible() {

        return true;
    }

    @Override
    public Color getLegendBackgroundColor() {

        return ChartColor.getAWTColor(ChartColor.WHITE);

    }

    @Override
    public Color getLegendBorderColor() {

        return ChartColor.getAWTColor(ChartColor.BLACK);
    }

    @Override
    public int getLegendPadding() {

        return 10;
    }

    @Override
    public int getLegendSeriesLineLength() {

        return 24;
    }

    @Override
    public LegendPosition getLegendPosition() {

        return LegendPosition.OutsideE;
    }

    // Chart Axes

    @Override
    public boolean isXAxisTitleVisible() {

        return true;
    }

    @Override
    public boolean isYAxisTitleVisible() {

        return true;
    }

    @Override
    public Font getAxisTitleFont() {

        return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    }

    @Override
    public boolean isXAxisTicksVisible() {

        return true;
    }

    @Override
    public boolean isYAxisTicksVisible() {

        return true;
    }

    @Override
    public Font getAxisTickLabelsFont() {

        return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    }

    @Override
    public int getAxisTickMarkLength() {

        return 5;
    }

    @Override
    public int getAxisTickPadding() {

        return 4;
    }

    @Override
    public int getPlotMargin() {

        return 3;
    }

    @Override
    public Color getAxisTickMarksColor() {

        return ChartColor.getAWTColor(ChartColor.BLACK);
    }

    @Override
    public Stroke getAxisTickMarksStroke() {

        return new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, new float[]{3.0f, 0.0f}, 0.0f);
    }

    @Override
    public Color getAxisTickLabelsColor() {

        return ChartColor.getAWTColor(ChartColor.BLACK);
    }

    @Override
    public boolean isAxisTicksLineVisible() {

        return false;
    }

    @Override
    public boolean isAxisTicksMarksVisible() {

        return false;
    }

    @Override
    public int getAxisTitlePadding() {

        return 10;
    }

    @Override
    public int getXAxisTickMarkSpacingHint() {

        return 74;
    }

    @Override
    public int getYAxisTickMarkSpacingHint() {

        return 44;
    }

    // Chart Plot Area

    @Override
    public boolean isPlotGridLinesVisible() {

        return true;
    }

    @Override
    public boolean isPlotGridVerticalLinesVisible() {

        return true;
    }

    @Override
    public boolean isPlotGridHorizontalLinesVisible() {

        return true;
    }

    @Override
    public Color getPlotBackgroundColor() {

        return ChartColor.getAWTColor(ChartColor.WHITE);
    }

    @Override
    public Color getPlotBorderColor() {

        return ChartColor.getAWTColor(ChartColor.BLACK);
    }

    @Override
    public boolean isPlotBorderVisible() {

        return true;
    }

    @Override
    public boolean isPlotTicksMarksVisible() {

        return true;
    }

    @Override
    public Color getPlotGridLinesColor() {

        return ChartColor.getAWTColor(ChartColor.BLACK);
    }

    @Override
    public Stroke getPlotGridLinesStroke() {

        return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[]{1.0f, 2.0f}, 0.0f);

    }

    @Override
    public double getPlotContentSize() {

        return .92;
    }

    // Bar Charts

    @Override
    public double getBarWidthPercentage() {

        return 0.9;
    }

    @Override
    public boolean isBarsOverlapped() {

        return false;
    }

    // Pie Charts

    @Override
    public boolean isCircular() {

        return true;
    }

    @Override
    public double getStartAngleInDegrees() {

        return 0;
    }

    @Override
    public Font getPieFont() {

        return new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    }

    @Override
    public double getAnnotationDistance() {
        return .67;
    }

    @Override
    public AnnotationType getAnnotationType() {
        return AnnotationType.Label;
    }

    // Line, Scatter, Area Charts

    @Override
    public int getMarkerSize() {
        return 8;
    }

    // Error Bars

    @Override
    public Color getErrorBarsColor() {
        return ChartColor.getAWTColor(ChartColor.BLACK);
    }

    @Override
    public boolean isErrorBarsColorSeriesColor() {
        return false;
    }

}
