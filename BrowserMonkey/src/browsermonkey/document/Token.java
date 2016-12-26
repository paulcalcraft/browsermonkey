package browsermonkey.document;

import java.util.HashMap;
import java.util.Map;
import browsermonkey.utility.RegexUtility;

/**
 * Represents a token for use in the tokeniser and the parser. Stores the tag or
 * text found at that position in the html file and stores information about the
 * tag if it is one. Such as whether the tag is an end tag and attributes.
 * @author Lawrence Dine
 */
public class Token {
    private String tag;
    private boolean endTag;
    private String fullTag;
    private Map<String, String> attributes;
    private TokenType type;

    /**
     * Constructor for class token. Creates a new token with the supplied tag and
     * type, extracts the tag itself and the attributes then sends it to be
     * classified.
     * @param fullTag Contains the 'full tag' in the case of text this is just text but in the case of the tag it includes the <> or </> and any attributes
     * @param type Whether this token is a Tag or Text type
     */
    public Token(String fullTag, TokenType type){
        this.fullTag = fullTag;
        this.type = type;

        attributes = null;

        endTag = false;

        if (type == TokenType.TEXT){
            tag = fullTag;
        } else if(type == TokenType.TAG) { //If type is tag
            //Regex to get the a in <a href="b">
            tag = RegexUtility.scan(fullTag, "[\\w:-]+")[0][0].toLowerCase();
            classifyTag();
        }
    }

    /**
     * Returns true if the token contains an end tag.
     * @return True if token represents end tag
     */
    public boolean isEndTag(){
        return endTag;
    }

    /**
     * Returns true if the token contains a start tag.
     * @return True if this token represents a start tag
     */
    public boolean isStartTag(){
        return !endTag;
    }

    /**
     * Returns true if this token has any attributes in its attribute map.
     * @return True if there are any attributes
     */
    public boolean hasAttributes(){
        return (attributes.size() > 0);
    }

    /**
     * Returns the attribute map from this token.
     * @return Map containing all the attributes stored for this tag token
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Returns the full tag value used to create the token, i.e: the tag with
     * all the attributes and the <> still in place.
     * @return Full tag for this token
     */
    public String getFullTag(){
        return fullTag;
    }

    /**
     * Returns the type of this token as a <code>TokenType</code>. Lets you check
     * to see whether this is a text or tag token.
     * @return <code>TokenType</code> of this token
     */
    public TokenType getType(){
        return type;
    }

    /**
     * Lets you change the tag of the token.
     * @param tag Tag to change this token to
     */
    public void setTag(String tag) {
        this.fullTag.replaceFirst(this.tag, tag);
        this.tag = tag;
    }

    /**
     * Getter for the tag variable.
     * @return Tag for this token
     */
    public String getTag(){
        return tag;
    }

    /**
     * This method extracts any attributes that may be present in the full tag of
     * this token. It extracts them, separates them and puts them into the attributes
     * map. Also sets up the variables like endTag.
     */
    public void classifyTag(){
        String[][] atts = RegexUtility.scan(fullTag, "<[\\w:-]+\\s+(.*)>");
        if (atts.length > 0) {
            String[][] attributeStrings = RegexUtility.scan(atts[0][0], "\\s*([\\w:-]+)\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\"'>][^\\s>]*)");
            //Regex grabs any attributes from the tag and stores them in a hash map
            attributes = new HashMap<String, String>();

            for (String[] attribute : attributeStrings) {
                String value = attribute[1];
                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("\'") && value.endsWith("\'")))
                    value = value.substring(1, value.length()-1);
                attributes.put(attribute[0].toLowerCase(), value);
            }
        }

        //Determine if the tag is an end tag by looking for a / before the tag name. (</b>)
        int endTagIndex = fullTag.indexOf('/');
        if(endTagIndex != -1){
            int tagPos = fullTag.toLowerCase().indexOf(tag);
            if(endTagIndex < tagPos){
                endTag = true;
            }
        }
    }


}