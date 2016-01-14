/**
 * Transform.java 
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

import jloda.util.Basic;
import jloda.util.Geometry;
import jloda.util.PolygonDouble;
import jloda.util.parse.NexusStreamParser;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Random;

/**
 * transformation class used to draw graph onto scrollpanel. Ensures that the rotated bounding box of
 * the world coordinates always has xmin,ymin corner at 0,0
 * Daniel Huson
 */
public class Transform {
    final Rectangle2D coordRect;
    final Rectangle2D rotatedCoordRect;
    final Point2D centerOfCoordRect;
    final Point2D centerOfRotatedCoordRect;
    double angle;
    double sinAngle;
    double cosAngle;
    double scaleX;
    double scaleY;
    boolean lockXYScale;
    boolean flipH;
    boolean flipV;
    int bottomMargin = 50;
    int topMargin = 50;
    int leftMargin = 50;
    int rightMargin = 50;
    private Random rand;
    Magnifier magnifier = null;

    final java.util.List<ITransformChangeListener> changeListeners = new LinkedList<>();

    /**
     * default constructor
     */
    public Transform() {
        coordRect = new Rectangle2D.Double();
        rotatedCoordRect = new Rectangle2D.Double();
        centerOfCoordRect = new Point2D.Double();
        centerOfRotatedCoordRect = new Point2D.Double();

        angle = 0;
        sinAngle = Math.sin(angle);
        cosAngle = Math.cos(angle);

        scaleX = 1;
        scaleY = 1;
        lockXYScale = true;
        flipH = false;
        flipV = false;
    }


    /**
     * constructor with given magnifier
     *
     * @param graphView
     */
    public Transform(GraphView graphView) {
        this();
        this.magnifier = new Magnifier(graphView, this);
    }

    /**
     * returns a clone (with no listeners)
     *
     * @return clone
     */
    public Object clone() throws CloneNotSupportedException {
        //super.clone();
        Transform trans = new Transform();
        trans.copy(this);
        return trans;
    }

    /**
     * copies a transform (except for its listeners)
     *
     * @param src
     */
    public void copy(Transform src) {
        coordRect.setRect(src.coordRect);
        rotatedCoordRect.setRect(src.rotatedCoordRect);
        centerOfCoordRect.setLocation(src.centerOfCoordRect);
        centerOfRotatedCoordRect.setLocation(src.centerOfRotatedCoordRect);
        angle = src.angle;
        sinAngle = src.sinAngle;
        cosAngle = src.cosAngle;
        scaleX = src.scaleX;
        scaleY = src.scaleY;
        lockXYScale = src.lockXYScale;
        flipH = src.flipH;
        flipV = src.flipV;
        bottomMargin = src.bottomMargin;
        topMargin = src.topMargin;
        leftMargin = src.leftMargin;
        rightMargin = src.rightMargin;
    }

    /**
     * reset scale, angle and flips
     */
    public void reset() {
        angle = 0;
        sinAngle = Math.sin(angle);
        cosAngle = Math.cos(angle);
        scaleX = 1;
        scaleY = 1;
        flipH = false;
        flipV = false;
        fireHasChanged();
    }

    /**
     * is the world empty
     *
     * @return true, if coordinate rectangle has width 0 and height 0
     */
    public boolean isEmpty() {
        return coordRect.getWidth() == 0 && coordRect.getHeight() == 0;
    }

    /**
     * gets a string representation
     */
    public String toString() {
        StringWriter sw = new StringWriter();
        try {
            write(sw);
        } catch (IOException ex) {
            Basic.caught(ex);
        }
        return sw.toString();
    }

    /**
     * writes the object
     *
     * @param w
     * @throws IOException
     */
    public void write(Writer w) throws IOException {
        w.write("angle:" + (float) angle + " scaleX:" + (float) scaleX + " scaleY:" + (float) scaleY + " flipH:" + (flipH ? 1 : 0) + " flipV:" + (flipV ? 1 : 0) +
                " leftMargin:" + leftMargin +
                " rightMargin:" + rightMargin +
                " topMargin:" + topMargin +
                " bottomMargin:" + bottomMargin +
                ";");
    }

