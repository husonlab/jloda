/*
 * FruchtermanReingold.java Copyright (C) 2022 Daniel H. Huson
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
package jloda.graph.fmm.algorithm;

import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.fmm.FastMultiLayerMethodOptions;
import jloda.graph.fmm.geometry.DPoint;
import jloda.graph.fmm.geometry.DPointMutable;
import jloda.graph.fmm.geometry.LayoutBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * implementation of the fast multilayer method (note: without multipole algorithm)
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class FruchtermanReingold {
    /**
     * calculate exact repulsive forces using Fruchterman-Reingold
     *
	 */
    public static void calculateExactRepulsiveForces(Graph graph, NodeArray<NodeAttributes> nodeAttributes, NodeArray<DPoint> force) {
        for (var v : graph.nodes()) {
            force.put(v, DPoint.ORIGIN);
        }

        for (var u : graph.nodes()) {
            var pos_u = nodeAttributes.get(u).getPosition();
            for (var v : graph.nodes(u)) {
                var pos_v = nodeAttributes.get(v).getPosition();
                if (pos_u.equals(pos_v)) {//if2  (Exception handling if two nodes have the same position)
                    pos_u = NumericalStability.chooseDistinctRandomPointInRadiusEpsilon(pos_u);
                }
                var vector_v_minus_u = pos_v.subtract(pos_u);
                var norm_v_minus_u = vector_v_minus_u.norm();
                var f_rep_u_on_v = new DPointMutable();

                if (!NumericalStability.repulsionNearMachinePrecision(norm_v_minus_u, f_rep_u_on_v)) {
                    var scalar = repulsionScalar(norm_v_minus_u) / norm_v_minus_u;
                    f_rep_u_on_v.setPosition(vector_v_minus_u.scaleBy(scalar));
                }
                force.put(v, force.get(v).add(f_rep_u_on_v));
                force.put(u, force.get(u).subtract(f_rep_u_on_v));
            }
        }
    }

    public static void calculateApproxRepulsiveForces(FastMultiLayerMethodOptions options, Graph graph, LayoutBox layoutBox, NodeArray<NodeAttributes> nodeAttributes, NodeArray<DPoint> force) {
        for (var v : graph.nodes()) {
            force.put(v, DPoint.ORIGIN);
        }

        var size = (int) (Math.sqrt(graph.getNumberOfNodes()) / options.getFrGridQuotient());

        if (size <= 1) {
            calculateExactRepulsiveForces(graph, nodeAttributes, force);
            return;
        }

        final var grid = new Array2D<List<Node>>(size, size);
        final var gridBoxLength = layoutBox.getLength() / (double) (size);

        for (var v : graph.nodes()) {
            var va = nodeAttributes.get(v);
            var x = va.getX() - layoutBox.getLeft();
            var y = va.getY() - layoutBox.getDown();
            var x_index = (int) (x / gridBoxLength);
            var y_index = (int) (y / gridBoxLength);
            grid.computeIfAbsent(x_index, y_index, (r, c) -> new ArrayList<>()).add(v);
        }

        //force calculation

        for (int row_u = 0; row_u < size; row_u++) {
            for (int col_u = 0; col_u < size; col_u++) {
                for (var u : grid.getOrDefault(row_u, col_u, Collections.emptyList())) {
                    var pos_u = nodeAttributes.get(u).getPosition();
                    for (var row_v = row_u; row_v <= row_u + 1 && row_v < size; row_v++) {
                        for (var col_v = col_u; col_v <= col_u + 1 && col_v < size; col_v++) {
                            for (var v : grid.getOrDefault(row_v, col_v, Collections.emptyList())) {
                                var pos_v = nodeAttributes.get(v).getPosition();
                                if (pos_u.equals(pos_v)) {
                                    pos_u = NumericalStability.chooseDistinctRandomPointInRadiusEpsilon(pos_u);
                                }
                                var vector_v_minus_u = pos_v.subtract(pos_u);
                                var norm_v_minus_u = vector_v_minus_u.norm();
                                var f_rep_u_on_v = new DPointMutable();
                                if (!NumericalStability.repulsionNearMachinePrecision(norm_v_minus_u, f_rep_u_on_v)) {
                                    var scalar = repulsionScalar(norm_v_minus_u) / norm_v_minus_u;
                                    f_rep_u_on_v.setPosition(vector_v_minus_u.scaleBy(scalar));
                                }
                                force.put(v, force.get(v).add(f_rep_u_on_v));
                                force.put(u, force.get(u).subtract(f_rep_u_on_v));
                            }
                        }
                    }
                }
            }
        }
    }

    private static double repulsionScalar(double d) {
        if (d > 0) {
            return 1 / d;

        } else {
            System.err.println("Error: repulsionScalar(): d=0");
            return 0;
        }
    }
}
