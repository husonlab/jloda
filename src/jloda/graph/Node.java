/*
 * Node.java Copyright (C) 2022 Daniel H. Huson
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Node class used by Graph class
 * Daniel Huson, 2003
 */
public class Node extends NodeEdge implements Comparable<Node> {
    private Edge firstAdjacentEdge;
    private Edge lastAdjacentEdge;
    private int inDegree = 0;
    private int outDegree = 0;

    /**
     * construct a new node for the given graph. The information in the node is replaced with obj. The node
     * is added to the end of the list of nodes. Any NewNode listeners are fired.
     *
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
	 */
    public Node(Graph G, Object info) {
        super();
        G.registerNewNode(info, this);
        G.fireNewNode(this);
        G.fireGraphHasChanged();
    }

    /**
     * initializes a new node in graph
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
		StringBuilder buf = new StringBuilder("[" + getId() + "]");
		if (getInfo() != null)
			buf.append(" [").append(getInfo().toString()).append("]");
		if (getData() != null)
			buf.append(" [").append(getData().toString()).append("]");
		buf.append(":");
		for (Edge e = getFirstAdjacentEdge(); e != null; e = getNextAdjacentEdge(e))
			buf.append(" ").append(e.getId());
		return buf.toString();
	}

    /**
     * delete this node from graph. Any incident adjacentEdges are deleted.
     */
    public void deleteNode() {
        var graph = getOwner();
        graph.fireDeleteNode(this);
        graph.unregisterNode(this);
        while (firstAdjacentEdge != null)
            firstAdjacentEdge.deleteEdge();
        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        setOwner(null);
        graph.fireGraphHasChanged();
    }

