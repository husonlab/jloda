/**
 * TemporaryFileSet.java 
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * provides a set of temporary files
 * Daniel Huson, 6.2010
 */
public class TemporaryFileSet {
    private final String dir = System.getProperty("user.home") + "/tmp";
    private final long fileSetId = (new Date()).getTime();
    private final Set<String> fileNames = new HashSet<>();


    public TemporaryFileSet() {
        File tmpDir = new File(dir);
        if (!tmpDir.exists()) {
            System.err.println("Creating temporary directory: " + dir);
            tmpDir.mkdir();
        }
    }

    /**
     * returns the path name of a temporary file. Path names of different files sets are unique
     *
     * @param name
     * @param suffix
     * @return path name
     */
    public String getTemporaryFile(String name, String suffix) {
        File file = new File(dir, fileSetId + name + suffix);
        file.deleteOnExit();
        fileNames.add(file.getPath());
        System.err.println("Temp file: " + file.getPath());
        return file.getPath();
    }

    /**
     * delete all files in this file set
     */
    public void deleteAllFiles() {
        for (String fileName : fileNames) {
            File file = new File(fileName);
            if (file.exists() && !file.delete())
                System.err.println("Warning: failed to delete temporary file: " + file.getPath());
        }
    }
}
