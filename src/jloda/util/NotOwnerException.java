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

