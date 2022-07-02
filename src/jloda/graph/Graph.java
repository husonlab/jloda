/*
 * Graph.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.phylo.PhyloGraph;
import jloda.util.Basic;
import jloda.util.INamed;
import jloda.util.IteratorUtils;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A graph
 * <p/>
 * The nodes and edges are stored in several doubly-linked lists.
 * The set of nodes in the graph is stored in a list
 * The set of edges in the graph is stored in a list
 * Around each node, the set of incident edges is stored in a list.
 * Daniel Huson, 2002
 * <p/>
 */
public class Graph extends GraphBase implements INamed {
    private Node firstNode;
    private Node lastNode;
    private int numberNodes;
    private int numberOfNodesThatAreHidden;
    private int maxNodeId; // max id assigned to any node

    private String name;

    private Edge firstEdge;
    protected Edge lastEdge;
    private int numberEdges;
    private int numberOfEdgesThatAreHidden;
    private int maxEdgeId; // max id assigned to any edge

    private boolean ignoreGraphHasChanged = false; // set this when we are deleting a whole graph

    private final List<GraphUpdateListener> graphUpdateListeners = new LinkedList<>();  //List of listeners that are fired when the graph changes.

    private NodeArray<Object> nodeInfo;
    private NodeArray<String> nodeLabel;
    private NodeArray<Object> nodeData;

    private EdgeArray<Object> edgeInfo;
    private EdgeArray<String> edgeLabel;
    private EdgeArray<Object> edgeData;

    private final List<WeakReference<NodeSet>> nodeSets = new LinkedList<>();
    // created node arrays are kept here. When an node is deleted, it's
    // entry in all node arrays is set to null
    private final List<WeakReference<NodeArray<?>>> nodeArrays = new LinkedList<>();

    // created edge arrays are kept here. When an edge is deleted, it's
    // entry in all edge arrays is set to null
    private final List<WeakReference<EdgeArray<?>>> edgeArrays = new LinkedList<>();
    // keep track of edge sets
    private final List<WeakReference<EdgeSet>> edgeSets = new LinkedList<>();

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
	 */
    public Node newNode(Object info, int recycledId) {
        var v = new Node(this, info);
        v.setId(recycledId);
        maxNodeId--; // count back down
        return v;
    }

