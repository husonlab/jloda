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

/**
 * object for counting. All methods are thread-safe (except addUnsynchronized)
 * Daniel Huson, 10.2011
 */
public class Counter {
    private long value;

    /**
     * constructor
     */
    public Counter() {
        this.value = 0;
    }

    /**
     * constructor
     *
     * @param value
     */
    public Counter(long value) {
        this.value = value;
    }

    /**
     * getter
     */
    public long get() {
        synchronized (this) {
            return value;
        }
    }

    /**
     * settter
     *
     * @param value
     */
    public void set(long value) {
        synchronized (this) {
            this.value = value;
        }
    }

    /**
     * increment
     */
    public void increment() {
        synchronized (this) {
            value++;
        }
    }

    /**
     * increment
     */
    public void add(long add) {
        synchronized (this) {
            value += add;
        }
    }

    /**
     * increment by value, unsynchronized
     *
     * @param add
     */
    public void addUnsynchronized(long add) {
        value += add;
    }

    /**
     * decrement
     */
    public void decrement() {
        synchronized (this) {
            value--;
        }
    }

    public String toString() {
        return "" + get();
    }
}
