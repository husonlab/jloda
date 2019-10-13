/*
 * FileFilterBase.java Copyright (C) 2019. Daniel H. Huson
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
    private final List<FileFilterBase> others = new LinkedList<>();
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
            if (first) {
            }
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
     * add another file filter
     *
     * @param fileFilter
     */
    public void add(FileFilterBase fileFilter) {
        if (!others.contains(fileFilter))
            others.add(fileFilter);
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

        for (FileFilterBase filter : others) {
            if (filter.accept(dir, name))
                return true;
        }

        return extensions.contains(".txt") && !file.getName().contains(".");
    }
}
