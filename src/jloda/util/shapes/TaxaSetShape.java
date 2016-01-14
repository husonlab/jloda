/**
 * TaxaSetShape.java 
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
package jloda.util.shapes;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by IntelliJ IDEA.
 * User: kloepper
 * Date: 18.02.2007
 * Time: 21:12:56
 * To change this template use File | Settings | File Templates.
 */
public class TaxaSetShape implements Shape {

    public boolean contains(double x, double y) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean contains(double x, double y, double w, double h) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean intersects(double x, double y, double w, double h) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Rectangle getBounds() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean contains(Point2D p) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Rectangle2D getBounds2D() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean contains(Rectangle2D r) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean intersects(Rectangle2D r) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