    /**
     * read the object
     *
     * @param r
     * @throws IOException
     */
    public void read(Reader r) throws IOException {
        read(new NexusStreamParser(r));

    }

    /**
     * read the object
     *
     * @param np
     * @throws IOException
     */
    public void read(NexusStreamParser np) throws IOException {
        java.util.List<String> tokens = np.getTokensLowerCase(null, ";");
        angle = np.findIgnoreCase(tokens, "angle:", (float) angle);
        sinAngle = Math.sin(angle);
        cosAngle = Math.cos(angle);
        scaleX = (double) np.findIgnoreCase(tokens, "scaleX:", (float) scaleX);
        scaleY = (double) np.findIgnoreCase(tokens, "scaleY:", (float) scaleY);
        flipH = (np.findIgnoreCase(tokens, "flipH:", flipH ? 1 : 0) != 0);
        flipV = (np.findIgnoreCase(tokens, "flipV:", flipV ? 1 : 0) != 0);
        leftMargin = (int) np.findIgnoreCase(tokens, "leftMargin:", leftMargin);
        rightMargin = (int) np.findIgnoreCase(tokens, "rightMargin:", rightMargin);
        topMargin = (int) np.findIgnoreCase(tokens, "topMargin:", topMargin);
        bottomMargin = (int) np.findIgnoreCase(tokens, "bottomMargin:", bottomMargin);

        if (tokens.size() > 0)
            throw new IOException("Transform.read: illegal tokens: " + tokens);
    }

    /**
     * tell the transformation what the current bounding box for user coordinates is
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setCoordinateRect(double x, double y, double width, double height) {
        coordRect.setRect(x, y, width, height);
        centerOfCoordRect.setLocation(coordRect.getCenterX(), coordRect.getCenterY());
        setRotatedCoordinateRect();
        fireHasChanged();
    }

    /**
     * tell the transformation what the current bounding box for user coordinates is
     *
     * @param rect
     */
    public void setCoordinateRect(Rectangle2D rect) {
        coordRect.setRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        centerOfCoordRect.setLocation(coordRect.getCenterX(), coordRect.getCenterY());
        setRotatedCoordinateRect();
        fireHasChanged();
    }

    /**
     * sets the rotated coordinate rect
     */
    private void setRotatedCoordinateRect() {
        if (angle == 0)
            rotatedCoordRect.setRect(coordRect);
        else {
            final Point2D p00 = Geometry.rotateAbout(new Point2D.Double(coordRect.getMinX(), coordRect.getMinY()), angle, centerOfCoordRect);
            final Point2D p01 = Geometry.rotateAbout(new Point2D.Double(coordRect.getMaxX(), coordRect.getMinY()), angle, centerOfCoordRect);
            final Point2D p10 = Geometry.rotateAbout(new Point2D.Double(coordRect.getMinX(), coordRect.getMaxY()), angle, centerOfCoordRect);
            final Point2D p11 = Geometry.rotateAbout(new Point2D.Double(coordRect.getMaxX(), coordRect.getMaxY()), angle, centerOfCoordRect);

            rotatedCoordRect.setRect(p00.getX(), p00.getY(), 0, 0);
            rotatedCoordRect.add(p01);
            rotatedCoordRect.add(p10);
            rotatedCoordRect.add(p11);
        }
        centerOfRotatedCoordRect.setLocation(rotatedCoordRect.getCenterX(), rotatedCoordRect.getCenterY());
    }


    public Point2D getDeviceRotationCenter() {
        Point2D wp = (Point2D) centerOfRotatedCoordRect.clone();
        return w2d(wp);
    }

    public Point2D getWorldRotationCenter() {
        return centerOfRotatedCoordRect;
    }

