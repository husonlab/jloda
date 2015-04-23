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
 * A wrapper class for an array mapping numbers to nodes
 * Daniel Huson, 1.2007
 */
public class Num2NodeArray {
    Node[] array = new Node[0];

    /**
     * default constructor
     */
    public Num2NodeArray() {
        array = new Node[0];
    }

    /**
     * default constructor
     *
     * @param n number of nodes (nodes are  numbered 1..n+1)
     */
    public Num2NodeArray(int n) {
        array = new Node[n + 1];
    }

    /**
     * wrapper constructor
     *
     * @param array
     */
    public Num2NodeArray(Node[] array) {
        this.array = array;
    }

    /**
     * sets the wrapped array
     *
     * @param array
     */
    public void set(Node[] array) {
        this.array = array;
    }

    /**
     * sets the i-th entry. Assumes the wrapped array has already been constructed or set
     *
     * @param i
     * @param v
     */
    public void put(int i, Node v) {
        array[i] = v;
    }

    /**
     * gets the -th entry
     *
     * @param i
     * @return node at position i of array
     */
    public Node get(int i) {
        return array[i];
    }

    /**
     * gets the length of the array
     *
     * @return length
     */
    public int length() {
        return array.length;
    }

    /**
     * gets the wrapped array
     *
     * @return node array
     */
    public Node[] get() {
        return array;
    }

    /**
     * erase and resize  to hold (0,1,...,n)
     *
     * @param n
     */
    public void clear(int n) {
        array = new Node[n + 1];
    }
}

