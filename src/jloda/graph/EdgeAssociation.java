/**
 * EdgeAssociation.java 
 * Copyright (C) 2016 Daniel H. Huson
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
    T get(Edge e);

    /**
     * Set the entry for edge e to obj.
     *
     * @param e   Edge
     * @param obj Object
     */
    void set(Edge e, T obj);

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
     * get the entry as an int
     *
     * @param e
     * @return int value
     */
    int getInt(Edge e);

    /**
     * get the entry as a double
     *
     * @param e
     * @return double value
     */
    double getDouble(Edge e);

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
