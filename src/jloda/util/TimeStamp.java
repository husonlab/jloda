/**
 * TimeStamp.java 
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
/** A unique time stamp at every call
 * @version $Id: TimeStamp.java,v 1.2 2006-06-06 18:56:04 huson Exp $
 * @author Daniel Huson
 * 5.03
 */

package jloda.util;

public class TimeStamp {
    private static long prevTimeStamp = 0;

    /**
     * Returns the next tick of the timestamp clock
     *
     * @return the next tick of the timestamp clock
     */
    public static long get() {
        return ++prevTimeStamp;
    }
}
