package org.xbib.io;

import org.xbib.io.archive.ArchiveSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

public abstract class AbstractConnectionFactory<S extends Session, C extends Connection<S>> implements ConnectionFactory<S> {

    @Override
    public abstract String getName();

    @Override
    public abstract C getConnection(URI uri) throws IOException;

    @Override
    public boolean canOpen(URI uri) {
        return ArchiveSession.canOpen(uri, getName(), true);
    }

    @Override
    public InputStream open(URI uri) throws IOException {
        if (!canOpen(uri)) {
            return null;
        }
        final String part = uri.getSchemeSpecificPart();
        InputStream in = new FileInputStream(part);
        Set<String> codecs = StreamCodecService.getCodecs();
        for (String codec : codecs) {
            String s = "." + codec;
            if (part.endsWith(s.toLowerCase()) || part.endsWith(s.toUpperCase())) {
                return StreamCodecService.getInstance().getCodec(codec).decode(in);
            }
        }
        return in;
    }
}
