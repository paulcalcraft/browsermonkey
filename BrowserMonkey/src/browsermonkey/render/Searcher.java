package browsermonkey.render;

import java.util.*;
import java.text.*;
import java.text.AttributedCharacterIterator.Attribute;

/**
 * The Searcher class carries out all searching operations for the browser.
 * @author Paul Calcraft
 */
public class Searcher {
    private static class AttributedTextRange {
        private AttributedString text;
        private int startIndex;
        private int endIndex;

        public AttributedTextRange(AttributedString text, int startIndex, int endIndex) {
            this.text = text;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public void addAttributes(Map<Attribute, Object> attributes) {
            text.addAttributes(attributes, startIndex, endIndex);
        }
    }

    /**
     * Searches through the document highlighting any words that match the term
     * provided.
     * @param textRanges Static class used to store results
     * @param term Term to be searched for
     * @param highlightAttributes Adds a highlight attribute to any terms found
     * @return Number of results found
     */
    public static int highlightSearchTerm(AttributedString[] textRanges, String term, Map<Attribute, Object> highlightAttributes) {
        for (AttributedString textRange : textRanges) {
            for (Attribute attribute : highlightAttributes.keySet())
                textRange.addAttribute(attribute, null);
        }
        
        if (term.length() == 0)
            return 0;

        term = term.toLowerCase();

        int findCount = 0;

        ArrayList<AttributedTextRange> currentRunRanges = new ArrayList<AttributedTextRange>();

        int currentRunCharacterIndex = 0;
        for (int i = 0; i < textRanges.length; i++) {
            
            AttributedCharacterIterator iterator = textRanges[i].getIterator();
            int rangeRunStartIndex = 0;
            for(char current = iterator.first(); current != CharacterIterator.DONE; current = iterator.next()) {
                current = Character.toLowerCase(current);
                if (term.charAt(currentRunCharacterIndex) == current) {
                    if (currentRunCharacterIndex == 0)
                        rangeRunStartIndex = iterator.getIndex();
                    currentRunCharacterIndex++;
                    if (currentRunCharacterIndex == term.length()) {
                        currentRunRanges.add(new AttributedTextRange(textRanges[i], rangeRunStartIndex, iterator.getIndex()+1));
                        
                        for (AttributedTextRange atr : currentRunRanges) {
                            atr.addAttributes(highlightAttributes);
                        }
                        
                        findCount++;
                        
                        currentRunCharacterIndex = 0;
                        currentRunRanges.clear();
                    }
                }
                else {
                    currentRunCharacterIndex = 0;
                    currentRunRanges.clear();
                }
            }
            if (currentRunCharacterIndex > 0) {
                currentRunRanges.add(new AttributedTextRange(textRanges[i], rangeRunStartIndex, iterator.getEndIndex()));
            }         
        }
        
        return findCount;
    }
}
