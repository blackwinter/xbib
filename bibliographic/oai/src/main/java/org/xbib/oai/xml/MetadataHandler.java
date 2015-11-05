package org.xbib.oai.xml;

import org.xbib.oai.util.RecordHeader;
import org.xml.sax.ContentHandler;

public interface MetadataHandler extends ContentHandler {

    MetadataHandler setHeader(RecordHeader header);

    RecordHeader getHeader();
}
