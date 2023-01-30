/*
 * ConvexHull.java Copyright (C) 2023 Daniel H. Huson
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

import java.util.ArrayList;

/**
 * computes convex hull for a collection of two dimensional points
 * <p>
 * daniel huson, 4.2015
 */
public class ConvexHull {
    private static ConvexHull instance;

    public static ConvexHull getInstance() {
        if (instance == null)
            instance = new ConvexHull();
        return instance;
    }

    private ConvexHull() {
    }

    /**
     * computes the convex hull of a set of two-dimensional points using the quick hull algorithm
     *
     * @return convex hull
     */
    public ArrayList<APoint2D> quickHull(final ArrayList<APoint2D> points0) {
        final ArrayList<APoint2D> points = new ArrayList<>(points0);

        final ArrayList<APoint2D> convexHull = new ArrayList<>();
        if (points.size() <= 3) {
            ArrayList<APoint2D> result = new ArrayList<>(points.size());
            result.addAll(points);
            return result;
        }
        int minPointIndex = -1;
        int maxPointIndex = -1;
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < points.size(); i++) {
            final APoint2D apt = points.get(i);
            if (apt.getX() < minX) {
                minX = apt.getX();
                minPointIndex = i;
            }
            if (apt.getX() > maxX) {
                maxX = apt.getX();
                maxPointIndex = i;
            }
        }
        final APoint2D a = points.get(minPointIndex);
        final APoint2D b = points.get(maxPointIndex);
        convexHull.add(a);
        convexHull.add(b);
        points.remove(a);
        points.remove(b);

        final ArrayList<APoint2D> leftSet = new ArrayList<>();
        final ArrayList<APoint2D> rightSet = new ArrayList<>();

        for (APoint2D p : points) {
            if (!isLeftOf(a, b, p))
                leftSet.add(p);
            else
                rightSet.add(p);
        }
        hullSet(a, b, rightSet, convexHull);
        hullSet(b, a, leftSet, convexHull);

        return convexHull;
    }

    /**
     * compute the hull set
     *
	 */
    private void hullSet(final APoint2D a, final APoint2D b, final ArrayList<APoint2D> set, final ArrayList<APoint2D> hull) {
        if (set.size() == 0) return;

        if (set.size() == 1) {
            APoint2D p = set.get(0);
            set.remove(p);
            final int insertPosition = hull.indexOf(b);
            hull.add(insertPosition, p);
            return;
        }

        double maxDistance = Double.NEGATIVE_INFINITY;
        int maxDistancePointIndex = -1;
        for (int i = 0; i < set.size(); i++) {
            final APoint2D p = set.get(i);
            double distance = distance(a, b, p);
            if (distance > maxDistance) {
                maxDistance = distance;
                maxDistancePointIndex = i;
            }
        }

        final APoint2D p = set.get(maxDistancePointIndex);
        set.remove(maxDistancePointIndex);
        final int insertPosition = hull.indexOf(b);
        hull.add(insertPosition, p);

        // Determine who's to the left of a
        final ArrayList<APoint2D> leftOfA = new ArrayList<>();
        for (final APoint2D m : set) {
            if (isLeftOf(a, p, m)) {
                leftOfA.add(m);
            }
        }

        // Determine who's to the left of b
        final ArrayList<APoint2D> leftOfB = new ArrayList<>();
        for (final APoint2D m : set) {
            if (isLeftOf(p, b, m)) {
                leftOfB.add(m);
            }
        }

        hullSet(a, p, leftOfA, hull);
        hullSet(p, b, leftOfB, hull);
    }

    /**
     * is z to the left of the line from a to b?
     *
     * @return true, if z to left of line from a to b
     */
    private boolean isLeftOf(final APoint2D a, final APoint2D b, final APoint2D z) {
        return ((b.getX() - a.getX()) * (z.getY() - a.getY()) - (b.getY() - a.getY()) * (z.getX() - a.getX())) > 0;
    }

    /**
     * returns distance of point z from line through a and b
     *
     * @return distance to line
     */
    private double distance(final APoint2D a, final APoint2D b, final APoint2D z) {
        final double ABx = b.getX() - a.getX();
        final double ABy = b.getY() - a.getY();
        return Math.abs(ABx * (a.getY() - z.getY()) - ABy * (a.getX() - z.getX()));
    }

}
