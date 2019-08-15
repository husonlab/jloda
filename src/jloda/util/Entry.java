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

/**
 * an entry
 *
 * @param <S>
 * @param <T>
 */
public class Entry<S, T> {
    private final S key;
    private final T value;

    public Entry(S key, T value) {
        this.key = key;
        this.value = value;
    }

    public S getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
