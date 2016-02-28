/**
 * BlastFileFilter.java 
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

import java.io.FilenameFilter;

/**
 * The blast file filter
 * Daniel Huson         2.2006
 */
public class BlastFileFilter extends FileFilterBase implements FilenameFilter {
    /**
     * constructor
     */
    public BlastFileFilter() {
        add("blast");
        add("blastx");
        add("blastn");
        add("blastp");
        add("blastout");
        add("blastxml");
        add("tab");
        add("blasttab");
        add("txt");
        add("xml");
    }

    /**
     * @return description of file matching the filter
     */
    public String getBriefDescription() {
        return "BLAST files";
    }
}
