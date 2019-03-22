/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * resource utilities
 * Daniel Huson, 2003
 */
public class ResourceUtils {
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
     * get all resources under the given package name
     *
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
        return resources.toArray(new String[0]);
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
}
