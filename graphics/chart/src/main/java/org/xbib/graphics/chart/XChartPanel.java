package org.xbib.graphics.chart;

import org.xbib.graphics.chart.BitmapEncoder.BitmapFormat;
import org.xbib.graphics.chart.VectorGraphicsEncoder.VectorGraphicsFormat;
import org.xbib.graphics.chart.internal.Series;
import org.xbib.graphics.chart.internal.SeriesAxesChart;
import org.xbib.graphics.chart.internal.chartpart.Chart;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Swing JPanel that contains a Chart
 * Right-click + Save As... or ctrl+S pops up a Save As dialog box for saving the chart as a JPeg or PNG file.
 */
public class XChartPanel<T extends Chart> extends JPanel {

    private final T chart;
    private final Dimension preferredSize;
    private String saveAsString = "Save As...";

    /**
     * Constructor
     *
     * @param chart
     */
    public XChartPanel(final T chart) {

        this.chart = chart;
        preferredSize = new Dimension(chart.getWidth(), chart.getHeight());

        // Right-click listener for saving chart
        this.addMouseListener(new PopUpMenuClickListener());

        // Control+S key listener for saving chart
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ctrlS, "save");
        this.getActionMap().put("save", new SaveAction());
    }

    /**
     * Set the "Save As..." String if you want to localize it.
     *
     * @param saveAsString
     */
    public void setSaveAsString(String saveAsString) {

        this.saveAsString = saveAsString;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        chart.paint(g2d, getWidth(), getHeight());
        g2d.dispose();
    }

    public T getChart() {

        return this.chart;
    }

    @Override
    public Dimension getPreferredSize() {

        return this.preferredSize;
    }

    private void showSaveAsDialog() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new SuffixSaveFilter("jpg"));
        FileFilter pngFileFilter = new SuffixSaveFilter("png");
        fileChooser.addChoosableFileFilter(pngFileFilter);
        fileChooser.addChoosableFileFilter(new SuffixSaveFilter("bmp"));
        fileChooser.addChoosableFileFilter(new SuffixSaveFilter("gif"));

        // VectorGraphics2D is optional, so if it's on the classpath, allow saving charts as vector graphic
        try {
            Class.forName("de.erichseifert.vectorgraphics2d.VectorGraphics2D");
            // it exists on the classpath
            fileChooser.addChoosableFileFilter(new SuffixSaveFilter("svg"));
            fileChooser.addChoosableFileFilter(new SuffixSaveFilter("eps"));
            fileChooser.addChoosableFileFilter(new SuffixSaveFilter("pdf"));
        } catch (ClassNotFoundException e) {
            // it does not exist on the classpath
        }

        fileChooser.setAcceptAllFileFilterUsed(false);

        fileChooser.setFileFilter(pngFileFilter);

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {

            if (fileChooser.getSelectedFile() != null) {
                File theFileToSave = fileChooser.getSelectedFile();
                try {
                    if (fileChooser.getFileFilter() == null) {
                        BitmapEncoder.saveBitmap(chart, theFileToSave.getCanonicalPath().toString(), BitmapFormat.PNG);
                    } else if (fileChooser.getFileFilter().getDescription().equals("*.jpg,*.JPG")) {
                        BitmapEncoder.saveJPGWithQuality(chart, BitmapEncoder.addFileExtension(theFileToSave.getCanonicalPath().toString(), BitmapFormat.JPG), 1.0f);
                    } else if (fileChooser.getFileFilter().getDescription().equals("*.png,*.PNG")) {
                        BitmapEncoder.saveBitmap(chart, theFileToSave.getCanonicalPath().toString(), BitmapFormat.PNG);
                    } else if (fileChooser.getFileFilter().getDescription().equals("*.bmp,*.BMP")) {
                        BitmapEncoder.saveBitmap(chart, theFileToSave.getCanonicalPath().toString(), BitmapFormat.BMP);
                    } else if (fileChooser.getFileFilter().getDescription().equals("*.gif,*.GIF")) {
                        BitmapEncoder.saveBitmap(chart, theFileToSave.getCanonicalPath().toString(), BitmapFormat.GIF);
                    } else if (fileChooser.getFileFilter().getDescription().equals("*.svg,*.SVG")) {
                        VectorGraphicsEncoder.saveVectorGraphic(chart, theFileToSave.getCanonicalPath().toString(), VectorGraphicsFormat.SVG);
                    } else if (fileChooser.getFileFilter().getDescription().equals("*.eps,*.EPS")) {
                        VectorGraphicsEncoder.saveVectorGraphic(chart, theFileToSave.getCanonicalPath().toString(), VectorGraphicsFormat.EPS);
                    } else if (fileChooser.getFileFilter().getDescription().equals("*.pdf,*.PDF")) {
                        VectorGraphicsEncoder.saveVectorGraphic(chart, theFileToSave.getCanonicalPath().toString(), VectorGraphicsFormat.PDF);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * Update a series by updating the X-Axis, Y-Axis and error bar data
     *
     * @param seriesName
     * @param newXData        - set null to be automatically generated as a list of increasing Integers starting from
     *                        1 and ending at the size of the new Y-Axis data list.
     * @param newYData
     * @param newErrorBarData - set null if there are no error bars
     * @return
     */
    public Series updateSeries(String seriesName, List<?> newXData, List<? extends Number> newYData, List<? extends Number> newErrorBarData) {

        Map<String, SeriesAxesChart> seriesMap = chart.getSeriesMap();
        SeriesAxesChart series = seriesMap.get(seriesName);
        if (series == null) {
            throw new IllegalArgumentException("Series name >" + seriesName + "< not found!!!");
        }
        if (newXData == null) {
            // generate X-Data
            List<Integer> generatedXData = new ArrayList<Integer>();
            for (int i = 1; i <= newYData.size(); i++) {
                generatedXData.add(i);
            }
            series.replaceData(generatedXData, newYData, newErrorBarData);
        } else {
            series.replaceData(newXData, newYData, newErrorBarData);
        }

        // Re-display the chart
        revalidate();
        repaint();

        return series;
    }

    private class SaveAction extends AbstractAction {

        public SaveAction() {

            super("save");
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            showSaveAsDialog();
        }
    }

    /**
     * File filter based on the suffix of a file. This file filter accepts all files that end with .suffix or the
     * capitalized suffix.
     */
    private class SuffixSaveFilter extends FileFilter {

        private final String suffix;

        /**
         * @param suffix This file filter accepts all files that end with .suffix or the capitalized suffix.
         */
        public SuffixSaveFilter(String suffix) {

            this.suffix = suffix;
        }

        @Override
        public boolean accept(File f) {

            if (f.isDirectory()) {
                return true;
            }

            String s = f.getName();

            return s.endsWith("." + suffix) || s.endsWith("." + suffix.toUpperCase());
        }

        @Override
        public String getDescription() {

            return "*." + suffix + ",*." + suffix.toUpperCase();
        }
    }

    private class PopUpMenuClickListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {

            if (e.isPopupTrigger()) {
                doPop(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {

            if (e.isPopupTrigger()) {
                doPop(e);
            }
        }

        private void doPop(MouseEvent e) {

            XChartPanelPopupMenu menu = new XChartPanelPopupMenu();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private class XChartPanelPopupMenu extends JPopupMenu {

        JMenuItem saveAsMenuItem;

        public XChartPanelPopupMenu() {

            saveAsMenuItem = new JMenuItem(saveAsString);
            saveAsMenuItem.addMouseListener(new MouseListener() {

                @Override
                public void mouseReleased(MouseEvent e) {

                    showSaveAsDialog();
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseClicked(MouseEvent e) {

                }
            });
            add(saveAsMenuItem);
        }
    }
}
