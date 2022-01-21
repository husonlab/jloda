/*
 * GFF3FileFilter.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.swing.util;

import jloda.util.FileUtils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * GFF file filter
 * Daniel Huson, 12.2017
 */
public class GFF3FileFilter extends FileFilterBase implements FilenameFilter {
    private boolean lookInside = true;

    public GFF3FileFilter(boolean allowGZip, boolean lookInside, String... additionalSuffixes) {
        this(allowGZip, additionalSuffixes);
        this.lookInside = lookInside;
    }

    public GFF3FileFilter(String... additionalSuffixes) {
        this(true, additionalSuffixes);
    }


    public GFF3FileFilter(boolean allowGZip, String... additionalSuffixes) {
        add("gff");
        add("gff3");
        for (String s : additionalSuffixes)
            add(s);
        setAllowGZipped(allowGZip);
    }

    public boolean accept(String fileName) {
        boolean suffixOk = super.accept(FileUtils.getFileNameWithoutZipOrGZipSuffix(fileName));
        if (suffixOk) {   // look inside the file
            if (lookInside) {
                final String[] lines = FileUtils.getFirstLinesFromFile(new File(fileName), 1);
                return lines != null && lines[0] != null && lines[0].startsWith("##gff-version 3");
            } else
                return true;
        } else
            return false;
    }

    /**
     * @return description of file matching the filter
     */
    public String getBriefDescription() {
        return "GFF3 Files";
    }

    public boolean isLookInside() {
        return lookInside;
    }

    public void setLookInside(boolean lookInside) {
        this.lookInside = lookInside;
    }
}
