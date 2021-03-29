/*
 * Graph.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jloda.graph;

import jloda.util.Basic;
import jloda.util.IteratorAdapter;
import jloda.util.IteratorUtils;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A graph
 * <p/>
 * The nodes and adjacentEdges are stored in several doubly-linked lists.
 * The set of nodes in the graph is stored in a list
 * The set of adjacentEdges in the graph is stored in a list
 * Around each node, the set of incident adjacentEdges is stored in a list.
 * Daniel Huson, 2002
 * <p/>
 */
public class Graph extends GraphBase {
    private Node firstNode;
    private Node lastNode;
    private int numberNodes;
    private int numberOfNodesThatAreHidden;
    private int idsNodes; // number of ids assigned to nodes

    private Edge firstEdge;
    protected Edge lastEdge;
    private int numberEdges;
    private int numberOfEdgesThatAreHidden;
    private int idsEdges; // number of ids assigned to adjacentEdges

    private boolean ignoreGraphHasChanged = false; // set this when we are deleting a whole graph

    private final List<GraphUpdateListener> graphUpdateListeners = new LinkedList<>();  //List of listeners that are fired when the graph changes.

    private NodeArray<Object> nodeInfo;
    private NodeArray<String> nodeLabel;
    private NodeArray<Object> nodeData;

    private EdgeArray<Object> edgeInfo;
    private EdgeArray<String> edgeLabel;
    private EdgeArray<Object> edgeData;
    private EdgeSet specialEdges;

    private final List<WeakReference<NodeSet>> nodeSets = new LinkedList<>();
    // created node arrays are kept here. When an node is deleted, it's
    // entry in all node arrays is set to null
    private final List<WeakReference<NodeArray<?>>> nodeArrays = new LinkedList<>();

    // created edge arrays are kept here. When an edge is deleted, it's
    // entry in all edge arrays is set to null
    private final List<WeakReference<EdgeArray<?>>> edgeArrays = new LinkedList<>();
    // keep track of edge sets
    private final List<WeakReference<EdgeSet>> edgeSets = new LinkedList<>();
    private String name;

    /**
     * Constructs a new empty graph.
     */
    public Graph() {
        setOwner(this);
    }

    /**
     * Constructs a new node of the type used in the graph. This does not add the node to the graph
     * structure
     *
     * @return Node a new node
     */
    public Node newNode() {
        return newNode(null);
    }

    /**
     * Constructs a new node and set its info to obj.  This does not add the node to the graph
     * structure
     *
     * @param info the info object
     * @return Node a new node
     */
    public Node newNode(Object info) {
        return new Node(this, info);
    }

    /**
     * constructs a new node and reuses an old id
     *
     * @param info
     * @param recycledId
     * @return
     */
    public Node newNode(Object info, int recycledId) {
        final Node v = new Node(this, info);
        v.setId(recycledId);
        idsNodes--; // count back down
        return v;
    }

    void registerNewNode(Object info, Node v) {
        v.init(this, lastNode, null, ++idsNodes, info);
        if (firstNode == null)
            firstNode = v;
        if (lastNode != null)
            lastNode.next = v;
        lastNode = v;
        numberNodes++;
    }

    /**
     * sets the hidden state of a node. Hidden nodes are not returned by node iterators
     *
     * @param v
     * @param hide
     * @return true, if hidden state changed
     */
    public boolean setHidden(Node v, boolean hide) {
        if (hide) {
            if (!v.isHidden()) {
                v.setHidden(true);
                numberOfNodesThatAreHidden++;
                return true;
            }
        } else {
            if (v.isHidden()) {
                v.setHidden(false);
                numberOfNodesThatAreHidden--;
                return true;
            }
        }
        return false;
    }

    /**
     * returns hidden state of a node
     *
     * @param v
     * @return true, if hidden
     */
    public boolean isHidden(Node v) {
        return v.isHidden();
    }


    /**
     * sets the hidden state of a edge. Hidden adjacentEdges are not returned by edge iterators
     *
     * @param e
     * @param hide
     * @return true, if hidden state changed
     */
    public boolean setHidden(Edge e, boolean hide) {
        if (hide) {
            if (!e.isHidden()) {
                e.setHidden(true);
                numberOfEdgesThatAreHidden++;
                return true;
            }
        } else {
            if (e.isHidden()) {
                e.setHidden(false);
                numberOfEdgesThatAreHidden--;
                return true;
            }
        }
        return false;
    }

    /**
     * returns hidden state of a edge
     *
     * @param e
     * @return true, if hidden
     */
    public boolean isHidden(Edge e) {
        return e.isHidden();
    }

    /**
     * Constructs a new edge between nodes v and w.
     *
     * @param v source node
     * @param w target node
     * @return a new edge between nodes v and w
     */
    public Edge newEdge(Node v, Node w) throws IllegalSelfEdgeException {
        return new Edge(this, v, w);
    }

    /**
     * Constructs a new edge between nodes v and w and sets its info to obj.
     *
     * @param v   source node
     * @param w   target node
     * @param obj the info object
     * @return a new edge between nodes v and w and sets its info to obj
     */
    public Edge newEdge(Node v, Node w, Object obj) throws IllegalSelfEdgeException {
        return new Edge(this, v, w, obj);
    }

    /**
     * Constructs a new edge between nodes v and w and sets its info to obj.
     *
     * @param v   source node
     * @param w   target node
     * @param obj the info object
     * @return a new edge between nodes v and w and sets its info to obj
     */
    public Edge newEdge(Node v, Node w, Object obj, int recycledId) throws IllegalSelfEdgeException {
        final Edge e = new Edge(this, v, w, obj);
        e.setId(recycledId);
        idsEdges--;
        return e;
    }

