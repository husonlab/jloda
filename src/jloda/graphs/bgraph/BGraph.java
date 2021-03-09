/*
 *  BGraph.java Copyright (C) 2021. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

package jloda.graphs.bgraph;


import jloda.graph.IllegalSelfEdgeException;
import jloda.graph.NotOwnerException;
import jloda.graphs.interfaces.IEdgeLabeled;
import jloda.graphs.interfaces.IEditableGraph;
import jloda.graphs.interfaces.INode;
import jloda.graphs.interfaces.INodeLabeled;
import jloda.util.IterationUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.StreamSupport;

/**
 * simple graph class for building graphs
 * note that total size will reflect all nodes and adjacentEdges that have been constructed during the life of this graph
 * Daniel Huson, 3.2021
 */
public class BGraph implements IEditableGraph<BGraph.Node, BGraph.Edge> {
    private long latestEdgeRemoval; // time of latest edge removal, used to update sets
    private long latestNodeRemoval; // time of latest node removal, used to update sets

    private int numberOfNodes = 0;
    private int numberOfEdges = 0;

    private final ArrayList<Node> nodes = new ArrayList<>();
    private final ArrayList<Edge> edges = new ArrayList<>();

    public BGraph() {
        nodes.add(null); // forces indices to start at 1
        edges.add(null);// forces indices to start at 1
    }

