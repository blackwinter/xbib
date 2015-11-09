
package org.xbib.common.xcontent.smile;

import com.fasterxml.jackson.core.JsonParser;
import org.xbib.common.xcontent.XContent;
import org.xbib.common.xcontent.json.JsonXContentParser;

public class SmileXContentParser extends JsonXContentParser {

    public SmileXContentParser(JsonParser parser) {
        super(parser);
    }

    @Override
    public XContent content() {
        return SmileXContent.smileXContent;
    }

}
