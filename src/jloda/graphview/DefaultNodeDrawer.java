/**
 * DefaultNodeDrawer.java
 * Copyright (C) 2016 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.graphview;

import gnu.jpdf.PDFGraphics;
import jloda.graph.Node;
import jloda.util.Geometry;
import jloda.util.ProgramProperties;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;

/**
 * default node drawer
 * Daniel Huson, 1.2013
 */
public class DefaultNodeDrawer implements INodeDrawer {
    private GraphView graphView;
    private Transform trans;
    private Graphics2D gc;

    /**
     * constructor
     *
     * @param graphView
     */
    public DefaultNodeDrawer(GraphView graphView) {
        this.graphView = graphView;
        this.trans = graphView.trans;
    }

    /**
     * setup data
     *
     * @param graphView
     * @param gc
     */
    public void setup(GraphView graphView, Graphics2D gc) {
        this.graphView = graphView;
        this.trans = graphView.trans;
        this.gc = gc;
    }

    /**
     * draw the node
     *
     * @param selected
     */
    public void draw(Node v, boolean selected) {
        NodeView nv = graphView.getNV(v);
        if (selected)
            hilite(nv);
        draw(nv);
    }

    /**
     * draw the label of the node
     *
     * @param selected
     */
    public void drawLabel(Node v, boolean selected) {
        NodeView nv = graphView.getNV(v);
        drawLabel(nv, graphView.getFont(), selected);
    }

    /**
     * draw the node and the label
     *
     * @param hilited
     */
    public void drawNodeAndLabel(Node v, boolean hilited) {
        NodeView nv = graphView.getNV(v);
        draw(nv, hilited);
        drawLabel(nv, graphView.getFont(), hilited);
    }

    /**
     * Draw the node.
     *
     * @param hilited
     */
    private void draw(NodeView nv, boolean hilited) {
        if (hilited)
            hilite(nv);
        draw(nv);
    }

