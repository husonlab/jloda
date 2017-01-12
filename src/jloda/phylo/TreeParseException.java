/**
 * TreeParseException.java 
 * Copyright (C) 2017 Daniel H. Huson
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

