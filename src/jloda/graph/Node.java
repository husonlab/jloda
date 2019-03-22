/**
 * Node.java
 * Copyright (C) 2019 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.graph;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Node class used by Graph class
 * Daniel Huson, 2003
 */
public class Node extends NodeEdge implements Comparable<Node> {
    private Edge firstAdjacentEdge;
    private Edge lastAdjacentEdge;
    private int inDegree = 0;
    private int outDegree = 0;
    private Object data = null;

    /**
     * construct a new node for the given graph. The information in the node is replaced with obj. The node
     * is added to the end of the list of nodes. Any NewNode listeners are fired.
     *
     * @param G
     */
    public Node(Graph G) {
        super();
        G.registerNewNode(null, this);
        G.fireNewNode(this);
        G.fireGraphHasChanged();
    }

    /**
     * construct a new node for the given graph. The information in the node is replaced with obj. The node
     * is added to the end of the list of nodes. Any NewNode listeners are fired.
     *
     * @param G
     */
    public Node(Graph G, Object info) {
        super();
        G.registerNewNode(info, this);
        G.fireNewNode(this);
        G.fireGraphHasChanged();
    }

    /**
     * initalizes a new node in graph
     *
     * @param G    Graph
     * @param prev NodeEdge
     * @param next NodeEdge
     * @param id   int
     * @param info Object
     */
    void init(Graph G, Node prev, Node next, int id, Object info) {
        super.init(G, prev, next, id, info);
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
        buf.append("]:");
        for (Edge e = getFirstAdjacentEdge(); e != null; e = getNextAdjacentEdge(e))
            buf.append(" ").append(String.valueOf(e.getId()));
        return buf.toString();
    }

    /**
     * delete this node from graph. Any incident edges are deleted.
     */
    public void deleteNode() {
        getOwner().fireDeleteNode(this);
        getOwner().unregisterNode(this);
        while (firstAdjacentEdge != null)
            firstAdjacentEdge.deleteEdge();
        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        Graph G = getOwner();
        setOwner(null);
        info = null;
        data = null;
        G.fireGraphHasChanged();
    }

    /**
     * rearrange the adjacent edges
     *
     * @param newOrder
     */
    public void rearrangeAdjacentEdges(Collection<Edge> newOrder) {
        Edge[] array = new Edge[newOrder.size()];
        int i = 0;
        for (Edge e : newOrder)
            array[newOrder.size() - (++i)] = e;

        for (i = 0; i < array.length; i++) {
            Edge e = array[i];
            checkOwner(e);
            Edge pred = e.getPrevIncidentTo(this);
            Edge succ = e.getNextIncidentTo(this);
            if (pred != null) {
                pred.setNext(this, succ);
                if (succ != null) {
                    succ.setPrev(this, pred);
                } else {
                    this.lastAdjacentEdge = pred;
                }
                e.setPrev(this, null);
                this.firstAdjacentEdge.setPrev(this, e);
                e.setNext(this, this.firstAdjacentEdge);
                this.firstAdjacentEdge = e;
            }
        }
        getOwner().fireGraphHasChanged();
    }

    /**
     * reverse the order of the adjacent edges
     */
    public void reverseOrderAdjacentEdges() {
        List<Edge> order = new ArrayList<>();
        for (Edge e = getLastAdjacentEdge(); e != null; e = getPrevAdjacentEdge(e))
            order.add(e);
        rearrangeAdjacentEdges(order);
    }

    /**
     * rotate the order of the adjacent edges
     */
    public void rotateOrderAdjacentEdges() {
        List<Edge> order = new ArrayList<>();
        for (Edge e = getFirstAdjacentEdge(); e != null; e = getNextAdjacentEdge(e))
            order.add(e);
        Edge e = order.remove(0);
        order.add(e);
        rearrangeAdjacentEdges(order);
    }

    /**
     * get node on opposite end of edge
     *
     * @param e
     * @return node
     */
    public Node getOpposite(Edge e) throws NotOwnerException {
        checkOwner(e);
        return e.getOpposite(this);
    }

    /**
     * get first adjacent edge
     *
     * @return first adjacent edge
     */
    public Edge getFirstAdjacentEdge() {
        return firstAdjacentEdge;
    }

