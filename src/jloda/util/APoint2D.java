/*
 * APoint2D.java Copyright (C) 2022 Daniel H. Huson
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
 * two dimensional point with user data
 *
 * @param <T> Daniel Huson, 3.2019
 */
public class APoint2D<T> {
    private double x;
    private double y;
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

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    public T getUserData() {
        return userData;
    }

    public void setUserData(T userData) {
        this.userData = userData;
    }

    public double[] getValues() {
        return new double[]{x, y};
    }

    public String toString() {
        return String.format("(%.3f,%.3f)%s", getX(), getY(), getUserData() == null ? "" : ": " + getUserData());
    }

    public double distance(APoint2D<?> other) {
        return Math.sqrt((x-other.x)*(x-other.x)+(y-other.y)*(y-other.y));
    }
}
