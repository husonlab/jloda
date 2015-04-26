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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Finds all classes in the named package, of the given type
 *
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
    public static List<Object> getInstances(String packageName, Class clazz) {
        return getInstances(new String[]{packageName}, clazz);
    }

    /**
     * get an instance of each class of the given type in the given packages
     *
     * @param packageNames
     * @param clazz
     * @return instances
     */
    public static List<Object> getInstances(String[] packageNames, Class clazz) {
        final List<Object> plugins = new LinkedList<>();

        final LinkedList<String> packageNameQueue = new LinkedList<>();
        packageNameQueue.addAll(Arrays.asList(packageNames));
        while (packageNameQueue.size() > 0) {
            String packageName = packageNameQueue.remove(0);
            // System.err.println("packageName: " + packageName);

            String[] resources = null;
            try {
                resources = Basic.fetchResources(packageName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (resources != null) {
                for (int i = 0; i != resources.length; ++i) {
                    //System.err.println("Resource: " + resources[i]);
                    if (resources[i].endsWith(".class")) {
                        try {
                            resources[i] = resources[i].substring(0, resources[i].length() - 6);
                            Class c = Basic.classForName(packageName.concat(".").concat(resources[i]));
                            if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && clazz.isAssignableFrom(c)) {
                                Object obj;
                                try {
                                    obj = c.newInstance();
                                    plugins.add(obj);
                                } catch (InstantiationException ex) {
                                    // continue; //Must be an abstract class
                                }
                            } else {
                                //  System.err.println("Skipping: " + c.getName());
                            }
                        } catch (Exception ex) {
                            // Basic.caught(ex);
                        }
                    } else {
                        try {
                            String newPackageName = resources[i];
                            if (!newPackageName.equals(packageName)) {
                                packageNameQueue.add(newPackageName);
                                // System.err.println("Adding package name: " + newPackageName);
                            }
                        } catch (Exception ex) {
                            // Basic.caught(ex);
                        }
                    }
                }
            }
        }
        return plugins;
    }

    /**
     * get an instance of each class of the given type in the given package, sorted
     *
     * @param packageName
     * @param type
     * @return instances
     */
    public static List getInstancesSorted(String packageName, Class type) {
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

