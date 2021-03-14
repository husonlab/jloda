/*
 *  FruchtermanReingoldLayoutNew.java Copyright (C) 2021. Daniel H. Huson
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

/*
 * FruchtermanReingoldLayout.java
 *  Copyright (C) 2019 Mathieu Jacomy
 * Original implementation in Gephi by Mathieu Jacomy
 */
package jloda.graph.algorithms;

import jloda.fx.util.ProgramExecutorService;
import jloda.graph.*;
import jloda.util.APoint2D;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import jloda.util.ProgressSilent;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * implements the Fruchterman-Reingold graph layout algorithm
 * <p/>
 * Original implementation in Gephi by Mathieu Jacomy
 * adapted by Daniel Huson, 5.2013, 2020
 */
public class FruchtermanReingoldLayoutNew {

    private static final float SPEED_DIVISOR = 800;
    private static final float AREA_MULTIPLICATOR = 10000;

    // parameters
    private int numberOfThreads = ProgramExecutorService.getNumberOfCoresToUse();
    private float optionArea = 600;
    private double optionSpeed = 1;
    private double optionGravity = 1;

    // data
    private final Graph graph;
    private final NodeArray<float[]> coordinates;
    private final NodeArray<float[]> forceDelta;

    // optional data
    private Function<Edge, ? extends Number> weights;
    private NodeArray<APoint2D<?>> initialNodePositions;
    private NodeSet fixedNodes;

    /**
     * constructor
     */
    public FruchtermanReingoldLayoutNew(Graph graph) {
        this.graph = graph;

        coordinates = graph.newNodeArray();
        forceDelta = graph.newNodeArray();
    }

    /**
     * run the algorithm
     *
     * @param numberOfIterations number of iterations
     * @param result             if non-null, will be used to return result
     * @param progress           if non-null, will be used for progress
     * @return coordinates
     * @throws CanceledException
     */
    public NodeArray<APoint2D<?>> apply(int numberOfIterations, NodeArray<APoint2D<?>> result, ProgressListener progress) throws CanceledException {
        if (result == null)
            result = graph.newNodeArray();
        if (progress == null)
            progress = new ProgressSilent();


        coordinates.clear();
        forceDelta.clear();

        if (initialNodePositions != null) {
            for (var v : graph.nodes()) {
                coordinates.put(v, new float[]{(float) initialNodePositions.get(v).getX(), (float) initialNodePositions.get(v).getY()});
            }
        } else {
            var nNodes = graph.getNumberOfNodes();
            final NodeSet seen = graph.newNodeSet();
            var queue = new LinkedList<Node>();
            int count = 0;
            for (var v : graph.nodes()) {
                if (!seen.contains(v)) {
                    seen.add(v);
                    queue.add(v);
                    while (queue.size() > 0) {
                        final Node w = queue.removeFirst();
                        var arg = (float) ((2 * Math.PI * count) / nNodes);
                        coordinates.put(w, new float[]{(float) (100 * Math.sin(arg)), (float) (100 * Math.cos(arg))});
                        count++;
                        for (var e : w.adjacentEdges()) {
                            Node u = e.getOpposite(w);
                            if (!seen.contains(u)) {
                                seen.add(u);
                                queue.add(u);
                            }
                        }
                    }
                }
            }
        }
        result.clear();

        for (var v : graph.nodes()) {
            forceDelta.put(v, new float[2]);
        }


        progress.setMaximum(numberOfIterations);
        progress.setProgress(0);

        final ExecutorService service = Executors.newFixedThreadPool(Math.max(1, numberOfThreads));

        try {
            for (int i = 0; i < numberOfIterations; i++) {
                optionSpeed = 100 * (1 - (double) i / numberOfIterations); // linear cooling
                iterate(service, progress);
                progress.incrementProgress();
            }
            for (var v : graph.nodes()) {
                result.put(v, new APoint2D<>(coordinates.get(v)[0], coordinates.get(v)[1]));
            }
        } finally {
            service.shutdownNow();
        }
        progress.reportTaskCompleted();
        return result;
    }

