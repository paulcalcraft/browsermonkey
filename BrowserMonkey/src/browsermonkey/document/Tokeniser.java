package browsermonkey.document;

import browsermonkey.utility.BrowserMonkeyLogger;
import java.util.*;

/**
 * Tokeniser class takes the raw text input and produces a useful tree of Tokens
 * for the Parser to use. It does this mainly by looking for <> characters.
 * @author Paul Calcraft, Daniel Cooper, Lawrence Dine
 */
public class Tokeniser {
    private List<Token> tokens;
    private String page;
    private int currentPos;
    private boolean conformant;

    /**
     * Returns true if the tokenisation didn't have to compensate for any
     * conformance errors.
     * @return True if conformant
     */
    public boolean isConformant() {
        return conformant;
    }
    
    private void conformanceError(String error){
        BrowserMonkeyLogger.conformance(error);
        conformant = false;
    }

    /**
     * Tokenises the text from the input into a list of tokens.
     * @param input a <code>Reader</code> for the input text
     */
    public Tokeniser(String input) {
        tokens = new ArrayList<Token>();
        page = input;
        currentPos = 0;
        conformant = true;
    }

    /**
     * Tokenise method is used to call the method that does the actual tokenisation
     * over and over again until the tokenising is complete.
     */
    public void tokenise() {
        while (currentPos < page.length()) {
            getNextToken();
        }
    }

    /**
     * This method does all the clever work for tokenising the text. See source
     * for explanatory comments.
     */
    public void getNextToken() {
        if (page.charAt(currentPos) == '<') {   //If the character at the current position in the text is a < and therefore is opening a tag
            if (page.length() >= currentPos + 4 && page.substring(currentPos + 1, currentPos + 4).equals("!--")) {   //First we do a check to see if it's a comment
                int tagTokenEnd = page.indexOf("-->", currentPos + 4);          //If it is then we skip it without doing anything
                if(tagTokenEnd == -1){                                          //Conformance testing
                    currentPos = page.length();
                    conformanceError("Comment tag does not end, treating rest of the document as a comment.");
                } else {
                    currentPos = tagTokenEnd + 3;               //Skipped
                }
            } else {
                int nextTagOpen = page.indexOf('<', currentPos + 1);            // if itisn't a comment we check for the next open and close tags
                int tagTokenEnd = page.indexOf('>', currentPos + 1);

                if (nextTagOpen == -1) {
                    nextTagOpen = page.length();        //check in case this is the last tag and there is no next tag
                }

                String fullTag;
                if (tagTokenEnd == -1 || tagTokenEnd > nextTagOpen) {               //Conformance fixing for if the next end tag is after an open tag
                    fullTag = page.substring(currentPos, nextTagOpen) + ">";        //instead of breaking it treats the whole text between the open and close as being one tag
                    tagTokenEnd = nextTagOpen;

                    if (nextTagOpen == page.length())
                        conformanceError("Tag does not end with '>' throughout the document, closing at end of document: "+fullTag);
                    else
                        conformanceError("Tag does not close with '>' before another is opened with '<', forcing close: "+fullTag);
                } else {
                    fullTag = page.substring(currentPos, tagTokenEnd + 1);
                    tagTokenEnd++;
                }

                currentPos = tagTokenEnd;               //Moving on current position by length of tag

                if (fullTag.matches("<\\s*/?\\s*>")) {
                    conformanceError("Empty tag found, ignoring.");
                    return;
                }

                Token token = new Token(fullTag, TokenType.TAG);           //creating a new tag token with the tag stored
                tokens.add(token);
                

                if (token.getTag().equals("title")) {               //Special case handling for title tag
                    int endTitle = page.substring(tagTokenEnd).toLowerCase().indexOf("</title>");
                    String text;
                    if (endTitle != -1) {
                        endTitle += tagTokenEnd;
                        text = page.substring(currentPos, endTitle);
                        currentPos = endTitle + 8;
                    } else {
                        conformanceError("Title tag does not end, treating rest of document as title.");
                        text = page.substring(currentPos, page.length());
                        currentPos = page.length();
                    }
                    tokens.add(new Token(text, TokenType.TEXT));
                    tokens.add(new Token("</title>", TokenType.TAG));
                }
            }
        } else {
            int textTokenEnd = page.indexOf('<', currentPos);       //This scoops all text between tags into a text token
            String text;
            if (textTokenEnd != -1) {
                text = page.substring(currentPos, textTokenEnd);
            } else {
                text = page.substring(currentPos, page.length());
            }
            currentPos = currentPos + text.length();
            tokens.add(new Token(text, TokenType.TEXT));
        }
    }

    /**
     * After the tokenisation is complete this is used to get an iterator
     * containing the tokens. This method is used by the <code>Parser</code>.
     * @return Iterator of Tokens
     */
    public Iterator<Token> getTokens() {
        return tokens.iterator();
    }
}