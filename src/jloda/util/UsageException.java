/**
 * UsageException.java 
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

