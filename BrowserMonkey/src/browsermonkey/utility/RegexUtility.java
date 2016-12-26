package browsermonkey.utility;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegexUtlity class is used to provide regex functionality.
 * @author Paul Calcraft
 */
public class RegexUtility {
    /**
     * Performs a regex scan over a String. Based on Ruby's String#scan method.
     * @param input the input string to scan
     * @param regex the regex string
     * @return an array of String[], each String[] representing the group
     * elements of each consecutive match.
     */
    public static String[][] scan(String input, String regex) {
        ArrayList<String[]> result = new ArrayList<String[]>();

        Pattern pattern = Pattern.compile(regex);
        Matcher patternMatcher = pattern.matcher(input);

        int groupCount = patternMatcher.groupCount();

        while (!patternMatcher.hitEnd()) {
            if (!patternMatcher.find())
                break;

            String[] matchElements;
            if (groupCount > 0) {
                matchElements = new String[groupCount];
                for (int i = 0; i < groupCount; i++) {
                    matchElements[i] = patternMatcher.group(i+1);
                }
            }
            else
                matchElements = new String[] { patternMatcher.group() };

            result.add(matchElements);
        }
        
        return result.toArray(new String[result.size()][]);
    }
}