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

package jloda.graph;

/**
 * @version $Id: NodeEdgeEnumeration.java,v 1.4 2005-01-07 14:23:05 huson Exp $
 *
 * @author Daniel Huson
 *
 */


/**
 * NodeEdgeEnumeration implements a Enumeration for nodes and edges
 */

class NodeEdgeItem {
    /**Constructor of NodeEdgeItem
     * @param ne0 NodeEdge
     * @param next0 NodeEdgeItem
     */
    NodeEdgeItem(NodeEdge ne0, NodeEdgeItem next0) {
        ne = ne0;
        next = next0;
    }

    final NodeEdge ne;
    final NodeEdgeItem next;
}

// EOF