    public AffineTransform getAffineTransform() {
        Point2D w00 = new Point2D.Double(0, 0);
        Point2D w10 = new Point2D.Double(0, 1);
        Point2D w11 = new Point2D.Double(1, 0);
        Point2D d00 = w2d(w00);
        Point2D d10 = w2d(w10);
        Point2D d11 = w2d(w11);

        double ratio1 = (w10.getX() - w00.getX()) / (w00.getY() - w10.getY());
        double ratio2 = (d00.getX() - d10.getX()) / (w00.getY() - w10.getY());

        double m00 = (d11.getX() - w11.getY() * ratio2 - d00.getX() + w00.getY() * ratio2) / (w11.getX() + w11.getY() * ratio1 - w00.getX() - w00.getY() * ratio1);
        double m01 = ratio1 * m00 + ratio2;
        double m02 = d00.getX() - w00.getX() * m00 - w00.getY() * m01;

        double ratio3 = (d00.getY() - d10.getY()) / (w00.getY() - w10.getY());

        double m10 = (d11.getY() - w11.getY() * ratio3 - d00.getY() + w00.getY() * ratio3) / (w11.getX() + w11.getY() * ratio1 - w00.getX() - w00.getY() * ratio1);
        double m11 = ratio1 * m10 + ratio3;
        double m12 = d00.getY() - w00.getX() * m10 - w00.getY() * m11;

        AffineTransform re = new AffineTransform();
        re.setTransform(m00, m10, m01, m11, m02, m12);
        return re;
    }

    /**
     * Computes the angle between a and b as observed from the center from device to world coordiante system
     *
     * @param deviceCenter
     * @param a
     * @param b
     * @return
     */
    public double computeObservedWorldAngle(Point2D deviceCenter, Point2D a, Point2D b) {
        Point2D wC = d2w(deviceCenter);
        Point2D wA = d2w(a);
        Point2D wB = d2w(b);
        return Geometry.computeObservedAngle(wC, wA, wB);
    }

    /**
     * transform from world coordinates to device coordinates
     *
     * @param wp
     * @param dp
     */
    public void w2d(Point2D wp, Point dp) {
        w2d(wp.getX(), wp.getY(), dp);
    }

    /**
     * transform from world coordinates to device coordinates
     *
     * @param x
     * @param y
     * @param dp
     */
    public void w2d(double x, double y, Point2D dp) {
        Point2D wp = new Point2D.Double(x,
                y);

        if (angle != 0) {
            // Geometry.rotateAbout(dd, angle, centerOfRotatedCoordRect, dd);
            // compute directly here to avoid overhead:
            wp.setLocation(wp.getX() - centerOfRotatedCoordRect.getX(), wp.getY() - centerOfRotatedCoordRect.getY());
            wp.setLocation(wp.getX() * cosAngle - wp.getY() * sinAngle + centerOfRotatedCoordRect.getX(),
                    wp.getX() * sinAngle + wp.getY() * cosAngle + centerOfRotatedCoordRect.getY());

        }
        wp = new Point2D.Double(flipH ? 2 * centerOfRotatedCoordRect.getX() - wp.getX() : wp.getX(),
                flipV ? 2 * centerOfRotatedCoordRect.getY() - wp.getY() : wp.getY());

        wp.setLocation(wp.getX() - rotatedCoordRect.getX(), wp.getY() - rotatedCoordRect.getY());

        wp.setLocation(scaleX != 1.0 ? wp.getX() * scaleX : wp.getX(), scaleY != 1.0 ? wp.getY() * scaleY : wp.getY());


        if (magnifier != null && magnifier.isActive())
            magnifier.applyMagnifier(wp.getX() + leftMargin, wp.getY() + topMargin, dp);
        else
            dp.setLocation(wp.getX() + leftMargin, wp.getY() + topMargin);
    }

    /**
     * gets the point in device coordinates
     *
     * @param wp point in world coordinatess
     * @return point in device coordinates
     */
    public Point w2d(Point2D wp) {
        final Point dp = new Point();
        w2d(wp, dp);
        return dp;
    }

