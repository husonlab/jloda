/**
 * NodeView.java 
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
/**
 * Node visualization
 *
 * @version $Id: NodeView.java,v 1.82 2010-05-27 14:17:33 huson Exp $
 *
 * @author Daniel Huson
 */
package jloda.graphview;

import gnu.jpdf.PDFGraphics;
import jloda.util.Basic;
import jloda.util.Geometry;
import jloda.util.ProgramProperties;
import jloda.util.parse.NexusStreamParser;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

final public class NodeView extends ViewBase implements Cloneable {
    private int height = 2;
    private int width = 2;

    private final int GAPSIZE = 2; // gap between edge of node and start of edge

    private Color borderColor = null;
    private byte shape = OVAL_NODE;
    //private byte imageLayout = NORTH;
    protected Point2D location = null;
    boolean fixedSize = true;

    public static final byte RECT_NODE = 1;
    public static final byte OVAL_NODE = 2;
    public static final byte TRIANGLE_NODE = 3;
    public static final byte DIAMOND_NODE = 4;
    public static final byte NONE_NODE = 0;

    private NodeImage image = null;
    protected Color bgColor = Color.WHITE;

    public static boolean END_EDGES_AT_BORDER_OF_NODES = true;


    public static Writer descriptionWriter = null;

    /**
     * Construct a node view.
     */
    public NodeView() {
        labelLayout = LAYOUT;
    }

    /**
     * Copy constructor.
     *
     * @param src NodeView
     */
    public NodeView(NodeView src) {
        this();
        copy(src);
    }

    /**
     * copies the values of the source node view
     *
     * @param src
     */
    public void copy(NodeView src) {
        super.copy(src);
        setLocation(src.getLocation());
        setBackgroundColor(src.getBackgroundColor());
        height = src.height;
        width = src.width;
        shape = src.shape;
        fixedSize = src.getFixedSize();
    }

    /**
     * Gets the location.
     *
     * @return location Point2D
     */
    public Point2D getLocation() {
        return location;
    }

    /**
     * Computes the connection point for an edge in device coordinates.
     *
     * @param other NodeView
     * @param trans Transform
     * @return p Point
     */
    public Point computeConnectPoint(Point2D other, Transform trans) {
        if (location == null || other == null)
            return null;

        Point apt = trans.w2d(getLocation());
        if (shape == NONE_NODE)
            return apt;

        int scaledWidth;
        int scaledHeight;
        if (fixedSize) {
            scaledWidth = width;
            scaledHeight = height;
        } else {
            scaledWidth = computeScaledWidth(trans, width);
            scaledHeight = computeScaledHeight(trans, height);
        }

        Point bpt = trans.w2d(other);

        int x = bpt.x - apt.x;
        int y = bpt.y - apt.y;

        int radius1 = scaledWidth >> 1;
        int radius2 = scaledHeight >> 1;

        Point p = new Point();

        if (shape == RECT_NODE) {
            if (y >= x && y >= -x) // top
            {
                p.x = apt.x;
                p.y = apt.y + radius2 + 2;
            } else if (y >= x && y <= -x) // left
            {
                p.x = apt.x - radius1 - 2;
                p.y = apt.y;
            } else if (y <= x && y <= -x) // bottom
            {
                p.x = apt.x;
                p.y = apt.y - radius2 - 2;
            } else if (y <= x && y >= -x) // right
            {
                p.x = apt.x + radius1 + 2;
                p.y = apt.y;
            }
        } else {
            int radius = Math.max(radius1, radius2) + GAPSIZE;
            double dist = apt.distance(bpt);
            if (dist == 0)
                p = apt;
            else {
                double factor = radius / dist;
                p = new Point((int) (apt.x + factor * (bpt.x - apt.x)),
                        (int) (apt.y + factor * (bpt.y - apt.y)));
            }
        }
        return p;
    }

