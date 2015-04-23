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
