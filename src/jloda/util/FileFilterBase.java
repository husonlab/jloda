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

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

/**
 * base class for file filters
 * Daniel Huson, 11.2008
 */
public abstract class FileFilterBase extends FileFilter implements FilenameFilter {
    private final List<String> extensions = new LinkedList<>();
    private boolean allowGZipped = false;
    private boolean allowZipped = false;

    public boolean isAllowGZipped() {
        return allowGZipped;
    }

    public void setAllowGZipped(boolean allowGZipped) {
        this.allowGZipped = allowGZipped;
    }

    public boolean isAllowZipped() {
        return allowZipped;
    }

    public void setAllowZipped(boolean allowZipped) {
        this.allowZipped = allowZipped;
    }

    /**
     * set brief description (without list of extensions
     *
     * @return
     */
    abstract public String getBriefDescription();

    /**
     * gets the list of file extensions
     *
     * @return file extensions
     */
    public List<String> getFileExtensions() {
        return extensions;
    }

    /**
     * @return description of file matching the filter
     */

    /**
     * @return description of file matching the filter
     */
    public String getDescription() {
        StringBuilder buf = new StringBuilder();
        buf.append(getBriefDescription()).append(" (extension: ");
        boolean first = true;
        for (String ex : getFileExtensions()) {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(ex);
        }
        if (allowGZipped) {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(".gz");
        }
        if (allowZipped) {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(".zip");
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * add another possible extension
     *
     * @param extension
     */
    public void add(String extension) {
        if (!extension.startsWith("."))
            extension = "." + extension;
        if (!extensions.contains(extension))
            extensions.add(extension);
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param fileName
     * @return
     */
    public boolean accept(String fileName) {
        return accept(null, fileName);
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param file
     * @return true if acceptable
     */
    public boolean accept(File file) {
        return accept(file.getPath());
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir  the directory in which the file was found (or null)
     * @param name the name of the file (or null)
     * @return <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
    @Override
    public boolean accept(File dir, String name) {
        if (dir == null && name == null)
            return false;
        final File file;
        if (dir == null)
            file = new File(name);
        else if (name == null)
            file = dir;
        else
            file = new File(dir, name);

        if (file.isDirectory())
            return true;
        for (String extension : extensions) {
            if (file.getName().endsWith(extension) || isAllowGZipped() && file.getName().endsWith(extension + ".gz") || isAllowZipped() && file.getName().endsWith(extension + ".zip"))
                return true;
        }
        return false;
    }
}
