/*
 * NodeData.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph;

import jloda.util.StringUtils;

/**
 * multi-sample data associated with a node
 * Daniel Huson, 1.2013
 */
public class NodeData {
    private float[] assigned;
    private float[] summarized;
    private float countAssigned;
    private float maxAssigned;
    private float countSummarized;
    private float maxSummarized;

    private double upPValue = -1; // p-value of left
    private double downPValue = -1;   // p-value for right

    /**
     * constructor
     *
	 */
    public NodeData(float[] assigned, float[] summarized) {
        setAssigned(assigned);
        setSummarized(summarized);
    }

    public float[] getAssigned() {
        return assigned;
    }

    public float getAssigned(int i) {
        return assigned[i];
    }

    public void setAssigned(float[] assigned) {
        this.assigned = assigned;
        countAssigned = 0;
        maxAssigned = 0;
        if (assigned != null) {
            for (float value : assigned) {
                countAssigned += value;
                maxAssigned = Math.max(maxAssigned, value);
            }
        }
    }

    public float[] getSummarized() {
        return summarized;
    }

    public float getSummarized(int i) {
        return summarized[i];
    }

    public void setSummarized(float[] summarized) {
        this.summarized = summarized;
        countSummarized = 0;
        maxSummarized = 0;
        if (summarized != null) {
            for (float value : summarized) {
                countSummarized += value;
                maxSummarized = Math.max(maxSummarized, value);
            }
        }
    }

    public void addToSummarized(int i, int add) {
        summarized[i] += add;
        countSummarized += add;
        if (summarized[i] > maxSummarized)
            maxSummarized = summarized[i];
    }

    public float getCountAssigned() {
        return countAssigned;
    }

    public float getMaxAssigned() {
        return maxAssigned;
    }

    public float getCountSummarized() {
        return countSummarized;
    }

    public float getMaxSummarized() {
        return maxSummarized;
    }

    public double getUpPValue() {
        return upPValue;
    }

    public void setUpPValue(double upPValue) {
        this.upPValue = upPValue;
    }

    public double getDownPValue() {
        return downPValue;
    }

    public void setDownPValue(double downPValue) {
        this.downPValue = downPValue;
    }

    public String toString() {
		return "assigned: " + StringUtils.toString(assigned, 0, assigned.length, ",", true) + ", summarized: " + StringUtils.toString(summarized, 0, summarized.length, ",", true);
    }

    public NodeData clone() {
        return new NodeData(assigned, summarized);
    }
}
