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

package jloda.util;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * track program properties
 *
 * @author huson
 *         Date: 08-Nov-2004
 */
public class ProgramProperties {
    static public final java.util.Properties props = new java.util.Properties();
    static private String defaultFileName = null;
    static private String programName = "";
    static private String programVersion = "";
    static private String programTitle = "";
    private static ImageIcon programIcon = null;
    private static final boolean macOS = (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().startsWith("mac"));
    private static boolean useGUI = false;

    public static final String OPENFILE = "OpenFile";
    public static final String SAVEFILE = "SaveFile";
    public static final String FINDFILE = "FindFile";
    public static final String SAVEFORMAT = "SaveFormat";
    public static final String EXPORTFILE = "ExportFile";
    public static final String RECENTFILES = "RecentFiles";
    public static final String MAXRECENTFILES = "MaxRecentFiles";
    public static final String TOOLBARITEMS = "ToolbarItems";
    public static final String SHOWTOOLBAR = "ShowToolbar";
    public static final String EVOLVERTOOLBARITEMS = "EvolverToolbarItems";
    public static final String SHOWEVOLVERTOOLBAR = "ShowEvolverToolbar";
    public static final String SHOWVERSIONINTITLE = "VINT";
    public static final String DRAWERKIND = "DrawerKind";
    public static final String LASTCOMMAND = "LastCommand";
    public static final String FINDSTRING = "FindString";
    public static final String MAIN_WINDOW_GEOMETRY = "MainWindowGeometry";
    public static final String MULTI_WINDOW_GEOMETRY = "MultiWindowGeometry";
    public static PageFormat pageFormat = null;
    public static final String DEFAULT_FONT = "DefaultFont";

    /**
     * load properties from default file
     */
    public static void load() {
        /* TODO: search for file in path */
        try {
            FileInputStream fis = new FileInputStream(getDefaultFileName());
            props.load(fis);
            fis.close();
            //System.err.println("Loaded properties from: " + getDefaultFileName());
        } catch (Exception ex) {
            //Basic.caught(ex);
        }
    }

    /**
     * load properties from specified file
     *
     * @param fileName
     */
    public static void load(String fileName) {
        setPropertiesFileName(fileName);
        load();
    }

    /**
     * save properties to default file
     */
    public static void store() {
        try {
            OutputStream fos = new FileOutputStream(getDefaultFileName());
            props.store(fos, programName);
            fos.close();
            //System.err.println("Stored properties to: " + getDefaultFileName());
        } catch (Exception ex) {
            //Basic.caught(ex);
        }
    }

    /**
     * save properties to specified file
     *
     * @param fileName
     */
    public static void store(String fileName) {
        setPropertiesFileName(fileName);
        store();
    }

    /**
     * gets a int property
     *
     * @param name
     * @param def
     * @return set property or default
     */
    public static int get(Object name, int def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else
            return Integer.parseInt(value);
    }

