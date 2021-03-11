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
    final private double[] xy;
    private T userData;

    public APoint2D(double x, double y) {
        this.xy = new double[]{x, y};
    }

    public APoint2D(double x, double y, T userData) {
        this.xy = new double[]{x, y};
        this.userData = userData;
    }

    public double getX() {
        return xy[0];
    }

    public double getY() {
        return xy[1];
    }

    public T getUserData() {
        return userData;
    }

    public void setUserData(T userData) {
        this.userData = userData;
    }

    public double[] getValues() {
        return xy;
    }

    public String toString() {
        return String.format("(%.3f,%.3f)%s", getX(), getY(), getUserData() == null ? "" : ": " + getUserData());
    }
}
