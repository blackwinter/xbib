package org.xbib.tools.output;

public class IndexDefinition {

    private String index;
    private String concreteIndexName;
    private String type;
    private String settingDef;
    private String mappingDef;
    private String timeWindow;
    private boolean mock;
    private boolean ignoreErrors;
    private boolean switchAliases;
    private boolean hasRetention;
    private int timestampDiff = 0;
    private int minToKeep = 0;
    private int replicaLevel = 0;

    public IndexDefinition(String index,
                           String concreteIndexName,
                           String type,
                           String settingDef,
                           String mappingDef,
                           String timeWindow,
                           boolean mock,
                           boolean ignoreErrors,
                           boolean switchAliases,
                           boolean hasRetention,
                           int timestampDiff,
                           int minToKeep,
                           int replicaLevel
    ) {
        this.index = index;
        this.concreteIndexName = concreteIndexName;
        this.type = type;
        this.settingDef = settingDef;
        this.mappingDef = mappingDef;
        this.timeWindow = timeWindow;
        this.mock = mock;
        this.ignoreErrors = ignoreErrors;
        this.switchAliases = switchAliases;
        this.hasRetention = hasRetention;
        this.timestampDiff = timestampDiff;
        this.minToKeep = minToKeep;
        this.replicaLevel = replicaLevel;
    }

    public String getIndex() {
        return index;
    }

    public String getConcreteIndex() {
        return concreteIndexName;
    }

    public String getType() {
        return type;
    }

    public String getSettingDef() {
        return settingDef;
    }

    public String getMappingDef() {
        return mappingDef;
    }

    public String getTimeWindow() {
        return timeWindow;
    }

    public boolean isMock() {
        return mock;
    }

    public boolean ignoreErrors() {
        return ignoreErrors;
    }

    public boolean isSwitchAliases() {
        return switchAliases;
    }

    public boolean hasRetention() {
        return hasRetention;
    }

    public int getTimestampDiff() {
        return timestampDiff;
    }

    public int getMinToKeep() {
        return minToKeep;
    }

    public int getReplicaLevel() {
        return replicaLevel;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IndexDefinition[name=").append(getIndex())
                .append(",type=").append(getType())
                .append(",timewindow=").append(getTimeWindow())
                .append(",concrete=").append(getConcreteIndex())
                .append(",settings=").append(getSettingDef())
                .append(",mapping=").append(getMappingDef())
                .append(",mock=").append(isMock())
                .append(",ignoreErrors=").append(ignoreErrors())
                .append(",switch=").append(isSwitchAliases())
                .append(",retention=").append(hasRetention())
                .append(",timestampDiff=").append(getTimestampDiff())
                .append(",minToKeep=").append(getMinToKeep())
                .append(",replicaLevel=").append(getReplicaLevel())
                .append("]");
        return sb.toString();
    }
}