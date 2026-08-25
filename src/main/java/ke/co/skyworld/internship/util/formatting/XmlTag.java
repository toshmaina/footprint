package ke.co.skyworld.internship.util.formatting;

import java.util.LinkedHashMap;



public class XmlTag {
    private String name;
    private String value;
    private LinkedHashMap<String, String> attributes;
    private String xmlContentAsString;
    private Integer childCount;

    public XmlTag() {
        this.attributes = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LinkedHashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(LinkedHashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getXmlContentAsString() {
        return xmlContentAsString;
    }

    public void setXmlContentAsString(String xmlContentAsString) {
        this.xmlContentAsString = xmlContentAsString;
    }

    public String getInnerXmlContentAsString(String parentTag) {
        String xmlContentAsString = this.xmlContentAsString;
        xmlContentAsString = xmlContentAsString.replaceAll(
                "<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>", "");
        xmlContentAsString = xmlContentAsString.replaceAll("<" + parentTag + ">", "");
        return xmlContentAsString.replaceAll("</" + parentTag + ">", "").trim();
    }

    public void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    public String getAttribute(String key) {
        return this.attributes.get(key);
    }

    public Integer getChildCount() {
        return childCount;
    }

    public void setChildCount(Integer childCount) {
        this.childCount = childCount;
    }

    @Override
    public String toString() {
        return "XmlTag{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", attributes=" + attributes +
                ", xmlContentAsString='" + xmlContentAsString + '\'' +
                ", childCount=" + childCount +
                '}';
    }
}
