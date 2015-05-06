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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;


/**
 * get icons and  cursors from resources
 */
public class ResourceManager {

    /**
     * Specifies the path where to look for icon files in a jar archive.
     */
    private static String iconPackagePath;

    /**
     * Specifies the path where to look for cursor files in a jar archive.
     */
    private static String cursorPackagePath;
    /**
     * Specifies the path where to look for data files in a jar archive.
     */
    private static String filePackagePath;


    /**
     * Maps icon names to initialized icons that are reachable by a getter.
     */
    private static final HashMap<String, ImageIcon> iconMap;

    /**
     * Maps cursor names to initialized icons that are reachable by a getter.
     */
    private static final HashMap<String, Cursor> cursorMap;

    /**
     * Maps file names to initialized files that are reachable by a getter.
     */
    private static final HashMap<String, File> dataMap;

    /**
     * Static constructor.
     */
    static {
        iconPackagePath = "resources.icons";
        cursorPackagePath = "resources.cursors";
        filePackagePath = "resources.files";
        iconMap = new HashMap<>();
        cursorMap = new HashMap<>();
        dataMap = new HashMap<>();
    }

    /**
     * get the icon package path
     *
     * @return icon package path
     */
    public static String getIconPackagePath() {
        return iconPackagePath;
    }

    /**
     * set the icon pacakage path
     *
     * @param iconPackagePath
     */
    public static void setIconPackagePath(String iconPackagePath) {
        ResourceManager.iconPackagePath = iconPackagePath;
    }

    /**
     * get the cursor package path
     *
     * @return path
     */
    public static String getCursorPackagePath() {
        return cursorPackagePath;
    }

    /**
     * set the cursor package path
     *
     * @param cursorPackagePath
     */
    public static void setCursorPackagePath(String cursorPackagePath) {
        ResourceManager.cursorPackagePath = cursorPackagePath;
    }

    public static String getFilePackagePath() {
        return filePackagePath;
    }

    public static void setFilePackagePath(String filePackagePath) {
        ResourceManager.filePackagePath = filePackagePath;
    }

    /**
     * Returns the icon with name specified by the parameter, or <code>null</code> if there is none.
     */
    public static ImageIcon getIcon(String name) {

        if (!iconMap.containsKey(name)) {
            Image iconImage = getImageResource(iconPackagePath, name);
            if (iconImage != null) {
                ImageIcon icon = new ImageIcon(iconImage);
                iconMap.put(name, icon);
            } else {
                if (Basic.getDebugMode())
                    System.err.println("ICON NOT FOUND: " + name + ", path: " + iconPackagePath);
                Image image = getImageResource(iconPackagePath, "sun/toolbarButtonGraphics/general/Help16.gif");
                if (image != null)
                    return new ImageIcon(image);
                else
                    return null;
            }
        }
        return iconMap.get(name);
    }

    /**
     * Returns the file with name specified by the parameter, or <code>null</code> if there is none.
     */
    public static File getFile(String name) {
        if (!dataMap.containsKey(name)) {
            File data = getFileResource(filePackagePath, name);
            dataMap.put(name, data);
        }
        return dataMap.get(name);
    }

    /**
     * Returns the path with name specified by the parameter, or just the name, else
     */
    public static String getFileName(String name) {
        if (!dataMap.containsKey(name)) {
            File data = getFileResource(filePackagePath, name);
            dataMap.put(name, data);
        }
        File file = dataMap.get(name);
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
        fileName = fileName.trim();
        if (fileName.length() == 0)
            return null;
        return getFileAsStream(filePackagePath, fileName);
    }

    /**
     * Returns file resource as stream, unless the string contains a slash, in which case returns Stream from the file system
     *
     * @param filePackage the package containing file
     * @param fileName       the name of the file
     */
    public static InputStream getFileAsStream(String filePackage, String fileName) {
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
            return getFileResourceAsStream(filePackage, fileName);

    }

