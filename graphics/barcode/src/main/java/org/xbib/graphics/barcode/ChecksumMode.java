package org.xbib.graphics.barcode;

/**
 * Enumeration type for checksum policy.
 */
public class ChecksumMode {

    /** "auto" chooses the barcode's default checksum behaviour */
    public static final ChecksumMode CP_AUTO   = new ChecksumMode("auto");
    /** "ignore" doesn't check nor add a checksum */
    public static final ChecksumMode CP_IGNORE = new ChecksumMode("ignore");
    /** "add" adds the necessary checksum to the message to be encoded */
    public static final ChecksumMode CP_ADD    = new ChecksumMode("add");
    /** "check" requires the check character to be present in the message. It 
     * will be checked.
     */
    public static final ChecksumMode CP_CHECK  = new ChecksumMode("check");

    private String name;
    
    /**
     * Creates a new ChecksumMode instance.
     * @param name the name of the ChecksumMode
     */
    protected ChecksumMode(String name) {
        this.name = name;
    }
    
    /**
     * @return the name of the instance.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns a ChecksumMode instance by name.
     * @param name the name of the ChecksumMode
     * @return the requested instance
     */
    public static ChecksumMode byName(String name) {
        if (name.equalsIgnoreCase(ChecksumMode.CP_AUTO.getName())) {
            return ChecksumMode.CP_AUTO;
        } else if (name.equalsIgnoreCase(ChecksumMode.CP_IGNORE.getName())) {
            return ChecksumMode.CP_IGNORE;
        } else if (name.equalsIgnoreCase(ChecksumMode.CP_ADD.getName())) {
            return ChecksumMode.CP_ADD;
        } else if (name.equalsIgnoreCase(ChecksumMode.CP_CHECK.getName())) {
            return ChecksumMode.CP_CHECK;
        } else {
            throw new IllegalArgumentException("Invalid ChecksumMode: " + name);
        }
    }
    
}
