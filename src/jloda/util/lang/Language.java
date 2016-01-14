/**
 * Language.java 
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
package jloda.util.lang;

import jloda.util.Alert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * base class for language support
 * Daniel Huson, 1.2009
 */
public class Language {
    protected final Map map;

    /**
     * constructor
     */
    public Language() {
        map = new HashMap();
        init();

    }

    /**
     * gets the map
     *
     * @return map
     */
    public Map getMap() {
        return map;
    }

    /**
     * initializes the translation
     */
    protected void init() {
    }

    /**
     * load translations from a file.
     * Format: each line contains a pair of the form English:Translation
     *
     * @param file
     * @throws java.io.IOException
     */
    public void load(File file) {
        try {
            System.err.println("Loading file: " + file);
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String aLine;
            while ((aLine = reader.readLine()) != null) {
                if (aLine.length() > 0 && !aLine.startsWith("#")) {
                    StringTokenizer st = new StringTokenizer(aLine, ":");
                    if (st.countTokens() == 2) {
                        map.put(st.nextToken(), st.nextToken());
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            new Alert("Warning: Language file not found: " + file);
        }
    }

    /**
     * set a pair of original and translated strings
     *
     * @param original
     * @param translated
     */
    public void put(String original, String translated) {
        map.put(original, translated);
    }
}
