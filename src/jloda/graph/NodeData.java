/**
 * NodeData.java 
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
package jloda.graph;

import jloda.util.Basic;

/**
 * multi-sample data associated with a node
 * Daniel Huson, 1.2013
 */
public class NodeData {
    private int[] assigned;
    private int[] summarized;
    private int countAssigned;
    private int maxAssigned;
    private int countSummarized;
    private int maxSummarized;

    private double upPValue = -1; // p-value of left
    private double downPValue = -1;   // p-value for right

    /**
     * constructor
     *
     * @param assigned
     * @param summarized
     */
    public NodeData(int[] assigned, int[] summarized) {
        setAssigned(assigned);
        setSummarized(summarized);
    }

    public int[] getAssigned() {
        return assigned;
    }

    public int getAssigned(int i) {
        return assigned[i];
    }

    public void setAssigned(int[] assigned) {
        this.assigned = assigned;
        countAssigned = 0;
        maxAssigned = 0;
        if (assigned != null) {
            for (int value : assigned) {
                countAssigned += value;
                maxAssigned = Math.max(maxAssigned, value);
            }
        }
    }

    public int[] getSummarized() {
        return summarized;
    }

    public int getSummarized(int i) {
        return summarized[i];
    }

    public void setSummarized(int[] summarized) {
        this.summarized = summarized;
        countSummarized = 0;
        maxSummarized = 0;
        if (summarized != null) {
            for (int value : summarized) {
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

    public int getCountAssigned() {
        return countAssigned;
    }

    public int getMaxAssigned() {
        return maxAssigned;
    }

    public int getCountSummarized() {
        return countSummarized;
    }

    public int getMaxSummarized() {
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
        return "assigned: " + Basic.toString(assigned, ",") + ", summarized: " + Basic.toString(summarized, ",");
    }

    public NodeData clone() {
        return new NodeData(assigned, summarized);
    }
}
