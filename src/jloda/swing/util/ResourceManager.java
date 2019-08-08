/*
 * ResourceManager.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.util;

import jloda.util.Basic;
import jloda.util.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * get icons and  cursors from resources
 */
public class ResourceManager {
    private static final ArrayList<Pair<Class, String>> classLoadersAndRoots = new ArrayList<>();

    private static final HashMap<String, ImageIcon> iconMap = new HashMap<>();
    private static final HashMap<String, BufferedImage> imageMap = new HashMap<>();

    private static boolean warningMissingIcon = true;

    static {
        classLoadersAndRoots.add(new Pair<>(ResourceManager.class, "jloda/resources"));
    }

    /**
     * Returns the icon with name specified by the parameter, or <code>null</code> if there is none.
     */
    public static ImageIcon getIcon(String name) {
        if (iconMap.containsKey(name))
            return iconMap.get(name);

        for (Pair<Class, String> pair : classLoadersAndRoots) {
            final Image iconImage = getImageResource(pair.getFirst(), pair.getSecond() + "/icons", name);
            if (iconImage != null) {
                final ImageIcon icon = new ImageIcon(iconImage);
                iconMap.put(name, icon);
                return iconMap.get(name);
            }
        }
        if (Basic.getDebugMode() && warningMissingIcon)
            System.err.println("ICON NOT FOUND: " + name);
        return null;
    }

    /**
     * get all named icons
     *
     * @param names
     * @return icons
     */
    public static ArrayList<ImageIcon> getIcons(String... names) {
        final ArrayList<ImageIcon> list = new ArrayList<>();
        for (String name : names) {
            list.add(getIcon(name));
        }
        return list;
    }

    public static BufferedImage getImage(String fileName) {
        if (imageMap.get(fileName) != null)
            return imageMap.get(fileName);
        for (Pair<Class, String> pair : classLoadersAndRoots) {
            final BufferedImage image = getImageResource(pair.getFirst(), pair.getSecond() + "/images", fileName);
            if (image != null) {
                imageMap.put(fileName, image);
                return image;
            }
        }
        return null;
    }


    /**
     * Returns the file with name specified by the parameter, or <code>null</code> if there is none.
     */
    public static File getFile(String name) {
        for (Pair<Class, String> pair : classLoadersAndRoots) {
            final File file = getFileResource(pair.getFirst(), pair.getSecond() + "/files", name);
            if (file != null)
                return file;
        }
        return null;
    }

    /**
     * Returns the file with name specified by the parameter, or <code>null</code> if there is none.
     */
    public static URL getCssURL(String name) {
        for (Pair<Class, String> pair : classLoadersAndRoots) {
            final URL url = getFileURL(pair.getFirst(), pair.getSecond() + "/css", name);
            if (url != null)
                return url;
        }
        return null;
    }

    /**
     * Returns the path with name specified by the parameter, or just the name, else
     */
    public static String getFileName(String name) {
        final File file = getFile(name);
        if (file != null)
            return file.getPath().replaceAll("%20", " ");
        else
            return "File " + name + ": Path not found";
    }

    /**
     * Gets stream from package resources.files, else attempts to open
     * stream from named file in file system
     *
     * @param name the name of the file
     */
    public static InputStream getFileAsStream(String name) {
        if (name == null)
            return null;
        for (Pair<Class, String> pair : classLoadersAndRoots) {
            final InputStream stream = getFileAsStream(pair.getFirst(), pair.getSecond() + "/files", name);
            if (stream != null)
                return stream;
        }
        return null;
    }

    /**
     * Returns file resource as stream, unless the string contains a slash, in which case returns Stream from the file system
     *
     * @param filePackage the package containing file
     * @param fileName    the name of the file
     */
    public static InputStream getFileAsStream(Class clazz, String filePackage, String fileName) {
        if (fileName.contains("/") || fileName.contains("\\")) {
            File file = new File(fileName);
            try {
                return Basic.getInputStreamPossiblyZIPorGZIP(file.getPath());
            } catch (IOException e) {
                if (!fileName.endsWith(".info")) // don't complain about missing info files
                    System.err.println(e.getMessage());
                return null;
            }
        } else
            return getFileResourceAsStream(clazz, filePackage, fileName);
    }

