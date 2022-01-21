/*
 * MultiComponents.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.fx.util.ProgramExecutorService;
import jloda.graph.*;
import jloda.graph.algorithms.Simple;
import jloda.graph.fmm.geometry.DPoint;
import jloda.graph.fmm.geometry.DRect;
import jloda.util.Single;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * applies the layout algorithm to a graph with multiple connected components
 * Daniel Huson, 5.2021
 */
public class MultiComponents {

    public static FastMultiLayerMethodLayout.Rectangle apply(FastMultiLayerMethodOptions options, double maxWidth, double maxLineWidth,
                                                             double hGap, double vGap,
                                                             Graph graph0, Function<Edge, ? extends Number> edgeWeights0,
                                                             BiConsumer<Node, FastMultiLayerMethodLayout.Point> result0) throws Exception {

        final Graph graph;
        Function<Edge, ? extends Number> edgeWeights;
        final BiConsumer<Node, FastMultiLayerMethodLayout.Point> result;
        if (graph0.isSimple()) {
            graph = graph0;
            edgeWeights = edgeWeights0;
            result = result0;
        } else {
            graph = new Graph();
            NodeArray<Node> tar2srcNode = graph.newNodeArray();
            EdgeArray<Edge> tar2srcEdge = graph.newEdgeArray();
            Simple.makeSimple(graph0, graph, tar2srcNode, tar2srcEdge);
            result = (v, p) -> result0.accept(tar2srcNode.get(v), p);
            edgeWeights = e -> edgeWeights0.apply(tar2srcEdge.get(e));
        }

        if (graph.isConnected()) {
            return FastMultiLayerMethodLayout.apply(options, graph, edgeWeights, result);
        } else {
            NodeArray<Node> src2tar = graph.newNodeArray();
            var components = graph.extractAllConnectedComponents(src2tar);
            var service = Executors.newFixedThreadPool(ProgramExecutorService.getNumberOfCoresToUse());
            var exception = new Single<Exception>();
            var list = new ArrayList<Component>();
            for (var component : components) {
                service.submit(() -> {
                    if (exception.isNull()) {
                        NodeArray<Node> tar2src = component.newNodeArray();
                        for (var v : src2tar.keys()) {
                            var w = src2tar.get(v);
                            if (w.getOwner() == component) {
                                tar2src.put(w, v);
                            }
                        }
                        NodeArray<FastMultiLayerMethodLayout.Point> coords = component.newNodeArray();
                        FastMultiLayerMethodLayout.Rectangle rect;
                        try {
                            rect = FastMultiLayerMethodLayout.apply(options, component, e -> 1, coords::put);
                            synchronized (list) {
                                list.add(new Component(component, rect, tar2src, coords));
                            }
                        } catch (Exception e) {
                            exception.setIfCurrentValueIsNull(e);
                        }
                    }
                });
            }
            service.shutdown();
            service.awaitTermination(1000, TimeUnit.DAYS);
            if (exception.isNotNull())
                throw exception.get();
            list.sort((a, b) -> Double.compare(b.getArea(), a.getArea())); // sort by decreasing area

            var scale = -1d;
            var h = 0d;
            var v = 0d;
            var lineMaxV = 0d;
            for (var component : list) {
                if (scale == -1d)
                    scale = maxWidth / component.getRectangle().getWidth();
                var layoutWidth = scale * component.getRectangle().getWidth();
                if (h + layoutWidth > maxLineWidth) {
                    h = 0;
                    if (lineMaxV > 0)
                        v = lineMaxV + vGap;
                }
                var rect = component.mapToRect(h, v, scale, result);
                h = rect.getMaxX() + hGap;
                lineMaxV = Math.max(lineMaxV, rect.getMaxY());
            }
            return new DRect(0, 0, maxLineWidth, v + lineMaxV);
        }
    }

    private static class Component {
        private final Graph graph;
        private final FastMultiLayerMethodLayout.Rectangle rectangle;
        private final NodeArray<Node> tar2src;
        private final NodeArray<FastMultiLayerMethodLayout.Point> points;

        public Component(Graph graph, FastMultiLayerMethodLayout.Rectangle rectangle, NodeArray<Node> tar2src, NodeArray<FastMultiLayerMethodLayout.Point> points) {
            this.graph = graph;
            this.rectangle = rectangle;
            this.tar2src = tar2src;
            this.points = points;
        }

        public Graph getGraph() {
            return graph;
        }

        public FastMultiLayerMethodLayout.Rectangle getRectangle() {
            return rectangle;
        }

        public NodeArray<Node> getTar2src() {
            return tar2src;
        }

        public NodeArray<FastMultiLayerMethodLayout.Point> getPoints() {
            return points;
        }

        public double getArea() {
            return rectangle.getWidth() * rectangle.getHeight();
        }

        public DRect mapToRect(double dx, double dy, double scale, BiConsumer<Node, FastMultiLayerMethodLayout.Point> result) {
            Function<Double, Double> mapX = x -> (x - rectangle.getMinX()) * scale + dx;
            Function<Double, Double> mapY = y -> (y - rectangle.getMinY()) * scale + dy;

            var minX = Double.MAX_VALUE;
            var maxX = Double.MIN_VALUE;
            var minY = Double.MAX_VALUE;
            var maxY = Double.MIN_VALUE;

            for (var a : graph.nodes()) {
                var p = points.get(a);
                var x = mapX.apply(p.getX());
                var y = mapY.apply(p.getY());

                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);

                result.accept(tar2src.get(a), new DPoint(x, y));
            }
            return new DRect(minX, minY, maxX, maxY);
        }
    }
}