    /**
     * get last adjacent edge
     *
     * @return last adjacent edge
     */
    public Edge getLastAdjacentEdge() {
        return lastAdjacentEdge;
    }

    /**
     * get next adjacent edge
     *
     * @param e
     * @return next adjacent edge
     */
    public Edge getNextAdjacentEdge(Edge e) {
        checkOwner(e);
        if (e.getSource() == this)
            return e.getSNext();
        else if (e.getTarget() == this)
            return e.getTNext();
        else
            return null;
    }

    /**
     * get previous adjacent edge
     *
     * @param e
     * @return previous adjacent edge or null
     */
    public Edge getPrevAdjacentEdge(Edge e) {
        checkOwner(e);
        if (e.getSource() == this)
            return e.getSPrev();
        else if (e.getTarget() == this)
            return e.getTPrev();
        else
            return null;
    }

    /**
     * get next adjacent edge in cyclic ordering
     *
     * @param e
     * @return next adjacent edge in cyclic ordering
     */
    public Edge getNextAdjacentEdgeCyclic(Edge e) {
        checkOwner(e);
        Edge f = getNextAdjacentEdge(e);
        if (f != null)
            return f;
        else
            return getFirstAdjacentEdge();
    }

    /**
     * get previous adjacent edge in cyclic ordering
     *
     * @param e
     * @return previous adjacent edge in cyclic ordering
     */
    public Edge getPrevAdjacentEdgeCyclic(Edge e) {
        checkOwner(e);
        Edge f = getPrevAdjacentEdge(e);
        if (f != null)
            return f;
        else
            return lastAdjacentEdge;
    }

    /**
     * gets the first out edge
     *
     * @return first out edge or null
     */
    public Edge getFirstOutEdge() {
        for (Edge e = getFirstAdjacentEdge(); e != null; e = getNextAdjacentEdge(e))
            if (e.getSource() == this)
                return e;
        return null;
    }

    /**
     * gets the next out edge
     *
     * @param e
     * @return next out edge or null
     */
    public Edge getNextOutEdge(Edge e) {
        for (e = getNextAdjacentEdge(e); e != null; e = getNextAdjacentEdge(e))
            if (e.getSource() == this)
                return e;
        return null;
    }

    /**
     * gets the last out edge
     *
     * @return last out edge or null
     */
    public Edge getLastOutEdge() {
        for (Edge e = getLastAdjacentEdge(); e != null; e = getPrevAdjacentEdge(e))
            if (e.getSource() == this)
                return e;
        return null;
    }

    /**
     * gets the previous out edge
     *
     * @param e
     * @return previous out edge or null
     */
    public Edge getPrevOutEdge(Edge e) {
        for (e = getPrevAdjacentEdge(e); e != null; e = getPrevAdjacentEdge(e))
            if (e.getSource() == this)
                return e;
        return null;
    }

    /**
     * gets the first in edge
     *
     * @return first in edge or null
     */
    public Edge getFirstInEdge() {
        for (Edge e = getFirstAdjacentEdge(); e != null; e = getNextAdjacentEdge(e))
            if (e.getTarget() == this)
                return e;
        return null;
    }

    /**
     * gets the next in edge
     *
     * @param e
     * @return next in edge or null
     */
    public Edge getNextInEdge(Edge e) {
        for (e = getNextAdjacentEdge(e); e != null; e = getNextAdjacentEdge(e))
            if (e.getTarget() == this)
                return e;
        return null;
    }

    /**
     * gets the last in edge
     *
     * @return last in edge or null
     */
    public Edge getLastInEdge() {
        for (Edge e = getLastAdjacentEdge(); e != null; e = getPrevAdjacentEdge(e))
            if (e.getTarget() == this)
                return e;
        return null;
    }

    /**
     * gets the previous in edge
     *
     * @param e
     * @return previous in edge or null
     */
    public Edge getPrevInEdge(Edge e) {
        for (e = getPrevAdjacentEdge(e); e != null; e = getPrevAdjacentEdge(e))
            if (e.getTarget() == this)
                return e;
        return null;
    }

    /**
     * get common edge between this node and w, or null
     *
     * @param w
     * @return common edge between this node and w, or null
     */
    public Edge getCommonEdge(Node w) {
        checkOwner(w);
        for (Edge e = getFirstAdjacentEdge(); e != null; e = getNextAdjacentEdge(e)) {
            if (getOpposite(e) == w)
                return e;
        }
        return null;
    }

