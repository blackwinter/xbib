
package org.xbib.io.stream;

import java.io.IOException;

public interface Streamable {

    void readFrom(StreamInput in) throws IOException;

    void writeTo(StreamOutput out) throws IOException;
}
