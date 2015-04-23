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
