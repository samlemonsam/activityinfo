package org.activityinfo.test.pageobject.odk;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;

/**
 * Wraps the XML page source provided by the WebDriver API for
 * faster client-side parsing.
 */
public class PageSource {

    private final XPath xpath;
    private final Document doc;

    public PageSource(String pageSource) {
        doc = parseDocument(pageSource);
        xpath = XPathFactory.newInstance().newXPath();

    }

    private Document parseDocument(String pageSource)  {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            return builder.parse(ByteSource.wrap(pageSource.getBytes(Charsets.UTF_8)).openStream());
        } catch(Exception e) {
            throw new RuntimeException("Error parsing xml: " + e.getMessage() + "\n" + pageSource, e);
        }
    }

    public NodeList query(String xpathQuery) {

        // XPath Query for showing all nodes value
        XPathExpression expr;
        try {
            expr = xpath.compile(xpathQuery);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Invalid XPath: " + e.getMessage(), e);
        }

        try {
            return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("XPath Query failed: " + e.getMessage());
        }
    }

    public Element findElement(String query) {
        NodeList nodes = query(query);
        if(nodes.getLength() != 1) {
            throw new AssertionError("Expected one matching element for query '" + query + ", found " + nodes.getLength());
        }
        return (Element) nodes.item(0);
    }
}
