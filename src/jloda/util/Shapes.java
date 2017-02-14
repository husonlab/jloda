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
        final float radius = 0.5f * Math.min(width, height) + 0.5f;
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
        final int radius = Math.min(width, height) / 2;
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
        ;
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