    /**
     * Constructs a new edge between nodes v and w. The edge is inserted into the list of edges incident with
     * v and the list of edges incident with w. The place it is inserted into these list for edges
     * incident with v is determined by e_v and dir_v: if dir_v = Edge.AFTER then it is inserted after
     * e_v in the list, otherwise it is inserted before e_v. Likewise for the list of edges incident with w.
     *
     * @param v     source node
     * @param e_v   reference edge incident to v
     * @param w     target node
     * @param e_w   reference edge incident to w
     * @param dir_v before or after reference e_v
     * @param dir_w before or after reference e_w
     * @param obj   the info object
     * @return a new edge
     */
    public Edge newEdge(Node v, Edge e_v, Node w, Edge e_w, int dir_v, int dir_w, Object obj) throws IllegalSelfEdgeException {
        return new Edge(this, v, e_v, w, e_w, dir_v, dir_w, obj);
    }

    /**
     * Adds a  edge to the graph. The edge is inserted into the list of edges incident with
     * v and the list of edges incident with w. The place it is inserted into these list for edges
     * incident with v is determined by e_v and dir_v: if dir_v = Edge.AFTER then it is inserted after
     * e_v in the list, otherwise it is inserted before e_v. Likewise for the list of edges incident with w.
     *
     * @param v     source
     * @param e_v   reference source edge
     * @param w     target
     * @param e_w   reference target edge
     * @param dir_v insert before/after source reference edge
     * @param dir_w insert before/after target reference edge
     * @param obj   info object
     * @param e     the new edge
     * @
     */
    void registerNewEdge(Node v, Edge e_v, Node w, Edge e_w, int dir_v, int dir_w, Object obj, Edge e) {
        checkOwner(v);
        checkOwner(w);
        v.incrementOutDegree();
        w.incrementInDegree();

        e.init(this, ++idsEdges, v, e_v, dir_v, w, e_w, dir_w, obj);
        if (firstEdge == null)
            firstEdge = e;
        if (lastEdge != null)
            lastEdge.next = e;
        lastEdge = e;
        numberEdges++;
    }

    /**
     * Removes edge e from the graph.
     *
     * @param e the edge
     */
    public void deleteEdge(Edge e) {
        checkOwner(e);
        // note: firstEdge and lastEdge are set in unregisterEdge
        e.deleteEdge();
    }

    /**
     * called from edge when being deleted
     *
     * @param e
     */
    void unregisterEdge(Edge e) {
        checkOwner(e);
        if (e.isHidden())
            numberOfEdgesThatAreHidden--;
        deleteEdgeFromArrays(e);
        deleteEdgeFromSets(e);

        getSource(e).decrementOutDegree();
        getTarget(e).decrementInDegree();
        if (firstEdge == e)
            firstEdge = (Edge) e.next;
        if (lastEdge == e)
            lastEdge = (Edge) e.prev;
        numberEdges--;
        if (numberEdges == 0)
            idsEdges = 0;
    }

    /**
     * Removes node v from the graph.
     *
     * @param v the node
     */
    public void deleteNode(Node v) {
        // note: firstNode and lastNode are set in unregisterNode
        v.deleteNode();

    }

    /**
     * called from node when being deleted
     *
     * @param v
     */
    void unregisterNode(Node v) {
        checkOwner(v);
        deleteNodeFromArrays(v);
        deleteNodeFromSets(v);
        if (v.isHidden())
            numberOfNodesThatAreHidden--;

        if (firstNode == v)
            firstNode = (Node) v.next;
        if (lastNode == v)
            lastNode = (Node) v.prev;
        numberNodes--;
        if (numberNodes == 0)
            idsNodes = 0;
    }

    /**
     * Deletes all edges.
     */
    public void deleteAllEdges() {
        while (firstEdge != null)
            deleteEdge(firstEdge);
    }

    /**
     * Deletes all nodes.
     */
    public void deleteAllNodes() {
        ignoreGraphHasChanged = true;
        while (firstNode != null) {
            deleteNode(firstNode);
        }
        ignoreGraphHasChanged = false;
    }

    /**
     * Clears the graph.
     */
    public void clear() {
        deleteAllNodes();
        nodeInfo = null;
        nodeLabel = null;
        nodeData = null;

        edgeInfo = null;
        edgeLabel = null;
        edgeData = null;
        specialEdges = null;
    }

    /**
     * Change the order of adjacentEdges adjacent to a node.
     *
     * @param v        the node in question.
     * @param newOrder the desired sequence of adjacentEdges.
     */
    public void rearrangeAdjacentEdges(Node v, List<Edge> newOrder) {
        checkOwner(v);
        v.rearrangeAdjacentEdges(newOrder);
    }

    /**
     * move the node to the front of the list of nodes
     *
     * @param v
     */
    public void moveToFront(Node v) {
        if (v != null && v != firstNode) {
            checkOwner(v);
            if (v.prev != null)
                v.prev.next = v.next;
            if (v.next != null)
                v.next.prev = v.prev;
            v.prev = null;
            Node w = firstNode;
            firstNode = v;
            v.next = w;
            if (w != null)
                w.prev = v;
            fireGraphHasChanged();
        }
    }

    /**
     * move the node to the back of the list of nodes
     *
     * @param v
     */
    public void moveToBack(Node v) {
        if (v != null && v != lastNode) {
            checkOwner(v);
            if (v.prev != null)
                v.prev.next = v.next;
            if (v.next != null)
                v.next.prev = v.prev;
            v.prev = null;
            Node w = lastNode;
            lastNode = v;
            v.prev = w;
            if (w != null)
                w.next = v;
            fireGraphHasChanged();
        }
    }

