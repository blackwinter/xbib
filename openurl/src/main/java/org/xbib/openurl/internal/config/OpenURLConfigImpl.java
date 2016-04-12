package org.xbib.openurl.internal.config;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.Serializer;
import org.xbib.openurl.Service;
import org.xbib.openurl.Transport;
import org.xbib.openurl.config.OpenURLConfig;
import org.xbib.openurl.descriptors.Identifier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenURLConfigImpl implements OpenURLConfig {

    private final static XPathFactory xpathFactory = XPathFactory.newInstance();

    private final static DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

    private Document openurlXMLConfig;

    /**
     * Construct default OpenURL configuration.
     *
     * @throws ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws IOException
     */
    public OpenURLConfigImpl() throws OpenURLException {
        this("/org/xbib/openurl/openurl.xml");
    }

    /**
     * Construct OpenURL configuration with custom resource name.
     *
     * @param resourceName
     * @throws ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws IOException
     */
    public OpenURLConfigImpl(String resourceName) throws OpenURLException {
        try {
            DocumentBuilder builder = documentFactory.newDocumentBuilder();
            this.openurlXMLConfig = builder.parse(getClass().getResourceAsStream(resourceName));
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
    }

    /**
     * Get transports
     *
     * @return
     * @throws OpenURLException
     */
    public List<Transport> getTransports() throws OpenURLException {
        List<Transport> transports = new ArrayList<>();
        try {
            XPathExpression expr = xpathFactory.newXPath().compile("/config/transportMap/transport");
            NodeList list = (NodeList) expr.evaluate(openurlXMLConfig, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                ClassConfigImpl classConfig = new ClassConfigImpl(node);
                String transportClassName = classConfig.getClassName();
                Class transportClass = Class.forName(transportClassName);
                Constructor transportConstructor = null;
                try {
                    transportConstructor = transportClass.getConstructor(OpenURLConfig.class);
                } catch (NoSuchMethodException e) {
                    transportConstructor = transportClass.getConstructor(OpenURLConfigImpl.class);
                }
                Transport transport =
                        (Transport) transportConstructor.newInstance(this);
                transports.add(transport);
            }
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
        return transports;
    }

    /**
     * Get serializers
     *
     * @return
     * @throws OpenURLException
     */
    public List<Serializer> getSerializers() throws OpenURLException {
        List<Serializer> serializers = new ArrayList<>();
        try {
            XPathExpression expr = xpathFactory.newXPath().compile("/config/serializerMap/serializers");
            NodeList list = (NodeList) expr.evaluate(openurlXMLConfig, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                ClassConfigImpl classConfig = new ClassConfigImpl(node);
                String serializerClassName = classConfig.getClassName();
                Class serializerClass = Class.forName(serializerClassName);
                Constructor serializerConstructor = null;
                try {
                    serializerConstructor = serializerClass.getConstructor(OpenURLConfig.class);
                } catch (NoSuchMethodException e) {
                    serializerConstructor = serializerClass.getConstructor(OpenURLConfigImpl.class);
                }
                Serializer serializer = (Serializer) serializerConstructor.newInstance(this);
                serializers.add(serializer);
            }
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
        return serializers;
    }

    public Service getService(Identifier id) throws OpenURLException {
        try {
            XPathExpression expr = xpathFactory.newXPath().compile("/config/serviceMap/service[@id='" +
                    id.getURI().toString() + "']");
            Node node = (Node) expr.evaluate(openurlXMLConfig, XPathConstants.NODE);
            if (node != null) {
                ClassConfigImpl classConfig = new ClassConfigImpl(node);
                //new DOMXPath("className").stringValueOf(node);
                String className = ((Node) xpathFactory.newXPath().compile("className").evaluate(node, XPathConstants.NODE)).getTextContent();
                Class serviceClass = Class.forName(className);
                Constructor serviceConstructor = null;
                try {
                    serviceConstructor = serviceClass.getConstructor(new Class[]{org.xbib.openurl.config.OpenURLConfig.class,
                            org.xbib.openurl.config.ClassConfig.class});
                } catch (NoSuchMethodException e) {
                    serviceConstructor = serviceClass.getConstructor(new Class[]{OpenURLConfigImpl.class,
                            ClassConfigImpl.class});
                }
                return (Service) serviceConstructor.newInstance(new Object[]{this, classConfig});
            }
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
        return null;
    }

    public Service getService(String className) throws OpenURLException {
        try {
            XPathExpression expr = xpathFactory.newXPath().compile("/config/serviceMap/service[className='" + className + "']");
            Node node = (Node) expr.evaluate(openurlXMLConfig, XPathConstants.NODE);
            ClassConfigImpl classConfig = new ClassConfigImpl(node);
            Class serviceClass = Class.forName(className);
            Constructor serviceConstructor;
            try {
                serviceConstructor = serviceClass.getConstructor(OpenURLConfig.class,
                        org.xbib.openurl.config.ClassConfig.class);
            } catch (NoSuchMethodException e) {
                serviceConstructor = serviceClass.getConstructor(OpenURLConfigImpl.class, ClassConfigImpl.class);
            }
            return (Service) serviceConstructor.newInstance(this, classConfig);
        } catch (ClassNotFoundException e) {
            // do nothing
        } catch (Exception e) {
            throw new OpenURLException(e);
        }

        return null;
    }

    public OpenURLRequestProcessor getProcessor() throws OpenURLException {
        try {
            XPathExpression expr = xpathFactory.newXPath().compile("/config/processor");
            Node node = (Node) expr.evaluate(openurlXMLConfig, XPathConstants.NODE);
            ClassConfigImpl classConfig = new ClassConfigImpl(node);
            String className = classConfig.getClassName();
            Class c = Class.forName(className);
            return (OpenURLRequestProcessor) c.newInstance();
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
    }

    public String getArg(String key) throws OpenURLException {
        try {
            return ((Node) xpathFactory.newXPath().compile("config/args/" + key).evaluate(openurlXMLConfig, XPathConstants.NODE)).getTextContent();
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
    }

    public List<String> getArgs(String key) throws OpenURLException {
        List<String> args = new ArrayList<>();
        try {
            XPathExpression expr = xpathFactory.newXPath().compile("config/args");
            NodeList list = (NodeList) expr.evaluate(openurlXMLConfig, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                args.add(node.getTextContent());
            }
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
        return args;
    }

    public Map<String, String> getArgs() throws OpenURLException {
        Map<String, String> map = new HashMap();
        if (openurlXMLConfig == null) {
            return map;
        }
        try {
            XPathExpression expr = xpathFactory.newXPath().compile("/config/args/*");
            NodeList list = (NodeList) expr.evaluate(openurlXMLConfig, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                map.put(node.getNodeName(), node.getTextContent());
            }
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
        return map;
    }
}
