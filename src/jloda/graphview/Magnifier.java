/**
 * Magnifier.java 
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
package jloda.graphview;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * manages the magnifier.
 * Daniel Huson, 1.2007
 */
public class Magnifier {
    final GraphView graphView;
    final Transform trans;
    final JScrollBar scrollBarX;
    final JScrollBar scrollBarY;
    private double centerXpercent = 50; // position of center in percent
    private double centerYpercent = 50; // position of center in percent
    private double radiusPercent = 90;  // radius in percent
    private double magnificationFactor = 1;
    private double displacement = 0.75; // new distance (*radius) from axis of a point that originally had distance 0.5*radius
    private boolean active = false;
    private boolean inRectilinearMode = false; // in this mode, don't need to add internal nodes to edges

    private boolean hyperbolicMode = false;

    // what did mouse down hit?
    public final static int HIT_NOTHING = 0;
    public final static int HIT_RESIZE = 1;
    public final static int HIT_MOVE = 2;
    public final static int HIT_INCREASE_MAGNIFICATION = 4;
    public final static int HIT_DECREASE_MAGNIFICATION = 8;

    /**
     * constructor
     *
     * @param graphView
     */
    public Magnifier(GraphView graphView, Transform trans) {
        this.graphView = graphView;
        this.trans = trans;
        scrollBarX = graphView.getScrollPane().getHorizontalScrollBar();
        scrollBarY = graphView.getScrollPane().getVerticalScrollBar();
    }

    /**
     * does mouse click hit magnifier?
     *
     * @param x
     * @param y
     * @return what was hit
     */
    public int hit(int x, int y) {
        if (isActive()) {
            if (!isInRectilinearMode()) {
                double centerX = scrollBarX.getValue() + getCenterX() * scrollBarX.getVisibleAmount() / 100;
                double centerY = scrollBarY.getValue() + getCenterY() * scrollBarY.getVisibleAmount() / 100;
                double min = Math.min(scrollBarX.getVisibleAmount(), scrollBarY.getVisibleAmount());
                double r = getRadius() * min / 200;
                Rectangle rect = new Rectangle((int) (centerX - r), (int) (centerY - r), (int) (2 * r), (int) (2 * r));
                ArrowButton ab1 = new ArrowButton(rect.x + rect.width / 2, rect.y, true);
                if (ab1.hit(x, y))
                    return HIT_RESIZE;
                ArrowButton ab2 = new ArrowButton(rect.x + rect.width / 2, rect.y, false);
                if (ab2.hit(x, y))
                    return HIT_RESIZE;
                ZoomButton zb1 = new ZoomButton(rect.x + rect.width, rect.y + rect.height / 2, true);
                if (zb1.hit(x, y))
                    return HIT_INCREASE_MAGNIFICATION;
                ZoomButton zb2 = new ZoomButton(rect.x + rect.width, rect.y + rect.height / 2, false);
                if (zb2.hit(x, y))
                    return HIT_DECREASE_MAGNIFICATION;

                double distance = new Point2D.Double(centerX, centerY).distance(x, y);
                if (Math.abs(distance - r) < 15)
                    return HIT_MOVE;
            } else {
                double centerY = scrollBarY.getValue() + getCenterY() * scrollBarY.getVisibleAmount() / 100;
                double r = getRadius() * scrollBarY.getVisibleAmount() / 200;
                Rectangle rect = new Rectangle(scrollBarX.getValue() + 1, (int) (centerY - r + 1), scrollBarX.getVisibleAmount() - 2, (int) (2 * r - 2));

                ArrowButton ab1 = new ArrowButton(rect.x + rect.width - 10, rect.y, true);
                if (ab1.hit(x, y))
                    return HIT_RESIZE;
                ArrowButton ab2 = new ArrowButton(rect.x + rect.width - 10, rect.y, false);
                if (ab2.hit(x, y))
                    return HIT_RESIZE;
                // draw zoom controls
                ZoomButton zb1 = new ZoomButton(rect.x + 20, rect.y + 5, true);
                if (zb1.hit(x, y))
                    return HIT_INCREASE_MAGNIFICATION;
                ZoomButton zb2 = new ZoomButton(rect.x + 20, rect.y + 5, false);
                if (zb2.hit(x, y))
                    return HIT_DECREASE_MAGNIFICATION;
                if (Math.abs(Math.abs(centerY - y) - r) < 15)
                    return HIT_MOVE;
            }
        }
        return HIT_NOTHING;
    }

