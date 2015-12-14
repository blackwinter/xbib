package org.xbib.io.http.client.handler.resumable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.xbib.io.http.client.util.MiscUtils.closeSilently;

/**
 * A {@link ResumableAsyncHandler.ResumableProcessor} which use a properties file
 * to store the download index information.
 */
public class PropertiesBasedResumableProcessor implements ResumableAsyncHandler.ResumableProcessor {
    private final static File TMP = new File(System.getProperty("java.io.tmpdir"), "ahc");
    private final static String storeName = "ResumableAsyncHandler.properties";
    private final ConcurrentHashMap<String, Long> properties = new ConcurrentHashMap<>();

    private static String append(Map.Entry<String, Long> e) {
        return e.getKey() + '=' + e.getValue() + '\n';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String url, long transferredBytes) {
        properties.put(url, transferredBytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String uri) {
        if (uri != null) {
            properties.remove(uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Map<String, Long> map) {
        FileOutputStream os = null;
        try {

            if (!TMP.exists() && !TMP.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " + TMP.getAbsolutePath());
            }
            File f = new File(TMP, storeName);
            if (!f.exists() && !f.createNewFile()) {
                throw new IllegalStateException("Unable to create temp file: " + f.getAbsolutePath());
            }
            if (!f.canWrite()) {
                throw new IllegalStateException();
            }

            os = new FileOutputStream(f);

            for (Map.Entry<String, Long> e : properties.entrySet()) {
                os.write(append(e).getBytes(UTF_8));
            }
            os.flush();
        } catch (Throwable e) {
            //log.warn(e.getMessage(), e);
        } finally {
            if (os != null) {
                closeSilently(os);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> load() {
        Scanner scan = null;
        try {
            scan = new Scanner(new File(TMP, storeName), UTF_8.name());
            scan.useDelimiter("[=\n]");

            String key;
            String value;
            while (scan.hasNext()) {
                key = scan.next().trim();
                value = scan.next().trim();
                properties.put(key, Long.valueOf(value));
            }
        } catch (FileNotFoundException ex) {
            //log.debug("Missing {}", storeName);
        } finally {
            if (scan != null) {
                scan.close();
            }
        }
        return properties;
    }
}