    /**
     * gets a int[] property
     *
     * @param name
     * @param def
     * @return set property or default
     */
    public static int[] get(Object name, int[] def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            try {
                java.util.List<String> list = new LinkedList<>();
                StringTokenizer tok = new StringTokenizer(value, ";");
                while (tok.hasMoreTokens())
                    list.add(tok.nextToken());
                int[] result = new int[list.size()];
                int i = 0;
                for (String s : list) {
                    result[i++] = Integer.parseInt(s);
                }
                if (def.length > 0 && result.length != def.length)
                    return def;
                else
                    return result;
            } catch (Exception ex) {
                return def;
            }
        }
    }

    /**
     * gets a color property
     *
     * @param name
     * @param def
     * @return set property or default
     */
    public static Color get(Object name, Color def) {
        String value = (String) props.get(name);
        if (value == null || value.equalsIgnoreCase("null"))
            return def;
        else
            return Color.decode(value);
    }


    /**
     * gets a double property
     *
     * @param name
     * @param def
     * @return set property or default
     */
    public static double get(Object name, double def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else
            return Double.parseDouble(value);
    }

    /**
     * gets a boolean property
     *
     * @param name
     * @param def
     * @return set property or default
     */
    public static boolean get(Object name, boolean def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else
            return Boolean.valueOf(value);
    }


    /**
     * gets a string property
     *
     * @param name
     * @param def
     * @return set property or default
     */
    public static String get(String name, String def) {
        return props.getProperty(name, def);
    }

    /**
     * gets a font property
     *
     * @param name
     * @param def
     * @return font or default
     */
    public static Font get(String name, Font def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            value = value.replaceAll(" ", "\\ ");
            return Font.decode(value);
        }
    }

    /**
     * gets a list of string pairs
     *
     * @param name
     * @param def
     * @return list of string pairs
     */
    public static Collection<Pair<String, String>> get(String name, Collection<Pair<String, String>> def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            Collection<Pair<String, String>> list = new LinkedList<>();
            String[] tokens = value.split("%%%");
            for (int i = 0; i < tokens.length - 1; i += 2)
                list.add(new Pair<>(tokens[i].trim(), tokens[i + 1].trim()));
            return list;
        }
    }

    /**
     * gets a list of strings
     *
     * @param name
     * @param def
     * @return list of string pairs
     */
    public static String[] get(String name, String[] def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            Collection<String> list = new LinkedList<>();
            String[] tokens = value.split("%%%");
            for (String token : tokens) list.add(token.trim());
            return list.toArray(new String[list.size()]);
        }
    }

    /**
     * get the default properties file name
     *
     * @return file name
     */
    public static String getDefaultFileName() {
        return defaultFileName;
    }

    /**
     * set the default properties file name
     *
     * @param defaultFileName
     */
    public static void setPropertiesFileName(String defaultFileName) {
        ProgramProperties.defaultFileName = defaultFileName;
    }

    public static File getFile(String key) {
        String fileName = props.getProperty(key);
        if (fileName != null)
            return new File(fileName);
        return null;
    }

    /**
     * remove a property
     *
     * @param key
     */
    public static void remove(String key) {
        props.remove(key);
    }

    /**
     * put a property
     *
     * @param key
     * @param value
     */
    public static void put(String key, int value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     *
     * @param key
     * @param value
     */
    public static void put(String key, int[] value) {
        StringBuilder buf = new StringBuilder();
        for (int aValue : value) buf.append(aValue).append(";");
        props.setProperty(key, "" + buf.toString());
    }

    /**
     * put a property
     *
     * @param key
     * @param value
     */
    public static void put(String key, double value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     *
     * @param key
     * @param value
     */
    public static void put(String key, boolean value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     *
     * @param key
     * @param value
     */
    public static void put(String key, String value) {
        props.setProperty(key, value);
    }

    /**
     * put a file property
     *
     * @param key
     * @param value
     */
    public static void put(String key, File value) {
        props.setProperty(key, value.getAbsolutePath());
    }


    /**
     * put a property
     *
     * @param key
     * @param value
     */
    public static void put(String key, Color value) {
        if (value == null)
            props.setProperty(key, "null");
        else
            props.setProperty(key, "" + value.getRGB());
    }


    /**
     * put a property
     *
     * @param key
     * @param value
     */
    public static void put(String key, Font value) {
        put(key, value.getFamily(), value.getStyle(), value.getSize());
    }

    /**
     * put a property
     *
     * @param key
     */
    public static void put(String key, String family, Integer style0, Integer size0) {
        Font def = get(key, (Font) null);
        String name;
        if (family == null)
            name = def.getFamily();
        else
            name = family;
        int style;
        if (style0 == null)
            style = def.getStyle();
        else
            style = style0;
        int size;
        if (size0 == null)
            size = def.getSize();
        else
            size = size0;

        switch (style) {
            case Font.BOLD + Font.ITALIC:
                name += "-BOLDITALIC";
                break;
            case Font.BOLD:
                name += "-BOLD";
                break;
            case Font.ITALIC:
                name += "-ITALIC";
                break;
            default:
            case Font.PLAIN:
                name += "-PLAIN";
                break;
        }
        name += "-" + size;
        props.setProperty(key, name);
    }

    /**
     * put a property
     *
     * @param key
     * @param value
     */
    public static void put(String key, Collection<Pair<String, String>> value) {
        StringBuilder buf = new StringBuilder();
        for (Pair<String, String> pair : value) {
            buf.append(pair.getFirst()).append("%%%");
            buf.append(pair.getSecond()).append("%%%");
        }
        props.setProperty(key, buf.toString());
    }

    /**
     * put a property
     *
     * @param key
     * @param value
     */
    public static void put(String key, String[] value) {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (String s : value) {
            if (first)
                first = false;
            else
                buf.append("%%%");
            buf.append(s);
        }
        props.setProperty(key, buf.toString());
    }


    /**
     * get a property
     *
     * @param key
     * @return property for key
     */
    public static String get(String key) {
        return props.getProperty(key);
    }

    /**
     * sets the name of the program generating these properties
     *
     * @param programName
     */
    public static void setProgramName(String programName) {
        ProgramProperties.programName = programName;
    }

    /**
     * gets the program name
     *
     * @return name
     */
    public static String getProgramName() {
        return programName;
    }

    /**
     * sets the program version string
     *
     * @param version
     */
    public static void setProgramVersion(String version) {
        ProgramProperties.programVersion = version;
    }

    /**
     * gets the program versions string
     *
     * @return version
     */
    public static String getProgramVersion() {
        return programVersion;
    }

    /**
     * sets the program title string
     *
     * @param title
     */
    public static void setProgramTitle(String title) {
        ProgramProperties.programTitle = title;
    }

    /**
     * gets the program titles string
     *
     * @return title
     */
    public static String getProgramTitle() {
        return programTitle;
    }

    /**
     * are we running on a mac?
     *
     * @return true, if os is mac
     */
    public static boolean isMacOS() {
        return macOS;
    }

    /**
     * gets the program icon
     *
     * @return program icon
     */
    public static ImageIcon getProgramIcon() {
        return programIcon;
    }

    /**
     * sets the program icon
     *
     * @param icon
     */
    public static void setProgramIcon(ImageIcon icon) {
        ProgramProperties.programIcon = icon;
    }

    public static PageFormat getPageFormat() {
        return pageFormat;
    }

    public static void setPageFormat(PageFormat pageFormat) {
        ProgramProperties.pageFormat = pageFormat;
    }

    /**
     * returns the given text, if the key has been set, otherwise returns ""
     *
     * @param key
     * @param text
     * @return text of ""
     */
    public static String getIfEnabled(String key, String text) {
        if (get(key, false))
            return text;
        else
            return "";
    }

    public static boolean isUseGUI() {
        return useGUI;
    }

    public static void setUseGUI(boolean useGUI) {
        ProgramProperties.useGUI = useGUI;
    }
}
