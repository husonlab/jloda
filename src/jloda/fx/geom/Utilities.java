/*
 * Utilities.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableFloatArray;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.Window;
import jloda.fx.util.SelectionEffect;

/**
 * Some geometry related utilities
 * Daniel Huson, 9.2015
 */
public class Utilities {
    /**
     * translate all points in a mesh
     */
    public static void translatePoints(Mesh mesh, Point3D delta) {
        final ObservableFloatArray source = ((TriangleMesh) mesh).getPoints();
        final float[] target = new float[source.size()];

        for (int i = 0; i < target.length; ) {
            target[i] = source.get(i++) + (float) delta.getX();
            target[i] = source.get(i++) + (float) delta.getY();
            target[i] = source.get(i++) + (float) delta.getZ();
        }
        ((TriangleMesh) mesh).getPoints().setAll(target);
    }

    /**
     * relocate all bodies so that they lie between the given bounds
     *
	 */
    public static void relocate(Body3D[] bodies, double min, double max) {
        // scale
        {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;

            for (Body3D body : bodies) {
                minX = Math.min(minX, body.getTx());
                minY = Math.min(minY, body.getTy());
                minZ = Math.min(minZ, body.getTz());
                maxX = Math.max(maxX, body.getTx());
                maxY = Math.max(maxY, body.getTy());
                maxZ = Math.max(maxZ, body.getTz());
            }
            final double dMax = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
            if (dMax > 0) {
                double factor = (max - min) / dMax;

                for (Body3D body : bodies) {
                    body.setTx(factor * body.getTx());
                    body.setTy(factor * body.getTy());
                    body.setTz(factor * body.getTz());
                }
            }
        }

        // center:
        {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;

            for (Body3D body : bodies) {
                minX = Math.min(minX, body.getTx());
                minY = Math.min(minY, body.getTy());
                minZ = Math.min(minZ, body.getTz());
                maxX = Math.max(maxX, body.getTx());
                maxY = Math.max(maxY, body.getTy());
                maxZ = Math.max(maxZ, body.getTz());
            }

            final double dX = 0.5 * (maxX + minX);
            final double dY = 0.5 * (maxY + minY);
            final double dZ = 0.5 * (maxZ + minZ);

            for (Body3D body : bodies) {
                body.setTx(body.getTx() - dX);
                body.setTy(body.getTy() - dY);
                body.setTz(body.getTz() - dZ);
            }
        }
    }

    /**
     * create a label that is bound to user determined transformations
     */
    public static Label createLabelWithBinding(String text, Node node, Color color, boolean visible, final ReadOnlyProperty... properties) {
        final Label label = new Label(text);
        label.setMouseTransparent(true);
        label.setTextFill(color);
        label.setVisible(visible);

        final ObjectBinding<Rectangle> binding = createBoundingBoxBinding(node, properties);

        label.translateXProperty().bind(new DoubleBinding() {
            {
                bind(binding);
            }

            @Override
            protected double computeValue() {
                return binding.get().getX() + binding.get().getWidth() + 1;
            }
        });
        label.translateYProperty().bind(new DoubleBinding() {
            {
                bind(binding);
            }

            @Override
            protected double computeValue() {
                return binding.get().getY() + (binding.get().getHeight() - label.getHeight()) / 2 + 1;
            }
        });

        return label;
    }


    /**
     * create a bounding box that is bound to user determined transformations
     */
    public static Rectangle createBoundingBoxWithBinding(Node node, boolean visible, final ReadOnlyProperty... properties) {
        final Rectangle boundingBox = new Rectangle();
		boundingBox.setStroke(SelectionEffect.getInstance().getColor());
		boundingBox.setFill(Color.TRANSPARENT);
        boundingBox.setMouseTransparent(true);
        boundingBox.setVisible(visible);

        final ObjectBinding<Rectangle> binding = createBoundingBoxBinding(node, properties);

        boundingBox.xProperty().bind(new DoubleBinding() {
            {
                bind(binding);
            }

            @Override
            protected double computeValue() {
                return binding.get().getX();
            }
        });
        boundingBox.yProperty().bind(new DoubleBinding() {
            {
                bind(binding);
            }

            @Override
            protected double computeValue() {
                return binding.get().getY();
            }
        });
        boundingBox.widthProperty().bind(new DoubleBinding() {
            {
                bind(binding);
            }

            protected double computeValue() {
                return binding.get().getWidth();
            }
        });
        boundingBox.heightProperty().bind(new DoubleBinding() {
            {
                bind(binding);
            }

            @Override
            protected double computeValue() {
                return binding.get().getHeight();
            }
        });

        return boundingBox;
    }

    /**
     * creates bounding box binding
     *
     * @return binding
     */
    private static ObjectBinding<Rectangle> createBoundingBoxBinding(final Node node, final ReadOnlyProperty... properties) {
        return new ObjectBinding<>() {
            {
                bind(properties);
                if (node instanceof TransformableGroup) {
                    bind(((TransformableGroup) node).s.xProperty());
                }
            }

            @Override
            protected Rectangle computeValue() {
                try {
                    final Window window = node.getScene().getWindow();
                    final Bounds boundsInLocal = node.getBoundsInLocal();
                    final Bounds boundsOnScreen = node.localToScreen(boundsInLocal);
                    return new Rectangle(
                            boundsOnScreen.getMinX() - window.getX() - node.getScene().getX(),
                            boundsOnScreen.getMinY() - window.getY() - node.getScene().getY(),
                            boundsOnScreen.getWidth(),
                            boundsOnScreen.getHeight());
                } catch (NullPointerException e) {
                    return new Rectangle(0, 0, 0, 0);
                }
            }
        };
    }
}
