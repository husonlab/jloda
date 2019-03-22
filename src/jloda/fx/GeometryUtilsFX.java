/*
 *  Copyright (C) 2018 Daniel H. Huson
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

/*
 *  Copyright (C) 2019 Daniel H. Huson
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

/*
 *  Copyright (C) 2019 Daniel H. Huson
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

/*
 *  Copyright (C) 2019 Daniel H. Huson
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

/*
 *  Copyright (C) 2019 Daniel H. Huson
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

/*
 *  Copyright (C) 2019 Daniel H. Huson
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

/*
 *  Copyright (C) 2019 Daniel H. Huson
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
package jloda.fx;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

public class GeometryUtilsFX {
    private final static double RAD_TO_DEG_FACTOR = 180.0 / Math.PI;
    private final static double DEG_TO_RAD_FACTOR = Math.PI / 180.0;

    /**
     * Computes the angle of a two-dimensional vector.
     *
     * @param p Point2D
     * @return angle in degrees
     */
    public static double computeAngle(Point2D p) {
        if (p.getX() != 0) {
            double x = Math.abs(p.getX());
            double y = Math.abs(p.getY());
            double a = Math.atan(y / x);

            if (p.getX() > 0) {
                if (p.getY() > 0)
                    return rad2deg(a);
                else
                    return rad2deg(2.0 * Math.PI - a);
            } else // p.getX()<0
            {
                if (p.getY() > 0)
                    return rad2deg(Math.PI - a);
                else
                    return rad2deg(Math.PI + a);
            }
        } else if (p.getY() > 0)
            return rad2deg(0.5 * Math.PI);
        else // p.y<0
            return rad2deg(-0.5 * Math.PI);
    }

    /**
     * computes the angle difference between a and b as viewed from center
     *
     * @param center
     * @param a
     * @param b
     * @return angle
     */
    public static double computeObservedAngle(Point2D center, Point2D a, Point2D b) {
        final Point2D da = a.subtract(center);
        final Point2D db = b.subtract(center);
        double angle = Math.abs(computeAngle(da) - computeAngle(db));
        if (angle > 180)
            angle = 360 - angle;

        final double det = da.getX() * db.getY() - da.getY() * db.getX();

        if (det >= 0)
            return angle;
        else
            return -angle;
    }

    /**
     * computes the angle difference between a and b as viewed from center
     *
     * @param center
     * @param a
     * @param b
     * @return angle
     */
    public static double basicComputeAngle(Point2D center, Point2D a, Point2D b) {
        Point2D da = a.subtract(center);
        Point2D db = b.subtract(center);
        return modulo360(computeAngle(db) - computeAngle(da));
    }

    /**
     * returns the signed difference of angles A and B
     *
     * @param AngleA
     * @param AngleB
     */
    public static double signedDiffAngle(double AngleA, double AngleB) {
        if (modulo360(AngleA - AngleB) > 180) {
            return -(360 - modulo360(AngleA - AngleB));
        } else {
            return modulo360(AngleA - AngleB);
        }
    }


    public static double rad2deg(double angle) {
        angle *= RAD_TO_DEG_FACTOR;
        while (angle > 360)
            angle -= 360;
        while (angle < 0)
            angle += 360;
        return angle;
    }

    public static double deg2rad(double deg) {
        return DEG_TO_RAD_FACTOR * deg;
    }

    /**
     * Rotates a point by angle alpha around a second point
     *
     * @param src    the point to be rotated
     * @param alpha  the angle
     * @param anchor the anchor point
     * @return the rotated point
     */
    public static Point2D rotateAbout(Point2D src, double alpha, Point2D anchor) {
        Point2D tar = new Point2D(src.getX() - anchor.getX(), src.getY() - anchor.getY());
        tar = rotate(tar, alpha);
        tar = new Point2D(tar.getX() + anchor.getX(), tar.getY() + anchor.getY());
        return tar;
    }

    /**
     * Translate a point in the direction specified by an angle.
     *
     * @param apt   Point2D
     * @param alpha double in degrees
     * @param dist  double
     * @return Point2D
     */
    public static Point2D translateByAngle(Point2D apt, double alpha, double dist) {
        double dx = dist * Math.cos(DEG_TO_RAD_FACTOR * alpha);
        double dy = dist * Math.sin(DEG_TO_RAD_FACTOR * alpha);
        if (Math.abs(dx) < 0.000001)
            dx = 0;
        if (Math.abs(dy) < 0.000001)
            dy = 0;
        return new Point2D(apt.getX() + dx, apt.getY() + dy);
    }

    /**
     * Rotates a two-dimensional vector by the angle alpha.
     *
     * @param p     point
     * @param alpha angle in degree
     * @return q point rotated around origin
     */
    public static Point2D rotate(Point2D p, double alpha) {
        return rotate(p.getX(), p.getY(), alpha);
    }


    /**
     * Rotates a two-dimensional vector by the angle alpha.
     *
     * @param x
     * @param y
     * @param alpha angle in degree
     * @return q point rotated around origin
     */
    public static Point2D rotate(double x, double y, double alpha) {
        double sina = Math.sin(DEG_TO_RAD_FACTOR * alpha);
        double cosa = Math.cos(DEG_TO_RAD_FACTOR * alpha);
        return new Point2D(x * cosa - y * sina, x * sina + y * cosa);
    }

    /**
     * put angle into range 0-360
     *
     * @param degrees
     * @return shifted angle
     */
    public static double modulo360(double degrees) {
        while (degrees > 360)
            degrees -= 360;
        while (degrees < 0)
            degrees += 360;
        return degrees;
    }

    /**
     * returns the average of angles A and B
     *
     * @param AngleA in deg
     * @param AngleB in deg
     */
    public static double midAngle(double AngleA, double AngleB) {
        if (modulo360(AngleA - AngleB) < 180) {
            return modulo360(AngleB + (modulo360(AngleA - AngleB)) / 2);
        } else {
            return modulo360(AngleB - (modulo360(AngleB - AngleA)) / 2);
        }
    }

    static final double PI2 = 2 * Math.PI;

    /**
     * clamp to range 0..2PI
     *
     * @param x
     * @return modulo 2PI
     */
    static public double moduloTwoPI(double x) {
        while (x < 0)
            x += PI2;
        while (x > PI2)
            x -= PI2;
        return x;
    }

    /**
     * returns the difference of angles A and B
     *
     * @param A
     * @param B
     */
    public static double squaredDistance(Point2D A, Point2D B) {
        return (B.getX() - A.getX()) * (B.getX() - A.getX()) + (B.getY() - A.getY()) * (B.getY() - A.getY());
    }

    public static Point3D from2Dto3D(Point2D point) {
        return new Point3D(point.getX(), point.getY(), 0);
    }

    public static Point2D from3Dto2D(Point3D point) {
        return new Point2D(point.getX(), point.getY());
    }
}
