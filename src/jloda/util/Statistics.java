/**
 * Statistics.java
 * Copyright (C) 2016 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.util;

import java.util.Collection;

/**
 * calculates basic statistics
 * Daniel Huson, 5.2006
 */
public class Statistics {
    private double mean;
    private final int count;
    private double sum;
    private double stdDev;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;

    /**
     * computes simple statistics for given collection of numbers
     *
     * @param data
     */
    public Statistics(Collection<? extends Number> data) {
        count = data.size();
        if (count > 0) {
            for (Number number : data) {
                double value = number.doubleValue();
                sum += value;
                if (value < min)
                    min = value;
                if (value > max)
                    max = value;
            }
            mean = sum / count;
            if (count > 1) {
                double sum2 = 0;
                for (Number number : data) {
                    double value = number.doubleValue();
                    sum2 += (value - mean) * (value - mean);
                }
                stdDev = Math.sqrt(sum2 / count);
            }
        }
    }

    /**
     * gets string representation of stats
     *
     * @return string
     */
    public String toString() {
        return "n=" + count + " mean=" + (float) mean + " stdDev=" + (float) stdDev + " min=" + (float) min + " max=" + (float) max;
    }

    public double getMean() {
        return mean;
    }

    public int getCount() {
        return count;
    }

    public double getSum() {
        return sum;
    }

    public double getStdDev() {
        return stdDev;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getNormalized(double value) {
        if (stdDev > 0)
            return (value - mean) / stdDev;
        else
            return value;
    }

    public double getZScore(double value) {
        return (stdDev > 0 ? (value - mean) / stdDev : 0);
    }
}
