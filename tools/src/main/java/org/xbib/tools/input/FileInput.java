package org.xbib.tools.input;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.util.Finder;
import org.xbib.util.concurrent.URIWorkerRequest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class FileInput {

    private final static Logger logger = LogManager.getLogger(FileInput.class);

    private final Map<String,List<String>> fileMap = new HashMap<>();

    public Map<String,List<String>> getFileMap() {
        return fileMap;
    }

    public void createQueue(Settings settings, BlockingQueue<URIWorkerRequest> queue) throws IOException, InterruptedException {
        if (settings.get("runhost") != null) {
            // check if we only allowed to run on a certain host
            logger.info("preparing input queue only allowed on host={}", settings.get("runhost"));
            boolean found = false;
            // not very smart...
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress addr : Collections.list(inetAddresses)) {
                    if (addr.getHostName().equals(settings.get("runhost"))) {
                        found = true;
                    }
                }
            }
            if (!found) {
                logger.error("host {} not present, stopping", settings.get("runhost"));
                return;
            }
        }
        Map<String,Settings> inputMap = settings.getGroups("input");
        for (Map.Entry<String,Settings> entry : inputMap.entrySet()) {
            String inputKey = entry.getKey();
            Settings inputSettings = entry.getValue();
            if (inputSettings.getAsArray("uri").length > 0) {
                String[] inputs = inputSettings.getAsArray("uri");
                logger.info("{}: preparing {} inputs from {}", inputKey, inputs.length, inputs);
                for (String input : inputs) {
                    if (inputKey.startsWith("queue")) {
                        URIWorkerRequest request = new URIWorkerRequest();
                        request.set(URI.create(input));
                        queue.put(request);
                    } else {
                        fileMap.put(inputKey, Arrays.asList(inputs));
                    }
                }
            } else if (inputSettings.get("uri") != null) {
                String input = settings.get("uri");
                boolean parallel = inputSettings.getAsBoolean("parallel", false);
                int concurrency = inputSettings.getAsInt("concurrency", 1);
                logger.info("{}: preparing one input from {} (parallel={},concurrency={})", inputKey, input, parallel, concurrency);
                if (inputKey.startsWith("queue")) {
                    URIWorkerRequest element = new URIWorkerRequest();
                    element.set(URI.create(input));
                    queue.put(element);
                    if (parallel) {
                        for (int i = 1; i < concurrency; i++) {
                            element = new URIWorkerRequest();
                            element.set(URI.create(input));
                            queue.put(element);
                        }
                    }
                } else {
                    fileMap.put(inputKey, Collections.singletonList(input));
                }
            } else if (inputSettings.get("pattern") != null) {
                String pattern = inputSettings.get("pattern");
                logger.info("{}: preparing inputs from pattern={}", inputKey, pattern);
                Queue<URI> uris = new Finder()
                        .find(inputSettings.get("base"),
                                inputSettings.get("basepattern"),
                                inputSettings.get("path"),
                                pattern)
                        .sortBy(inputSettings.get("sort_by"))
                        .order(inputSettings.get("order"))
                        .getURIs();
                int max = inputSettings.getAsInt("max", -1);
                logger.info("{} URIs = {}, max = {}", uris.size(), uris, max);
                if (inputKey.startsWith("queue")) {
                    for (URI uri : uris) {
                        URIWorkerRequest element = new URIWorkerRequest();
                        element.set(uri);
                        if (max < 0 || (max > 0 && queue.size() < max)) {
                            queue.put(element);
                        }
                    }
                } else {
                    fileMap.put(inputKey, uris.stream().map(URI::toString).collect(Collectors.toList()));
                }
            } else if (inputSettings.get("archive") != null) {
                String archive = inputSettings.get("archive");
                logger.info("{}: preparing inputs from archive={}", inputKey, archive);
                if (inputKey.startsWith("queue")) {
                    URI uri = URI.create(archive);
                    URIWorkerRequest element = new URIWorkerRequest();
                    element.set(uri);
                    queue.put(element);
                } else {
                    fileMap.put(inputKey, Collections.singletonList(archive));
                }
            }
        }
    }
}
