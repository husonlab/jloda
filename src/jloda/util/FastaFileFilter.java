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

/**
 * The fastA file filter
 * Daniel Huson         2.2006
 */
public class FastaFileFilter extends FileFilterBase implements FilenameFilter {
    private static FastaFileFilter instance;

    /**
     * gets an instance
     *
     * @return instance
     */
    public static FastaFileFilter getInstance() {
        if (instance == null) {
            instance = new FastaFileFilter();
            instance.setAllowGZipped(true);
            instance.setAllowZipped(true);
        }
        return instance;
    }


    /**
     * constructor
     */
    public FastaFileFilter() {
        add(".dna");
        add(".fa");
        add(".faa");
        add(".fna");
        add(".fasta");
    }

    /**
     * @return description of file matching the filter
     */
    public String getBriefDescription() {
        return "Sequence files in FastA format";
    }

    /**
     * does this look like a FastA file name?
     *
     * @param fileName
     * @return true, if fastA file name
     */
    public static boolean accept(String fileName, boolean allowGZipped) {
        final FastaFileFilter fastaFileFilter = (new FastaFileFilter());
        fastaFileFilter.setAllowGZipped(allowGZipped);
        return fastaFileFilter.accept(new File(fileName));
    }
}
