/**
 * ColorManager.java 
 * Copyright (C) 2015 Daniel H. Huson
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
package jloda.gui;

import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * default chart colors, based on hash code of label
 * Daniel Huson, 5.2012
 */
public class ColorManager {
    public static final String SAMPLE_ID = "#SampleID";

    private final Map<Integer, Color> colors = new HashMap<>(); // cache colors
    private final Map<String, Color> class2color = new HashMap<>(); // cache changes
    private final Map<String, Color> attribute2color = new HashMap<>(); // cache changes

    private ColorGetter seriesOverrideColorGetter = null;

    /**
     * get color for data set
     *
     * @param sample
     * @return color
     */
    public Color getSamplesColor(String sample) {
        Color color = null;
        if (seriesOverrideColorGetter != null)
            color = seriesOverrideColorGetter.get(sample);
        if (color == null)
            color = getAttributeColor(SAMPLE_ID, sample);
        if (color == null) {
            if (sample == null || sample.equals("GRAY"))
                color = Color.GRAY;
            else {
                int key = sample.hashCode();
                color = colors.get(key);
                if (color == null) {
                    color = PaletteManager.get(key);
                    colors.put(key, color);
                }
            }
        }
        return color;
    }

    /**
     * get color for data set
     *
     * @param sample
     * @return color
     */
    public Color getSamplesColor(String sample, int alpha) {
        Color color = null;
        if (seriesOverrideColorGetter != null)
            color = seriesOverrideColorGetter.get(sample);
        if (color == null)
            color = getAttributeColor("#SampleID", sample);
        if (color == null) {
            if (sample == null || sample.equals("GRAY")) {
                color = Color.GRAY;
            } else {
                int key = sample.hashCode();
                color = colors.get(key);
                if (color == null) {
                    color = PaletteManager.get(key, alpha);
                    colors.put(key, color);
                }
            }
        }
        if (color.getAlpha() == alpha)
            return color;
        else
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }


    /**
     * set the color of a series
     *
     * @param sample
     * @param color
     */
    public void setSampleColor(String sample, Color color) {
        setAttributeColor(SAMPLE_ID, sample, color);
    }

    /**
     * get the color fo a specific chart and class
     *
     * @param className
     * @return color
     */
    public Color getClassColor(String className) {
        //  if(!(className.equals("Pathogenic in:No") || className.equals("Pathogenic in:Human") || className.equals("Pathogenic in:Human, Animals")))
        //      return new Color(150,150,150);

        Color color = class2color.get(className);
        if (color == null && className != null) {
            if (className.equals("GRAY"))
                color = Color.GRAY;
            else {
                int key = className.hashCode();
                color = colors.get(key);
                if (color == null) {
                    color = PaletteManager.get(key);
                    colors.put(key, color);
                }
            }
        }
        return color;
    }

    /**
     * get the color fo a specific chart and class
     *
     * @param className
     * @return color
     */
    public Color getClassColor(String className, int alpha) {
        //  if(!(className.equals("Pathogenic in:No") || className.equals("Pathogenic in:Human") || className.equals("Pathogenic in:Human, Animals")))
        //      return new Color(150,150,150,alpha);

        Color color = class2color.get(className);
        if (color == null) {
            if (className.equals("GRAY"))
                color = Color.GRAY;
            else {
                int key = className.hashCode();
                color = colors.get(key);
                if (color == null) {
                    color = PaletteManager.get(key);
                    colors.put(key, color);
                }
            }
        }
        if (color.getAlpha() == alpha)
            return color;
        else
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }


    /**
     * set the color for a class
     *
     * @param className
     * @param color
     */
    public void setClassColor(String className, Color color) {
        class2color.put(className, color);
    }

    /**
     * get the color fo a specific chart and attribute
     *
     * @param attributeState
     * @return color
     */
    public Color getAttributeColor(String attributeState) {
        Color color = attribute2color.get(attributeState);
        if (color == null) {
            if (attributeState.equals("GRAY"))
                color = Color.GRAY;
            else {
                int key = attributeState.hashCode();
                color = colors.get(key);
                if (color == null) {
                    color = PaletteManager.get(key);
                    colors.put(key, color);
                }
            }
        }
        return color;
    }

    /**
     * get the color fo a specific chart and attribute
     *
     * @param attributeName
     * @return color
     */
    public Color getAttributeColor(String attributeName, String state) {
        return getAttributeColor(attributeName + "::" + state);
    }

