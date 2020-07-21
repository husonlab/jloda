/*
 * LabeledArrow.java Copyright (C) 2020. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *  No usage, copying or distribution without explicit permission.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package jloda.fx.geom;

import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Line;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import jloda.fx.util.GeometryUtilsFX;
import jloda.swing.util.Geometry;



/**
 * 3D labeled arrow
 * Created by huson 1/2016
 */
public class LabeledArrow extends Group {
    private static final Point3D xAxis = new Point3D(1, 0, 0);
    private static final Point3D yAxis = new Point3D(0, 1, 0);
    private static final Point3D zAxis = new Point3D(0, 0, 1);

    private final Label label;

    /**
     * constructor
     */
    public LabeledArrow(Point3D start, Point3D finish, String text, Color color, ReadOnlyProperty... properties) {
        start = new Point3D(start.getX(), -start.getY(), start.getZ());
        finish = new Point3D(finish.getX(), -finish.getY(), finish.getZ());
        final Point3D diff = finish.subtract(start);
        double length = diff.magnitude();
        final Group line = makeXArrow(length, color);
        final Node labelAnchor = makeInvisibleBox(1.1 * length);
        line.getChildren().add(labelAnchor);

        line.getTransforms().add(new Translate(start.getX(), start.getY(), start.getZ()));

        Transform transform;
        Point3D point1;
        // transform diff to x-axis:
        if (diff.getY() != 0 || diff.getZ() != 0) {
            // compute z-angle:
            double angle1 =GeometryUtilsFX.computeAngle(new Point2D(diff.getZ(), diff.getY()));
            transform = new Rotate(angle1, xAxis); // rotate into
            point1 = transform.transform(diff);
        } else {
            transform = null;
            point1 = diff;
        }
        if (point1.getX() != 0 || point1.getZ() != 0) {
            double angle2 = GeometryUtilsFX.computeAngle(new Point2D(point1.getX(), point1.getZ()));
            final Rotate rotate2 = new Rotate(angle2, transform == null ? yAxis : transform.transform(yAxis));
            transform = (transform == null ? rotate2 : transform.createConcatenation(rotate2));
        }

        Transform inverse = null;
        if (transform != null) {
            try {
                inverse = transform.createInverse();
            } catch (NonInvertibleTransformException e) {
                e.printStackTrace();
            }
        }
        if (inverse != null) {
            line.getTransforms().add(inverse);
        }

        getChildren().add(line);

        label = Utilities.createLabelWithBinding(text, labelAnchor, color, true, properties);
    }

    /**
     * make an arrow along the x-axis
     *
     * @param length
     * @param color
     * @return arrow along x-axis
     */
    private Group makeXArrow(double length, Color color) {
        final Line line1 = new Line(0, 0, length, 0);
        line1.setStroke(color);
        line1.getTransforms().add(new Rotate(90, xAxis));
        final Line line2 = new Line(0, 0, length, 0);
        line2.setStroke(color);
        final Line head1 = new Line(length - 2, -2, length, 0);
        head1.setStroke(color);
        final Line head2 = new Line(length - 2, 2, length, 0);
        head2.setStroke(color);
        final Line head3 = new Line(length - 2, -2, length, 0);
        head3.setStroke(color);
        head3.getTransforms().add(new Rotate(90, xAxis));
        final Line head4 = new Line(length - 2, 2, length, 0);
        head4.setStroke(color);
        head4.getTransforms().add(new Rotate(90, xAxis));

        return new Group(line1, line2, head1, head2, head3, head4);
    }

    private Node makeInvisibleBox(double x) {
        final Box box = new Box(1, 1, 1);
        box.setVisible(false);
        box.getTransforms().add(new Translate(x, 0, 0));
        return box;
    }

    /**
     * label to place in 2D overlay
     *
     * @return x
     */
    public Label getLabel() {
        return label;
    }

    public boolean isShow() {
        return isVisible();
    }

    public void setShow(boolean show) {
        setVisible(show);
        label.setVisible(show);
    }
}
