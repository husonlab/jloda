/*
 * NodeEdge.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph;

/**
 * NodeEdge: base class for both Node and Edge
 * Daniel Huson, 2003
 */

abstract class NodeEdge extends GraphBase {
    private final static int HIDDEN_MASK = (1 << 31);
    private final static int ID_MASK = ~HIDDEN_MASK;
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
     * @param graph Graph
     * @param prev  NodeEdge
     * @param next  NodeEdge
     * @param id    int
     * @param info  Object
     */
    void init(Graph graph, NodeEdge prev, NodeEdge next, int id, Object info) {
        setOwner(graph);
        this.prev = prev;
        this.next = next;
        setId(id);
        if (info != null)
            setInfo(info);
    }

    abstract void setInfo(Object info);

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
	 */
    void setId(int id) {
        this.id = id & ID_MASK;
    }

    /**
     * is this node hidden? If hidden, this node or edge will not be considered when using an iteration
     *
     * @return hidden
     */
    public boolean isHidden() {
        return (id & HIDDEN_MASK) == HIDDEN_MASK;
    }

    /**
     * set the hidden state of this node
     *
	 */
    void setHidden(boolean hidden) {
        if (hidden)
            id |= HIDDEN_MASK;
        else
            id &= (~HIDDEN_MASK);
    }
}

// EOF