    /**
     * get the color fo a specific chart and attribute
     *
     * @param attributeState
     * @return color
     */
    public Color getAttributeColor(String attributeState, int alpha) {
        Color color = attribute2color.get(attributeState);
        if (color == null) {
            if (attributeState.equals("GRAY"))
                color = Color.GRAY;
            else {
                int key = attributeState.hashCode();
                color = colors.get(key);
                if (color == null) {
                    color = PaletteManager.get(key);
                    colors.put(key, color);
                }
            }
        }
        if (color.getAlpha() == alpha)
            return color;
        else
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * get the color fo a specific chart and attribute
     *
     * @param attributeName
     * @return color
     */
    public Color getAttributeColor(String attributeName, String state, int alpha) {
        return getAttributeColor(attributeName + "::" + state, alpha);
    }


    /**
     * set the color for a attribute
     *
     * @param attributeName
     * @param color
     */
    public void setAttributeColor(String attributeName, String state, Color color) {
        setAttributeColor(attributeName + "::" + state, color);
    }

    /**
     * set the color for a attribute
     *
     * @param attributeAndState attribute name and state separated by ::
     * @param color
     */
    public void setAttributeColor(String attributeAndState, Color color) {
        attribute2color.put(attributeAndState, color);
    }

    /**
     * load or reload colors from properties
     */
    public void loadFromProperties() {
        String colorString = ProgramProperties.get("ColorMap", "");
        if (colorString.length() > 0) {
            String[] lines = colorString.split(";");
            try {
                read(lines);
            } catch (IOException e) {
            }
        }
    }

    /**
     * load or reload colors
     */
    public void saveToProperties() {
        StringWriter w = new StringWriter();
        try {
            write(w, ";");
            ProgramProperties.put("ColorMap", w.toString());
            w.close();
        } catch (IOException e) {
        }
    }

    /**
     * write color table
     *
     * @param w
     * @throws java.io.IOException
     */
    public void write(Writer w) throws IOException {
        write(w, "\n");
    }

    /**
     * write color table
     *
     * @param w
     * @throws java.io.IOException
     */
    public void write(Writer w, String separator) throws IOException {
        for (Map.Entry<String, Color> entry : class2color.entrySet()) {
            Color color = entry.getValue();
            if (color != null)
                w.write("C" + "\t" + entry.getKey() + "\t" + color.getRGB() + (color.getAlpha() < 255 ? "\t" + color.getAlpha() : "") + separator);
        }
        for (Map.Entry<String, Color> entry : attribute2color.entrySet()) {
            Color color = entry.getValue();
            if (color != null)
                w.write("A" + "\t" + entry.getKey() + "\t" + color.getRGB() + (color.getAlpha() < 255 ? "\t" + color.getAlpha() : "") + separator);
        }
    }

    /**
     * read color table
     *
     * @param r0
     * @throws IOException
     */
    public void read(Reader r0) throws IOException {
        String[] tokens = Basic.getLines(r0);
        read(tokens);
    }

    /**
     * read color table
     *
     * @param lines
     * @throws IOException
     */
    public void read(String[] lines) throws IOException {
        for (String aLine : lines) {
            aLine = aLine.trim();
            if (aLine.length() > 0 && !aLine.startsWith("#")) {
                String[] tokens = aLine.split("\t");
                if (tokens.length >= 3 && Basic.isInteger(tokens[2])) {
                    switch (tokens[0]) {
                        case "C": {
                            String className = tokens[1];
                            Color color = new Color(Integer.parseInt(tokens[2]));
                            if (tokens.length >= 4) {
                                int alpha = Integer.parseInt(tokens[3]);
                                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                            }
                            class2color.put(className, color);
                            break;
                        }
                        case "A": {
                            String attribute = tokens[1];
                            Color color = new Color(Integer.parseInt(tokens[2]));
                            if (tokens.length >= 4) {
                                int alpha = Integer.parseInt(tokens[3]);
                                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                            }
                            attribute2color.put(attribute, color);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * gets a series color getter that overrides MEGAN-wide colors
     *
     * @return color getter
     */
    public ColorGetter getSeriesOverrideColorGetter() {
        return seriesOverrideColorGetter;
    }

    /**
     * sets a series color getter that overrides MEGAN-wide colors. Is used to implement document-specific colors
     *
     * @param seriesOverrideColorGetter
     */
    public void setSeriesOverrideColorGetter(ColorGetter seriesOverrideColorGetter) {
        this.seriesOverrideColorGetter = seriesOverrideColorGetter;
    }

    public ColorGetter getSeriesColorGetter() {
        return new ColorGetter() {
            public Color get(String label) {
                return getSamplesColor(label);
            }
        };
    }

    public ColorGetter getClassColorGetter() {
        return new ColorGetter() {
            public Color get(String label) {
                return getClassColor(label);
            }
        };
    }

    public interface ColorGetter {
        Color get(String label);
    }
}
