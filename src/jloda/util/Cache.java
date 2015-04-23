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
