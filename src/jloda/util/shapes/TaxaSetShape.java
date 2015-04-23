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
