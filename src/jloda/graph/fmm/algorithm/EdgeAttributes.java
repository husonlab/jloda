/*
 * EdgeAttributes.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.graph.Edge;

/**
 * implementation of the fast multilayer method (note: without multipole algorithm)
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class EdgeAttributes {
    private double length;

    private Edge originalEdge;
    private Edge subgraphEdge;

    private boolean moonEdge;
    private boolean extraEdge;

    public EdgeAttributes() {
    }

    public EdgeAttributes(double length) {
        setLength(length);
    }

    public EdgeAttributes(double length, Edge originalEdge, Edge subgraphEdge) {
        this.length = (float) length;
        this.originalEdge = originalEdge;
        this.subgraphEdge = subgraphEdge;
    }

    public void setAttributes(double length, Edge originalEdge, Edge subgraphEdge) {
        this.length = (float) length;
        this.originalEdge = originalEdge;
        this.subgraphEdge = subgraphEdge;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public Edge getOriginalEdge() {
        return originalEdge;
    }

    public void setOriginalEdge(Edge originalEdge) {
        this.originalEdge = originalEdge;
    }

    public Edge getSubgraphEdge() {
        return subgraphEdge;
    }

    public void setSubgraphEdge(Edge subgraphEdge) {
        this.subgraphEdge = subgraphEdge;
    }

    public Edge getCopyEdge() {
        return subgraphEdge;
    }

    public void setCopyEdge(Edge copyEdge) {
        this.subgraphEdge = copyEdge;
    }

    // used in mult-level step:
    public Edge getHigherLevelEdge() {
        return subgraphEdge;
    }

    public void setHigherLevelEdge(Edge higherLevelEdge) {
        this.subgraphEdge = higherLevelEdge;
    }

    public boolean isMoonEdge() {
        return moonEdge;
    }

    public void makeMoonEdge() {
        this.moonEdge = true;
    }

    public boolean isExtraEdge() {
        return extraEdge;
    }

    public void makeExtraEdge() {
        this.extraEdge = true;
    }

    public void initMultiLevelValues() {
        subgraphEdge = null;
        moonEdge = false;
    }
}
