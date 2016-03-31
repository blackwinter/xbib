package org.xbib.io.field;

/**
 * The MarcXchange listener is an interface for catching events while
 * reading from ISO 2709 / MARC format family streams.
 *
 * Each record is framed by a leader and a trailer event. The leader
 * event fires directly after the begin of a record when a leader element
 * is found, the trailer (which is not defined in ISO 2709/MarcXchange) is fired
 * just before the record ends. The trailer event is useful for post-processing
 * fields before the record end event is fired.
 *
 * Data field events are fired in the sequence they are found in a record.
 * Sub fields can be nested in data fields, but at most for one nesting level.
 *
 * Control fields are defined as data fields in the tag range from 000 to 009.
 * They do not have any indicators or sub fields.
 *
 * Field data is carried only in the end events, where begin events carry
 * information about field indicators and subfield identifiers.
 */
public interface MarcListener {

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