    /**
     * move the magnification center by the given difference of coordinates
     *
     * @param xOld
     * @param yOld
     * @param xNew
     * @param yNew
     */
    public void move(int xOld, int yOld, int xNew, int yNew) {
        double dXPercent = 0;
        if (!isInRectilinearMode())
            dXPercent = 100.0 * (xNew - xOld) / (double) scrollBarX.getVisibleAmount();
        double dYPercent = 100.0 * (yNew - yOld) / (double) scrollBarY.getVisibleAmount();

        double newX = getCenterX() + dXPercent;
        double newY = getCenterY() + dYPercent;
        if (newX > 0 && newX < 100 && newY > 0 && newY < 100)
            setCenter(getCenterX() + dXPercent, getCenterY() + dYPercent);
    }

    /**
     * resize the magnification center by the given difference of coordinates
     *
     * @param yOld
     * @param yNew
     */
    public void resize(int yOld, int yNew) {
        double dYPercent = 200.0 * (yOld - yNew) / (double) scrollBarY.getVisibleAmount();
        setRadius(Math.max(0, Math.min(100, getRadius() + dYPercent)));
    }


    /**
     * get the magnification radius between 0 and 100 %
     *
     * @return magnification factor
     */
    public double getRadius() {
        return radiusPercent;
    }

    /**
     * set the magnification radius between 0 and 100 %
     *
     * @param radiusPercent
     */
    public void setRadius(double radiusPercent) {
        this.radiusPercent = Math.max(0, Math.min(100, radiusPercent));

        // reset factor:
        int min = Math.min(scrollBarX.getVisibleAmount(), scrollBarY.getVisibleAmount());
        double r = this.radiusPercent * min / 200;
        magnificationFactor = 0.5 * r * (1 - displacement) / (displacement - 0.5);
    }

    /**
     * set the magnification center in percent of window width and height
     *
     * @param xPercent
     * @param yPercent
     */
    public void setCenter(double xPercent, double yPercent) {
        centerXpercent = xPercent;
        centerYpercent = yPercent;
    }

    /**
     * set the magnification center in percent of window width
     *
     * @return x percentage
     */
    public double getCenterX() {
        return centerXpercent;
    }

    /**
     * set the magnification center in percent of window height
     *
     * @return y percentage
     */
    public double getCenterY() {
        return centerYpercent;
    }

    /**
     * get the magnification displacement
     *
     * @return displacement
     */
    public double getDisplacement() {
        return displacement;
    }

    /**
     * set the displacement. This must be a number between 0.5 and 1
     *
     * @param displacement >0.5 and <1
     */
    public void setDisplacement(double displacement) {
        this.displacement = displacement;
        int min = Math.min(scrollBarX.getVisibleAmount(), scrollBarY.getVisibleAmount());
        double r = this.radiusPercent * min / 200;
        magnificationFactor = 0.5 * r * (1 - displacement) / (displacement - 0.5);
    }

    /**
     * increase the displacment
     *
     * @return true, if changed
     */
    public boolean increaseDisplacement() {
        setDisplacement(getDisplacement() + 0.1 * (1.0 - getDisplacement()));
        return true;

    }

    /**
     * decrease the displacment
     *
     * @return true, if changed
     */
    public boolean decreaseDisplacement() {
        double d = (10.0 * getDisplacement() - 1.0) / 9.0;
        if (d <= 0.5)
            d = (0.5 * (0.5 + getDisplacement()));
        if (d > 0.5) {
            setDisplacement(d);
            return true;
        }
        return false;
    }


    /**
     * set the magnifier on or off
     *
     * @param active
     */
    public void setActive(boolean active) {
        this.active = active;
        if (active)
            setDisplacement(getDisplacement()); // make sure everything is uptodate
    }

    /**
     * get the magnifier on or off
     *
     * @return true, if magnifier is being used
     */
    public boolean isActive() {
        return active;
    }

