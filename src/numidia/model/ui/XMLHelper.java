package numidia.model.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import numidia.model.CCError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * This class aids on the XML activities so everything related to XML parsing, conversion, encoding, etc, should be done here
 *
 * @author Ricardo Espírito Santo - Linkare TI
 *
 */
public class XMLHelper {

    /**
     * Converts a XML Document to a byte[] with no identation and on the given charset
     *
     * @param doc
     * @param targetCharset
     * @return
     * @throws CCError
     */
    public static byte[] documentToByteArray(final Document doc, final Charset targetCharset) throws CCError {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, targetCharset.name());
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            transformer.transform(new DOMSource(doc), new StreamResult(baos));

            return baos.toByteArray();
        } catch (TransformerException ex) {
            throw new CCError(ex);
        }
    }

    /**
     * Attempts to convert a <code>PTEID_CCXML_Doc</code> in a String format to a <code>Document</code> This given string is also stripped for line separator
     * characters since this breaks some browsers
     *
     * @param xml
     *            the XML String to convert
     * @return the converted Document
     * @throws CCError
     * @throws CCXmlParsingException
     */
    public static Document convertXMLStringToDocument(final String xml) throws CCError, CCXmlParsingException {
        javax.xml.parsers.DocumentBuilder builder = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            final String noLineFeedXML = xml.replace(System.getProperty("line.separator"), "");
            return builder.parse(new InputSource(new StringReader(noLineFeedXML)));
        } catch (FactoryConfigurationError e) {
            throw new CCError(e);
        } catch (ParserConfigurationException e) {
            throw new CCError(e);
        } catch (SAXException e) {
            throw new CCXmlParsingException("XML para conversão é inválido", e);
        } catch (IOException e) {
            throw new CCError(e);
        }
    }

    /**
     * This method is responsible for parsing the given request String into a list of Attributes
     *
     * @param requestData
     *            the requested fields as a string
     * @return a list of all the attributes already parsed from the XML
     *
     * @throws CCXmlParsingException
     * @throws CCError
     */
    public static List<Attribute> parseRequestedData(final String requestData) throws CCXmlParsingException, CCError {
        List<Attribute> tempSet = new ArrayList<Attribute>();

        try {
            final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document doc = db.parse(new InputSource(new StringReader(requestData)));
            final Element rootNode = doc.getDocumentElement();

            addLeafNodes(tempSet, rootNode);

            return tempSet;
        } catch (SAXException ex) {
            throw new CCXmlParsingException("Parâmetro dos atributos não é XML vaĺido", ex);
        } catch (ParserConfigurationException ex) {
            throw new CCError(ex);
        } catch (IOException ex) {
            throw new CCError(ex);
        }
    }

    /**
     * Useful method for adding the leaf nodes from the given element to the given list of attributes
     *
     * @param attributes
     * @param element
     */
    private static void addLeafNodes(final List<Attribute> attributes, final Element element) {
        if (element.hasChildNodes()) {
            final NodeList childNodes = element.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    addLeafNodes(attributes, (Element) childNode);
                }
            }
        } else {

            final boolean optional = element.hasAttribute("opcional") ? Boolean.parseBoolean(element.getAttribute("opcional")) : false;
            final String tagName = element.getTagName();
            final Attribute attribute = AttributeUtils.getAttributeNamed(tagName);
            attribute.setOptional(optional);
            attributes.add(attribute);
        }
    }
}
