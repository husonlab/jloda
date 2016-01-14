/**
 * Translator.java 
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * translate all english text into another language
 * Daniel Huson, 11.2008
 */
public class Translator {
    private final Map map = new HashMap();
    private static Translator instance;
    private boolean doTranslation = false;

    private File logFile;
    private final Set unresolved = new HashSet();

    /**
     * get the one instance of the translator
     *
     * @return translator instance
     */
    public static Translator getInstance() {
        if (instance == null)
            instance = new Translator();
        return instance;
    }

    /**
     * load translation from a language object
     *
     * @param language
     */
    public void load(Language language) {
        Map lmap = language.getMap();
        for (Object obj : lmap.keySet()) {
            if (lmap.get(obj) != null)
                map.put(obj, lmap.get(obj));
        }
    }

    /**
     * get the translation for a string, if translation is on
     *
     * @param original
     * @param log      log missing translations?
     * @return translation
     */
    public String getTranslation(String original, boolean log) {
        if (doTranslation) {
            String translated = (String) map.get(original);
            if (translated == null) {
                if (!unresolved.contains(original) && log) {
                    unresolved.add(original);
                }
                return original;
            } else
                return translated.trim();
        } else
            return original;
    }

    /**
     * translate a string
     *
     * @param original
     * @return translation
     */
    public static String get(String original) {
        return getInstance().getTranslation(original, true);
    }

    /**
     * translate a string
     *
     * @param original
     * @param log      log missing translations?
     * @return translation
     */
    public static String get(String original, boolean log) {
        return getInstance().getTranslation(original, log);
    }

    /**
     * is translation on?
     *
     * @return true, if translation on
     */
    public boolean isDoTranslation() {
        return doTranslation;
    }

    /**
     * do translation?
     *
     * @param doTranslation
     */
    public void setDoTranslation(boolean doTranslation) {
        this.doTranslation = doTranslation;
    }

    /**
     * dump all defined and undefined translations to a file
     *
     * @param file
     * @throws IOException
     */
    public void dumpToFile(File file) throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(file));

        for (Object o : map.keySet()) {
            String label = (String) o;
            w.write("\tput(\"" + label + "\",\"" + map.get(label) + "\");\n");
        }
        w.write("\t// Unresolved:\n");
        for (Object anUnresolved : unresolved) {
            String label = (String) anUnresolved;
            w.write("\tput(\"" + label + "\",null);\n");
        }
        w.flush();
        w.close();
    }
}
