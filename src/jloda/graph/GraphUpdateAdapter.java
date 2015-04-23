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
 *@version $Id: GraphUpdateAdapter.java,v 1.4 2005-01-07 14:23:05 huson Exp $
 *
 *@author Daniel Huson
 * 11.02
 */
package jloda.graph;

/** Extend this to get a GraphUpdateListener
 */
public class GraphUpdateAdapter implements GraphUpdateListener {
    /** A node has been created
     *@param v the new node
     */
    public void newNode(Node v) {
    }

    /** A node is about to be deleted
     *@param v the node that will be deleted
     */
    public void deleteNode(Node v) {
    }

    /** An edge has been created
     *@param e the new edge
     */
    public void newEdge(Edge e) {
    }

    /** An edge is about to be deleted
     *@param e the edge that will be deleted
     */
    public void deleteEdge(Edge e) {
    }

    /** The graph has changed.
     * This method is called after one of the above specific methods has be
     * called
     */
    public void graphHasChanged() {
    }

    /** (Partial) graph was read from Reader
     *@param nodes the new nodes
     *@param edges the new edges
     */
    public void graphWasRead(NodeSet nodes, EdgeSet edges) {
    }
}
