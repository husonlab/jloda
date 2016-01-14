/**
 * Edge.java 
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
 * @version $Id: Edge.java,v 1.17 2010-06-10 12:07:57 scornava Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;

import jloda.util.NotOwnerException;

/**
 * Edge class used by Graph class
 *
 * @author Daniel Huson, 2003
 */
public class Edge extends NodeEdge implements Comparable {
    /**
     * insert before reference edge
     */
    final public static int AFTER = 1;
    /**
     * insert after reference edge
     */
    final public static int BEFORE = -1;

    private Node source;   //Source vertex
    private Node target;   //Target vertex

    private Edge sNext;    //Next edge in list of edges incident with v
    private Edge sPrev;    //Previous edge in list of edges incident with v

    private Edge tNext;   //Next edge in list of edges incident with w
    private Edge tPrev;   //Previous edge in list of edges incident with w

    /**
     * Construct a new edge from v to w.
     *
     * @param G
     * @param v
     * @param w
     */
    public Edge(Graph G, Node v, Node w) throws IllegalSelfEdgeException {
        this(G, v, null, w, null, Edge.AFTER, Edge.AFTER, null);
    }

    /**
     * construct a new edge from v to w and set its info object. The next and previous edges
     * are set to Edge.AFTER.
     *
     * @param G
     * @param v
     * @param w
     * @param obj
     */
    public Edge(Graph G, Node v, Node w, Object obj) throws IllegalSelfEdgeException {
        this(G, v, null, w, null, Edge.AFTER, Edge.AFTER, obj);
    }

    /**
     * constructs an edge from v to w, inserting it before or after the two given reference edges.
     *
     * @param graph graph
     * @param v     source node
     * @param e_v   reference edge at source node
     * @param w     target node
     * @param e_w   reference edge at target node
     * @param dir_v place before or after source reference edge
     * @param dir_w place before or after target reference edge
     * @param obj   the info object
     */
    public Edge(final Graph graph, final Node v, final Edge e_v, final Node w, final Edge e_w, final int dir_v, final int dir_w, final Object obj) throws IllegalSelfEdgeException {
        if (v == w)
            throw new IllegalSelfEdgeException();
        graph.registerNewEdge(v, e_v, w, e_w, dir_v, dir_w, obj, this);
        getOwner().fireNewEdge(this);
        getOwner().fireGraphHasChanged();
    }

    /**
     * initialize this edge
     *
     * @param G     The graph
     * @param id    The id of the edge
     *              // [REMOVED] param prev0  The edge before this one in the list of all edges.
     * @param v     Source vertex
     * @param e_v
     * @param dir_v
     * @param w     Target Node
     * @param e_w
     * @param dir_w
     * @param obj   If e_v is null, then edge is inserted immediately after the last node in the list of
     *              edges incident with v (or before the first node if dir_v = BEFORE). Likewise for e_w.
     */
    void init(Graph G, int id, Node v, Edge e_v, int dir_v, Node w, Edge e_w, int dir_w, Object obj) throws NotOwnerException {

        //ToDo: [DAVE] removed the prev0 parameter here, as this will cause a bug if prev0 is not the final edge.
        super.init(G, G.lastEdge, null, id, obj);

        source = v;
        target = w;

        if (dir_v == AFTER) {
            if (e_v == null)
                e_v = source.getLastAdjacentEdge();
            if (e_v != null) {
                Edge ee = e_v.getNextIncidentTo(source);
                if (ee != null)
                    ee.setPrev(source, this);
                this.setPrev(source, e_v);
                e_v.setNext(source, this);
                this.setNext(source, ee);
            } else {
                this.setPrev(source, null);
                this.setNext(source, null);
            }
            if (source.getFirstAdjacentEdge() == null)
                source.setFirstAdjacentEdge(this);
            if (source.getLastAdjacentEdge() == e_v)
                source.setLastAdjacentEdge(this);
        } else // dir_v==before
        {
            if (e_v == null)
                e_v = source.getFirstAdjacentEdge();
            if (e_v != null) {
                Edge ee = e_v.getPrevIncidentTo(source);
                if (ee != null)
                    ee.setNext(source, this);
                this.setNext(source, e_v);
                e_v.setPrev(source, this);
                this.setPrev(source, ee);
            } else {
                this.setPrev(source, null);
                this.setNext(source, null);
            }
            if (source.getLastAdjacentEdge() == null)
                source.setLastAdjacentEdge(this);
            if (source.getFirstAdjacentEdge() == e_v)
                source.setFirstAdjacentEdge(this);
        }
        if (dir_w == AFTER) {
            if (e_w == null)
                e_w = target.getLastAdjacentEdge();
            if (e_w != null) {
                Edge ee = e_w.getNextIncidentTo(target);
                if (ee != null)
                    ee.setPrev(target, this);
                this.setPrev(target, e_w);
                e_w.setNext(target, this);
                this.setNext(target, ee);
            } else {
                this.setPrev(target, null);
                this.setNext(target, null);
            }
            if (target.getFirstAdjacentEdge() == null)
                target.setFirstAdjacentEdge(this);
            if (target.getLastAdjacentEdge() == e_w)
                target.setLastAdjacentEdge(this);
        } else // dir==before
        {
            if (e_w == null)
                e_w = target.getFirstAdjacentEdge();
            if (e_w != null) {
                Edge ee = e_w.getPrevIncidentTo(target);
                if (ee != null)
                    ee.setNext(target, this);
                this.setNext(target, e_w);
                e_w.setPrev(target, this);
                this.setPrev(target, ee);
            } else {
                this.setPrev(target, null);
                this.setNext(target, null);
            }
            if (target.getLastAdjacentEdge() == null)
                target.setLastAdjacentEdge(this);
            if (target.getFirstAdjacentEdge() == e_w)
                target.setFirstAdjacentEdge(this);
        }
    }


