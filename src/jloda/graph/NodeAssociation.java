/**
 * NodeAssociation.java 
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
 * Node assocation
 * Daniel Huson, 2006
 */
public interface NodeAssociation<T> {
    /**
     * Clear all entries.
     */
    void clear();

    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return an Object the entry for node v
     */
    T get(Node v);

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param obj Object
     */
    void set(Node v, T obj);

    /**
     * Set the entry for all nodes to obj.
     *
     * @param obj Object
     */
    void setAll(T obj);

    /**
     * get the entry as an int
     *
     * @param v
     * @return int value
     */
    int getInt(Node v);

    /**
     * get the entry as a double
     *
     * @param v
     * @return double value
     */
    double getDouble(Node v);

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
