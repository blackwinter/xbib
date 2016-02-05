package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.style.Styler.ChartTheme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to create a Chart object from a folder containing one or more CSV files. The parent folder's name
 * becomes the title of the
 * chart. Each CSV file in the folder becomes a series on the chart. the CSV file's name becomes the series' name.
 */
public class CSVImporter {

    /**
     * @param path2Directory
     * @param dataOrientation
     * @param width
     * @param height
     * @param chartTheme
     * @return
     */
    public static ChartXY getChartFromCSVDir(String path2Directory, DataOrientation dataOrientation, int width, int height, ChartTheme chartTheme) {

        // 1. get the directory, name chart the dir name
        ChartXY chart = null;
        if (chartTheme != null) {
            chart = new ChartXY(width, height, chartTheme);
        } else {
            chart = new ChartXY(width, height);
        }

        // 2. get all the csv files in the dir
        File[] csvFiles = getAllFiles(path2Directory, ".*.csv");

        // 3. create a series for each file, naming the series the file name
        for (int i = 0; i < csvFiles.length; i++) {
            File csvFile = csvFiles[i];
            String[] xAndYData = null;
            if (dataOrientation == DataOrientation.Rows) {
                xAndYData = getSeriesDataFromCSVRows(csvFile);
            } else {
                xAndYData = getSeriesDataFromCSVColumns(csvFile);
            }

            if (xAndYData[2] == null || xAndYData[2].trim().equalsIgnoreCase("")) {
                chart.addSeries(csvFile.getName().substring(0, csvFile.getName().indexOf(".csv")), getAxisData(xAndYData[0]), getAxisData(xAndYData[1]));
            } else {
                chart.addSeries(csvFile.getName().substring(0, csvFile.getName().indexOf(".csv")), getAxisData(xAndYData[0]), getAxisData(xAndYData[1]), getAxisData(xAndYData[2]));
            }
        }

        return chart;
    }

    /**
     * @param path2Directory
     * @param dataOrientation
     * @param width
     * @param height
     * @return
     */
    public static ChartXY getChartFromCSVDir(String path2Directory, DataOrientation dataOrientation, int width, int height) {

        return getChartFromCSVDir(path2Directory, dataOrientation, width, height, null);
    }

    /**
     * Get the series's data from a file
     *
     * @param csvFile
     * @return
     */
    private static String[] getSeriesDataFromCSVRows(File csvFile) {

        String[] xAndYData = new String[3];

        BufferedReader bufferedReader = null;
        try {
            int counter = 0;
            String line = null;
            bufferedReader = new BufferedReader(new FileReader(csvFile));
            while ((line = bufferedReader.readLine()) != null) {
                xAndYData[counter++] = line;
            }

        } catch (Exception e) {
            System.out.println("Exception while reading csv file: " + e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return xAndYData;
    }

    /**
     * @param csvFile
     * @return
     */
    private static String[] getSeriesDataFromCSVColumns(File csvFile) {

        String[] xAndYData = new String[3];
        xAndYData[0] = "";
        xAndYData[1] = "";
        xAndYData[2] = "";

        BufferedReader bufferedReader = null;
        try {
            int counter = 0;
            String line = null;
            bufferedReader = new BufferedReader(new FileReader(csvFile));
            while ((line = bufferedReader.readLine()) != null) {
                String[] dataArray = line.split(",");
                xAndYData[0] += dataArray[0] + ",";
                xAndYData[1] += dataArray[1] + ",";
                if (dataArray.length > 2) {
                    xAndYData[2] += dataArray[2] + ",";
                }
            }

        } catch (Exception e) {
            System.out.println("Exception while reading csv file: " + e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return xAndYData;
    }

    /**
     * @param stringData
     * @return
     */
    private static List<Number> getAxisData(String stringData) {

        List<Number> axisData = new ArrayList<Number>();
        String[] stringDataArray = stringData.split(",");
        for (int i = 0; i < stringDataArray.length; i++) {
            String dataPoint = stringDataArray[i];
            try {
                Double value = Double.parseDouble(dataPoint);
                axisData.add(value);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing >" + dataPoint + "< !");
                throw (e);
            }
        }
        return axisData;
    }

    /**
     * This method returns the files found in the given directory matching the given regular expression.
     *
     * @param dirName - ex. "./path/to/directory/" *make sure you have the '/' on the end
     * @param regex   - ex. ".*.csv"
     * @return File[] - an array of files
     */
    public static File[] getAllFiles(String dirName, String regex) {

        File[] allFiles = getAllFiles(dirName);

        List<File> matchingFiles = new ArrayList<File>();

        for (int i = 0; i < allFiles.length; i++) {

            if (allFiles[i].getName().matches(regex)) {
                matchingFiles.add(allFiles[i]);
            }
        }

        return matchingFiles.toArray(new File[matchingFiles.size()]);

    }

    /**
     * This method returns the Files found in the given directory
     *
     * @param dirName - ex. "./path/to/directory/" *make sure you have the '/' on the end
     * @return File[] - an array of files
     */
    public static File[] getAllFiles(String dirName) {

        File dir = new File(dirName);

        File[] files = dir.listFiles(); // returns files and folders

        if (files != null) {
            List<File> filteredFiles = new ArrayList<File>();
            for (File file : files) {

                if (file.isFile()) {
                    filteredFiles.add(file);
                }
            }
            return filteredFiles.toArray(new File[filteredFiles.size()]);
        } else {
            System.out.println(dirName + " does not denote a valid directory!");
            return new File[0];
        }
    }

    public enum DataOrientation {

        Rows, Columns
    }

}
