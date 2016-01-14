/**
 * RTFFileFilter.java 
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * RTF file filter
 * daniel huson, 2015
 */
public class RTFFileFilter implements FilenameFilter {
    private static RTFFileFilter instance;

    /**
     * get an instance of this file filter
     *
     * @return instance
     */
    public static RTFFileFilter getInstance() {
        if (instance == null)
            instance = new RTFFileFilter();
        return instance;
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir  the directory in which the file was found.
     * @param name the name of the file.
     * @return <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
    @Override
    public boolean accept(File dir, String name) {
        FileInputIterator it;
        try {
            it = new FileInputIterator(new File(dir, name));
            try {
                if (it.next().startsWith("{\\rtf"))
                    return true;
            } finally {
                it.close();
            }
        } catch (IOException e) {
        }
        return false;
    }

    /**
     * returns all stripped lines from an rtf file
     *
     * @param file
     * @return stripped files
     */
    public static String[] getStrippedLines(File file) {
        if (getInstance().accept(file.getParentFile(), file.getName())) {
            try {
                List<String> lines = new LinkedList<>();
                try (FileInputIterator it = new FileInputIterator(file)) {
                    while (it.hasNext()) {
                        String aLine = it.next().replaceAll("\\{\\*?\\\\[^{}]+}|[{}]|\\\\\\n?[A-Za-z]+\\n?(?:-?\\d+)?[ ]?", "").replaceAll("\\\\", "").trim();
                        if (aLine.contains("Email:") && aLine.contains("mailto:"))
                            aLine = aLine.replaceAll(".*mailto:", "Email: ").replaceAll("\"", "").trim();
                        if (aLine.length() > 0)
                            lines.add(aLine);
                    }
                    return lines.toArray(new String[lines.size()]);
                }
            } catch (Exception e) {
            }
        }
        return new String[0];
    }
}
