/*
 * MaxClique.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.graph;


import jloda.util.Basic;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

/**
 * slow exact max clique search and fast d-degree heuristic
 *
 * @author huson
 *         Date: 10-Aug-2004
 */
public class MaxClique {
    /**
     * computes a maximum size clique
     *
     * @param graph
     * @return max clique
     */
    public static NodeSet compute(Graph graph) {
        int numNodes = graph.getNumberOfNodes();
        Node[] id2node = new Node[numNodes];
        NodeIntegerArray node2id = new NodeIntegerArray(graph);

        int[][] matrix = convertGraphToMatrix(graph, id2node, node2id);

        // compute max clique for matrix:
        BitSet maxClique = compute(matrix);

        return convertNodeSet(graph, maxClique, id2node);

    }

    /**
     * computes the induced subgraph in which each node has degree >=d
     *
     * @param graph
     * @return induced subgraph in which each node has degree >=d
     */
    public static NodeSet computeDSubgraph(Graph graph, int d) {
        int numNodes = graph.getNumberOfNodes();
        Node[] id2node = new Node[numNodes];
        NodeIntegerArray node2id = new NodeIntegerArray(graph);

        int[][] matrix = convertGraphToMatrix(graph, id2node, node2id);

        BitSet clique = computeDSubgraph(matrix, d);

        return convertNodeSet(graph, clique, id2node);
    }


    /**
     * computes max clique from adjacency matrix
     *
     * @param matrix
     * @return max clique
     */
    public static BitSet compute(int[][] matrix) {
        BitSet maxClique = new BitSet();
        int numNodes = matrix.length;
        /*
        * compute max clique for adjaceny matrix
        */
        for (int vi = 0; vi < numNodes; vi++) {
            BitSet possible = getAllAdjacentNodes(matrix, vi);
            BitSet clique = new BitSet();
            clique.set(vi);
            recurse(matrix, possible, clique, maxClique);
        }
        return maxClique;
    }

    /**
     * gets the set of all nodes adjacent to vi
     *
     * @param matrix
     * @param vi
     * @return all adjacent nodes
     */
    private static BitSet getAllAdjacentNodes(int[][] matrix, int vi) {
        BitSet adjNodes = new BitSet();
        for (int wi = 0; wi < matrix.length; wi++)
            if (matrix[vi][wi] != 0 || matrix[wi][vi] != 0)
                adjNodes.set(wi);
        return adjNodes;
    }

    /**
     * recursively try to extend clique
     *
     * @param possible
     * @param clique
     * @param maxClique
     */
    private static void recurse(int[][] matrix, BitSet possible, BitSet clique, BitSet maxClique) {
        if (clique.cardinality() > maxClique.cardinality()) {
            maxClique.clear();
            maxClique.or(clique);
        }
        // this is the bound step in branch and bound:
        if (!checkBoundCondition(matrix, clique, possible, maxClique.cardinality()))
            return;

        for (int p = possible.nextSetBit(0); p >= 0; p = possible.nextSetBit(p + 1)) {
            possible.set(p, false);

            // FIRST, consider the case inwhich we include p in the clique:
            {
                boolean ok = true;

                for (int q = clique.nextSetBit(0); q >= 0; q = clique.nextSetBit(q + 1))
                    if (matrix[p][q] == 0) {
                        ok = false;
                        break;
                    }
                if (ok) {
                    clique.set(p);
                    // remove
                    BitSet impossible = new BitSet();
                    for (int wi = possible.nextSetBit(0); wi >= 0; wi = possible.nextSetBit(wi + 1)) {
                        if (matrix[p][wi] == 0)
                            impossible.set(wi);
                    }
                    possible.andNot(impossible);

                    recurse(matrix, possible, clique, maxClique);
                    clique.set(p, false);

                    possible.or(impossible);
                }
            }

            // second compute clique not containing p:
            {
                recurse(matrix, possible, clique, maxClique);
            }

            possible.set(p, true);
        }
    }

    /**
     * checks whether the current clique has any chance of beating the global  bound
     *
     * @param matrix
     * @param clique
     * @param possible
     * @param globalBound
     * @return false, if current clique has no hope of beating the global bound
     */
    private static boolean checkBoundCondition(int[][] matrix, BitSet clique, BitSet possible, int globalBound) {
        for (int i = clique.nextSetBit(0); i >= 0; i = clique.nextSetBit(i + 1)) {
            int additional = 0;
            for (int p = possible.nextSetBit(0); p >= 0; p = possible.nextSetBit(p + 1))
                if (matrix[i][p] != 0)
                    additional++;
            if (additional + clique.cardinality() <= globalBound)
                return false;
        }
        return true;
    }

