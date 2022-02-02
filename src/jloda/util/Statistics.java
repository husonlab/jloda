/*
 * Statistics.java Copyright (C) 2022 Daniel H. Huson
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
    private double min = Integer.MAX_VALUE;
    private double max = Integer.MIN_VALUE;

    /**
     * computes simple statistics for given collection of numbers
     *
	 */
    public Statistics(Iterable<? extends Number> data) {
        int count = 0;
        for (Number number : data) {
            double value = number.doubleValue();
            sum += value;
            if (value < min)
                min = value;
            if (value > max)
                max = value;
            count++;
        }
        this.count = count;
        if (count > 0) {
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
     * computes simple statistics for given collection of numbers
     *
	 */
    public Statistics(int[] data) {
        count = data.length;
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
     * computes simple statistics for given collection of numbers
     *
	 */
    public Statistics(float[] data) {
        count = data.length;
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
     * computes simple statistics for given collection of numbers
     *
	 */
    public Statistics(double[] data) {
        count = data.length;
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
		return String.format("n=%d mean=%s stdDev=%s min=%s max=%s", count, StringUtils.removeTrailingZerosAfterDot("" + (float) mean),
				StringUtils.removeTrailingZerosAfterDot("" + (float) stdDev),
				StringUtils.removeTrailingZerosAfterDot("" + (float) min),
				StringUtils.removeTrailingZerosAfterDot("" + (float) max));
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

    public static int[] getBinnedCounts(Collection<Integer> values, int min, int max, int numberOfBins) {
        final int[] result = new int[numberOfBins];

        for (int value : values) {
            result[(int) (numberOfBins * (double) (value - min) / (max - min + 1))]++;
        }
        return result;
    }
}
