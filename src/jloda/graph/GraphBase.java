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
 * @version $Id: GraphBase.java,v 1.6 2005-01-30 13:00:39 huson Exp $
 *
 * Base class for all graph related stuff.
 *
 * @author Daniel Huson
 */
package jloda.graph;

import jloda.util.NotOwnerException;

public class GraphBase {
    private Graph owner;

    /**
     * Sets the owner.
     *
     * @param G Graph
     */
    void setOwner(Graph G) {
        owner = G;
    }

    /**
     * Returns the owning graph.
     *
     * @return owner a Graph
     */
    public Graph getOwner() {
        return owner;
    }

    /**
     * If this and obj do not have the same graph, throw NotOwnerException
     *
     * @param obj GraphBase
     */
    public void checkOwner(GraphBase obj) {
        if (obj == null)
            throw new NotOwnerException("object is null");
        if (obj.owner == null)
            throw new NotOwnerException("object's owner is null");
        if (owner == null)
            throw new NotOwnerException("reference's owner is null");
        if (owner != obj.owner) {
            throw new NotOwnerException("wrong owner");
        }
    }
}

// EOF