    /**
     * gets the point in device coordinates
     *
     * @param x point in world coordinates
     * @param y point in world coordinates
     * @return point in device coordinates
     */
    public Point w2d(double x, double y) {
        final Point dp = new Point();
        w2d(new Point2D.Double(x, y), dp);
        return dp;
    }

    /**
     * transform from device coordinates to world coordinates
     *
     * @param dp
     * @param wp
     */
    public void d2w(Point2D dp, Point2D wp) {
        double x = dp.getX() - leftMargin;
        double y = dp.getY() - topMargin;
        if (scaleX != 1.0)
            x /= scaleX;
        if (scaleY != 1.0)
            y /= scaleY;
        x += rotatedCoordRect.getX();
        y += rotatedCoordRect.getY();

        wp.setLocation(flipH ? 2 * centerOfRotatedCoordRect.getX() - x : x, flipV ? 2 * centerOfRotatedCoordRect.getY() - y : y);

        if (angle != 0)
            Geometry.rotateAbout(wp, -angle, centerOfRotatedCoordRect, wp);
    }

    /**
     * gets the point in world coordinates
     *
     * @param dp point in device coordinatess
     * @return point in world coordinates
     */
    public Point2D d2w(Point2D dp) {
        final Point2D wp = new Point2D.Double();
        d2w(dp, wp);
        return wp;
    }

    /**
     * gets the point in world coordinates
     *
     * @return point in world coordinates
     */
    public Point2D d2w(int x, int y) {
        return d2w(new Point(x, y));
    }

    /**
     * transform from world coordinates to device coordinates.
     * Note that the location and the size of the rectangle are modified, but not
     * not its orientation, that is, the rectangle is NOT rotated
     *
     * @param rectWC input: rectangle in world coordinates
     * @param polyDC output: rectangular polygon in device coordinates
     */
    public void w2d(Rectangle2D rectWC, Polygon polyDC) {
        if (rectWC != null && polyDC != null) {
            polyDC.reset();
            Point a = w2d(rectWC.getX(), rectWC.getY());
            polyDC.addPoint(a.x, a.y);
            a = w2d(rectWC.getX(), rectWC.getY() + rectWC.getHeight());
            polyDC.addPoint(a.x, a.y);
            a = w2d(rectWC.getX() + rectWC.getWidth(), rectWC.getY() + rectWC.getHeight());
            polyDC.addPoint(a.x, a.y);
            a = w2d(rectWC.getX() + rectWC.getWidth(), rectWC.getY());
            polyDC.addPoint(a.x, a.y);
        }
    }

    /**
     * transform from world coordinates to device coordinates.
     * Note that the location and the size of the rectangle are modified, but not
     * not its orientation, that is, the rectangle is NOT rotated
     *
     * @param wp
     * @param dp
     */
    public Rectangle w2d(Rectangle2D wp, Rectangle dp) {
        if (wp != null && dp != null) {
            Point2D anchor = w2d(new Point2D.Double(wp.getX(), wp.getY()));
            double width = scaleX * wp.getWidth();
            double height = scaleY * wp.getHeight();
            dp.setRect(anchor.getX() - (flipH ? width : 0), anchor.getY() - (flipV ? height : 0), width, height);
            return dp;
        }
        return null;
    }

    /**
     * transforms from world coordinates to device coordinates.
     *
     * @param wp rect in world coordinatess
     * @return polygon in device coordinates
     */
    public Polygon w2d(Rectangle2D wp) {
        final Polygon dp = new Polygon();
        w2d(wp, dp);
        return dp;
    }


    /**
     * transform from device coordinates to world coordinates. Note that the rectangle is not rotated
     *
     * @param dp
     * @param wp
     */
    public void d2w(Rectangle2D dp, Rectangle2D wp) {
        Point2D anchor = d2w(new Point2D.Double(dp.getX(), dp.getY()));
        wp.setRect(anchor.getX(), anchor.getY(), dp.getWidth() / scaleX, dp.getHeight() / scaleY);
    }

