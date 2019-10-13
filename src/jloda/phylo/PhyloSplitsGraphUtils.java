/*
 * PhyloSplitsGraphUtils.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.phylo;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.util.APoint2D;

import java.util.*;

public class PhyloSplitsGraphUtils {

    /**
     * Embeds the graph using the given cyclic ordering.
     *
     * @param ordering   the cyclic ordering.
     * @param useWeights scale edges by their weights?
     * @param noise      alter split-angles randomly by a small amount to prevent occlusion of edges.
     * @return node array of coordinates
     */
    public static NodeArray<APoint2D> embed(PhyloSplitsGraph graph, int[] ordering, boolean useWeights, boolean noise) {
        int ntax = ordering.length - 1;

        Node[] ordering_n = new Node[ntax];

        for (int i = 1; i <= ntax; i++) {
            ordering_n[graph.getTaxon2Cycle(i) - 1] = graph.getTaxon2Node(i);
        }

        // get splits
        HashMap<Integer, ArrayList<Node>> splits = getSplits(graph, ordering_n);
        for (Integer key : splits.keySet()) sortSplit(graph, ordering_n, splits.get(key));

        HashMap<Integer, Double> dirs = getDirectionVectors(graph, splits, ordering_n, noise);

        return computeCoords(graph, dirs, ordering_n, useWeights);
    }

    /**
     * get splits:
     * depth search / cross each split just once
     * add taxa to currently crossed splits.
     *
     * @param ordering the cyclic ordering
     */
    private static HashMap<Integer, ArrayList<Node>> getSplits(PhyloSplitsGraph graph, Node[] ordering) {

        HashMap<Integer, ArrayList<Node>> splits = new HashMap<>();

        Stack<Node> toVisit = new Stack<>();
        Stack<Boolean> backtrack = new Stack<>();
        Stack<Edge> edges = new Stack<>();
        ArrayList<Node> seen = new ArrayList<>();
        ArrayList<Integer> crossedSplits = new ArrayList<>();

        // init..
        toVisit.push(ordering[0]);
        backtrack.push(false);
        Edge enter = null;

        // start traversal
        while (!toVisit.empty()) {
            // current Node
            Node u = toVisit.pop();
            // enter-edge
            if (!edges.isEmpty()) enter = edges.pop();
            // are we backtracking?
            boolean backtracking = backtrack.pop();

            /* first visit (not backtracking) */
            if (!backtracking) {   //  && !seen.contains(u)
                if (enter != null) {
                    // current split-id
                    Integer cId = graph.getSplit(enter);
                    crossedSplits.add(cId);
                    if (!splits.containsKey(cId))
                        splits.put(cId, new ArrayList<>());
                }
                seen.add(u);

                /* if the current Node is a taxa-node, add it to currently crossed splits */
                if (graph.getNumberOfTaxa(u) != 0) {
                    for (Integer crossedSplit : crossedSplits) {
                        ArrayList<Node> s = splits.get(crossedSplit);
                        s.add(u);
                    }
                }

                /*
                 * push adjacent nodes (if not already seen)
                 * and current node (backtrack)
                 */
                for (Edge edge : u.adjacentEdges()) {
                    Integer sId = graph.getSplit(edge);
                    Node v = edge.getOpposite(u);
                    if (!seen.contains(v) && !crossedSplits.contains(sId)) {
                        toVisit.push(u);
                        backtrack.push(true);
                        toVisit.push(v);
                        backtrack.push(false);
                        // push edge twice (visit & backtrack)
                        edges.push(edge);
                        edges.push(edge);
                    }
                }

                /* backtrack */
            } else {
                // backtracking -> remove crossed split
                if (enter != null) {
                    Integer cId = graph.getSplit(enter);
                    crossedSplits.remove(cId);
                }

            }
        } // end while
        return splits;
    }

    /**
     * sort a split according to the cyclic ordering.
     *
     * @param ordering the cyclic ordering
     * @param split    the split which has to be sorted
     */
    private static void sortSplit(PhyloSplitsGraph graph, Node[] ordering, ArrayList<Node> split) {

        // convert Node[] to List in order to use List.indexOf(..)
        List<Node> orderingList = Arrays.asList(ordering);
        ArrayList<Node> t1 = new ArrayList<>(split.size());
        ArrayList<Node> t2 = new ArrayList<>(split.size());
        for (int i = 0; i < split.size(); i++) {
            int index = orderingList.indexOf(split.get(i)) - 1;
            if (index == -1) index = ordering.length - 1;
            // split doesn't contain previous taxa in the cyclic ordering
            // => the following (split.cardinality) taxa in the cyclic ordering
            //      give the sorted split.
            if (!(split.contains(ordering[index]))) {
                int j = 1;
                // get both sides of the split
                for (; j < split.size() + 1; j++) {
                    t1.add(ordering[(index + j) % ordering.length]);
                }
                for (int k = j; k < j + (ordering.length - split.size()); k++) {
                    t2.add(ordering[(index + k) % ordering.length]);
                }
                break;
            }
        }
        // chose the split that doesn't contain the first taxon in the
        // cyclic ordering, because coordinates are computed starting there.
        split.clear();
        if (t2.contains(ordering[0]))
            split.addAll(t1);
        else
            split.addAll(t2);
    }


    /**
     * determine the direction vectors for each split.
     * angle: ((leftSplitBoundary + rightSplitBoundary)/amountOfTaxa)*Pi
     *
     * @param splits   the sorted splits
     * @param ordering the cyclic ordering
     * @param noise    alter split-angles randomly by a small amount to prevent occlusion of edges
     * @return direction vectors for each split
     */
    private static HashMap<Integer, Double> getDirectionVectors(PhyloSplitsGraph graph, HashMap<Integer, ArrayList<Node>> splits, Node[] ordering, boolean noise) {
        final Random rand = new Random(666);    // add noise, if necessary
        final HashMap<Integer, Double> dirs = new HashMap<>(splits.size());
        final List<Node> orderingList = Arrays.asList(ordering);

        Edge currentEdge = graph.getFirstEdge();
        int currentSplit;

        for (int j = 0; j < graph.getNumberOfEdges(); j++) {
            //We do a loop on the edges to keep the angles of the splits which have already been computed
            double angle;
            currentSplit = graph.getSplit(currentEdge);
            Integer splitId = currentSplit;
            if (!dirs.containsKey(splitId)) {
                if (graph.getAngle(currentEdge) > 0.00000000001) {
                    //This is an old edge, we affect its angle to its split
                    angle = graph.getAngle(currentEdge);
                    dirs.put(currentSplit, angle);
                } else {
                    //This is a new edge, so we affect it an angle according to the equal angle algorithm
                    final ArrayList<Node> split = splits.get(splitId);
                    int xp = 0;
                    int xq = 0;

                    if (split.size() > 0) {
                        xp = orderingList.indexOf(split.get(0));
                        xq = orderingList.indexOf(split.get(split.size() - 1));
                    }

                    angle = ((((double) xp + (double) xq) / (double) ordering.length) * Math.PI);
                    if (noise && split.size() > 1) {
                        angle = 0.02 * rand.nextFloat() + angle;
                    }
                    dirs.put(splitId, angle);
                }
            } else {
                angle = dirs.get(currentSplit);
            }

            graph.setAngle(currentEdge, angle);
            currentEdge = graph.getNextEdge(currentEdge);
        }
        return dirs;
    }


    /**
     * compute coords for each node.
     * depth first traversal / cross each split just once before backtracking
     *
     * @param dirs       the direction vectors for each split
     * @param ordering   the cyclic ordering
     * @param useWeights scale edges by edge weights?
     * @return node array of coordinates
     */
    public static NodeArray<APoint2D> computeCoords(PhyloSplitsGraph graph, HashMap<Integer, Double> dirs, Node[] ordering, boolean useWeights) {
        final NodeArray<APoint2D> coords = new NodeArray<>(graph);

        /* stack for nodes which still have to be visited */
        final Stack<Node> toVisit = new Stack<>();
        /* Boolean-stack to determine wether current Node is backtracking-node */
        Stack<Boolean> backtrack = new Stack<>();
        /* Edge-stack to determine enter-edge */
        final Stack<Edge> edges = new Stack<>();
        /* collect already seen nodes */
        final ArrayList<Node> seen = new ArrayList<>();
        /* collect already computed nodes to check equal locations */
        final HashMap<Node, APoint2D> locations = new HashMap<>();
        /* collect currently crossed split-ids */
        ArrayList<Integer> crossedSplits = new ArrayList<>();
        /* current node-location */
        APoint2D currentPoint = new APoint2D(0, 0);

        // init..
        toVisit.push(ordering[0]);
        backtrack.push(false);
        Edge enter = null;

        // start traversal
        while (!toVisit.empty()) {
            // current Node
            Node u = toVisit.pop();
            // enter-edge
            if (!edges.isEmpty()) enter = edges.pop();

            // are we backtracking?
            boolean backtracking = backtrack.pop();

            /* visit */
            if (!backtracking) {
                if (enter != null) {
                    // current split-id
                    Integer cId = graph.getSplit(enter);
                    double w = (useWeights ? graph.getWeight(enter) : 1.0);
                    crossedSplits.add(cId);

                    double angle = dirs.get(cId);
                    currentPoint = translateByAngle(currentPoint, angle, w);
                }

                // set location, check equal locations
                final APoint2D loc = new APoint2D(currentPoint.getX(), currentPoint.getY());
                // equal locations: append labels
                if (locations.containsValue(loc)) {
                    Node twinNode;
                    String tLabel = graph.getLabel(u);

                    for (Node v : locations.keySet()) {
                        if (locations.get(v).equals(loc)) {
                            twinNode = v;
                            if (graph.getLabel(twinNode) != null)
                                tLabel = (tLabel != null) ? tLabel + ", " + graph.getLabel(twinNode)
                                        : graph.getLabel(twinNode);
                            graph.setLabel(twinNode, null);
                            graph.setLabel(u, tLabel);
                        }
                    }
                }
                coords.put(u, loc);
                locations.put(u, loc);

                seen.add(u);

                /*
                 * push adjacent nodes (if not already seen)
                 * and current node (backtrack)
                 */
                for (Edge edge : u.adjacentEdges()) {
                    Integer sId = graph.getSplit(edge);
                    Node v = edge.getOpposite(u);
                    if (!seen.contains(v) && !crossedSplits.contains(sId)) {
                        toVisit.push(u);
                        backtrack.push(true);
                        toVisit.push(v);
                        backtrack.push(false);
                        // push edge twice (visit & backtrack)
                        edges.push(edge);
                        edges.push(edge);
                    }
                }

                /* backtrack */
            } else {
                if (enter != null) {
                    Integer cId = graph.getSplit(enter);
                    crossedSplits.remove(cId);
                    double w = (useWeights ? graph.getWeight(enter) : 1.0);
                    double angle = dirs.get(cId);
                    currentPoint = translateByAngle(currentPoint, angle, -w);
                }
            }
        } // end while
        return coords;
    }

    /**
     * Translate a point in the direction specified by an angle.
     *
     * @param apt   Point2D
     * @param alpha double
     * @param dist  double
     * @return Point2D
     */
    private static APoint2D translateByAngle(APoint2D apt, double alpha, double dist) {
        double dx = dist * Math.cos(alpha);
        double dy = dist * Math.sin(alpha);
        if (Math.abs(dx) < 0.000000001)
            dx = 0;
        if (Math.abs(dy) < 0.000000001)
            dy = 0;
        return new APoint2D(apt.getX() + dx, apt.getY() + dy);
    }

}
