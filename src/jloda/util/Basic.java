/*
 * Basic.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.util;

import javafx.collections.ObservableList;
import jloda.thirdparty.HexUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.*;

/**
 * Some basic useful stuff
 *
 * @author Daniel Huson, 2005
 */
public class Basic {
    public final static int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8; // maximum length that a Java array can have

    static boolean debugMode = true;
    static final PrintStream origErr = System.err;
    static final PrintStream origOut = System.out;
    static final PrintStream nullOut = new PrintStream(new NullOutStream());
    static private CollectOutStream collectOut;

    /**
     * Catch an exception.
     */
    public static void caught(Throwable ex) {
        if (!(ex instanceof UsageException && ex.getMessage().startsWith("Help"))) {
            if (debugMode) {
                System.err.println("Caught:");
                ex.printStackTrace();
            } else
                System.err.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /**
     * set debug mode. In debug mode, stack traces are printed
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
        String result = getCollected();
        collectOut = null;
        return result;
    }

    public static String getCollected() {
        return collectOut != null ? collectOut.toString() : "";
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
     * skip all spaces starting at position i
     * @return first position containing a non-space character or str.length()
     */
    public static int skipSpaces(String str, int i) {
        while (i < str.length() && Character.isSpaceChar(str.charAt(i)))
            i++;
        return i;
    }

	/**
     * given a list, returns a new collection in random order
     * @return iterator in random order
     */
    public static <T> ArrayList<T> randomize(List<T> list, long seed) {
        return randomize(list, new Random(seed));
    }

    /**
     * given an array, returns it randomized (Durstenfeld 1964)
     *
     * @return array in random order
     */
    public static <T> T[] randomize(T[] array, long seed) {
        return randomize(array, new Random(seed));
    }

    /**
     * given an array, returns it randomized (Durstenfeld 1964)
     * @return array in random order
     */
    public static <T> T[] randomize(T[] array, Random random) {
        final T[] result = Arrays.copyOf(array, array.length);

        for (int i = result.length - 1; i >= 1; i--) {
            int j = random.nextInt(i + 1);
            if (j != i) {
                T tmp = result[i];
                result[i] = result[j];
                result[j] = tmp;
            }
        }
        return result;
    }

    /**
     * given an array, returns it randomized (Durstenfeld 1964)
     * @return array in random order
     */
    public static <T> ArrayList<T> randomize(Collection<T> array, Random random) {
        final ArrayList<T> result = new ArrayList<>(array);

        for (int i = result.size() - 1; i >= 1; i--) {
            int j = random.nextInt(i + 1);
            if (j != i) {
                T tmp = result.get(i);
                result.set(i, result.get(j));
                result.set(j, tmp);
            }
        }
        return result;
    }

    /**
     * randomize array of longs using (Durstenfeld 1964)
     */
    public static void randomize(long[] array, long seed) {
        Random random = new Random(seed);
        for (int i = array.length - 1; i >= 1; i--) {
            int j = random.nextInt(i + 1);
            if (j != i) {
                long tmp = array[i];
                array[i] = array[j];
                array[j] = tmp;
            }
        }
    }

    /**
     * randomize array of int using (Durstenfeld 1964)
     */
    public static void randomize(int[] array, long seed) {
        Random random = new Random(seed);
        for (int i = array.length - 1; i >= 1; i--) {
            int j = random.nextInt(i + 1);
            if (j != i) {
                int tmp = array[i];
                array[i] = array[j];
                array[j] = tmp;
            }
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
     * gets the current date
     *
     * @param pattern, e.g. yyyy-MM-dd hh:mm:ss
     * @return date string
     */
    public static String getDateString(String pattern) {
        return (new SimpleDateFormat(pattern)).format(System.currentTimeMillis());
    }

    /**
     * returns the short name of a class
     *
     * @return short name
     */
    public static String getShortName(Class<?> clazz) {
        return clazz.getSimpleName();
    }


    /**
     * gets the min value of an array
     * @return min
     */
    public static int min(int... array) {
        var m = Integer.MAX_VALUE;
        for (var x : array) {
            if (x < m)
                m = x;

        }
        return m;
    }

    /**
     * gets the max value of an array
     * @return max
     */
    public static int max(int... array) {
        var m = Integer.MIN_VALUE;
        for (var x : array) {
            if (x > m)
                m = x;
        }
        return m;
    }

    /**
     * gets the max value of an array
     */
    public static int max(Iterable<Integer> list) {
        var m = Integer.MIN_VALUE;
        for (var x : list) {
            if (x != null && x > m)
                m = x;
        }
        return m;
    }


    /**
     * returns true, if string can be parsed as int
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
     * returns true, if string can be parsed as a boolean
     * @return true, if boolean
     */
    public static boolean isBoolean(String next) {
        return next.equalsIgnoreCase("true") || next.equalsIgnoreCase("false");
    }

    /**
     * returns true, if string can be parsed as int
     * @return true, if boolean
     */
    public static boolean parseBoolean(String next) {
        next = next.trim();
        return next.length() >= 4 && next.substring(0, 4).toLowerCase().startsWith("true");
    }


    /**
     * returns true, if string can be parsed as long
     * @return true, if long
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
     * @return true, if float
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
     * @return true, if double
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
     * get list will objects in reverse order
     * @return reverse order list
     */
    public static <T> List<T> reverseList(Collection<T> list) {
        final var result = new LinkedList<T>();
        for (T aList : list) {
            result.add(0, aList);
        }
        return result;
    }

    /**
     * get list with objects in rotated order
     * @return rotated order
     */
    public static <T> List<T> rotateList(Collection<T> list) {
        final var result = new LinkedList<T>();
        if (list.size() > 0) {
            result.addAll(list);
            result.add(result.remove(0));
        }
        return result;
    }


    /**
     * get bits as list of integers
     * @return list of integers
     */
    public static List<Integer> asList(BitSet bits) {
        var result = new ArrayList<Integer>();
        if (bits != null) {
            for (int i = bits.nextSetBit(0); i != -1; i = bits.nextSetBit(i + 1))
                result.add(i);
        }
        return result;
    }

    /**
     * get list of integers as bit set
     * @return bits
     */
    public static BitSet asBitSet(List<Integer> integers) {
        var bits = new BitSet();
        if (integers != null) {
            for (Integer i : integers) {
                bits.set(i);
            }
        }
        return bits;
    }

    /**
     * gets the memory usage string in MB
     *
     * @return current memory usage
     */
    public static String getMemoryUsageString() {
        long used = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000);
        long available = (Runtime.getRuntime().maxMemory() / 1000000);
        if (available < 1024) {
            return String.format("%d of %dM", used, available);
        } else {
            return String.format("%.1f of %.1fG", (double) used / 1000.0, (double) available / 1000.0);
        }
    }

	/**
     * gets the index of an object s in an array of objects
     * @return index or -1
     */
    public static int getIndex(Object s, Object[]... array) {
        for (var i = 0; i < array.length; i++)
            if (s.equals(array[i]))
                return i;
        return -1;
    }

    /**
     * attempts to parse the string as an integer, skipping leading chars and trailing characters, if necessary.
     * Returns 0, if no number found
     * @return value or 0
     */
    public static int parseInt(String string) {
        try {
            if (string != null) {
                var start = 0;
                while (start < string.length()) {
                    var ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    var finish = start + 1;
                    while (finish < string.length() && Character.isDigit(string.charAt(finish)))
                        finish++;
                    if (start < finish)
                        return Integer.parseInt(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
            if(string.equalsIgnoreCase("-inf"))
                return Integer.MIN_VALUE;
            else if(string.equalsIgnoreCase("inf"))
                return Integer.MAX_VALUE;

        }
        return 0;
    }

    /**
     * attempts to parse the string as a long, skipping leading chars and trailing characters, if necessary
     * @return value or 0
     */
    public static long parseLong(String string) {
        try {
            if (string != null) {
                var start = 0;
                while (start < string.length()) {
                    var ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '+' || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    var finish = start + 1;
                    while (finish < string.length() && Character.isDigit(string.charAt(finish)))
                        finish++;
                    if (start < finish)
                        return Long.parseLong(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
            if(string.equalsIgnoreCase("-inf"))
                return Long.MIN_VALUE;
            else if(string.equalsIgnoreCase("inf"))
                return Long.MAX_VALUE;

        }
        return 0;
    }

    /**
     * attempts to parse the string as an float, skipping leading chars and trailing characters, if necessary
     * @return value or 0
     */
    public static float parseFloat(String string) {
        try {
            if (string != null) {
                var start = 0;
                while (start < string.length()) {
                    var ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '+' || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    var finish = start + 1;
                    while (finish < string.length() && (Character.isDigit(string.charAt(finish)) || string.charAt(finish) == '.'
                            || string.charAt(finish) == 'E' || string.charAt(finish) == 'e' || string.charAt(finish) == '-'))
                        finish++;
                    if (start < finish)
                        return Float.parseFloat(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
            if(string.equalsIgnoreCase("-inf"))
                return Float.NEGATIVE_INFINITY;
            else if(string.equalsIgnoreCase("inf"))
                return Float.POSITIVE_INFINITY;
        }
        return 0;
    }

    /**
     * attempts to parse the string as a double, skipping leading chars and trailing characters, if necessary
     * @return value or 0
     */
    public static double parseDouble(String string) {
        try {
            if (string != null) {
                var start = 0;
                while (start < string.length()) {
                    var ch = string.charAt(start);
                    if (Character.isDigit(ch) || ch == '+' || ch == '-')
                        break;
                    start++;
                }
                if (start < string.length()) {
                    var finish = start + 1;
                    while (finish < string.length() && (Character.isDigit(string.charAt(finish)) || string.charAt(finish) == '.'
                            || string.charAt(finish) == 'E' || string.charAt(finish) == 'e' || string.charAt(finish) == '-' || string.charAt(finish) == '+'))
                        finish++;
                    if (start < finish)
                        return Double.parseDouble(string.substring(start, finish));
                }
            }
        } catch (Exception ex) {
            if(string.equalsIgnoreCase("-inf"))
                return Double.NEGATIVE_INFINITY;
            else if(string.equalsIgnoreCase("inf"))
                return Double.POSITIVE_INFINITY;

        }
        return 0;
    }


    final private static long kilo = 1024;
    final private static long mega = 1024 * kilo;
    final private static long giga = 1024 * mega;
    final private static long tera = 1024 * giga;

    /**
     * get memory size string (using TB, GB, MB, kB or B)
     * @return string
     */
    public static String getMemorySizeString(long bytes) {

        if (Math.abs(bytes) >= tera)
            return StringUtils.removeTrailingZerosAfterDot(String.format("%3.1f TB", (bytes / (double) tera)));
        else if (Math.abs(bytes) >= giga)
            return StringUtils.removeTrailingZerosAfterDot(String.format("%3.1f GB", (bytes / (double) giga)));
        else if (Math.abs(bytes) >= mega)
            return StringUtils.removeTrailingZerosAfterDot(String.format("%3.1f MB", (bytes / (double) mega)));
        else if (Math.abs(bytes) >= kilo)
            return StringUtils.removeTrailingZerosAfterDot(String.format("%3.1f kB", (bytes / (double) kilo)));
        else
            return StringUtils.removeTrailingZerosAfterDot(String.format("%3d B", bytes));
    }


    /**
     * get the sum of values
     * @return sum
     */
    public static int getSum(Integer[] values) {
        var sum = 0;
        for (var value : values) {
            if (value != null)
                sum += value;
        }
        return sum;
    }

    /**
     * get the sum of values
     * @return sum
     */
    public static int getSum(int[] values) {
        var sum = 0;
        for (var value : values) {
            sum += value;
        }
        return sum;
    }

    /**
     * get the sum of values
     * @return sum
     */
    public static float getSum(float[] values) {
        var sum = 0;
        for (var value : values) {
            sum += value;
        }
        return sum;
    }


    /**
     * get the sum of values
     * @return sum
     */
    public static int getSum(Collection<Integer> values) {
        var sum = 0;
        for (var value : values)
            sum += value;
        return sum;
    }

    /**
     * get the sum of values
     *
     * @return sum
     */
    public static int getSum(int[] values, int offset, int len) {
        var sum = 0;
        for (var i = offset; i < len; i++) {
            var value = values[i];
            sum += value;
        }
        return sum;
    }

    public static long getSum(long[] array) {
        var result = 0L;
        for (var value : array)
            result += value;
        return result;
    }

    /**
     * get all the lines found in a reader
     *
     * @return lines
     */
    public static String[] getLines(Reader r0) throws IOException {
        var r = new BufferedReader(r0);
        LinkedList<String> lines = new LinkedList<>();
        String aLine;
        while ((aLine = r.readLine()) != null) {
            lines.add(aLine);
        }
        return lines.toArray(new String[0]);
    }

	/**
     * restrict a value to a given range
     * @return value between min and max
     */
    public static int restrictToRange(int min, int max, int value) {
        if (value < min)
            return min;
        return Math.min(value, max);
    }

    /**
     * restrict a value to a given range
     * @return value between min and max
     */
    public static double restrictToRange(double min, double max, double value) {
        if (value < min)
            return min;
        return Math.min(value, max);
    }


    /**
     * gets the compile time version of the given class
     *
     * @return compile time version
     */
    public static String getVersion(final Class<?> clazz) {
        return getVersion(clazz, clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1));
    }

    /**
     * gets the compile time version of the given class
     *
     * @return compile time version
     */
    public static String getVersion(final Class<?> clazz, final String name) {
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
     * computes the intersection different of two hash sets
     * @return symmetric different
     */
    public static <T> Collection<T> intersection(final Collection<T> set1, final Collection<T> set2) {
        final Collection<T> result = new HashSet<>();
        for (T element : set1) {
            if (set2.contains(element))
                result.add(element);
        }
        return result;
    }

    public static <T> boolean intersects(Collection<T> a, Collection<T> b) {
        if (a.size() == 0 || b.size() == 0)
            return false;
        for (T element : a) {
            if (b.contains(element))
                return true;
        }
        return false;
    }

    public static <T> HashSet<T> union(Collection<T> a, Collection<T> b) {
        final HashSet<T> union = new HashSet<>(a);
        union.addAll(b);
        return union;
    }

    public static <T> ArrayList<T> difference(Collection<T> a, Collection<T> b) {
        final ArrayList<T> union = new ArrayList<>();
        for (T t : a) {
            if (!b.contains(t))
                union.add(t);
        }
        return union;
    }

    /**
     * read and verify a magic number from a stream
     */
    public static void readAndVerifyMagicNumber(InputStream ins, byte[] expectedMagicNumber) throws IOException {
        byte[] magicNumber = new byte[expectedMagicNumber.length];
        if (ins.read(magicNumber) != expectedMagicNumber.length || !equal(magicNumber, expectedMagicNumber)) {
            System.err.println("Expected: " + StringUtils.toString(expectedMagicNumber));
            System.err.println("Got:      " + StringUtils.toString(magicNumber));
            throw new IOException("Index is too old or incorrect file (wrong magic number). Please recompute index.");
        }
    }

    /**
     * compare two byte arrays of the same length
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
     * return list in reverse order
     */
    public static <T> ArrayList<T> reverse(Collection<T> list) {
        var source = new ArrayList<T>(list);
        var target = new ArrayList<T>(list.size());
        for (var i = source.size() - 1; i >= 0; i--)
            target.add(source.get(i));
        return target;
    }

    /**
     * reverses an array list in place
     */
    public static <T> void reverseInPlace(ArrayList<T> list) {
        final var top = list.size() / 2;
        final var size1 = list.size() - 1;
        for (var i = 0; i < top; i++) {
            var tmp = list.get(i);
            list.set(i, list.get(size1 - i));
            list.set(size1 - i, tmp);
        }
    }

    /**
     * gets the rank of a value in a list
     */
    public static <T> int getRank(List<T> list, T value) {
        return list.indexOf(value);
    }

    /**
     * gets the rank of a value in an array
     */
    public static <T> int getRank(T[] list, T value) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] != null && list[i].equals(value))
                return i;
        }
        return -1;
    }

    /**
     * does the given array contain the given object`?
     * @return true, if contained
     */
    public static <T> boolean contains(T[] array, T obj) {
        if (obj == null) {
            for (T a : array)
                if (a == null)
                    return true;
        } else
            for (T a : array) {
                if (a != null && a.equals(obj))
                    return true;
            }
        return false;
    }


    /**
     * replace value by replacement, if null
     * @return value, if non-null, else replacment
     */
    public static <T> T replaceNull(T value, T replacementValue) {
        if (value == null)
            return replacementValue;
        else
            return value;
    }

    /**
     * transposes a matrix
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

    /**
     * kinda generic array construction
     * @return array
     */
    public static <T> T[] toArray(final Collection<T> collection) {
        return (T[]) collection.toArray();
    }


	/**
     * sort a list using the given comparator
      */
    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        T[] array = (T[]) list.toArray();
        Arrays.sort(array, comparator);
        list.clear();
        list.addAll(Arrays.asList(array));
    }

    public static String getDurationString(long start, long end) {
        long diff = Math.abs(end - start);

        if (diff > 3600000)
            return String.format("%.1f", diff / 3600000.0) + "m";
        else if (diff > 60000)
            return String.format("%.1f", diff / 60000.0) + "m";
        else
            return String.format("%.1f", diff / 1000.0) + "s";
    }

    /**
     * find a element in the list for which clazz is assignable from
     */
    public static <T> T findByClass(ObservableList<T> list, Class<?> clazz) {
        for (T t : list) {
            if (clazz.isAssignableFrom(t.getClass()))
                return t;
        }
        return null;
    }

    public static boolean equals(double a, double b, double threshold) {
        return Math.abs(a - b) <= threshold;
    }

    /**
     * computes the weighted sum of two arrays (not necessarily of the same length_
     */
    public static double[] weightedSum(double p, double[] array1, double q, double[] array2) {
        final int top = Math.max(array1.length, array2.length);

        final double[] sum = new double[top];
        for (int i = 0; i < top; i++) {
            sum[i] = (i < array1.length ? p * array1[i] : 0) + (i < array2.length ? q * array2[i] : 0);
        }
        return sum;
    }

    /**
     * parse a string that might end on k, m or g, for kilo, mega or giga
     */
    public static long parseKiloMegaGiga(String string) {
        string = string.toLowerCase();
        int pos = string.lastIndexOf("k");
        if (pos != -1)
            return 1024L * Long.parseLong(string.substring(0, pos));
        pos = string.lastIndexOf("m");
        if (pos != -1)
            return 1048576L * Long.parseLong(string.substring(0, pos));
        pos = string.lastIndexOf("g");
        if (pos != -1)
            return 1073741824L * Long.parseLong(string.substring(0, pos));
        return Long.parseLong(string);
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
    private final StringBuilder buf = new StringBuilder();

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
