/**
 * Basic.java 
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
package jloda.util;

/**
 * Some basic useful stuff
 *
 * @author Daniel Huson, 2005
 */

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.*;
import java.io.FileFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;

public class Basic {
    public final static int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8; // maximum length that a Java array can have

    static boolean debugMode = true;
    static final PrintStream origErr = System.err;
    static final PrintStream origOut = System.out;
    static final PrintStream nullOut = new PrintStream(new NullOutStream());
    static private CollectOutStream collectOut;

    /**
     * Catch an exception.
     *
     * @param ex Exception
     */
    public static void caught(Throwable ex) {
        if (debugMode) {
            System.err.println("Caught:");
            ex.printStackTrace();
        } else
            System.err.println(ex.getMessage());
    }

    /**
     * set debug mode. In debug mode, stack traces are printed
     *
     * @param mode
     */
    static public void setDebugMode(boolean mode) {
        debugMode = mode;
    }

    /**
     * Get debug mode. In debug mode, stack traces are printed
     *
     * @return debug mode
     */
    static public boolean getDebugMode() {
        return debugMode;
    }

    /**
     * Ignore all output written to System.err
     *
     * @return the current PrintStream connected to System.err
     */
    public static PrintStream hideSystemErr() {
        PrintStream current = System.err;
        System.setErr(nullOut);
        return current;
    }

    /**
     * send the system err messages to System out
     */
    public static void sendSystemErrToSystemOut() {
        System.setErr(origOut);
    }

    public static void startCollectionStdErr() {
        collectOut = new CollectOutStream();
        System.setErr(new PrintStream(collectOut));
    }

    public static String stopCollectingStdErr() {
        if (collectOut != null) {
            String result = collectOut.toString();
            collectOut = null;
            return result;
        } else
            return "";
    }

    /**
     * Restore the System.err to the given PrintStream
     *
     * @param ps the print stream
     */
    public static void restoreSystemErr(PrintStream ps) {
        System.setErr(ps);
    }

    /**
     * Restore System.err to the standard error stream, even if it was
     * set to something else in between
     */
    public static void restoreSystemErr() {
        System.setErr(origErr);
    }

    /**
     * Ignore all output written to System.out
     *
     * @return the current PrintStream connected to System.out
     */
    public static PrintStream hideSystemOut() {
        PrintStream current = System.out;
        System.setOut(nullOut);
        return current;
    }

    /**
     * Restore the System.out stream to the given PrintStream
     *
     * @param ps the new print stream
     */
    public static void restoreSystemOut(PrintStream ps) {
        System.setOut(ps);
    }

    /**
     * Restore System.out to the standard output stream, even if it was
     * set to something else in between
     */
    public static void restoreSystemOut() {
        System.setOut(origOut);
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
     * skip all spaces starting at position i
     *
     * @param str
     * @param i
     * @return first position containing a non-space character or str.length()
     */
    public static int skipSpaces(String str, int i) {
        while (i < str.length() && Character.isSpaceChar(str.charAt(i)))
            i++;
        return i;
    }

    /**
     * Matches prefix of string and return remainder of string.
     * Prefix need not match string, i.e. only length of prefix is used
     *
     * @param string
     * @param prefix
     * @return remainder of string after prefix, trimmed
     * @exeception IOException if given prefix doesn't match prefix of string
     */
    public static String matchPrefix(String string, String prefix) throws IOException {
        if (!string.startsWith(prefix))
            throw new IOException("Prefix <" + prefix + "> not matched in <" + string + ">");
        return string.substring(prefix.length(), string.length()).trim();
    }

    /**
     * Matches prefix of string and return remainder of string.
     * Prefix need not match string, i.e. only length of prefix is used
     *
     * @param string
     * @param prefix
     * @return remainder of string after prefix, trimmed
     * @exeception IOException if given prefix doesn't match prefix of string
     */
    public static String matchPrefix(String string, String prefix, String altPrefix) throws IOException {
        if (string.startsWith(prefix))
            return string.substring(prefix.length(), string.length()).trim();
        else if (string.startsWith(altPrefix))
            return string.substring(altPrefix.length(), string.length()).trim();
        else
            throw new IOException("Prefix <" + prefix + "> or <" + altPrefix + "> not matched in <" + string + ">");
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
     * replaces all white spaces in the given string str  by the given character c.
     * Represents consecutive spaces by one c
     *
     * @param str
     * @param c
     * @return string was spaces replaced
     */
    public static String replaceSpaces(String str, char c) {
        StringBuilder buf = new StringBuilder();
        boolean prevWasSpace = false;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                if (!prevWasSpace) {
                    buf.append(c);
                    prevWasSpace = true;
                }
            } else {
                buf.append(str.charAt(i));
                prevWasSpace = false;
            }
        }
        return buf.toString();
    }

    /**
     * formats a string so that it looks nice in a dialog box
     *
     * @param message
     * @return formated string
     */
    public static String toMessageString(String message) {
        // insert line breaks
        StringBuilder buf = new StringBuilder();
        int lineLength = 0;
        int numLines = 0;
        for (int i = 0; i < message.length(); i++) {
            if (lineLength > 50 && Character.isSpaceChar(message.charAt(i))) {
                buf.append("\n");
                lineLength = 0;
                numLines++;
                if (numLines > 10) {
                    buf.append("...");
                    break;
                }
            }
            if (lineLength == 80) {
                buf.append("\n").append(message.charAt(i));
                lineLength = 0;
                numLines++;
            } else {
                buf.append(message.charAt(i));
                if (message.charAt(i) == '\n')
                    lineLength = 0;
                else
                    lineLength++;
            }
            if (buf.length() > 2000) {
                buf.append("...");
                break;
            }
        }
        return buf.toString();
    }

    /**
     * gets the text in quotes, removing any quotes already present
     *
     * @param text
     * @return quoted text
     */
    public static String getInCleanQuotes(String text) {
        return "\"" + text.replaceAll("\"", "") + "\"";
    }

    /**
     * given a iterator, returns a new iterator in random order
     *
     * @param it
     * @param seed
     * @return iterator in random order
     */
    public static <T> Iterator<T> randomize(Iterator<T> it, int seed) {
        return randomize(it, new Random(seed));
    }

    /**
     * given a iterator, returns a new iterator in random order
     *
     * @param it
     * @param random
     * @return iterator in random order
     */
    public static <T> Iterator<T> randomize(Iterator<T> it, Random random) {
        final ArrayList<T> list = new ArrayList<>();
        while (it.hasNext())
            list.add(it.next());
        final Object[] array = randomize(list.toArray(), random);
        list.clear();
        return new Iterator<T>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < array.length;
            }

