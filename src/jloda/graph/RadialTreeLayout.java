/*
 * RadialTreeLayout.java Copyright (C) 2019. Daniel H. Huson
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

import java.util.function.Function;

/**
 * radial tree layout
 * Daniel Huson, 3.2019
 */
public class RadialTreeLayout {
    private final static double DEG_TO_RAD_FACTOR = Math.PI / 180.0;

    /**
     * applies a radial tree layout using the given set of edges
     *
     * @param graph
     * @param edges
     * @param edge2length
     * @return node coordinates
     */
    public static NodeArray<float[]> apply(Graph graph, EdgeSet edges, Function<Edge, Float> edge2length) {
        int highestDegree = -1;
        Node root = null;
        int numberOfLeaves = 0;
        for (Object obj : graph.nodes()) {
            final Node v = (Node) obj;
            final int degree = getDegree(v, edges);
            if (degree == 1) {
                numberOfLeaves++;
            }
            if (degree > highestDegree) {
                highestDegree = degree;
                root = v;
            }
        }

        if (root == null)
            throw new RuntimeException("Graph is empty");

        directAwayFromRoot(root, null, edges);

        final EdgeFloatArray edge2Angle = new EdgeFloatArray(graph); // angle of edge
        setAnglesForCircularLayoutRec(root, null, edges, 0, numberOfLeaves, edge2Angle);

        final NodeArray<float[]> result = new NodeArray<>(graph);
        computeNodeLocationsForRadialRec(root, new float[]{0, 0}, edges, edge2length, edge2Angle, result);
        return result;
    }

    /**
     * Recursively determines the angle of every edge in a circular layout
     *
     * @param v
     * @param e
     * @param nextLeafNum
     * @param angleParts
     * @param edgeAngles
     * @return number of leaves visited
     */
    private static int setAnglesForCircularLayoutRec(final Node v, final Edge e, EdgeSet edges, int nextLeafNum, final int angleParts, final EdgeFloatArray edgeAngles) {
        if (getDegree(v, edges) == 1) { // leaf
            if (e != null)
                edgeAngles.put(e, 360f / angleParts * nextLeafNum);
            return nextLeafNum + 1;
        } else {
            final float firstLeaf = nextLeafNum;
            float firstAngle = Float.MIN_VALUE;

            for (Edge f : v.outEdges()) {
                if (edges.contains(f)) {
                    nextLeafNum = setAnglesForCircularLayoutRec(f.getTarget(), f, edges, nextLeafNum, angleParts, edgeAngles);
                    final float angle = edgeAngles.get(f);
                    if (firstAngle == Float.MIN_VALUE)
                        firstAngle = angle;
                }
            }

            if (e != null) {
                edgeAngles.put(e, 180f / angleParts * (firstLeaf + nextLeafNum - 1));
            }
            return nextLeafNum;
        }
    }

    /**
     * set the locations of all nodes in a radial tree layout
     *
     * @param v
     * @param vPoint
     * @param edge2length
     * @param edgeAngles
     * @param node2point
     */
    private static void computeNodeLocationsForRadialRec(Node v, float[] vPoint, EdgeSet edges, Function<Edge, Float> edge2length, EdgeFloatArray edgeAngles, NodeArray<float[]> node2point) {
        node2point.put(v, vPoint);
        for (Edge f : v.outEdges()) {
            if (edges.contains(f)) {
                final Node w = f.getOpposite(v);
                final float[] wLocation = translateByAngle(vPoint, edgeAngles.get(f), edge2length.apply(f));
                node2point.put(w, wLocation);
                computeNodeLocationsForRadialRec(w, wLocation, edges, edge2length, edgeAngles, node2point);
            }
        }
    }

    /**
     * Translate a point in the direction specified by an angle.
     *
     * @param apt   Point2D
     * @param alpha double in degrees
     * @param dist  double
     * @return Point2D
     */
    private static float[] translateByAngle(float[] apt, float alpha, float dist) {
        double dx = dist * Math.cos(DEG_TO_RAD_FACTOR * alpha);
        double dy = dist * Math.sin(DEG_TO_RAD_FACTOR * alpha);
        if (Math.abs(dx) < 0.000001)
            dx = 0;
        if (Math.abs(dy) < 0.000001)
            dy = 0;
        return new float[]{(float) (apt[0] + dx), (float) (apt[1] + dy)};
    }

    /**
     * get the degree induced by the set of edges
     *
     * @param v
     * @param edges
     * @return induced degree
     */
    private static int getDegree(Node v, EdgeSet edges) {
        int degree = 0;
        for (Edge e : v.adjacentEdges()) {
            if (edges.contains(e))
                degree++;
        }
        return degree;
    }

    private static void directAwayFromRoot(Node v, Edge e, EdgeSet edges) {
        for (Edge f : v.adjacentEdges()) {
            if (f != e && edges.contains(f) && f.getTarget() == v)
                f.reverse();
        }
        for (Edge f : v.outEdges()) {
            if (edges.contains(f))
                directAwayFromRoot(f.getTarget(), f, edges);
        }
    }
}
