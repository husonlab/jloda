/*
 * FruchtermanReingoldLayout.java
 *  Copyright (C) 2019 Mathieu Jacomy
 * Original implementation in Gephi by Mathieu Jacomy
 */
package jloda.graph;

import jloda.util.APoint2D;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;

import java.util.BitSet;
import java.util.Stack;

/**
 * implements the Fruchterman-Reingold graph layout algorithm
 * <p/>
 * Original implementation in Gephi by Mathieu Jacomy
 * adapted by Daniel Huson, 5.2013
 */
public class FruchtermanReingoldLayout {

    private static final float SPEED_DIVISOR = 800;
    private static final float AREA_MULTIPLICATOR = 10000;

    //Properties
    private float area;
    private double gravity;
    private double speed;

    // data
    private final Graph graph;
    private final Node[] nodes;
    private final int[][] edges;
    private final float[][] coordinates;
    private final float[][] forceDelta;

    private final BitSet fixed;

    /**
     * constructor. Do not change graph after calling the constructor
     *
     * @param graph
     */
    public FruchtermanReingoldLayout(Graph graph) {
        this(graph, null, null);
    }

    /**
     * constructor. Do not change graph after calling the constructor
     *
     * @param graph
     * @param fixedNodes nodes not to be moved
     */
    public FruchtermanReingoldLayout(Graph graph, NodeSet fixedNodes) {
        this(graph, fixedNodes, null);
    }


    /**
     * constructor. Do not change graph after calling the constructor
     *
     * @param graph
     * @param fixedNodes nodes not to be moved
     * @param node2start starting coordinates
     */
    public FruchtermanReingoldLayout(Graph graph, NodeSet fixedNodes, NodeArray<float[]> node2start) {
        this.graph = graph;
        nodes = graph.getNodesAsSet().toArray();
        edges = new int[2][graph.getNumberOfEdges()];
        coordinates = new float[2][nodes.length];
        forceDelta = new float[2][nodes.length];
        fixed = new BitSet();

        initialize(fixedNodes, node2start);
    }

    /**
     * initialize
     */
    private void initialize(NodeSet fixedNodes, NodeArray<float[]> node2start) {
        NodeArray<Integer> node2id = new NodeArray<>(graph);
        for (int v = 0; v < nodes.length; v++) {
            node2id.put(nodes[v], v);
            if (fixedNodes != null && fixedNodes.contains(nodes[v]))
                fixed.set(v);
        }
        int eId = 0;
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            edges[0][eId] = node2id.get(e.getSource());
            edges[1][eId] = node2id.get(e.getTarget());
            eId++;
        }

