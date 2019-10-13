/*
 * BasicSwing.java Copyright (C) 2019. Daniel H. Huson
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
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.*;

/**
 * basic stuff used with swing
 * Daniel Huson, 2005, 3.2019
 */
public class BasicSwing {
    static boolean memoryWarned = false;

    /**
     * get all files listed below the given root directory
     *
     * @param rootDirectory
     * @param fileFilter
     * @param recursively
     * @return list of files
     */
    public static List<File> getAllFilesInDirectory(File rootDirectory, javax.swing.filechooser.FileFilter fileFilter, boolean recursively) {
        final List<File> result = new LinkedList<>();

        final Queue<File> queue = new LinkedList<>();
        File[] list = rootDirectory.listFiles();
        if (list != null) {
            Collections.addAll(queue, list);
            while (queue.size() > 0) {
                File file = queue.poll();
                if (file.isDirectory()) {
                    if (recursively) {
                        File[] below = file.listFiles();
                        if (below != null) {
                            Collections.addAll(queue, below);
                        }
                    }
                } else if (fileFilter == null || fileFilter.accept(file)) {
                    result.add(file);
                }
            }
        }

        return result;
    }

    /**
     * get all files listed below the given root directory
     *
     * @param rootDirectory
     * @param fileFilter
     * @param recursively
     * @return list of files
     */
    public static List<String> getAllFilesInDirectory(String rootDirectory, javax.swing.filechooser.FileFilter fileFilter, boolean recursively) {
        final List<String> result = new LinkedList<>();

        final Queue<File> queue = new LinkedList<>();
        File[] list = (new File(rootDirectory)).listFiles();
        if (list != null) {
            Collections.addAll(queue, list);
            while (queue.size() > 0) {
                File file = queue.poll();
                if (file.isDirectory()) {
                    if (recursively) {
                        File[] below = file.listFiles();
                        if (below != null) {
                            Collections.addAll(queue, below);
                        }
                    }
                } else if (fileFilter == null || fileFilter.accept(file)) {
                    result.add(file.getPath());
                }
            }
        }

        return result;
    }

    /**
     * returns the decodeable description of a font
     *
     * @param font
     * @return family-style-size
     */
    public static String getCode(Font font) {
        String result = font.getFamily();
        switch (font.getStyle()) {
            default:
            case Font.PLAIN:
                result += "-PLAIN";
                break;
            case Font.ITALIC:
                result += "-ITALIC";
                break;
            case Font.BOLD:
                result += "-BOLD";
                break;
            case Font.BOLD + Font.ITALIC:
                result += "-BOLDITALIC";
                break;
        }
        result += "-" + font.getSize();
        return result;
    }

    /**
     * returns the size in device coordinates of the string str
     *
     * @param str
     * @return size
     */
    public static Dimension getStringSize(Graphics gc, String str, Font font) {
        if (str == null)
            return new Dimension(1, 1);
        Font gcFont = gc.getFont();
        if (font != null && !font.equals(gcFont))
            gc.setFont(font);
        int width = gc.getFontMetrics().stringWidth(str);
        int height = gc.getFont().getSize();
        if (!gc.getFont().equals(gcFont))
            gc.setFont(gcFont);
        return new Dimension(width, height);
    }

