/*
 * Shapes.java Copyright (C) 2019. Daniel H. Huson
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

/**
 * creates some basic shapes
 * Created by huson on 2/13/17.
 */
public class Shapes {
    /**
     * create n-pointed star
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param n
     * @return coordinates
     */
    public static float[][] createStar(int x, int y, int width, int height, int n) {
        final float radius = 0.5f * Math.min(width, height);
        x += radius;
        y += radius;
        final float[] xCoordinates = new float[2 * n];
        final float[] yCoordinates = new float[2 * n];
        for (int i = 0; i < n; i++) {
            xCoordinates[2 * i] = x + (float) (radius * Math.sin(i * Math.PI / (0.5 * n)));
            yCoordinates[2 * i] = y - (float) (radius * Math.cos(i * Math.PI / (0.5 * n)));
            xCoordinates[2 * i + 1] = x + (float) (0.5f * radius * Math.sin((i + 1.0f / n) * Math.PI / (0.5 * n)));
            yCoordinates[2 * i + 1] = y - (float) (0.5f * radius * Math.cos((i + 1.0f / n) * Math.PI / (0.5 * n)));
        }
        return new float[][]{xCoordinates, yCoordinates};
    }

    /**
     * create n-sided regular polygon
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param n
     * @return coordinates
     */
    public static float[][] createRegularPolygon(int x, int y, int width, int height, int n) {
        final float radius = 0.5f * Math.min(width, height);
        x += radius;
        y += radius;
        final float[] xCoordinates = new float[n];
        final float[] yCoordinates = new float[n];
        for (int i = 0; i < n; i++) {
            xCoordinates[i] = x + (float) (radius * Math.cos(i * Math.PI / (0.5 * n)));
            yCoordinates[i] = y + (float) (radius * Math.sin(i * Math.PI / (0.5 * n)));
        }
        return new float[][]{xCoordinates, yCoordinates};
    }

    /**
     * create a cross +
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @return coordinates
     */
    public static float[][] createCrossPlus(int x, int y, int width, int height) {
        final float x1 = x + 0.333f * width;
        final float x2 = x + 0.666f * width;
        final float x3 = x + width;
        final float y1 = y + 0.333f * height;
        final float y2 = y + 0.666f * height;
        final float y3 = y + height;
        return new float[][]{{x3, x2, x2, x1, x1, x, x, x1, x1, x2, x2, x3}, {y1, y1, y, y, y1, y1, y2, y2, y3, y3, y2, y2}};
    }

    /**
     * create a cross X
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @return coordinates
     */
    public static float[][] createCrossX(int x, int y, int width, int height) {
        final float x1 = x + 0.333f * width;
        final float x2 = x + 0.5f * width;
        final float x3 = x + 0.666f * width;

        final float x4 = x + width;
        final float y1 = y + 0.333f * height;
        final float y2 = y + 0.5f * height;
        final float y3 = y + 0.666f * height;
        final float y4 = y + height;
        return new float[][]{
                {x4, x3, x2, x1, x, x1, x, x1, x2, x3, x4, x3},
                {y, y, y1, y, y, y2, y4, y4, y3, y4, y4, y2}};
    }
}