    /**
     * Gets the width.
     *
     * @return width int
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height.
     *
     * @return height int
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the bounding box in device coordinates
     *
     * @param trans Transform
     * @return Rectangle
     */
    public Rectangle getBox(Transform trans) {
        if (location == null)
            return null;

        int scaledWidth;
        int scaledHeight;
        if (shape == NONE_NODE) {
            scaledWidth = scaledHeight = 2;
        } else {
            if (fixedSize) {
                scaledWidth = width;
                scaledHeight = height;
            } else {
                scaledWidth = computeScaledWidth(trans, width);
                scaledHeight = computeScaledHeight(trans, height);
            }
        }

        int w = Math.max(6, scaledWidth);
        int h = Math.max(6, scaledHeight);
        Point apt = trans.w2d(location);
        apt.x -= (w / 2);
        apt.y -= (h / 2);

        return new Rectangle(apt.x, apt.y, w, h);
    }

    /**
     * Sets the width.
     *
     * @param a int
     */
    public void setWidth(int a) {
        if (a < 0)
            a = Byte.MAX_VALUE;
        width = a;
    }

    /**
     * Sets the height.
     *
     * @param a int
     */
    public void setHeight(int a) {
        if (a < 0)
            a = Byte.MAX_VALUE;
        height = a;
    }

    /**
     * Sets the node shape.
     *
     * @param a int
     */
    public void setShape(byte a) {
        shape = a;
    }

    /**
     * Gets the node shape.
     *
     * @return the shape
     */

    public byte getShape() {
        return shape;
    }

    /**
     * Gets the border color
     *
     * @return the borger color
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the border color
     *
     * @param borderColor
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Draw the node.
     *
     * @param gc      Graphics
     * @param trans   Transform
     * @param hilited
     */
    public void draw(Graphics gc, Transform trans, boolean hilited) {
        if (hilited)
            hilite(gc, trans);
        draw(gc, trans);
    }

    /**
     * Draw the node.
     *
     * @param gc    Graphics
     * @param trans Transform
     */
    public void draw(Graphics gc, Transform trans) {
        if (location == null)
            return; // no location, don't draw
        Point apt = trans.w2d(location);

        int scaledWidth;
        int scaledHeight;
        if (fixedSize) {
            scaledWidth = width;
            scaledHeight = height;
        } else {
            scaledWidth = computeScaledWidth(trans, width);
            scaledHeight = computeScaledHeight(trans, height);
        }

        apt.x -= (scaledWidth >> 1);
        apt.y -= (scaledHeight >> 1);

        if (this.borderColor != null) {
            if (enabled)
                gc.setColor(this.borderColor);
            else
                gc.setColor(DISABLED_COLOR);
            if (shape == OVAL_NODE) {
                gc.drawOval(apt.x - 2, apt.y - 2, scaledWidth + 4, scaledHeight + 4);
                gc.drawOval(apt.x - 3, apt.y - 3, scaledWidth + 6, scaledHeight + 6);
            } else if (shape == RECT_NODE) {
// default shape==GraphView.RECT_NODE
                gc.drawRect(apt.x - 2, apt.y - 2, scaledWidth + 4, scaledHeight + 4);
                gc.drawRect(apt.x - 3, apt.y - 3, scaledWidth + 6, scaledHeight + 6);
            }
// else draw nothing
        }

        if (bgColor != null) {
            if (enabled)
                gc.setColor(bgColor);
            else
                gc.setColor(Color.WHITE);
            if (shape == OVAL_NODE)
                gc.fillOval(apt.x, apt.y, scaledWidth, scaledHeight);
            else if (shape == RECT_NODE)
                gc.fillRect(apt.x, apt.y, scaledWidth, scaledHeight);

        }
        if (fgColor != null) {
            if (enabled)
                gc.setColor(fgColor);
            else
                gc.setColor(DISABLED_COLOR);
            if (shape == OVAL_NODE)
                gc.drawOval(apt.x, apt.y, scaledWidth, scaledHeight);
            else if (shape == RECT_NODE)
                gc.drawRect(apt.x, apt.y, scaledWidth, scaledHeight);
        }
    }

    /**
     * gets the scaled width
     *
     * @param trans
     * @param width
     * @return scaled width
     */
    public static int computeScaledWidth(Transform trans, int width) {
        int scaledWidth = (int) (width / Math.max(1 / trans.getScaleX(), 1 / trans.getScaleY()));
        if (width > 0) scaledWidth = Math.max(1, scaledWidth);
        return scaledWidth;

    }

