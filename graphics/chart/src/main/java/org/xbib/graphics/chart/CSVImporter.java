package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.style.Styler.ChartTheme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This class is used to create a Chart object from a folder containing one or more CSV files. The parent folder's name
 * becomes the title of the chart. Each CSV file in the folder becomes a series on the chart.
 * The CSV file's name becomes the series' name.
 */
public class CSVImporter {

    public static ChartXY getChartFromCSVDir(Path path, DataOrientation dataOrientation, int width, int height)
     throws IOException {
        return getChartFromCSVDir(path, dataOrientation, width, height, null);
    }

    public static ChartXY getChartFromCSVDir(Path path, DataOrientation dataOrientation, int width, int height,
                                             ChartTheme chartTheme) throws IOException {
        ChartXY chart;
        if (chartTheme != null) {
            chart = new ChartXY(width, height, chartTheme);
        } else {
            chart = new ChartXY(width, height);
        }
        final PathMatcher pathMatcher = path.getFileSystem().getPathMatcher("glob:*.csv");
        Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(file.getFileName())) {
                    String[] xAndYData = null;
                    if (dataOrientation == DataOrientation.Rows) {
                        xAndYData = getSeriesDataFromCSVRows(Files.newInputStream(file));
                    } else {
                        xAndYData = getSeriesDataFromCSVColumns(Files.newInputStream(file));
                    }
                    if (xAndYData[2] == null || xAndYData[2].trim().equalsIgnoreCase("")) {
                        chart.addSeries(file.toString().substring(0, file.toString().indexOf(".csv")),
                                getAxisData(xAndYData[0]), getAxisData(xAndYData[1]));
                    } else {
                        chart.addSeries(file.toString().substring(0, file.toString().indexOf(".csv")),
                                getAxisData(xAndYData[0]), getAxisData(xAndYData[1]), getAxisData(xAndYData[2]));
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return chart;
    }

    private static String[] getSeriesDataFromCSVRows(InputStream inputStream) throws IOException {
        String[] xAndYData = new String[3];
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                Charset.forName("UTF-8")))) {
            int counter = 0;
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                xAndYData[counter++] = line;
            }
        }
        return xAndYData;
    }

    private static String[] getSeriesDataFromCSVColumns(InputStream inputStream) throws IOException {
        String[] xAndYData = new String[3];
        xAndYData[0] = "";
        xAndYData[1] = "";
        xAndYData[2] = "";
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                Charset.forName("UTF-8")))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] dataArray = line.split(",");
                xAndYData[0] += dataArray[0] + ",";
                xAndYData[1] += dataArray[1] + ",";
                if (dataArray.length > 2) {
                    xAndYData[2] += dataArray[2] + ",";
                }
            }
        }
        return xAndYData;
    }

    private static List<Number> getAxisData(String stringData) {
        List<Number> axisData = new ArrayList<>();
        String[] stringDataArray = stringData.split(",");
        for (String dataPoint : stringDataArray) {
            axisData.add(Double.parseDouble(dataPoint));
        }
        return axisData;
    }

    public enum DataOrientation {

        Rows, Columns
    }

}
