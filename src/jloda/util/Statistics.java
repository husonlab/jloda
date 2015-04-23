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

import java.util.Collection;

/**
 * calculates basic statistics
 * Daniel Huson, 5.2006
 */
public class Statistics {
    double mean;
    int count;
    double sum;
    double stdDev;
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;

    /**
     * computes simple statistics for given collection of numbers
     *
     * @param data
     */
    public Statistics(Collection data) {
        for (Object aData : data) {
            double value = ((Number) aData).doubleValue();
            sum += value;
            count++;
            if (value < min)
                min = value;
            if (value > max)
                max = value;
        }
        if (count > 0) {
            mean = sum / count;
            if (data.size() > 1) {
                double sum2 = 0;
                for (Object aData : data) {
                    double value = ((Number) aData).doubleValue();
                    sum2 += (value - mean) * (value - mean);
                }
                stdDev = Math.sqrt(sum2 / (data.size() - 1));
            }
        } else {
            min = max = 0;
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
}
