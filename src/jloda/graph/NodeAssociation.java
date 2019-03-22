/**
 * NodeAssociation.java
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
    T getValue(Node v);

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param obj Object
     */
    void setValue(Node v, T obj);

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param obj Object
     */
    void put(Node v, T obj);

    /**
     * Set the entry for all nodes to obj.
     *
     * @param obj Object
     */
    void setAll(T obj);

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