    void registerNewNode(Object info, Node v) {
        v.init(this, lastNode, null, ++maxNodeId, info);
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
     * @param v    node
     * @param hide hidden state
     */
    public void setHidden(Node v, boolean hide) {
        if (hide) {
            if (!v.isHidden()) {
                v.setHidden(true);
                numberOfNodesThatAreHidden++;
            }
        } else {
            if (v.isHidden()) {
                v.setHidden(false);
                numberOfNodesThatAreHidden--;
            }
        }
    }

    /**
     * returns hidden state of a node
     * @return true, if hidden
     */
    public boolean isHidden(Node v) {
        return v.isHidden();
    }


    /**
     * sets the hidden state of a edge. Hidden edges are not returned by edge iterators
     *
     * @param e    edge
     * @param hide hidden state
     */
    public void setHidden(Edge e, boolean hide) {
        if (hide) {
            if (!e.isHidden()) {
                e.setHidden(true);
                numberOfEdgesThatAreHidden++;
            }
        } else {
            if (e.isHidden()) {
                e.setHidden(false);
                numberOfEdgesThatAreHidden--;
            }
        }
    }

    /**
     * returns hidden state of a edge
     *
     * @param e edge
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
        maxEdgeId--;
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

        e.init(this, ++maxEdgeId, v, e_v, dir_v, w, e_w, dir_w, obj);
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
     * @param e edge
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
            maxEdgeId = 0;
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
     * @param v node
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
            maxNodeId = 0;
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
    }

    /**
     * Change the order of edges adjacent to a node.
     *
     * @param v        the node in question.
     * @param newOrder the desired sequence of edges.
     */
    public void rearrangeAdjacentEdges(Node v, List<Edge> newOrder) {
        checkOwner(v);
        v.rearrangeAdjacentEdges(newOrder);
    }

    /**
     * move the node to the front of the list of nodes
     *
     * @param v node
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
     * @param v node
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
     * @param e edge
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
     * @param e edge
     */
    public void moveToBack(Edge e) {
        if (e != null && e != lastEdge) {
            checkOwner(e);
            if (e.prev != null)
                e.prev.next = e.next;
            if (e.next != null)
                e.next.prev = e.prev;
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
		return new Iterator<>() {
			private Edge e = firstEdge;

			@Override
			public boolean hasNext() {
				return e != null;
			}

			@Override
			public Edge next() {
				if (e == null)
					throw new NoSuchElementException();
				var result = e;
				e = e.getNext();
				return result;
			}
		};
    }

    /**
     * Get an iterator over all nodes
     *
     * @return node iterator
     */
    public Iterator<Node> nodeIteratorIncludingHidden() {
		return new Iterator<>() {
			private Node v = firstNode;

			@Override
			public boolean hasNext() {
				return v != null;
			}

			@Override
			public Node next() {
				if (v == null)
					throw new NoSuchElementException();
				var result = v;
				v = v.getNext();
				return result;
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
     * gets the string representation of this graph
     *
     * @return tree
     */
    public String toString() {
        if (name == null || name.isBlank())
            return
                    "Nodes: " + getNumberOfNodes() + " Edges: " + getNumberOfEdges();
        else
            return getName();
    }


    /**
     * Get a string representation of the graph.
     *
     * @return the string
     */
    public String toStringFull() {
        StringBuilder buf = new StringBuilder("Graph:\n");
        buf.append("Nodes: ").append(getNumberOfNodes()).append("\n");

        for (var v : nodes())
            buf.append(v.toString()).append("\n");
        buf.append("Edges: ").append(getNumberOfEdges()).append("\n");
        for (var e : edges())
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
     * @param v node
     * @param label label
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
     * @param e edge
     * @param label label
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
    public void copy(Graph src) {
        final NodeArray<Node> oldNode2newNode = src.newNodeArray();
        copy(src, oldNode2newNode, null);
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
            var w = newNode();
            w.setId(v.getId());
            setInfo(w, src.getInfo(v));
            setData(w, src.getData(v));
            setLabel(w, src.getLabel(v));
            oldNode2newNode.put(v, w);
        }
        maxNodeId = src.maxNodeId;

        for (var e : src.edges()) {
            var p = oldNode2newNode.get(src.getSource(e));
            var q = oldNode2newNode.get(src.getTarget(e));
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

            oldEdge2newEdge.put(e, f);
        }
        maxEdgeId = src.maxEdgeId;

        // change all adjacencies to reflect order in old graph:
        for (var v : src.nodes()) {
            var w = oldNode2newNode.get(v);
            var newOrder = new ArrayList<Edge>(v.getDegree());
            for (var e : v.adjacentEdges()) {
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

        final var edges = new HashSet<Edge>();// don't used an edge set here in multi-thread use

        for (var v : srcNodes) {
            Node w = newNode();
            w.setId(v.getId());
            setInfo(w, src.getInfo(v));
            oldNode2newNode.put(v, w);
            for (Edge e : v.outEdges())
                edges.add(e);
        }

        for (var e : edges) {
            final var p = oldNode2newNode.get(e.getSource());
            final var q = oldNode2newNode.get(e.getTarget());
            Edge f = null;
            try {
                f = newEdge(p, q);
                f.setId(e.getId());
            } catch (IllegalSelfEdgeException e1) {
                Basic.caught(e1);
            }
            setInfo(f, src.getInfo(e));
            oldEdge2newEdge.put(e, f);
        }

        // change all adjacencies to reflect order in old graph:
        for (var v : srcNodes) {
            final var w = oldNode2newNode.get(v);
            final var newOrder = new ArrayList<Edge>(v.getDegree());
            for (var e : v.adjacentEdges()) {
                newOrder.add(oldEdge2newEdge.get(e));
            }
            w.rearrangeAdjacentEdges(newOrder);
        }
    }

    /**
     * Sets the label of a node.
     */
    public void setLabel(Node v, String label) {
        if (nodeLabel == null) {
            if (label == null)
                return;
            else
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
            else
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
            else
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
            else
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
            else
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
            else
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
		var result = new Graph();
        result.copy(this);
        return result;
    }

    /**
     * called from constructor of node array to register with graph
     *
     * @param array node array
     */
    void registerNodeArray(NodeArray<?> array) {
        synchronized (nodeArrays) {
            final var toDelete = new LinkedList<WeakReference<NodeArray<?>>>();
            for (var ref : nodeArrays) {
                if (ref.get() == null)
                    toDelete.add(ref); // reference is dead
            }
            nodeArrays.removeAll(toDelete);
            nodeArrays.add(new WeakReference<>(array));
        }
    }

    /**
     * called from deleteNode to clean all array entries for the node
     *
     * @param v node
     */
    void deleteNodeFromArrays(Node v) {
        checkOwner(v);
        synchronized (nodeArrays) {
            var toDelete = new LinkedList<WeakReference<NodeArray<?>>>();
            for (var ref : nodeArrays) {
				var as = ref.get();
                if (as == null)
                    toDelete.add(ref); // reference is dead
                else {
                    as.put(v, null);
                }
            }
            nodeArrays.removeAll(toDelete);
        }
    }

    /**
     * called from constructor of NodeSet to register with graph
     *
     * @param set node set
     */
    void registerNodeSet(NodeSet set) {
        synchronized (nodeSets) {
            final var toDelete = new LinkedList<WeakReference<NodeSet>>();
            for (var ref : nodeSets) {
                if (ref.get() == null)
                    toDelete.add(ref); // reference is dead
            }
            nodeSets.removeAll(toDelete);
            nodeSets.add(new WeakReference<>(set));
        }
    }

    /**
     * called from deleteNode to clean all array entries for the node
     *
     * @param v node
     */
    private void deleteNodeFromSets(Node v) {
        checkOwner(v);
        synchronized (nodeSets) {
            final var toDelete = new LinkedList<WeakReference<NodeSet>>();
            for (var ref : nodeSets) {
                if (ref != null) {
                    final NodeSet set = ref.get();
                    if (set == null)
                        toDelete.add(ref); // reference is dead
                    else {
                        set.remove(v);
                    }
                }
            }
            nodeSets.removeAll(toDelete);
        }
    }

    /**
     * called from constructor of EdgeAssociation to register with graph
     *
	 */
    void registerEdgeArray(EdgeArray<?> array) {
        synchronized (edgeArrays) {
            final var toDelete = new LinkedList<WeakReference<EdgeArray<?>>>();
            for (var ref : edgeArrays) {
                if (ref.get() == null)
                    toDelete.add(ref); // reference is dead
            }
            edgeArrays.removeAll(toDelete);
            edgeArrays.add(new WeakReference<>(array));
        }
    }

    /**
     * called from deleteEdge to clean all array entries for the edge
     *
     * @param e edge
     */
    void deleteEdgeFromArrays(Edge e) {
        checkOwner(e);
        synchronized (edgeArrays) {
            var toDelete = new LinkedList<WeakReference<EdgeArray<?>>>();
            for (var ref : edgeArrays) {
                EdgeArray<?> as = ref.get();
                if (as == null)
                    toDelete.add(ref); // reference is dead
                else {
                    as.put(e, null);
                }
            }
            edgeArrays.removeAll(toDelete);
        }
    }

    /**
     * called from constructor of EdgeSet to register with graph
     *
     * @param set edge set
     */
    void registerEdgeSet(EdgeSet set) {
        synchronized (edgeSets) {
            final var toDelete = new LinkedList<WeakReference<EdgeSet>>();
            for (var ref : edgeSets) {
                if (ref.get() == null)
                    toDelete.add(ref); // reference is dead
            }
            edgeSets.removeAll(toDelete);
            edgeSets.add(new WeakReference<>(set));
        }
    }

    /**
     * called from deleteEdge to clean all array entries for the edge
     *
     * @param e edge
     */
    void deleteEdgeFromSets(Edge e) {
        checkOwner(e);
        synchronized (edgeSets) {
            var toDelete = new LinkedList<WeakReference<EdgeSet>>();
            for (var ref : edgeSets) {
                var set = ref.get();
                if (set == null)
                    toDelete.add(ref); // reference is dead
                else {
                    set.remove(e);
                }
            }
            edgeSets.removeAll(toDelete);
        }
    }

    /**
     * gets the current maximal node id
     *
     * @return max node id
     */
    public int getMaxNodeId() {
        return maxNodeId;
    }

    /**
     * gets the current maximal edge id
     *
     * @return max edge id
     */
    public int getMaxEdgeId() {
        return maxEdgeId;
    }

    /**
     * erase all data components
     */
    public void clearData() {
		nodes().forEach(v -> v.setData(null));
    }

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

            {
                while (e != null && e.isHidden())
                    e = e.getNext();
            }

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
				var result = e;
                e = e.getNext();
                while (e != null && e.isHidden()) {
                    e = e.getNext();
                }
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
				var result = v;
                v = v.getNext();
                return result;
            }
        };
    }

    /**
     * gets a node stream
     *
	 */
    public Stream<Node> nodeStream() {
        return nodeStream(null);
    }

    /**
     * gets a node stream
     *
	 */
    public Stream<Node> nodeParallelStream() {
        return nodeParallelStream(null);
    }

    /**
     * gets a node stream
     *
	 */
    public Stream<Node> nodeStream(Node afterMe) {
        return StreamSupport.stream(nodes(afterMe).spliterator(), false);
    }

    /**
     * gets a node stream
     *
	 */
    public Stream<Node> nodeParallelStream(Node afterMe) {
        return StreamSupport.stream(nodes(afterMe).spliterator(), true);
    }

    /**
     * get the unhidden subset
     *
     * @param nodes node to look at
     * @return unhidden subset
     */
    public NodeSet getUnhiddenSubset(Collection<Node> nodes) {
        var unhidden = new NodeSet(this);
        for (var v : nodes) {
            if (!v.isHidden())
                unhidden.add(v);
        }
        return unhidden;
    }

    /**
     * reorders nodes in graph.
     *
     * @param nodes  these nodes are put at the front of the list of nodes
     */
    public void reorderNodes(List<Node> nodes) {
        final var newOrder = new ArrayList<Node>(numberNodes + numberOfNodesThatAreHidden);
        final var toMove = new HashSet<Node>();
        for (var v : nodes) {
            if (v.getOwner() != null) {
                newOrder.add(v);
                toMove.add(v);
            }
        }

        if (toMove.size() > 0) {
            for (var it = nodeIteratorIncludingHidden(); it.hasNext(); ) {
                var v = it.next();
                if (!toMove.contains(v))
                    newOrder.add(v);
            }

            Node previousNode = null;
            for (var v : newOrder) {
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
                    for(var u:v.adjacentNodes()) {
                        if(components.get(u)==null)
                            stack.add(u);
                    }
                }
                count++;
            }
        }
        return count;
    }

    /**
     * extract a subgraph
     *
     * @param nodes use only these nodes, if not null
     * @param edges use only these edges, if not null
     * @return src to target map
     */
    public NodeArray<Node> extract(Collection<Node> nodes, Collection<Edge> edges, Graph tarGraph) {
		final NodeArray<Node> src2tarNode = newNodeArray();
        extract(nodes, edges, tarGraph, src2tarNode, null);
        return src2tarNode;
    }


    /**
     * extract a subgraph
     */
    public void extract(Collection<Node> nodes, Collection<Edge> edges, Graph tarGraph, NodeArray<Node> src2tarNode, EdgeArray<Edge> src2tarEdge) {
        if (src2tarNode == null)
            src2tarNode = newNodeArray();
        for (var srcNode : (nodes != null ? nodes : nodes())) {
            var tarNode = tarGraph.newNode();
            tarNode.setLabel(srcNode.getLabel());
            tarNode.setInfo(srcNode.getInfo());
            tarNode.setData(srcNode.getData());
            src2tarNode.put(srcNode, tarNode);
        }

        for (var srcEdge : (edges != null ? edges : edges())) {
            var tarA = src2tarNode.get(srcEdge.getSource());
            var tarB = src2tarNode.get(srcEdge.getTarget());
            if (tarA != null && tarB != null) {
                var tarEdge = tarGraph.newEdge(tarA, tarB);
                tarEdge.setInfo(srcEdge.getInfo());
                tarEdge.setLabel(srcEdge.getLabel());
                tarEdge.setData(srcEdge.getData());
                if (this instanceof PhyloGraph srcPhyloGraph && tarGraph instanceof PhyloGraph tarPhyloGraph) {
                    tarPhyloGraph.setWeight(tarEdge, srcPhyloGraph.getWeight(srcEdge));
                    tarPhyloGraph.setConfidence(tarEdge, srcPhyloGraph.getConfidence(srcEdge));
                }
                if (src2tarEdge != null)
                    src2tarEdge.put(srcEdge, tarEdge);
            }
        }
    }

    /**
     * extract all sub graphs
     *
     * @return the list of all sub graphs
     */
    public ArrayList<Graph> extractAllConnectedComponents() {
        return extractAllConnectedComponents(newNodeArray());
    }

    /**
     * extract all sub graphs
     *
     * @return the list of all sub graphs
     */
    public ArrayList<Graph> extractAllConnectedComponents(NodeArray<Node> src2tar) {
        var component = newNodeIntArray();

        var count = computeConnectedComponents(component);
        var subGraphs = new ArrayList<Graph>(count);
        var nodes = new ArrayList<Set<Node>>(count);

            for (int i = 0; i < count; i++) {
                var subGraph=new Graph();
                subGraph.setName(String.valueOf(i+1));
                subGraphs.add(subGraph);
                nodes.add(new HashSet<>());
            }
            for(var v:nodes()) {
                nodes.get(component.get(v)).add(v);
            }
            for(int c=0;c<count;c++){
                src2tar.putAll(extract(nodes.get(c), null, subGraphs.get(c)));
            }
        return subGraphs;
    }

    public boolean isSimple () {
        var sortedEdges = getEdgesAsList();
        sortedEdges.sort(Comparator.comparingInt(a -> Math.max(a.getSource().getId(), a.getTarget().getId())));
        sortedEdges.sort(Comparator.comparingInt(a -> Math.min(a.getSource().getId(), a.getTarget().getId())));
        Edge prev = null;
        for (var e : sortedEdges) {
            if (prev != null && (e.getSource() == prev.getSource() && e.getTarget() == prev.getTarget() || e.getSource() == prev.getTarget() && e.getTarget() == prev.getSource())) {
                return false;
            }
            prev = e;
        }
        return true;
    }

    public boolean isConnected() {
		if (getNumberOfNodes() == 0)
			return true;
		try (var visited = newNodeSet()) {
			var stack = new Stack<Node>();
			stack.push(getFirstNode());
			while (stack.size() > 0) {
				var v = stack.pop();
				visited.add(v);
				for (var w : v.adjacentNodes()) {
					if (!visited.contains(w))
						stack.push(w);
				}
			}
			return visited.size() == getNumberOfNodes();
		}
	}

    /**
     * contract an edge
     *
     * @param e to be contracted
     * @return remaining node
     */
    public Node contract(Edge e) {
        var s = e.getSource();
        var t = e.getTarget();
        for(var f:s.adjacentEdges()) {
			if (f != e) {
				var g = f.getTarget() == s ? newEdge(f.getSource(), t) : newEdge(t, f.getTarget());
				g.setData(f.getData());
				g.setInfo(f.getInfo());
				g.setLabel(f.getLabel());
			}
		}
		deleteNode(s);
		return t;
	}

	void close(NodeSet set) {
		synchronized (nodeSets) {
			for (var ref : nodeSets) {
				if (ref.get() == set) {
					nodeSets.remove(ref);
					return;
				}
			}
		}
	}

	void close(EdgeSet set) {
		synchronized (edgeSets) {
			for (var ref : edgeSets) {
				if (ref.get() == set) {
					edgeSets.remove(ref);
					return;
				}
			}
		}
	}

	void close(NodeArray<?> array) {
		synchronized (nodeArrays) {
			for (var ref : nodeArrays) {
				if (ref.get() == array) {
					nodeArrays.remove(ref);
					return;
				}
			}
		}
	}

    void close(EdgeArray<?> array) {
        synchronized (edgeArrays) {
            for (var ref : edgeArrays) {
                if (ref.get() == array) {
                    edgeArrays.remove(ref);
                    return;
                }
            }
        }
    }

    /**
     * iterates over all nodes of degree 1
     */
    public Iterable<Node> leaves() {
        return () -> new Iterator<>() {
            private Node v = getFirstNode();

            {
                while (v != null && v.getOutDegree() > 0) {
                    v = v.getNext();
                }
            }

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public Node next() {
                final var result = v;
                {
                    v = v.getNext();
                    while (v != null) {
                        if (v.getOutDegree() == 0)
                            break;
                        else
                            v = v.getNext();
                    }
                }
                return result;
            }
        };
    }

    /**
     * counts all nodes of degree 1
     */
    public int countLeaves() {
        return IteratorUtils.count(leaves());
    }
}

// EOF