    /**
     * Draw the node.
     */
    private void draw(NodeView nv) {
        if (nv.getLocation() == null)
            return; // no location, don't draw
        Point apt = trans.w2d(nv.getLocation());

        int scaledWidth;
        int scaledHeight;
        if (nv.getFixedSize()) {
            scaledWidth = nv.getWidth();
            scaledHeight = nv.getHeight();
        } else {
            scaledWidth = NodeView.computeScaledWidth(trans, nv.getWidth());
            scaledHeight = NodeView.computeScaledHeight(trans, nv.getHeight());
        }

        apt.x -= (scaledWidth >> 1);
        apt.y -= (scaledHeight >> 1);

        if (nv.getBorderColor() != null) {   // selected
            if (nv.isEnabled())
                gc.setColor(nv.getBorderColor());
            else
                gc.setColor(NodeView.DISABLED_COLOR);
            if (nv.getShape() == NodeView.OVAL_NODE) {
                gc.drawOval(apt.x - 2, apt.y - 2, scaledWidth + 4, scaledHeight + 4);
                gc.drawOval(apt.x - 3, apt.y - 3, scaledWidth + 6, scaledHeight + 6);
            } else if (nv.getShape() == NodeView.RECT_NODE) {
                // default shape==GraphView.RECT_NODE
                gc.drawRect(apt.x - 2, apt.y - 2, scaledWidth + 4, scaledHeight + 4);
                gc.drawRect(apt.x - 3, apt.y - 3, scaledWidth + 6, scaledHeight + 6);
            } else if (nv.getShape() == NodeView.TRIANGLE_NODE) {
                Shape polygon = new Polygon(new int[]{apt.x - 2, apt.x + scaledWidth + 2, apt.x + scaledHeight / 2},
                        new int[]{apt.y + scaledHeight + 2, apt.y + scaledHeight + 2, apt.y - 2}, 3);
                gc.draw(polygon);
            } else if (nv.getShape() == NodeView.DIAMOND_NODE) {
                Shape polygon = new Polygon(new int[]{apt.x - 2, apt.x + scaledWidth / 2, apt.x + scaledWidth + 2, apt.x + scaledHeight / 2},
                        new int[]{apt.y + scaledHeight / 2, apt.y + scaledHeight + 2, apt.y + scaledHeight / 2, apt.y - 2}, 4);
                gc.draw(polygon);
            }

            // else draw nothing
        }

        if (nv.getBackgroundColor() != null) {
            if (nv.isEnabled())
                gc.setColor(nv.getBackgroundColor());
            else
                gc.setColor(Color.WHITE);
            if (nv.getShape() == NodeView.OVAL_NODE)
                gc.fillOval(apt.x, apt.y, scaledWidth, scaledHeight);
            else if (nv.getShape() == NodeView.RECT_NODE)
                gc.fillRect(apt.x, apt.y, scaledWidth, scaledHeight);
            else if (nv.getShape() == NodeView.TRIANGLE_NODE) {
                Shape polygon = new Polygon(new int[]{apt.x, apt.x + scaledWidth, apt.x + scaledHeight / 2},
                        new int[]{apt.y + scaledHeight, apt.y + scaledHeight, apt.y}, 3);
                gc.fill(polygon);
            } else if (nv.getShape() == NodeView.DIAMOND_NODE) {
                Shape polygon = new Polygon(new int[]{apt.x, apt.x + scaledWidth / 2, apt.x + scaledWidth, apt.x + scaledHeight / 2},
                        new int[]{apt.y + scaledHeight / 2, apt.y + scaledHeight, apt.y + scaledHeight / 2, apt.y}, 4);
                gc.fill(polygon);
            }
        }
        if (nv.getColor() != null) {
            if (nv.isEnabled())
                gc.setColor(nv.getColor());
            else
                gc.setColor(NodeView.DISABLED_COLOR);
            if (nv.getShape() == NodeView.OVAL_NODE)
                gc.drawOval(apt.x, apt.y, scaledWidth, scaledHeight);
            else if (nv.getShape() == NodeView.RECT_NODE)
                gc.drawRect(apt.x, apt.y, scaledWidth, scaledHeight);
            else if (nv.getShape() == NodeView.TRIANGLE_NODE) {
                Shape polygon = new Polygon(new int[]{apt.x, apt.x + scaledWidth, apt.x + scaledHeight / 2},
                        new int[]{apt.y + scaledHeight, apt.y + scaledHeight, apt.y}, 3);
                gc.draw(polygon);
            } else if (nv.getShape() == NodeView.DIAMOND_NODE) {
                Shape polygon = new Polygon(new int[]{apt.x, apt.x + scaledWidth / 2, apt.x + scaledWidth, apt.x + scaledHeight / 2},
                        new int[]{apt.y + scaledHeight / 2, apt.y + scaledHeight, apt.y + scaledHeight / 2, apt.y}, 4);
                gc.draw(polygon);
            }
        }
    }

    /**
     * Highlights the node.
     */
    private void hilite(NodeView nv) {
        if (nv.getLocation() == null)
            return;
        int scaledWidth;
        int scaledHeight;
        if (nv.getShape() == NodeView.NONE_NODE) {
            scaledWidth = scaledHeight = 2;
        } else {
            if (nv.getFixedSize()) {
                scaledWidth = nv.getWidth();
                scaledHeight = nv.getHeight();
            } else {
                scaledWidth = NodeView.computeScaledWidth(trans, nv.getWidth());
                scaledHeight = NodeView.computeScaledHeight(trans, nv.getHeight());
            }
        }

        Point apt = trans.w2d(nv.getLocation());
        apt.x -= (scaledWidth >> 1);
        apt.y -= (scaledHeight >> 1);

        Shape shape = new Rectangle(apt.x - 2, apt.y - 2, scaledWidth + 4, scaledHeight + 4);
        gc.setColor(ProgramProperties.SELECTION_COLOR);
        gc.fill(shape);
        gc.setColor(ProgramProperties.SELECTION_COLOR_DARKER);
        final Stroke oldStroke = gc.getStroke();
        gc.setStroke(NodeView.NORMAL_STROKE);
        gc.draw(shape);
        gc.setStroke(oldStroke);
    }


