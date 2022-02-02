/*
 * FruchtermanReingoldLayout.java Copyright (C) 2022 Daniel H. Huson
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

/*
 * FruchtermanReingoldLayout.java
 *  Copyright (C) 2019 Mathieu Jacomy
 * Original implementation in Gephi by Mathieu Jacomy
 */
package jloda.graph.algorithms;

import jloda.fx.util.ProgramExecutorService;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.NodeSet;
import jloda.util.APoint2D;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressSilent;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * implements the Fruchterman-Reingold graph layout algorithm
 * <p/>
 * Original implementation in Gephi by Mathieu Jacomy
 * adapted by Daniel Huson, 5.2013, 2020
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
     */
    public FruchtermanReingoldLayout(Graph graph) {
        this(graph, null, null);
    }

    /**
     * constructor. Do not change graph after calling the constructor
     *
     * @param fixedNodes nodes not to be moved
     */
    public FruchtermanReingoldLayout(Graph graph, NodeSet fixedNodes) {
        this(graph, fixedNodes, null);
    }

    /**
     * constructor. Do not change graph after calling the constructor
     *
     * @param fixedNodes nodes not to be moved
     * @param node2start starting coordinates
     */
    public FruchtermanReingoldLayout(Graph graph, NodeSet fixedNodes, NodeArray<APoint2D<?>> node2start) {
        this.graph = graph;
        nodes = new Node[graph.getMaxNodeId()];
        {
            int i = 0;
            for (var v : graph.nodes()) {
                nodes[i++] = v;
            }
        }
        edges = new int[2][graph.getMaxEdgeId()];
        coordinates = new float[2][graph.getMaxNodeId()];
        forceDelta = new float[2][graph.getMaxNodeId()];
        fixed = new BitSet();

        initialize(fixedNodes, node2start);
    }

    /**
     * initialize
     */
    private void initialize(NodeSet fixedNodes, NodeArray<APoint2D<?>> node2start) {
        var node2id = graph.newNodeIntArray();
        for (int v = 0; v < nodes.length; v++) {
            node2id.put(nodes[v], v);
            if (fixedNodes != null && fixedNodes.contains(nodes[v]))
                fixed.set(v);
        }
        {
            int eId = 0;
            for (var e : graph.edges()) {
                edges[0][eId] = node2id.get(e.getSource());
                edges[1][eId] = node2id.get(e.getTarget());
                eId++;
            }
        }

        if (graph.getNumberOfNodes() > 0) {
            if (node2start != null) {
                for (var v : graph.nodes()) {
                    final int id = node2id.get(v);
                    coordinates[0][id] = (float) node2start.get(v).getX();
                    coordinates[1][id] = (float) node2start.get(v).getY();
                }
            } else {
                final NodeSet seen = graph.newNodeSet();
                final Stack<Node> stack = new Stack<>();
                int count = 0;
                for (var v : graph.nodes()) {
                    if (!seen.contains(v)) {
                        seen.add(v);
                        stack.push(v);
                        while (stack.size() > 0) {
                            final Node w = stack.pop();
                            final int id = node2id.get(w);
                            coordinates[0][id] = (float) (100 * Math.sin(2 * Math.PI * count / nodes.length));
                            coordinates[1][id] = (float) (100 * Math.cos(2 * Math.PI * count / nodes.length));
                            count++;
                            for (var e : w.adjacentEdges()) {
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
        }

        speed = 1;
        area = 600;
        gravity = 5;
    }

    /**
     * apply the algorithm
     *
	 */
    public NodeArray<APoint2D<?>> apply(int numberOfIterations) {
        final NodeArray<APoint2D<?>> result = graph.newNodeArray();
        try {
            apply(numberOfIterations, result, new ProgressSilent(), ProgramExecutorService.getNumberOfCoresToUse());
        } catch (CanceledException ignored) { // can't happen
        }
        return result;
    }

    /**
     * apply the algorithm
     *
	 */
    public void apply(int numberOfIterations, NodeArray<APoint2D<?>> result) {
        try {
            apply(numberOfIterations, result, new ProgressSilent(), ProgramExecutorService.getNumberOfCoresToUse());
        } catch (CanceledException ignored) { // can't happen
        }
    }

    /**
     * apply the algorithm
     *
	 */
    public void apply(int numberOfIterations, NodeArray<APoint2D<?>> result, ProgressListener progress, int numberOfThreads) throws CanceledException {
        progress.setMaximum(numberOfIterations);
        progress.setProgress(0);

        final ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        try {
            for (int i = 0; i < numberOfIterations; i++) {
                speed = 100 * (1 - (double) i / numberOfIterations); // linear cooling
                iterate(service, numberOfThreads, progress);
                progress.incrementProgress();
            }

            {
                final int threads = Math.min(numberOfThreads, nodes.length);
                final CountDownLatch countDownLatch = new CountDownLatch(threads);

                for (int t = 0; t < threads; t++) {
                    final int thread = t;
                    service.submit(() -> {
                        try {
                            for (int v = thread; v < nodes.length; v += threads) {
                                result.put(nodes[v], new APoint2D<>(coordinates[0][v], coordinates[1][v]));
                            }
                        } finally {
                            countDownLatch.countDown();
                        }
                    });
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    Basic.caught(e);
                }
                progress.checkForCancel();
            }
        } finally {
            service.shutdownNow();
        }
    }

    /**
     * run one iteration of the algorithm
     */
    private void iterate(ExecutorService service, int numberOfThreads, ProgressListener progress) throws CanceledException {

        float maxDisplace = (float) (Math.sqrt(AREA_MULTIPLICATOR * area) / 10f);
        float k = (float) Math.sqrt((AREA_MULTIPLICATOR * area) / (1f + nodes.length));

        Arrays.fill(forceDelta[0], 0);
        Arrays.fill(forceDelta[1], 0);

        // repulsion
        {
            final int threads = Math.min(numberOfThreads, nodes.length);
            final CountDownLatch countDownLatch = new CountDownLatch(threads);

            for (int t = 0; t < threads; t++) {
                final int thread = t;
                service.submit(() -> {
                    try {
                        for (int v1 = thread; v1 < nodes.length; v1 += numberOfThreads) {
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
                            progress.checkForCancel();
                        }
                    } catch (CanceledException ignored) {
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Basic.caught(e);
            }
            progress.checkForCancel();
        }
        // attraction
        {
            final int threads = Math.min(numberOfThreads, edges[0].length);
            final CountDownLatch countDownLatch = new CountDownLatch(threads);

            for (int t = 0; t < threads; t++) {
                final int thread = t;
                service.submit(() -> {
                    try {
                        for (int e = thread; e < edges[0].length; e += threads) {
                            int v1 = edges[0][e];
                            int v2 = edges[1][e];
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
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Basic.caught(e);
            }
            progress.checkForCancel();
        }

        // gravity
        if (gravity>0) {
            final int threads = Math.min(numberOfThreads, nodes.length);
            final CountDownLatch countDownLatch = new CountDownLatch(threads);

            for (int t = 0; t < threads; t++) {
                final int thread = t;
                service.submit(() -> {
                    try {
                        for (int v = thread; v < nodes.length; v += threads) {
                            float distSquared = (float) Math.sqrt(coordinates[0][v] * coordinates[0][v] + coordinates[1][v] * coordinates[1][v]);
                            float gravityF = 0.01f * k * (float) gravity * distSquared;
                            forceDelta[0][v] -= gravityF * coordinates[0][v] / distSquared;
                            forceDelta[1][v] -= gravityF * coordinates[1][v] / distSquared;
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Basic.caught(e);
            }
            progress.checkForCancel();
        }


        // apply the forces:
        {
            final int threads = Math.min(numberOfThreads, nodes.length);
            final CountDownLatch countDownLatch = new CountDownLatch(threads);

            for (int t = 0; t < threads; t++) {
                final int thread = t;
                service.submit(() -> {
                    try {
                        for (int v = thread; v < nodes.length; v += threads) {
                            double xDist = forceDelta[0][v]* speed / SPEED_DIVISOR;
                            double yDist = forceDelta[1][v]* speed / SPEED_DIVISOR;
                            float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
                            if (dist > 0 && !fixed.get(v)) {
                                float limitedDist = Math.min(maxDisplace * ((float) speed / SPEED_DIVISOR), dist);
                                coordinates[0][v] += xDist / dist * limitedDist;
                                coordinates[1][v] += yDist / dist * limitedDist;
                            }
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Basic.caught(e);
            }
            progress.checkForCancel();
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
