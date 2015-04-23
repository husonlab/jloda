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

/**
 * Random numbers from Gaussian distribution
 * @version $Id: RandomGaussian.java,v 1.3 2006-06-06 18:56:04 huson Exp $
 * @author Daniel Huson
 *        9.2003
 */
package jloda.util;

import java.util.Random;

/**
 * Random numbers for Gaussian distribution
 */
public class RandomGaussian {
    private double mean;
    private double stdDev;
    private final Random rand;

    /**
     * construct Gaussian random source with mean 0, std deviation 1
     */
    public RandomGaussian() {
        mean = 0;
        stdDev = 1;
        rand = new Random();
    }

    /**
     * construct Gaussian random source with given mean and std deviation
     *
     * @param mean
     * @param stdDev
     */
    public RandomGaussian(double mean, double stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
        rand = new Random();
    }

    /**
     * construct Gaussian random source with mean 0, std deviation 1
     */
    public RandomGaussian(int seed) {
        mean = 0;
        stdDev = 1;
        rand = new Random(seed);
    }

    /**
     * construct Gaussian random source with given mean and std deviation
     *
     * @param mean
     * @param stdDev
     */
    public RandomGaussian(double mean, double stdDev, int seed) {
        this.mean = mean;
        this.stdDev = stdDev;
        rand = new Random(seed);
    }

    /**
     * get mean
     *
     * @return mean
     */
    public double getMean() {
        return mean;
    }

    /**
     * sets the mean
     *
     * @param mean
     */
    public void setMean(double mean) {
        this.mean = mean;
    }

    /**
     * gets the set standard deviation
     *
     * @return standard deviation
     */
    public double getStdDev() {
        return stdDev;
    }

    /**
     * sets the standard deviation
     *
     * @param stdDev
     */
    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    /**
     * gets the next gaussian value
     *
     * @return next value
     */
    public double nextDouble() {
        return mean + (rand.nextGaussian() * stdDev);
    }

    /**
     * gets the next gaussian value
     *
     * @return next value
     */
    public float nextFloat() {
        return (float) nextDouble();
    }

    /**
     * gets the next gaussian value
     *
     * @return next value
     */
    public int nextInt() {
        return Math.round(nextFloat());
    }

    /**
     * gets the next gaussian value
     *
     * @return next value
     */
    public long nextLong() {
        return Math.round(nextDouble());
    }

    /**
     * sets the seed
     *
     * @param seed
     */
    public void setSeed(long seed) {
        rand.setSeed(seed);
    }
}
