package org.xbib.cluster;

public interface MigrationListener {
    void migrationStart(Member sourceMember);

    void migrationEnd(Member sourceMember);
}
