/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package jloda.util;

import java.awt.*;
import java.util.ArrayList;

/**
 * computes convex hull for a collection of two dimensional points
 *
 * daniel huson, 4.2015
 */
public class ConvexHull {
    /**
     * computes the convex hull of a set of two-dimensional points using the quick hull algorithm
     *
     * @param points
     * @return convex hull
     */
    public static ArrayList<Point> quickHull(final ArrayList<Point> points) {
        ArrayList<Point> convexHull = new ArrayList<>();
        if (points.size() < 3) {
            ArrayList<Point> result = new ArrayList<>(points.size());
            result.addAll(points);
            return result;
        }
        int minPointIndex = -1;
        int maxPointIndex = -1;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < minX) {
                minX = points.get(i).x;
                minPointIndex = i;
            }
            if (points.get(i).x > maxX) {
                maxX = points.get(i).x;
                maxPointIndex = i;
            }
        }
        final Point a = points.get(minPointIndex);
        final Point b = points.get(maxPointIndex);
        convexHull.add(a);
        convexHull.add(b);
        points.remove(a);
        points.remove(b);

        ArrayList<Point> leftSet = new ArrayList<>();
        ArrayList<Point> rightSet = new ArrayList<>();

        for (Point p : points) {
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
     * @param a
     * @param b
     * @param set
     * @param hull
     */
    private static void hullSet(final Point a, final Point b, final ArrayList<Point> set, final ArrayList<Point> hull) {
        if (set.size() == 0) return;

        if (set.size() == 1) {
            Point p = set.get(0);
            set.remove(p);
            final int insertPosition = hull.indexOf(b);
            hull.add(insertPosition, p);
            return;
        }

        int maxDistance = Integer.MIN_VALUE;
        int maxDistancePointIndex = -1;
        for (int i = 0; i < set.size(); i++) {
            final Point p = set.get(i);
            int distance = distance(a, b, p);
            if (distance > maxDistance) {
                maxDistance = distance;
                maxDistancePointIndex = i;
            }
        }

        final Point p = set.get(maxDistancePointIndex);
        set.remove(maxDistancePointIndex);
        final int insertPosition = hull.indexOf(b);
        hull.add(insertPosition, p);

        // Determine who's to the left of a
        final ArrayList<Point> leftOfA = new ArrayList<>();
        for (final Point m : set) {
            if (isLeftOf(a, p, m)) {
                leftOfA.add(m);
            }
        }

        // Determine who's to the left of b
        final ArrayList<Point> leftOfB = new ArrayList<>();
        for (final Point m : set) {
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
     * @param a
     * @param b
     * @param z
     * @return true, if z to left of line from a to b
     */
    private static boolean isLeftOf(final Point a, final Point b, final Point z) {
        return ((b.x - a.x) * (z.y - a.y) - (b.y - a.y) * (z.x - a.x)) > 0;
    }

    /**
     * returns distance of point z from line through a and b
     *
     * @param a
     * @param b
     * @param z
     * @return distance to line
     */
    private static int distance(final Point a, final Point b, final Point z) {
        final int ABx = b.x - a.x;
        final int ABy = b.y - a.y;
        return Math.abs(ABx * (a.y - z.y) - ABy * (a.x - z.x));
    }
}
