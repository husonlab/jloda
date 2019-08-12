/*
 * GraphicsFileFilters.java Copyright (C) 2019. Daniel H. Huson
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
 */

package jloda.swing.export;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Daniel Huson, Michael Schroeder
 */
public class GraphicsFileFilters {


    static class AllTypesFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                return extension.equalsIgnoreCase("jpeg") ||
                        extension.equalsIgnoreCase("jpg") ||
                        extension.equalsIgnoreCase("eps") ||
                        extension.equalsIgnoreCase("svg") ||
                        extension.equalsIgnoreCase("gif") ||
                        extension.equalsIgnoreCase("png");
            } else {
                return false;
            }

        }

        public String getDescription() {
            return "supported image file types (*.jpg,*.eps,*.svg,*.gif,*.png)";
        }
    }

    static class JpgFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                return extension.equalsIgnoreCase("jpeg") ||
                        extension.equalsIgnoreCase("jpg");
            } else {
                return false;
            }
        }

        public String getDescription() {
            return "JPG";
        }

        public String getString() {
            return getDescription();
        }
    }

    static class EpsFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = getExtension(f);
            if (extension != null) {
                return extension.equalsIgnoreCase("eps");
            } else {
                return false;
            }
        }

        public String getDescription() {
            return "Encapsulated Postscript (*.eps)";
        }

        public String getString() {
            return getDescription();
        }
    }

    static class SvgFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = getExtension(f);
            if (extension != null) {
                return extension.equalsIgnoreCase("svg");
            } else {
                return false;
            }
        }

        public String getDescription() {
            return "Scalable Vector Graphics (*.svg)";
        }

        public String getString() {
            return getDescription();
        }
    }

    static class GifFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = getExtension(f);
            if (extension != null) {
                return extension.equalsIgnoreCase("gif");
            } else {
                return false;
            }
        }

        public String getDescription() {
            return "GIF";
        }

        public String getString() {
            return getDescription();
        }
    }

    static class PngFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = getExtension(f);
            if (extension != null) {
                return extension.equalsIgnoreCase("png");
            } else {
                return false;
            }
        }

        public String getDescription() {
            return "PNG";
        }

        public String getString() {
            return getDescription();
        }
    }


    /**
     * returns the extension of a given file.
     *
     * @param f the file
     * @return the file extension of <code>f</code>
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1);
        }
        return ext;
    }
}
