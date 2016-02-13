package org.xbib.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclFileAttributeView;
import java.util.List;

public class AclTester {

    private final static Logger logger = LogManager.getLogger(AclTester.class.getName());

    @Test
    public void testAcl() throws IOException {
        Path path = new File("/tmp").toPath();
        logger.info("path={}", path.toAbsolutePath());
        AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
        if (view != null) {
            List<AclEntry> acl = view.getAcl();
            for (AclEntry entry : acl) {
                logger.info(entry.principal().getName());
                for (AclEntryFlag flag : entry.flags()) {
                    logger.info(flag.name());
                }
                for (AclEntryPermission permission : entry.permissions()) {
                    logger.info(permission.name());
                }
            }
        }
    }
}
