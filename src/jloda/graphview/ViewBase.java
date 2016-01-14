/**
 * ViewBase.java 
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

import java.awt.*;

/**
 * class of stuff common both to a NodeView and an EdgeView
 * Daniel Huson, 2.2006
 */
public abstract class ViewBase {
    public static final Stroke NORMAL_STROKE = new BasicStroke(1);
    public static final Stroke HEAVY_STROKE = new BasicStroke(2);

    protected Color labelColor = Color.black;
    protected Color labelBackgroundColor = null;
    protected Font font = null;
    protected int dxLabel = 0; //6
    protected int dyLabel = 0;    // 5
    protected float labelAngle = 0;
    protected byte labelLayout = WEST;
    protected String label;
    protected boolean labelVisible = true;
    protected boolean enabled = true;
    protected Dimension labelSize = null;
    /**
     * Label positions
     */
    public static final byte NORTH = 1;
    public static final byte NORTHWEST = 2;
    public static final byte WEST = 3;
    public static final byte SOUTHWEST = 4;
    public static final byte SOUTH = 5;
    public static final byte SOUTHEAST = 6;
    public static final byte EAST = 7;
    public static final byte NORTHEAST = 8;
    public static final byte USER = 9;
    public static final byte LAYOUT = 10;
    public static final byte CENTRAL = 11;
    public static final byte RADIAL = 12;
    public static final byte MAXLAYOUT = RADIAL;

    public static final Color DISABLED_COLOR = Color.GRAY;
    protected byte linewidth = 1;
    protected Color fgColor = Color.black;

    /**
     * Gets the label.
     *
     * @return label String
     */
    public String getLabel() {
        return label;
    }

    /**
     * copy
     *
     * @param src
     */
    public void copy(ViewBase src) {
        setLabelColor(src.getLabelColor());
        setLabelBackgroundColor(src.getLabelBackgroundColor());
        setFont(src.getFont());
        setColor(src.getColor());
        this.dxLabel = src.dxLabel;
        this.dyLabel = src.dyLabel;
        this.labelAngle = src.labelAngle;
        labelLayout = src.labelLayout;
        this.label = src.label;
        this.labelVisible = src.labelVisible;
        this.enabled = src.enabled;
        this.labelSize = src.labelSize;
        linewidth = src.linewidth;
    }


    /**
     * Gets the label color.
     *
     * @return labelcol Color
     */
    public Color getLabelColor() {
        return labelColor;
    }

    /**
     * Gets the label background color.
     *
     * @return labelcol Color
     */
    public Color getLabelBackgroundColor() {
        return labelBackgroundColor;
    }

    /**
     * set the size of the label rect in device coordinates
     *
     * @param size
     */
    public void setLabelSize(Dimension size) {
        labelSize = size;
    }

    /**
     * set the size of the label rect in device coordinates
     *
     * @param gc graphics context
     */
    public void setLabelSize(Graphics gc) {
        setLabelSize(Basic.getStringSize(gc, getLabel(), font).getSize());
    }

    /**
     * gets the set label size
     *
     * @return label size
     */
    public Dimension getLabelSize() {
        return labelSize;
    }

    /**
     * gets the font
     *
     * @return font used for drawing label, or null, if default is to be used
     */
    public Font getFont() {
        return font;
    }

    /**
     * sets the font
     *
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Sets the label.
     *
     * @param a String
     */
    public void setLabel(String a) {
        label = a;
        labelSize = null;
    }

    /**
     * gets the label layout
     *
     * @return the label position such as NORTH, etc
     */
    public byte getLabelLayout() {
        return labelLayout;
    }

    /**
     * sets the label layout, such as NORTH etc
     *
     * @param labelLayout
     */
    public void setLabelLayout(byte labelLayout) {
        this.labelLayout = labelLayout;
    }