    /**
     * gets the rectangle in device coordinates. Note that it is not rotated
     *
     * @param dp rectangle in device coordinatess
     * @return rectangle in world coordinates
     */
    public Rectangle2D d2w(Rectangle2D dp) {
        final Rectangle2D wp = new Rectangle2D.Double();
        d2w(dp, wp);
        return wp;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = Geometry.moduloTwoPI(angle);
        sinAngle = Math.sin(this.angle);
        cosAngle = Math.cos(this.angle);
        setRotatedCoordinateRect();
        fireHasChanged();
    }

    public void composeAngle(double delta) {
        this.angle += delta;
        sinAngle = Math.sin(angle);
        cosAngle = Math.cos(angle);
        setRotatedCoordinateRect();
        fireHasChanged();
    }

    public boolean getFlipH() {
        return flipH;
    }

    public void setFlipH(boolean flipH) {
        this.flipH = flipH;
        fireHasChanged();
    }

    public boolean getFlipV() {
        return flipV;
    }

    public void setFlipV(boolean flipV) {
        this.flipV = flipV;
        fireHasChanged();
    }

    public boolean getLockXYScale() {
        return lockXYScale;
    }

    public void setLockXYScale(boolean lockXYScale) {
        this.lockXYScale = lockXYScale;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
        fireHasChanged();
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
        fireHasChanged();
    }

    public void setScale(double scaleX, double scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        fireHasChanged();
    }

    public void composeScale(double deltaX, double deltaY) {
        this.scaleX *= deltaX;
        this.scaleY *= deltaY;
        fireHasChanged();
    }

    public void composeScaleCentered(double deltaX, double deltaY) {
        this.scaleX *= deltaX;
        this.scaleY *= deltaY;
        fireHasChanged();
    }

    public void composeScaleX(double deltaX) {
        this.scaleX *= deltaX;
        fireHasChanged();
    }

    public void composeScaleY(double deltaY) {
        this.scaleY *= deltaY;
        fireHasChanged();
    }


