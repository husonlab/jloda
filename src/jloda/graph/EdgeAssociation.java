/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
