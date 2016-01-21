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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class FileInput {

    private final static Logger logger = LogManager.getLogger(FileInput.class);

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
                logger.info("{}: preparing {} requests from {}", inputKey, inputs.length, inputs);
                for (String input : inputs) {
                    URIWorkerRequest request = new URIWorkerRequest();
                    request.set(URI.create(input));
                    queue.put(request);
                }
            } else if (inputSettings.get("uri") != null) {
                String input = settings.get("uri");
                boolean parallel = inputSettings.getAsBoolean("parallel", false);
                int concurrency = inputSettings.getAsInt("concurrency", 1);
                logger.info("{}: preparing one request from {} (parallel={},concurrency={})", inputKey, input, parallel, concurrency);
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
            } else if (inputSettings.get("path") != null) {
                String path = inputSettings.get("path");
                logger.info("{}: preparing requests from path={}", inputKey, path);
                Queue<URI> uris = new Finder()
                        .find(inputSettings.get("base"),
                                inputSettings.get("basepattern"),
                                path, inputSettings.get("pattern"))
                        .sortByName(inputSettings.getAsBoolean("sort_by_name", false))
                        .sortByLastModified(inputSettings.getAsBoolean("sort_by_lastmodified", false))
                        .getURIs();
                logger.info("{} URIs = {}", uris.size(), uris);
                for (URI uri : uris) {
                    URIWorkerRequest element = new URIWorkerRequest();
                    element.set(uri);
                    queue.put(element);
                }
            } else if (inputSettings.get("archive") != null) {
                String archive = inputSettings.get("archive");
                logger.info("{}: preparing requests from archive={}", inputKey, archive);
                URI uri = URI.create(archive);
                URIWorkerRequest element = new URIWorkerRequest();
                element.set(uri);
                queue.put(element);
            }
        }
    }
}
