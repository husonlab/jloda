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

package jloda.export;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Daniel Huson, Michael Schr�der
 * @version $Id: GraphicsFileFilters.java,v 1.4 2006-05-15 03:34:46 huson Exp $
 */
public class GraphicsFileFilters {


    static class AllTypesFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (
                        extension.equalsIgnoreCase("jpeg") ||
                                extension.equalsIgnoreCase("jpg") ||
                                extension.equalsIgnoreCase("eps") ||
                                extension.equalsIgnoreCase("svg") ||
                                extension.equalsIgnoreCase("gif") ||
                                extension.equalsIgnoreCase("png")) {
                    return true;
                }
            } else {
                return false;
            }

            return false;
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
                if (extension.equalsIgnoreCase("jpeg") ||
                        extension.equalsIgnoreCase("jpg")) {
                    return true;
                }
            } else {
                return false;
            }
            return false;
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
                if (extension.equalsIgnoreCase("eps"))
                    return true;
            } else {
                return false;
            }
            return false;
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
                if (extension.equalsIgnoreCase("svg"))
                    return true;
            } else {
                return false;
            }
            return false;
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
                if (extension.equalsIgnoreCase("gif"))
                    return true;
            } else {
                return false;
            }
            return false;
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
                if (extension.equalsIgnoreCase("png"))
                    return true;
            } else {
                return false;
            }
            return false;
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
