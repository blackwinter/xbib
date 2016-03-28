package org.xbib.io.field;

public interface MarcFieldListener {

    /**
     * Begin of a record collection
     */
    void beginCollection();

    /**
     * Begin of a record
     *
     * @param format the record format
     * @param type   the record type
     */
    void beginRecord(String format, String type);

    /**
     * The leader (or label) of a record
     *
     * @param label the label
     */
    void leader(String label);

    /**
     * A field
     * @param field the field
     */
    void field(MarcField field);

    /**
     * End of a record
     */
    void endRecord();

    /**
     * End of a collection
     */
    void endCollection();


}
