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
