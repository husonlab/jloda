/*
 * APoint2D.java Copyright (C) 2020. Daniel H. Huson
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
 * two dimensional point with user data
 *
 * @param <T> Daniel Huson, 3.2019
 */
public class APoint2D<T> {
    final private double x;
    final private double y;
    private T userData;

    public APoint2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public APoint2D(double x, double y, T userData) {
        this.x = x;
        this.y = y;
        this.userData = userData;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public T getUserData() {
        return userData;
    }

    public void setUserData(T userData) {
        this.userData = userData;
    }
}
