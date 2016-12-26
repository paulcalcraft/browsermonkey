package browsermonkey.document;

import java.util.Map;

/**
 * Represents a tag in our <code>DocumentNode</code> tree.
 * @author Paul Calcraft
 */
public class TagDocumentNode extends DocumentNode {
    private String type;
    private Map<String, String> attributes;

    /**
     * Gets the tag type (e.g. "table").
     * @return
     */
    public String getType() {
        return type;
    }


    /**
     * Gets the value of the specified attribute, or null if it is not
     * set.
     * @param attribute the attribute to obtain a value for
     * @return the value as a String, or null
     */
    public String getAttribute(String attribute) {
        if (attributes == null)
            return null;
        return attributes.get(attribute);
    }

    /**
     * Consructs a new <code>TagDocumentNode</code> with the specified type and
     * attributes.
     * @param type
     * @param attributes the map of attributes, can be null if empty
     */
    public TagDocumentNode(String type, Map<String, String> attributes){
        this.type = type;
        this.attributes = attributes;
    }

    /**
     * Consructs a new <code>TagDocumentNode</code> with the specified type,
     * attributes and children.
     * @param type
     * @param attributes the map of attributes, can be null if empty
     * @param children
     */
    public TagDocumentNode(String type, Map<String, String> attributes, DocumentNode... children) {
        super(children);
        this.type = type;
        this.attributes = attributes;
    }

    @Override
    public String toDebugString() {
        StringBuilder builder = new StringBuilder();
        
        // Write the start of the tag
        builder.append('<');
        builder.append(type);
        // Write all the attributes
        if (attributes != null)
            for (Map.Entry<String, String> attribute : attributes.entrySet())
               builder.append(" "+attribute.getKey()+"=\""+attribute.getValue()+"\"");
        // End the tag
        builder.append('>');
        builder.append("\r\n");
        // Add the debug string for all children.
        for (DocumentNode child : children)
            builder.append(child.toDebugString()+"\r\n");
        // Close the tag.
        builder.append("</");
        builder.append(type);
        builder.append('>');

        return builder.toString();
    }
}