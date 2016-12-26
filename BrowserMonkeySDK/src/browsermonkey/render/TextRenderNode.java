package browsermonkey.render;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.regex.*;
import javax.swing.SwingUtilities;

/**
 * Renders formatted text with word wrap as a self-sizing RenderNode.
 * @author Paul Calcraft
 */
public class TextRenderNode extends RenderNode {
    private AttributedString text;
    private String textString;
    private boolean centred;
    private ArrayList<Integer> hardLineBreaks;
    private Map<Rectangle, TextLayout> textLayouts;

    private static FontRenderContext fontRenderContext;
    private static Map<String, Character> characterEntities;

    static {
        // Set up the font render context for text measuring and rendering.
        
        // Default hint values (basic anti-aliasing and no fractional metrics).
        Object aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
        Object fmHint = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;

        // Attempt to get system text rendering settings and apply if available.
        // This e.g. allows Windows machines to use ClearType when enabled.
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Map map = (Map)(toolkit.getDesktopProperty("awt.font.desktophints"));
            if (map != null) {
                Object systemAA = map.get(RenderingHints.KEY_TEXT_ANTIALIASING);
                if (systemAA != null)
                    aaHint = systemAA;
                Object systemFM = map.get(RenderingHints.KEY_FRACTIONALMETRICS);
                if (systemFM != null)
                    fmHint = systemFM;
            }
        } catch (Exception e) {
            // Error getting system values, just use defaults.
        }

        fontRenderContext = new FontRenderContext(null, aaHint, fmHint);