    /**
     * turn magnification on
     *
     * @param centerXpercent x coordinate of center of magnification in percent of width
     * @param centerYpercent y coordinate of center of magnification in percent of height
     * @param radiusPercent  radius or height of magnifier in percent of maximal possible
     */
    public void setMagnifier(int centerXpercent, int centerYpercent, int radiusPercent, double displacement) {
        this.centerXpercent = centerXpercent;
        this.centerYpercent = centerYpercent;
        this.radiusPercent = radiusPercent;
        this.displacement = displacement;
        int min = Math.min(scrollBarX.getVisibleAmount(), scrollBarY.getVisibleAmount());
        double r = this.radiusPercent * min / 200;
        magnificationFactor = 0.5 * r * (1 - displacement) / (displacement - 0.5);

    }

    /**
     * apply the magnifier to a point. This is used by Transform
     *
     * @param x     in device coordinates before application   of magnification
     * @param y     in device coordinates before application   of magnification
     * @param point result in device   after magnifiation
     */
    public void applyMagnifier(double x, double y, Point2D point) {
        if (inRectilinearMode)  // magnify along the center horizontal axis
        {
            double z = scrollBarY.getValue() + centerYpercent * scrollBarY.getVisibleAmount() / 100;
            double h = radiusPercent * scrollBarY.getVisibleAmount() / 200;

            if (!hyperbolicMode && y > z - h && y < z + h) {
                double yNew;

                if (y <= z)
                    yNew = z - (h * (z - y) / (z - y + magnificationFactor)) * (1 + magnificationFactor / h);
                else
                    yNew = z + (h * (y - z) / (y - z + magnificationFactor)) * (1 + magnificationFactor / h);
                point.setLocation(x, yNew);
            } else if (hyperbolicMode) {
                double yNew;

                if (y <= z)
                    yNew = z - h * (z - y) / (z - y + magnificationFactor);
                else
                    yNew = z + h * (y - z) / (y - z + magnificationFactor);
                point.setLocation(x, yNew);
            } else
                point.setLocation(x, y);

        } else // centralized magnification
        {
            int min = Math.min(scrollBarX.getVisibleAmount(), scrollBarY.getVisibleAmount());

            double centerX = scrollBarX.getValue() + centerXpercent * scrollBarX.getVisibleAmount() / 100;
            double centerY = scrollBarY.getValue() + centerYpercent * scrollBarY.getVisibleAmount() / 100;
            double radius = radiusPercent * min / 200;

            double d12 = (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY);

            if (!hyperbolicMode && d12 > 0 && d12 < radius * radius) {
                double d1 = Math.sqrt(d12);
                double d2 = radius * (d1 / (d1 + magnificationFactor));
                double xNew = centerX + ((x - centerX) * d2 / d1) * (1 + magnificationFactor / radius);
                double yNew = centerY + ((y - centerY) * d2 / d1) * (1 + magnificationFactor / radius);
                point.setLocation(xNew, yNew);
            } else if (hyperbolicMode) {
                double d1 = Math.sqrt(d12);
                double d2 = radius * (d1 / (d1 + magnificationFactor));
                double xNew = centerX + (x - centerX) * d2 / d1;
                double yNew = centerY + (y - centerY) * d2 / d1;
                point.setLocation(xNew, yNew);
            } else {
                point.setLocation(x, y);
            }
        }
    }

    /**
     * are we in rectilinear mode?
     * In this mode, all edges are drawn either horizontal or vertical and zoom is vertical only. No need
     * to add internal nodes to edges. Used by TreeDrawerParallel
     *
     * @return true, if so
     */
    public boolean isInRectilinearMode() {
        return inRectilinearMode;
    }

    /**
     * set rectilinear mode
     *
     * @param inRectilinearMode
     */
    public void setInRectilinearMode(boolean inRectilinearMode) {
        this.inRectilinearMode = inRectilinearMode;
    }

