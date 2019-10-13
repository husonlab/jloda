/*
 * IntervalNode.java Copyright (C) 2019. Daniel H. Huson
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
 * IntervalNode.java
 * do What The F... you want to Public License
 *  Version 1.0, March 2000
 *  * Copyright (C) 2000 Banlu Kemiyatorn (]d).
 * 136 Nives 7 Jangwattana 14 Laksi Bangkok
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * Ok, the purpose of this license is simple and you just
 * DO WHAT THE F... YOU WANT TO.
 */
package jloda.util.interval;

import java.util.*;
import java.util.Map.Entry;

/**
 * The Node class contains the interval tree information for one single node
 *
 * @author Kevin Dolan
 * Extended by Daniel Huson, 2.2017
 */
public class IntervalNode<Type> {
    private final SortedMap<Interval<Type>, List<Interval<Type>>> intervals;
    private final int center;
    private IntervalNode<Type> leftNode;
    private IntervalNode<Type> rightNode;

    /**
     * create new empty node
     */
    IntervalNode() {
        intervals = new TreeMap<>();
        center = 0;
        leftNode = null;
        rightNode = null;
    }

    /**
     * create node for a collection of intervals
     *
     * @param intervals
     */
    IntervalNode(Collection<Interval<Type>> intervals) {
        if (intervals.size() == 0) {
            this.intervals = new TreeMap<>();
            center = 0;
            leftNode = null;
            rightNode = null;
        } else {
            this.intervals = new TreeMap<>();

            // set center to median:
            if (intervals.size() == 1) {
                final Interval<Type> interval = intervals.iterator().next();
                center = (interval.getStart() + interval.getEnd()) >>> 1;
            } else {
                final int[] endPoints = new int[2 * intervals.size()];

                int z = 0;
                for (Interval<Type> interval : intervals) {
                    endPoints[z++] = interval.getStart();
                    endPoints[z++] = interval.getEnd();
                }
                Arrays.sort(endPoints);
                center = endPoints[endPoints.length >>> 1]; // median, not interpolated
            }

            final List<Interval<Type>> left = new ArrayList<>();
            final List<Interval<Type>> right = new ArrayList<>();

            for (final Interval<Type> interval : intervals) {
                if (interval.getEnd() < center)
                    left.add(interval);
                else if (interval.getStart() > center)
                    right.add(interval);
                else {
                    List<Interval<Type>> posting = this.intervals.computeIfAbsent(interval, k -> new ArrayList<>());
                    posting.add(interval);
                }
            }

            if (left.size() > 0)
                leftNode = new IntervalNode<>(left);
            if (right.size() > 0)
                rightNode = new IntervalNode<>(right);
        }
    }

    /**
     * create node with single interval
     *
     * @param interval
     */
    IntervalNode(Interval<Type> interval) {
        intervals = new TreeMap<>();
        center = (interval.getStart() + interval.getEnd()) / 2;

        final List<Interval<Type>> posting = new ArrayList<>();
        posting.add(interval);
        intervals.put(interval, posting);
    }

    /**
     * add a node to an existing tree
     *
     * @param interval
     */
    void add(Interval<Type> interval) {
        if (interval.getEnd() < center) {
            if (leftNode == null)
                leftNode = new IntervalNode<>(interval);
            else
                leftNode.add(interval);
        } else if (interval.getStart() > center) {
            if (rightNode == null)
                rightNode = new IntervalNode<>(interval);
            else
                rightNode.add(interval);
        } else {
            List<Interval<Type>> posting = intervals.computeIfAbsent(interval, k -> new ArrayList<>());
            posting.add(interval);
        }
    }

    /**
     * Perform a stabbing query on the node
     *
     * @param pos the pos to query at
     * @return all intervals containing pos
     */
    ArrayList<Interval<Type>> stab(int pos) {
        final ArrayList<Interval<Type>> result = new ArrayList<>();

        for (Entry<Interval<Type>, List<Interval<Type>>> entry : intervals.entrySet()) {
            if (entry.getKey().contains(pos))
                result.addAll(entry.getValue());
            else if (entry.getKey().getStart() > pos)
                break;
        }

        if (pos < center && leftNode != null)
            result.addAll(leftNode.stab(pos));
        else if (pos > center && rightNode != null)
            result.addAll(rightNode.stab(pos));
        return result;
    }

    /**
     * Perform an interval intersection query on the node
     *
     * @param target the interval to intersect
     * @return all intervals containing time
     */
    ArrayList<Interval<Type>> query(Interval<?> target) {
        final ArrayList<Interval<Type>> result = new ArrayList<>();

        for (Entry<Interval<Type>, List<Interval<Type>>> entry : intervals.entrySet()) {
            if (entry.getKey().intersects(target))
                result.addAll(entry.getValue());
            else if (entry.getKey().getStart() > target.getEnd())
                break;
        }

        if (target.getStart() < center && leftNode != null)
            result.addAll(leftNode.query(target));
        if (target.getEnd() > center && rightNode != null)
            result.addAll(rightNode.query(target));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(center).append(": ");
        for (Entry<Interval<Type>, List<Interval<Type>>> entry : intervals.entrySet()) {
            sb.append("[").append(entry.getKey().getStart()).append(",").append(entry.getKey().getEnd()).append("]:{");
            for (Interval<Type> interval : entry.getValue()) {
                sb.append("(").append(interval.getStart()).append(",").append(interval.getEnd()).append(",").append(interval.getData()).append(")");
            }
            sb.append("} ");
        }
        return sb.toString();
    }

    /**
     * recursively creates string that describes this node and subtree below
     *
     * @param level
     * @return string
     */
    public String toStringRec(int level) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(Math.max(0, level)));
        sb.append(toString()).append("\n");
        if (leftNode != null)
            sb.append(leftNode.toStringRec(level + 1));
        if (rightNode != null)
            sb.append(rightNode.toStringRec(level + 1));
        return sb.toString();
    }
}