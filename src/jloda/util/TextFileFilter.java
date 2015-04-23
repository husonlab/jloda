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
