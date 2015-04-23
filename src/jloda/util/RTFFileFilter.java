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
