package org.ghost4j;

public class FontAnalysisItem {

    private String name;
    private boolean embedded;
    private boolean subSet;

    @Override
    public String toString() {
        String embeddedString = "NOT_EMBEDDED";
        if (embedded) {
            embeddedString = "EMBEDDED";
        }
        String setString = "FULL_SET";
        if (subSet) {
            setString = "SUB_SET";
        }
        return name + ": " + embeddedString + " " + setString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public boolean isSubSet() {
        return subSet;
    }

    public void setSubSet(boolean subSet) {
        this.subSet = subSet;
    }
}
