/*
 * ProgramProperties.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

/**
 * track program properties
 *
 * @author huson
 *         Date: 08-Nov-2004
 */
public class ProgramProperties {
    static private final java.util.Properties props = new java.util.Properties();

    static private final ObservableList<Image> programIconsFX = FXCollections.observableArrayList();
    static private javafx.scene.text.Font defaultFontFX = javafx.scene.text.Font.font("Arial", 12);
    private static final ArrayList<ImageIcon> programIcons = new ArrayList<>();

    public static Color SELECTION_COLOR = new Color(252, 208, 102);
    public static Color SELECTION_COLOR_DARKER = new Color(210, 190, 95);
    public static Color SELECTION_COLOR_ADDITIONAL_TEXT = new Color(93, 155, 206);

    static private String programName = "";
    static private String programVersion = "";
    static private String programTitle = "";
    static private String programLicence = "";

    static private String defaultFileName = null;


    private static final boolean macOS = (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().startsWith("mac"));
    private static boolean useGUI = false;
    private static IStateChecker stateChecker = null;
    public static final String OPENFILE = "OpenFile";
    public static final String SAVEFILE = "SaveFile";
    public static final String SAVEFORMAT = "SaveFormat";
    public static final String EXPORTFILE = "ExportFile";
    public static final String RECENTFILES = "RecentFiles";
    public static final String MAXRECENTFILES = "MaxRecentFiles";
    public static final String SHOWVERSIONINTITLE = "VINT";
    public static final String DRAWERKIND = "DrawerKind";
    public static final String LASTCOMMAND = "LastCommand";
    public static final String MAIN_WINDOW_GEOMETRY = "MainWindowGeometry";
    public static final String MULTI_WINDOW_GEOMETRY = "MultiWindowGeometry";
    public static PageFormat pageFormat = null;
    public static final String DEFAULT_FONT = "DefaultFont";
    public static final String SEARCH_URL = "SearchURL";
    public static final String defaultSearchURL = "http://www.google.com/search?q=%s";


    /**
     * load properties from default file
     */
    public static void load() {
        try (FileInputStream fis = new FileInputStream(getDefaultFileName())) {
            props.load(fis);
            //System.err.println("Loaded properties from: " + getDefaultFileName());
        } catch (Exception ex) {
            //Basic.caught(ex);
        }
    }

    /**
     * load properties from specified file
     */
    public static void load(String fileName) {
        setPropertiesFileName(fileName);
        load();
    }

    /**
     * save properties to default file
     */
    public static void store() {
            try (OutputStream fos = new FileOutputStream(getDefaultFileName())) {
                props.store(fos, programName);
            //System.err.println("Stored properties to: " + getDefaultFileName());
        } catch (Exception ex) {
            //Basic.caught(ex);
        }
    }

    /**
     * save properties to specified file
     */
    public static void store(String fileName) {
        setPropertiesFileName(fileName);
        store();
    }

