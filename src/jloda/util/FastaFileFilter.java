/**
 * FastaFileFilter.java 
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