    /**
     * gets the scaled height
     *
     * @param trans
     * @param height
     * @return scaled height
     */
    public static int computeScaledHeight(Transform trans, int height) {
        int scaledHeight = (int) (height / Math.max(1 / trans.getScaleX(), 1 / trans.getScaleY()));
        if (height > 0) scaledHeight = Math.max(1, scaledHeight);
        return scaledHeight;
    }


    /**
     * Highlights the node.
     *
     * @param gc    Graphics
     * @param trans Transform
     */
    public void hilite(Graphics gc, Transform trans) {
        if (location == null)
            return;
        int scaledWidth;
        int scaledHeight;
        if (shape == NONE_NODE) {
            scaledWidth = scaledHeight = 2;
        } else {
            if (fixedSize) {
                scaledWidth = width;
                scaledHeight = height;
            } else {
                scaledWidth = computeScaledWidth(trans, width);
                scaledHeight = computeScaledHeight(trans, height);
            }
        }

        Point apt = trans.w2d(location);
        apt.x -= (scaledWidth >> 1);
        apt.y -= (scaledHeight >> 1);

        gc.setColor(ProgramProperties.SELECTION_COLOR);
        gc.drawRect(apt.x - 2, apt.y - 2, scaledWidth + 4, scaledHeight + 4);
    }


    /**
     * Highlights the node label
     *
     * @param gc          Graphics
     * @param trans       Transform
     * @param defaultFont font to use if node has no font set
     */
    public void hiliteLabel(Graphics2D gc, Transform trans, Font defaultFont) {
        if (location == null)
            return;

        if (labelColor != null && label != null && labelVisible && label.length() > 0) {
            gc.setStroke(NORMAL_STROKE);
            if (getFont() != null)
                gc.setFont(getFont());
            else
                gc.setFont(defaultFont);
            gc.setColor(ProgramProperties.SELECTION_COLOR);
            Shape shape = getLabelShape(trans);
            gc.fill(shape);
            gc.setColor(ProgramProperties.SELECTION_COLOR_DARKER);
            final Stroke oldStroke = gc.getStroke();
            gc.setStroke(NORMAL_STROKE);
            gc.draw(shape);
            gc.setStroke(oldStroke);
        }
    }

    /**
     * Draws the node's label and the image, if set
     *
     * @param gc    Graphics
     * @param trans Transform
     */
    public void drawLabel(Graphics2D gc, Transform trans, Font defaultFont) {
        drawLabel(gc, trans, defaultFont, false);
    }


