/**
 * NotOwnerException.java 
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
 * @version $Id: NotOwnerException.java,v 1.7 2006-06-06 18:56:03 huson Exp $
 *
 * Exceptions for all of jloda
 *
 * @author Daniel Huson
 */

package jloda.util;
//import java.lang.Exception;

/**
 * This exception indicates that a given node or edge is not owned by
 * the given Graph, NodeArray or EdgeArray.
 */
public class NotOwnerException extends RuntimeException {
    final Object obj;

    /**
     * Constructor of NotOwnerException
     *
     * @param obj Object
     */
    public NotOwnerException(Object obj) {
        super("" + obj);
        this.obj = obj;
    }

    /**
     * Gets the object
     *
     * @return the Object
     */
    public Object getObject() {
        return obj;
    }
}
// EOF

