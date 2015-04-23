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

package jloda.phylo;
/**
 * @version $Id: TreeParseException.java,v 1.4 2007-01-03 06:32:45 huson Exp $
 *
 * Expections for all of jloda
 *
 * @author Daniel Huson
 */


/**
 * Error parsing phylogenetic tree in bracket format.
 */
public class TreeParseException extends Exception {
    /**
     * Constructor of TreeParseexception
     *
     * @param str String
     */
    public TreeParseException(String str) {
        super(str);
    }
}
// EOF

