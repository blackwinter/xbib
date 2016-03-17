package org.xbib.tools.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.io.StreamCodecService;
import org.xbib.util.Finder;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class FileOutput {

    private final static Logger logger = LogManager.getLogger(FileOutput.class);

    private final Map<String,Entry> fileMap = new HashMap<>();

    public Map<String,Entry> getMap() {
        return fileMap;
    }

    public void createFileMap(Settings outSettings) throws IOException {
        Map<String,Settings> output = outSettings.getGroups("output");
        for (Map.Entry<String,Settings> entry : output.entrySet()) {
            Settings settings = entry.getValue();
            String fileName = settings.get("name", entry.getKey());
            // skip reserved outputs here
            if ("elasticsearch".equals(fileName)) {
                continue;
            }
            Path path = Paths.get(fileName);
            boolean overwrite = settings.getAsBoolean("overwrite", true);
            boolean append = settings.getAsBoolean("append", true);
            StandardOpenOption option1 = overwrite ?
                    StandardOpenOption.CREATE : StandardOpenOption.CREATE_NEW;
            StandardOpenOption option2 = append ?
                    StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;
            OutputStream outputStream = Files.newOutputStream(path, option1, option2);
            for (String codec : StreamCodecService.getCodecs()) {
                if (fileName.endsWith("." + codec)) {
                    outputStream = StreamCodecService.getInstance().getCodec(codec).encode(outputStream);
                    break;
                }
            }
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            logger.info("opening {} for write", entry.getKey());
            fileMap.put(entry.getKey(), new Entry(entry.getKey(), settings, path, bufferedOutputStream));
        }
    }

    public void closeFileMap(int returncode) throws IOException {
        for (Map.Entry<String,Entry> entry : fileMap.entrySet()) {
            if (returncode == 0 && entry.getValue().getSettings().getAsBoolean("retention", false)) {
                performRetention(entry.getValue());
            }
            logger.info("closing writes to {}", entry.getKey());
            entry.getValue().getOut().close();
        }
    }

    private void performRetention(Entry entry) throws IOException {
        Settings settings = entry.getSettings();
        Path parent = entry.getPath().getParent();
        String basepattern = settings.get("basepattern", ".*");
        String pattern = settings.get("pattern", ".*");
        long keep = settings.getAsLong("keep", 1L);
        logger.info("performing retention: base={} basepattern={} path={}, pattern={}", parent, entry.getPath(), pattern);
        Queue<Finder.PathFile> paths = new Finder()
                .find(parent, basepattern, entry.getPath(), pattern)
                .sortBy(settings.get("sort_by", "lastmodified"))
                .order(settings.get("order"))
                .skipPathFiles(keep);
        logger.info("deleting files: {}", paths);
        paths.stream().forEach(pf -> {
                    try {
                        Files.deleteIfExists(pf.getPath());
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                });
    }

    public class Entry {
        private final String name;
        private final Settings settings;
        private final Path path;
        private final BufferedOutputStream out;

        Entry(String name, Settings settings, Path path, BufferedOutputStream out) {
            this.name = name;
            this.settings = settings;
            this.path = path;
            this.out = out;
        }

        public String getName() {
            return name;
        }

        public Settings getSettings() {
            return settings;
        }

        public Path getPath() {
            return path;
        }

        public BufferedOutputStream getOut() {
            return out;
        }
    }
}