    @Override
    public void clear() {
        // invalidate all nodes and edges:
        for (var v : nodes())
            v.owner = null;
        for (var e : edges())
            e.owner = null;

        nodes.clear();
        edges.clear();

        nodes.add(null); // forces indices to start at 1
        edges.add(null);// forces indices to start at 1

        numberOfNodes = 0;
        numberOfEdges = 0;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getNumberOfEdges() {
        return numberOfEdges;
    }

    @Override
    public Node newNode(Object info) {
        var node = new Node(nodes.size(), info);
        nodes.add(node);
        numberOfNodes++;
        return node;
    }

    @Override
    public void deleteNode(Node node) {
        for (var edge : node.adjacentEdges()) {
            edge.getOpposite(node).edges.remove(edge);
            edges.set(edge.getId(), null);
            numberOfEdges--;
            latestEdgeRemoval++;
        }
        nodes.set(node.getId(), null);
        node.owner = null;
        numberOfNodes--;
        latestNodeRemoval++;
    }

    @Override
    public Edge newEdge(Node src, Node tar, Object info) throws IllegalSelfEdgeException {
        if (src.id == tar.id)
            throw new IllegalSelfEdgeException();

        var edge = new Edge(edges.size(), src.id, tar.id, info);
        edges.add(edge);
        numberOfEdges++;
        return edge;
    }

    @Override
    public void deleteEdge(Edge edge) {
        edge.getSource().edges.remove(edge);
        edge.getTarget().edges.remove(edge);
        edges.set(edge.getId(), null);
        edge.owner = null;
        numberOfEdges--;
        latestEdgeRemoval++;
    }

    public Iterable<Node> nodes() {
        return () -> IterationUtils.iteratorNonNullElements(nodes.iterator());
    }

    public Iterable<Edge> edges() {
        return () -> IterationUtils.iteratorNonNullElements(edges.iterator());
    }

    public Node getNode(int id) {
        return id >= nodes.size() ? null : nodes.get(id);
    }

    public Edge getEdge(int id) {
        return id >= edges.size() ? null : edges.get(id);
    }

    long getLatestNodeRemoval() {
        return latestNodeRemoval;
    }

    long getLatestEdgeRemoval() {
        return latestEdgeRemoval;
    }

    private BGraph getGraph() {
        return this;
    }

    @Override
    public BNodeSet newNodeSet() {
        return new BNodeSet(this);
    }

    @Override
    public <T> BNodeArray<T> newNodeArray() {
        return new BNodeArray<T>(this);
    }

    @Override
    public BNodeIntegerArray newNodeIntArray() {
        return new BNodeIntegerArray(this);
    }

    @Override
    public BNodeDoubleArray newNodeDoubleArray() {
        return new BNodeDoubleArray(this);
    }

    @Override
    public BEdgeSet newEdgeSet() {
        return new BEdgeSet(this);
    }

    @Override
    public <T> BEdgeArray<T> newEdgeArray() {
        return new BEdgeArray<T>(this);
    }

    @Override
    public BEdgeIntegerArray newEdgeIntArray() {
        return new BEdgeIntegerArray(this);
    }

    @Override
    public BEdgeDoubleArray newEdgeDoubleArray() {
        return new BEdgeDoubleArray(this);
    }

    public class Node implements INode, INodeLabeled {
        private BGraph owner;
        private final int id;
        private final ArrayList<Edge> edges = new ArrayList<>();
        private Object info;
        private String label;

        public Node(int id, Object info) {
            this.id = id;
            this.info = info;
            owner = getGraph();
        }

        public int getId() {
            return id;
        }

        public Object getInfo() {
            return info;
        }

        public void setInfo(Object info) {
            this.info = info;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Iterable<Edge> adjacentEdges() {
            return edges;
        }

        public Iterable<Edge> inEdges() {
            return () -> new Iterator<>() {
                final Iterator<Edge> it = adjacentEdges().iterator();
                Edge next = getNext();

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Edge next() {
                    var result = next;
                    next = getNext();
                    return result;
                }

                private Edge getNext() {
                    while (it.hasNext()) {
                        var edge = it.next();
                        if (edge.getTarget() == Node.this)
                            return edge;
                    }
                    return null;
                }
            };
        }

        public Iterable<Edge> outEdges() {
            return () -> new Iterator<>() {
                final Iterator<Edge> it = adjacentEdges().iterator();
                Edge next = getNext();

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Edge next() {
                    var result = next;
                    next = getNext();
                    return result;
                }

                private Edge getNext() {
                    while (it.hasNext()) {
                        var edge = it.next();
                        if (edge.getSource() == Node.this)
                            return edge;
                    }
                    return null;
                }
            };
        }

        @Override
        public Iterable<BGraph.Node> children() {
            return () -> new Iterator<>() {
                final private Iterator<BGraph.Edge> it = outEdges().iterator();
                private BGraph.Edge next = it.next();

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public BGraph.Node next() {
                    BGraph.Node result = next.getTarget();
                    next = it.next();
                    return result;
                }
            };
        }

        @Override
        public Iterable<BGraph.Node> parents() {
            return () -> new Iterator<>() {
                final private Iterator<BGraph.Edge> it = inEdges().iterator();
                private BGraph.Edge next = it.next();

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public BGraph.Node next() {
                    BGraph.Node result = next.getSource();
                    next = it.next();
                    return result;
                }
            };
        }

        public int getDegree() {
            return edges.size();
        }

        public int getInDegree() {
            return (int) StreamSupport.stream(inEdges().spliterator(), false).count();
        }

        public int getOutDegree() {
            return (int) StreamSupport.stream(outEdges().spliterator(), false).count();
        }

        @Override
        public boolean isAdjacent(INode v) {
            for (var e : adjacentEdges()) {
                if (e.getOpposite(this) == v)
                    return true;
            }
            return false;
        }

        public BGraph getOwner() {
            return owner;
        }

        public void checkOwner(BGraph bGraph) {
            if (owner != bGraph)
                throw new NotOwnerException(this);
        }
    }

    public class Edge implements IEdgeLabeled {
        private BGraph owner;
        private final int id;
        private final int sourceId;
        private final int targetId;
        private Object info;
        private String label;
        private double weight;

        public Edge(int id, int sourceId, int targetId, Object info) {
            this.id = id;
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.info = info;

            getSource().edges.add(this);
            getTarget().edges.add(this);

            this.owner = getGraph();
        }

        public int getId() {
            return id;
        }

        public Node getSource() {
            return nodes.get(sourceId);
        }

        public Node getTarget() {
            return nodes.get(targetId);
        }

        public Node getOpposite(INode v) {
            if (getSource() == v)
                return getTarget();
            else if (getTarget() == v)
                return getSource();
            else
                return null;
        }

        public Object getInfo() {
            return info;
        }

        public void setInfo(Object info) {
            this.info = info;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public BGraph getOwner() {
            return owner;
        }

        public void checkOwner(BGraph bGraph) {
            if (owner != bGraph)
                throw new NotOwnerException(this);
        }
    }
}
