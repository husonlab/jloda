/*
 * JointIterators.java Copyright (C) 2019. Daniel H. Huson
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * iterator over multiple iterators
 * Created by huson on 2/7/17.
 */
public class JointIterators<T> implements Iterator<T> {
    private final ArrayList<Iterator<T>> iterators;
    private Iterator<T> current;

    public JointIterators(Iterator<T>... iterators) {
        this.iterators = new ArrayList<>(Arrays.asList(iterators));
        if (this.iterators.size() > 0)
            current = this.iterators.remove(0);

    }

    @Override
    public boolean hasNext() {
        while (current != null) {
            if (current.hasNext())
                return true;
            else if (iterators.size() > 0)
                current = iterators.remove(0);
            else {
                current = null;
            }
        }
        return false;
    }

    @Override
    public T next() {
        return current.next();
    }

    @Override
    public void remove() {
    }
}
