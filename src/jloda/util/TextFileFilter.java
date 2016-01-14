/**
 * TextFileFilter.java 
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
 * @author Daniel Huson
 *         text file filter
 *         12.03
 */

public class TextFileFilter extends FileFilterBase implements FilenameFilter {
    public TextFileFilter() {
        this(new String[0], false);
    }

    public TextFileFilter(String additionalSuffix) {
        this(additionalSuffix, false);
    }

    public TextFileFilter(String[] additionalSuffixes) {
        this(additionalSuffixes, false);
    }

    public TextFileFilter(String additionalSuffix, boolean allowGZip) {
        this(new String[]{additionalSuffix}, allowGZip);
    }

    public TextFileFilter(String[] additionalSuffixes, boolean allowGZip) {
        add("txt");
        add("text");
        for (String s : additionalSuffixes)
            add(s);
        setAllowGZipped(allowGZip);
    }

    /**
     * @return description of file matching the filter
     */
    public String getBriefDescription() {
        return "Text Files";
    }
}
