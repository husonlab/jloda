/**
 * ListOfLongs.java 
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
