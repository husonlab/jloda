/*
 * ProgramProperties.java Copyright (C) 2022 Daniel H. Huson
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Basic program properties
 * <p>
 * Daniel Huson, 2004, 2022
 */
public class ProgramProperties {
    static protected final java.util.Properties props = new java.util.Properties();

    private static final Map<String, Object> presets = new HashMap<>();

	static private String programName = "";
	static private String programVersion = "";
	static private String programTitle = "";
	static private String programLicence = "";

	static private String defaultFileName = null;

	private static final boolean macOS = (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().startsWith("mac"));
	private static boolean useGUI = false;

	private static Runnable stateChecker = null;
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
    public static final String DEFAULT_FONT = "DefaultFont";
    public static final String SEARCH_URL = "SearchURL";
    public static final String defaultSearchURL = "http://www.google.com/search?q=%s";


    /**
     * load properties from default file
     */
    public static void load() {
        try (var fis = new FileInputStream(getDefaultFileName())) {
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
        try (var fos = new FileOutputStream(getDefaultFileName())) {
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
     *
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
            return Boolean.parseBoolean(value);
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
        final String value = (String) props.get(name);
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
     */
    public static void remove(String key) {
        props.remove(key);
    }

    /**
     * put a property
     */
    public static void put(String key, int value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     */
    public static void put(String key, int[] value) {
        StringBuilder buf = new StringBuilder();
        for (int aValue : value) buf.append(aValue).append(";");
        props.setProperty(key, "" + buf);
    }

    /**
     * put a property
     */
    public static void put(String key, double value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     */
    public static void put(String key, boolean value) {
        props.setProperty(key, "" + value);
    }

    /**
     * put a property
     */
    public static void put(String key, String value) {
        if (value != null)
            props.setProperty(key, value);
        else
            props.remove(key);
    }

    /**
     * put a file property
     */
    public static void put(String key, File value) {
        props.setProperty(key, value.getAbsolutePath());
    }


    /**
     * put a property
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
     */
    public static void setProgramTitle(String title) {
        ProgramProperties.programTitle = title;
    }

    /**
     * gets the program titles string
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

	public static boolean isConfirmQuit() {
		return get("ConfirmQuit", true);
	}

	public static void setConfirmQuit(boolean confirmQuit) {
		put("ConfirmQuit", confirmQuit);
	}

	public static void checkState() {
		if (stateChecker != null)
			stateChecker.run();
	}

	public static void setStateChecker(Runnable stateChecker) {
		ProgramProperties.stateChecker = stateChecker;
	}


    public static void preset(String key, Object value) {
        presets.put(key, value);
    }

    public static void addPresets() {
        for (String key : presets.keySet()) {
            props.put(key, presets.get(key).toString());
        }
    }
}
