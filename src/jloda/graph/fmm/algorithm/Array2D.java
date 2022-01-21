/*
 * Array2D.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * a two dimension array of fixed size
 * Daniel Huson, 3.2021
 */
public class Array2D<T> {
    private final Object[][] table;

    public Array2D(int numberOfRows, int numberOfCols) {
        table = new Object[numberOfRows][numberOfCols];
    }

    public T get(int row, int col) {
        return (T) table[row][col];
    }

    public T getOrDefault(int row, int col, T defaultValue) {
        T result = get(row, col);
        if (result == null)
            return defaultValue;
        else
            return result;
    }

    public T computeIfAbsent(int row, int col, BiFunction<Integer, Integer, T> function) {
        T result = get(row, col);
        if (result == null) {
            result = function.apply(row, col);
            if (result != null)
                put(row, col, result);
        }
        return result;
    }

    public void put(int row, int col, T value) {
        table[row][col] = value;
    }

    public void clear() {
        for (var row : table) {
            Arrays.fill(row, null);
        }
    }

    public int getNumberOfRows() {
        return table.length;
    }

    public int getNumberOfColumns() {
        return getNumberOfRows() == 0 ? 0 : table[0].length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Array2D<?> that = (Array2D<?>) o;
        return getNumberOfRows() == that.getNumberOfRows() && getNumberOfColumns() == that.getNumberOfColumns()
                && Arrays.deepEquals(table, that.table);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(table);
    }
}
