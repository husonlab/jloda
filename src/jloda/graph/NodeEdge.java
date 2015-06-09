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
 * @version $Id:
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;

/**
 * NodeEdge: util class for both Node and Edge
 * Daniel Huson, 2003
 */

public class NodeEdge extends GraphBase {
    private final static int HIDDEN_MASK = (1 << 31);
    private final static int ID_MASK = ~HIDDEN_MASK;
    Object info;
    private int id;
    NodeEdge prev;
    NodeEdge next;

    /**
     * make an empty object
     */
    NodeEdge() {
    }

    /**
     * initialize
     *
     * @param G    Graph
     * @param prev NodeEdge
     * @param next NodeEdge
     * @param id   int
     * @param info Object
     */
    void init(Graph G, NodeEdge prev, NodeEdge next, int id, Object info) {
        setOwner(G);
        this.prev = prev;
        this.next = next;
        setId(id);
        if (info != null)
            setInfo(info);
    }

    /**
     * Get the associated info object
     *
     * @return info object
     */
    public Object getInfo() {
        return info;
    }

    /**
     * Set  the info   object
     *
     * @param info info object
     */
    public void setInfo(Object info) {
        this.info = info;
    }

    /**
     * Get the hash code of this object
     *
     * @return hash code
     */
    public int hashCode() {
        return id;
    }

    /**
     * Get the id
     *
     * @return id
     */
    public int getId() {
        return id & ID_MASK;
    }

    /**
     * sets the id
     *
     * @param id
     */
    void setId(int id) {
        this.id = id & ID_MASK;
    }

    /**
     * is this node hidden? If hidden, this node will not be considered when using an iteration
     *
     * @return hidden
     */
    public boolean isHidden() {
        return (id & HIDDEN_MASK) == HIDDEN_MASK;
    }

    /**
     * set the hidden state of this node
     *
     * @param hidden
     */
    void setHidden(boolean hidden) {
        if (hidden)
            id |= HIDDEN_MASK;
        else
            id &= (~HIDDEN_MASK);
    }
}

// EOF