    /**
     * move the edge to the front of the list of edges
     *
     * @param e
     */
    public void moveToFront(Edge e) {
        if (e != null && e != firstEdge) {
            checkOwner(e);
            if (e.prev != null)
                e.prev.next = e.next;
            if (e.next != null)
                e.next.prev = e.prev;
            e.prev = null;
            Edge f = firstEdge;
            firstEdge = e;
            e.next = f;
            if (f != null)
                f.prev = e;
            fireGraphHasChanged();
        }
    }

    /**
     * move the edge to the back of the list of edges
     *
     * @param e
     */
    public void moveToBack(Edge e) {
        if (e != null && e != lastEdge) {
            checkOwner(e);
            if (e.prev != null)
                e.prev.next = e.next;
            if (e.next != null)
                e.next.prev = e.prev;
            e.prev = null;
            Edge f = lastEdge;
            lastEdge = f;
            e.prev = f;
            if (f != null)
                f.next = e;
            fireGraphHasChanged();
        }
    }

    /**
     * Returns the node opposite node v via edge e.
     *
     * @param v the node
     * @param e the edge
     * @return the opposite node
     * Better: use v.getOpposite(e) or e.getOpposite(v)
     */
    public Node getOpposite(Node v, Edge e) {
        checkOwner(e);
        return e.getOpposite(v);
    }

    /**
     * Get the first adjacent edge to v.
     *
     * @param v the node
     * @return the first adjacent edge
     * Better: use v.getFirstAdjacentEdge()
     */
    public Edge getFirstAdjacentEdge(Node v) {
        checkOwner(v);
        return v.getFirstAdjacentEdge();
    }

    /**
     * Get the last adjacent edge to v.
     *
     * @param v the node
     * @return the last adjacent edge
     * Better: use v.getLastAdjacentEdge()
     */
    public Edge getLastAdjacentEdge(Node v) {
        checkOwner(v);
        return v.getLastAdjacentEdge();
    }


    /**
     * Get the successor of e adjacent to v
     *
     * @param e the edge
     * @param v the node
     * @return the successor of edge adjacent to v
     * Better: use v.getNextAdjacentEdge(e)
     */
    public Edge getNextAdjacentEdge(Edge e, Node v) {
        checkOwner(v);
        return v.getNextAdjacentEdge(e);
    }

    /**
     * Get the predecessor of e adjacent to v
     *
     * @param e the edge
     * @param v the node
     * @return the predecessor of edge adjacent to v
     * Better: use v.getPrevAdjacentEdge(e)
     */
    public Edge getPrevAdjacentEdge(Edge e, Node v) {
        checkOwner(v);
        return v.getPrevAdjacentEdge(e);
    }

    /**
     * Get the cyclic successor of e adjacent to v.
     *
     * @param e the edge
     * @param v the node
     * @return the cyclic successor of edge adjacent to v
     * Better: use v.getNextAdjacentEdgeCyclic(e)
     */
    public Edge getNextAdjacentEdgeCyclic(Edge e, Node v) {
        checkOwner(v);
        return v.getNextAdjacentEdgeCyclic(e);
    }

    /**
     * Get the cyclic predecessor of e adjacent to v.
     *
     * @param e the edge
     * @param v the node
     * @return the cyclic predecessor of edge adjacent to v
     * Better: use v.getPrevAdjacentEdgeCyclic(e)
     */
    public Edge getPrevAdjacentEdgeCyclic(Edge e, Node v) {
        checkOwner(v);
        return v.getPrevAdjacentEdgeCyclic(e);
    }

    /**
     * Get the first edge in the graph.
     *
     * @return the first edge
     */
    public Edge getFirstEdge() {
        if (firstEdge != null && firstEdge.isHidden()) {
            return firstEdge.getNext();
        }
        return firstEdge;
    }

    /**
     * Get the last edge in the graph.
     *
     * @return the last edge
     */
    public Edge getLastEdge() {
        if (lastEdge != null && lastEdge.isHidden())
            return lastEdge.getPrev();
        return lastEdge;
    }

    /**
     * Get the successor of edge e.
     *
     * @param e edge
     * @return the successor edge
     */
    public Edge getNextEdge(Edge e) {
        checkOwner(e);
        return e.getNext();
    }

    /**
     * Get the predecessor of edge e.
     *
     * @param e edge
     * @return the predecessor edge
     */
    public Edge getPrevEdge(Edge e) {
        checkOwner(e);
        return e.getPrev();
    }

    /**
     * Get an edge between the two nodes v and w, if it exists
     *
     * @param v source node
     * @param w target node
     * @return an edge between v and w
     */
    public Edge getCommonEdge(Node v, Node w) {
        checkOwner(v);
        return v.getCommonEdge(w);
    }

    /**
     * Get the first node in the graph.
     *
     * @return the first node
     */
    public Node getFirstNode() {
        if (firstNode != null && firstNode.isHidden())
            return firstNode.getNext();
        return firstNode;
    }

    /**
     * Get the last node in the graph.
     *
     * @return the last node
     */
    public Node getLastNode() {
        if (lastNode != null && lastNode.isHidden())
            return lastNode.getPrev();
        return lastNode;
    }

    /**
     * Get the successor node of v
     *
     * @param v the node
     * @return the successor node
     */
    public Node getNextNode(Node v) {
        checkOwner(v);
        return v.getNext();
    }

