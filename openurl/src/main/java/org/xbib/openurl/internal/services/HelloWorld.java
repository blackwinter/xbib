package org.xbib.openurl.internal.services;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequest;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.OpenURLResponse;
import org.xbib.openurl.Service;
import org.xbib.openurl.config.ClassConfig;
import org.xbib.openurl.config.OpenURLConfig;
import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.descriptors.Identifier;
import org.xbib.openurl.entities.ServiceType;
import org.xbib.openurl.internal.descriptors.IdentifierImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;

/**
 * A web service to say "Hello World".
 */
public class HelloWorld implements Service {

    private static final Identifier SERVICE_ID = new IdentifierImpl(URI.create("info:localhost/svc_id/HelloWorld"));

    private String something;

    private String name;

    /**
     * Construct a Hello World web service class.
     *
     * @param openURLConfig
     * @param classConfig
     */
    public HelloWorld(OpenURLConfig openURLConfig, ClassConfig classConfig) throws OpenURLException {
        this.something = classConfig.getArg("something");
    }

    public Identifier getServiceID() {
        return SERVICE_ID;
    }

    /**
     * Say "Hello" to someone.
     *
     * @param name the name of the person you want to greet.
     * @return a personal greeting
     */
    public String sayHello(String name) {
        return "Hello " + name + " (" + something + ")";
    }

    public OpenURLResponse resolve(
            ServiceType serviceType,
            ContextObject contextObject,
            OpenURLRequest openURLRequest,
            OpenURLRequestProcessor processor)
            throws OpenURLException {
        Collection<Descriptor> data =
                contextObject.getReferent().getDescriptors();
        this.name = !data.isEmpty() ? data.iterator().next().toString() : "<unknown referent>";
        return new OpenURLResponse(200, "text/plain; charset=UTF-8");
    }

    public boolean write(OutputStream out) throws IOException {
        out.write(sayHello(name).getBytes("UTF-8"));
        return true;
    }

}
