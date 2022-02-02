/*
 * FastMultiLayerMethodLayout.java Copyright (C) 2022 Daniel H. Huson
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
package jloda.graph.fmm;

import jloda.graph.*;
import jloda.graph.fmm.algorithm.*;
import jloda.graph.fmm.geometry.*;
import jloda.util.IteratorUtils;
import jloda.util.Single;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * implementation of the fast multilayer method (note: without multipole algorithm)
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class FastMultiLayerMethodLayout {
    /**
     * run the algorithm
     *
     * @param options     the algorithms options (may be null for default)
     * @param graph       the graph to be embedded
     * @param edgeWeights optional edge weights (may be null for unit lengths)
     * @param result      the resulting node coordinates
     */
    public static Rectangle apply(FastMultiLayerMethodOptions options, Graph graph, Function<Edge, ? extends Number> edgeWeights,
                                  BiConsumer<Node, Point> result) {
        if (!graph.isSimple())
            throw new IllegalArgumentException("graph is not simple");
        if (!graph.isConnected())
            throw new IllegalArgumentException("graph is not connected");

        if (graph.getNumberOfNodes() > 2) {
            if (options == null)
                options = new FastMultiLayerMethodOptions();

            NodeArray<NodeAttributes> nodeAttributes = graph.newNodeArray();
            EdgeArray<EdgeAttributes> edgeAttributes = graph.newEdgeArray();

            graph.nodeStream().forEach(v -> nodeAttributes.put(v, new NodeAttributes()));
            graph.edgeStream().forEach(e -> edgeAttributes.put(e, new EdgeAttributes(edgeWeights.apply(e).doubleValue())));

            initializeIdealEdgeLengths(graph, options, nodeAttributes, edgeAttributes, edgeWeights);

            // compute the layout
            callDivideAndConquerStep(options, graph, nodeAttributes, edgeAttributes);

            // extract result:
            for (var v : graph.nodes()) {
                var va = nodeAttributes.get(v);
                result.accept(v, va.getPosition());
            }
            return DRect.computeBBox(nodeAttributes.values());

        } else if (graph.getNumberOfNodes() == 2) {
            if (options == null)
                options = new FastMultiLayerMethodOptions();

            result.accept(graph.getFirstNode(), new DPoint(0, 0));
            double weight = options.getUnitEdgeLength();
            if (graph.getNumberOfEdges() == 1)
                weight *= edgeWeights.apply(graph.getFirstEdge()).doubleValue();
            else if (graph.getNumberOfEdges() == 2)
                weight *= 0.5 * (edgeWeights.apply(graph.getFirstEdge()).doubleValue() + edgeWeights.apply(graph.getLastEdge()).doubleValue());
            result.accept(graph.getLastNode(), new DPoint(weight, 0));
            return new DRect(0, 0, weight, 1);
        } else if (graph.getNumberOfNodes() == 1) {
            result.accept(graph.getFirstNode(), new DPoint(0, 0));
        }
        return new DRect(0, 0, 1, 1);
    }

    private static void initializeIdealEdgeLengths(Graph graph, FastMultiLayerMethodOptions options, NodeArray<NodeAttributes> nodeAttributes,
                                                   EdgeArray<EdgeAttributes> edgeAttributes, Function<Edge, ? extends Number> weights) {
        var factor = options.getUnitEdgeLength();
        if (options.getEdgeLengthMeasurement() == FastMultiLayerMethodOptions.EdgeLengthMeasurement.MidPoint) {
            for (var e : graph.edges()) {
                edgeAttributes.get(e).setLength(factor * weights.apply(e).floatValue());
            }
        } else { // BoundingCircle
            for (var e : graph.edges()) {
                var ea = edgeAttributes.get(e);
                ea.setLength(factor * weights.apply(e).floatValue() + nodeAttributes.get(e.getSource()).getRadius() + nodeAttributes.get(e.getTarget()).getRadius());
            }
        }
    }

    private static void callDivideAndConquerStep(FastMultiLayerMethodOptions options, Graph graph, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes) {
        int maxLevel = 30;

        if (options.isUseSimpleAlgorithmForChainsAndCycles()) {
            var minDegree = graph.nodeStream().mapToInt(Node::getDegree).min().orElse(0);
            var maxDegree = graph.nodeStream().mapToInt(Node::getDegree).max().orElse(0);
            if (minDegree == 1 && maxDegree == 2 && placeChain(options, graph, nodeAttributes, edgeAttributes))
                return;
            else if (minDegree == 2 && maxDegree == 2 && placeCycle(options, graph, nodeAttributes, edgeAttributes)) {
                return;
            }
        }

        if (options.isMSingleLevel())
            options.setMinGraphSize(graph.getNumberOfNodes());

        var multiLevelGraph = new Graph[maxLevel + 1];
        var multiLevelNodeAttributes = (NodeArray<NodeAttributes>[]) new NodeArray[maxLevel + 1];
        var multiLevelEdgeAttributes = (EdgeArray<EdgeAttributes>[]) new EdgeArray[maxLevel + 1];

        var topLevel = MultiLevel.createMultiLevelRepresentations(options, graph, nodeAttributes, edgeAttributes, multiLevelGraph, multiLevelNodeAttributes, multiLevelEdgeAttributes);

        var box = new LayoutBox();
        for (int level = topLevel; level >= 0; level--) {
            if (level == topLevel) {
                createInitialPlacement(options, multiLevelGraph[level], multiLevelNodeAttributes[level], multiLevelEdgeAttributes[level], box);
            } else {
                MultiLevel.findInitialPlacementForLevel(level, options, multiLevelGraph, multiLevelNodeAttributes, multiLevelEdgeAttributes);
                box.update(multiLevelGraph[level], multiLevelNodeAttributes[level]);
            }
            callForceCalculationStep(options, box, multiLevelGraph[level], multiLevelNodeAttributes[level], multiLevelEdgeAttributes[level], level, topLevel);

            if (options.getNumberOfChainSmoothingRounds() > 0)
                smooth(options, multiLevelGraph[level], multiLevelNodeAttributes[level]);
            if (true && options.getStepsForRotatingComponents() > 0)
                RotateLayout.apply(options, graph, nodeAttributes);
        }
    }

    private static boolean placeChain(FastMultiLayerMethodOptions options, Graph graph, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes) {
        var v = graph.nodeStream().filter(a -> a.getDegree() == 1).findAny().orElse(null);
        if (v == null)
            return false;

        var x = 0.0;
        var inEdge = (Edge) null;
        var random = new Random();

        do {
            nodeAttributes.get(v).setPosition(x, 0.1 * options.getUnitEdgeLength() * random.nextDouble());
            var outEdge = (Edge) null;
            for (Edge e : v.adjacentEdges()) {
                if (e != inEdge) {
                    outEdge = e;
                    break;
                }
            }
            if (outEdge != null) {
                x += edgeAttributes.get(outEdge).getLength();
                v = outEdge.getOpposite(v);
                inEdge = outEdge;
            } else
                v = null;
        }
        while (v != null);
        return true;
    }

    private static boolean placeCycle(FastMultiLayerMethodOptions options, Graph graph, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes) {
        var total = graph.edgeStream().mapToDouble(e -> edgeAttributes.get(e).getLength()).sum();
        var v = graph.getFirstNode();
        var inEdge = (Edge) null;
        var random = new Random();
        var part = 0.0;
        var radius = total / (2 * Math.PI);
        var x = Math.cos(0) * radius;
        var y = Math.sin(0) * radius;

        do {
            nodeAttributes.get(v).setPosition(1.1 * (x + 0.1 * options.getUnitEdgeLength() * random.nextDouble()), 0.6 * (y + 0.1 * options.getUnitEdgeLength() * random.nextDouble()));
            var outEdge = (Edge) null;
            for (Edge e : v.adjacentEdges()) {
                if (e != inEdge) {
                    outEdge = e;
                    break;
                }
            }
            if (outEdge == null)
                return false;
            part += edgeAttributes.get(outEdge).getLength();
            var angle = part / total * 2 * Math.PI;
            x = Math.cos(angle) * radius;
            y = Math.sin(angle) * radius;

            v = outEdge.getOpposite(v);
            inEdge = outEdge;
        }
        while (v != graph.getFirstNode());
        return true;
    }

    private static void callForceCalculationStep(FastMultiLayerMethodOptions options, LayoutBox layoutBox, Graph graph, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes, int level, int maxLevel) {
        final int ITERBOUND = 10000; //guarantees termination if stopCriterion() == Threshold

        if (graph.getNumberOfNodes() > 1) {
            var iter = 1;
            var maxMultiIterations = getMaxMultiIterations(options, level, maxLevel, graph.getNumberOfNodes());
            var activeForceVectorLength = options.getThreshold() + 1.0;

            NodeArray<DPoint> forces_rep = graph.newNodeArray();
            NodeArray<DPoint> forces_attr = graph.newNodeArray();
            NodeArray<DPoint> forces = graph.newNodeArray();
            NodeArray<DPoint> lastNodeMovement = graph.newNodeArray(); //stores the force vectors F of the last iterations (needed to avoid oscillations)

            var averageIdealEdgeLength = computeAverageIdealEdgeLength(graph, edgeAttributes);//needed for easy scaling of the forces

            var coolFactor = new Single<>(0f);
            while (((options.getStopCriterion() == FastMultiLayerMethodOptions.StopCriterion.FixedIterations) && (iter <= maxMultiIterations))
                    || ((options.getStopCriterion() == FastMultiLayerMethodOptions.StopCriterion.Threshold) && (activeForceVectorLength >= options.getThreshold()) && iter <= ITERBOUND)
                    || ((options.getStopCriterion() == FastMultiLayerMethodOptions.StopCriterion.FixedIterationsOrThreshold) && (iter <= maxMultiIterations) && (activeForceVectorLength >= options.getThreshold()))) {
                calculateForces(options, graph, layoutBox, averageIdealEdgeLength, coolFactor, nodeAttributes, edgeAttributes, forces, forces_attr, forces_rep, lastNodeMovement, iter, options.getFineTuningIterations());
                if (options.getStopCriterion() != FastMultiLayerMethodOptions.StopCriterion.FixedIterations)
                    activeForceVectorLength = getAverageLength(forces);
                iter++;
            }

            if (level == 0) {
                fixTwistedSplits(graph, nodeAttributes);
                callPostprocessingStep(options, graph, layoutBox, averageIdealEdgeLength, nodeAttributes, edgeAttributes, forces, forces_attr, forces_rep, lastNodeMovement);
            }
        }
    }

    private static double getAverageLength(NodeArray<DPoint> points) {
        var sum = 0.0;
        var count = 0;
        for (var point : points) {
            sum += point.norm();
            count++;
        }
        if (count > 0)
            return sum / count;
        else
            return 0;
    }

    private static void fixTwistedSplits(Graph graph, NodeArray<NodeAttributes> nodeAttributes) {
        for (var v : graph.nodes()) {
            // In order to be a simple two-way split, this node must lead to three others, two of which merge back
            // together in the same number of steps.
            var adjacentNodes = IteratorUtils.asList(v.adjacentNodes());
            if (adjacentNodes.size() == 3) {
                var direction1Finish = new Single<Node>();
                var direction1Path = new ArrayList<Node>();
                var direction1Steps = new Single<Integer>();
                followNodesUntilBranch(v, adjacentNodes.get(0), direction1Finish, direction1Path, direction1Steps);
                var direction2Finish = new Single<Node>();
                var direction2Path = new ArrayList<Node>();
                var direction2Steps = new Single<Integer>();
                followNodesUntilBranch(v, adjacentNodes.get(1), direction2Finish, direction2Path, direction2Steps);
                var direction3Finish = new Single<Node>();
                var direction3Path = new ArrayList<Node>();
                var direction3Steps = new Single<Integer>();
                followNodesUntilBranch(v, adjacentNodes.get(2), direction3Finish, direction3Path, direction3Steps);

                ArrayList<Node> path1 = null;
                ArrayList<Node> path2 = null;

                if (direction1Finish.get() == direction2Finish.get() && direction1Steps.get().equals(direction2Steps.get()) && direction1Finish.get() != direction3Finish.get()) {
                    path1 = direction1Path;
                    path2 = direction1Path;
                } else if (direction1Finish.get() == direction3Finish.get() && direction1Steps.get().equals(direction3Steps.get()) && direction1Finish.get() != direction2Finish.get()) {
                    path1 = direction1Path;
                    path2 = direction3Path;
                } else if (direction2Finish.get() == direction3Finish.get() && direction2Steps.get().equals(direction3Steps.get()) && direction2Finish.get() != direction1Finish.get()) {
                    path1 = direction2Path;
                    path2 = direction3Path;
                }
                if (path1 != null && path2 != null && path1.size() > 1 && path2.size() > 1) {
                    // If we got here, that means we've found a simple split! path1 and path2 store the nodes in order, so
                    // we check if any of them cross, and if so, we swap their positions to uncross them.
                    for (int i = 0; i < path1.size() - 1; i++) {
                        var path1Node1 = path1.get(i);
                        var path1Node2 = path1.get(i + 1);
                        var path2Node1 = path2.get(i);
                        var path2Node2 = path2.get(i + 1);

                        var path1Node1Position = nodeAttributes.get(path1Node1).getPosition();
                        var path1Node2Position = nodeAttributes.get(path1Node2).getPosition();
                        var path2Node1Position = nodeAttributes.get(path2Node1).getPosition();
                        var path2Node2Position = nodeAttributes.get(path2Node2).getPosition();

                        var line1 = new DLine(path1Node1Position, path1Node2Position);
                        var line2 = new DLine(path2Node1Position, path2Node2Position);

                        var intersectionPoint = new DPointMutable();

                        if (line1.intersection(line2, intersectionPoint, true)) {
                            nodeAttributes.get(path1Node2).setPosition(path2Node2Position);
                            nodeAttributes.get(path2Node2).setPosition(path1Node2Position);
                        }
                    }
                }
            }
        }
    }

    private static void followNodesUntilBranch(Node start, Node first, Single<Node> finish, ArrayList<Node> path, Single<Integer> steps) {
        var prev = start;
        var current = first;
        steps.set(0);
        var list = new ArrayList<Node>();
        while (true) {
            list.clear();
            for (var w : current.adjacentNodes()) {
                if (w != prev) {
                    list.add(w);
                }
            }
            if (list.size() != 1)
                break;
            prev = current;
            current = list.get(0);
            steps.set(steps.get() + 1);
            path.add(prev);
        }
        finish.set(current);
    }

    private static void smooth(FastMultiLayerMethodOptions options, Graph graph, NodeArray<NodeAttributes> nodeAttributes) {
        var diNodes = new ArrayList<Node>();
        graph.nodeStream().filter(v -> v.getDegree() == 2).forEach(diNodes::add);
        if (diNodes.size() > 0) {
            var bbox = DRect.computeBBox(nodeAttributes.values());
            for (var i = 0; i < options.getNumberRandomTries(); i++) {
                var angles = graph.newNodeDoubleArray();
                for (var v : diNodes) {
                    var u = v.getFirstAdjacentEdge().getOpposite(v);
                    var w = v.getLastAdjacentEdge().getOpposite(v);
                    angles.put(v, DPoint.angle(nodeAttributes.get(u).getPosition(), nodeAttributes.get(v).getPosition(), nodeAttributes.get(w).getPosition()));
                }
                diNodes.sort((v, w) -> Double.compare(Math.abs(Math.PI - angles.get(w)), Math.abs(Math.PI - angles.get(v))));
                if (Math.abs(Math.PI - angles.get(diNodes.get(0))) < 0.1)
                    break;
                for (var v : diNodes) {
                    if (Math.abs(Math.PI - angles.get(diNodes.get(0))) < 0.1)
                        break;
                    var u = v.getFirstAdjacentEdge().getOpposite(v);
                    var w = v.getLastAdjacentEdge().getOpposite(v);
                    // set to midpoint between (midpoint between u and w), and v
                    nodeAttributes.get(v).setPosition(nodeAttributes.get(u).getPosition().add(nodeAttributes.get(w).getPosition()).scaleBy(0.5).add(nodeAttributes.get(v).getPosition()).scaleBy(0.5));
                }
                DRect.fitToBox(nodeAttributes.values(), bbox);
            }
        }
    }

    private static void callPostprocessingStep(FastMultiLayerMethodOptions options, Graph graph, LayoutBox layoutBox, double averageIdealEdgeLength, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes, NodeArray<DPoint> F, NodeArray<DPoint> F_attr, NodeArray<DPoint> F_rep, NodeArray<DPoint> lastNodeMovement) {
        {
            var coolFactor = new Single<>(0f);
            for (int i = 1; i <= 10; i++)
                calculateForces(options, graph, layoutBox, averageIdealEdgeLength, coolFactor, nodeAttributes, edgeAttributes, F, F_attr, F_rep, lastNodeMovement, i, 1);
        }
        if (options.isResizeDrawing()) {
            adaptDrawingToIdealAverageEdgeLength(options, graph, nodeAttributes, edgeAttributes);
            layoutBox.update(graph, nodeAttributes);
        }

        {
            var coolFactor = new Single<>(0f);
            for (int i = 1; i <= options.getFineTuningIterations(); i++)
                calculateForces(options, graph, layoutBox, averageIdealEdgeLength, coolFactor, nodeAttributes, edgeAttributes, F, F_attr, F_rep, lastNodeMovement, i, 2);
        }

        if (options.isResizeDrawing())
            adaptDrawingToIdealAverageEdgeLength(options, graph, nodeAttributes, edgeAttributes);

    }

    private static void adaptDrawingToIdealAverageEdgeLength(FastMultiLayerMethodOptions options, Graph graph, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes) {
        var sumIdealEdgeLength = 0.0;
        var sumRealEdgeLength = 0.0;

        for (var e : graph.edges()) {
            sumIdealEdgeLength += edgeAttributes.get(e).getLength();
            sumRealEdgeLength += nodeAttributes.get(e.getTarget()).getPosition().distance(nodeAttributes.get(e.getSource()).getPosition());
        }

        var scalingFactor = (sumRealEdgeLength == 0 ? 1.0 : sumIdealEdgeLength / sumRealEdgeLength);

        for (var v : graph.nodes()) {
            var va = nodeAttributes.get(v);
            va.setPosition(options.getResizingScalar() * scalingFactor * va.getX(), options.getResizingScalar() * scalingFactor * va.getY());
        }
    }

    private static void calculateForces(FastMultiLayerMethodOptions options, Graph graph, LayoutBox layoutBox, double averageIdealEdgeLength,
                                        Single<Float> coolFactor, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes,
                                        NodeArray<DPoint> forces, NodeArray<DPoint> forces_attr, NodeArray<DPoint> forces_rep,
                                        NodeArray<DPoint> lastNodeMovement, int iter, int fineTuningStep) {

        if (options.getAllowedPositions() != FastMultiLayerMethodOptions.AllowedPositions.All)
            makePositionsInteger(graph, layoutBox, averageIdealEdgeLength, nodeAttributes);

        calculateAttractiveForces(options, graph, nodeAttributes, edgeAttributes, forces_attr);
        calculateRepulsiveForces(options, graph, layoutBox, nodeAttributes, edgeAttributes, forces_rep);
        addAttractiveRepulsiveForces(options, graph, layoutBox, averageIdealEdgeLength, coolFactor, forces_attr, forces_rep, forces, iter, fineTuningStep);
        preventOscillations(graph, forces, lastNodeMovement, iter);
        moveNodes(graph, nodeAttributes, forces);
        layoutBox.update(graph, nodeAttributes);
    }

    private static void makePositionsInteger(Graph graph, LayoutBox layoutBox, double averageIdealEdgeLength, NodeArray<NodeAttributes> nodeAttributes) {
        var maxIntegerPosition = 100 * averageIdealEdgeLength * graph.getNumberOfNodes() * graph.getNumberOfNodes();

        //restrict positions to lie in [-max_integer_position,max_integer_position]
        //X [-max_integer_position,max_integer_position]
        for (var v : graph.nodes()) {
            var va = nodeAttributes.get(v);
            if ((va.getX() > maxIntegerPosition) || (va.getY() > maxIntegerPosition) || (va.getX() < maxIntegerPosition * (-1.0)) || (va.getY() < maxIntegerPosition * (-1.0))) {

                var oldPoint = new DPoint(va.getX(), va.getY());
                var lt = new DPoint(maxIntegerPosition * (-1.0), maxIntegerPosition);
                var rt = new DPoint(maxIntegerPosition, maxIntegerPosition);
                var lb = new DPoint(maxIntegerPosition * (-1.0), maxIntegerPosition * (-1.0));
                var rb = new DPoint(maxIntegerPosition, maxIntegerPosition * (-1.0));

                var s = new DLine(DPoint.ORIGIN, oldPoint);
                var left_bound = new DLine(lb, lt);
                var right_bound = new DLine(rb, rt);
                var top_bound = new DLine(lt, rt);
                var bottom_bound = new DLine(lb, rb);

                var cross_point = new DPointMutable();
                if (s.intersection(left_bound, cross_point)) {
                    va.setPosition(cross_point);
                } else if (s.intersection(right_bound, cross_point)) {
                    va.setPosition(cross_point);
                } else if (s.intersection(top_bound, cross_point)) {
                    va.setPosition(cross_point);
                } else if (s.intersection(bottom_bound, cross_point)) {
                    va.setPosition(cross_point);
                } else System.err.println("Error FMMMLayout:: makePositionsInteger()");
            }
        }

        //make positions integer
        for (var v : graph.nodes()) {
            var va = nodeAttributes.get(v);
            var new_x = Math.floor(va.getX());
            var new_y = Math.floor(va.getY());
            if (new_x < layoutBox.getLeft()) {
                layoutBox.setLength(layoutBox.getLength() + 2);
                layoutBox.setLeft(layoutBox.getLeft() - 2);
            }
            if (new_y < layoutBox.getDown()) {
                layoutBox.setLength(layoutBox.getLength() + 2);
                layoutBox.setDown(layoutBox.getLeft() - 2);
            }
            va.setPosition(new_x, new_y);
        }
    }

    private static void calculateAttractiveForces(FastMultiLayerMethodOptions options, Graph graph, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes, NodeArray<DPoint> force) {
        //initialisation
        for (var v : graph.nodes())
            force.put(v, DPoint.ORIGIN);

        //calculation
        for (var e : graph.edges()) {
            var u = e.getSource();
            var v = e.getTarget();
            var vector_v_minus_u = nodeAttributes.get(v).getPosition().subtract(nodeAttributes.get(u).getPosition());
            var norm_v_minus_u = vector_v_minus_u.norm();
            DPoint fu;
            if (vector_v_minus_u.equals(DPoint.ORIGIN))
                fu = DPoint.ORIGIN;
            else // if(!N.f_near_machine_precision(norm_v_minus_u,f_u))
            {
                var scalar = attractionScalar(options, norm_v_minus_u, edgeAttributes.get(e).getLength()) / norm_v_minus_u;
                fu = new DPoint(scalar * vector_v_minus_u.getX(), scalar * vector_v_minus_u.getY());
            }
            force.put(v, force.get(v).subtract(fu));
            force.put(u, force.get(u).add(fu));
        }
    }

    private static void calculateRepulsiveForces(FastMultiLayerMethodOptions options, Graph graph, LayoutBox layoutBox, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes, NodeArray<DPoint> force) {
        switch (options.getRepulsiveForcesCalculation()) {
            case Exact -> {
                FruchtermanReingold.calculateExactRepulsiveForces(graph, nodeAttributes, force);
            }
            case GridApproximation -> {
                FruchtermanReingold.calculateApproxRepulsiveForces(options, graph, layoutBox, nodeAttributes, force);
            }

            /*
            default:
                MultipoleMethod.calculate_repulsive_forces(options,graph,box, nodeAttributes, force);
                break;
             */
        }
    }

    private static void addAttractiveRepulsiveForces(FastMultiLayerMethodOptions options, Graph graph, LayoutBox layoutBox, double averageIdealEdgeLength, Single<Float> coolFactor, NodeArray<DPoint> force_attr, NodeArray<DPoint> force_rep, NodeArray<DPoint> force, int iter, int fineTuningStep) {
        //set cool_factor
        if (!options.isCoolTemperature())
            coolFactor.set(1.0f);
        else if (fineTuningStep == 0) {
            if (iter == 1)
                coolFactor.set(options.getCoolValue());
            else
                coolFactor.set(coolFactor.get() * options.getCoolValue());
        }

        if (fineTuningStep == 1)
            coolFactor.set(coolFactor.get() / 10f); //decrease the temperature rapidly
        else if (fineTuningStep == 2) {
            if (iter <= options.getFineTuningIterations() - 5)
                coolFactor.set(options.getFineTuneScalar()); //decrease the temperature rapidly
            else
                coolFactor.set(options.getFineTuneScalar() / 10f);
        }

        //set the values for the spring strength and strength of the rep. force field
        double act_spring_strength;
        double act_rep_force_strength;
        if (fineTuningStep <= 1)//usual case
        {
            act_spring_strength = options.getSpringStrength();
            act_rep_force_strength = options.getRepForcesStrength();
        } else if (!options.isAdjustPostRepStrengthDynamically()) {
            act_spring_strength = options.getPostSpringStrength();
            act_rep_force_strength = options.getPostStrengthOfRepForces();
        } else //adjustPostRepStrengthDynamically())
        {
            act_spring_strength = options.getPostSpringStrength();
            act_rep_force_strength = getPostRepForceStrength(graph.getNumberOfNodes());
        }

        for (var v : graph.nodes()) {
            var force_attr_v = force_attr.get(v);
            var force_rep_v = force_rep.get(v);

            var force_v = new DPointMutable();

            force_v.setX(act_spring_strength * force_attr_v.getX() + act_rep_force_strength * force_rep_v.getX());
            force_v.setY(act_spring_strength * force_attr_v.getY() + act_rep_force_strength * force_rep_v.getY());
            force_v.setX(averageIdealEdgeLength * averageIdealEdgeLength * force_v.getX());
            force_v.setY(averageIdealEdgeLength * averageIdealEdgeLength * force_v.getY());

            var norm_f = force_v.norm();
            if (force_v.equals(DPoint.ORIGIN)) {
                // nothing
            } else if (NumericalStability.nearMachinePrecision(norm_f, force_v)) {
                layoutBox.restrictToBox(force_v);
            } else {
                var scalar = Math.min(norm_f * coolFactor.get() * options.getForceScalingFactor(), layoutBox.computeMaxRadius(iter)) / norm_f;
                force_v.setX(scalar * force_v.getX());
                force_v.setY(scalar * force_v.getY());
            }
            force.put(v, force_v.asDPoint());
        }
    }

    private static double getPostRepForceStrength(int n) {
        return Math.min(0.2, 400.0 / (double) n);
    }

    private static void preventOscillations(Graph graph, NodeArray<DPoint> force, NodeArray<DPoint> lastNodeMovement, int iter) {
        final double pi_times_1_over_6 = 0.52359878;
        final double pi_times_2_over_6 = 2 * pi_times_1_over_6;
        final double pi_times_3_over_6 = 3 * pi_times_1_over_6;
        final double pi_times_4_over_6 = 4 * pi_times_1_over_6;
        final double pi_times_5_over_6 = 5 * pi_times_1_over_6;
        final double pi_times_7_over_6 = 7 * pi_times_1_over_6;
        final double pi_times_8_over_6 = 8 * pi_times_1_over_6;
        final double pi_times_9_over_6 = 9 * pi_times_1_over_6;
        final double pi_times_10_over_6 = 10 * pi_times_1_over_6;
        final double pi_times_11_over_6 = 11 * pi_times_1_over_6;

        if (iter == 1) { // initialize last node movement
            for (var v : graph.nodes()) {
                lastNodeMovement.put(v, force.get(v));
            }
        } else if (iter > 1) {
            for (var v : graph.nodes()) {
                var force_new = force.get(v);
                var norm_new = force_new.norm();
                var force_old = lastNodeMovement.get(v);
                var norm_old = force_old.norm();
                if ((norm_new > 0) && (norm_old > 0)) {//if2
                    var quot_old_new = norm_old / norm_new;

                    //prevent oscillations
                    var fi = DPoint.angle(DPoint.ORIGIN, force_old, force_new);
                    if (((fi <= pi_times_1_over_6) || (fi >= pi_times_11_over_6)) && ((norm_new > (norm_old * 2.0)))) {
                        force.put(v, force_new.scaleBy(quot_old_new * 2.0));
                    } else if ((fi >= pi_times_1_over_6) && (fi <= pi_times_2_over_6) && (norm_new > (norm_old * 1.5))) {
                        force.put(v, force_new.scaleBy(quot_old_new * 1.5));
                    } else if ((fi >= pi_times_2_over_6) && (fi <= pi_times_3_over_6) && (norm_new > (norm_old))) {
                        force.put(v, force_new.scaleBy(quot_old_new));
                    } else if ((fi >= pi_times_3_over_6) && (fi <= pi_times_4_over_6) && (norm_new > (norm_old * 0.66666666))) {
                        force.put(v, force_new.scaleBy(quot_old_new * 0.66666666));
                    } else if ((fi >= pi_times_4_over_6) && (fi <= pi_times_5_over_6) && (norm_new > (norm_old * 0.5))) {
                        force.put(v, force_new.scaleBy(quot_old_new * 0.5));
                    } else if ((fi >= pi_times_5_over_6) && (fi <= pi_times_7_over_6) && (norm_new > (norm_old * 0.33333333))) {
                        force.put(v, force_new.scaleBy(quot_old_new * 0.33333333));
                    } else if ((fi >= pi_times_7_over_6) && (fi <= pi_times_8_over_6) && (norm_new > (norm_old * 0.5))) {
                        force.put(v, force_new.scaleBy(quot_old_new * 0.5));
                    } else if ((fi >= pi_times_8_over_6) && (fi <= pi_times_9_over_6) && (norm_new > (norm_old * 0.66666666))) {
                        force.put(v, force_new.scaleBy(quot_old_new * 0.66666666));
                    } else if ((fi >= pi_times_9_over_6) && (fi <= pi_times_10_over_6) && (norm_new > (norm_old))) {
                        force.put(v, force_new.scaleBy(quot_old_new));
                    } else if ((fi >= pi_times_10_over_6) && (fi <= pi_times_11_over_6) && (norm_new > (norm_old * 1.5))) {
                        force.put(v, force_new.scaleBy(quot_old_new * 1.5));
                    }
                }
                lastNodeMovement.put(v, force.get(v));
            }
        }
    }

    private static void moveNodes(Graph graph, NodeArray<NodeAttributes> nodeAttributes, NodeArray<DPoint> forces) {
        for (var v : graph.nodes()) {
            var na = nodeAttributes.get(v);
            na.setPosition(na.getPosition().add(forces.get(v)));
        }
    }

    private static double computeAverageIdealEdgeLength(Graph graph, EdgeArray<EdgeAttributes> edgeAttributes) {
        if (graph.getNumberOfEdges() > 0) {
            var total = 0.0;
            var count = 0;
            for (var ea : edgeAttributes.values()) {
                total += ea.getLength();
                count++;
            }
            return total / count;
        } else
            return 50;
    }

    private static int getMaxMultiIterations(FastMultiLayerMethodOptions options, int level, int maxLevel, int numberOfNodes) {
        int iter;
        if (options.getMaxIterChange() == FastMultiLayerMethodOptions.MaxIterChange.Constant) //nothing to do
            iter = options.getFixedIterations();
        else if (options.getMaxIterChange() == FastMultiLayerMethodOptions.MaxIterChange.LinearlyDecreasing) //linearly decreasing values
        {
            if (maxLevel == 0)
                iter = options.getFixedIterations() + ((options.getMaxIterFactor() - 1) * options.getFixedIterations());
            else
                iter = options.getFixedIterations() + (int) ((double) level / (double) maxLevel) * ((options.getMaxIterFactor() - 1)) * options.getFixedIterations();
        } else //maxIterChange == RapidlyDecreasing
        {
            if (level == maxLevel)
                iter = options.getFixedIterations() + ((options.getMaxIterFactor() - 1) * options.getFixedIterations());
            else if (level == maxLevel - 1)
                iter = options.getFixedIterations() + (int) (0.5 * (options.getMaxIterFactor() - 1) * options.getFixedIterations());
            else if (level == maxLevel - 2)
                iter = options.getFixedIterations() + (int) (0.25 * (options.getMaxIterFactor() - 1) * options.getFixedIterations());
            else // level >= maxLevel - 3
                iter = options.getFixedIterations();
        }

        //helps to get good drawings for small graphs and graphs with few multi-levels
        if ((numberOfNodes <= 500) && (iter < 100))
            return 100;
        else
            return iter;

    }

    private static double attractionScalar(FastMultiLayerMethodOptions options, double d, double ind_ideal_edge_length) {
        double s;
        switch (options.getForceModel()) {
            default /* includes  FruchtermanReingold */ -> s = d * d / (ind_ideal_edge_length * ind_ideal_edge_length * ind_ideal_edge_length);
            case Eades -> {
                double c = 10;
                if (d == 0)
                    s = -1e10;
                else
                    s = c * Math.log(d / ind_ideal_edge_length) / (Math.log(2) * ind_ideal_edge_length);
            }
            case New -> {
                double c = Math.log(d / ind_ideal_edge_length) / Math.log(2);
                if (d > 0)
                    s = c * d * d / (ind_ideal_edge_length * ind_ideal_edge_length * ind_ideal_edge_length);
                else
                    s = -1e10;
            }
        }
        return s;
    }

    private static void createInitialPlacement(FastMultiLayerMethodOptions options, Graph graph, NodeArray<NodeAttributes> nodeAttributes, EdgeArray<EdgeAttributes> edgeAttributes, LayoutBox layoutBox) {
        if (options.getInitialPlacementForces() == FastMultiLayerMethodOptions.InitialPlacementForces.KeepPositions) {
            layoutBox.init(graph, nodeAttributes);
        } else if (options.getInitialPlacementForces() == FastMultiLayerMethodOptions.InitialPlacementForces.UniformGrid) {
            layoutBox.init(graph, nodeAttributes);
            var level = (int) Math.ceil(Math.log(graph.getNumberOfNodes()) / Math.log(4));
            var m = (int) (Math.pow(2.0, level) - 1);
            double boxLengthAll = layoutBox.getLength() / (m + 1); //box length for boxes at the lowest level (depth)

            var allNodes = graph.getNodesAsList();
            var v = allNodes.get(0);
            var k = 0;
            var i = 0;
            boolean finished = false;
            while ((!finished) && (i <= m)) {//while1
                var j = 0;
                while ((!finished) && (j <= m)) {//while2
                    nodeAttributes.get(v).setPosition(layoutBox.getLength() * i / (m + 1) + boxLengthAll / 2, layoutBox.getLength() * j / (m + 1) + boxLengthAll / 2);
                    if (k == graph.getNumberOfNodes() - 1)
                        finished = true;
                    else {
                        k++;
                        v = allNodes.get(k);
                    }
                    j++;
                }
                i++;
            }
        } else { // random
            layoutBox.init(graph, nodeAttributes);
            var random = new Random();
            if (options.getInitialPlacementForces() == FastMultiLayerMethodOptions.InitialPlacementForces.RandomTime)//(RANDOM based on actual CPU-time)
                random.setSeed(System.currentTimeMillis());
            for (var v : graph.nodes()) {
                nodeAttributes.get(v).setPosition(new DPoint(random.nextDouble() * (layoutBox.getLength() - 2) + 1, random.nextDouble() * (layoutBox.getLength() - 2) + 1));
            }
        }//(random)
        layoutBox.update(graph, nodeAttributes);
    }

    /**
     * point
     */
    public interface Point {
        double getX();

        double getY();
    }

    /**
     * a rectangle
     */
    public interface Rectangle {
        double getMinX();

        double getMinY();

        double getWidth();

        double getHeight();
    }
}
