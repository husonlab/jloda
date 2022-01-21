/*
 * LayoutBox.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.graph.fmm.algorithm.NodeAttributes;

import java.util.Objects;

/**
 * implementation of the fast multilayer method (note: without multipole algorithm)
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class LayoutBox {
    private final DPointMutable leftBottomCorner = new DPointMutable(0, 0);
    private double length = 100;

    public LayoutBox() {
    }

    public DPoint getLeftBottomCorner() {
        return leftBottomCorner;
    }

    public void setLeftBottomCorner(double left, double down) {
        leftBottomCorner.setPosition(left, down);
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getLeft() {
        return leftBottomCorner.getX();
    }

    public void setLeft(double left) {
        leftBottomCorner.setX(left);
    }

    public double getDown() {
        return leftBottomCorner.getY();
    }

    public void setDown(double down) {
        leftBottomCorner.setY(down);
    }

    public double computeMaxRadius(int iter) {
        return (iter == 1) ? length / 1000 : length / 5;
    }

    public void init(Graph graph, NodeArray<NodeAttributes> nodeAttributes) {
        final double MIN_NODE_SIZE = 10;

        var w = 0.0;
        var h = 0.0;
        for (var v : graph.nodes()) {
            w += Math.max(nodeAttributes.get(v).getWidth(), MIN_NODE_SIZE);
            h += Math.max(nodeAttributes.get(v).getHeight(), MIN_NODE_SIZE);
        }
        setLength(Math.ceil(Math.max(w, h) * 1.01 + 2));
        setLeftBottomCorner(0, 0);
    }

    public void update(Graph graph, NodeArray<NodeAttributes> nodeAttributes) {
        if (nodeAttributes.size() > 0) {
            var xMin = Double.MAX_VALUE;
            var xMax = Double.MIN_VALUE;
            var yMin = Double.MAX_VALUE;
            var yMax = Double.MIN_VALUE;

            for (var na : nodeAttributes) {
                var pt = na.getPosition();
                xMin = Math.min(xMin, pt.getX());
                xMax = Math.max(xMax, pt.getX());
                yMin = Math.min(yMin, pt.getY());
                yMax = Math.max(yMax, pt.getY());
            }
            setLeftBottomCorner(Math.floor(xMin - 1), Math.floor(yMin - 1));
            setLength(Math.ceil(Math.max(xMax - xMin, yMax - yMin)) * 1.01 + 2);

            if (length <= 2) {
                length = graph.getNumberOfNodes() * 20;
                setLeftBottomCorner(Math.floor(xMin) - length / 2, Math.floor(yMin) - length / 2);
            }
        }
    }

    public void restrictToBox(DPointMutable force) {
        double x_min = getLeft();
        double x_max = getLeft() + getLength();
        double y_min = getDown();
        double y_max = getDown() + getLength();
        if (force.getX() < x_min)
            force.setX(x_min);
        else if (force.getX() > x_max)
            force.setX(x_max);
        if (force.getY() < y_min)
            force.setY(y_min);
        else if (force.getY() > y_max)
            force.setY(y_max);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LayoutBox)) return false;
        LayoutBox layoutBox = (LayoutBox) o;
        return Double.compare(layoutBox.length, length) == 0 && Objects.equals(leftBottomCorner, layoutBox.leftBottomCorner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftBottomCorner, length);
    }
}
