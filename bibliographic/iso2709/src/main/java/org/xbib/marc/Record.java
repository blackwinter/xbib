package org.xbib.marc;

import org.xbib.io.FastByteArrayOutputStream;
import org.xbib.marc.label.RecordLabel;

import java.util.ArrayList;
import java.util.List;

public class Record {

    private String leader;

    private RecordLabel label;

    private List<Field> controlfields;

    private List<FieldList> datafields;

    private FieldList current;

    private FastByteArrayOutputStream directoryStream;

    private FastByteArrayOutputStream fieldStream;

    public Record() {
        controlfields = new ArrayList<Field>();
        datafields = new ArrayList<FieldList>();
        directoryStream = new FastByteArrayOutputStream(2048);
        fieldStream = new FastByteArrayOutputStream(8192);
    }

    public Record setLeader(String leader) {
        this.leader = leader;
        return this;
    }

    public String getLeader() {
        return leader;
    }

    public Record setLabel(RecordLabel label) {
        this.label = label;
        return this;
    }

    public RecordLabel getLabel() {
        return label;
    }

    public List<Field> getControlfields() {
        return controlfields;
    }

    public List<FieldList> getDatafields() {
        return datafields;
    }

    public FastByteArrayOutputStream getDirectoryStream() {
        return directoryStream;
    }

    public FastByteArrayOutputStream getFieldStream() {
        return fieldStream;
    }

    public void newFieldCollection() {
        this.current = new FieldList();
    }

    public FieldList getCurrent() {
        return current;
    }

    public void cleanup() {
        label = null;
        controlfields = null;
        datafields = null;
        current = null;
    }
}
