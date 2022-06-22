/*
 * NodeArray.java Copyright (C) 2022 Daniel H. Huson
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


import jloda.util.Basic;
import jloda.util.IteratorUtils;

import java.util.*;
import java.util.function.Function;

/**
 * Node array
 * Daniel Huson 2004, 2021
 */

public class NodeArray<T> extends GraphBase implements Iterable<T>, Map<Node, T>, Function<Node, T>, AutoCloseable {
	private T[] data;
	private int size = 0;

	/**
	 * Construct an node array with default value null
	 */
	public NodeArray(Graph g) {
		setOwner(g);
		data = (T[]) new Object[g.getMaxNodeId() + 1];
		g.registerNodeArray(this);
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        Arrays.fill(data, null);
        size = 0;
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeArray(NodeArray<T> src) {
        setOwner(src.getOwner());
        data = (T[]) new Object[src.data.length];
        System.arraycopy(src.data, 0, data, 0, data.length);
        size = src.size();
    }

    /**
     * Set the entry for node e to obj.
     *
     * @param a      Node
     * @param object Object
     */
    public T put(Node a, T object) {
        checkOwner(a);
        int id = a.getId();
        if (id >= data.length) {
            if (object == null)
                return null; // nothing to do
            grow(a.getId());
        }
        if (data[id] == null) {
            if (object != null) {
                data[id] = object;
                size++;
            }
        } else if (data[id] != null) {
            data[id] = object;
            if (object == null) {
                size--;
            }
        }
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
     * Set the entry for all nodes.
     *
     * @param value Object
     */
    public void putAll(T value) {
        clear();
        if (value != null && getOwner().getNumberOfNodes() > 0) {
            getOwner().nodes().forEach(e -> put(e, value));
        }
    }

    public Iterator<T> iterator() {
        if (isEmpty())
            return Collections.emptyIterator();
        else
            return IteratorUtils.iteratorNonNullElements(new Iterator<>() {
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
    public Iterable<Node> keys() {
        if (isEmpty())
            return Collections::emptyIterator;
        else
            return () -> new Iterator<>() {
                private Node a = getOwner().getFirstNode();

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
                public Node next() {
                    if (a == null)
                        throw new NoSuchElementException();
                    Node result = a;
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

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public T remove(Object key) {
        if (key instanceof Node) {
            var a = (Node) key;
            T value = get(a);
            put(a, null);
            return value;
        } else
            return null;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Node) {
            return get(key) != null;
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
        if (key instanceof Node) {
            var a = (Node) key;
            checkOwner(a);
            if (a.getId() < data.length && data[a.getId()] != null)
                return data[a.getId()];
            else
                return null;
        } else
            return null;
    }

    public T getOrDefault(Object key, T defaultValue) {
        if(containsKey(key))
            return get(key);
        else
            return defaultValue;
    }

    @Override
    public void putAll(Map<? extends Node, ? extends T> map) {
        for (var a : map.entrySet()) {
            put(a.getKey(), a.getValue());
        }
    }

    @Override
    public Set<Node> keySet() {
        return IteratorUtils.asSet(keys());
    }

    @Override
    public Set<Entry<Node, T>> entrySet() {
        var set = new HashSet<Map.Entry<Node, T>>();
        for (var k : keys()) {
            set.add(new Map.Entry<>() {
                @Override
                public Node getKey() {
                    return k;
                }

                @Override
                public T getValue() {
                    return get(k);
                }

                @Override
                public T setValue(T value) {
                    return put(k, value);
                }
            });
        }
        return set;
    }

	@Override
	public Collection<T> values() {
		return IteratorUtils.asList(this);
	}

	@Override
	public T apply(Node node) {
		return get(node);
	}

	@Override
	public void close() {
		getOwner().close(this);
	}
}


// EOF
