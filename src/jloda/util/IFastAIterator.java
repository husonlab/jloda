/*
 * IFastAIterator.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.util;

/**
 * header for fastA and fastQAsFastA iterators
 * Daniel Huson, 3.2014
 */
public interface IFastAIterator extends ICloseableIterator<Pair<String, String>> {
    /**
     * get position in file of current record
     *
     * @return position in file
     */
    long getPosition();

    /**
     * get number of bytes occupied by this sequence in file
     *
     * @return
     */
    long getNumberOfBytes();
}
