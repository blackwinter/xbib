package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.SeriesAxesChart;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class is used to export Chart data to a path.
 */
public class CSVExporter {

    private final static String LF = System.getProperty("line.separator");

    public static void writeCSVRows(SeriesAxesChart series, Path path) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(path),
                Charset.forName("UTF-8")))) {
            String csv = join(series.getXData(), ",") + LF;
            bufferedWriter.write(csv);
            csv = join(series.getYData(), ",") + LF;
            bufferedWriter.write(csv);
            if (series.getErrorBars() != null) {
                csv = join(series.getErrorBars(), ",") + LF;
                bufferedWriter.write(csv);
            }
        }
    }

    public static void writeCSVColumns(SeriesAxesChart series, Path path) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(path),
                Charset.forName("UTF-8")))) {
            Collection<?> xData = series.getXData();
            Collection<? extends Number> yData = series.getYData();
            Collection<? extends Number> errorBarData = series.getErrorBars();
            Iterator<?> itrx = xData.iterator();
            Iterator<? extends Number> itry = yData.iterator();
            Iterator<? extends Number> itrErrorBar = null;
            if (errorBarData != null) {
                itrErrorBar = errorBarData.iterator();
            }
            while (itrx.hasNext()) {
                Number xDataPoint = (Number) itrx.next();
                Number yDataPoint = itry.next();
                Number errorBarValue = null;
                if (itrErrorBar != null) {
                    errorBarValue = itrErrorBar.next();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(xDataPoint).append(",");
                sb.append(yDataPoint).append(",");
                if (errorBarValue != null) {
                    sb.append(errorBarValue).append(",");
                }
                sb.append(LF);
                bufferedWriter.write(sb.toString());
            }
        }
    }

    private static String join(Collection collection, String separator) {
        if (collection == null) {
            return null;
        }
        Iterator iterator = collection.iterator();
        if (!iterator.hasNext()) {
            return "";
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return first == null ? "" : first.toString();
        }
        StringBuilder sb = new StringBuilder();
        if (first != null) {
            sb.append(first);
        }
        while (iterator.hasNext()) {
            if (separator != null) {
                sb.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null) {
                sb.append(obj);
            }
        }
        return sb.toString();
    }
}