    /**
     * converts matrix-based node set  to graph-based node set
     *
     * @param graph
     * @param nodes
     * @param id2node
     * @return graph-based nodes
     */
    private static NodeSet convertNodeSet(Graph graph, BitSet nodes, Node[] id2node) {
        // convert clique to node set
        NodeSet cliqueNS = new NodeSet(graph);
        for (int vi = nodes.nextSetBit(0); vi >= 0; vi = nodes.nextSetBit(vi + 1))
            cliqueNS.add(id2node[vi]);
        return cliqueNS;
    }

    /**
     * converts graph to adjacency matrix
     *
     * @param graph
     * @param id2node
     * @param node2id
     * @return adjacency matrix
     */
    private static int[][] convertGraphToMatrix(Graph graph, Node[] id2node, NodeIntegerArray node2id) {
        int[][] matrix = new int[graph.getNumberOfNodes()][graph.getNumberOfNodes()];
        // convert graph to adjacency matrix:
        try {
            int id = 0;
            for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
                id2node[id] = v;
                node2id.set(v, id++);
            }

            for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
                for (Edge e = graph.getFirstAdjacentEdge(v); e != null;
                     e = graph.getNextAdjacentEdge(e, v)) {
                    int vi = node2id.getValue(v);
                    int wi = node2id.getValue(graph.getOpposite(v, e));
                    matrix[vi][wi] = matrix[wi][vi] = 1;
                }
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
        return matrix;
    }

    /**
     * computes the induced subgraph in which each node has degree >=d
     *
     * @param matrix
     * @param d
     * @return set of nodes such that all nodes habe degree >=d in induced subgraph
     */
    public static BitSet computeDSubgraph(int[][] matrix, int d) {
        int num = matrix.length;
        int[][] work = matrix.clone();

        int[] deg = new int[num];
        BitSet nodes = new BitSet();
        for (int p = 0; p < num; p++)
            nodes.set(p);

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int p = nodes.nextSetBit(0); p >= 0; p = nodes.nextSetBit(p + 1)) {
                int count = 0;
                for (int q = nodes.nextSetBit(0); q >= 0; q = nodes.nextSetBit(q + 1))
                    if (work[p][q] != 0)
                        count++;
                deg[p] = count;
            }
            for (int p = nodes.nextSetBit(0); p >= 0; p = nodes.nextSetBit(p + 1)) {
                if (deg[p] < d) {
                    nodes.set(p, false);
                    changed = true;
                }
            }
        }

        // now find and return largest component:
        BitSet seen = new BitSet();
        BitSet maxComponent = new BitSet();
        for (int p = nodes.nextSetBit(0); p >= 0; p = nodes.nextSetBit(p + 1)) {
            if (!seen.get(p)) {
                BitSet component = new BitSet();
                visitComponent(matrix, p, nodes, component);
                if (component.cardinality() > maxComponent.cardinality())
                    maxComponent = component;
            }
        }
        return maxComponent;
    }

    /**
     * recursively visit all nodes reachable from p
     *
     * @param matrix
     * @param p
     * @param nodes
     * @param component
     */
    private static void visitComponent(int[][] matrix, int p, BitSet nodes, BitSet component) {
        if (!component.get(p)) {
            component.set(p);
            for (int q = nodes.nextSetBit(0); q >= 0; q = nodes.nextSetBit(q + 1)) {
                if (matrix[p][q] != 0)
                    visitComponent(matrix, q, nodes, component);
            }
        }
    }

    /**
     * Given an adjacency matrix and a perfect elimination scheme on the nodes,
     * returns the list of all maximal cliques
     *
     * @param matrix
     * @param ordering perfect elimination scheme
     * @return all maximal cliques
     */
    static public List computeAll(int[][] matrix, int[] ordering) throws Exception {
        if (matrix.length != ordering.length)
            throw new Exception("matrix.length=" + matrix.length + " != ordering.length=" +
                    ordering.length);
        List cliques = new LinkedList();

        int previousReach = -1;
        for (int i = 0; i < ordering.length; i++) {
            int v = ordering[i];
            int reach = i;
            for (int j = i + 1; j < matrix.length; j++) {
                int w = ordering[j];
                if (matrix[v][w] != 0) // is successor of v in ordering
                {
                    if (j > reach)
                        reach = j;
                }
            }
            if (reach > previousReach) {
                BitSet clique = new BitSet();
                for (int k = i; k <= reach; k++)
                    clique.set(ordering[k]);
                cliques.add(clique);
                previousReach = reach;
            }
        }
        return cliques;
    }
}