            @Override
            public T next() {
                return (T) array[i++];
            }
        };
    }

    /**
     * given an array, returns it randomized (Durstenfeld 1964)
     *
     * @param array
     * @param seed
     * @return array in random order
     */
    public static <T> T[] randomize(T[] array, int seed) {
        return randomize(array, new Random(seed));
    }

    /**
     * given an array, returns it randomized (Durstenfeld 1964)
     *
     * @param array
     * @param random
     * @return array in random order
     */
    public static <T> T[] randomize(T[] array, Random random) {
        T[] result = (T[]) new Object[array.length];
        System.arraycopy(array, 0, result, 0, array.length);

        for (int i = result.length - 1; i >= 1; i--) {
            int j = random.nextInt(i + 1);
            T tmp = result[i];
            result[i] = result[j];
            result[j] = tmp;
        }
        return result;
    }

    /**
     * randomize array of longs using (Durstenfeld 1964)
     *
     * @param array
     * @param seed
     */
    public static void randomize(long[] array, int seed) {
        Random random = new Random(seed);
        for (int i = array.length - 1; i >= 1; i--) {
            int j = random.nextInt(i + 1);
            long tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /**
     * Round to a given number of significant figures
     *
     * @param num    double
     * @param digits number of digits
     * @return double
     */
    public static double roundSigFig(double num, int digits) {
        if (num == 0) {
            return 0;
        }

        final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
        final int power = digits - (int) d;

        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }


    /**
     * returns the sign of x
     *
     * @param x
     * @return sign of x
     */
    public static int sign(double x) {
        if (x > 0)
            return 1;
        else if (x < 0)
            return -1;
        else
            return 0;
    }

    /**
     * returns a wrapped around string
     *
     * @param str
     * @param lineLength
     * @return wrapped around string
     */
    public static String wraparound(String str, int lineLength) {
        StringBuilder buf = new StringBuilder();

        for (int p = 0; p < str.length(); p += lineLength) {
            buf.append(str.substring(p, Math.min(str.length(), p + lineLength))).append("\n");
        }
        return buf.toString();
    }

    /**
     * returns a collection in a space-separated string
     *
     * @param collection
     * @return space-separated string
     */
    public static String collection2string(Collection collection) {
        return collection2string(collection, " ");
    }

    /**
     * returns a collection in a string
     *
     * @param collection
     * @return space-separated string
     */
    public static String collection2string(Collection collection, String separator) {
        StringBuilder buf = new StringBuilder();
        Iterator it = collection.iterator();
        boolean first = true;
        while (it.hasNext()) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(it.next());
        }
        return buf.toString();
    }

    private static final Set<String> usedFileNames = new HashSet<>();

    /**
     * given a file name, returns a file with a unique file name.
     * The file returned has not been seen during the run of this program and doesn't
     * exist in the file system
     *
     * @param name
     * @return file with new and unique name
     */
    public static File getFileWithNewUniqueName(String name) {
        String prefix;
        String suffix = "";
        int cpyPos = name.lastIndexOf("_cpy");
        int lastDot = name.lastIndexOf(".");

        if (cpyPos > 0)
            prefix = name.substring(0, cpyPos);
        else if (lastDot > 0)
            prefix = name.substring(0, lastDot);
        else
            prefix = name;

        if (lastDot > cpyPos)
            suffix = name.substring(lastDot);

        int count = 0;
        while (true) {
            String newName;
            if (count == 0)
                newName = name;
            else if (count == 1)
                newName = prefix + "_cpy" + suffix;
            else
                newName = prefix + "_cpy" + count + suffix;
            File newFile = new File(newName);
            if (!newFile.exists() && !usedFileNames.contains(newName)) {
                usedFileNames.add(newName);
                return newFile;
            }
            count++;
        }
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
     * sort a list using the given comparator
     *
     * @param list
     * @param comparator
     */
    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        T[] array = (T[]) list.toArray();
        Arrays.sort(array, comparator);
        list.clear();
        list.addAll(Arrays.asList(array));
    }

    /**
     * converts int[] to list of Integers
     *
     * @param array
     * @return list of Integers
     */
    public static List<Integer> asList(int[] array) {
        List<Integer> list = new LinkedList<>();
        for (int value : array) list.add(value);
        return list;
    }

    /**
     * returns the suffix of a file name
     *
     * @param fileName
     * @return file name extension
     */
    public static String getSuffix(String fileName) {
        if (fileName == null)
            return null;
        int pos = fileName.lastIndexOf(".");
        if (pos == -1 || pos == fileName.length() - 1)
            return null;
        else {
            return fileName.substring(pos + 1);
        }
    }

    /**
     * returns the short name of a class
     *
     * @param clazz
     * @return short name
     */
    public static String getShortName(Class clazz) {
        return getSuffix(clazz.getName());
    }

    /**
     * converts a string containing spaces into an array of strings.
     *
     * @param str
     * @return array of strings that where originally separated by spaces
     */
    public static String[] toArray(String str) {
        List<String> list = new LinkedList<>();

        for (int j, i = skipSpaces(str, 0); i < str.length(); i = skipSpaces(str, j)) {
            for (j = i + 1; j < str.length(); j++)
                if (Character.isSpaceChar(str.charAt(j)))
                    break; // found next space
            list.add(str.substring(i, j));
        }
        return list.toArray(new String[list.size()]);

    }


    /**
     * converts a string containing newlines into a list of string
     *
     * @param str
     * @return list of strings
     */
    public static List<String> toList(String str) {
        List<String> list = new LinkedList<>();

        int i = 0;
        while (i < str.length()) {
            int j = i + 1;
            while (j < str.length() && str.charAt(j) != '\n')
                j++;
            list.add(str.substring(i, j));
            i = j + 1;
        }
        return list;
    }

    /**
     * removes all text between any pair of left- and right-delimiters.
     * No nesting
     *
     * @param str
     * @param leftDelimiter
     * @param rightDelimiter
     * @return string with comments removed
     */
    public static String removeComments(String str, char leftDelimiter, char rightDelimiter) {
        StringBuilder buf = new StringBuilder();

        boolean inComment = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (inComment && ch == rightDelimiter) {
                inComment = false;
            } else if (ch == leftDelimiter) {
                inComment = true;
            } else if (!inComment)
                buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * folds the given string so that no line is longer than max length, if possible.
     * Replaces all spaces by single spaces
     *
     * @param str
     * @param maxLength
     * @return string folded at spaces
     */
    public static String fold(String str, int maxLength) {
        return fold(str, maxLength, "\n");
    }

    /**
     * folds the given string so that no line is longer than max length, if possible.
     * Replaces all spaces by single spaces
     *
     * @param str
     * @param maxLength
     * @return string folded at spaces
     */
    public static String fold(String str, int maxLength, String lineBreakString) {
        StringBuilder buf = new StringBuilder();
        StringTokenizer st = new StringTokenizer(str);
        int lineLength = 0;
        boolean first = true;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int pos = token.lastIndexOf(lineBreakString);
            if (pos != -1) {
                if (!first) {
                    buf.append(" ");
                } else
                    first = false;
                buf.append(token.substring(0, pos + lineBreakString.length()));
                lineLength = 0;
                token = token.substring(pos + lineBreakString.length());
            }
            if (lineLength > 0 && lineLength + token.length() >= maxLength) {
                buf.append(lineBreakString);
                lineLength = 0;
            } else {
                if (!first) {
                    buf.append(" ");
                    lineLength++;
                } else
                    first = false;
            }
            lineLength += token.length();
            buf.append(token);
        }
        return buf.toString();
    }


    /**
     * fold to given length
     *
     * @param str
     * @param length
     * @return folded string
     */
    public static String foldHard(String str, int length) {
        StringBuilder buf = new StringBuilder();
        int pos = 0;
        for (int i = 0; i < str.length(); i++) {
            buf.append(str.charAt(i));
            if (str.charAt(i) == '\n')
                pos = 0;
            else
                pos++;
            if (pos == length) {
                buf.append("\n");
                pos = 0;
            }
        }
        // if ((str.length() % length) != 0)
        //     buf.append("\n");
        return buf.toString();
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
        Arrays.sort(array, new Comparator<JMenuItem>() {
            public int compare(JMenuItem o1, JMenuItem o2) {
                String name1 = o1.getText();
                String name2 = o2.getText();
                return name1.compareTo(name2);
            }
        });

        while (menu.getItemCount() > firstItem)
            menu.remove(menu.getItemCount() - 1);

        for (JMenuItem anArray : array) menu.add(anArray);
    }

    /**
     * returns the delta between two binary strings
     *
     * @param a
     * @param b
     * @return delta
     */
    static public String deltaBinarySequences(String a, String b) {
        StringBuilder buf = new StringBuilder();
        int diffStart = -1;
        boolean first = true;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == b.charAt(i)) {
                if (diffStart > -1) {
                    if (first)
                        first = false;
                    else
                        buf.append(",");
                    if (i - 1 == diffStart)
                        buf.append(diffStart + 1);
                    else
                        buf.append(diffStart + 1).append("-").append(i);
                    diffStart = -1;
                }
            } else // chars differ
            {
                if (diffStart == -1)
                    diffStart = i;
            }
        }
        if (diffStart > -1) {
            if (!first)
                buf.append(",");
            if (diffStart == a.length() - 1)
                buf.append(diffStart + 1);
            else
                buf.append(diffStart + 1).append("-").append(a.length());
        }
        if (buf.length() > 0)
            return buf.toString();
        else
            return null;
    }

    /**
     * compute the majority sequence of three binary sequences
     *
     * @param a
     * @param b
     * @param c
     * @return majority sequence
     */
    static public String majorityBinarySequences(String a, String b, String c) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            if ((a.charAt(i) == '1' && (b.charAt(i) == '1' || c.charAt(i) == '1'))
                    || (b.charAt(i) == '1' && c.charAt(i) == '1'))
                buf.append('1');
            else
                buf.append('0');
        }
        return buf.toString();
    }

    /**
     * gets the min value of an array
     *
     * @param array
     * @return min
     */
    public static int min(int[] array) {
        int m = Integer.MAX_VALUE;
        for (int x : array) {
            if (x < m)
                m = x;

        }
        return m;
    }

    /**
     * gets the max value of an array
     *
     * @param array
     * @return max
     */
    public static int max(int[] array) {
        int m = Integer.MIN_VALUE;
        for (int x : array) {
            if (x > m)
                m = x;
        }
        return m;
    }

    /**
     * returns an array of integers as a separated string
     *
     * @param array
     * @return string representation
     */
    public static String toString(int[] array) {
        return toString(array, 0, array.length, ", ");
    }

    /**
     * returns an array of integers as a string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(int[] array, String separator) {
        return toString(array, 0, array.length, separator);
    }


    /**
     * returns an array of integers as astring
     *
     * @param array
     * @return string representation
     */
    public static String toString(int[] array, int offset, int length, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        length = Math.min(offset + length, array.length);
        for (int i = offset; i < length; i++) {
            int x = array[i];
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(x);
        }
        return buf.toString();
    }

    /**
     * returns an array of integers as a separated string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(Object[] array, String separator) {
        return toString(array, 0, array.length, separator);
    }

    /**
     * returns an array of integers as a separated string
     *
     * @param array
     * @param offset where to start reading array
     * @param length how many entries to read
     * @param separator
     * @return string representation
     */
    public static String toString(Object[] array, int offset, int length, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (int i = 0; i < length; i++) {
            Object anArray = array[i + offset];
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(anArray);
        }
        return buf.toString();
    }

    /**
     * returns an array of integers as a separated string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(long[] array, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (long a : array) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(a);
        }
        return buf.toString();
    }


    /**
     * returns an array of double as a separated string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(double[] array, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (double a : array) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(a);
        }
        return buf.toString();
    }

    /**
     * returns an array of double as a separated string
     *
     * @param array
     * @param separator
     * @return string representation
     */
    public static String toString(String format, double[] array, String separator) {
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (double a : array) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(String.format(format, a));
        }
        return buf.toString();
    }

    /**
     * returns a collection of objects a separated string
     *
     * @param collection
     * @param separator
     * @return string representation
     */
    public static String toString(Collection collection, String separator) {
        if (collection == null)
            return "";
        final StringBuilder buf = new StringBuilder();

        boolean first = true;
        for (Object aCollection : collection) {
            if (aCollection != null) {
                if (first)
                    first = false;
                else if (separator != null)
                    buf.append(separator);
                buf.append(aCollection);
            }
        }
        return buf.toString();
    }

    /**
     * concatenates a collection of strings and removes any white spaces
     *
     * @param strings
     * @return concatenated string with no white spaces
     */
    public static String concatenateAndRemoveWhiteSpaces(Collection<String> strings) {
        final StringBuilder buf = new StringBuilder();

        for (String s : strings) {
            for (int pos = 0; pos < s.length(); pos++) {
                char ch = s.charAt(pos);
                if (!Character.isWhitespace(ch))
                    buf.append(ch);
            }
        }
        return buf.toString();
    }


    /**
     * returns a set of bits as a comma separated string
     *
     * @param bits
     * @return string representation
     */
    public static String toString(BitSet bits) {
        if (bits == null)
            return "null";

        final StringBuilder buf = new StringBuilder();

        int startRun = 0;
        int inRun = 0;
        boolean first = true;
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            if (first) {
                first = false;
                buf.append(i);
                startRun = inRun = i;
            } else {
                if (i == inRun + 1) {
                    inRun = i;
                } else if (i > inRun + 1) {
                    if (inRun == startRun || i == startRun + 1)
                        buf.append(",").append(i);
                    else if (inRun == startRun + 1)
                        buf.append(",").append(inRun).append(",").append(i);
                    else
                        buf.append("-").append(inRun).append(",").append(i);
                    inRun = startRun = i;
                }
            }
        }
        // dump last:
        if (inRun == startRun + 1)
            buf.append(",").append(inRun);
        else if (inRun > startRun + 1)
            buf.append("-").append(inRun);
        return buf.toString();
    }

    /**
     * Fetch all resources (i.e. files) that are directly under the specified package structure.
     *
     * @param pckg
     * @return files in given package
     * @throws IOException
     */
    public static String[] fetchResources(String pckg) throws IOException {
        return fetchResources(pckg, getBasicClassLoader());
    }

    /**
     * Get the classloader that can find all resources.
     * Currently this is the system classloader.
     *
     * @return basic class loader
     */
    public static ClassLoader getBasicClassLoader() {
        ClassLoader loaderPlugin = Basic.class.getClassLoader();
        if (loaderPlugin == null) loaderPlugin = ClassLoader.getSystemClassLoader();
        return loaderPlugin;
    }

    /**
     * Get a class instance for the given fully qualified classname.
     * The plugin classloader is used as returned by {@link #getBasicClassLoader()}.
     * <p/>
     * <p/>
     * It is discouraged to use {@link Class#forName(java.lang.String)}.
     *
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    public static Class classForName(String name) throws ClassNotFoundException {
        return getBasicClassLoader().loadClass(name);
    }

    /**
     * get all resources under the given package name
     * @param packageName
     * @param loaderPlugin
     * @return list of resources
     * @throws IOException
     */
    static String[] fetchResources(String packageName, ClassLoader loaderPlugin) throws IOException {
        packageName = packageName.replaceAll("\\.", "/").concat("/");

        Enumeration e = loaderPlugin.getResources(packageName);
        Set<String> resources = new TreeSet<>();
        while (e.hasMoreElements()) {
            final URL url = ((URL) e.nextElement());
            String urlString = URLDecoder.decode(url.getPath(), "UTF-8");
            if (urlString.matches(".+!.+")) //the zip/jar - entry delimiter
            {
                String[] split = urlString.split("!", 2);
                urlString = split[0];
                if (urlString.startsWith("file:"))
                    urlString = urlString.substring("file:".length());

                //recurse through the jar
                try {
                    ZipFile archive = (urlString.endsWith(".jar") ? new JarFile(urlString) : new ZipFile(urlString));
                    Enumeration entries = archive.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry ze = (ZipEntry) entries.nextElement();
                        String name = ze.getName();
                        if (name.startsWith(packageName)) {
                            if (!ze.isDirectory() && name.indexOf('/', packageName.length()) < 0) {
                                resources.add(name.substring(packageName.length()));
                            } else        // subpackages
                            {
                                name = name.replaceAll("/", ".");
                                if (name.endsWith("."))
                                    name = name.substring(0, name.length() - 1);
                                resources.add(name);
                            }
                        }
                    }
                } catch (IOException ex) {
                    System.err.println("URL=" + urlString);
                    Basic.caught(ex);
                }
            } else //we are still in the file system
            {
                final File file = new File(urlString);
                File[] contents = null;
                if (file.isDirectory())
                    contents = file.listFiles();

                if (contents != null)
                    for (int i = 0; i != contents.length; ++i) {
                        if (contents[i].isDirectory()) {
                            String subPackageName = packageName + contents[i].getName();
                            subPackageName = subPackageName.replaceAll("/", ".");
                            resources.add(subPackageName);
                        } else {
                            resources.add(contents[i].getName());
                        }
                    }
            }
        }
        return resources.toArray(new String[resources.size()]);
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
     * returns true, if string can be parsed as int
     *
     * @param next
     * @return true, if int
     */
    public static boolean isInteger(String next) {
        try {
            Integer.parseInt(next);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * returns true, if string can be parsed as long
     *
     * @param next
     * @return true, if int
     */
    public static boolean isLong(String next) {
        try {
            Long.parseLong(next);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


    /**
     * returns true, if string can be parsed as float
     *
     * @param next
     * @return true, if int
     */
    public static boolean isFloat(String next) {
        try {
            Float.parseFloat(next);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * returns true, if string can be parsed as double
     *
     * @param next
     * @return true, if int
     */
    public static boolean isDouble(String next) {
        try {
            Double.parseDouble(next);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


    /**
     * double backslashes
     *
     * @param str
     * @return string with doubled back slashes
     */
    public static String doubleBackSlashes(String str) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\\')
                buf.append('\\');
            buf.append(str.charAt(i));
        }
        return buf.toString();
    }

    /**
     * returns name with .suffix removed
     *
     * @param name
     * @return name without .suffix
     */
    public static String getFileBaseName(String name) {
            if (name != null) {
                int pos = name.lastIndexOf(".");
                if (pos > 0)
                    name = name.substring(0, pos);
            }
        return name;
    }

    /**
     * returns the suffix of a file name. Returns null name is null
     *
     * @param name
     * @return suffix   or null
     */
    public static String getFileSuffix(String name) {
        if (name == null)
            return null;
        name = getFileNameWithoutPath(name);
        int index = name.lastIndexOf('.');
        if (index > 0)
            return name.substring(index);
        else
            return "";
    }

    /**
     * returns name with path removed
     *
     * @param name
     * @return name without path
     */
    public static String getFileNameWithoutPath(String name) {
            if (name != null) {
                int pos = name.lastIndexOf(File.separatorChar);
                if (pos != -1 && pos < name.length() - 1) {
                    name = name.substring(pos + 1);
                }
            }
        return name;
    }

    /**
     * remove all characters except for letters and digits
     *
     * @param str
     * @return string of letters and digits
     */
    public static String removeAllButLettersDigits(String str) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isLetterOrDigit(ch))
                buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * converts a list of objects to a string
     *
     * @param result
     * @param separator
     * @return string
     */
    public static <T> String listAsString(List<T> result, String separator) {
        final StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (T aResult : result) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(aResult.toString());
        }
        return buf.toString();
    }

    /**
     * get list will objects in reverse order
     *
     * @param list
     * @return reverse order list
     */
    public static <T> List<T> reverseList(Collection<T> list) {
        final List<T> result = new LinkedList<>();
        for (T aList : list) {
            result.add(0, aList);
        }
        return result;
    }

    /**
     * get list will objects in rotated order
     *
     * @param list
     * @return rotated order
     */
    public static <T> List<T> rotateList(Collection<T> list) {
        final List<T> result = new LinkedList<>();
        if (list.size() > 0) {
            result.addAll(list);
            result.add(result.remove(0));
        }
        return result;
    }

    /**
     * reduce the number of elements in a list to n
     *
     * @param list
     * @param n
     * @return sublist with n elements
     */
    public static <T> List reduceList(List<T> list, int n) {
        List<T> result = new LinkedList<>();
        int mod = list.size() / n;
        int i = 0;
        int count = 0;
        for (T t : list) {
            if (++i == mod) {
                result.add(t);
                i = 0;
                if (++count == n)
                    break;
            }
        }
        return result;
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
     * trims away empty lines at the beginning and end of a string
     *
     * @param str
     * @return string without leading and trailing empty lines
     */
    public static String trimEmptyLines(String str) {
        int startOfLine = 0;
        for (int p = 0; p < str.length(); p++) {
            if (!Character.isSpaceChar(str.charAt(p)))
                break;
            else if (str.charAt(p) == '\n' || str.charAt(p) == '\r')
                startOfLine = p + 1;
        }

        int endOfLine = str.length();
        for (int p = str.length() - 1; p >= 0; p--) {
            if (!Character.isSpaceChar(str.charAt(p)))
                break;
            else if (str.charAt(p) == '\n' || str.charAt(p) == '\r')
                endOfLine = p;
        }

        if (startOfLine < endOfLine && endOfLine <= str.length()) {
            return str.substring(startOfLine, endOfLine);
        } else
            return str;
    }

    /**
     * counts the number of occurrences of c in string str
     *
     * @param str
     * @param c
     * @return count
     */
    public static int countOccurrences(String str, char c) {
        int count = 0;
        if (str != null) {
            for (int i = 0; i < str.length(); i++)
                if (str.charAt(i) == c)
                    count++;
        }
        return count;
    }

    /**
     * counts the number of occurrences of c at beginning of string str
     *
     * @param str
     * @param c
     * @return count
     */
    public static int countLeadingOccurrences(String str, char c) {
        int count = 0;
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == c)
                    count++;
                else break;
            }
        }
        return count;
    }

    /**
     * counts the number of occurrences of c in byte[] str
     *
     * @param str
     * @param c
     * @return count
     */
    public static int countOccurrences(byte[] str, char c) {
        int count = 0;
        if (str != null) {
            for (byte aStr : str)
                if (aStr == c)
                    count++;
        }
        return count;
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
     * cleans a taxon name so that it only contains of letters, digits, .'s and _'s
     *
     * @param name
     * @return clean taxon name
     */
    public static String toCleanName(String name) {
        if (name == null)
            return "";
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '.' || ch == '_' || ch == '-')
                buf.append(ch);
            else
                buf.append("_");
        }
        String str = buf.toString();
        while (str.length() > 1 && str.startsWith("_"))
            str = str.substring(1);
        while (str.length() > 1 && str.endsWith("_"))
            str = str.substring(0, str.length() - 1);

        return str;
    }

    /**
     * get the lines of a files as a list of strings
     *
     * @param file
     * @return list of strings
     * @throws IOException
     */
    public static List<String> getLinesFromFile(String file) throws IOException {
        List<String> result = new LinkedList<>();
        BufferedReader r = new BufferedReader(new FileReader(file));
        String aLine;
        while ((aLine = r.readLine()) != null) {
            result.add(aLine);
        }
        return result;
    }

    /**
     * gets all individual non-empty lines from a string
     *
     * @param string
     * @return lines
     */
    public static List<String> getLinesFromString(String string) {
        List<String> result = new LinkedList<>();
        BufferedReader r = new BufferedReader(new StringReader(string));
        String aLine;
        try {
            while ((aLine = r.readLine()) != null) {
                aLine = aLine.trim();
                if (aLine.length() > 0)
                    result.add(aLine);
            }
        } catch (IOException e) {
        }
        return result;
    }

    /**
     * remove any strings that are empty or start with #, after trimming. Keep all non-string objects
     *
     * @param list
     * @return cleaned listed of strings
     */
    public static <T> List<T> cleanListOfStrings(Collection<T> list) {
        List<T> result = new LinkedList<>();
        for (T obj : list) {
            if (obj instanceof String) {
                String str = ((String) obj).trim();
                if (str.length() > 0 && !str.startsWith("#"))
                    result.add((T) str);
            } else
                result.add(obj);
        }
        return result;
    }

    /**
     * concatenates two collections and a returns a list
     *
     * @param listA
     * @param listB
     * @return concatenated list
     */
    public static <T> List<T> getConcatenation(Collection<T> listA, Collection<T> listB) {
        List<T> all = new LinkedList<>();
        all.addAll(listA);
        all.addAll(listB);
        return all;
    }

    /**
     * insert spaces before uppercases that follow lower case letters
     *
     * @param x
     * @return
     */
    public static String insertSpacesBetweenLowerCaseAndUpperCaseLetters(String x) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < x.length(); i++) {
            if (i > 0 && Character.isLowerCase(x.charAt(i - 1)) && Character.isUpperCase(x.charAt(i)))
                buf.append(' ');
            buf.append(x.charAt(i));
        }
        return buf.toString();
    }

    /**
     * is file an image file?
     *
     * @param file
     * @return true, if image file
     */
    public static boolean isImageFile(File file) {
        if (file.isDirectory())
            return false;
        final String name = file.getName().toLowerCase();
        return name.endsWith(".gif") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp") || name.endsWith(".png");
    }

    /**
     * get bits as list of integers
     *
     * @param bits
     * @return list of integers
     */
    public static List<Integer> asList(BitSet bits) {
        List<Integer> result = new LinkedList<>();
        if (bits != null) {
            for (int i = bits.nextSetBit(0); i != -1; i = bits.nextSetBit(i + 1))
                result.add(i);
        }
        return result;
    }

    /**
     * get list of integers as bit set
     *
     * @param integers
     * @return bits
     */
    public static BitSet asBitSet(List<Integer> integers) {
        BitSet bits = new BitSet();
        if (integers != null) {
            for (Integer i : integers) {
                bits.set(i);
            }
        }
        return bits;
    }

    static boolean memoryWarned = false;

    /**
     * gets the memory usage string in MB
     *
     * @return current memory usage
     */
    public static String getMemoryUsageString() {
        long used = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
        long available = (Runtime.getRuntime().maxMemory() / 1048576);
        if (available < 1024) {
            return String.format("%d of %dM", used, available);
        } else {
            return String.format("%.1f of %.1fG", (double) used / 1024.0, (double) available / 1024.0);
        }
    }

    /**
     * gets the memory usage string in MB
     *
     * @param warnLevel warn when less than this amount of memory available
     * @return current memory usage
     */
    public static String getMemoryUsageString(int warnLevel) {
        long used = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
        long available = (Runtime.getRuntime().maxMemory() / 1048576);
        if (!memoryWarned && warnLevel > 0 && used + warnLevel >= available) {
            String program = ProgramProperties.getProgramName();
            System.gc();
            new Alert(program + " may require more memory to open this file. Possible fix: cancel the current task, assign more memory to " + program + " and restart");
            memoryWarned = true;
        }
        return used + " of " + available + "M";
    }

    /**
     * does label match pattern?
     *
     * @param pattern
     * @param label
     * @return true, if match
     */
    public static boolean matches(Pattern pattern, String label) {
        if (label == null)
            label = "";
        Matcher matcher = pattern.matcher(label);
        return matcher.find();
    }

    /**
     * convert bytes to a string
     *
     * @return string
     */
    static public String toString(byte[] bytes) {
        if (bytes == null)
            return "";
        char[] array = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            array[i] = (char) bytes[i];
        return new String(array);
    }

    /**
     * convert bytes to a string
     * @param length number of bytes, starting at index 0
     * @return string
     */
    static public String toString(byte[] bytes, int length) {
        if (bytes == null)
            return "";
        char[] array = new char[length];
        for (int i = 0; i < length; i++)
            array[i] = (char) bytes[i];
        return new String(array);
    }

    /**
     * convert bytes to a string
     * @param offset start here
     * @param length number of bytes
     * @return string
     */
    static public String toString(byte[] bytes, int offset, int length) {
        if (bytes == null)
            return "";
        char[] array = new char[length];
        for (int i = 0; i < length; i++)
            array[i] = (char) bytes[i + offset];
        return new String(array);
    }

    /**
     * convert boolean to a string
     *
     * @return string
     */
    static public String toString(boolean[] bools) {
        StringBuilder buf = new StringBuilder();
        if (bools != null) {
            for (boolean a : bools) {
                buf.append(a ? "1" : "0");
            }
        }
        return buf.toString();
    }

    /**
     * convert chars to a string
     *
     * @return string
     */
    static public String toString(char[] chars) {
        return new String(chars);
    }

    /**
     * make a version info file
     *
     * @param fileName
     * @throws IOException
     */
    public static void saveVersionInfo(String fileName) throws IOException {
        fileName = Basic.replaceFileSuffix(fileName, ".info");
        Writer w = new FileWriter(fileName);
        w.write("Created on " + (new Date()) + "\n");
        w.close();
    }

    /**
     * gets the index of a string s in an array of strings
     *
     * @param s
     * @param array
     * @return index or -1
     */
    public static int getIndex(String s, String[] array) {
        for (int i = 0; i < array.length; i++)
            if (s.equals(array[i]))
                return i;
        return -1;
    }

    /**
     * gets the index of a string s in a collection of strings
     *
     * @param s
     * @param collection
     * @return index or -1
     */
    public static int getIndex(String s, Collection<String> collection) {
        int count = 0;
        for (String a : collection) {
            if (a.equals(s))
                return count;
            count++;
        }
        return -1;
    }

    /**
     * gets the number of non-white space characters in a string
     *
     * @param string
     * @return non-space chars
     */
    static public int getNumberOfNonSpaceCharacters(String string) {
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isWhitespace(string.charAt(i)))
                count++;
        }
        return count;
    }

    /**
     * attempts to parse the string as an integer, skipping leading chars and trailing characters, if necessary.
     * Returns 0, if no number found
     *
     * @param string
     * @return value or 0
     */
    public static int parseInt(String string) {
        try {
            if (string != null) {
                int start = 0;
                while (start < string.length()) {
                    int ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    int finish = start + 1;
                    while (finish < string.length() && Character.isDigit(string.charAt(finish)))
                        finish++;
                    if (start < finish)
                        return Integer.parseInt(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    /**
     * attempts to parse the string as a long, skipping leading chars and trailing characters, if necessary
     *
     * @param string
     * @return value or 0
     */
    public static long parseLong(String string) {
        try {
            if (string != null) {
                int start = 0;
                while (start < string.length()) {
                    int ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '+' || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    int finish = start + 1;
                    while (finish < string.length() && Character.isDigit(string.charAt(finish)))
                        finish++;
                    if (start < finish)
                        return Long.parseLong(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    /**
     * attempts to parse the string as an float, skipping leading chars and trailing characters, if necessary
     *
     * @param string
     * @return value or 0
     */
    public static float parseFloat(String string) {
        try {
            if (string != null) {
                int start = 0;
                while (start < string.length()) {
                    int ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '+' || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    int finish = start + 1;
                    while (finish < string.length() && (Character.isDigit(string.charAt(finish)) || string.charAt(finish) == '.'
                            || string.charAt(finish) == 'E' || string.charAt(finish) == 'e' || string.charAt(finish) == '-'))
                        finish++;
                    if (start < finish)
                        return Float.parseFloat(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    /**
     * attempts to parse the string as a double, skipping leading chars and trailing characters, if necessary
     *
     * @param string
     * @return value or 0
     */
    public static double parseDouble(String string) {
        try {
            if (string != null) {
                int start = 0;
                while (start < string.length()) {
                    int ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '+' || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    int finish = start + 1;
                    while (finish < string.length() && (Character.isDigit(string.charAt(finish)) || string.charAt(finish) == '.'
                            || string.charAt(finish) == 'E' || string.charAt(finish) == 'e' || string.charAt(finish) == '-' || string.charAt(finish) == '+'))
                        finish++;
                    if (start < finish)
                        return Double.parseDouble(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }

    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            if (path.listFiles() != null) {
                for (File file : path.listFiles()) {
                    if (file.isDirectory()) {
                        if (!deleteDirectory(file))
                            return false;
                    } else {
                        if (!file.delete())
                            return false;
                    }
                }
            }
        }
        return path.delete();
    }

    /**
     * gets the length of the longest common prefix of the two strings
     *
     * @param a
     * @param b
     * @return length of lcp
     */
    static public int getLongestCommonPrefixLength(String a, String b) {
        int top = Math.min(a.length(), b.length());
        for (int i = 0; i < top; i++)
            if (a.charAt(i) != b.charAt(i))
                return i;
        return top;
    }

    /**
     * gets the total count
     *
     * @param counts
     * @return total
     */
    public static int getTotal(int[] counts) {
        int total = 0;
        for (int count : counts) {
            total += count;
        }
        return total;
    }

    /**
     * gets the total count
     *
     * @param counts
     * @return total
     */
    public static long getTotal(long[] counts) {
        long total = 0;
        for (long count : counts) {
            total += count;
        }
        return total;
    }

    /**
     * replace the suffix of a file
     *
     * @param fileName
     * @param newSuffix
     * @return new file name
     */
    public static String replaceFileSuffix(String fileName, String newSuffix) {
        return replaceFileSuffix(new File(fileName), newSuffix).getPath();
    }

    /**
     * replace the suffix of a file
     *
     * @param file
     * @param newSuffix
     * @return new file
     */
    public static File replaceFileSuffix(File file, String newSuffix) {
        String name = Basic.getFileBaseName(file.getName());
        if (newSuffix != null && !name.endsWith(newSuffix))
            name = name + newSuffix;
        return new File(file.getParent(), name);
    }

    public static String getFileNameWithoutZipOrGZipSuffix(String fileName) {
        if (Basic.isZIPorGZIPFile(fileName))
            return replaceFileSuffix(fileName, "");
        else
            return fileName;
    }

    /**
     * get reverse complement
     *
     * @param sequence
     * @return reverse complement
     */
    public static String getReverseComplement(String sequence) {
        StringBuilder buf = new StringBuilder();
        for (int i = sequence.length() - 1; i >= 0; i--)
            switch (sequence.charAt(i)) {
                case 'A':
                    buf.append('T');
                    break;
                case 'C':
                    buf.append('G');
                    break;
                case 'G':
                    buf.append('C');
                    break;
                case 'T':
                    buf.append('A');
                    break;
                case 'a':
                    buf.append('t');
                    break;
                case 'c':
                    buf.append('g');
                    break;
                case 'g':
                    buf.append('c');
                    break;
                case 't':
                    buf.append('a');
                    break;
                case 'U':
                    buf.append('A');
                    break;
                case 'u':
                    buf.append('a');
                    break;
                default:
                    buf.append(sequence.charAt(i));
            }
        return buf.toString();
    }

    /**
     * get format string that has enough leading zeros to display this number
     *
     * @param number
     * @return format string
     */
    public static String getIntegerFormatLeadingZeros(int number) {
        if (number < 10)
            return "%d";
        else if (number < 100)
            return "%02d";
        else if (number < 1000)
            return "%03d";
        else
            return "%04d";
    }

    final private static long kilo = 1024;
    final private static long mega = 1024 * kilo;
    final private static long giga = 1024 * mega;
    final private static long tera = 1024 * giga;

    /**
     * get memory size string (using TB, GB, MB, kB or B)
     *
     * @param bytes
     * @return string
     */
    public static String getMemorySizeString(long bytes) {

        if (Math.abs(bytes) >= tera)
            return String.format("%3.1f TB", (bytes / (double) tera));
        else if (Math.abs(bytes) >= giga)
            return String.format("%3.1f GB", (bytes / (double) giga));
        else if (Math.abs(bytes) >= mega)
            return String.format("%3.1f MB", (bytes / (double) mega));
        else if (Math.abs(bytes) >= kilo)
            return String.format("%3.1f kB", (bytes / (double) kilo));
        else
            return String.format("%3d B", bytes);
    }
    /**
     * capitalize the first letter of a string
     *
     * @param s
     * @return capitalized word
     */
    public static String capitalizeFirstLetter(String s) {
        if (s.length() > 0 && Character.isLetter(s.charAt(0)) && Character.isLowerCase(s.charAt(0))) {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        } else
            return s;
    }

    /**
     * returns the first line of a text
     *
     * @param text
     * @return first line1
     */
    public static String getFirstLine(String text) {
        if (text == null)
            return "";
        int pos = text.indexOf("\r");
        if (pos != -1)
            return text.substring(0, pos);
        pos = text.indexOf("\n");
        if (pos != -1)
            return text.substring(0, pos);
        return text;
    }

    /**
     * returns the first block of a text up to an empty line. Consecutive lines are separated by single spaces
     *
     * @param text
     * @return first block
     */
    public static String getFirstParagraphAsALine(String text) {
        if (text == null)
            return "";
        StringBuilder buf = new StringBuilder();
        BufferedReader r = new BufferedReader(new StringReader(text));
        String aLine;
        try {
            boolean first = true;
            while ((aLine = r.readLine()) != null) {
                aLine = aLine.trim();
                if (first)
                    first = false;
                else {
                    if (aLine.length() == 0)
                        break;  // found empty line, break;
                    buf.append(" ");
                }
                buf.append(aLine);
            }
        } catch (IOException e) {
        }
        return buf.toString();
    }

    /**
     * get the last line in a text
     *
     * @param text
     * @return last line or empty string
     */
    public static String getLastLine(String text) {
        if (text == null)
            return "";
        int pos = text.lastIndexOf("\r");
        if (pos == text.length() - 1)
            pos = text.lastIndexOf("\r", pos - 1);
        if (pos != -1)
            return text.substring(pos + 1, text.length());
        pos = text.lastIndexOf("\n");
        if (pos == text.length() - 1)
            pos = text.lastIndexOf("\n", pos - 1);
        if (pos != -1)
            return text.substring(pos + 1, text.length());
        return text;
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
     * gets the index of the first space in the string
     *
     * @param string
     * @return index or -1
     */
    public static int getIndexOfFirstWhiteSpace(String string) {
        for (int i = 0; i < string.length(); i++)
            if (Character.isWhitespace(string.charAt(i)))
                return i;
        return -1;
    }

    /**
     * gets the first word in the given string
     *
     * @param string
     * @return word (delimited by a white space) or empty string, if the first character is a white space
     */
    public static String getFirstWord(String string) {
        int i = getIndexOfFirstWhiteSpace(string);
        if (i != -1)
            return string.substring(0, i);
        else
            return string;
    }

    /**
     * gets the first word in the given src string and returns it in the target string
     *
     * @param src
     * @param target
     * @return length
     */
    public static int getFirstWord(byte[] src, byte[] target) {
        for (int i = 0; i < src.length; i++) {
            if (Character.isWhitespace((char) src[i]) || src[i] == 0) {
                return i;
            }
            target[i] = src[i];
        }
        return src.length;
    }


    /**
     * remove all white spaces
     *
     * @param s
     * @return string without white spaces
     */
    public static String removeAllWhiteSpaces(String s) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i)))
                buf.append(s.charAt(i));
        }
        return buf.toString();
    }

    /**
     * reverse a string
     *
     * @param s
     * @return reversed string
     */
    public static String reverseString(String s) {
        StringBuilder buf = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--)
            buf.append(s.charAt(i));
        return buf.toString();
    }

    /**
     * get a string of spaces
     *
     * @param count
     * @return spaces
     */
    public static String spaces(int count) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < count; i++)
            buf.append(" ");
        return buf.toString();
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
     * gets the common name prefix of a set of files
     *
     * @param files
     * @param defaultPrefix
     * @return prefix
     */
    public static String getCommonPrefix(File[] files, String defaultPrefix) {
        List<String> names = new LinkedList<>();
        for (File file : files)
            names.add(file.getName());
        return getCommonPrefix(names, defaultPrefix);
    }

    /**
     * gets the common prefix of a set of names
     *
     * @param names
     * @param defaultPrefix
     * @return prefix
     */
    public static String getCommonPrefix(List<String> names, String defaultPrefix) {
        if (names.size() == 0)
            return "";
        else if (names.size() == 1)
            return Basic.getFileBaseName(names.get(0));

        int posOfFirstDifference = 0;

        boolean ok = true;
        while (ok) {
            int ch = 0;
            for (String name : names) {
                if (posOfFirstDifference >= name.length()) {
                    ok = false;
                    break;
                }
                if (ch == 0)
                    ch = name.charAt(posOfFirstDifference);
                else if (name.charAt(posOfFirstDifference) != ch) {
                    ok = false;
                    break;
                }
            }
            posOfFirstDifference++;
        }

        // get rid of trailing spaces, _ and .
        String name = names.get(0);
        while (posOfFirstDifference > 0) {
            int ch = name.charAt(posOfFirstDifference - 1);
            if (ch != '.' && ch != '_' && !Character.isWhitespace(ch))
                break;
            posOfFirstDifference--;
        }

        if (posOfFirstDifference > 0)
            return name.substring(0, posOfFirstDifference);
        return defaultPrefix;
    }

    /**
     * swallow a leading >, if present
     *
     * @param word
     * @return string with leading > removed
     */
    public static String swallowLeadingGreaterSign(String word) {
        if (word.startsWith(">"))
            return word.substring(1).trim();
        else
            return word;
    }

    /**
     * get the sum of values
     *
     * @param values
     * @return sum
     */
    public static int getSum(Integer[] values) {
        int sum = 0;
        for (Integer value : values) {
            if (value != null)
                sum += value;
        }
        return sum;
    }

    /**
     * get the sum of values
     *
     * @param values
     * @return sum
     */
    public static int getSum(int[] values) {
        int sum = 0;
        for (Integer value : values) {
            sum += value;
        }
        return sum;
    }

    /**
     * get the sum of values
     *
     * @param values
     * @return sum
     */
    public static int getSum(Collection<Integer> values) {
        int sum = 0;
        for (Number value : values)
            sum += value.intValue();
        return sum;
    }

    /**
     * get the sum of values
     *
     * @param values
     * @return sum
     */
    public static int getSum(int[] values, int offset, int len) {
        int sum = 0;
        for (int i = offset; i < len; i++) {
            Integer value = values[i];
            sum += value;
        }
        return sum;
    }

    public static long getSum(long[] array) {
        long result = 0;
        for (long value : array)
            result += value;
        return result;
    }

    /**
     * get all the lines found in a reader
     *
     * @param r0
     * @return lines
     * @throws IOException
     */
    public static String[] getLines(Reader r0) throws IOException {
        BufferedReader r = new BufferedReader(r0);
        LinkedList<String> lines = new LinkedList<>();
        String aLine;
        while ((aLine = r.readLine()) != null) {
            lines.add(aLine);
        }
        return lines.toArray(new String[lines.size()]);
    }

    public static String capitalizeWords(String str) {
        StringBuilder buf = new StringBuilder();
        boolean previousWasSpaceOrPunctuation = true;
        for (int i = 0; i < str.length(); i++) {
            int ch = str.charAt(i);
            if (Character.isWhitespace(ch) || ".:".contains("" + ch)) {
                buf.append((char) ch);
                previousWasSpaceOrPunctuation = true;
            } else {
                if (previousWasSpaceOrPunctuation && Character.isLetter(ch))
                    buf.append((char) Character.toUpperCase(ch));
                else if (Character.isLetter(ch))
                    buf.append((char) Character.toLowerCase(ch));
                else
                    buf.append((char) ch);
                previousWasSpaceOrPunctuation = false;
            }
        }
        return buf.toString();
    }

    public static boolean fileExistsAndIsNonEmpty(String fileName) {
        File file = new File(fileName);
        return file.exists() && file.length() > 0;
    }

    public static void checkFileReadableNonEmpty(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists())
            throw new IOException("No such file: " + fileName);
        if (file.length() == 0)
            throw new IOException("File is empty: " + fileName);
        if (!file.canRead())
            throw new IOException("File not readable: " + fileName);
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

    public static boolean isDate(String s) {
        long time;
        try {
            time = DateFormat.getDateInstance().parse(s).getTime();
        } catch (Exception ex) {
            return false;
        }
        return time > 1000;
    }

    /**
     * replace all backslashes by double backslashes
     *
     * @param str
     * @return string with protected back slashes
     */
    public static String protectBackSlashes(String str) {
        if (str == null)
            return null;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            buf.append(str.charAt(i));
            if (str.charAt(i) == '\\')
                buf.append('\\');
        }
        return buf.toString();
    }

    /**
     * skip all characters upto first digit or '-'
     *
     * @param token
     * @return first number   or null
     */
    public static String skipToNumber(String token) {
        int pos = 0;
        while (pos < token.length()) {
            if (Character.isDigit(token.charAt(pos)) || token.charAt(pos) == '-')
                return token.substring(pos);
            pos++;
        }
        return null;
    }

    /**
     * gets next long
     *
     * @param rand
     * @param max
     * @return long
     */
    public static long nextLong(Random rand, long max) {
        if (max <= 0)
            return 0;
        else if (max < Integer.MAX_VALUE)
            return rand.nextInt((int) max);
        else {
            return (long) (rand.nextDouble() * max);
        }
    }

    /**
     * split a string by the given separator, but honoring quotes around items
     *
     * @param string
     * @param separator
     * @return tokens
     */
    public static String[] splitWithQuotes(String string, char separator) {
        //return string.split(""+separator);

        List<String> list = new LinkedList<>();
        int i = 0;
        while (i < string.length()) {
            if (string.charAt(i) == '\"') { // start of quoted item
                int j = string.indexOf('\"', i + 1);
                if (j == -1) {
                    list.add(string.substring(i + 1, string.length()).trim());
                    break;  // unfinished quote, really should throw an exception
                } else {
                    list.add(string.substring(i + 1, j).trim());
                    i = j + 2;
                }
            } else if (string.charAt(i) == separator) { // separator that follows a separator...
                list.add("");
                i++;
            } else // start of unquoted item
            {
                int j = i + 1;
                while (j < string.length()) {
                    if (string.charAt(j) == separator)
                        break;
                    j++;
                }
                list.add(string.substring(i, j).trim());
                i = j + 1;
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * returns a quoted string if the string for value contains a tab and not a quote
     *
     * @param value
     * @return string or quoted string
     */
    public static String quoteIfContainsTab(Object value) {
        String string = value.toString();
        if (string.contains("\t") && !string.contains("\""))
            return "\"" + string + "\"";
        else
            return string;
    }

    /**
     * restrict a value to a given range
     *
     * @param min
     * @param max
     * @param value
     * @return value between min and max
     */
    public static int restrictToRange(int min, int max, int value) {
        if (value < min)
            return min;
        if (value >= max)
            return max;
        return value;
    }

    /**
     * restrict a value to a given range
     *
     * @param min
     * @param max
     * @param value
     * @return value between min and max
     */
    public static double restrictToRange(double min, double max, double value) {
        if (value < min)
            return min;
        if (value >= max)
            return max;
        return value;
    }


    /**
     * gets the name of a read. This is the first word in the line, skipping any '>' or '@' at first position
     *
     * @param aLine
     * @return word (delimited by a space)
     */
    public static String getReadName(String aLine) {
        if (aLine.length() == 0)
            return "";
        int start;
        if (aLine.charAt(0) == '@' || aLine.charAt(0) == '>')
            start = 1;
        else
            start = 0;
        while (start < aLine.length() && Character.isWhitespace(aLine.charAt(start)))
            start++;
        int finish = start;
        while (finish < aLine.length() && !Character.isWhitespace(aLine.charAt(finish)))
            finish++;
        return aLine.substring(start, finish);
    }

    /**
     * gets the compile time version of the given class
     *
     * @param clazz
     * @return compile time version
     */
    public static String getVersion(final Class clazz) {
        return getVersion(clazz, clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1));
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
            final ClassLoader classLoader = clazz.getClassLoader();
            String threadContexteClass = clazz.getName().replace('.', '/');
            URL url = classLoader.getResource(threadContexteClass + ".class");
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

    /**
     * is either a single word or consists only of spaces
     *
     * @param str
     * @return true if a single word or consists only of spaces
     */
    public static boolean isOneWord(String str) {
        str = str.trim();
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * is named file a directory?
     *
     * @param fileName
     * @return true if directory
     */
    public static boolean isDirectory(String fileName) {
        return ((new File(fileName)).isDirectory());
    }

    /**
     * copy a file
     *
     * @param source
     * @param dest
     * @throws java.io.IOException
     */
    public static void copyFile(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null)
                sourceChannel.close();
            if (destChannel != null)
                destChannel.close();
        }
    }

    /**
     * write a stream to a file
     *
     * @param inputStream
     * @param outputFile
     * @throws IOException
     */
    public static void writeStreamToFile(InputStream inputStream, File outputFile) throws IOException {
        System.err.println("Writing file: " + outputFile);
        if (inputStream == null)
            throw new IOException("Input stream is null");

        try (BufferedOutputStream outs = new BufferedOutputStream(new FileOutputStream(outputFile), 1048576)) {
            while (true) {
                int a = inputStream.read();
                if (a == -1)
                    break;
                else
                    outs.write((byte) a);
            }
        }
    }

    /**
     * append a file
     *
     * @param source
     * @param dest
     * @throws java.io.IOException
     */
    public static void appendFile(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        RandomAccessFile raf = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            raf = new RandomAccessFile(dest, "rw");
            destChannel = raf.getChannel();
            destChannel.transferFrom(sourceChannel, raf.length(), sourceChannel.size());
        } finally {
            if (sourceChannel != null)
                sourceChannel.close();
            if (destChannel != null)
                destChannel.close();
            if (raf != null)
                raf.close();
        }
    }

    /**
     * copy a file and uncompress if necessary
     *
     * @param source
     * @param dest
     * @throws java.io.IOException
     */
    public static void copyFileUncompressed(File source, File dest) throws IOException {
        if (Basic.isZIPorGZIPFile(source.getPath())) {
            try (InputStream ins = Basic.getInputStreamPossiblyZIPorGZIP(source.getPath()); OutputStream outs = new BufferedOutputStream(new FileOutputStream(dest), 8192)) {
                byte[] buffer = new byte[8192];
                int len = ins.read(buffer);
                while (len != -1) {
                    outs.write(buffer, 0, len);
                    len = ins.read(buffer);
                }
            }
        } else
            copyFile(source, dest);
    }

    /**
     * gets a inputstream. If file ends on gz or zip opens appropriate unzipping stream
     *
     * @param fileName
     * @return input stream
     * @throws IOException
     */
    public static InputStream getInputStreamPossiblyZIPorGZIP(String fileName) throws IOException {
        final File file = new File(fileName);
        if (file.isDirectory())
            throw new IOException("Directory, not a file: " + file);
        if (!file.exists())
            throw new IOException("No such file: " + file);
        final InputStream ins;
        if (fileName.toLowerCase().endsWith(".gz")) {
            ins = new GZIPInputStream(new FileInputStream(file));
        } else if (fileName.toLowerCase().endsWith(".zip")) {
            ZipFile zf = new ZipFile(file);
            Enumeration e = zf.entries();
            ZipEntry entry = (ZipEntry) e.nextElement(); // your only file
            ins = zf.getInputStream(entry);
        } else
            ins = new FileInputStream(file);
        return ins;
    }

    /**
     * gets a outputstream. If file ends on gz or zip opens appropriate zipping stream
     *
     * @param fileName
     * @return input stream
     * @throws IOException
     */
    public static OutputStream getOutputStreamPossiblyZIPorGZIP(String fileName) throws IOException {
        OutputStream outs = new FileOutputStream(fileName);
        if (fileName.toLowerCase().endsWith(".gz")) {
            outs = new GZIPOutputStream(outs);
        } else if (fileName.toLowerCase().endsWith(".zip")) {
            final ZipOutputStream out = new ZipOutputStream(outs);
            ZipEntry e = new ZipEntry(Basic.replaceFileSuffix(fileName, ""));
            out.putNextEntry(e);
        }
        return outs;
    }

    /**
     * is this a gz or zip file?
     *
     * @param fileName
     * @return true, if gz or zip file
     */
    public static boolean isZIPorGZIPFile(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".gz") || fileName.endsWith(".zip");
    }

    /**
     * get approximate uncompressed size of file (for use with ProgressListener)
     *
     * @param fileName
     * @return approximate umcompressed size of file
     */
    public static long guessUncompressedSizeOfFile(String fileName) {
        return (isZIPorGZIPFile(fileName) ? 10 : 1) * (new File(fileName)).length();
    }

    /**
     * returns a file that has the same given path and one of the given file extensions
     *
     * @param path
     * @param fileExtensions
     * @return file name or null
     */
    public static String getAnExistingFileWithGivenExtension(String path, final List<String> fileExtensions) {
        if (isZIPorGZIPFile(path))
            path = Basic.replaceFileSuffix(path, "");
        String prev;
        do {
            prev = path;
            for (String ext : fileExtensions) {
                final File file = new File(Basic.replaceFileSuffix(path, ext));
                if (file.exists())
                    return file.getPath();
            }
            path = Basic.getFileBaseName(path); // removes last suffix
        }
        while (path.length() < prev.length()); // while a suffix was actually removed
        return null;
    }

    /**
     * split string on given character. Note that results are subsequently trimmed
     *
     * @param aLine
     * @param splitChar
     * @return split string, trimmed
     */
    public static String[] split(String aLine, char splitChar) {
        if (aLine.length() == 0)
            return new String[0];

        int count = (aLine.charAt(aLine.length() - 1) == splitChar ? 0 : 1);
        for (int i = 0; i < aLine.length(); i++)
            if (aLine.charAt(i) == splitChar)
                count++;
        if (count == 1)
            return new String[]{aLine};
        final String[] result = new String[count];
        int prev = 0;
        int which = 0;
        int pos = 0;
        for (; pos < aLine.length(); pos++) {
            if (aLine.charAt(pos) == splitChar) {
                result[which++] = aLine.substring(prev, pos).trim();
                prev = pos + 1;
            }
        }
        if (pos > prev) {
            result[which] = aLine.substring(prev, pos).trim();
        }
        return result;
    }

    /**
     * split string on given characters. Note that results are subsequently trimmed
     *
     * @param aLine
     * @param splitChar
     * @return split string, trimmed
     */
    public static String[] split(String aLine, char splitChar, char... splitChars) {
        if (aLine.length() == 0)
            return new String[0];

        int count = (aLine.charAt(aLine.length() - 1) == splitChar || contains(splitChars, aLine.charAt(aLine.length() - 1)) ? 0 : 1);

        for (int i = 0; i < aLine.length(); i++)
            if (aLine.charAt(i) == splitChar || contains(splitChars, aLine.charAt(i)))
                count++;
        if (count == 1)
            return new String[]{aLine};
        final String[] result = new String[count];
        int prev = 0;
        int which = 0;
        int pos = 0;
        for (; pos < aLine.length(); pos++) {
            if (aLine.charAt(pos) == splitChar || contains(splitChars, aLine.charAt(pos))) {
                result[which++] = aLine.substring(prev, pos).trim();
                prev = pos + 1;
            }
        }
        if (pos > prev) {
            result[which] = aLine.substring(prev, pos).trim();
        }
        return result;
    }

    /**
     * computes the symmetric different of two hash sets
     *
     * @param set1
     * @param set2
     * @param <T>
     * @return symmetric different
     */
    public static <T> HashSet<T> symmetricDifference(final HashSet<T> set1, final HashSet<T> set2) {
        final HashSet<T> result = new HashSet<>();
        for (T element : set1) {
            if (!set2.contains(element))
                result.add(element);
        }
        for (T element : set2) {
            if (!set1.contains(element))
                result.add(element);
        }
        return result;
    }

    /**
     * computes the symmetric different of two hash sets
     *
     * @param set1
     * @param set2
     * @param <T>
     * @return symmetric different
     */
    public static <T> HashSet<T> intersection(final HashSet<T> set1, final HashSet<T> set2) {
        final HashSet<T> result = new HashSet<>();
        for (T element : set1) {
            if (set2.contains(element))
                result.add(element);
        }
        return result;
    }

    /**
     * read and verify a magic number from a stream
     *
     * @param ins
     * @param expectedMagicNumber
     * @throws java.io.IOException
     */
    public static void readAndVerifyMagicNumber(InputStream ins, byte[] expectedMagicNumber) throws IOException {
        {
            byte[] magicNumber = new byte[expectedMagicNumber.length];
            if (ins.read(magicNumber) != expectedMagicNumber.length || !equal(magicNumber, expectedMagicNumber)) {
                System.err.println("Expected: " + toString(expectedMagicNumber));
                System.err.println("Got:      " + toString(magicNumber));
                throw new IOException("Index is too old or incorrect file (wrong magic number). Please recompute index.");
            }
        }
    }

    /**
     * compare two byte arrays of the same length
     *
     * @param a
     * @param b
     * @return true, if equal values
     */
    public static boolean equal(byte[] a, byte[] b) {
        if (a == null)
            return b == null; // either a==null, b!=null or both null
        else if (b == null)
            return false; // a!=null, b==null

        if (a.length != b.length)
            return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }

    /**
     * copy an int array to an integer array
     *
     * @param array
     * @return integer array copy
     */
    public static Integer[] copyAsIntegerArray(int[] array) {
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
            result[i] = array[i];
        return result;
    }

    /**
     * copy an Integer array to an int array
     *
     * @param array
     * @return int array copy
     */
    public static int[] copyAsIntArray(Collection<Integer> array) {
        int[] result = new int[array.size()];
        int i = 0;
        for (Integer value : array) {
            result[i++] = value;
        }
        return result;
    }

    /**
     * Finds the value of the given enumeration by name, case-insensitive.
     * Throws an IllegalArgumentException if no match is found.
     */
    public static <T extends Enum<T>> T valueOfIgnoreCase(Class<T> enumeration, String name) {
        for (T enumValue : enumeration.getEnumConstants()) {
            if (enumValue.name().equalsIgnoreCase(name)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("There is no value with name '" + name + " in Enum " + enumeration.getClass().getName());
    }

    /**
     * returns file with .gz ending if only that exists
     *
     * @param file
     * @return file or file.gz
     */
    public static File gzippedIfNecessary(File file) {
        if (file.exists() || !(new File(file.getPath() + ".gz")).exists())
            return file;
        else
            return new File(file.getPath() + ".gz");
    }

    /**
     * determines whether the given string contains the given subword, ignoring case. Uses stupid slow algorithm
     *
     * @param string
     * @param subWord
     * @return true, if contained
     */
    public static boolean containsIgnoringCase(String string, int[] subWord) {
        int pos = 0;
        while (pos + subWord.length < string.length()) {
            int i = 0;
            for (; i < subWord.length; i++) {
                if (Character.toLowerCase(string.charAt(pos + i)) != Character.toLowerCase(subWord[i])) {
                    break;
                }
            }
            if (i == subWord.length)
                return true;
            pos++;
        }
        return false;
    }

    /**
     * gets the file type (based on suffix)
     *
     * @param name
     * @return file type or "Unknown"
     */
    public static String getFileType(String name) {
        int pos = name.lastIndexOf(".");
        if (pos != 1 && pos < name.length() - 1) {
            return name.substring(pos + 1).toUpperCase();
        } else
            return "Unknown";
    }

    /**
     * counts commands in a string.
     *
     * @param s
     * @return
     */
    public static int countCommands(String s) {
        s = s.trim();
        if (s.endsWith(";"))
            return countOccurrences(s, ';');
        else
            return countOccurrences(s, ';') + 1;
    }

    /**
     * gets a temporary file name modelled on the given name
     *
     * @param name
     * @return temporary file name
     */
    public static String getTemporaryFileName(String name) {
        String zipSuffix = null;
        if (isZIPorGZIPFile(name)) {
            zipSuffix = getSuffix(name);
            name = getFileNameWithoutZipOrGZipSuffix(name);
        }
        final String suffix = getSuffix(name);
        name = getFileBaseName(name);
        final int number = (int) (System.currentTimeMillis() & ((1 << 20) - 1));
        return String.format("%s-tmp%d.%s%s", name, number, suffix, zipSuffix != null ? "." + zipSuffix : "");
    }

    /**
     * Get string representation of a double matrix
     *
     * @param matrix
     * @return string representation
     */
    public static String toString(double[][] matrix) {
        StringBuilder buf = new StringBuilder();
        for (double[] row : matrix) {
            buf.append(Basic.toString(row, " ")).append("\n");
        }
        return buf.toString();
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

    /**
     * return the lowest power of 2 that is greater or equal to the given number
     *
     * @param i
     * @return next power of 2
     */
    public static int nextPowerOf2(int i) {
        int k = 1;
        while (k < Integer.MAX_VALUE) {
            if (k >= i)
                break;
            k <<= 1;
        }
        return k;
    }

    /**
     * gets value as binary string, always showing all 64 positions
     *
     * @param value
     * @return binary string
     */
    public static String toBinaryString(long value) {
        StringBuilder buf = new StringBuilder();
        for (int shift = 63; shift >= 0; shift--) {
            buf.append((value & (1l << shift)) >>> shift);
        }
        return buf.toString();
    }

    /**
     * gets value as binary string, always showing all 64 positions
     *
     * @param value
     * @return binary string
     */
    public static String toBinaryString(int value) {
        StringBuilder buf = new StringBuilder();
        for (int shift = 31; shift >= 0; shift--) {
            buf.append((value & (1 << shift)) >>> shift);
        }
        return buf.toString();
    }

    /**
     * get all files listed below the given root directory
     *
     * @param rootDirectory
     * @param fileFilter
     * @param recursively
     * @return list of files
     */
    public static List<File> getAllFilesInDirectory(File rootDirectory, FileFilter fileFilter, boolean recursively, ProgressListener progress) {
        final List<File> result = new LinkedList<>();

        try {
            int totalCount = 0;
            final Queue<File> queue = new LinkedList<>();
            File[] list = rootDirectory.listFiles();
            if (list != null) {
                Collections.addAll(queue, list);
                totalCount += queue.size();
                progress.setMaximum(totalCount);
                while (queue.size() > 0) {
                    File file = queue.poll();
                    if (file.isDirectory()) {
                        if (recursively) {
                            File[] below = file.listFiles();
                            if (below != null) {
                                Collections.addAll(queue, below);
                                totalCount += below.length;
                                progress.setMaximum(totalCount);
                            }
                        }
                    } else if (fileFilter == null || fileFilter.accept(file)) {
                        result.add(file);
                    }
                    progress.incrementProgress();
                }
            }
        } catch (CanceledException ex) {
            System.err.println("USER CANCELED, list of files may be incomplete");
        }
        return result;
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base   the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    public static File getRelativeFile(File target, File base) throws IOException {
        String[] baseComponents = base.getCanonicalPath().split(Pattern.quote(File.separator));
        String[] targetComponents = target.getCanonicalPath().split(Pattern.quote(File.separator));

        // skip common components
        int index = 0;
        for (; index < targetComponents.length && index < baseComponents.length; ++index) {
            if (!targetComponents[index].equals(baseComponents[index]))
                break;
        }

        StringBuilder result = new StringBuilder();
        if (index != baseComponents.length) {
            // backtrack to base directory
            for (int i = index; i < baseComponents.length; ++i)
                result.append("..").append(File.separator);
        }
        for (; index < targetComponents.length; ++index)
            result.append(targetComponents[index]).append(File.separator);
        if (!target.getPath().endsWith("/") && !target.getPath().endsWith("\\")) {
            // remove final path separator
            result.delete(result.length() - File.separator.length(), result.length());
        }
        return new File(result.toString());
    }

    /**
     * returns all trimmed lines in a file, excluding empty lines or lines that start with #
     *
     * @param fileName
     * @return lines
     * @throws java.io.IOException
     */
    public static List<String> getAllLines(String fileName) throws IOException {
        final List<String> list = new ArrayList<>();
        FileInputIterator it = new FileInputIterator(fileName);
        while (it.hasNext()) {
            String aLine = it.next().trim();
            if (aLine.length() > 0 && !aLine.startsWith("#"))
                list.add(aLine);
        }
        it.close();
        return list;
    }

    /**
     * gets the file path to the named file using the directory of the referenceFile
     *
     * @param referenceFile
     * @param fileName
     * @return
     */
    public static String getFilePath(String referenceFile, String fileName) {
        if (referenceFile == null || referenceFile.length() == 0)
            return fileName;
        else {
            return new File(((new File(referenceFile).getParent())), getFileNameWithoutPath(fileName)).getPath();
        }
    }

    /**
     * gets the desired column from a tab-separated line of tags
     *
     * @param aLine
     * @param column
     * @return
     */
    public static String getTokenFromTabSeparatedLine(String aLine, int column) {
        int a = 0;
        int count = 0;
        for (int i = 0; i < aLine.length(); i++) {
            if (aLine.charAt(i) == '\t') {
                if (count == column)
                    return aLine.substring(a, i);
                count++;
                if (count == column)
                    a = i + 1;
            }
        }
        if (count == column)
            return aLine.substring(a);
        else
            return "";
    }

    /**
     * return array in reverse order
     *
     * @param strings
     * @return
     */
    public static String[] reverse(String[] strings) {
        String[] result = new String[strings.length];
        for (int i = 0; i < strings.length; i++)
            result[strings.length - 1 - i] = strings[i];
        return result;
    }


    /**
     * return array in reverse order
     *
     * @param strings
     * @return
     */
    public static String[] reverse(Collection<String> strings) {
        final String[] result = new String[strings.size()];
        int pos = strings.size();
        for (String str : strings) {
            result[--pos] = str;
        }
        return result;
    }

    /**
     * gets the rank of a value in a list
     *
     * @param list
     * @param value
     * @return rank or -1
     */
    public static <T> int getRank(List<T> list, T value) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(value))
                return i;
        }
        return -1;
    }

    /**
     * gets the rank of a value in an array
     *
     * @param list
     * @param value
     * @return rank or -1
     */
    public static <T> int getRank(T[] list, T value) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] != null && list[i].equals(value))
                return i;
        }
        return -1;
    }

    /**
     * gets the first line in a file. File may be zgipped or zipped
     *
     * @param file
     * @return first line or null
     * @throws IOException
     */
    public static String getFirstLineFromFile(File file) {
        try {
            try (BufferedReader ins = new BufferedReader(new InputStreamReader(getInputStreamPossiblyZIPorGZIP(file.getPath())))) {
                return ins.readLine();
            }
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * gets the first line in a file. File may be zgipped or zipped
     *
     * @param file
     * @return first line or null
     * @throws IOException
     */
    public static String[] getFirstLinesFromFile(File file, int count) {
        try {
            String[] lines = new String[count];
            try (BufferedReader ins = new BufferedReader(new InputStreamReader(getInputStreamPossiblyZIPorGZIP(file.getPath())))) {
                for (int i = 0; i < count; i++) {
                    lines[i] = ins.readLine();
                    if (lines[i] == null)
                        break;
                }
            }
            return lines;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * gets the first bytes from a file. File may be zgipped or zipped
     *
     * @param file
     * @return first bytes
     * @throws IOException
     */
    public static byte[] getFirstBytesFromFile(File file, int count) {
        try {
            try (InputStream ins = getInputStreamPossiblyZIPorGZIP(file.getPath())) {
                byte[] bytes = new byte[count];
                ins.read(bytes, 0, count);
                return bytes;
            }
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * does the given string contain the given count of character ch?
     *
     * @param string
     * @param ch
     * @param count
     * @return true, if string contains atleast count occurrences of ch
     */
    public static boolean contains(String string, char ch, int count) {
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == ch && --count == 0)
                return true;
        }
        return false;
    }

    /**
     * does the given array of characters contain the given one?
     *
     * @param string
     * @param ch
     * @return true, if contained
     */
    public static boolean contains(char[] string, char ch) {
        for (char a : string) {
            if (a == ch)
                return true;
        }
        return false;
    }

    /**
     * abbreviate a string to the given length
     *
     * @param string
     * @param length
     * @return abbreviated string
     */
    public static String abbreviate(String string, int length) {
        if (string.length() <= length)
            return string;
        else
            return string.substring(0, length - 1) + ".";
    }

    /**
     * abbreviate a string to the given length
     *
     * @param string
     * @param length
     * @return abbreviated string
     */
    public static String abbreviateDotDotDot(String string, int length) {
        if (string.length() <= length)
            return string;
        else
            return string.substring(0, length - 1) + "...";

    }

    /**
     * skip the first line in a string
     *
     * @param string
     * @return first line
     */
    public static String skipFirstLine(String string) {
        int pos = string.indexOf('\n');
        if (pos != -1)
            return string.substring(pos + 1);
        else
            return string;
    }

    /**
     * skip the first word in a string and trim
     *
     * @param string
     * @return first line
     */
    public static String skipFirstWord(String string) {
        for (int pos = 0; pos < string.length(); pos++) {
            if (Character.isWhitespace(string.charAt(pos)))
                return string.substring(pos).trim();
        }
        return "";
    }

    /**
     * convert a string with spaces and/or underscores to camel case
     *
     * @param string
     * @return camel case
     */
    public static String toCamelCase(String string) {
        int pos = 0;
        while (pos < string.length() && (Character.isWhitespace(string.charAt(pos)) || string.charAt(pos) == '_'))
            pos++;
        boolean afterWhiteSpace = false;
        StringBuilder buf = new StringBuilder();
        while (pos < string.length()) {
            final char ch = string.charAt(pos);
            if (Character.isWhitespace(ch) || ch == '_')
                afterWhiteSpace = true;
            else if (afterWhiteSpace) {
                buf.append(Character.toUpperCase(ch));
                afterWhiteSpace = false;
            } else
                buf.append(Character.toLowerCase(ch));
            pos++;
        }
        return buf.toString();
    }

    /**
     * convert a string with spaces and/or underscores to camel case
     *
     * @param string
     * @return camel case
     */
    public static String fromCamelCase(String string) {
        boolean afterWhiteSpace = true;
        StringBuilder buf = new StringBuilder();
        for (int pos = 0; pos < string.length(); pos++) {
            final char ch = string.charAt(pos);
            if (Character.isUpperCase(ch)) {
                if (!afterWhiteSpace) {
                    buf.append(" ");
                }
            }
            buf.append(ch);
            afterWhiteSpace = (Character.isWhitespace(ch));
        }
        return buf.toString();
    }

    /**
     * gets next word after given first word
     * @param first
     * @param aLine
     * @return next word or null
     */
    public static String getWordAfter(String first, String aLine) {
        int start = aLine.indexOf(first);
        if (start == -1)
            return null;
        start += first.length();
        while (start < aLine.length() && Character.isWhitespace(aLine.charAt(start)))
            start++;
        int finish = start;
        while (finish < aLine.length() && !Character.isWhitespace(aLine.charAt(finish)))
            finish++;
        if (finish < aLine.length())
            return aLine.substring(start, finish);
        else
            return aLine.substring(start);

    }

    /**
     * gets everything after the first word
     *
     * @param first
     * @param aLine
     * @return everything after the given word or null
     */
    public static String getAfter(String first, String aLine) {
        int start = aLine.indexOf(first);
        if (start == -1)
            return null;
        start += first.length();
        while (start < aLine.length() && Character.isWhitespace(aLine.charAt(start)))
            start++;
        int finish = start;
        return aLine.substring(start);

    }

    /**
     * determines whether a ends with b, ignoring case
     *
     * @param a
     * @param b
     * @return true, if a ends with b, ignoring case
     */
    public static boolean endsWithIgnoreCase(String a, String b) {
        return a.toLowerCase().endsWith(b.toLowerCase());
    }

     /**
     * get the number of bytes used to terminate a line
      *
      * @param file
      * @return 1 or 2
      */
    public static int determineEndOfLinesBytes(File file) {
        try {
            RandomAccessFile r = new RandomAccessFile(file, "r");
            int count = 0;
            long length = 0;
            for (; count < 5; count++) {
                String aLine = r.readLine();
                if (aLine == null)
                    break;
                length += aLine.length();
            }
            long diff = r.getFilePointer() - length;
            r.close();
            return (int) (diff / count);
        } catch (Exception e) {
            //Basic.caught(e);
            return 1;
        }
    }

    /**
     * replace value by replacement, if null
     *
     * @param value
     * @param replacementValue
     * @param <T>
     * @return value, if non-null, else replacment
     */
    public static <T> T replaceNull(T value, T replacementValue) {
        if (value == null)
            return replacementValue;
        else
            return value;
    }

    /**
     * get comparator that compares by decreasing length of second and then lexicographical on first
     *
     * @return comparator
     */
    public static Comparator<Pair<String, String>> getComparatorDecreasingLengthOfSecond() {
        return new Comparator<Pair<String, String>>() {
            @Override
            public int compare(Pair<String, String> pair1, Pair<String, String> pair2) { // sorting in decreasing order of length
                if (pair1.getSecond().length() > pair2.getSecond().length())
                    return -1;
                else if (pair1.getSecond().length() < pair2.getSecond().length())
                    return 1;
                else
                    return pair1.getFirst().compareTo(pair2.getFirst());
            }
        };
    }

    /**
     * transposes a matrix
     *
     * @param matrix
     * @return transposed
     */
    public static float[][] transposeMatrix(float[][] matrix) {
        final float[][] transposed = new float[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < transposed.length; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }
}

/**
 * silent stream
 */
class NullOutStream extends OutputStream {
    public void write(int b) {
    }
}

class CollectOutStream extends OutputStream {
    private StringBuilder buf = new StringBuilder();

    @Override
    public void write(int b) throws IOException {
        Basic.origErr.write(b);
        buf.append((char) b);
    }

    public String toString() {
        return buf.toString();
    }
}

// EOF
