package org.xbib.analyzer.output
import org.xbib.analyzer.dublincore.DublinCoreContext
import org.xbib.elements.CountableElementOutput
import org.xbib.rdf.Resource
import org.xbib.rdf.xcontent.ContentBuilder

public class DublinCoreOutput extends CountableElementOutput<DublinCoreContext, Resource> {
    public void output(DublinCoreContext context, ContentBuilder builder) {
         println 'scripted output, got resource ' + context.resource()
         return
    }
    public long getCounter() {
        return 0;
    }
}