    /**
     * Get the predecessor of v.
     *
     * @param v the node
     * @return the predecessor node
     */
    public Node getPrevNode(Node v) {
        checkOwner(v);
        return v.getPrev();
    }

    /**
     * Get the number of nodes.
     *
     * @return the number of nodes
     */
    public int getNumberOfNodes() {
        return numberNodes - numberOfNodesThatAreHidden;
    }

    /**
     * Get number of edges.
     *
     * @return the number of edges
     */
    public int getNumberOfEdges() {
        return numberEdges - numberOfEdgesThatAreHidden;
    }

    /**
     * Get the source node of e.
     *
     * @param e the edge
     * @return the source of e
     * Better: use e.getSource()
     */
    public Node getSource(Edge e) {
        checkOwner(e);
        return e.getSource();
    }

    /**
     * Get the target node of e.
     *
     * @param e the edge
     * @return the target of e
     * Better: use e.getTarget()
     */
    public Node getTarget(Edge e) {
        checkOwner(e);
        return e.getTarget();
    }

    /**
     * Get the degree of node v.
     *
     * @return the degree
     * Better: use v.getDegree()
     */
    public int getDegree(Node v) {
        checkOwner(v);
        return v.getDegree();
    }

    /**
     * Get the in-degree of node v.
     *
     * @return the in-degree
     * Better: use v.getInDegree()
     */
    public int getInDegree(Node v) {
        checkOwner(v);
        return v.getInDegree();
    }

    /**
     * Get the out-degree of node v.
     *
     * @return the out-degree
     * Better: use v.getOutDegree()
     */
    public int getOutDegree(Node v) {
        checkOwner(v);
        return v.getOutDegree();
    }

    /**
     * Get an iterator over all edges, including hidden ones
     *
     * @return edge iterator
     */
    public Iterator<Edge> edgeIteratorIncludingHidden() {
        return new IteratorAdapter<>() {
            private Edge e = firstEdge;

            protected Edge findNext() throws NoSuchElementException {
                if (e != null) {
                    final Edge result = e;
                    checkOwner(e);
                    e = (Edge) result.next;
                    return result;
                } else {
                    throw new NoSuchElementException("at end");
                }
            }
        };
    }

    /**
     * Get an iterator over all nodes
     *
     * @return node iterator
     */
    public Iterator<Node> nodeIteratorIncludingHidden() {
        return new IteratorAdapter<>() {
            private Node v = firstNode;

            protected Node findNext() throws NoSuchElementException {
                if (v != null) {
                    final Node result = v;
                    v = (Node) v.next;
                    return result;
                } else {
                    throw new NoSuchElementException("at end");
                }
            }

            public boolean hasNext() {
                return v != null;
            }
        };
    }

    /**
     * Get the id of node v.
     *
     * @param v node
     * @return the id
     */
    public int getId(Node v) {
        checkOwner(v);
        return v.getId();
    }

    /**
     * Get the id of edge e.
     *
     * @param e edge
     * @return the id
     */
    public int getId(Edge e) {
        checkOwner(e);
        return e.getId();
    }

    /**
     * Get a string representation of the graph.
     *
     * @return the string
     */
    public String toString() {
        StringBuilder buf = new StringBuilder("Graph:\n");
        buf.append("Nodes: ").append(getNumberOfNodes()).append("\n");

        for (Node v = getFirstNode(); v != null; v = getNextNode(v))
            buf.append(v.toString()).append("\n");
        buf.append("Edges: ").append(getNumberOfEdges()).append("\n");
        for (Edge e = getFirstEdge(); e != null; e = getNextEdge(e))
            buf.append(e.toString()).append("\n");

        return buf.toString();
    }

    /**
     * Get an edge directed from one given node to another, if it exists.
     *
     * @param v source node
     * @param w target node
     * @return edge from v tp w, if it exists, else null
     */
    public Edge findDirectedEdge(Node v, Node w) {
        checkOwner(v);
        return v.findDirectedEdge(w);
    }

    /**
     * Adds a GraphUpdateListener
     *
     * @param graphUpdateListener the listener to be added
     */
    public void addGraphUpdateListener(GraphUpdateListener graphUpdateListener) {
        graphUpdateListeners.add(graphUpdateListener);
    }

    /**
     * Removes a GraphUpdateListener
     *
     * @param graphUpdateListener the listener to be removed
     */
    public void removeGraphUpdateListener
    (GraphUpdateListener graphUpdateListener) {
        graphUpdateListeners.remove(graphUpdateListener);
    }

