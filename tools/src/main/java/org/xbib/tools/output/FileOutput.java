package org.xbib.tools.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.io.StreamCodecService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class FileOutput {

    private final static Logger logger = LogManager.getLogger(FileOutput.class);

    private final Map<String,BufferedOutputStream> fileMap = new HashMap<>();

    public Map<String,BufferedOutputStream> getFileMap() {
        return fileMap;
    }

    public void createFileMap(Settings settings) throws IOException {
        Map<String,Settings> output = settings.getGroups("output");
        for (Map.Entry<String,Settings> entry : output.entrySet()) {
            String fileName = settings.get("name", entry.getKey());
            Path path = Paths.get(fileName);
            Settings outputSettings = entry.getValue();
            boolean overwrite = outputSettings.getAsBoolean("overwrite", true);
            StandardOpenOption option = overwrite ?
                    StandardOpenOption.CREATE : StandardOpenOption.CREATE_NEW;
            OutputStream outputStream = Files.newOutputStream(path, option);
            for (String codec : StreamCodecService.getCodecs()) {
                if (fileName.endsWith("." + codec)) {
                    outputStream = StreamCodecService.getInstance().getCodec(codec).encode(outputStream);
                    break;
                }
            }
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            logger.info("opening {} for write", entry.getKey());
            fileMap.put(entry.getKey(), bufferedOutputStream);
        }
    }

    public void closeFileMap() throws IOException {
        for (Map.Entry<String,BufferedOutputStream> entry : fileMap.entrySet()) {
            entry.getValue().close();
            logger.info("closing writes to {}", entry.getKey());
        }
    }

}
