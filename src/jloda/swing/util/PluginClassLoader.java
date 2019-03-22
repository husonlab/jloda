/*
 *  Copyright (C) 2015 Daniel H. Huson
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
import jloda.util.ResourceUtils;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Finds all classes in the named package, of the given type
 * <p>
 * 2003
 */
public class PluginClassLoader {
    // Extended the PluginClassLoader to include the Plugins from the pluginFolder
    static private HashMap<String, URLClassLoader> pluginName2URLClassLoader = new HashMap<>();

    /**
     * get an instance of each class of the given type in the given package
     *
     * @param packageName
     * @param clazz
     * @return instances
     */
    public static List<Object> getInstances(String packageName, Class<jloda.swing.commands.ICommand> clazz) {
        return getInstances(new String[]{packageName}, clazz);
    }

    /**
     * get an instance of each class of the given type in the given packages
     *
     * @param clazz
     * @param packageNames
     * @return instances
     */
    public static List<Object> getInstances(Class clazz, String... packageNames) {
        return getInstances(clazz, null, packageNames);
    }

    /**
     * get an instance of each class of the given types in the given packages
     *
     * @param clazz1       this must be assignable from the returned class
     * @param clazz2       if non-null, must also be assignable from the returned class
     * @param packageNames
     * @return instances
     */
    public static List<Object> getInstances(Class clazz1, Class clazz2, String... packageNames) {
        final List<Object> plugins = new LinkedList<>();
        final LinkedList<String> packageNameQueue = new LinkedList<>();
        packageNameQueue.addAll(Arrays.asList(packageNames));
        while (packageNameQueue.size() > 0) {
            try {
                final String packageName = packageNameQueue.removeFirst();
                final String[] resources = ResourceUtils.fetchResources(packageName);

                for (int i = 0; i != resources.length; ++i) {
                    //System.err.println("Resource: " + resources[i]);
                    if (resources[i].endsWith(".class")) {
                        try {
                            resources[i] = resources[i].substring(0, resources[i].length() - 6);
                            final Class c = Basic.classForName(packageName.concat(".").concat(resources[i]));
                            if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && clazz1.isAssignableFrom(c) && (clazz2 == null || clazz2.isAssignableFrom(c))) {
                                try {
                                    plugins.add(c.newInstance());
                                } catch (InstantiationException ex) {
                                    //Basic.caught(ex);
                                }
                            }
                        } catch (Exception ex) {
                            // Basic.caught(ex);
                        }
                    } else {
                        try {
                            final String newPackageName = resources[i];
                            if (!newPackageName.equals(packageName)) {
                                packageNameQueue.addLast(newPackageName);
                                // System.err.println("Adding package name: " + newPackageName);
                            }
                        } catch (Exception ex) {
                            // Basic.caught(ex);
                        }
                    }
                }
            } catch (IOException ex) {
                Basic.caught(ex);
            }
        }
        return plugins;
    }

    /**
     * get an instance of each class of the given type in the given packages
     *
     * @param packageNames
     * @param clazz
     * @return instances
     */
    public static List<Object> getInstances(String[] packageNames, Class clazz) {
        return getInstances(clazz, null, packageNames);
    }

    /**
     * get an instance of each class of the given type in the given package, sorted
     *
     * @param packageName
     * @param type
     * @return instances
     */
    public static List getInstancesSorted(String packageName, Class<jloda.swing.commands.ICommand> type) {
        List plugins = getInstances(packageName, type);

        Object[] array = plugins.toArray();

        Arrays.sort(array, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                // First compare the interface... if equal, compare the name

                Class[] int1 = o1.getClass().getInterfaces();
                Class[] int2 = o2.getClass().getInterfaces();

                if (int1.length == 0 || int2.length == 0) {
                    if (int1.length == 0 && int2.length > 0)
                        return 1;
                    else if (int1.length > 0)
                        return -1;
                    else
                        return o1.getClass().getName().compareTo(o2.getClass().getName());
                }
                String name1;
                String name2;
                if (int1[0] == int2[0]) {
                    // Compare the names of the classes if the same interface
                    name1 = o1.getClass().getName();
                    name2 = o2.getClass().getName();
                } else {
                    // Compare the names of the interfaces if not the same
                    name1 = int1[0].getName(); // Only look at the first it implements
                    name2 = int2[0].getName();
                }
                return name1.compareTo(name2);

            }
        });
        return Arrays.asList(array);
    }

    public static HashMap<String, URLClassLoader> getPluginName2URLClassLoader() {
        return pluginName2URLClassLoader;
    }

    public static void setPluginName2URLClassLoader(HashMap<String, URLClassLoader> pluginClass2URLClassLoader) {
        pluginName2URLClassLoader = pluginClass2URLClassLoader;
    }

}