    /* Fires the newNode event for all GraphUpdateListeners
     *@param v the node
     */
    protected void fireNewNode(Node v) {
        checkOwner(v);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.newNode(v);
        }
    }

    /* Fires the deleteNode event for all GraphUpdateListeners
     *@param v the node
     */

    protected void fireDeleteNode(Node v) {
        checkOwner(v);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.deleteNode(v);
        }
    }

    /**
     * fires the node label changed event
     *
     * @param v
     * @param label
     */
    protected void fireNodeLabelChanged(Node v, String label) {
        checkOwner(v);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.nodeLabelChanged(v, label);
        }
    }

    /* Fires the newEdge event for all GraphUpdateListeners
     *@param e the edge
     */

    protected void fireNewEdge(Edge e) {
        checkOwner(e);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.newEdge(e);
        }
    }

    /* Fires the deleteEdge event for all GraphUpdateListeners
     *@param e the edge
     */

    protected void fireDeleteEdge(Edge e) {
        checkOwner(e);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.deleteEdge(e);
        }
    }

    /**
     * fires the edge label changed event
     *
     * @param e
     * @param label
     */
    protected void fireEdgeLabelChanged(Edge e, String label) {
        checkOwner(e);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.edgeLabelChanged(e, label);
        }
    }

    /* Fires the graphHasChanged event for all GraphUpdateListeners
     */

    protected void fireGraphHasChanged() {
        if (!ignoreGraphHasChanged) {

            for (GraphUpdateListener gul : graphUpdateListeners) {
                gul.graphHasChanged();
            }
        }
    }

    /**
     * copies a graph
     */
    public NodeArray<Node> copy(Graph src) {
        NodeArray<Node> oldNode2newNode = src.newNodeArray();
        copy(src, oldNode2newNode, null);
        return oldNode2newNode;
    }

    /**
     * Copies one graph onto another. Maintains the ids of nodes and edges
     *
     * @param src             the source graph
     * @param oldNode2newNode if not null, returns map: old node id onto new node id
     * @param oldEdge2newEdge if not null, returns map: old edge id onto new edge id
     */
    public void copy(Graph src, NodeArray<Node> oldNode2newNode, EdgeArray<Edge> oldEdge2newEdge) {
        clear();

        if (oldNode2newNode == null)
            oldNode2newNode = src.newNodeArray();
        if (oldEdge2newEdge == null)
            oldEdge2newEdge = src.newEdgeArray();

        for (var v : src.nodes()) {
            Node w = newNode();
            w.setId(v.getId());
            setInfo(w, src.getInfo(v));
            setData(w, src.getData(v));
            setLabel(w, src.getLabel(v));
            oldNode2newNode.put(v, w);
        }
        idsNodes = src.idsNodes;

        for (var e : src.edges()) {
            Node p = oldNode2newNode.get(src.getSource(e));
            Node q = oldNode2newNode.get(src.getTarget(e));
            Edge f = null;
            try {
                f = newEdge(p, q);
                f.setId(e.getId());
            } catch (IllegalSelfEdgeException e1) {
                Basic.caught(e1);
            }
            setInfo(f, src.getInfo(e));
            setData(f, src.getData(e));
            setLabel(f, src.getLabel(e));
            setSpecial(f, src.isSpecial(e));

            oldEdge2newEdge.put(e, f);
        }
        idsEdges = src.idsEdges;

        // change all adjacencies to reflect order in old graph:
        for (Node v = src.getFirstNode(); v != null; v = src.getNextNode(v)) {
            Node w = oldNode2newNode.get(v);
            List<Edge> newOrder = new LinkedList<>();
            for (Edge e = v.getFirstAdjacentEdge(); e != null; e = v.getNextAdjacentEdge(e)) {
                newOrder.add(oldEdge2newEdge.get(e));
            }
            w.rearrangeAdjacentEdges(newOrder);
        }
    }

    /**
     * Copies one graph onto another. Maintains the ids of nodes and edges
     *
     * @param src             the source graph
     * @param srcNodes        the nodes of the source graph to be copied
     * @param oldNode2newNode if not null, returns map: old node id onto new node id
     * @param oldEdge2newEdge if not null, returns map: old edge id onto new edge id
     */
    public void copy(Graph src, Set<Node> srcNodes, Map<Node, Node> oldNode2newNode, HashMap<Edge, Edge> oldEdge2newEdge) {
        clear();

        if (oldNode2newNode == null)
            oldNode2newNode = new HashMap<>();
        if (oldEdge2newEdge == null)
            oldEdge2newEdge = new HashMap<>();

        final Set<Edge> edges = new HashSet<>();// don't used an edge set here in multi-thread use

        for (Node v : srcNodes) {
            Node w = newNode();
            w.setId(v.getId());
            setInfo(w, src.getInfo(v));
            oldNode2newNode.put(v, w);
            for (Edge e : v.outEdges())
                edges.add(e);
        }

        for (Edge e : edges) {
            final Node p = oldNode2newNode.get(e.getSource());
            final Node q = oldNode2newNode.get(e.getTarget());
            Edge f = null;
            try {
                f = newEdge(p, q);
                f.setId(e.getId());
            } catch (IllegalSelfEdgeException e1) {
                Basic.caught(e1);
            }
            if (src.isSpecial(e))
                setSpecial(f, true);
            setInfo(f, src.getInfo(e));
            oldEdge2newEdge.put(e, f);
        }

        // change all adjacencies to reflect order in old graph:
        for (Node v : srcNodes) {
            final Node w = oldNode2newNode.get(v);
            final List<Edge> newOrder = new LinkedList<>();
            for (Edge e : v.adjacentEdges()) {
                newOrder.add(oldEdge2newEdge.get(e));
            }
            w.rearrangeAdjacentEdges(newOrder);
        }
    }

    /**
     * extract a subgraph
     *
     * @param nodes use only these nodes, if not null
     * @param edges use only these edges, if not null
     * @return src to target map
     */
    public static NodeArray<Node> extract(Graph src, Set<Node> nodes, Set<Edge> edges, Graph tar) {
        NodeArray<Node> src2tarNode = src.newNodeArray();
        for (var srcNode : (nodes != null ? nodes : src.nodes())) {
            var tarNode = tar.newNode();
            src2tarNode.put(srcNode, tarNode);
            tarNode.setLabel(srcNode.getLabel());
            tarNode.setInfo(srcNode.getInfo());
            tarNode.setData(srcNode.getData());
        }

        for (var srcEdge : (edges != null ? edges : src.edges())) {
            var tarA = src2tarNode.get(srcEdge.getSource());
            var tarB = src2tarNode.get(srcEdge.getTarget());
            if (tarA != null && tarB != null) {
                var tarEdge = tar.newEdge(tarA, tarB);
                tarEdge.setInfo(srcEdge.getInfo());
                tarEdge.setLabel(srcEdge.getLabel());
                tarEdge.setData(tarEdge.getData());
            }
        }
        return src2tarNode;
    }

    /**
     * Sets the label of a node.
     */
    public void setLabel(Node v, String label) {
        if (nodeLabel == null) {
            if (label == null)
                return;
            nodeLabel = newNodeArray();
        }
        nodeLabel.put(v, label);
        fireNodeLabelChanged(v, label);
    }

    /**
     * Gets the taxon label of a node.
     */
    public String getLabel(Node v) {
        return nodeLabel == null ? null : nodeLabel.get(v);
    }

    /**
     * Sets the label of an edge.
     */
    public void setLabel(Edge e, String label) {
        if (edgeLabel == null) {
            if (label == null)
                return;
            edgeLabel = newEdgeArray();
        }
        edgeLabel.put(e, label);
        fireEdgeLabelChanged(e, label);

    }

    /**
     * Gets the label of an edge.
     */
    public String getLabel(Edge e) {
        return edgeLabel == null ? null : edgeLabel.get(e);
    }

    /**
     * Sets the info of a node.
     */
    public void setInfo(Node v, Object info) {
        if (nodeInfo == null) {
            if (info == null)
                return;
            nodeInfo = newNodeArray();
        }
        nodeInfo.put(v, info);
    }

    /**
     * Gets the info of a node.
     */
    public Object getInfo(Node v) {
        return nodeInfo == null ? null : nodeInfo.get(v);
    }

    /**
     * Sets the info of an edge.
     */
    public void setInfo(Edge e, Object info) {
        if (edgeInfo == null) {
            if (info == null)
                return;
            edgeInfo = newEdgeArray();
        }
        edgeInfo.put(e, info);
    }

    /**
     * Gets the info of an edge.
     */
    public Object getInfo(Edge e) {
        return edgeInfo == null ? null : edgeInfo.get(e);
    }

    /**
     * Sets the data of a node.
     *
     * @param v    Node
     * @param data String
     */
    public void setData(Node v, Object data) {
        if (nodeData == null) {
            if (data == null)
                return;
            nodeData = newNodeArray();
        }
        nodeData.put(v, data);
    }

    /**
     * Gets the data of a node.
     */
    public Object getData(Node v) {
        return nodeData == null ? null : nodeData.get(v);
    }

    /**
     * Sets the data of an edge.
     *
     * @param e    Edge
     * @param data String
     */
    public void setData(Edge e, Object data) {
        if (edgeData == null) {
            if (data == null)
                return;
            edgeData = newEdgeArray();
        }
        edgeData.put(e, data);
    }

    /**
     * Gets the data of an edge.
     */
    public Object getData(Edge e) {
        return edgeData == null ? null : edgeData.get(e);
    }


    public Iterable<String> nodeLabels() {
        return nodeLabel != null ? nodeLabel.values() : Collections.emptySet();
    }

    public Iterable<String> edgeLabels() {
        return edgeLabel != null ? edgeLabel.values() : Collections.emptySet();
    }

    /**
     * produces a clone of this graph
     *
     * @return a clone of this graph
     */
    public Object clone() {
        Graph result = new Graph();
        result.copy(this);
        return result;
    }

    /**
     * called from constructor of NodeAssociation to register with graph
     *
     * @param array
     */
    void registerNodeArray(NodeArray<?> array) {
        synchronized (nodeArrays) {
            final List<WeakReference> toDelete = new LinkedList<>();
            for (WeakReference<NodeArray<?>> ref : nodeArrays) {
                if (ref.get() == null)
                    toDelete.add(ref); // reference is dead
            }
            if (toDelete.size() > 0)
                nodeArrays.removeAll(toDelete);
            nodeArrays.add(new WeakReference<>(array));
        }
    }

    /**
     * called from deleteNode to clean all array entries for the node
     *
     * @param v
     */
    void deleteNodeFromArrays(Node v) {
        checkOwner(v);
        synchronized (nodeArrays) {
            List<WeakReference> toDelete = new LinkedList<>();
            for (WeakReference<NodeArray<?>> ref : nodeArrays) {
                NodeArray<?> as = ref.get();
                if (as == null)
                    toDelete.add(ref); // reference is dead
                else {
                    as.put(v, null);
                }
            }
            for (WeakReference ref : toDelete) {
                nodeArrays.remove(ref);
            }
        }
    }

    /**
     * called from constructor of NodeSet to register with graph
     *
     * @param set
     */
    void registerNodeSet(NodeSet set) {
        synchronized (nodeSets) {
            final List<WeakReference> toDelete = new LinkedList<>();
            for (WeakReference<NodeSet> ref : nodeSets) {
                if (ref.get() == null)
                    toDelete.add(ref); // reference is dead
            }
            if (toDelete.size() > 0)
                nodeSets.removeAll(toDelete);
            nodeSets.add(new WeakReference<>(set));
        }
    }

    /**
     * called from deleteNode to clean all array entries for the node
     *
     * @param v
     */
    private void deleteNodeFromSets(Node v) {
        checkOwner(v);
        synchronized (nodeSets) {
            final List<WeakReference> toDelete = new LinkedList<>();
            for (WeakReference<NodeSet> ref : nodeSets) {
                if (ref != null) {
                    final NodeSet set = ref.get();
                    if (set == null)
                        toDelete.add(ref); // reference is dead
                    else {
                        set.remove(v);
                    }
                }
            }
            for (WeakReference ref : toDelete) {
                nodeSets.remove(ref);
            }
        }
    }

    /**
     * called from constructor of EdgeAssociation to register with graph
     *
     * @param array
     */
    void registerEdgeArray(EdgeArray<?> array) {
        synchronized (edgeArrays) {
            final List<WeakReference> toDelete = new LinkedList<>();
            for (WeakReference<EdgeArray<?>> ref : edgeArrays) {
                if (ref.get() == null)
                    toDelete.add(ref); // reference is dead
            }
            if (toDelete.size() > 0)
                edgeArrays.removeAll(toDelete);
        }
        edgeArrays.add(new WeakReference<>(array));
    }

    /**
     * called from deleteEdge to clean all array entries for the edge
     *
     * @param edge
     */
    void deleteEdgeFromArrays(Edge edge) {
        checkOwner(edge);
        synchronized (edgeArrays) {
            List<WeakReference> toDelete = new LinkedList<>();
            for (WeakReference<EdgeArray<?>> ref : edgeArrays) {
                EdgeArray<?> as = ref.get();
                if (as == null)
                    toDelete.add(ref); // reference is dead
                else {
                    as.put(edge, null);
                }
            }
            for (WeakReference ref : toDelete) {
                edgeArrays.remove(ref);
            }
        }
    }

    /**
     * called from constructor of EdgeSet to register with graph
     *
     * @param set
     */
    void registerEdgeSet(EdgeSet set) {
        synchronized (edgeSets) {
            final List<WeakReference> toDelete = new LinkedList<>();
            for (WeakReference<EdgeSet> ref : edgeSets) {
                if (ref.get() == null)
                    toDelete.add(ref); // reference is dead
            }
            if (toDelete.size() > 0)
                edgeSets.removeAll(toDelete);
            edgeSets.add(new WeakReference<>(set));
        }
    }

    /**
     * called from deleteEdge to clean all array entries for the edge
     *
     * @param e
     */
    void deleteEdgeFromSets(Edge e) {
        checkOwner(e);
        synchronized (edgeSets) {
            List<WeakReference> toDelete = new LinkedList<>();
            for (WeakReference<EdgeSet> ref : edgeSets) {
                EdgeSet set = ref.get();
                if (set == null)
                    toDelete.add(ref); // reference is dead
                else {
                    set.remove(e);
                }
            }
            for (WeakReference ref : toDelete) {
                edgeSets.remove(ref);
            }
        }
    }

    /**
     * gets the current maximal node id
     *
     * @return max node id
     */
    int getMaxNodeId() {
        return idsNodes;
    }

    /**
     * gets the current maximal edge id
     *
     * @return max edge id
     */
    int getMaxEdgeId() {
        return idsEdges;
    }

    /**
     * is this a special edge?
     *
     * @param e
     * @return true, if marked as special
     */
    public boolean isSpecial(Edge e) {
        return specialEdges != null && specialEdges.size() > 0 && specialEdges.contains(e);
    }

    /**
     * mark as special or not
     *
     * @param e
     * @param special
     */
    public void setSpecial(Edge e, boolean special) {
        if (specialEdges == null) {
            if (!special)
                return;
            specialEdges = newEdgeSet();
        }
        if (special)
            specialEdges.add(e);
        else
            specialEdges.remove(e);
    }

    /**
     * gets the number of special edges
     *
     * @return number of special edges
     */
    public int getNumberSpecialEdges() {
        return specialEdges == null ? 0 : specialEdges.size();
    }

    /**
     * gets the set of special edges
     */
    public Iterable<Edge> specialEdges() {
        return specialEdges != null ? specialEdges : Collections.emptySet();
    }

    /**
     * get the non-special-edge connected component containing v
     *
     * @param v
     * @return component
     */
    public NodeSet getSpecialComponent(Node v) {
        NodeSet nodes = new NodeSet(this);
        List<Node> queue = new LinkedList<>();
        queue.add(v);
        nodes.add(v);
        while (queue.size() > 0) {
            v = queue.remove(0);
            for (Edge e = v.getFirstAdjacentEdge(); e != null; e = v.getNextAdjacentEdge(e)) {
                if (!isSpecial(e)) {
                    Node w = e.getOpposite(v);
                    if (!nodes.contains(w)) {
                        queue.add(w);
                        nodes.add(w);
                    }
                }
            }
        }
        return nodes;
    }

    /**
     * erase all data components
     */
    public void clearData() {
        for (Node v = getFirstNode(); v != null; v = v.getNext()) {
            v.setData(null);
        }
    }

    /*public NodeSet computeSetOfLeaves(Node v) {
        NodeSet sons = new NodeSet(this);
        getLeavesRec(v,sons);
        return sons;
   }
    public void getLeavesRec(Node v, NodeSet nodes) {
        for (Edge f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
           Node w = f.getTarget();
           getLeavesRec(w,sons);
       }
                  if (v.getOutDegree() == 0)
           nodes.add(w);
        return sons;
   }*/

    /**
     * gets all nodes as a new set
     *
     * @return node set of nodes
     */
    public List<Node> getNodesAsList() {
        return IteratorUtils.asList(nodes());
    }

    /**
     * iterable over all edges
     *
     * @return iterable over all edges
     */
    public Iterable<Edge> edges() {
        return edges(null);
    }

    /**
     * iterable over all edges after given edge prev
     *
     * @param afterMe the previous edge, after which the iteration starts. If null, iterates over all edges
     * @return iterable over all edges
     */
    public Iterable<Edge> edges(Edge afterMe) {
        return () -> new Iterator<>() {
            Edge e = (afterMe == null ? getFirstEdge() : afterMe.getNext());

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
                Edge result = e;
                e = e.getNext();
                return result;
            }
        };
    }

    public Stream<Edge> edgeStream() {
        return edgeStream(null);
    }

    public Stream<Edge> edgeParallelStream() {
        return edgeParallelStream(null);
    }


    public Stream<Edge> edgeStream(Edge afterMe) {
        return StreamSupport.stream(edges(afterMe).spliterator(), false);
    }

    public Stream<Edge> edgeParallelStream(Edge afterMe) {
        return StreamSupport.stream(edges(afterMe).spliterator(), true);
    }

    /**
     * gets all edges as a new set
     *
     * @return edge set of edges
     */
    public List<Edge> getEdgesAsList() {
        return IteratorUtils.asList(edges());
    }

    /**
     * iterable over all nodes
     *
     * @return iterable over all nodes
     */
    public Iterable<Node> nodes() {
        return nodes(null);
    }

    /**
     * iterable over all nodes after given node prev
     *
     * @param afterMe the previous node, after which the iteration starts. If null, iterates over all nodes
     * @return iterable over all nodes
     */
    public Iterable<Node> nodes(Node afterMe) {
        return () -> new Iterator<>() {
            Node v = (afterMe == null ? getFirstNode() : afterMe.getNext());

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public Node next() {
                Node result = v;
                v = v.getNext();
                return result;
            }
        };
    }

    /**
     * gets a node stream
     *
     * @return
     */
    public Stream<Node> nodeStream() {
        return nodeStream(null);
    }

    /**
     * gets a node stream
     *
     * @return
     */
    public Stream<Node> nodeParallelStream() {
        return nodeParallelStream(null);
    }

    /**
     * gets a node stream
     *
     * @return
     */
    public Stream<Node> nodeStream(Node afterMe) {
        return StreamSupport.stream(nodes(afterMe).spliterator(), false);
    }

    /**
     * gets a node stream
     *
     * @return
     */
    public Stream<Node> nodeParallelStream(Node afterMe) {
        return StreamSupport.stream(nodes(afterMe).spliterator(), true);
    }


    /**
     * get the unhidden subset
     *
     * @param nodes
     * @return
     */
    public NodeSet getUnhiddenSubset(Collection<Node> nodes) {
        NodeSet unhidden = new NodeSet(this);
        for (Node v : nodes) {
            if (!v.isHidden())
                unhidden.add(v);
        }
        return unhidden;
    }

    /**
     * reorders nodes in graph. These nodes are put at the front of the list of nodes
     *
     * @param nodes
     */
    public void reorderNodes(List<Node> nodes) {
        final List<Node> newOrder = new ArrayList<>(numberNodes + numberOfNodesThatAreHidden);
        final Set<Node> toMove = new HashSet<>();
        for (Node v : nodes) {
            if (v.getOwner() != null) {
                newOrder.add(v);
                toMove.add(v);
            }
        }

        if (toMove.size() > 0) {
            for (Iterator<Node> it = nodeIteratorIncludingHidden(); it.hasNext(); ) {
                Node v = it.next();
                if (!toMove.contains(v))
                    newOrder.add(v);
            }

            Node previousNode = null;
            for (Node v : newOrder) {
                if (previousNode == null) {
                    firstNode = v;
                    v.prev = null;
                } else {
                    previousNode.next = v;
                    v.prev = previousNode;
                }
                previousNode = v;
            }
            if (previousNode != null) {
                previousNode.next = null;
            }
            lastNode = previousNode;
        }
    }

    /**
     * computes the set of all nodes with outdegree 0
     *
     * @return all leaves
     */
    public NodeSet computeSetOfLeaves() {
        var nodes = newNodeSet();
        for (var v : nodes())
            if (v.getOutDegree() == 0)
                nodes.add(v);
        return nodes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node findNodeById(int id) {
        for (Node v : nodes())
            if (v.getId() == id)
                return v;
        return null;
    }

    public Edge findEdgeById(int id) {
        for (Edge e : edges())
            if (e.getId() == id)
                return e;
        return null;
    }

    public NodeSet newNodeSet() {
        return new NodeSet(this);
    }

    public <T> NodeArray<T> newNodeArray() {
        return new NodeArray<>(this);
    }

    public NodeIntArray newNodeIntArray() {
        return new NodeIntArray(this);
    }

    public NodeFloatArray newNodeFloatArray() {
        return new NodeFloatArray(this);
    }

    public NodeDoubleArray newNodeDoubleArray() {
        return new NodeDoubleArray(this);
    }

    public EdgeSet newEdgeSet() {
        return new EdgeSet(this);
    }

    public <T> EdgeArray<T> newEdgeArray() {
        return new EdgeArray<>(this);
    }

    public EdgeIntArray newEdgeIntArray() {
        return new EdgeIntArray(this);
    }

    public EdgeFloatArray newEdgeFloatArray() {
        return new EdgeFloatArray(this);
    }

    public EdgeDoubleArray newEdgeDoubleArray() {
        return new EdgeDoubleArray(this);
    }

    public int computeConnectedComponents(NodeIntArray components) {
        components.clear();
        var count=0;
        for(var v:nodes()) {
            if(components.get(v)==null) {
                final Stack<Node> stack=new Stack<>();
                stack.push(v);
                while(stack.size()>0) {
                    v=stack.pop();
                    components.put(v,count);
                    stack.addAll(IteratorUtils.asList(v.adjacentNodes()));
                }
                count++;
            }
        }
        return count;
    }
}

// EOF
