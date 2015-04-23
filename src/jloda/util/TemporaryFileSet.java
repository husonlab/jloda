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