    /**
     * Draws the node's label and the image, if set
     *
     * @param gc    Graphics
     * @param trans Transform
     */
    public void drawLabel(Graphics2D gc, Transform trans, Font defaultFont, boolean hilited) {
        if (location == null)
            return;

        if (labelColor != null && label != null && label.length() > 0) {
            //labelShape = null;
            //gc.setColor(Color.WHITE);
            //gc.fill(getLabelRect(trans));
            if (labelBackgroundColor != null && labelVisible && enabled) {
                gc.setColor(labelBackgroundColor);
                gc.fill(getLabelShape(trans));
            }

            if (getFont() != null)
                gc.setFont(getFont());
            else
                gc.setFont(defaultFont);

            if (hilited)
                hiliteLabel(gc, trans, defaultFont);

            if (enabled)
                gc.setColor(labelColor);
            else
                gc.setColor(DISABLED_COLOR);

            Point2D apt = getLabelPosition(trans);

            if (apt != null) {
                if (labelVisible) {
                    if (labelAngle == 0) {
                        gc.drawString(label, (int) apt.getX(), (int) apt.getY());
                    } else {
                        float labelAngle = this.labelAngle + 0.00001f; // to ensure that labels all get same orientation in

                        Dimension labelSize = getLabelSize();
                        if (gc instanceof PDFGraphics) {
                            if (labelAngle >= 0.5 * Math.PI && labelAngle <= 1.5 * Math.PI) {
                                apt = Geometry.translateByAngle(apt, labelAngle, labelSize.getWidth());
                                ((PDFGraphics) gc).drawString(label, (float) (apt.getX()), (float) (apt.getY()), (float) (labelAngle - Math.PI));
                            } else {
                                ((PDFGraphics) gc).drawString(label, (float) (apt.getX()), (float) (apt.getY()), labelAngle);
                            }
                        } else {
                            // save current transform:
                            AffineTransform saveTransform = gc.getTransform();
                            // a vertical phylogram view

                            /*
                            AffineTransform localTransform =  gc.getTransform();
                            // rotate label to desired angle
                            if (labelAngle >= 0.5 * Math.PI && labelAngle <= 1.5 * Math.PI) {
                                double d = getLabelSize().getWidth();
                                apt = Geometry.translateByAngle(apt, labelAngle, d);
                                localTransform.rotate(Geometry.moduloTwoPI(labelAngle - Math.PI), apt.getX(), apt.getY());
                            } else
                                localTransform.rotate(labelAngle, apt.getX(), apt.getY());
                           gc.setTransform(localTransform);
                            */
                            // todo: this doesn't work well as the angles aren't drawn correctly

                            // rotate label to desired angle
                            if (labelAngle >= 0.5 * Math.PI && labelAngle <= 1.5 * Math.PI) {
                                apt = Geometry.translateByAngle(apt, labelAngle, getLabelSize().getWidth());
                                gc.rotate(Geometry.moduloTwoPI(labelAngle - Math.PI), apt.getX(), apt.getY());
                            } else {
                                gc.rotate(labelAngle, apt.getX(), apt.getY());
                            }
                            gc.drawString(label, (int) apt.getX(), (int) apt.getY());
                            gc.setTransform(saveTransform);
                        }
                    }
                }
            }
        }
        // draw the image:
        if (getImage() != null && getImage().isVisible()) {
            getImage().draw(this, trans, gc, hilited);
        }

        if (descriptionWriter != null && getLabelVisible() && getLabel() != null && getLabel().length() > 0) {
            Rectangle bounds;
            if (labelAngle == 0) {
                bounds = getLabelRect(trans).getBounds();
            } else {
                bounds = getLabelShape(trans).getBounds();
            }
            try {
                descriptionWriter.write(String.format("%s; x=%d y=%d w=%d h=%d\n", getLabel(), bounds.x, bounds.y, bounds.width, bounds.height));
            } catch (IOException e) {
                // silently ignore
            }
        }
    }

    /**
     * Sets the position of the label in device coordinates.
     *
     * @param x     int
     * @param y     int
     * @param trans Transform
     */
    public void setLabelPosition(int x, int y, Transform trans) {
        Point apt = trans.w2d(location);
        if (labelLayout != USER && labelLayout != LAYOUT)
            setLabelLayout(USER);
        dxLabel = x - apt.x;
        dyLabel = y - apt.y;
    }

    /**
     * gets the relative position of the label in device coordinates
     *
     * @return relative position
     */
    public Point getLabelPositionRelative(Transform trans) {
        if (labelLayout == USER)
            return new Point(dxLabel, dyLabel);
        else {
            Point aPt = trans.w2d(getLocation());
            Point bPt = getLabelPosition(trans);
            return new Point(bPt.x - aPt.x, bPt.y - aPt.y);
        }
    }

    /**
     * Gets the label position in device coordinates
     *
     * @param trans Transform
     * @return locations
     */
    public Point getLabelPosition(Transform trans) {
        if (location == null)
            return null;

        if (label == null || labelSize == null)
            return null;

        int scaledWidth;
        int scaledHeight;

        if (shape == NONE_NODE)
            scaledWidth = scaledHeight = 2;
        else {
            if (fixedSize) {
                scaledWidth = width;
                scaledHeight = height;
            } else {
                scaledWidth = computeScaledWidth(trans, width);
                scaledHeight = computeScaledHeight(trans, height);
            }
        }
        Point apt = trans.w2d(location);
        Dimension size = getLabelSize();
        switch (labelLayout) {
            case RADIAL:
            case USER:
            case LAYOUT:
                apt.x += dxLabel;
                apt.y += dyLabel;
                break;
            case CENTRAL:
                apt.x -= size.width / 2;
                apt.y += size.height / 2;
                break;
            case NORTH:
                apt.x -= size.width / 2;
                apt.y -= (scaledHeight / 2 + 3);
                break;
            case NORTHEAST:
                apt.x += (scaledWidth / 2 + 3);
                apt.y -= (scaledHeight / 2 + 3);
                break;
            case EAST:
                apt.x += (scaledWidth / 2 + 3);
                apt.y += size.height / 2;
                break;
            case SOUTHEAST:
                apt.x -= (scaledWidth / 2 + 3);
                apt.y += (scaledHeight / 2 + 3) + size.height;
                break;
            case SOUTH:
                apt.x -= size.width / 2;
                apt.y += (scaledHeight / 2 + 3) + size.height;
                break;
            case SOUTHWEST:
                apt.x -= (scaledWidth / 2 + size.width + 3);
                apt.y += (scaledHeight / 2 + 3) + size.height;
                break;
            case WEST:
                apt.x -= (scaledWidth / 2 + size.width + 3);
                apt.y += size.height / 2;
                break;
            case NORTHWEST:
                apt.x -= (scaledWidth / 2 + size.width + 3);
                apt.y -= (scaledHeight / 2 + 3);
                break;
        }
        return apt;
    }

