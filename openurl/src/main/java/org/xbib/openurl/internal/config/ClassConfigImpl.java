package org.xbib.openurl.internal.config;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xbib.openurl.OpenURLException;
import org.xbib.openurl.config.ClassConfig;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassConfigImpl implements ClassConfig {

    private final static XPathFactory xpathFactory = XPathFactory.newInstance();

    private Node classNode;

    ClassConfigImpl(Node classNode) {
        this.classNode = classNode;
    }

    public String getClassName() throws OpenURLException {
        try {
            return ((Node) xpathFactory.newXPath().compile("className").evaluate(classNode, XPathConstants.NODE)).getTextContent();
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
    }

    public String getArg(String key) throws OpenURLException {
        try {
            return ((Node) xpathFactory.newXPath().compile("args/" + key).evaluate(classNode, XPathConstants.NODE)).getTextContent();
        } catch (Exception e) {
            throw new OpenURLException(e);
        }
    }

    public List<String> getArgs(String key) throws OpenURLException {
        List<String> args = new ArrayList<>();
        try {
            XPath xpath = xpathFactory.newXPath();
            XPathExpression expr = xpath.compile("args/" + key);
            NodeList list = (NodeList) expr.evaluate(classNode, XPathConstants.NODESET);
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
        Map<String, String> map = new HashMap<>();
        try {
            XPath xpath = xpathFactory.newXPath();
            XPathExpression expr = xpath.compile("args/*");
            NodeList list = (NodeList) expr.evaluate(classNode, XPathConstants.NODESET);
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