    /**
     * sets the label layout to NORTH etc, approximating the given angle
     *
     * @param radian
     */
    public void setLabelLayoutFromAngle(double radian) {
        final double PI_8 = Math.PI / 8.0;
        radian = Geometry.moduloTwoPI(radian);
        if (radian < PI_8)
            setLabelLayout(EAST);
        else if (radian < 3 * PI_8)
            setLabelLayout(SOUTHEAST);
        else if (radian < 5 * PI_8)
            setLabelLayout(SOUTH);
        else if (radian < 7 * PI_8)
            setLabelLayout(SOUTHWEST);
        else if (radian < 9 * PI_8)
            setLabelLayout(WEST);
        else if (radian < 11 * PI_8)
            setLabelLayout(NORTHWEST);
        else if (radian < 13 * PI_8)
            setLabelLayout(NORTH);
        else if (radian < 15 * PI_8)
            setLabelLayout(NORTHEAST);
        else
            setLabelLayout(EAST);
    }

    /**
     * Sets the label color.
     *
     * @param a Color
     */
    public void setLabelColor(Color a) {
        labelColor = a;
    }

    /**
     * Sets the label color.
     *
     * @param a Color
     */
    public void setLabelBackgroundColor(Color a) {
        labelBackgroundColor = a;
    }

    /**
     * Is the label visible ?
     *
     * @return is the label visible?
     */
    public boolean isLabelVisible() {
        return labelVisible;
    }

    /**
     * Set label visibility
     *
     * @param labelVisible
     */
    public void setLabelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
    }

    /**
     * is label visible?
     *
     * @return true, if  visible
     */

    public boolean getLabelVisible() {
        return labelVisible;
    }

    /**
     * Sets the relative position of the label in device coordinates.
     *
     * @param x int
     * @param y int
     */
    public void setLabelPositionRelative(int x, int y) {
        if (labelLayout != USER && labelLayout != LAYOUT)
            labelLayout = USER;
        dxLabel = x;
        dyLabel = y;
    }

    /**
     * Sets the relative position of the label in device coordinates.
     *
     * @param apt
     */
    public void setLabelPositionRelative(Point apt) {
        if (labelLayout != USER && labelLayout != LAYOUT)
            labelLayout = USER;
        dxLabel = apt.x;
        dyLabel = apt.y;
    }

    /**
     * gets the offset used in USER_POS
     *
     * @return offset in device coordinates
     */
    public Point getLabelOffset() {
        return new Point(dxLabel, dyLabel);
    }

    /**
     * sets the offset used by USER_POS layout, in device coordinates
     *
     * @param offset
     */
    public void setLabelOffset(Point offset) {
        dxLabel = offset.x;
        dyLabel = offset.y;
    }

    /**
     * gets the angle at which the label will be drawn
     *
     * @return angle
     */
    public float getLabelAngle() {
        return labelAngle;
    }

    /**
     * sets the angle at which label will be drawn
     *
     * @param labelAngle
     */
    public void setLabelAngle(float labelAngle) {
        this.labelAngle = (float) Geometry.moduloTwoPI(labelAngle);
    }

    /**
     * get the label rectangle
     *
     * @param trans
     * @return rectangle
     */
    abstract public Rectangle getLabelRect(Transform trans);

    /**
     * gets the label shape
     *
     * @param trans
     * @return shape of label
     */
    abstract public Shape getLabelShape(Transform trans);

    /**
     * is this node or edge enabled? If not, it will be drawn in grey
     *
     * @return true, if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * enable or disable this node or edge
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the line width.
     *
     * @return linewidth int
     */
    public int getLineWidth() {
        return linewidth;
    }

    /**
     * Sets the line width.
     *
     * @param a int
     */
    public void setLineWidth(byte a) {
        if (a < 0)
            a = Byte.MAX_VALUE;
        linewidth = a;
    }

    /**
     * Gets the foreground color.
     *
     * @return fgcol Color the foreground color
     */
    public Color getColor() {
        return fgColor;
    }

    /**
     * Sets the foreground color.
     *
     * @param a Color
     */
    public void setColor(Color a) {
        fgColor = a;
    }

}
