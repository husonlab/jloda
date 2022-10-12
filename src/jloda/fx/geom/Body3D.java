/*
 * Body3D.java Copyright (C) 2022 Daniel H. Huson
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
package jloda.fx.geom;

import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape3D;
import javafx.stage.Window;
import jloda.fx.util.SelectionEffect;
import jloda.util.NodeShape;

/**
 * A shape that has a text
 * Daniel Huson, 9.2015
 */
public class Body3D extends TransformableGroup {
    private static final Color labelColor = Color.BLACK;

    private final String name;
    private String text;
    private Shape3D shape;

    private Tooltip tooltip;
    private javafx.scene.shape.Rectangle selectionRectangle;
    private Label label;

    private Object userData;

    /**
     * constructor
     *
	 */
    public Body3D(String name, String text, Shape3D shape, Color color, Object userData) {
        this.name = name;
        this.text = text;
        this.shape = shape;
        getChildren().add(shape);
        this.userData = userData;


        final PhongMaterial material = new PhongMaterial();
        if (color == null)
            color = Color.WHITE;
        material.setDiffuseColor(color.darker());
        material.setSpecularColor(color.brighter());

        shape.setMaterial(material);
        shape.setUserData(this);
    }

    /**
     * make body for datapoint
     *
     * @return body
     */
    public static Body3D makeBody(String name, String text, Point3D vector, Color color, NodeShape nodeShape, Object userData) {
        final Shape3D shape = ShapeFactory.makeShape(2, nodeShape);
        final Body3D body3D = new Body3D(name, text, shape, color, userData);
        body3D.setCoordinates((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
        return body3D;
    }

    private void setCoordinates(float x, float y, float z) {
        setTx(x);
        setTy(y);
        setTz(z);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        getLabel().setText(text);
        getTooltip().setText(text);
    }

    public javafx.scene.shape.Rectangle getSelectionRectangle() {
        return selectionRectangle;
    }

    public void update(boolean isSelected, boolean isShowLabel, ReadOnlyProperty... properties) {
        selectionRectangle = Utilities.createBoundingBoxWithBinding(this, isSelected, properties);
        selectionRectangle.setStroke(Color.GOLD);
        selectionRectangle.setEffect(SelectionEffect.getInstance());
        label = Utilities.createLabelWithBinding(text, this, labelColor, isShowLabel, properties);
    }

    public Label getLabel() {
        return label;
    }

    private Tooltip getTooltip() {
        if (tooltip == null) {
            tooltip = new Tooltip(text);
            tooltip.setAutoHide(true);
            Tooltip.install(shape, tooltip);
        }
        return tooltip;
    }

    public void setShowSelected(boolean show) {
        selectionRectangle.setVisible(show);
        label.setEffect(show ? SelectionEffect.getInstance() : null);
    }

    public void setShowLabel(boolean show) {
        label.setVisible(show);
    }

    /**
     * gets the window rectangle for this object
     *
     * @return screen rectangle
     */
    public Rectangle getBoundsInWindow() {
        if (shape == null || shape.getScene() == null)
            return new Rectangle();

        final Window window = shape.getScene().getWindow();
        final Bounds bounds = shape.getBoundsInLocal();
        final Bounds screenBounds = shape.localToScreen(bounds);
        return new Rectangle((int) Math.round(screenBounds.getMinX() - window.getX()), (int) Math.round(screenBounds.getMinY() - window.getY()),
                (int) Math.round(screenBounds.getWidth()), (int) Math.round(screenBounds.getHeight()));
    }

    public void changeShape(Shape3D shape, boolean keepMaterial) {
        getChildren().remove(this.shape);
        if (keepMaterial) {
            Material material = this.shape.getMaterial();
            shape.setMaterial(material);
        }
        getChildren().add(shape);
        this.shape = shape;
    }

    public void changeColor(Color color) {
        if (shape.getMaterial() instanceof PhongMaterial) {
            PhongMaterial material = (PhongMaterial) shape.getMaterial();
            material.setDiffuseColor(color.darker());
            material.setSpecularColor(color.brighter());
            shape.setMaterial(material);
        }
    }

    public Color getColor() {
        return ((PhongMaterial) shape.getMaterial()).getDiffuseColor();
    }

    public Point3D getCenter() {
        Bounds bounds = shape.getBoundsInLocal();
        return new Point3D(0.5 * (bounds.getMaxX() + bounds.getMinX()), 0.5 * (bounds.getMaxY() - bounds.getMinY()), 0.5 * (bounds.getMaxZ() - bounds.getMinZ()));
    }

    public String getName() {
        return name;
    }

    public Shape3D getShape() {
        return shape;
    }

    @Override
    public Object getUserData() {
        return userData;
    }

    @Override
    public void setUserData(Object userData) {
        this.userData = userData;
    }
}