    /**
     * Highlights the node label
     *
     * @param defaultFont font to use if node has no font set
     */
    public void hiliteLabel(NodeView nv, Font defaultFont) {
        if (nv.getLocation() == null)
            return;

        if (nv.getLabelColor() != null && nv.getLabel() != null && nv.isLabelVisible() && nv.getLabel().length() > 0) {
            if (nv.getFont() != null)
                gc.setFont(nv.getFont());
            else if (defaultFont != null)
                gc.setFont(defaultFont);
            Shape shape = (nv.getLabelAngle() == 0 ? nv.getLabelRect(trans) : nv.getLabelShape(trans));
            gc.setColor(ProgramProperties.SELECTION_COLOR);
            gc.fill(shape);
            gc.setColor(ProgramProperties.SELECTION_COLOR_DARKER);
            final Stroke oldStroke = gc.getStroke();
            gc.setStroke(ViewBase.NORMAL_STROKE);
            gc.draw(shape);
            gc.setStroke(oldStroke);
        }
    }

    /**
     * Draws the node's label and the image, if set
     */
    private void drawLabel(NodeView nv, Font defaultFont, boolean hilited) {
        if (nv.getLocation() == null)
            return;

        if (nv.getLabelColor() != null && nv.getLabel() != null && nv.getLabel().length() > 0) {
            if (hilited)
                hiliteLabel(nv, defaultFont);
            else {
                //labelShape = null;
                //gc.setColor(Color.WHITE);
                //gc.fill(getLabelRect(trans));
                if (nv.getLabelBackgroundColor() != null && nv.isLabelVisible() && nv.isEnabled()) {
                    gc.setColor(nv.getLabelBackgroundColor());
                    gc.fill(nv.getLabelShape(trans));
                }
            }

            if (nv.getFont() != null)
                gc.setFont(nv.getFont());
            else if (defaultFont != null)
                gc.setFont(defaultFont);

            if (nv.isEnabled())
                gc.setColor(nv.getLabelColor());
            else
                gc.setColor(NodeView.DISABLED_COLOR);

            Point2D apt = nv.getLabelPosition(trans);

            if (apt != null) {
                if (nv.isLabelVisible()) {
                    if (nv.getLabelAngle() == 0) {
                        gc.drawString(nv.getLabel(), (int) apt.getX(), (int) apt.getY());
                    } else {
                        float labelAngle = nv.getLabelAngle() + 0.00001f; // to ensure that labels all get same orientation in

                        Dimension labelSize = nv.getLabelSize();
                        if (gc instanceof PDFGraphics) {
                            if (labelAngle >= 0.5 * Math.PI && labelAngle <= 1.5 * Math.PI) {
                                apt = Geometry.translateByAngle(apt, labelAngle, labelSize.getWidth());
                                ((PDFGraphics) gc).drawString(nv.getLabel(), (float) (apt.getX()), (float) (apt.getY()), (float) (labelAngle - Math.PI));
                            } else {
                                ((PDFGraphics) gc).drawString(nv.getLabel(), (float) (apt.getX()), (float) (apt.getY()), labelAngle);
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
                                apt = Geometry.translateByAngle(apt, labelAngle, nv.getLabelSize().getWidth());
                                gc.rotate(Geometry.moduloTwoPI(labelAngle - Math.PI), apt.getX(), apt.getY());
                            } else {
                                gc.rotate(labelAngle, apt.getX(), apt.getY());
                            }
                            gc.drawString(nv.getLabel(), (int) apt.getX(), (int) apt.getY());
                            gc.setTransform(saveTransform);
                        }
                    }
                }
            }
        }
        // draw the image:
        if (nv.getImage() != null && nv.getImage().isVisible()) {
            nv.getImage().draw(nv, trans, gc, hilited);
        }

        if (NodeView.descriptionWriter != null && nv.getLabelVisible() && nv.getLabel() != null
                && nv.getLabel().length() > 0) {
            Rectangle bounds;
            if (nv.getLabelAngle() == 0) {
                bounds = nv.getLabelRect(trans).getBounds();
            } else {
                bounds = nv.getLabelShape(trans).getBounds();
            }
            try {
                NodeView.descriptionWriter.write(String.format("%s; x=%d y=%d w=%d h=%d\n", nv.getLabel(),
                        bounds.x, bounds.y, bounds.width, bounds.height));
            } catch (IOException e) {
                // silently ignore
            }
        }
    }
}
