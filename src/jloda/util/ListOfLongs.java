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
 * a list of longs
 * Created by huson on 5/16/14.
 */
public class ListOfLongs {
    private long[] data;
    private int size = 0;

    public ListOfLongs() {
        this(1024);
    }

    public ListOfLongs(int initialSize) {
        data = new long[initialSize];
    }

    public void clear() {
        size = 0;
    }

    public void add(long value) {
        if (size == data.length) {
            long[] tmp = new long[(int) Math.min(Integer.MAX_VALUE, 2l * data.length)];
            System.arraycopy(data, 0, tmp, 0, data.length);
            data = tmp;
        }
        data[size++] = value;
    }

    public int size() {
        return size;
    }

    public long get(int i) {
        return data[i];
    }

    public void addAll(ListOfLongs listOfLongs) {
        long newSize = size + listOfLongs.size;
        if (newSize >= data.length) {
            long[] tmp = new long[(int) Math.min(Integer.MAX_VALUE, newSize)];
            System.arraycopy(data, 0, tmp, 0, data.length);
            data = tmp;
        }
        System.arraycopy(listOfLongs.data, 0, data, size, listOfLongs.size);
        size += listOfLongs.size;
    }
}
