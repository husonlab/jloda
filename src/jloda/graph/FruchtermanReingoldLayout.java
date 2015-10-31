/**
 * FruchtermanReingoldLayout.java
 *  Copyright (C) 2015 Mathieu Jacomy
 * Original implementation in Gephi by Mathieu Jacomy
 */
package jloda.graph;

import java.awt.geom.Point2D;
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
     * @param fixedNodes nodes not to be moved
     */
    public FruchtermanReingoldLayout(Graph graph, NodeSet fixedNodes) {
        this.graph = graph;
        nodes = graph.getNodes().toArray();
        edges = new int[graph.getNumberOfEdges()][2];
        coordinates = new float[nodes.length][2];
        forceDelta = new float[nodes.length][2];
        fixed = new BitSet();

        initialize(fixedNodes);
    }

    /**
     * initialize
     */
    private void initialize(NodeSet fixedNodes) {
        NodeArray<Integer> node2id = new NodeArray<>(graph);
        for (int v = 0; v < nodes.length; v++) {
            node2id.set(nodes[v], v);
            if (fixedNodes != null && fixedNodes.contains(nodes[v]))
                fixed.set(v);
        }
        int eId = 0;
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            edges[eId][0] = node2id.get(e.getSource());
            edges[eId][1] = node2id.get(e.getTarget());
            eId++;
        }

        if (graph.getNumberOfNodes() > 0) {
            NodeSet seen = new NodeSet(graph);
            Stack<Node> stack = new Stack<>();
            int count = 0;
            for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
                if (!seen.contains(v)) {
                    seen.add(v);
                    stack.push(v);
                    while (stack.size() > 0) {
                        Node w = stack.pop();
                        int id = node2id.get(w);
                        coordinates[id][0] = (float) (100 * Math.sin(2 * Math.PI * count / nodes.length));
                        coordinates[id][1] = (float) (100 * Math.cos(2 * Math.PI * count / nodes.length));
                        count++;
                        for (Edge e = w.getFirstAdjacentEdge(); e != null; e = w.getNextAdjacentEdge(e)) {
                            Node u = e.getOpposite(w);
                            if (!seen.contains(u)) {
                                seen.add(u);
                                stack.push(u);
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
     * @param result
     */
    public void apply(int numberOfIterations, NodeArray<Point2D> result) {

        for (int i = 0; i < numberOfIterations; i++) {
            speed = 100 * (1 - i / numberOfIterations); // linear cooling
            iterate();
        }

        for (int v = 0; v < nodes.length; v++) {
            result.set(nodes[v], new Point2D.Float(coordinates[v][0], coordinates[v][1]));

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
                    float xDist = coordinates[v1][0] - coordinates[v2][0];
                    float yDist = coordinates[v1][1] - coordinates[v2][1];
                    float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
                    if (dist > 0) {
                        float repulsiveF = k * k / dist;
                        forceDelta[v1][0] += xDist / dist * repulsiveF;
                        forceDelta[v1][1] += yDist / dist * repulsiveF;
                    }
                }
            }
        }
        // attraction
        for (int[] edge : edges) {
            int v1 = edge[0];
            int v2 = edge[1];
            float xDist = coordinates[v1][0] - coordinates[v2][0];
            float yDist = coordinates[v1][1] - coordinates[v2][1];
            float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
            if (dist > 0) {
                float attractiveF = dist * dist / k;
                forceDelta[v1][0] -= xDist / dist * attractiveF;
                forceDelta[v1][1] -= yDist / dist * attractiveF;
                forceDelta[v2][0] += xDist / dist * attractiveF;
                forceDelta[v2][1] += yDist / dist * attractiveF;
            }
        }

        // gravity
        for (int v = 0; v < nodes.length; v++) {
            float distSquared = (float) Math.sqrt(coordinates[v][0] * coordinates[v][0] + coordinates[v][1] * coordinates[v][1]);
            float gravityF = 0.01f * k * (float) gravity * distSquared;
            forceDelta[v][0] -= gravityF * coordinates[v][0] / distSquared;
            forceDelta[v][1] -= gravityF * coordinates[v][1] / distSquared;
        }

        // speed
        for (int v = 0; v < nodes.length; v++) {
            forceDelta[v][0] *= speed / SPEED_DIVISOR;
            forceDelta[v][1] *= speed / SPEED_DIVISOR;

        }

        // apply the forces:
        for (int v = 0; v < nodes.length; v++) {
            float xDist = forceDelta[v][0];
            float yDist = forceDelta[v][1];
            float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
            if (dist > 0 && !fixed.get(v)) {
                float limitedDist = Math.min(maxDisplace * ((float) speed / SPEED_DIVISOR), dist);
                coordinates[v][0] += xDist / dist * limitedDist;
                coordinates[v][1] += yDist / dist * limitedDist;
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
