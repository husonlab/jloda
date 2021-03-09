/*
 *  AGraph.java Copyright (C) 2021. Daniel H. Huson
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


package jloda.graphs.agraph;


import jloda.graph.IllegalSelfEdgeException;
import jloda.graphs.interfaces.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * array based, immutable graph
 * Daniel Huson, 3.2021
 */
public class AGraph implements IGraph<AGraph.Node, AGraph.Edge> {
    private final Node[] nodes;
    private final Edge[] edges;

    private ANodeArray<Object> nodeInfos;
    private ANodeArray<String> nodeLabels;
    private AEdgeArray<Object> edgeInfos;
    private AEdgeArray<String> edgeLabels;
    private AEdgeDoubleArray edgeWeights;

    AGraph(int numNodes, int numEdges) {
        nodes = new Node[numNodes];
        edges = new Edge[numEdges];
    }

    @Override
    public int getNumberOfNodes() {
        return nodes.length;
    }

    @Override
    public int getNumberOfEdges() {
        return edges.length;
    }

    @Override
    public Iterable<AGraph.Node> nodes() {
        return () -> new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < nodes.length;
            }

            @Override
            public Node next() {
                return nodes[i++];
            }
        };
    }

    @Override
    public Iterable<AGraph.Edge> edges() {
        return () -> new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < edges.length;
            }

            @Override
            public Edge next() {
                return edges[i++];
            }
        };
    }

    public Node getNode(int id) {
        return nodes[id];
    }

    public Edge getEdge(int id) {
        return edges[id];
    }

    public static <N extends INode, E extends IEdge> AGraph create(IGraph<N, E> graph) {
        return create(graph, null, null);
    }

    /**
     * create an immutable graph from a given graph
     *
     * @param srcGraph
     * @param nodes    use only these nodes, if not null
     * @param edges    use only these adjacentEdges, if not null
     * @return an immutable graph
     */
    public static <N extends INode, E extends IEdge> AGraph create(IGraph<N, E> srcGraph, INodeSet<N> nodes, IEdgeSet<E> edges) {
        var numberOfNodes = (nodes == null ? srcGraph.getNumberOfNodes() : nodes.size());
        var numberOfEdges = (edges == null ? srcGraph.getNumberOfEdges() : edges.size());

        final var tarGraph = new AGraph(numberOfNodes, numberOfEdges);

        var nodeId = 0;
        var src2tarNode = new HashMap<N, Node>();
        for (var srcNode : (nodes != null ? nodes : srcGraph.nodes())) {
            var aNode = tarGraph.newNode(nodeId++);
            src2tarNode.put(srcNode, aNode);
            if (srcNode.getLabel() != null)
                aNode.setLabel(srcNode.getLabel());
            if (srcNode.getInfo() != null)
                aNode.setInfo(srcNode.getInfo());
        }

        var edgeId = 0;
        for (var srcEdge : (edges != null ? edges : srcGraph.edges())) {
            @SuppressWarnings("SuspiciousMethodCalls")
            var tarA = src2tarNode.get(srcEdge.getSource());
            @SuppressWarnings("SuspiciousMethodCalls")
            var tarB = src2tarNode.get(srcEdge.getTarget());
            var tarEdge = tarGraph.newEdge(edgeId++, tarA, tarB);
            if (srcEdge.getInfo() != null)
                tarEdge.setInfo(srcEdge.getInfo());
            if (srcEdge.getLabel() != null)
                tarEdge.setLabel(srcEdge.getLabel());
        }
        return tarGraph;
    }


    public String getLabel(Node a) {
        if (nodeLabels == null)
            return null;
        else
            return nodeLabels.getValue(a);
    }

    public void setLabel(Node a, String label) {
        if (nodeLabels == null) {
            if (label == null)
                return;
            nodeLabels = new ANodeArray<>(this);
        }
        nodeLabels.put(a, label);
    }

    public String getLabel(Edge a) {
        if (edgeLabels == null)
            return null;
        else
            return edgeLabels.getValue(a);
    }

    public void setLabel(Edge a, String label) {
        if (edgeLabels == null)
            edgeLabels = new AEdgeArray<>(this);
        edgeLabels.put(a, label);
    }

    public Object getInfo(Node a) {
        if (nodeInfos == null)
            return null;
        return nodeInfos.getValue(a);
    }

    public void setInfo(Node a, Object info) {
        if (nodeInfos == null) {
            if (info == null)
                return;
            nodeInfos = new ANodeArray<>(this);
        }
        nodeInfos.put(a, info);
    }

    public Object getInfo(Edge a) {
        if (edgeInfos == null)
            return null;
        return edgeInfos.getValue(a);
    }

    public void setInfo(Edge a, Object info) {
        if (edgeInfos == null) {
            if (info == null)
                return;
            edgeInfos = new AEdgeArray<>(this);
        }
        edgeInfos.put(a, info);
    }

    public Double getWeight(Edge a) {
        if (edgeWeights == null)
            return null;
        return edgeWeights.getValue(a);
    }

    public void setWeight(Edge a, Double weight) {
        if (edgeWeights == null) {
            if (weight == null)
                return;
            edgeWeights = new AEdgeDoubleArray(this);
        }
        edgeWeights.put(a, weight);
    }

    @Override
    public ANodeSet newNodeSet() {
        return new ANodeSet(this);
    }

    @Override
    public <T> ANodeArray<T> newNodeArray() {
        return new ANodeArray<>(this);
    }

    @Override
    public ANodeIntegerArray newNodeIntArray() {
        return new ANodeIntegerArray(this);
    }

    @Override
    public ANodeDoubleArray newNodeDoubleArray() {
        return new ANodeDoubleArray(this);
    }

    @Override
    public AEdgeSet newEdgeSet() {
        return new AEdgeSet(this);
    }

    @Override
    public <T> AEdgeArray<T> newEdgeArray() {
        return new AEdgeArray<>(this);
    }

    @Override
    public AEdgeIntegerArray newEdgeIntArray() {
        return new AEdgeIntegerArray(this);
    }

    @Override
    public AEdgeDoubleArray newEdgeDoubleArray() {
        return new AEdgeDoubleArray(this);
    }

    Node newNode(int id) {
        return new Node(id);
    }

    Edge newEdge(int id, Node src, Node tar) {
        return new Edge(id, src, tar);
    }

    public class Node implements INode, INodeLabeled {
        private int[] nodeDetails; // id, indegree, adjacent adjacentEdges

        private Node(int id) {
            nodeDetails = new int[]{id, -1};
            nodes[id] = this;
        }

        @Override
        public Iterable<Edge> adjacentEdges() {
            return () -> new Iterator<>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < nodeDetails.length - 2;
                }

                @Override
                public Edge next() {
                    return getEdge(nodeDetails[2 + i++]);
                }
            };
        }

        @Override
        public Iterable<Edge> inEdges() {
            return () -> new Iterator<>() {
                final private Iterator<Edge> it = adjacentEdges().iterator();
                private Edge next = getNext();

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Edge next() {
                    Edge result = next;
                    next = getNext();
                    return result;
                }

                private Edge getNext() {
                    while (it.hasNext()) {
                        Edge e = it.next();
                        if (e.getTarget() == Node.this) {
                            return e;
                        }
                    }
                    return null;
                }
            };
        }

        @Override
        public Iterable<Edge> outEdges() {
            return () -> new Iterator<>() {
                final private Iterator<Edge> it = adjacentEdges().iterator();
                private Edge next = getNext();

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Edge next() {
                    Edge result = next;
                    next = getNext();
                    return result;
                }

                private Edge getNext() {
                    while (it.hasNext()) {
                        Edge e = it.next();
                        if (e.getSource() == Node.this) {
                            return e;
                        }
                    }
                    return null;
                }
            };
        }

        @Override
        public int getDegree() {
            return nodeDetails.length - 2;
        }

        @Override
        public int getInDegree() {
            if (nodeDetails[1] == -1) {
                nodeDetails[1] = 0;
                for (var ignored : inEdges())
                    nodeDetails[1]++;
            }
            return nodeDetails[1];
        }

        @Override
        public int getOutDegree() {
            return getDegree() - getInDegree();
        }

        @Override
        public Iterable<Node> children() {
            return () -> new Iterator<>() {
                final private Iterator<Edge> it = outEdges().iterator();
                private Edge next = it.next();

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Node next() {
                    Node result = next.getTarget();
                    next = it.next();
                    return result;
                }
            };
        }

        @Override
        public Iterable<Node> parents() {
            return () -> new Iterator<>() {
                final private Iterator<Edge> it = inEdges().iterator();
                private Edge next = it.next();

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Node next() {
                    Node result = next.getSource();
                    next = it.next();
                    return result;
                }
            };
        }

        @Override
        public boolean isAdjacent(INode v) {
            for (var e : adjacentEdges()) {
                if (e.getOpposite(this) == v)
                    return true;
            }
            return false;
        }

        @Override
        public Object getInfo() {
            return AGraph.this.getInfo(this);
        }

        @Override
        public void setInfo(Object info) {
            AGraph.this.setInfo(this, info);
        }

        @Override
        public String getLabel() {
            return AGraph.this.getLabel(this);
        }

        @Override
        public void setLabel(String label) {
            AGraph.this.setLabel(this, label);
        }

        @Override
        public int getId() {
            return nodeDetails[0];
        }

        private void updateNodeDetails(ArrayList<Edge> edges) {
            if (edges == null) {
                if (nodeDetails.length > 2)
                    nodeDetails = new int[]{nodeDetails[0], nodeDetails[1]};
            } else {
                int[] tmp = new int[2 + edges.size()];
                tmp[0] = nodeDetails[0];
                tmp[1] = -1;
                for (int i = 0; i < edges.size(); i++) {
                    tmp[2 + i] = edges.get(i).getId();
                }
                nodeDetails = tmp;
            }
        }
    }

    public class Edge implements IEdgeLabeled {
        private final int[] edgeDetails; // id, sourceId, targetId

        private Edge(int id, Node source, Node target) throws IllegalSelfEdgeException {
            if (source.getId() == target.getId())
                throw new IllegalSelfEdgeException();
            edgeDetails = new int[]{id, source.getId(), target.getId()};
            edges[id] = this;
        }

        @Override
        public Node getSource() {
            return nodes[edgeDetails[1]];
        }

        @Override
        public Node getTarget() {
            return nodes[edgeDetails[2]];
        }

        @Override
        public Node getOpposite(INode v) {
            if (v.getId() == edgeDetails[1])
                return nodes[edgeDetails[2]];
            else if (v.getId() == edgeDetails[2])
                return nodes[edgeDetails[1]];
            else
                return null;
        }

        @Override
        public Object getInfo() {
            return AGraph.this.getInfo(this);
        }

        @Override
        public void setInfo(Object info) {
            AGraph.this.setInfo(this, info);
        }

        @Override
        public String getLabel() {
            return AGraph.this.getLabel(this);
        }

        @Override
        public void setLabel(String label) {
            AGraph.this.setLabel(this, label);
        }

        @Override
        public double getWeight() {
            return AGraph.this.getWeight(this);
        }

        @Override
        public void setWeight(double wgt) {
            AGraph.this.setWeight(this, wgt);
        }

        @Override
        public int getId() {
            return edgeDetails[0];
        }
    }
}

