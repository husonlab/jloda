/*
 *  Copyright (C) 2015 Daniel H. Huson
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
package jloda.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * calculates basic statistics
 * Daniel Huson, 5.2006
 */
public class Correlation {

    /**
     * compute Pearson's correlation coefficient
     *
     * @param n
     * @param x
     * @param y
     * @return correlation coefficient between -1 and 1
     */
    public static double computePersonsCorrelationCoefficent(int n, double[] x, double[] y) {
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        double sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        final double bottom = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        if (bottom == 0)
            return 0;
        final double top = n * sumXY - sumX * sumY;
        return (float) (top / bottom);
    }

    /**
     * compute Pearson's correlation coefficient
     *
     * @param n
     * @param x
     * @param y
     * @return correlation coefficient between -1 and 1
     */
    public static float computePersonsCorrelationCoefficent(int n, float[] x, float[] y) {
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        double sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        final double bottom = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        if (bottom == 0)
            return 0;
        final double top = n * sumXY - sumX * sumY;
        return (float) (top / bottom);
    }

    /**
     * compute Pearson's correlation coefficient
     *
     * @param n
     * @param xValues
     * @param yValues
     * @return correlation coefficient between -1 and 1
     */
    public static <T extends Number> double computePersonsCorrelationCoefficent(int n, Collection<T> xValues, Collection<T> yValues) {
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        double sumY2 = 0;

        final Iterator<T> itX = xValues.iterator();
        final Iterator<T> itY = yValues.iterator();
        for (int i = 0; i < n; i++) {
            double x = itX.next().doubleValue();
            double y = itY.next().doubleValue();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            sumY2 += y * y;
        }

        final double bottom = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        if (bottom == 0)
            return 0;
        final double top = n * sumXY - sumX * sumY;
        return (float) (top / bottom);
    }
}
