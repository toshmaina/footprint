package ke.co.skyworld.internship.util.formatting;


import ke.co.skyworld.internship.config.Constants;
import org.fusesource.jansi.Ansi;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static org.fusesource.jansi.Ansi.ansi;



public class XmlUtils {

    private static Document parseXML() {
        Document doc = null;
        try {
            File inputFile = new File(Constants.LOCAL_CONF_FILENAME);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println(ansi().fg(Ansi.Color.YELLOW).bold().a("INFO: Terminating server"));
            System.exit(0);
        }
        return doc;
    }

    public static Document parseXml(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readXMLTag(String tagPath) {

        Document XML = parseXML();

        XPathExpression xp;
        try {
            xp = XPathFactory.newInstance().newXPath().compile(tagPath);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return Constants.SSA_DELIMITER;
        }

        try {
            return Objects.requireNonNull(xp).evaluate(XML);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return Constants.SSA_DELIMITER;
        }
    }

    public static String readXMLTag(String xml, String tagPath) {

        Document XML = parseXml(xml);

        XPathExpression xp;
        try {
            xp = XPathFactory.newInstance().newXPath().compile(tagPath);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return Constants.SSA_DELIMITER;
        }

        try {
            return Objects.requireNonNull(xp).evaluate(XML);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return Constants.SSA_DELIMITER;
        }
    }

    public static boolean updateXMLTag(String tagPath, String tagTextContent) {
        Document XML = parseXML();

        try {
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath().compile(tagPath)
                    .evaluate(XML, XPathConstants.NODESET);

            nodeList.item(0).setTextContent(tagTextContent);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(XML);
            StreamResult streamResult = new StreamResult(new File(Constants.LOCAL_CONF_FILENAME));
            transformer.transform(source, streamResult);

            return true;

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean updateXMLTagWithAttribute(String tagPath, String tagTextContent,
                                                    String attributeName, String attributeValue) {
        Document XML = parseXML();

        try {
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath().compile(tagPath)
                    .evaluate(XML, XPathConstants.NODESET);

            if (nodeList.getLength() == 0) {
                return false;
            }

            Element element = (Element) nodeList.item(0);
            element.setTextContent(tagTextContent);
            element.setAttribute(attributeName, attributeValue);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(XML);
            StreamResult streamResult = new StreamResult(new File(Constants.LOCAL_CONF_FILENAME));
            transformer.transform(source, streamResult);

            return true;

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String readXMLTag(Document xmlDoc, String tagPath) {

        XPathExpression xp;
        try {
            xp = XPathFactory.newInstance().newXPath().compile(tagPath);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return Constants.SSA_DELIMITER;
        }

        try {
            return Objects.requireNonNull(xp).evaluate(xmlDoc);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return Constants.SSA_DELIMITER;
        }
    }

    public static Document updateXMLTag(Document xmlDoc, String tagPath, String tagTextContent) {

        try {
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath().compile(tagPath)
                    .evaluate(xmlDoc, XPathConstants.NODESET);

            nodeList.item(0).setTextContent(tagTextContent);

            return xmlDoc;

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return xmlDoc;
    }

    public static List<List<XmlTag>> marshalTagsInXmlTag(Document xmlDoc, String path) {

//        ArrayList<XmlTag> xmlTags = new ArrayList<>();
        List<List<XmlTag>> questions = new ArrayList<>();

        try {
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath().compile(path)
                    .evaluate(xmlDoc, XPathConstants.NODESET);

//            System.out.println(nodeList.getLength());

            for (int i = 0; i < nodeList.getLength(); i++) {

                NodeList children = nodeList.item(i).getChildNodes();
                List<XmlTag> question = new ArrayList<>();

                for (int j = 0; j < children.getLength(); j++) {

                    XmlTag questionProperty = new XmlTag();

                    if (!children.item(j).getNodeName().equals("#text")) {
                        questionProperty.setName(children.item(j).getNodeName());
                        questionProperty.setValue(children.item(j).getTextContent());
                        questionProperty.setXmlContentAsString(convertNodeToStr(children.item(j)));
                        questionProperty.setChildCount(children.item(j).getChildNodes().getLength());

                        NamedNodeMap questionPropertyAttributes = children.item(j).getAttributes();

                        for (int k = 0; k < questionPropertyAttributes.getLength(); k++) {
                            Node questionPropertyAttribute = questionPropertyAttributes.item(k);
                            questionProperty.addAttribute(questionPropertyAttribute.getNodeName(),
                                    questionPropertyAttribute.getTextContent());
                        }

                        question.add(questionProperty);
                    }
                }

                questions.add(question);

            }

            return questions;

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String convertNodeToStr(Node node) {
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(node);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String xmlToString(Document xmlDocument) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        StringWriter writer = new StringWriter();
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));
            String xmlString = writer.getBuffer().toString();

            writer.close();
            return xmlString;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                writer.close();
            } catch (Exception ignore) {
            }

        }

        try {
            writer.close();
        } catch (Exception ignore) {
        }


        return "";
    }

    public static HashMap<String, String> getValuesAndAttributes(Document xmlDoc, String tagName,
                                                                 String attributeName) {
        HashMap<String, String> valuesAndAttributes = new LinkedHashMap<>();

        try {
            XPathFactory fact = XPathFactory.newInstance();
            XPath xpath = fact.newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("//*", xmlDoc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeName().equals(tagName)) {
                    valuesAndAttributes.put(node.getTextContent(),
                            node.getAttributes().getNamedItem(attributeName).getTextContent());
                }

            }

            return valuesAndAttributes;

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String convertXmlDocObjectToStr(Document document, boolean omitXmlDeclaration) throws Exception {
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
        transformer.transform(domSource, result);
        return writer.toString();
    }


    public static List<String> readXMLTags(String xmlPath, String tagName) {
        List<String> paths = new ArrayList<>();
        Document xmlDocument = parseXML();

        String expression = xmlPath + "/" + tagName;

        XPathExpression xp;
        try {
            xp = XPathFactory.newInstance().newXPath().compile(expression);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return paths;
        }

        try {
            NodeList nodeList = (NodeList) xp.evaluate(xmlDocument, javax.xml.xpath.XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                paths.add(element.getTextContent());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return paths;
    }
}