    /**
     * Returns an Image (icon) with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the icon file
     */
    public static BufferedImage getImageResource(Class clazz, String packageName, String fileName) {
        final String resname = "/" + packageName.replace('.', '/') + "/" + fileName.replaceAll(" ", "\\ ");
        try (InputStream ins = clazz.getResourceAsStream(resname)) {
            if (ins != null)
                return ImageIO.read(ins);
        } catch (Exception exc) {
        }
        return null;
    }

    /**
     * Returns an Image (icon) with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the icon file
     */
    public static BufferedImage getBufferedImageResource(Class clazz, String packageName, String fileName) {
        BufferedImage bufferedImage = null;
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replaceAll(" ", "\\ ");
            try (InputStream ins = clazz.getResourceAsStream(resourceName)) {
                if (ins != null) {
                    bufferedImage = ImageIO.read(ins);
                }
            }
        } catch (Exception exc) {
        }
        return bufferedImage;
    }


    /**
     * Returns File with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the file
     */
    public static File getFileResource(Class clazz, String packageName, String fileName) {
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replaceAll(" ", "\\ ");
            final URL url = clazz.getResource(resourceName);
            if (url != null)
                return new File(url.getFile());
        } catch (Exception exc) {
        }
        return null;
    }

    /**
     * Returns URL with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the file
     */
    public static URL getFileURL(Class clazz, String packageName, String fileName) {
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replaceAll(" ", "\\ ");
            return clazz.getResource(resourceName);
        } catch (Exception exc) {
        }
        return null;
    }

    /**
     * Returns file resource as stream
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the file
     */
    public static InputStream getFileResourceAsStream(Class clazz, String packageName, String fileName) {
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replace(" ", "\\ ");
            return clazz.getResourceAsStream(resourceName);
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        return null;
    }

    /**
     * gets an image from the named package
     *
     * @param packageName
     * @param fileName
     * @return image
     * @throws IOException
     */
    public static Image getImage(Class clazz, String packageName, String fileName) throws IOException {
        return getImageResource(clazz, packageName, fileName);
    }

    /**
     * does the named file exist as a resource or file?
     *
     * @param name
     * @return true if named file exists as a resource or file
     */
    public static boolean fileExists(String name) {
        if (name == null || name.length() == 0)
            return false;

        try (InputStream ins = getFileAsStream(name)) {
            return (ins != null);
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * does the named file exist as a resource ?
     *
     * @param packageName
     * @param name
     * @return true if named file exists as a resource
     */
    public static boolean fileResourceExists(Class clazz, String packageName, String name) {
        if (name == null || name.length() == 0)
            return false;

        try (InputStream ins = getFileResourceAsStream(clazz, packageName, name)) {
            return (ins != null);
        } catch (IOException ex) {
            return false;
        }
    }

    public static void setWarningMissingIcon(boolean warningMissingIcon) {
        ResourceManager.warningMissingIcon = warningMissingIcon;
    }

    public static HashMap<String, ImageIcon> getIconMap() {
        return iconMap;
    }

    public static ArrayList<Pair<Class, String>> getClassLoadersAndRoots() {
        return classLoadersAndRoots;
    }

    public static void addResourceRoot(Class clazzForClassLoader, String rootPath) {
        classLoadersAndRoots.add(0, new Pair<>(clazzForClassLoader, rootPath));
    }

    public static void addResourceRoot(Class clazzInResourceRoot) {
        classLoadersAndRoots.add(0, new Pair<>(clazzInResourceRoot, clazzInResourceRoot.getPackageName()));
    }
}