    /**
     * rearrange the adjacent edge
     *
     * @param newOrder new order in which edges should occur
     */
    public void rearrangeAdjacentEdges(Collection<Edge> newOrder) {
        final var array = new Edge[newOrder.size()];
        {
            var i = 0;
            for (var e : newOrder)
                array[newOrder.size() - (++i)] = e;
        }

        for (var e : array) {
            var pred = e.getPrevIncidentTo(this);
            var succ = e.getNextIncidentTo(this);
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
     * reverse the order of the adjacent adjacentEdges
     */
    public void reverseOrderAdjacentEdges() {
        var order = new ArrayList<Edge>();
        for (var e = getLastAdjacentEdge(); e != null; e = getPrevAdjacentEdge(e))
            order.add(e);
        rearrangeAdjacentEdges(order);
    }

    /**
     * rotate the order of the adjacent adjacentEdges
     */
    public void rotateOrderAdjacentEdges() {
        var order = new ArrayList<Edge>();
        for (var e = getFirstAdjacentEdge(); e != null; e = getNextAdjacentEdge(e))
            order.add(e);
        var e = order.remove(0);
        order.add(e);
        rearrangeAdjacentEdges(order);
    }

    /**
     * get node on opposite end of edge
     *
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

    public boolean isAdjacent(Node v) {
        for (var e : adjacentEdges()) {
            if (e.getOpposite(this) == v)
                return true;
        }
        return false;
    }


    public boolean isDoubleAdjacent(Node v) {
        var foundOne=false;
        for (var e : adjacentEdges()) {
            if (e.getOpposite(this) == v) {
                if(!foundOne)
                    foundOne=true;
                else
                    return true;
            }
        }
        return false;
    }


    /**
     * get next adjacent edge
     *
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
     * @return next adjacent edge in cyclic ordering
     */
    public Edge getNextAdjacentEdgeCyclic(Edge e) {
        checkOwner(e);
        var f = getNextAdjacentEdge(e);
        if (f != null)
            return f;
        else
            return getFirstAdjacentEdge();
    }

    /**
     * get previous adjacent edge in cyclic ordering
     *
     * @return previous adjacent edge in cyclic ordering
     */
    public Edge getPrevAdjacentEdgeCyclic(Edge e) {
        checkOwner(e);
        var f = getPrevAdjacentEdge(e);
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
        for (var e = getFirstAdjacentEdge(); e != null; e = getNextAdjacentEdge(e))
            if (e.getSource() == this)
                return e;
        return null;
    }

    /**
     * gets the next out edge
     *
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
        for (var e = getFirstAdjacentEdge(); e != null; e = getNextAdjacentEdge(e))
            if (e.getTarget() == this)
                return e;
        return null;
    }

    /**
     * gets the next in edge
     *
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
        for (var e = getLastAdjacentEdge(); e != null; e = getPrevAdjacentEdge(e))
            if (e.getTarget() == this)
                return e;
        return null;
    }

    /**
     * gets the previous in edge
     *
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
     * @return common edge between this node and w, or null
     */
    public Edge getCommonEdge(Node w) {
        checkOwner(w);
        for(var e:adjacentEdges()) {
            if (getOpposite(e) == w)
                return e;
        }
        return null;
    }

    /**
     * get  edge from this node to w, or null
     *
     * @return common edge from this node to w, or null
     */
    public Edge getEdgeTo(Node w) {
        checkOwner(w);
        for (var e : outEdges()) {
            if (getOpposite(e) == w)
                return e;
        }
        return null;
    }

    /**
     * get  edge to this node from w, or null
     *
     * @return common edge from this node to w, or null
     */
    public Edge getEdgeFrom(Node w) {
        checkOwner(w);
        for (var e : inEdges()) {
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
     * get the in degree of this node
     *
     * @return in degree of this node
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
     * @return directed edge from this node to w, or null
     */
    public Edge findDirectedEdge(Node w) throws NotOwnerException {
        checkOwner(w);
        for (var e : outEdges()) {
            if (getOpposite(e) == w)
                return e;
        }
        return null;
    }

    /**
     * compares with another node of the same graph
     *
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


    public Iterable<Edge> outEdges() {
        return () -> new Iterator<>() {
            Edge e = getFirstOutEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
                var result = e;
                e = getNextOutEdge(e);
                return result;
            }
        };
    }

    public Stream<Edge> outEdgesStream(boolean parallel) {
        return StreamSupport.stream(outEdges().spliterator(), parallel);
    }

    public Iterable<Edge> inEdges() {
        return () -> new Iterator<>() {
            Edge e = getFirstInEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
                var result = e;
                e = getNextInEdge(e);
                return result;
            }
        };
    }

    public Stream<Edge> inEdgesStream(boolean parallel) {
        return StreamSupport.stream(inEdges().spliterator(), parallel);
    }


    public Iterable<Edge> adjacentEdges() {
        return () -> new Iterator<>() {
            Edge e = getFirstAdjacentEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
                var result = e;
                e = getNextAdjacentEdge(e);
                return result;
            }
        };
    }

    public Stream<Edge> adjacentEdgesStream(boolean parallel) {
        return StreamSupport.stream(adjacentEdges().spliterator(), parallel);
    }

    public Iterable<Node> adjacentNodes() {
        return () -> new Iterator<>() {
            private Edge e = getFirstAdjacentEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Node next() {
                final var result = e.getOpposite(Node.this);
                e = getNextAdjacentEdge(e);
                return result;
            }
        };
    }

    public Stream<Node> adjacentNodeStream(boolean parallel) {
        return StreamSupport.stream(adjacentNodes().spliterator(), parallel);
    }

    public Iterable<Node> children() {
        return () -> new Iterator<>() {
            private Edge e = getFirstOutEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

			@Override
			public Node next() {
                final var result = e.getTarget();
				e = getNextOutEdge(e);
				return result;
			}
		};
	}

	public Stream<Node> childrenStream() {
		return StreamSupport.stream(children().spliterator(), false);
	}

	public Stream<Node> childrenStream(boolean parallel) {
		return StreamSupport.stream(children().spliterator(), parallel);
	}

	public Iterable<Node> parents() {
		return () -> new Iterator<>() {
			private Edge e = getFirstInEdge();

			@Override
			public boolean hasNext() {
                return e != null;
            }

            @Override
            public Node next() {
                final var result = e.getSource();
                e = getNextInEdge(e);
                return result;
            }
        };
    }


    public Stream<Node> parentsStream(boolean parallel) {
        return StreamSupport.stream(parents().spliterator(), parallel);
    }

    public Node getParent() {
        if (inDegree > 0)
            return getFirstInEdge().getSource();
        else
            return null;
    }

    public boolean isLeaf() {
        return outDegree == 0;
    }

    public void deleteAllAdjacentEdges() {
        for (var e : adjacentEdges())
            e.deleteEdge();
    }

    public boolean isChild(Node y) {
        for(var c:children()) {
            if(c==y)
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof final Node that)) return false;
        return getOwner() == that.getOwner() && getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }

    public String getLabel() {
        return getOwner().getLabel(this);
    }

    public void setLabel(String label) {
        getOwner().setLabel(this, label);
    }

    public Object getInfo() {
        return getOwner().getInfo(this);
    }

    public void setInfo(Object info) {
        getOwner().setInfo(this, info);
    }

    public Object getData() {
        return getOwner()==null?null:getOwner().getData(this);
    }

    public void setData(Object data) {
        getOwner().setData(this, data);
    }
}

// EOF

