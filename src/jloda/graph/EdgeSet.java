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

/**
 * @version $Id: EdgeSet.java,v 1.11 2007-02-06 17:48:53 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;

import jloda.util.IteratorAdapter;

import java.util.*;

/**
 * EdgeSet implements a set of edges contained in a given graph
 */
public class EdgeSet extends GraphBase implements Set<Edge> {
    final BitSet bits;

    /**
     * Constructs a new empty EdgeSet for Graph G.
     *
     * @param graph Graph
     */
    public EdgeSet(Graph graph) {
        setOwner(graph);
        graph.registerEdgeSet(this);
        bits = new BitSet();
    }

    /**
     * Is edge v member?
     *
     * @param e Edge
     * @return a boolean value
     */
    public boolean contains(Object e) {
        return bits.get(getOwner().getId((Edge) e));
    }

    /**
     * Insert edge e.
     *
     * @param e Edge
     * @return true, if new
     */
    public boolean add(Edge e) {
        if (contains(e))
            return false;
        else {
            bits.set(getOwner().getId(e), true);
            return true;
        }
    }

    /**
     * Delete edge v from set.
     *
     * @param e Edge
     */
    public boolean remove(Object e) {
        if (contains(e)) {
            bits.set(getOwner().getId((Edge) e), false);
            return true;
        } else
            return false;

    }

    /**
     * adds all edges in the given collection
     *
     * @param collection
     * @return true, if some element is new
     */
    public boolean addAll(Collection collection) {
        Iterator it = collection.iterator();

        boolean result = false;
        while (it.hasNext()) {
            if (add((Edge) it.next()))
                result = true;
        }
        return result;
    }

    /**
     * returns true if all elements of collection are contained in this set
     *
     * @param collection
     * @return all contained?
     */
    public boolean containsAll(Collection collection) {

        for (Object aCollection : collection) {
            if (!contains(aCollection))
                return false;
        }
        return true;
    }

    /**
     * removes all edges in the collection
     *
     * @param collection
     * @return true, if something actually removed
     */
    public boolean removeAll(Collection collection) {
        Iterator it = collection.iterator();

        boolean result = false;
        while (it.hasNext()) {
            if (remove(it.next()))
                result = true;
        }
        return result;
    }

    /**
     * keep only those elements contained in the collection
     *
     * @param collection
     * @return true, if set changes
     */
    public boolean retainAll(Collection collection) {
        boolean changed = (collection.size() != size() || !containsAll(collection));
        EdgeSet was = (EdgeSet) this.clone();

        clear();
        for (Object e : collection) {
            if (e instanceof Edge) {
                if (was.contains(e))
                    add((Edge) e);
            }
        }
        return changed;
    }

    /**
     * Delete all edges from set.
     */
    public void clear() {
        bits.clear();
    }

    /**
     * is empty?
     *
     * @return true, if empty
     */
    public boolean isEmpty() {
        return bits.isEmpty();
    }

    /**
     * return all contained edges as edges
     *
     * @return contained edges
     */
    public Edge[] toArray() {
        Edge[] result = new Edge[bits.cardinality()];
        int i = 0;
        Iterator<Edge> it = getOwner().edgeIterator();
        while (it.hasNext()) {
            Edge e = it.next();
            if (contains(e))
                result[i++] = e;
        }
        return result;
    }


    public <T> T[] toArray(T[] ts) {
        int i = 0;
        Iterator<Edge> it = getOwner().edgeIterator();
        while (it.hasNext()) {
            Edge e = it.next();
            if (contains(e))
                ts[i++] = (T) e;
        }
        return ts;
    }

    /**
     * Puts all edges into set.
     */
    public void addAll() {
        Iterator<Edge> it = getOwner().edgeIterator();
        while (it.hasNext())
            add(it.next());
    }

    /**
     * Returns the size of the set.
     *
     * @return size
     */
    public int size() {
        return bits.cardinality();
    }

    /**
     * Returns an enumeration of the elements in the set.
     *
     * @return an enumeration of the elements in the set
     */
    public Iterator<Edge> iterator() {
        return new IteratorAdapter<Edge>() {
            private Edge e = getFirstElement();

            protected Edge findNext() throws NoSuchElementException {
                if (e != null) {
                    final Edge result = e;
                    e = getNextElement(e);
                    return result;
                } else {
                    throw new NoSuchElementException("at end");
                }
            }
        };
    }

    /**
     * returns the edges in the given array, if they fit, or in a new array, otherwise
     *
     * @param edges
     * @return edges in this set
     */
    public Edge[] toArray(Edge[] edges) {
        return toArray();
        /*
        if (edges == null)
            throw new NullPointerException();
        if (bits.cardinality() > edges.length)
            edges = (Edge[]) Array.newInstance((edges[0]).getClass(), bits.cardinality());

        int i = 0;
        Iterator it = getOwner().edgeIterator();
        while (it.hasNext()) {
            Edge v = it.next();
            if (contains(v) == true)
                edges[i++] = it.next();
        }
        return edges;
        */
    }

    /**
     * Returns the first element in the set.
     *
     * @return v Edge
     */
    public Edge getFirstElement() {
        Edge e;
        for (e = getOwner().getFirstEdge(); e != null; e = getOwner().getNextEdge(e))
            if (contains(e))
                break;
        return e;
    }

    /**
     * Gets the successor element in the set.
     *
     * @param v Edge
     * @return a Edge the successor of edge v
     */
    public Edge getNextElement(Edge v) {
        for (v = getOwner().getNextEdge(v); v != null; v = getOwner().getNextEdge(v))
            if (contains(v))
                break;
        return v;
    }

    /**
     * Gets the predecessor element in the set.
     *
     * @param v Edge
     * @return a Edge the predecessor of edge v
     */
    public Edge getPrevElement(Edge v) {
        for (v = getOwner().getPrevEdge(v); v != null; v = getOwner().getPrevEdge(v))
            if (contains(v))
                break;
        return v;
    }


    /**
     * Returns the last element in the set.
     *
     * @return the Edge the last element in the set
     */
    public Edge getLastElement() {
        Edge v = null;
        for (v = getOwner().getLastEdge(); v != null; v = getOwner().getPrevEdge(v))
            if (contains(v))
                break;
        return v;
    }

    /**
     * returns a clone of this set
     *
     * @return a clone
     */
    public Object clone() {
        EdgeSet result = new EdgeSet(getOwner());
        for (Edge edge : this) result.add(edge);
        return result;
    }

    /**
     * do the two sets have a non-empty intersection?
     *
     * @param aset
     * @return true, if intersection is non-empty
     */
    public boolean intersects(EdgeSet aset) {
        return bits.intersects(aset.bits);
    }
}

// EOF