        if (graph.getNumberOfNodes() > 0) {
            if(node2start != null) {
                for (Object obj : graph.nodes()) {
                    final Node v = (Node) obj;
                    final int id = node2id.get(v);
                    coordinates[0][id] = 10 * node2start.get(v)[0];
                    coordinates[1][id] = 10 * node2start.get(v)[1];
                }
            } else {
                final NodeSet seen = new NodeSet(graph);
                final Stack<Node> stack = new Stack<>();
                int count = 0;
                for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
                    if (!seen.contains(v)) {
                        seen.add(v);
                        stack.push(v);
                        while (stack.size() > 0) {
                            final Node w = stack.pop();
                            final int id = node2id.get(w);
                            coordinates[0][id] = (float) (100 * Math.sin(2 * Math.PI * count / nodes.length));
                            coordinates[1][id] = (float) (100 * Math.cos(2 * Math.PI * count / nodes.length));
                            count++;
                            for (Edge e = w.getFirstAdjacentEdge(); e != null; e = w.getNextAdjacentEdge(e)) {
                                final Node u = e.getOpposite(w);
                                if (!seen.contains(u)) {
                                    seen.add(u);
                                    stack.push(u);
                                }
                            }
                        }
                    }
                }
            }
        }

        speed = 1;
        area = 600;
        gravity = 5;
    }

    /**
     * apply the algorithm
     *
     * @param numberOfIterations
     * @return
     */
    public NodeArray<APoint2D> apply(int numberOfIterations) {
        final NodeArray<APoint2D> result = new NodeArray<>(graph);
        apply(numberOfIterations, result);
        return result;
    }

    /**
     * apply the algorithm
     *
     * @param numberOfIterations
     * @param result
     */
    public void apply(int numberOfIterations, NodeArray<APoint2D> result) {
        try {
            apply(numberOfIterations, result, null);
        } catch (CanceledException ex) {
            Basic.caught(ex); // can't happen
        }
    }

    /**
     * apply the algorithm
     *
     * @param numberOfIterations
     * @param result
     */
    public void apply(int numberOfIterations, NodeArray<APoint2D> result, ProgressListener progress) throws CanceledException {

        for (int i = 0; i < numberOfIterations; i++) {
            speed = 100 * (1 - (double) i / numberOfIterations); // linear cooling
            iterate();
            if (progress != null)
                progress.checkForCancel();
        }

        for (int v = 0; v < nodes.length; v++) {
            result.put(nodes[v], new APoint2D(coordinates[0][v], coordinates[1][v]));
        }
    }

    /**
     * run one iteration of the algorithm
     */
    private void iterate() {

        float maxDisplace = (float) (Math.sqrt(AREA_MULTIPLICATOR * area) / 10f);
        float k = (float) Math.sqrt((AREA_MULTIPLICATOR * area) / (1f + nodes.length));

        // repulsion
        for (int v1 = 0; v1 < nodes.length; v1++) {
            for (int v2 = 0; v2 < nodes.length; v2++) {
                if (v1 != v2) {
                    float xDist = coordinates[0][v1] - coordinates[0][v2];
                    float yDist = coordinates[1][v1] - coordinates[1][v2];
                    float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
                    if (dist > 0) {
                        float repulsiveF = k * k / dist;
                        forceDelta[0][v1] += xDist / dist * repulsiveF;
                        forceDelta[1][v1] += yDist / dist * repulsiveF;
                    }
                }
            }
        }
        // attraction
        for (int i = 0; i < edges[0].length; i++) {
            int v1 = edges[0][i];
            int v2 = edges[1][i];
            float xDist = coordinates[0][v1] - coordinates[0][v2];
            float yDist = coordinates[1][v1] - coordinates[1][v2];
            float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
            if (dist > 0) {
                float attractiveF = dist * dist / k;
                forceDelta[0][v1] -= xDist / dist * attractiveF;
                forceDelta[1][v1] -= yDist / dist * attractiveF;
                forceDelta[0][v2] += xDist / dist * attractiveF;
                forceDelta[1][v2] += yDist / dist * attractiveF;
            }
        }

        // gravity
        for (int v = 0; v < nodes.length; v++) {
            float distSquared = (float) Math.sqrt(coordinates[0][v] * coordinates[0][v] + coordinates[1][v] * coordinates[1][v]);
            float gravityF = 0.01f * k * (float) gravity * distSquared;
            forceDelta[0][v] -= gravityF * coordinates[0][v] / distSquared;
            forceDelta[1][v] -= gravityF * coordinates[1][v] / distSquared;
        }

        // speed
        for (int v = 0; v < nodes.length; v++) {
            forceDelta[0][v] *= speed / SPEED_DIVISOR;
            forceDelta[1][v] *= speed / SPEED_DIVISOR;

        }

        // apply the forces:
        for (int v = 0; v < nodes.length; v++) {
            float xDist = forceDelta[0][v];
            float yDist = forceDelta[1][v];
            float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
            if (dist > 0 && !fixed.get(v)) {
                float limitedDist = Math.min(maxDisplace * ((float) speed / SPEED_DIVISOR), dist);
                coordinates[0][v] += xDist / dist * limitedDist;
                coordinates[1][v] += yDist / dist * limitedDist;
            }
        }
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
