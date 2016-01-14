/**
 * TextPrinter.java 
 * Copyright (C) 2016 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package jloda.util;

import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Daniel Huson, Michael Schr�der
 * @version $Id: TextPrinter.java,v 1.1 2005-12-09 15:51:18 huson Exp $
 */
public class TextPrinter implements Printable {

    // Constants for font name, size, style and line spacing
    public static final float LINESPACEFACTOR = 1.1f;
    Vector lines;        // The text to be printed, broken into lines
    final Font font;           // The font to print with
    float linespacing;   // How much space between lines
    int linesPerPage;    // How many lines fit on a page
    int numPages = 1;    // How many pages required to print all lines
    int baseline = -1;   // The baseline position of the font
    final String text;         // the text to be printed

    /**
     * Constructs a TextPrinter
     *
     * @param text the text to be printed
     * @param font the font to print with
     */
    public TextPrinter(String text, Font font) {
        this.text = text;
        this.font = font;
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        Graphics2D g = (Graphics2D) graphics;
        g.setColor(Color.black);

        if (baseline == -1) { // init printing
            FontMetrics fm = g.getFontMetrics(font);
            baseline = fm.getAscent();
            linespacing = LINESPACEFACTOR * fm.getHeight();
            linesPerPage = (int) Math.floor(pageFormat.getImageableHeight() / linespacing);
            lines = new Vector();
            BufferedReader buf = new BufferedReader(new StringReader(text));

            float wrapWidth = (float) pageFormat.getImageableWidth();

            Map textAttributes = new HashMap();
            textAttributes.put(TextAttribute.FONT, font);
            textAttributes.put(TextAttribute.SIZE, font.getSize2D());
            textAttributes.put(TextAttribute.FOREGROUND, Color.black);

            String line;
            try {
                while ((line = buf.readLine()) != null) {

                    if (line.length() > 0) {
                        AttributedString styledText = new AttributedString(line, textAttributes);
                        AttributedCharacterIterator charIt = styledText.getIterator();
                        LineBreakMeasurer measurer = new LineBreakMeasurer(charIt, g.getFontRenderContext());
                        while (measurer.getPosition() < charIt.getEndIndex()) {
                            TextLayout layout = measurer.nextLayout(wrapWidth);
                            lines.add(layout);
                        }
                    }
                }
            } catch (IOException e) {
                Basic.caught(e);
            }

            numPages = lines.size() / linesPerPage;

        } // end init printing

        if (pageIndex > numPages) {
            return NO_SUCH_PAGE;
        } else {
            int startLine = pageIndex * linesPerPage;
            int endLine = startLine + linesPerPage - 1;
            if (endLine >= lines.size())
                endLine = lines.size() - 1;

            float x0 = (float) pageFormat.getImageableX();
            float y0 = (float) pageFormat.getImageableY() + baseline;

            for (int i = startLine; i <= endLine; i++) {

                TextLayout line = (TextLayout) lines.elementAt(i);
                line.draw(g, x0, y0);

                y0 += linespacing;
            }

            return PAGE_EXISTS;
        }

    }

}
