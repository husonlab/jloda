/*
 * Basic.java Copyright (C) 2022 Daniel H. Huson
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

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
     */
    public static PrintStream hideSystemErr() {
        var current = System.err;
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
     */
    public static PrintStream hideSystemOut() {
        var current = System.out;
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
            version = name + " $ " + e;
        }
        return version;
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
     * replace value by replacement, if null
     * @return value, if non-null, else replacment
     */
    public static <T> T replaceNull(T value, T replacementValue) {
        if (value == null)
            return replacementValue;
        else
            return value;
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

    /**
     * are two collections equal?
     */
    public static <T> boolean equal(Collection<T> a, Collection<T> b) {
        if (a == b)
            return true;
        if (a == null || b == null || a.size() != b.size())
            return false;
        a = new HashSet<>(a);
        b = new HashSet<>(b);
        if (a.size() != b.size())
            return false;
        b.removeAll(a);
        return b.isEmpty();
    }

    public static <T> T getAnyMostFrequent(ArrayList<T> list) {
        return list.stream().reduce(BinaryOperator.maxBy(Comparator.comparingInt(o -> Collections.frequency(list, o)))).orElse(null);
    }

    public static String getMethodName() {
        var stackTrace = Thread.currentThread().getStackTrace();
        return stackTrace[2].getMethodName();
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
    public void write(int b) {
        Basic.origErr.write(b);
        buf.append((char) b);
    }

    public String toString() {
        return buf.toString();
    }
}

// EOF
