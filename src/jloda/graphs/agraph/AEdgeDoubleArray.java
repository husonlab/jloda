/*
 *  AEdgeArray.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.graphs.agraph;

import jloda.graphs.interfaces.IEdgeDoubleArray;

import java.util.Arrays;
import java.util.Iterator;

/**
 * an edge double array
 * Daniel Huson, 3.2021
 */
public class AEdgeDoubleArray implements IEdgeDoubleArray<AGraph.Edge> {
    private final double[] array;

    public AEdgeDoubleArray(AGraph aGraph) {
        array = new double[aGraph.getNumberOfEdges()];
    }

    @Override
    public void put(AGraph.Edge a, Double value) {
        array[a.getId()] = value;
    }

    @Override
    public Double getValue(AGraph.Edge a) {
        return array[a.getId()];
    }

    @Override
    public void clear() {
        Arrays.fill(array, 0);
    }

    public void fill(double value) {
        Arrays.fill(array, value);
    }

    @Override
    public Iterable<Double> values() {
        return () -> new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < array.length;
            }

            @Override
            public Double next() {
                double result = array[i];
                i = i + 1;
                return result;
            }
        };
    }
}
