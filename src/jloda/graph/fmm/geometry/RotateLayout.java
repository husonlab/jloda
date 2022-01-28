/*
 * RotateLayout.java Copyright (C) 2022 Daniel H. Huson
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
package jloda.graph.fmm.geometry;

import jloda.graph.Graph;
import jloda.graph.NodeArray;
import jloda.graph.fmm.FastMultiLayerMethodOptions;
import jloda.graph.fmm.algorithm.NodeAttributes;

/**
 * implementation of the fast multilayer method (note: without multipole algorithm)
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class RotateLayout {
    public static void apply(FastMultiLayerMethodOptions options, Graph graph, NodeArray<NodeAttributes> nodeAttributes) {
        NodeArray<DPoint> bestCoordinates = graph.newNodeArray();
        NodeArray<DPoint> originalCoordinates = graph.newNodeArray();

        for (var v : graph.nodes()) {
            var pos = nodeAttributes.get(v).getPosition();
            originalCoordinates.put(v, pos);
            bestCoordinates.put(v, pos);
        }

        // find the best
        var r_best = DRect.computeBBox(nodeAttributes.values());
        var best_area = r_best.getArea();

        for (var j = 0; j <= options.getStepsForRotatingComponents(); j++) {
            //calculate new positions for the nodes, the new rectangle and area
            var angle = 0.5 * Math.PI * (double) j / (double) (options.getStepsForRotatingComponents() + 1) - 0.25 * Math.PI;
            var sin_j = Math.sin(angle);
            var cos_j = Math.cos(angle);
            for (var v : graph.nodes()) {
                var old = originalCoordinates.get(v);
                var newPos = new DPoint(cos_j * old.getX() - sin_j * old.getY(), sin_j * old.getX() + cos_j * old.getY());
                nodeAttributes.get(v).setPosition(newPos);
            }

            var r_act = DRect.computeBBox(nodeAttributes.values());
            var act_area = r_act.getArea();

            //store placement of the nodes with minimal area
            if (act_area < best_area) {
                r_best = r_act;
                best_area = act_area;
                for (var v : graph.nodes()) {
                    bestCoordinates.put(v, nodeAttributes.get(v).getPosition());
                }
            }
        }

        // If a component is taller than it is wide, rotate it 90 degrees.
        var ratio = r_best.getWidth() / r_best.getHeight();
        if (ratio < 1) {
            for (var v : graph.nodes()) {
                var best = bestCoordinates.get(v);
                bestCoordinates.put(v, new DPoint(-best.getY(), best.getX()));
            }
        }

        //save the computed information
        for (var v : graph.nodes()) {
            nodeAttributes.get(v).setPosition(bestCoordinates.get(v));
        }
    }
}