    /**
     * gets the bounding box of the label in device coordinates
     *
     * @param trans
     * @return bounding box
     */
    public Rectangle getLabelRect(Transform trans) {
        if (labelSize != null) {
            Point apt = getLabelPosition(trans);
            if (apt != null)
                return new Rectangle(apt.x, apt.y - labelSize.height + 1, labelSize.width, labelSize.height);
        }
        return null;
    }

    /**
     * gets the bounding box of the label in device coordinates as a shape (rectangle or polygon)
     *
     * @param trans
     * @return bounding box
     */
    public Shape getLabelShape(Transform trans) {
        if (labelSize != null) {
            Point2D apt = getLabelPosition(trans);
            if (apt != null) {
                if (labelAngle == 0) {
                    return new Rectangle((int) apt.getX(), (int) apt.getY() - labelSize.height + 1, labelSize.width, labelSize.height);
                } else {
                    double labelAngle = this.labelAngle + 0.0001; // to ensure that labels all get same orientation in

                    AffineTransform localTransform = new AffineTransform();
                    // rotate label to desired angle
                    if (labelAngle >= 0.5 * Math.PI && labelAngle <= 1.5 * Math.PI) {
                        double d = getLabelSize().getWidth();
                        apt = Geometry.translateByAngle(apt, labelAngle, d);
                        localTransform.rotate(Geometry.moduloTwoPI(labelAngle - Math.PI), apt.getX(), apt.getY());
                    } else
                        localTransform.rotate(labelAngle, apt.getX(), apt.getY());
                    double[] pts = new double[]{apt.getX(), apt.getY(),
                            apt.getX() + labelSize.width, apt.getY(),
                            apt.getX() + labelSize.width, apt.getY() - labelSize.height,
                            apt.getX(), apt.getY() - labelSize.height};
                    localTransform.transform(pts, 0, pts, 0, 4);
                    return new Polygon(new int[]{(int) pts[0], (int) pts[2], (int) pts[4], (int) pts[6]}, new int[]{(int) pts[1], (int) pts[3],
                            (int) pts[5], (int) pts[7]}, 4);
                }
            }
        }
        return null;
    }

    /**
     * Sets the location
     *
     * @param p Point2D
     */
    public void setLocation(Point2D p) {
        location = p;
    }

    /**
     * Sets the location
     *
     * @param x
     * @param y
     */
    public void setLocation(double x, double y) {
        location = new Point2D.Double(x, y);
    }

    /**
     * draw this noded at a fixed size?
     *
     * @return fixed-size mode?
     */
    public boolean getFixedSize() {
        return fixedSize;
    }

    /**
     * draw this node at a fixed size?
     *
     * @param fixedSize
     */
    public void setFixedSize(boolean fixedSize) {
        this.fixedSize = fixedSize;
    }

    /**
     * writes this node view
     *
     * @param w
     */
    public void write(Writer w) throws IOException {
        w.write(toString(null));
        w.write("\n");
    }

    /**
     * gets a string representation of this node view, including coordinates
     *
     * @return string representation
     */
    public String toString() {
        return toString(null, true, true);
    }

