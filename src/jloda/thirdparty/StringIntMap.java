/*
 *  Copyright 2015 the original author or authors.
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  The initial idea for this class is from "org.apache.commons.lang.IntHashMap";
 *  http://commons.apache.org/commons-lang-2.6-src.zip
 *
 */

package jloda.thirdparty;

import jloda.util.Entry;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Paul Kim (sjkim@whatap.io)
 */
public class StringIntMap {
    private static final int DEFAULT_CAPACITY = 1001;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private StringIntEntry[] table;
    private int count;
    private int threshold;
    private final float loadFactor;

    public StringIntMap(int initCapacity, float loadFactor) {
        if (initCapacity < 0)
            throw new RuntimeException("Capacity Error: " + initCapacity);
        if (loadFactor <= 0)
            throw new RuntimeException("Load Count Error: " + loadFactor);
        if (initCapacity == 0)
            initCapacity = 1;
        this.loadFactor = loadFactor;
        table = new StringIntEntry[initCapacity];
        threshold = (int) (initCapacity * loadFactor);
    }

    private int NONE = 0;

    public StringIntMap setNullValue(int none) {
        this.NONE = none;
        return this;
    }

    public StringIntMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public int size() {
        return count;
    }

    public synchronized boolean containsValue(int value) {
        final StringIntEntry[] tab = table;
        int i = tab.length;
        while (i-- > 0) {
            for (StringIntEntry e = tab[i]; e != null; e = e.next) {
                if (e.value == value) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean containsKey(String key) {
        if (key == null)
            return false;
        final StringIntEntry[] tab = table;
        int index = (key.hashCode() & Integer.MAX_VALUE) % tab.length;
        for (StringIntEntry e = tab[index]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public synchronized int get(String key) {
        if (key == null)
            return NONE;
        final StringIntEntry[] tab = table;
        int index = (key.hashCode() & Integer.MAX_VALUE) % tab.length;
        for (StringIntEntry e = tab[index]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                return e.value;
            }
        }
        return NONE;
    }

    protected void rehash() {
        int oldCapacity = table.length;
        StringIntEntry[] oldMap = table;
        int newCapacity = oldCapacity * 2 + 1;
        StringIntEntry[] newMap = new StringIntEntry[newCapacity];
        threshold = (int) (newCapacity * loadFactor);
        table = newMap;
        for (int i = oldCapacity; i-- > 0; ) {
            StringIntEntry old = oldMap[i];
            while (old != null) {
                StringIntEntry e = old;
                old = old.next;
                int index = (e.key.hashCode() & Integer.MAX_VALUE) % newCapacity;
                e.next = newMap[index];
                newMap[index] = e;
            }
        }
    }

    public synchronized int put(String key, int value) {
        if (key == null)
            return NONE;
        StringIntEntry[] tab = table;
        int index = (key.hashCode() & Integer.MAX_VALUE) % tab.length;
        for (StringIntEntry e = tab[index]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                int old = e.value;
                e.value = value;
                return old;
            }
        }
        if (count >= threshold) {
            rehash();
            tab = table;
            index = (key.hashCode() & Integer.MAX_VALUE) % tab.length;
        }
        StringIntEntry e = new StringIntEntry(key, value, tab[index]);
        tab[index] = e;
        count++;
        return NONE;
    }

    public synchronized int add(String key, int value) {
        if (key == null)
            return NONE;
        StringIntEntry[] tab = table;
        int index = (key.hashCode() & Integer.MAX_VALUE) % tab.length;
        for (StringIntEntry e = tab[index]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                int old = e.value;
                e.value += value;
                return old;
            }
        }
        if (count >= threshold) {
            rehash();
            tab = table;
            index = (key.hashCode() & Integer.MAX_VALUE) % tab.length;
        }
        StringIntEntry e = new StringIntEntry(key, value, tab[index]);
        tab[index] = e;
        count++;
        return NONE;
    }

    public synchronized int remove(String key) {
        if (key == null)
            return NONE;
        StringIntEntry[] tab = table;
        int index = (key.hashCode() & Integer.MAX_VALUE) % tab.length;
        for (StringIntEntry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
            if (e.key.equals(key)) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                count--;
                int oldValue = e.value;
                e.value = NONE;
                return oldValue;
            }
        }
        return NONE;
    }

    public synchronized void clear() {
        StringIntEntry[] tab = table;
        for (int index = tab.length; --index >= 0; )
            tab[index] = null;
        count = 0;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        var first = true;
        for (var e : entries()) {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(e.getKey()).append("=").append(e.getValue());
        }
        buf.append("}");
        return buf.toString();
    }

    public String toFormatString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        for (var e : entries()) {
            buf.append("\t").append(e.getKey()).append("=").append(e.getValue()).append("\n");
        }
        buf.append("}");
        return buf.toString();
    }

    public static class StringIntEntry {
        final String key;
		int value;
        StringIntEntry next;

        protected StringIntEntry(String key, int value, StringIntEntry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        protected Object clone() {
            return new StringIntEntry(key, value, (next == null ? null : (StringIntEntry) next.clone()));
        }

        public String getKey() {
            return key;
        }

        public int getValue() {
            return value;
        }

        public int setValue(int value) {
            int oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof StringIntEntry e))
                return false;
            return
                    e.key.equals(key) && e.value == value;
        }

        public int hashCode() {
            return key.hashCode() ^ value;
        }

        public String toString() {
            return key + "=" + value;
        }
    }

    public Iterable<String> keys() {
        return () -> new Iterator<>() {
            final StringIntEntry[] tab = table;
            int index = tab.length;
            StringIntEntry entry = null;
            StringIntEntry lastReturned = null;

            @Override
            public boolean hasNext() {
                while (entry == null && index > 0)
                    entry = tab[--index];
                return entry != null;
            }

            @Override
            public String next() {
                while (entry == null && index > 0)
                    entry = tab[--index];
                if (entry != null) {
                    StringIntEntry e = lastReturned = entry;
                    entry = e.next;
                    return e.key;
                } else
                    throw new NoSuchElementException();
            }
        };
    }

    public Iterable<Integer> values() {
        return () -> new Iterator<>() {
            final StringIntEntry[] tab = table;

            int index = tab.length;
            StringIntEntry entry = null;
            StringIntEntry lastReturned = null;

            @Override
            public boolean hasNext() {
                while (entry == null && index > 0)
                    entry = tab[--index];
                return entry != null;
            }

            @Override
            public Integer next() {
                while (entry == null && index > 0)
                    entry = tab[--index];
                if (entry != null) {
                    StringIntEntry e = lastReturned = entry;
                    entry = e.next;
                    return e.value;
                } else
                    throw new NoSuchElementException();
            }
        };
    }

    public Iterable<Entry<String, Integer>> entries() {
        return () -> new Iterator<>() {
            final StringIntEntry[] tab = table;
            int index = tab.length;
            StringIntEntry entry = null;
            StringIntEntry lastReturned = null;

            @Override
            public boolean hasNext() {
                while (entry == null && index > 0)
                    entry = tab[--index];
                return entry != null;
            }

            @Override
            public Entry<String, Integer> next() {
                while (entry == null && index > 0)
                    entry = tab[--index];
                if (entry != null) {
                    StringIntEntry e = lastReturned = entry;
                    entry = e.next;
                    return new Entry<>(entry.getKey(), entry.getValue());
                } else
                    throw new NoSuchElementException();
            }
        };
    }

    public static void main(String[] args) {
    }
}