    /**
     * set the next Edge of the current edge
     *
     * @param v Node
     * @param f Edge is the next edge of e
     */
    void setNext(Node v, Edge f) throws NotOwnerException {
        checkOwner(v);
        if (f != null)
            checkOwner(f);
        if (source == v)
            this.sNext = f;
        else if (target == v)
            this.tNext = f;
    }

    /**
     * set the previous edge of the current edge
     *
     * @param v Node
     * @param f Edge is the previous edge of e
     */
    void setPrev(Node v, Edge f) throws NotOwnerException {
        checkOwner(v);
        if (f != null)
            checkOwner(f);
        if (source == v)
            this.sPrev = f;
        else if (target == v)
            this.tPrev = f;
    }

    /**
     * Get the next edge incident to v
     *
     * @param v Node
     * @return the next edge of e
     */
    public Edge getNextIncidentTo(Node v) throws NotOwnerException {
        checkOwner(v);
        if (source == v)
            return getSNext();
        else if (target == v)
            return getTNext();
        else
            return null;
    }

    /**
     * Get the previous edge incident to v
     *
     * @param v Node
     * @return the previous edge
     */
    public Edge getPrevIncidentTo(Node v) throws NotOwnerException {
        checkOwner(v);
        if (source == v)
            return getSPrev();
        else if (target == v)
            return getTPrev();
        else
            return null;
    }

    /**
     * Gets the opposite Node
     *
     * @param v Node
     * @return a Node the Opposite of the Node u
     */
    public Node getOpposite(Node v) throws NotOwnerException {
        checkOwner(v);
        if (v == source)
            return target;
        else if (v == target)
            return source;
        else
            return null;
    }

    /**
     * Produces a string representation
     *
     * @return string representation
     */
    public String toString() {
        StringBuilder buf = new StringBuilder("[" + String.valueOf(getId()) + "] [");
        if (getInfo() != null)
            buf.append(getInfo().toString());
        buf.append("]: ").append(source.getId()).append(" ").append(target.getId());
        if (isHidden())
            buf.append(" (hidden)");
        return buf.toString();
    }

    /**
     * remove this edge from the graph
     */
    public void deleteEdge() {
        final Graph graph = getOwner();
        graph.fireDeleteEdge(this);
        graph.unregisterEdge(this);

        if (source.getFirstAdjacentEdge() == this)
            source.setFirstAdjacentEdge(getNextIncidentTo(source));
        if (source.getLastAdjacentEdge() == this)
            source.setLastAdjacentEdge(this.getPrevIncidentTo(source));
        if (target.getFirstAdjacentEdge() == this)
            target.setFirstAdjacentEdge(this.getNextIncidentTo(target));
        if (target.getLastAdjacentEdge() == this)
            target.setLastAdjacentEdge(this.getPrevIncidentTo(target));

        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        if (sPrev != null)
            sPrev.setNext(source, sNext);
        if (sNext != null)
            sNext.setPrev(source, sPrev);
        if (tPrev != null)
            tPrev.setNext(target, tNext);
        if (tNext != null)
            tNext.setPrev(target, tPrev);

        setOwner(null);
        info = null;
        graph.fireGraphHasChanged();
    }

    /**
     * get next edge in list of all edges
     *
     * @return next edge in list of all edges
     */
    public Edge getNext() {
        Edge e = (Edge) next;
        while (e != null && e.isHidden())
            e = (Edge) e.next;
        return e;
    }

    /**
     * get previous edge in list of all edges
     *
     * @return previous edge in list of all edges
     */
    public Edge getPrev() {
        Edge e = (Edge) prev;
        while (e != null && e.isHidden())
            e = (Edge) e.prev;
        return e;
    }


    /**
     * Get the source node of this edge
     *
     * @return source
     */

    public Node getSource() {
        return source;
    }

    /**
     * Get the target node of this edge
     *
     * @return target
     */

    public Node getTarget() {
        return target;
    }

    /**
     * compares with another edge of the same graph
     *
     * @param o
     * @return -1, 1 or 0
     */
    public int compareTo(Object o) {
        final Edge e = (Edge) o;
        checkOwner(e);
        if (this.getId() < e.getId())
            return -1;
        else if (this.getId() > e.getId())
            return 1;
        else
            return 0;
    }

    /**
     * reverses the orientation of this edge by swapping source and target
     */
    public void reverse() {
        source.incrementInDegree();
        source.decrementOutDegree();
        target.decrementInDegree();
        target.incrementOutDegree();

        Node tmpNode = source;
        source = target;
        target = tmpNode;

        Edge tmpEdge = sNext;
        sNext = tNext;
        tNext = tmpEdge;

        tmpEdge = sPrev;
        sPrev = tPrev;
        tPrev = tmpEdge;

        getOwner().fireGraphHasChanged();
    }

    Edge getSPrev() {
        return sPrev;
    }

    Edge getSNext() {
        return sNext;
    }

    Edge getTPrev() {
        return tPrev;
    }

    Edge getTNext() {
        return tNext;
    }
}

// EOF