    /**
     * Returns the cursor with name specified by the parameter, or <code>null</code> if there is none.
     */
    public static Cursor getCursor(String name) {
        if (!cursorMap.containsKey(name)) {
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            final Dimension dim = toolkit.getBestCursorSize(20, 20);
            final int x = dim.width / 2;
            final int y = dim.height / 2;

            Image image = getImageResource(cursorPackagePath, name);
            if ((new ImageIcon(image)).getImageLoadStatus() == MediaTracker.COMPLETE) {
                Cursor cursor = toolkit.createCustomCursor(image, new Point(x, y), name);
                cursorMap.put(name, cursor);

            }
        }
        return cursorMap.get(name);
    }

    /**
     * Returns an Image (icon) with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName       the name of the icon file
     */
    public static Image getImageResource(String packageName, String fileName) {
        Image ret = null;
        try {
            String resname = "/" + packageName.replace('.', '/') + "/" + fileName;
            resname = resname.replaceAll(" ", "\\ ");
            InputStream is = ResourceManager.class.getResourceAsStream(resname);
            if (is != null) {
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
                ret = Toolkit.getDefaultToolkit().createImage(buffer);
                is.close();
            }
        } catch (Exception exc) {
            Basic.caught(exc);
        }
        return ret;
    }

    /**
     * Returns an Image (icon) with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName       the name of the icon file
     */
    public static BufferedImage getBufferedImageResource(String packageName, String fileName) {
        BufferedImage bufferedImage = null;
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replaceAll(" ", "\\ ");
            final InputStream is = ResourceManager.class.getResourceAsStream(resourceName);
            if (is != null) {
                bufferedImage = ImageIO.read(is);
                is.close();
            }
        } catch (Exception exc) {
            Basic.caught(exc);
        }
        return bufferedImage;
    }


    /**
     * Returns File with specified file name at the location specified by <code>packageName</code>.
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName       the name of the file
     */
    public static File getFileResource(String packageName, String fileName) {
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replaceAll(" ", "\\ ");
            final URL url = ResourceManager.class.getResource(resourceName);
            return new File(url.getFile());
        } catch (Exception exc) {
        }
        return null;
    }

    /**
     * Returns file resource as stream
     *
     * @param packageName the path through a package (the name of the subpackage) where to look for the icon
     * @param fileName       the name of the file
     */
    public static InputStream getFileResourceAsStream(String packageName, String fileName) {
        try {
            final String resourceName = ("/" + packageName.replace('.', '/') + "/" + fileName).replace(" ", "\\ ");
            return ResourceManager.class.getResourceAsStream(resourceName);
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        return null;
    }

    /**
     * does resource file exist?
     *
     * @param fileName
     * @return true if file exists
     */
    public static boolean resourceFileExists(String fileName) {
        try {
            final InputStream ins = ResourceManager.class.getResourceAsStream("/resources/files/" + fileName);
            if (ins != null) {
                ins.close();
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * gets an image from the named package
     *
     * @param packageName
     * @param fileName
     * @return image
     * @throws IOException
     */
    public static Image getImage(String packageName, String fileName) throws IOException {
        return getImageResource(packageName, fileName);
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

        InputStream ins = null;
        try {
            ins = getFileAsStream(name);
            return (ins != null);
        } catch (Exception ex) {
        } finally {
            if (ins != null)
                try {
                    ins.close();
                } catch (IOException e) {
                }
        }
        return false;
    }

    /**
     * does the named file exist as a resource ?
     *
     * @param packageName
     * @param name
     * @return true if named file exists as a resource
     */
    public static boolean fileResourceExists(String packageName, String name) {
        if (name == null || name.length() == 0)
            return false;

        InputStream ins = null;
        boolean existsAsStream = false;
        try {
            ins = getFileResourceAsStream(packageName, name);
            existsAsStream = (ins != null);
        } catch (Exception ex) {
        } finally {
            if (ins != null)
                try {
                    ins.close();
                } catch (IOException e) {
                }
        }
        return existsAsStream || (new File(name)).canRead();
    }

}


