/*
 * ResourceManagerFX.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.util;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jloda.util.Basic;
import jloda.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * get icons and  cursors from resources
 * Daniel Huson and others, 2003, 2018
 */
public class ResourceManagerFX {
    private static final ArrayList<Pair<Class, String>> classLoadersAndRoots = new ArrayList<>();

    private static final HashMap<String, Image> iconMap = new HashMap<>();
    private static final HashMap<String, Image> imageMap = new HashMap<>();

    static {
        classLoadersAndRoots.add(new Pair<>(ResourceManagerFX.class, "jloda/resources"));
    }

    /**
     * gets the named icon
     */
    public static Image getIcon(String fileName) {
        if (iconMap.containsKey(fileName))
            return iconMap.get(fileName);

        for (Pair<Class, String> pair : classLoadersAndRoots) {
            final Image iconImage = getImageResource(pair.getFirst(), pair.getSecond() + "/icons", fileName);
            if (iconImage != null) {
                iconMap.put(fileName, iconImage);
                return iconMap.get(fileName);
            }
        }
        if (Basic.getDebugMode())
            System.err.println("ICON NOT FOUND: " + fileName);
        return null;
    }

    public static ImageView getIconAsImageView(String fileName, final double height) {
        final ImageView imageView = new ImageView(getIcon(fileName));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(height);
        return imageView;
    }

    public static ArrayList<Image> getIcons(String... fileNames) {
        final ArrayList<Image> list = new ArrayList<>();
        for (String name : fileNames) {
            final Image image = getIcon(name);
            if (image != null)
                list.add(image);
        }
        return list;
    }


    public static Image getImage(String fileName) {
        if (imageMap.containsKey(fileName))
            return imageMap.get(fileName);

        for (Pair<Class, String> pair : classLoadersAndRoots) {
            final Image image = getImageResource(pair.getFirst(), pair.getSecond() + "/images", fileName);
            if (image != null) {
                imageMap.put(fileName, image);
                return image;
            }
        }
        return null;
    }


    /**
     * Gets the named file
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
     * @param fileName the name of the file
     */
    public static InputStream getFileAsStream(String fileName) {
        if (fileName == null)
            return null;
        for (Pair<Class, String> pair : classLoadersAndRoots) {
            final InputStream stream = getFileAsStream(pair.getFirst(), pair.getSecond() + "/files", fileName);
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
            final File file = new File(fileName);
            try {
                return Basic.getInputStreamPossiblyZIPorGZIP(file.getPath());
            } catch (IOException e) {
                if (!fileName.endsWith(".info")) // don't complain about missing info files
                    System.err.println(e.getMessage());
                return null;
            }
        } else {
            try {
                InputStream ins = getFileResourceAsStream(clazz, filePackage, fileName);
                if (ins != null)
                    return Basic.getInputStreamPossiblyGZIP(ins, fileName);
            } catch (IOException e) {
            }
            return null;
        }
    }


    /**
     * Returns an Image (icon) with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the icon file
     */
    public static Image getImageResource(Class clazz, String packageName, String fileName) {
        final String resname = ("/" + packageName.replace('.', '/') + "/" + fileName).replaceAll(" ", "\\ ");
        try (InputStream is = clazz.getResourceAsStream(resname)) {
            byte[] buffer = new byte[0];
            byte[] tmpbuf = new byte[1024];
            while (true) {
                int len = is.read(tmpbuf);
                if (len <= 0)
                    break;
                byte[] newbuf = new byte[buffer.length + len];
                System.arraycopy(buffer, 0, newbuf, 0, buffer.length);
                System.arraycopy(tmpbuf, 0, newbuf, buffer.length, len);
                buffer = newbuf;
            }
            return new Image(new ByteArrayInputStream(buffer));
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Returns File with specified file name at the location specified by <code>packageName</code>.
     *
     * @param clazz
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName    the name of the file
     */
    public static File getFileResource(Class clazz, String packageName, String fileName) {
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replaceAll(" ", "\\ ");
            final URL url = clazz.getResource(resourceName);
            return new File(url.getFile());
        } catch (Exception exc) {
            return null;
        }
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
            return null;
        }
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
            return null;
        }
    }

    /**
     * gets an image from the named package
     *
     * @param packageName
     * @param fileName
     * @return image
     * @throws IOException
     */
    public static Image getImage(Class clazz, String packageName, String fileName) {
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

    public static ArrayList<Pair<Class, String>> getClassLoadersAndRoots() {
        return classLoadersAndRoots;
    }

    public static void addResourceRoot(Class clazzForClassLoader, String rootPath) {
        classLoadersAndRoots.add(new Pair<>(clazzForClassLoader, rootPath));
    }

    /**
     * gets the compile time version of the given class
     *
     * @param clazz
     * @param name
     * @return compile time version
     */
    public static String getVersion(final Class clazz, final String name) {
        String version;
        try {
            final String threadContexteClass = clazz.getName().replace('.', '/');
            final URL url = clazz.getResource(threadContexteClass + ".class");
            if (url == null) {
                version = name + " $ (no manifest) $";
            } else {
                final String path = url.getPath();
                final String jarExt = ".jar";
                int index = path.indexOf(jarExt);
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                if (index != -1) {
                    final String jarPath = path.substring(0, index + jarExt.length());
                    final File file = new File(jarPath);
                    final String jarVersion = file.getName();
                    final JarFile jarFile = new JarFile(new File(new URI(jarPath)));
                    final JarEntry entry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
                    version = name + " $ " + jarVersion.substring(0, jarVersion.length() - jarExt.length()) + " $ " + sdf.format(new Date(entry.getTime()));
                    jarFile.close();
                } else {
                    final File file = new File(path);
                    version = name + " $ " + sdf.format(new Date(file.lastModified()));
                }
            }
        } catch (Exception e) {
            //Basic.caught(e);
            version = name + " $ " + e.toString();
        }
        return version;
    }
}


