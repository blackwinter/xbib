package org.xbib.io.ftp.agent;

import java.nio.file.AccessMode;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Collection;

/**
 * A convenience interface over {@link BasicFileAttributeView}
 *
 * <p>This interface extends {@link BasicFileAttributeView} to provide an
 * easier access to basic {@link AccessMode} privileges.</p>
 */
public interface FtpFileView extends BasicFileAttributeView {
    Collection<AccessMode> getAccess();
}