    /**
     * gets a string representation of this node view
     *
     * @param withCoordinates show coordinates as well?
     * @return string representation
     */
    public String toString(boolean withCoordinates) {
        return toString(null, withCoordinates, true);
    }


    /**
     * writes this node view
     *
     * @param w
     * @param previousNV if not null, only write those fields that differ from the values in previousNV
     */
    public void write(Writer w, NodeView previousNV) throws IOException {
        w.write(toString(previousNV));
        w.write("\n");
    }

    /**
     * gets a string representation of this node view
     *
     * @param previousNV if not null, only write those fields that differ from the values in previousNV
     * @return string representation
     */
    public String toString(NodeView previousNV) {
        return toString(previousNV, true, true);
    }

    /**
     * gets a string representation of this node view
     *
     * @param previousNV if not null, only write those fields that differ from the values in previousNV
     * @return string representation
     */
    public String toString(NodeView previousNV, boolean withCoordinates, boolean withLabel) {
        StringBuilder buf = new StringBuilder();
        if (previousNV == null || height != previousNV.height)
            buf.append(" nh=").append(height);
        if (previousNV == null || width != previousNV.width)
            buf.append(" nw=").append(width);
        if (fgColor != null && (previousNV == null || previousNV.fgColor == null || !fgColor.equals(previousNV.fgColor)))
            buf.append(" fg=").append(Basic.toString3Int(fgColor));
        if (bgColor != null && (previousNV == null || previousNV.bgColor == null || !bgColor.equals(previousNV.bgColor)))
            buf.append(" bg=").append(Basic.toString3Int(bgColor));
        if (borderColor != null && (previousNV == null || previousNV.borderColor == null || !borderColor.equals(previousNV.borderColor)))
            buf.append(" bd=").append(Basic.toString3Int(borderColor));
        if (previousNV == null || linewidth != previousNV.linewidth)
            buf.append(" w=").append(linewidth);
        if (previousNV == null || shape != previousNV.shape)
            buf.append(" sh=").append(shape);
        if (withCoordinates && location != null) {
            buf.append(" x=").append((float) location.getX());
            buf.append(" y=").append((float) location.getY());
        }
        if (previousNV == null || fixedSize != previousNV.fixedSize)
            buf.append(" fx=").append(fixedSize ? 1 : 0);

        if (labelColor != null && (previousNV == null || previousNV.labelColor == null || !labelColor.equals(previousNV.labelColor)))
            buf.append(" lc=").append(Basic.toString3Int(labelColor));
        if (previousNV == null || ((previousNV.labelBackgroundColor == null) != (labelBackgroundColor == null))
                ||
                (previousNV.labelBackgroundColor != null && labelBackgroundColor != null && !previousNV.labelBackgroundColor.equals(labelBackgroundColor))) {
            buf.append(" lk=").append(Basic.toString3Int(labelBackgroundColor));
        }

        if (font != null && (previousNV == null || previousNV.font == null || !font.equals(previousNV.font)))
            buf.append(" ft='").append(Basic.getCode(font)).append("'");
        if (previousNV == null || dxLabel != previousNV.dxLabel)
            buf.append(" lx=").append(dxLabel);
        if (previousNV == null || dyLabel != previousNV.dyLabel)
            buf.append(" ly=").append(dyLabel);
        if (previousNV == null || labelLayout != previousNV.labelLayout)
            buf.append(" ll=").append(labelLayout);
        if (previousNV == null || labelVisible != previousNV.labelVisible)
            buf.append(" lv=").append(labelVisible ? 1 : 0);
        if (labelAngle != 0)
            buf.append(" la=").append(labelAngle);
        if (withLabel && label != null && label.length() > 0)
            buf.append(" lb='").append(label).append("'");

        buf.append(";");
        return buf.toString();
    }

    /**
     * read node format from a string
     *
     * @param src
     * @throws IOException
     */
    public void read(String src) throws IOException {
        read(src, this);
    }

    /**
     * read node format from a string. Use prevNV for defaults
     *
     * @param src
     * @param prevNV
     * @throws IOException
     */
    public void read(String src, NodeView prevNV) throws IOException {
        NexusStreamParser np = new NexusStreamParser(new StringReader(src));
        java.util.List<String> tokens = np.getTokensRespectCase(null, ";");
        read(np, tokens, prevNV != null ? prevNV : this);
    }