        // Set up the character entities map.
        characterEntities = new HashMap<String, Character>();
        characterEntities.put("nbsp", '\u00A0');
        characterEntities.put("pound", '£');
        characterEntities.put("copy", '©');
        characterEntities.put("reg", '®');
        characterEntities.put("trade", '™');
        characterEntities.put("quot", '"');
        characterEntities.put("apos", '\'');
        characterEntities.put("amp", '&');
        characterEntities.put("lt", '<');
        characterEntities.put("gt", '>');
        characterEntities.put("bull", '•');
        characterEntities.put("raquo", '▼');
        characterEntities.put("para", '¶');
        characterEntities.put("frac14", '¼');
        characterEntities.put("frac12", '½');
        characterEntities.put("frac34", '¾');
        characterEntities.put("ntilde", 'ñ');
        characterEntities.put("hellip", '…');
    }

    public TextRenderNode(Linkable linker) {
        this(linker, false);
    }

    /**
     * Constructs an optionally centred node, ready for text to be added.
     * @param linker the linker for the document this text is in
     * @param centred
     */
    public TextRenderNode(Linkable linker, boolean centred) {
        super(linker);

        this.centred = centred;
        textString = "";
        text = new AttributedString(textString);
        hardLineBreaks = new ArrayList<Integer>();
        textLayouts = new LinkedHashMap<Rectangle, TextLayout>();

        // Detect mouse clicks to respond to links.
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                click(new Point(e.getX(), e.getY()));
            }

            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
        });
    }

    @Override
    public void setZoomLevel(float zoomLevel) {
        // Don't zoom if empty.
        if (!isEmpty()) {
            // Get the screen resolution and perform dpi correction, and use this
            // with the zoom level to set the size transform of the text.
            int screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
            double dpiCorrection = screenResolution/72d;
            text.addAttribute(TextAttribute.TRANSFORM, new TransformAttribute(AffineTransform.getScaleInstance(zoomLevel*dpiCorrection, zoomLevel*dpiCorrection)));
        }

        calculateBasicSizeRequirements();
    }

    @Override
    public void extractTextInto(ArrayList<AttributedString> text) {
        if (!isEmpty())
            text.add(this.text);
    }


    /**
     * The TextAttribute (anonymous subclass) instance for storing the link
     * href. Values for this attribute should be Strings.
     */
    public static final AttributedCharacterIterator.Attribute HREF_ATTRIBUTE = new AttributedCharacterIterator.Attribute("href") {};

    // Handles a click event on this node.
    private void click(Point hitPoint) {
        int cumulativeCharacterCount = 0;
        // Gets the set of entries in the text layouts for mapping areas of the
        // node to TextLayout objects.
        Set<Map.Entry<Rectangle, TextLayout>> textLines = textLayouts.entrySet();
        for (Map.Entry<Rectangle, TextLayout> line : textLines) {
            // Retrieve key and value.
            Rectangle lineRect = line.getKey();
            TextLayout lineLayout = line.getValue();
            // If our hit is in the rectangle for this line...
            if (lineRect.contains(hitPoint)) {
                // Get the character that was clicked, if any.
                TextHitInfo hitInfo = lineLayout.hitTestChar(hitPoint.x-lineRect.x, hitPoint.y-lineRect.y);
                if (hitInfo != null) {
                    // Get an attributed iterator at this point in the string.
                    AttributedCharacterIterator aci = text.getIterator();
                    aci.setIndex(cumulativeCharacterCount + hitInfo.getCharIndex());

                    // Retrieve the href attribute at this character.
                    Object hrefValue = aci.getAttribute(HREF_ATTRIBUTE);
                    if (hrefValue != null) {
                        // If there is one, find the start and end of the link,
                        // and turn it red, as it's active.
                        int hrefStart = aci.getRunStart(HREF_ATTRIBUTE);
                        int hrefEnd = aci.getRunLimit(HREF_ATTRIBUTE);
                        text.addAttribute(TextAttribute.FOREGROUND, Color.red, hrefStart, hrefEnd);
                        repaint();
                        // Follow the link specified by the href attribute.
                        linker.followLink((String)hrefValue);
                    }
                    return;
                }
                
            }
            // Increment the cumulative character count to get the right place
            // in the String when we do find a hit.
            cumulativeCharacterCount += lineLayout.getCharacterCount();
        }
    }

    /**
     * Returns whether this text node is empty.
     * @return
     */
    public boolean isEmpty() {
        return textString.isEmpty();
    }

    /**
     * Adds some text to the node with the given formatting.
     * @param newText
     * @param formatting
     */
    public void addText(String newText, Map<Attribute,Object> formatting) {
        if (newText.isEmpty())
            return;

        // AttributedStrings cannot be concatenated so we construct a new one
        // manually.

        // The indices of the ends of each run (range with constant attribute
        // set).
        ArrayList<Integer> endIndices = new ArrayList<Integer>();
        // Map of formatting for each run.
        ArrayList<Map<Attribute,Object>> formats = new ArrayList<Map<Attribute,Object>>();

        // Retrieve an iterator for the attribute string.
        AttributedCharacterIterator it = text.getIterator();

        // Iterate through the string looking for run boundaries, populating the
        // above ArrayLists.
        int previousEndIndex = 0;
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if (it.getRunLimit() > previousEndIndex) {
                previousEndIndex = it.getRunLimit();
                endIndices.add(previousEndIndex);
                formats.add(it.getAttributes());
            }
        }

        // Create a StringBuilder from the existing text.
        StringBuilder builder = new StringBuilder(textString);

        // If we start with a whitespace character and the existing text string
        // is either empty or ends with a space, ignore our first space.
        // Note the parser guarantees all whitespace be collapsed to a single
        // space for non-preformatted elements.
        if (newText.startsWith(" ") && (isEmpty() || textString.endsWith(" ")))
            newText = newText.substring(1);

        // If the text we're adding is now empty, don't bother.
        if (newText.isEmpty())
            return;

        // Trim line breaks to a single \n character.
        newText = newText.replaceAll("\\r\\n|\\n\\r|\\r|\\n", "\n");

        // Append the input string into newText, while replacing any character
        // entities.
        int currentPos = 0;
        int findPos;
        while ((findPos = newText.indexOf('&', currentPos)) != -1) {
            int endEntityIndex = newText.indexOf(';', findPos+1);
            if (endEntityIndex == -1)
                break;
            builder.append(newText.substring(currentPos, findPos));
            boolean entityReplaced = false;
            String entityText = newText.substring(findPos+1, endEntityIndex).toLowerCase();
            if (entityText.charAt(0) == '#') {
                try {
                    int value;
                    if (entityText.charAt(1) == 'x')
                        value = Integer.parseInt(entityText.substring(2), 16);
                    else
                        value = Integer.parseInt(entityText.substring(1));
                    builder.append((char)value);
                    entityReplaced = true;
                } catch (NumberFormatException ex) {
                    entityReplaced = false;
                }
            }
            else {
                Character character = characterEntities.get(entityText);
                if (character != null) {
                    builder.append(character);
                    entityReplaced = true;
                }
            }

            if (entityReplaced)
                currentPos = endEntityIndex+1;
            else {
                currentPos = findPos+1;
                builder.append('&');
            }
        }

        // Append the rest of the string.
        builder.append(newText.substring(currentPos));

        // Add a run end index as the end of the whole string.
        endIndices.add(builder.length());
        // This is for the run that is the new text we've added, so add the
        // corresponding formatting in the formats ArrayList.
        formats.add(formatting);

        // Update the text for this node, and create the new attributed string.
        textString = builder.toString();
        text = new AttributedString(textString);

        // Add all the formatting for the runs in the text string.
        previousEndIndex = 0;
        for (int i = 0; i < endIndices.size(); i++) {
            text.addAttributes(formats.get(i), previousEndIndex, endIndices.get(i));
            previousEndIndex = endIndices.get(i);
        }

        // Find any hard line breaks in the input.
        // (Should only occur within pre tags.)
        hardLineBreaks.clear();
        int nextLineBreak = -1;
        while ((nextLineBreak = textString.indexOf("\n", nextLineBreak+1)) != -1) {
            hardLineBreaks.add(nextLineBreak);
        }
        // Specify a hard line break at the end of the string.
        hardLineBreaks.add(textString.length());
    }

    // Compiled regular expressions pattern for identifying where a line can
    // break. According to the LineBreakMeasurer, a space between 2 non-breaking
    // spaces should not break, so we have to use negative lookahead and
    // lookbehind in the regex pattern.
    private static Pattern breakableStringPattern = Pattern.compile("(?<!\u00A0) (?!\u00A0)|\\n");

    // Calculates the basic size constraints of the text node and applies them.
    private void calculateBasicSizeRequirements() {
        if (isEmpty()) {
            Dimension newDimension = new Dimension(0, 0);
            setMinimumSize(newDimension);
            setMaximumSize(newDimension);
            return;
        }

        AttributedCharacterIterator it = text.getIterator();

        TextMeasurer measurer = new TextMeasurer(it, fontRenderContext);

        // Find the longest non-breaking "word" in the text, this defines
        // the smallest possible width the text node can have.
        int longestSingleWord = 0;

        Matcher softLineBreakFinder = breakableStringPattern.matcher(textString);
        int previousBreak = 0;
        while (previousBreak < textString.length()) {
            // Default values for breaks at end of text.
            int currentBreak = textString.length();
            int currentBreakEnd = textString.length();

            // If we can find another soft line break, use that.
            if (softLineBreakFinder.find()) {
                currentBreak = softLineBreakFinder.start();
                currentBreakEnd = softLineBreakFinder.end();
            }

            // If there is text between the previous break and the current
            // break, calculate its width, and use in max operation to find
            // longest single word pixel length.
            if (currentBreak > previousBreak) {
                TextLayout layout = measurer.getLayout(previousBreak, currentBreak);
                int pixelLength = (int)Math.ceil(layout.getAdvance());
                longestSingleWord = Math.max(longestSingleWord, pixelLength);
            }
            // Set the previous break for the next iteration as after this
            // iteration's break.
            previousBreak = currentBreakEnd;
        }

        // Calculate the bounds of the text assuming it has as bigger
        // width as it needs. This becomes the maximum width and the minimum
        // height.
        int maximumWidth = 0;
        int minimumHeight = 0;
        int previousLineBreakIndex = 0;
        for (Integer lineBreakIndex : hardLineBreaks) {
            if (lineBreakIndex > previousLineBreakIndex) {
                TextLayout singleLineLayout = measurer.getLayout(previousLineBreakIndex, lineBreakIndex);
                maximumWidth = Math.max(maximumWidth, (int)Math.ceil(singleLineLayout.getAdvance()));
                minimumHeight += (int)Math.ceil(singleLineLayout.getAscent() + singleLineLayout.getDescent() + singleLineLayout.getLeading());
            }
            previousLineBreakIndex = lineBreakIndex;
        }

        setMinimumSize(new Dimension(longestSingleWord, minimumHeight));
        setMaximumSize(new Dimension(maximumWidth, Short.MAX_VALUE));

        // Use invokeLater to revalidate (and re-layout) the component after the
        // current layout operation has completed.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                revalidate();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        // If empty, don't paint.
        if (isEmpty()) {
            return;
        }

        AttributedCharacterIterator it = text.getIterator();
        LineBreakMeasurer lineBreaker = new LineBreakMeasurer(it, fontRenderContext);

        // Maps line bounding rectangles to their TextLayouts for links.
        // Linked hash map to maintain order of text layouts in iteration.
        textLayouts.clear();

        // Render the text using the AttributedCharacterIterator.
        
        int hardLineIndex = 0;
        Point coord = new Point(0, 0);
        // Wrap to current width of text node.
        float wrappingWidth = getWidth();
        TextLayout layout = null;
        // While the upcoming line break is less than the final character.
        while (lineBreaker.getPosition() < it.getEndIndex()) {
            // If we've moved beyond a hard line break, update.
            if (lineBreaker.getPosition() > hardLineBreaks.get(hardLineIndex))
                hardLineIndex++;

            // Find a line break within the wrapping width but don't fill the
            // line any further than the next hard line break.
            layout = lineBreaker.nextLayout(wrappingWidth, hardLineBreaks.get(hardLineIndex)+1, true);

            if (layout == null) {
                break;
            }

            // Add the ascent of the line to the y co-ordinate.
            coord.y += layout.getAscent() ;

            // Calculate delta x based on alignment.
            float dx;
            if (centred)
                dx = (wrappingWidth-layout.getAdvance())/2f;
            else
                dx = layout.isLeftToRight() ? 0 : (wrappingWidth - layout.getAdvance());

            // Draw the attributed text at the appropriate co-ordinates.
            layout.draw((Graphics2D)g, coord.x + dx, coord.y);
            // Add to the click rectangle/layout lookup.
            textLayouts.put(layout.getPixelBounds(null, coord.x + dx, coord.y), layout);
            // Add the line spacing.
            coord.y += layout.getDescent() + layout.getLeading();
        }
    }

    // Calculates the size constraints of the component based on wrapping the
    // text at the current width.
    private void calculateCurrentSizeRequirements() {
        if (isEmpty())
            return;

        AttributedCharacterIterator it = text.getIterator();
        LineBreakMeasurer lineBreaker = new LineBreakMeasurer(it, fontRenderContext);

        // Render the text using the AttributedCharacterIterator.
        int hardLineIndex = 0;
        Point coord = new Point(0, 0);

        // Wrap to current width of text node.
        float wrappingWidth = getWidth();
        TextLayout layout = null;
        // While the upcoming line break is less than the final character.
        while (lineBreaker.getPosition() < it.getEndIndex()) {
            // If we've moved beyond a hard line break, update.
            if (lineBreaker.getPosition() > hardLineBreaks.get(hardLineIndex))
                hardLineIndex++;

            // Find a line break within the wrapping width but don't fill the
            // line any further than the next hard line break.
            layout = lineBreaker.nextLayout(wrappingWidth, hardLineBreaks.get(hardLineIndex)+1, true);

            if (layout == null) {
                return;
            }

            // Add the ascent of the line to the y co-ordinate.
            coord.y += layout.getAscent() ;

            // Add the line spacing.
            coord.y += layout.getDescent() + layout.getLeading();
        }

        setMinimumSize(new Dimension(getMinimumSize().width, coord.y));
        setMaximumSize(new Dimension(getMaximumSize().width, coord.y));

        // Use invokeLater to revalidate (and re-layout) the component after the
        // current layout operation has completed.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                revalidate();
            }
        });
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        // If the bounding width changes, we need to set widthChanged so the
        // paint method knows to reset the height when it word wraps.
        int currentWidth = getWidth();
        int currentHeight = getHeight();
        super.setBounds(x, y, width, height);
        if (currentWidth != width || currentHeight != height) {
            calculateCurrentSizeRequirements();
        }
        
    }
}