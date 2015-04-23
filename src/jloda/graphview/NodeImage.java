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

package jloda.graphview;

import jloda.util.Geometry;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

/**
 * image associated with a node
 * Daniel Huson, 7.2007
 */
public class NodeImage {
    final ImageObserver observer;
    Image image;
    Image scaledImage;
    int width = -1;
    int height = 50;
    boolean visible = true;
    byte layout = NodeView.RADIAL;
    final Rectangle boundingBox = new Rectangle();

    /**
     * constructor
     *
     * @param observer
     */
    public NodeImage(ImageObserver observer) {
        this.observer = observer;
    }

    /**
     * construct from file
     *
     * @param file
     * @param observer
     * @throws IOException
     */
    public NodeImage(File file, ImageObserver observer) throws IOException {
        this(observer);
        read(file);
    }

    /**
     * read image from a file
     *
     * @param file
     * @throws IOException
     */
    public void read(File file) throws IOException {
        setImage(ImageIO.read(file));
    }

    /**
     * draw the image
     *
     * @param nv
     * @param trans
     * @param gc
     * @param hilite
     */
    public void draw(NodeView nv, Transform trans, Graphics2D gc, boolean hilite) {
        // draw the image:
        Image scaledImage = getScaledImage();
        if (observer != null && scaledImage != null) {
            Shape shape = nv.getLabelShape(trans);
            if (shape != null) {
                Rectangle rect = shape.getBounds();
                if (!nv.isLabelVisible()) {
                    Point location = nv.getLabelPosition(trans);
                    rect = new Rectangle(location.x, location.y, 0, 0);
                }
                int x;
                int y;
                byte useLayout = layout;
                if (layout == ViewBase.RADIAL) {
                    double useAngle = Geometry.moduloTwoPI(nv.getLabelAngle());

                    double eightsOfPi = 8 * useAngle / Math.PI;
                    if (eightsOfPi < 1)
                        useLayout = ViewBase.EAST;
                    else if (eightsOfPi < 3)
                        useLayout = ViewBase.SOUTHEAST;
                    else if (eightsOfPi < 5)
                        useLayout = ViewBase.SOUTH;
                    else if (eightsOfPi < 7)
                        useLayout = ViewBase.SOUTHWEST;
                    else if (eightsOfPi < 9)
                        useLayout = ViewBase.WEST;
                    else if (eightsOfPi < 11)
                        useLayout = ViewBase.NORTHWEST;
                    else if (eightsOfPi < 13)
                        useLayout = ViewBase.NORTH;
                    else if (eightsOfPi < 15)
                        useLayout = ViewBase.NORTHEAST;
                    else
                        useLayout = ViewBase.EAST;
                }
                switch (useLayout) {
                    case ViewBase.NORTHWEST:
                        x = (int) (rect.getX() - scaledImage.getWidth(observer) - 5);
                        y = (int) (rect.getY() - scaledImage.getHeight(observer) - 3);
                        break;
                    case ViewBase.NORTHEAST:
                        x = (int) (rect.getX() + rect.getWidth() + 5);
                        y = (int) (rect.getY() - scaledImage.getHeight(observer) - 3);
                        break;
                    case ViewBase.NORTH:
                        x = (int) (rect.getX() + 0.5 * (rect.getWidth() - scaledImage.getWidth(observer)));
                        y = (int) (rect.getY() - scaledImage.getHeight(observer) - 3);
                        break;
                    case ViewBase.SOUTHWEST:
                        x = (int) (rect.getX() - scaledImage.getWidth(observer) - 5);
                        y = (int) (rect.getY() + rect.getHeight() + 3);
                        break;
                    case ViewBase.SOUTHEAST:
                        x = (int) (rect.getX() + rect.getWidth() + 5);
                        y = (int) (rect.getY() + rect.getHeight() + 3);
                        break;

                    case ViewBase.SOUTH:
                        x = (int) (rect.getX() + 0.5 * (rect.getWidth() - scaledImage.getWidth(observer)));
                        y = (int) (rect.getY() + rect.getHeight() + 3);
                        break;
                    case ViewBase.WEST:
                        x = (int) (rect.getX() - scaledImage.getWidth(observer) - 5);
                        y = (int) (rect.getY() + 0.5 * (rect.getHeight() - scaledImage.getHeight(observer)));
                        break;
                    default:
                    case ViewBase.EAST:
                        x = (int) (rect.getX() + rect.getWidth() + 5);
                        y = (int) (rect.getY() + 0.5 * (rect.getHeight() - scaledImage.getHeight(observer)));
                        break;

                }
                gc.drawImage(scaledImage, x, y, observer);
                boundingBox.setRect(x, y, scaledImage.getWidth(observer), scaledImage.getHeight(observer));
                if (hilite) {
                    gc.setColor(Color.RED);
                    gc.draw(boundingBox);
                }
            }
        }
    }

    /**
     * does point hit this?
     *
     * @param x
     * @param y
     * @return true, if hit
     *         todo: this is broken
     */
    public boolean contains(int x, int y) {
        //System.err.println("x y: "+x+" "+y);
        //System.err.println("rect: "+ boundingBox);
        return boundingBox.contains(x, y);
    }

    /**
     * gets the image
     *
     * @return image
     */
    public Image getImage() {
        return image;
    }

    /**
     * set the image and the scaled image
     *
     * @param image
     */
    public void setImage(Image image) {
        this.image = image;
        if (image != null)
            scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        else
            scaledImage = null;
    }

    /**
     * get the scaled image
     *
     * @return scaled image
     */
    public Image getScaledImage() {
        if (scaledImage == null && image != null)
            setImage(image);
        return scaledImage;
    }

    /**
     * get the width or -1
     *
     * @return width or -1
     */
    public int getWidth() {
        return width;
    }

    /**
     * set the width. use -1 for no constraint
     *
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
        setImage(image);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        setImage(image);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public byte getLayout() {
        return layout;
    }

    public void setLayout(byte layout) {
        this.layout = layout;
    }

    public Rectangle getRectangle() {
        return boundingBox;
    }
}
