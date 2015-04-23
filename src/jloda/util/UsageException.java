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

/**
 * @version $Id: UsageException.java,v 1.3 2006-06-06 18:56:04 huson Exp $
 *
 * Command line options usage exception
 *
 * @author Daniel Huson
 */
package jloda.util;

/**
 * Command line options usage exception
 */
public class UsageException extends Exception {
    /**
     * constructor of UsageException
     *
     * @param str String
     */
    public UsageException(String str) {
        super(str + ", use option '-h' for help");
    }
}
// EOF

