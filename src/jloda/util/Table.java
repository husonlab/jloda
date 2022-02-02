/*
 * Table.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.util;

import java.util.*;

/**
 * a two dimensional table, similar to Guave Table
 * Daniel Huson, 12.2012
 */
public class Table<R, C, V> {
    private final Map<R, Map<C, V>> dataMap = new HashMap<>();

    /**
     * constructor
     */
    public Table() {
    }

    /**
     * does table contain cell
     *
	 */
    public boolean contains(R rowKey, C columnKey) {
        if (rowKey == null || columnKey == null)
            return false;
        final Map<C, V> row = dataMap.get(rowKey);
        return row != null && row.containsKey(columnKey);
    }

    /**
     * row contained?
     *
	 */
    public boolean containsRow(R rowKey) {
        return dataMap.containsKey(rowKey);
    }

    /**
     * column contained?
     *
	 */
    public boolean containsColumn(C columnKey) {
        if (columnKey == null)
            return false;
        for (Map<C, V> row : dataMap.values()) {
            if (row.containsKey(columnKey))
                return true;
        }
        return false;
    }

    /**
     * does table contain given value
     *
	 */
    public boolean containsValue(V value) {
        if (value == null)
            return false;
        for (Map<C, V> row : dataMap.values()) {
            if (row.containsValue(value))
                return true;
        }
        return false;

    }

    /**
     * get the value or null
     *
	 */
    public V get(R rowKey, C columnKey) {
        if (rowKey == null || columnKey == null)
            return null;
        final Map<C, V> row = dataMap.get(rowKey);
        if (row == null)
            return null;
        return row.get(columnKey);
    }

    /**
     * is table empty?
     *
	 */
    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    /**
     * get the size of the table
     *
	 */
    public int size() {
        int size = 0;
        for (Map<C, V> row : dataMap.values()) {
            size += row.size();
        }
        return size;
    }

    /**
     * compares equality
     * todo: this needs to be fixed
     *
	 */
    public boolean equals(Object obj) {
        return (obj instanceof Table) && dataMap.equals(obj);
    }

    /**
     * Returns the hash code for this table. The hash code of a table is defined
     * as the hash code of its cell tree, as returned by {@link #cellSet}.
     */
    public int hashCode() {
        return dataMap.hashCode();
    }

    // Mutators

    /**
     * erase
     */
    public void clear() {
        dataMap.clear();
    }

    /**
     * put the value for a cell
     *
	 */
    public void put(R rowKey, C columnKey, V value) {
        if (rowKey != null && columnKey != null) {
            Map<C, V> row = row(rowKey);
            if (row == null) {
                row = new HashMap<>();
                dataMap.put(rowKey, row);
            }
            V oldValue = row.get(columnKey);
            row.put(columnKey, value);
            if (oldValue != null) {
            }
        }
    }

    /**
     * put all values
     *
	 */
	public void putAll(Table<R, C, V> table) {
		for (R rowKey : table.rowKeySet()) {
			final Map<C, V> row = table.row(rowKey);
			for (C columnKey : row.keySet()) {
				put(rowKey, columnKey, row.get(columnKey));
			}
		}
	}

	public void putRow(R rowKey, Map<C, V> columnKey2ValueMap) {
		for (var entry : columnKey2ValueMap.entrySet()) {
			put(rowKey, entry.getKey(), entry.getValue());
		}
	}

	public void putColumn(C columnKey, Map<R, V> rowKey2ValueMap) {
		for (var entry : rowKey2ValueMap.entrySet()) {
			put(entry.getKey(), columnKey, entry.getValue());
		}
	}

    /**
     * removes the given cell, returns the old value or null
     *
	 */
    public void remove(R rowKey, C columnKey) {
        if (rowKey == null || columnKey == null)
            return;
        final Map<C, V> row = row(rowKey);
        if (row == null)
            return;
        final V oldValue = get(rowKey, columnKey);
        row.remove(columnKey);
    }

    // Views

    /**
     * get a row. Changes to this set affect the table and vice versa
     * @return row or null
     */
    public Map<C, V> row(R rowKey) {
        if (rowKey == null)
            return null;
        return dataMap.get(rowKey);
    }

    /**
     * gets a column. Changes to this map do not affect the table
     */
    public Map<R, V> column(final C columnKey) {
        final HashMap<R, V> map = new HashMap<>();
        for (R rowKey : dataMap.keySet()) {
            final Map<C, V> row = dataMap.get(rowKey);
            final V value = row.get(columnKey);
            if (value != null)
                map.put(rowKey, value);
        }
        return map;
    }

    /**
     * get current set of all cells. Changes to this set do not affect the table
	 */
    public Set<Triplet<R, C, V>> cellSet() {
        final Set<Triplet<R, C, V>> set = new HashSet<>();
        for (R rowKey : dataMap.keySet()) {
            final Map<C, V> row = dataMap.get(rowKey);
            for (C columnKey : row.keySet()) {
                set.add(new Triplet<>(rowKey, columnKey, row.get(columnKey)));
            }
        }
        return set;
    }

    /**
     * get the set of all row keys. Changes to this set affect the Table and vice versa
	 */
    public Set<R> rowKeySet() {
        return dataMap.keySet();
    }

    /**
     * gets current column keys. Changes to this set do not affect the Table
	 */
    public Set<C> columnKeySet() {
        final Set<C> set = new HashSet<>();
        for (Map<C, V> row : dataMap.values()) {
            set.addAll(row.keySet());
        }
		return set;
    }


    /**
     * remove a given column
     */
    public void removeColumn(C columnKey) {
        var changed = false;
        for (var rowKey : column(columnKey).keySet()) {
            remove(rowKey, columnKey);
            changed = true;
		}
    }

	public boolean removeRow(R rowKey) {
		var changed = row(rowKey).size() > 0;
		dataMap.remove(rowKey);
		return changed;
	}

	/**
	 * gets all current values. Changes to this collection do not affect the Table
	 *
	 * @return values
	 */
	public Collection<V> values() {
		final Collection<V> values = new LinkedList<>();
		for (Map<C, V> row : dataMap.values()) {
            values.addAll(row.values());
        }
        return values;
    }

    /**
     * compute table with tranposed rows and cols
     */
    public Table<C, R, V> computeTransposedTable() {
        final Table<C, R, V> transposed = new Table<>();
        for (R row : rowKeySet())
            for (C col : columnKeySet())
                transposed.put(col, row, get(row, col));
        return transposed;
    }

    /**
     * returns a copy
     *
     * @return copy
     */
    public Table<R, C, V> copy() {
        final Table<R, C, V> copy = new Table<>();
        for (R row : rowKeySet())
            for (C col : columnKeySet())
                copy.put(row, col, get(row, col));
        return copy;
    }

    public int getNumberOfRows() {
        return dataMap.size();
    }
}