    /**
     * register a new change listener
     *
     * @param listener
     */
    public void addChangeListener(ITransformChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * remove a registered change listener
     *
     * @param listener
     */
    public void removeChangeListener(ITransformChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * remove all change listeners
     */
    public void removeAllChangeListeners() {
        changeListeners.clear();
    }

    /**
     * fire the change listeners
     */
    public void fireHasChanged() {
        for (ITransformChangeListener changeListener : changeListeners) {
            changeListener.hasChanged(this);
        }
    }

    /**
     * gets the dimensions of the bounding box in device coordinates
     *
     * @return preferred size
     */
    public Dimension getPreferredSize() {
        Polygon rect = w2d(coordRect);
        return new Dimension((int) Math.round(rect.getBounds().getWidth() + getLeftMargin() + getRightMargin()),
                (int) Math.round(rect.getBounds().getHeight() + getBottomMargin() + getTopMargin()));
    }

    /**
     * gets the preferred rectangle in device coordinates
     *
     * @return preferred size
     */
    public Rectangle getPreferredRect() {
        Rectangle rect = w2d(coordRect).getBounds();
        rect.setRect(rect.x - getLeftMargin(), rect.y - getTopMargin(),
                (int) rect.getBounds().getWidth() + getLeftMargin() + getRightMargin(),
                (int) rect.getBounds().getHeight() + getBottomMargin() + getTopMargin());
        return rect;
    }


    public int getBottomMargin() {
        return bottomMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    /**
     * Return a point that is contained in the area the world that is
     * currently mapped onto the device rectangle.
     *
     * @return a random point in the currently visible world
     */
    public Point2D getRandomVisibleLocation() {
        if (rand == null)
            rand = new Random();
        return new Point2D.Double
                (coordRect.getX() + rand.nextDouble() * coordRect.getWidth(),
                        coordRect.getY() + rand.nextDouble() * coordRect.getHeight());
    }

    /**
     * gets a copy of the current world rectangle
     *
     * @return world rectangle
     */
    public Rectangle2D getWorldRect() {
        return (Rectangle2D) rotatedCoordRect.clone();
    }

    /**
     * set scale so that  the rotated coordinate rectangle has the given size
     *
     * @param size
     */
    public void fitToSize(Dimension size) {
        fitToSize(rotatedCoordRect, size);
    }

    /**
     * zoom such that worldRect fits exactly into a device of the given size
     *
     * @param worldRect the worldrectangle to fit
     * @param size      the device size to fit into
     */
    public void fitToSize(Rectangle2D worldRect, Dimension size) {
        if (rotatedCoordRect.getWidth() == 0)
            scaleX = 1;
        else if (size.getWidth() - getLeftMargin() - getRightMargin() > 0)
            scaleX = (size.getWidth() - getLeftMargin() - getRightMargin()) / worldRect.getWidth();
        else
            scaleX = size.getWidth() / rotatedCoordRect.getWidth();
        if (rotatedCoordRect.getHeight() == 0)
            scaleY = 1;
        else if (size.getHeight() - getTopMargin() - getBottomMargin() > 0)
            scaleY = (size.getHeight() - getTopMargin() - getBottomMargin()) / worldRect.getHeight();
        else
            scaleY = size.getHeight() / rotatedCoordRect.getHeight();

        if (lockXYScale) {
            scaleX = scaleY = Math.min(scaleX, scaleY);
        }
        if (scaleX == 0 && scaleY == 0)
            scaleX = scaleY = 1;
        fireHasChanged();
    }


    /**
     * transforms a world polygon into a device polygon
     *
     * @param wp
     * @return device polygon
     */
    public Polygon w2d(PolygonDouble wp) {
        if (wp.npoints == 0)
            return new Polygon();

        java.util.List<Point> list = new LinkedList<>();

        double prevXw = wp.xpoints[0];
        double prevYw = wp.ypoints[0];
        Point prevPtd = w2d(prevXw, prevYw);

        list.add(prevPtd);

        for (int i = 1; i < wp.npoints; i++) {
            double currentXw = wp.xpoints[i];
            double currentYw = wp.ypoints[i];
            Point currentPtd = w2d(currentXw, currentYw);
            double dist = prevPtd.distance(currentPtd);
            int count = (int) (dist / 10);
            if (count > 0) {
                double dX = (currentXw - prevXw) / count;
                double dY = (currentYw - prevYw) / count;

                for (int j = 1; j < count; j++)
                    list.add(w2d(prevXw + j * dX, prevYw + j * dY));
            }
            list.add(currentPtd);
            prevXw = currentXw;
            prevYw = currentYw;
            prevPtd = currentPtd;

        }
        Polygon polygon = new Polygon();
        for (Point apt : list) {
            polygon.addPoint(apt.x, apt.y);
        }
        return polygon;
    }

    /**
     * gets the magnifier
     *
     * @return magnifier
     */
    public Magnifier getMagnifier() {
        return magnifier;
    }

    /**
     * adjusts angle so that it is either north, south, east or west
     */
    public void adjustAngleToNorthSouthEastWest() {
        double newAngle = getAngle();
        if (newAngle >= 0.25 * Math.PI && newAngle < 0.75 * Math.PI) // north
        {
            newAngle = 0.5 * Math.PI;
        } else if (newAngle >= 0.75 * Math.PI && newAngle < 1.25 * Math.PI) // west
        {
            newAngle = Math.PI;
        } else if (newAngle >= 1.25 * Math.PI && newAngle < 1.75 * Math.PI) // south
        {
            newAngle = 1.5 * Math.PI;
        } else // east
        {
            newAngle = 0;
        }
        if (newAngle != getAngle())
            setAngle(newAngle);
    }
}