    /**
     * selects a line in a text area
     *
     * @param ta
     * @param lineno
     */
    public static void selectLine(JTextArea ta, int lineno) {
        if (ta == null || lineno < 0)
            return;
        try {
            String text = ta.getText();
            if (lineno > 0) {
                int start = 0;
                int end;
                int count = 1;
                while (count++ < lineno) {
                    start = text.indexOf('\n', start) + 1;
                }

                end = text.indexOf('\n', start);
                if (end == -1)
                    end = text.length() - 1;

                if (start > 0 && end >= start) {
                    ta.select(start, end);
                }
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
    }

    /**
     * sorts all menu items alphabetically starting at first item
     *
     * @param menu
     * @param firstItem
     */
    public static void sortMenuAlphabetically(JMenu menu, int firstItem) {
        if (menu.getItemCount() - firstItem <= 0)
            return;

        JMenuItem[] array = new JMenuItem[menu.getItemCount() - firstItem];

        for (int i = firstItem; i < menu.getItemCount(); i++) {
            if (menu.getItem(i).getText() == null)
                return; // won't be able to sort these!
            array[i - firstItem] = menu.getItem(i);
        }
        Arrays.sort(array, (o1, o2) -> {
            String name1 = o1.getText();
            String name2 = o2.getText();
            return name1.compareTo(name2);
        });

        while (menu.getItemCount() > firstItem)
            menu.remove(menu.getItemCount() - 1);

        for (JMenuItem anArray : array) menu.add(anArray);
    }

    /**
     * centers a dialog in a parent frame
     *
     * @param dialog
     * @param parent
     */
    public static void centerDialogInParent(JDialog dialog, JFrame parent) {
        if (parent != null)   // center
            dialog.setLocation(new Point(parent.getLocation().x + (parent.getWidth() - dialog.getWidth()) / 2,
                    parent.getLocation().y + (parent.getHeight() - dialog.getHeight()) / 2));
        else
            dialog.setLocation(new Point(300, 300));
    }

    /**
     * centers a dialog on the screen
     *
     * @param dialog
     */
    static public void centerDialogOnScreen(JDialog dialog) {
        Dimension dim = dialog.getToolkit().getScreenSize();
        Rectangle abounds = dialog.getBounds();
        dialog.setLocation((dim.width - abounds.width) / 2,
                (dim.height - abounds.height) / 2);
    }

    /**
     * converts an image to a buffered image
     *
     * @param image
     * @param imageObserver
     * @return buffered image
     */
    public static BufferedImage convertToBufferedImage(Image image, ImageObserver imageObserver) throws IOException, InterruptedException {
        int imageWidth = image.getWidth(imageObserver);
        int imageHeight = image.getHeight(imageObserver);
        int[] array = convertToArray(image, imageWidth, imageHeight);
        BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, imageWidth, imageHeight, array, 0, imageWidth);
        return bufferedImage;
    }

    /**
     * converts the image to a 1-D image
     *
     * @param image
     * @return 1-d image
     */
    private static int[] convertToArray(Image image, int imageWidth, int imageHeight) throws InterruptedException, IOException {
        int[] array = new int[imageWidth * imageHeight];
        PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, imageWidth, imageHeight, array, 0, imageWidth);
        if (pixelGrabber.grabPixels() && ((pixelGrabber.getStatus() & ImageObserver.ALLBITS) != 0))
            return array;
        else
            throw new IOException("Internal error: failed to convert image to 1D array");
    }

    /**
     * gets color as 'r g b' or 'r g b a' string  or string "null"
     *
     * @param color
     * @return r g b a
     */
    public static String toString3Int(Color color) {
        if (color == null)
            return "null";
        final StringBuilder buf = new StringBuilder().append(color.getRed()).append(" ").append(color.getGreen()).append(" ").append(color.getBlue());
        if (color.getAlpha() < 255)
            buf.append(" ").append(color.getAlpha());
        return buf.toString();
    }

    /**
     * gets the memory usage string in MB
     *
     * @param warnLevel warn when less than this amount of memory available
     * @return current memory usage
     */
    public static String getMemoryUsageString(int warnLevel) {
        long used = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000);
        long available = (Runtime.getRuntime().maxMemory() / 1000000);
        if (!memoryWarned && warnLevel > 0 && used + warnLevel >= available) {
            String program = ProgramProperties.getProgramName();
            System.gc();
            new Alert(program + " may require more memory to open this file. Possible fix: cancel the current task, assign more memory to " + program + " and restart");
            memoryWarned = true;
        }
        return used + " of " + available + "M";
    }

    /**
     * gets a color as a background color
     *
     * @param color
     * @return color
     */
    static public String getBackgroundColorHTML(Color color) {
        return String.format("<font bgcolor=#%x>", (color.getRGB() & 0xFFFFFF));
    }

    /**
     * encode a font as a string that can be decoded using Font.decode()
     *
     * @param font
     * @return string
     */
    public static String encode(Font font) {
        String style = "";
        if (font.isBold())
            style += "BOLD";
        if (font.isItalic())
            style += "ITALIC";
        if (style.length() == 0)
            style = "PLAIN";
        return font.getFontName() + "-" + style + "-" + font.getSize();
    }

    /**
     * change font size
     *
     * @param component
     * @param newFontSize
     */
    static public void changeFontSize(Component component, int newFontSize) {
        Font font = new Font(component.getFont().getName(), component.getFont().getStyle(), newFontSize);
        component.setFont(font);
    }

    /**
     * open the given URI in a web browser
     *
     * @param uri
     */
    public static void openWebPage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * open the given URL in a web browser
     *
     * @param url
     */
    public static void openWebPage(URL url) {
        try {
            openWebPage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