    /**
     * get  edge from this node to w, or null
     *
     * @param w
     * @return common edge from this node to w, or null
     */
    public Edge getEdgeTo(Node w) {
        checkOwner(w);
        for (Edge e : outEdges()) {
            if (getOpposite(e) == w)
                return e;
        }
        return null;
    }

    /**
     * get  edge to this node from w, or null
     *
     * @param w
     * @return common edge from this node to w, or null
     */
    public Edge getEdgeFrom(Node w) {
        checkOwner(w);
        for (Edge e : inEdges()) {
            if (getOpposite(e) == w)
                return e;
        }
        return null;
    }

    /**
     * get next node in list of all node
     *
     * @return next node
     */
    public Node getNext() {
        return (Node) next;
    }

    /**
     * get previous node
     *
     * @return previous node
     */
    public Node getPrev() {
        return (Node) prev;
    }

    /**
     * get the degree of this node
     *
     * @return degree of this node
     */
    public int getDegree() {
        return inDegree + outDegree;
    }

    /**
     * get the indegree of this node
     *
     * @return indegree of this node
     */
    public int getInDegree() {
        return inDegree;
    }

    /**
     * get the out degree of this node
     *
     * @return out degree of this node
     */
    public int getOutDegree() {
        return outDegree;
    }


    /**
     * get a directed edge from this node to w, or null
     *
     * @param w
     * @return directed edge from this node to w, or null
     */
    public Edge findDirectedEdge(Node w) throws NotOwnerException {
        checkOwner(w);
        for (Edge e : outEdges()) {
            if (getOpposite(e) == w)
                return e;
        }
        return null;
    }

    /**
     * is this node adjacent to w
     *
     * @param w
     * @return adjacent
     */
    public boolean isAdjacent(Node w) throws NotOwnerException {
        checkOwner(w);
        for (Node v : adjacentNodes()) {
            if (v == w)
                return true;
        }
        return false;
    }

    /**
     * compares with another node of the same graph
     *
     * @param v
     * @return -1, 1 or 0
     */
    public int compareTo(Node v) {
        checkOwner(v);
        return Integer.compare(this.getId(), v.getId());
    }

    void setFirstAdjacentEdge(Edge e) {
        firstAdjacentEdge = e;
    }

    void setLastAdjacentEdge(Edge e) {
        lastAdjacentEdge = e;
    }

    void incrementInDegree() {
        inDegree++;
    }

    void incrementOutDegree() {
        outDegree++;
    }

    void decrementInDegree() {
        inDegree--;
    }

    void decrementOutDegree() {
        outDegree--;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Iterable<Edge> outEdges() {
        return () -> new Iterator<Edge>() {
            Edge e = getFirstOutEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
                Edge result = e;
                e = getNextOutEdge(e);
                return result;
            }
        };
    }

    public Iterable<Edge> inEdges() {
        return () -> new Iterator<Edge>() {
            Edge e = getFirstInEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
                Edge result = e;
                e = getNextInEdge(e);
                return result;
            }
        };
    }

    public Iterable<Edge> adjacentEdges() {
        return () -> new Iterator<Edge>() {
            Edge e = getFirstAdjacentEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
                Edge result = e;
                e = getNextAdjacentEdge(e);
                return result;
            }
        };
    }

    public Iterable<Node> adjacentNodes() {
        return () -> new Iterator<Node>() {
            private Edge e = getFirstAdjacentEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Node next() {
                final Node result = e.getOpposite(Node.this);
                e = getNextAdjacentEdge(e);
                return result;
            }
        };
    }

    public Iterable<Node> children() {
        return () -> new Iterator<Node>() {
            private Edge e = getFirstOutEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Node next() {
                final Node result = e.getTarget();
                e = getNextOutEdge(e);
                return result;
            }
        };
    }

    public Iterable<Node> parents() {
        return () -> new Iterator<Node>() {
            private Edge e = getFirstInEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Node next() {
                final Node result = e.getSource();
                e = getNextInEdge(e);
                return result;
            }
        };
    }

    public boolean isLeaf() {
        return outDegree == 0;
    }

    public void deleteAllAdjacentEdges() {
        for (Edge e : adjacentEdges())
            e.deleteEdge();
    }
}

// EOF