    private void iterate(ExecutorService service, ProgressListener progress) throws CanceledException {
        float maxDisplace = (float) (Math.sqrt(AREA_MULTIPLICATOR * optionArea) / 10f);
        float k = (float) Math.sqrt((AREA_MULTIPLICATOR * optionArea) / (1f + graph.getNumberOfNodes()));

        // repulsion
        {
            var countdownLatch = new CountDownLatch(graph.getNumberOfNodes());
            for (var v1 : graph.nodes()) {
                var c1 = coordinates.get(v1);
                service.submit(() -> {
                    try {
                        for (var v2 : graph.nodes()) {
                            if (v1 != v2) {
                                var c2 = coordinates.get(v2);

                                var dx = c1[0] - c2[0];
                                var dy = c1[1] - c2[1];

                                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                                if (dist > 0) {
                                    float k1;
                                    if (weights != null) {
                                        var e = v1.getCommonEdge(v2);
                                        if (e != null)
                                            k1 = weights.apply(e).floatValue();
                                        else
                                            k1 = k;
                                    } else
                                        k1 = k;
                                    var repulsiveF = k1 * k1 / dist;

                                    var fd = forceDelta.get(v1);
                                    fd[0] += dx / dist * repulsiveF;
                                    fd[1] += dy / dist * repulsiveF;
                                }
                            }
                        }
                    } finally {
                        countdownLatch.countDown();
                    }
                });
            }
            try {
                countdownLatch.await();
            } catch (InterruptedException ignored) {
            }
            progress.checkForCancel();
        }
        // attraction
        {
            var countdownLatch = new CountDownLatch(graph.getNumberOfEdges());
            for (var e : graph.edges()) {
                service.submit(() -> {
                            try {
                                var v1 = e.getSource();
                                var v2 = e.getTarget();

                                float dx = coordinates.get(v1)[0] - coordinates.get(v2)[0];
                                float dy = coordinates.get(v1)[1] - coordinates.get(v2)[1];
                                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                                if (dist > 0) {
                                    var k1 = (weights == null ? k : weights.apply(e).floatValue());

                                    float attractiveF = dist * dist / k1;
                                    forceDelta.get(v1)[0] -= dx / dist * attractiveF;
                                    forceDelta.get(v1)[1] -= dy / dist * attractiveF;
                                    forceDelta.get(v2)[0] += dx / dist * attractiveF;
                                    forceDelta.get(v2)[1] += dy / dist * attractiveF;
                                }
                            } finally {
                                countdownLatch.countDown();
                            }
                        }
                );
            }
            try {
                countdownLatch.await();
            } catch (InterruptedException ignored) {
            }
            progress.checkForCancel();
        }

        // gravity
        if (optionGravity > 0) {
            var countdownLatch = new CountDownLatch(graph.getNumberOfNodes());

            for (var v : graph.nodes()) {
                service.submit(() -> {
                    try {
                        var c = coordinates.get(v);
                        var distSquared = (float) Math.sqrt(c[0] * c[0] + c[1] * c[1]);
                        var gravityF = 0.01f * k * (float) optionGravity * distSquared;
                        forceDelta.get(v)[0] -= gravityF * c[0] / distSquared;
                        forceDelta.get(v)[1] -= gravityF * c[1] / distSquared;
                    } finally {
                        countdownLatch.countDown();
                    }
                });
            }
            try {
                countdownLatch.await();
            } catch (InterruptedException ignored) {
            }
            progress.checkForCancel();
        }

        // speed
        {
            var countdownLatch = new CountDownLatch(graph.getNumberOfNodes());

            for (var v : graph.nodes()) {
                service.submit(() -> {
                            try {
                                forceDelta.get(v)[0] *= optionSpeed / SPEED_DIVISOR;
                                forceDelta.get(v)[1] *= optionSpeed / SPEED_DIVISOR;
                            } finally {
                                countdownLatch.countDown();
                            }
                        }
                );
            }
            try {
                countdownLatch.await();
            } catch (InterruptedException ignored) {
            }
        }


        // apply the forces:
        {
            var countdownLatch = new CountDownLatch(graph.getNumberOfNodes());

            for (var v : graph.nodes()) {
                try {
                    if (fixedNodes == null || !fixedNodes.contains(v)) {
                        var dx = forceDelta.get(v)[0];
                        var dy = forceDelta.get(v)[1];
                        float dist = (float) Math.sqrt(dx * dx + dy * dy);
                        if (dist > 0) {
                            float limitedDist = Math.min(maxDisplace * ((float) optionSpeed / SPEED_DIVISOR), dist);
                            coordinates.get(v)[0] += dx / dist * limitedDist;
                            coordinates.get(v)[1] += dy / dist * limitedDist;
                        }
                    }
                } finally {
                    countdownLatch.countDown();
                }
            }
            progress.checkForCancel();
        }
    }

    public Function<Edge, ? extends Number> getWeights() {
        return weights;
    }

    /**
     * set edge weights
     *
     * @param weights
     */
    public void setWeights(Function<Edge, ? extends Number> weights) {
        this.weights = weights;
    }

    public NodeArray<APoint2D<?>> getInitialNodePositions() {
        return initialNodePositions;
    }

    /**
     * set initial node positions
     *
     * @param positions
     */
    public void setInitialNodePositions(NodeArray<APoint2D<?>> positions) {
        this.initialNodePositions = positions;
    }

    public NodeSet getFixedNodes() {
        return fixedNodes;
    }

    /**
     * set nodes that are not to be moved
     *
     * @param fixedNodes
     */
    public void setFixedNodes(NodeSet fixedNodes) {
        this.fixedNodes = fixedNodes;
    }


    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public float getOptionArea() {
        return optionArea;
    }

    public void setOptionArea(float optionArea) {
        this.optionArea = optionArea;
    }

    public double getOptionSpeed() {
        return optionSpeed;
    }

    public void setOptionSpeed(double optionSpeed) {
        this.optionSpeed = optionSpeed;
    }

    public double getOptionGravity() {
        return optionGravity;
    }

    public void setOptionGravity(double optionGravity) {
        this.optionGravity = optionGravity;
    }
}
