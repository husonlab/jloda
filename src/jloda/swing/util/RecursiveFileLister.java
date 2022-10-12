/*
 * RecursiveFileLister.java Copyright (C) 2022. Daniel H. Huson
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
 *
 */

package jloda.swing.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * recursively list all files with the given name or below, satisfying one of the provided file filters
 * Daniel Huson, 9.2017
 */
public class RecursiveFileLister {
    /**
     * apply
     *
	 */
    public static List<String> apply(String name, FileFilterBase... fileFilters) {
        final ArrayList<String> list = new ArrayList<>();
        applyRec(new File(name), list, fileFilters);
        return list;
    }

    /**
     * recursively does the work
     *
	 */
    private static void applyRec(File file, ArrayList<String> list, FileFilterBase... fileFilters) {
        if (file.isFile()) {
            if (fileFilters.length == 0) {
                list.add(file.getPath());
            } else {
                for (FileFilterBase fileFilter : fileFilters) {
                    if (fileFilter.accept(file)) {
                        list.add(file.getPath());
                        return;
                    }
                }
            }
        } else if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files != null) {
                for (File fileBelow : files) {
                    applyRec(fileBelow, list, fileFilters);
                }
            }
        }
    }
}
