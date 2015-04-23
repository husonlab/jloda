/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    private final Map<Integer, Color> colors = new HashMap<Integer, Color>(); // cache colors
    private final Map<String, Color> series2color = new HashMap<String, Color>(); // cache changes
    private final Map<String, Color> class2color = new HashMap<String, Color>(); // cache changes
    private final Map<String, Color> attribute2color = new HashMap<String, Color>(); // cache changes

    private ColorGetter seriesOverrideColorGetter = null;

    /**
     * get color for data set
     *
     * @param series
     * @return color
     */
    public Color getSeriesColor(String series) {
        Color color = null;
        if (seriesOverrideColorGetter != null)
            color = seriesOverrideColorGetter.get(series);
        if (color == null)
            color = series2color.get(series);
        if (color == null) {
            if (series == null || series.equals("GRAY"))
                color = Color.GRAY;
            else {
                int key = series.hashCode();
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
     * @param series
     * @return color
     */
    public Color getSeriesColor(String series, int alpha) {
        Color color = null;
        if (seriesOverrideColorGetter != null)
            color = seriesOverrideColorGetter.get(series);
        if (color == null)
            color = series2color.get(series);
        if (color == null) {
            if (series == null || series.equals("GRAY")) {
                color = Color.GRAY;
            } else {
                int key = series.hashCode();
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
     * @param series
     * @param color
     */
    public void setSeriesColor(String series, Color color) {
        series2color.put(series, color);
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

        for (Map.Entry<String, Color> entry : series2color.entrySet()) {
            Color color = entry.getValue();
            if (color != null)
                w.write("S" + "\t" + entry.getKey() + "\t" + color.getRGB() + (color.getAlpha() < 255 ? "\t" + color.getAlpha() : "") + separator);
        }
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
                    if (tokens[0].equals("S")) {
                        String series = tokens[1];
                        Color color = new Color(Integer.parseInt(tokens[2]));
                        if (tokens.length >= 4) {
                            int alpha = Integer.parseInt(tokens[3]);
                            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                        }
                        series2color.put(series, color);

                    } else if (tokens[0].equals("C")) {
                        String className = tokens[1];
                        Color color = new Color(Integer.parseInt(tokens[2]));
                        if (tokens.length >= 4) {
                            int alpha = Integer.parseInt(tokens[3]);
                            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                        }
                        class2color.put(className, color);
                    } else if (tokens[0].equals("A")) {
                        String attribute = tokens[1];
                        Color color = new Color(Integer.parseInt(tokens[2]));
                        if (tokens.length >= 4) {
                            int alpha = Integer.parseInt(tokens[3]);
                            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                        }
                        attribute2color.put(attribute, color);
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
                return getSeriesColor(label);
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
        public Color get(String label);
    }
}