    /**
     * reads a node view from a line
     *
     * @param tokens
     * @param prevNV this must be !=null, for example can be set to graphView.defaultNodeView
     */
    public void read(NexusStreamParser np, java.util.List<String> tokens, NodeView prevNV) throws IOException {
        if (prevNV == null)
            throw new IOException("prevNV=null");
        height = (byte) np.findIgnoreCase(tokens, "nh=", prevNV.height);
        width = (byte) np.findIgnoreCase(tokens, "nw=", prevNV.width);
        fgColor = np.findIgnoreCase(tokens, "fg=", prevNV.fgColor);
        bgColor = np.findIgnoreCase(tokens, "bg=", prevNV.bgColor);
        borderColor = np.findIgnoreCase(tokens, "bd=", prevNV.borderColor);
        linewidth = (byte) np.findIgnoreCase(tokens, "w=", prevNV.linewidth);
        shape = (byte) np.findIgnoreCase(tokens, "sh=", prevNV.shape);

        if ((prevNV != null && prevNV != this) || (tokens.contains("x=") && tokens.contains("y="))) {
            double x = np.findIgnoreCase(tokens, "x=", prevNV.getLocation() != null ? (float) prevNV.getLocation().getX() : 0);
            double y = np.findIgnoreCase(tokens, "y=", prevNV.getLocation() != null ? (float) prevNV.getLocation().getY() : 0);
            setLocation(new Point2D.Double(x, y));
        }

        fixedSize = (np.findIgnoreCase(tokens, "fx=", prevNV.fixedSize ? 1 : 0) != 0);

        labelColor = np.findIgnoreCase(tokens, "lc=", prevNV.labelColor);
        labelBackgroundColor = np.findIgnoreCase(tokens, "lk=", prevNV.labelBackgroundColor);

        String fontName = np.findIgnoreCase(tokens, "ft=", null, "");
        if (fontName != null && fontName.length() > 0)
            font = Font.decode(fontName);
        else if (prevNV.getFont() != null && prevNV != this)
            font = prevNV.getFont(); // will use default font
        else
            font = GraphView.defaultNodeView.getFont();

        dxLabel = (int) np.findIgnoreCase(tokens, "lx=", prevNV.dxLabel);
        dyLabel = (int) np.findIgnoreCase(tokens, "ly=", prevNV.dyLabel);
        setLabelAngle(np.findIgnoreCase(tokens, "la=", 0));
        labelLayout = (byte) np.findIgnoreCase(tokens, "ll=", prevNV.labelLayout);
        labelVisible = (np.findIgnoreCase(tokens, "lv=", prevNV.labelVisible ? 1 : 0) != 0);

        label = np.findIgnoreCase(tokens, "lb=", null, "");
        if (label != null && label.length() == 0)
            label = null;
        setLabel(label);

        if (tokens.size() > 0) {
            throw new IOException("Unexpected tokens: " + tokens);
        }
    }

    /**
     * get the image associated with this node
     *
     * @return
     */
    public NodeImage getImage() {
        return image;
    }

    /**
     * set the image associated with this node
     *
     * @param image
     */
    public void setImage(NodeImage image) {
        this.image = image;
    }

    /**
     * does node contain mouse click
     *
     * @param trans
     * @param x
     * @param y
     * @return true, if hit
     */
    public boolean contains(Transform trans, int x, int y) {
        Rectangle box = getBox(trans);
        return box != null && box.contains(x, y) || image != null && image.isVisible() && image.contains(x, y);
    }

    /**
     * does node intersect rectangle?
     *
     * @param trans
     * @param rect
     * @return
     */
    public boolean intersects(Transform trans, Rectangle rect) {
        Rectangle box = getBox(trans);

        return box != null && box.intersects(rect) || image != null && image.isVisible() && image.getRectangle().intersects(rect);
    }

    /**
     * Gets the background color.
     *
     * @return bgcol Color the background color
     */
    public Color getBackgroundColor() {
        return bgColor;
    }

    /**
     * Sets the background color.
     *
     * @param a Color
     */
    public void setBackgroundColor(Color a) {
        bgColor = a;
    }
}

// EOF
