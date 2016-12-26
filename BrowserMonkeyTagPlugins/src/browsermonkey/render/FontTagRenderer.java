package browsermonkey.render;

import browsermonkey.document.*;
import browsermonkey.utility.BrowserMonkeyLogger;
import java.text.AttributedCharacterIterator.Attribute;
import java.awt.*;
import java.awt.font.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Renders font tags from font TagDocumentNodes.
 * @author Paul Calcraft
 */
public class FontTagRenderer extends TagRenderer {
    public FontTagRenderer(Linkable linker) {
        super(linker);
    }

    /**
     * Attempts to find a colour by name from the java Color constants.
     * @param colourName
     * @return the Color if found, otherwise null
     */
    private Color getNamedColour(String colourName) {
        try {
            Field field = Color.class.getField(colourName.toLowerCase());
            return (Color)field.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        Map<Attribute, Object> newFormatting = (Map<Attribute, Object>)((HashMap)formatting).clone();
        String color = tag.getAttribute("color");
        String face = tag.getAttribute("face");

        // Determine if the 'color' attribute is set and valid
        if (color != null) {
            Color colour;
            if (color.charAt(0) == '#') {
                // Hex colour
                try {
                    int colourValue = Integer.parseInt(color.substring(1), 16);
                    if (colourValue > 0xFFFFFF || colourValue < 0x0)
                        colour = null;
                    else
                        colour = new Color(colourValue);
                } catch (NumberFormatException ex) {
                    colour = null;
                }
            }
            else
                colour = getNamedColour(color);

            if (colour != null) {
                newFormatting.put(TextAttribute.FOREGROUND, colour);
            }
            else {
                // Attribute 'color' is set but can't be parsed - bad attribute.
                BrowserMonkeyLogger.conformance("Invalid color attribute value \""+color+"\" in font tag.");
                renderer.foundConformanceError();
            }
        }

        // Determine if the face value is set and valid.
        if (face != null) {
            Font font = Font.decode(face);
            String fontFamilyName = font.getFamily();
            if (fontFamilyName.equalsIgnoreCase(face))
                newFormatting.put(TextAttribute.FAMILY, fontFamilyName);
            else {
                // Attribute 'font' is set but does not match a font on the system.
                BrowserMonkeyLogger.conformance("Invalid font face attribute value \""+face+"\" in font tag.");
                renderer.foundConformanceError();
            }
        }

        // Render all children into the same parent using the Renderer and the
        // appropriate formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, parent, newFormatting);
    }
}