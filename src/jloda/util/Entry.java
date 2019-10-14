/*
 * Entry.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

package jloda.util;

import java.util.Map;
import java.util.Objects;

/**
 * an entry
 *
 * @param <K>
 * @param <V>
 */
public class Entry<K, V> {
    private final K key;
    private V value;

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        return new Entry<>(key, value);
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        if (value == null)
            throw new NullPointerException();

        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

        return (key == null ? e.getKey() == null : key.equals(e.getKey())) &&
                (value == null ? e.getValue() == null : value.equals(e.getValue()));
    }

    public int hashCode() {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    public String toString() {
        return key.toString() + "=" + value.toString();
    }
}
