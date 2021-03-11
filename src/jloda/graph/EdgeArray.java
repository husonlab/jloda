/*
 * EdgeArray.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jloda.graph;


import jloda.util.Basic;
import jloda.util.IteratorUtils;

import java.util.*;

/**
 * Edge array
 * Daniel Huson 2004, 2021
 */

public class EdgeArray<T> extends GraphBase implements Iterable<T>, Map<Edge, T> {
    private T[] data;
    private boolean isClear = true;
    private T defaultValue;

    /**
     * Construct an edge array with default value null
     */
    public EdgeArray(Graph g) {
        setOwner(g);
        data = (T[]) new Object[g.getMaxEdgeId() + 1];
        g.registerEdgeArray(this);
    }

    /**
     * Construct an edge array for the given graph and set the default value
     */
    public EdgeArray(Graph g, T defaultValue) {
        this(g);
        this.defaultValue = defaultValue;
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        Arrays.fill(data, null);
        isClear = true;
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeArray(EdgeArray<T> src) {
        setOwner(src.getOwner());
        getOwner().edges().forEach(e -> put(e, src.get(e)));
        defaultValue = src.getDefaultValue();
    }


    /**
     * Set the entry for edge e to obj.
     *
     * @param a      Edge
     * @param object Object
     */
    public T put(Edge a, T object) {
        checkOwner(a);
        int id = a.getId();
        if (id >= data.length) {
            if (object == null)
                return null; // nothing to do
            grow(a.getId());
        }
        data[id] = object;
        if (object != null && isClear)
            isClear = false;
        return object;
    }

    /**
     * grows the array. Repeatedly doubles the size of the array until it contains index n
     *
     * @param n index to be included in array
     */
    private void grow(int n) {
        int newSize = Math.max(1, 2 * data.length);
        while (newSize <= n && 2L * newSize < (long) Basic.MAX_ARRAY_SIZE) {
            newSize *= 2;
        }
        if (newSize > data.length) {
            T[] newData = (T[]) new Object[newSize];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    /**
     * has not been set since last clear()?
     */
    public boolean isClear() {
        return isClear;
    }

    public Iterator<T> iterator() {
        if (isClear())
            return Collections.emptyIterator();
        else
            return IteratorUtils.iteratorNonNullElements(new Iterator<T>() {
                int i = 0;

                @Override
                public boolean hasNext() {
                    return i < data.length;
                }

                @Override
                public T next() {
                    return data[i++];
                }
            });
    }


    /**
     * get all keys with non-null values
     */
    public Iterable<Edge> keys() {
        if (isClear())
            return Collections::emptyIterator;
        else
            return () -> new Iterator<>() {
                private Edge a = getOwner().getFirstEdge();

                {
                    while (a != null) {
                        if (a.getId() < data.length && data[a.getId()] != null)
                            break;
                        a = a.getNext();
                    }
                }

                @Override
            public boolean hasNext() {
                    return a != null;
            }

                @Override
                public Edge next() {
                    if (a == null)
                        throw new NoSuchElementException();
                    Edge result = a;
                    a = a.getNext();
                    while (a != null) {
                        if (a.getId() < data.length && data[a.getId()] != null)
                            break;
                        a = a.getNext();
                    }
                    return result;
                }
            };
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public T remove(Object key) {
        if (key instanceof Edge) {
            var a = (Edge) key;
            T value = get(a);
            put(a, null);
            return value;
        } else
            return null;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Edge) {
            return get((Edge) key) != null;
        } else
            return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value != null) {
            for (var a : this) {
                if (a.equals(value))
                    return true;
            }
        }
        return false;
    }

    @Override
    public T get(Object key) {
        if (key instanceof Edge) {
            var a = (Edge) key;
            if (a.getId() < data.length && data[a.getId()] != null)
                return data[a.getId()];
            else
                return defaultValue;

        } else
            return null;
    }

    @Override
    public void putAll(Map<? extends Edge, ? extends T> map) {
        for (var e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public Set<Edge> keySet() {
        return IteratorUtils.asSet(keys());
    }

    @Override
    public Set<Entry<Edge, T>> entrySet() {
        Set<Map.Entry<Edge, T>> set = new HashSet<>();
        for (var k : keys()) {
            set.add(new jloda.util.Entry<>(k, get(k)));
        }
        return set;
    }

    @Override
    public Collection<T> values() {
        return IteratorUtils.asList(this);
    }
 }


// EOF
