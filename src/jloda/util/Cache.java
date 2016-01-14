/**
 * Cache.java 
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
package jloda.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * simple RLU cache
 * Source: http://stackoverflow.com/questions/224868/easy-simple-to-use-lru-cache-in-java
 *
 * @param <K> key
 * @param <V> value
 */
public class Cache<K, V> {
    final Map<K, V> MRUdata;
    final Map<K, V> LRUdata;

    public Cache(final int capacity) {
        LRUdata = new WeakHashMap<>();

        MRUdata = new LinkedHashMap<K, V>(capacity + 1, 1.0f, true) {
            protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
                if (entry != null && this.size() > capacity) {
                    LRUdata.put(entry.getKey(), entry.getValue());
                    return true;
                }
                return false;
            }
        };
    }

    public V tryGet(K key) {
        V value = MRUdata.get(key);
        if (value != null)
            return value;
        value = LRUdata.get(key);
        if (value != null) {
            LRUdata.remove(key);
            MRUdata.put(key, value);
        }
        return value;
    }

    public void set(K key, V value) {
        LRUdata.remove(key);
        MRUdata.put(key, value);
    }
}