    /**
     * gets a int property
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
     * @return set property or default
     */
    public static int[] get(Object name, int[] def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            try {
                final String[] tokens = value.split(value.contains(";") ? ";" : "\\s+");
                final int[] result = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++)
                    result[i] = Integer.parseInt(tokens[i]);
                return result;
            } catch (Exception ex) {
                return def;
            }
        }
    }

    /**
     * gets a color property
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
     * put a property
     */
    public static void put(String key, Color value) {
        if (value == null)
            props.setProperty(key, "null");
        else
            props.setProperty(key, "" + value.getRGB());
    }

    /**
     * gets a double property
     *
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
     * @return set property or default
     */
    public static String get(String name, String def) {
        return props.getProperty(name, def);
    }

    /**
     * gets a font property
     *
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
     * @return list of string pairs
     */
    public static Collection<Pair<String, String>> get(String name, Collection<Pair<String, String>> def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            final Collection<Pair<String, String>> list = new LinkedList<>();
            String[] tokens = value.split("%%%");
            for (int i = 0; i < tokens.length - 1; i += 2)
                list.add(new Pair<>(tokens[i].trim(), tokens[i + 1].trim()));
            return list;
        }
    }

    /**
     * gets a list of strings
     *
     * @return list of string pairs
     */
    public static String[] get(String name, String[] def) {
        String value = (String) props.get(name);
        if (value == null)
            return def;
        else {
            return value.split("%%%");
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
     */
    public static void remove(String key) {
        props.remove(key);
    }

    /**
     * put a property
     *
     */
    public static void put(String key, int value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     *
     */
    public static void put(String key, int[] value) {
        StringBuilder buf = new StringBuilder();
        for (int aValue : value) buf.append(aValue).append(";");
        props.setProperty(key, "" + buf.toString());
    }

    /**
     * put a property
     *
     */
    public static void put(String key, double value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     *
     */
    public static void put(String key, boolean value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     *
     */
    public static void put(String key, String value) {
        if (value != null)
            props.setProperty(key, value);
        else
            props.remove(key);
    }

    /**
     * put a file property
     *
     */
    public static void put(String key, File value) {
        props.setProperty(key, value.getAbsolutePath());
    }


    /**
     * put a property
     *
     */
    public static void put(String key, Font value) {
        put(key, value.getFamily(), value.getStyle(), value.getSize());
    }

    /**
     * put a property
     */
    public static void put(String key, String family, Integer style0, Integer size0) {
        Font def = get(key, (Font) null);
        String name;
        if (family == null)
            name = def.getFamily();
        else
            name = family;
        int style;
        style = Objects.requireNonNullElseGet(style0, def::getStyle);
        int size;
        size = Objects.requireNonNullElseGet(size0, def::getSize);

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
     * @return property for key
     */
    public static String get(String key) {
        return props.getProperty(key);
    }

    /**
     * sets the name of the program generating these properties
     *
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
     * sets the program version string, if not already set...
     *
     */
    public static void setProgramVersion(String version) {
        if (programVersion == null || programVersion.length() == 0)
            ProgramProperties.programVersion = version;
    }

    public static void resetProgramVersion(String version) {
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
     */
    public static void setProgramTitle(String title) {
        ProgramProperties.programTitle = title;
    }

    /**
     * gets the program titles string
     *
     */
    public static String getProgramTitle() {
        return programTitle;
    }

    public static String getProgramLicence() {
        return programLicence;
    }

    public static void setProgramLicence(String programLicence) {
        ProgramProperties.programLicence = programLicence;
    }

    /**
     * are we running on a mac?
     *
     * @return true, if os is mac
     */
    public static boolean isMacOS() {
        return macOS;
    }

    public static PageFormat getPageFormat() {
        return pageFormat;
    }

    public static ArrayList<ImageIcon> getProgramIcons() {
        return programIcons;
    }

    public static void setProgramIcons(Collection<ImageIcon> icons) {
        programIcons.clear();
        for (ImageIcon icon : icons) {
            if (icon != null)
                programIcons.add(icon);
        }
    }

    public static ArrayList<java.awt.Image> getProgramIconImages() {
        final ArrayList<java.awt.Image> images = new ArrayList<>();
        for (ImageIcon icon : getProgramIcons()) {
            images.add(icon.getImage());
        }
        return images;
    }

    /**
     * gets the program icon
     *
     * @return program icon
     */
    public static ImageIcon getProgramIcon() {
        ImageIcon result = null;
        for (ImageIcon imageIcon : getProgramIcons()) {
            if (result == null || imageIcon.getIconHeight() < 128 && imageIcon.getIconHeight() > result.getIconHeight()) // 64 preferred
                result = imageIcon;
        }
        return result;
    }

    public static void setPageFormat(PageFormat pageFormat) {
        ProgramProperties.pageFormat = pageFormat;
    }

    /**
     * returns the given text, if the key has been set, otherwise returns ""
     *
     * @return text or ""
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

    public static void checkState() {
        if (stateChecker != null)
            stateChecker.check();
    }

    public static void setStateChecker(IStateChecker stateChecker) {
        ProgramProperties.stateChecker = stateChecker;
    }

    public static ObservableList<javafx.scene.image.Image> getProgramIconsFX() {
        return programIconsFX;
    }


    public static javafx.scene.paint.Color get(Object name, javafx.scene.paint.Color defaultColorFX) {
        String value = (String) props.get(name);
        if (value == null || value.equalsIgnoreCase("null"))
            return defaultColorFX;
        else
            return javafx.scene.paint.Color.valueOf(value);
    }

    public static void put(String key, javafx.scene.paint.Color colorFX) {
        if (colorFX == null)
            props.setProperty(key, "null");
        else
            props.setProperty(key, "" + colorFX.toString());
    }

    public static javafx.scene.text.Font getDefaultFontFX() {
        return defaultFontFX;
    }

    public static void setDefaultFontFX(javafx.scene.text.Font defaultFontFX) {
        ProgramProperties.defaultFontFX = defaultFontFX;
    }

}
