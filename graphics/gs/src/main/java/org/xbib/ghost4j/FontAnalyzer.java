package org.ghost4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Font analyzer: analyze fonts used in a document.
 */
public class FontAnalyzer {

    public List<FontAnalysisItem> analyze(Path path) throws IOException, GhostscriptException {
        Ghostscript gs = Ghostscript.getInstance();
        String[] gsArgs = {
                "-fonta",
                "-dQUIET", "-dNOPAUSE", "-dBATCH", "-dNODISPLAY",
                "-sFile=" + path.toAbsolutePath().toString(),
                "-sOutputFile=%stdout",
                "-f", "-"};
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("script/AnalyzePDFFonts.ps")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            gs.setStdIn(is);
            gs.setStdOut(baos);
            gs.initialize(gsArgs);
            List<FontAnalysisItem> result = new ArrayList<>();
            String s = baos.toString();
            String[] lines = s.split("\n");
            boolean inResults = false;
            for (String line : lines) {
                if (line.equals("---")) {
                    inResults = true;
                } else if (inResults) {
                    String[] columns = line.split(" ");
                    if (columns.length == 2) {
                        FontAnalysisItem font = new FontAnalysisItem();
                        String name = columns[0];
                        String[] nameParts = name.split("\\+");
                        if (nameParts.length > 1) {
                            name = nameParts[1];
                            font.setSubSet(true);
                        }
                        font.setName(name);
                        font.setEmbedded(false);
                        if (columns[1].equals("EM") || columns[1].equals("SU")) {
                            font.setEmbedded(true);
                        }
                        result.add(font);
                    }
                }
            }
            baos.close();
            return result;
        } finally {
            Ghostscript.deleteInstance();
        }
    }

}
