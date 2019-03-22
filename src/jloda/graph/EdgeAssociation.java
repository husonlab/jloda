/**
 * EdgeAssociation.java
 * Copyright (C) 2019 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.graph;

/**
 * Edge association
 * daniel huson 2005
 */
public interface EdgeAssociation<T> {
    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return an object the entry for edge e
     */
    T getValue(Edge e);

    /**
     * Set the entry for edge e to obj.
     *
     * @param e   Edge
     * @param obj Object
     */
    void put(Edge e, T obj);

    /**
     * Set the entry for edge e to obj.
     *
     * @param e   Edge
     * @param obj Object
     */
    void setValue(Edge e, T obj);

    /**
     * Set the entry for all edges.
     *
     * @param obj Object
     */
    void setAll(T obj);

    /**
     * Clear all entries.
     */
    void clear();

    /**
     * returns a reference to the graph that owns this association
     *
     * @return owner
     */
    Graph getOwner();


    /**
     * is clean, that is, has never been set since last erase
     *
     * @return true, if erase
     */
    boolean isClear();
}
