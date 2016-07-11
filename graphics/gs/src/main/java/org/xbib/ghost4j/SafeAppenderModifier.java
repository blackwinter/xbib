package org.ghost4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SafeAppenderModifier {

    protected Path modify(Path sourcePath, Path appendPath) throws IOException, GhostscriptException {
        Ghostscript gs = Ghostscript.getInstance();
        Path outputPath = Files.createTempFile("pdf", "pdf");
        String[] gsArgs = {
                "-psconv",
                "-dNOPAUSE", "-dSAFER", "-dBATCH",
                "-sDEVICE=pdfwrite",
                "-sOutputFile=" + outputPath.toAbsolutePath().toString(),
                "-q", "-f",
                sourcePath.toAbsolutePath().toString(),
                appendPath.toAbsolutePath().toString()};
        try {
            gs.initialize(gsArgs);
            gs.exit();
            return outputPath;
        } finally {
            Ghostscript.deleteInstance();
            Files.delete(sourcePath);
            Files.delete(appendPath);
        }
    }
}