    /**
     * draw the magnifier
     *
     * @param gc
     */
    public void draw(Graphics2D gc) {
        if (isActive()) {
            gc.setColor(Color.GREEN);

            if (!inRectilinearMode) {
                double centerX = scrollBarX.getValue() + getCenterX() * scrollBarX.getVisibleAmount() / 100;
                double centerY = scrollBarY.getValue() + getCenterY() * scrollBarY.getVisibleAmount() / 100;
                int min = Math.min(scrollBarX.getVisibleAmount(), scrollBarY.getVisibleAmount());
                double r = getRadius() * min / 200.0;
                Rectangle rect = new Rectangle((int) (centerX - r), (int) (centerY - r), (int) (2 * r), (int) (2 * r));
                // draw circle:
                gc.drawArc(rect.x, rect.y, rect.width, rect.height, 0, 360);
                // draw up and down arrows:
                ArrowButton ab1 = new ArrowButton(rect.x + rect.width / 2, rect.y, true);
                ab1.draw(gc);
                ArrowButton ab2 = new ArrowButton(rect.x + rect.width / 2, rect.y, false);
                ab2.draw(gc);

                // draw zoom controls
                ZoomButton zb1 = new ZoomButton(rect.x + rect.width, rect.y + rect.height / 2, true);
                zb1.draw(gc);
                ZoomButton zb2 = new ZoomButton(rect.x + rect.width, rect.y + rect.height / 2, false);
                zb2.draw(gc);

                //gc.drawString("" + radiusPercent, rect.x + 10, rect.y + rect.height - 10);
                // gc.drawString("" + getDisplacement(), rect.x + 10, rect.y + rect.height - 10);

            } else {
                double centerY = scrollBarY.getValue() + getCenterY() * scrollBarY.getVisibleAmount() / 100;
                double r = getRadius() * scrollBarY.getVisibleAmount() / 200;
                Rectangle rect = new Rectangle(scrollBarX.getValue() + 1, (int) (centerY - r + 1), scrollBarX.getVisibleAmount() - 2, (int) (2 * r - 2));
                // draw rect:
                gc.draw(rect);
                ArrowButton ab1 = new ArrowButton(rect.x + rect.width - 10, rect.y, true);
                ab1.draw(gc);
                ArrowButton ab2 = new ArrowButton(rect.x + rect.width - 10, rect.y, false);
                ab2.draw(gc);

                // draw zoom controls
                ZoomButton zb1 = new ZoomButton(rect.x + 20, rect.y + 5, true);
                zb1.draw(gc);
                ZoomButton zb2 = new ZoomButton(rect.x + 20, rect.y + 5, false);
                zb2.draw(gc);

                //gc.drawString("" + radiusPercent, rect.x + 10, rect.y + rect.height - 10);
                // gc.drawString("" + getDisplacement(), rect.x + 10, rect.y + rect.height - 10);
            }
        }
    }


    class ArrowButton {
        final Polygon polygon;

        ArrowButton(int x, int y, boolean up) {
            if (up)
                polygon = new Polygon(new int[]{x - 5, x, x + 5}, new int[]{y, y - 5, y}, 3);
            else
                polygon = new Polygon(new int[]{x - 5, x, x + 5}, new int[]{y, y + 5, y}, 3);

        }

        void draw(Graphics2D gc) {
            gc.fill(polygon);
        }

        boolean hit(int x, int y) {
            return polygon.contains(x, y);
        }
    }

    class ZoomButton {
        final boolean up;
        final Rectangle rect;

        ZoomButton(int x, int y, boolean up) {
            this.up = up;
            if (up)
                rect = new Rectangle(x - 10, y - 5, 10, 10);
            else
                rect = new Rectangle(x, y - 5, 10, 10);
        }

        void draw(Graphics2D gc) {
            gc.draw(rect);
            if (up) {
                gc.drawLine(rect.x + 2, rect.y + rect.height / 2, rect.x + rect.width - 2, rect.y + rect.height / 2);
                gc.drawLine(rect.x + rect.width / 2, rect.y + 2, rect.x + rect.width / 2, rect.y + rect.height - 2);
            } else {
                gc.drawLine(rect.x + 2, rect.y + rect.height / 2, rect.x + rect.width - 2, rect.y + rect.height / 2);
            }
        }

        boolean hit(int x, int y) {
            return rect.contains(x, y);
        }
    }

    /**
     * get hyperbolic mode
     *
     * @return mode
     */
    public boolean isHyperbolicMode() {
        return hyperbolicMode;
    }

    /**
     * set hyperbolic mode
     *
     * @param hyperbolicMode
     */
    public void setHyperbolicMode(boolean hyperbolicMode) {
        this.hyperbolicMode = hyperbolicMode;
    }
}
