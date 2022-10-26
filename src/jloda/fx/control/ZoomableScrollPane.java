/*
 * ZoomableScrollPane.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * zoomable scroll pane that zooms to point under mouse
 * Adapted from: https://stackoverflow.com/questions/39827911/javafx-8-scaling-zooming-scrollpane-relative-to-mouse-position
 * Daniel Huson, 1.2018
 */
public class ZoomableScrollPane extends ScrollPane {
    private double mouseScrollZoomFactor = 1.01; // 1%

    private final BooleanProperty lockAspectRatio = new SimpleBooleanProperty(false);
    private final BooleanProperty allowZoom = new SimpleBooleanProperty(true);

    private final BooleanProperty requireShiftOrControlToZoom = new SimpleBooleanProperty(false);

    private Node content;
    private final Group zoomNode;
    private final StackPane outerNode;

    private double zoomX = 1;
    private double zoomY = 1;
    private double zoomFactorX = 1;
    private double zoomFactorY = 1;

    private final ObjectProperty<Runnable> updateScaleMethod;

    /**
     * constructor
     *
     */
    public ZoomableScrollPane(Node content) {
        super();
        this.content = content;
        zoomNode = new Group();
        if (content != null)
            zoomNode.getChildren().add(content);
        outerNode = createOuterNode();
        outerNode.getChildren().add(zoomNode);
        setContent(outerNode);

        updateScaleMethod = new SimpleObjectProperty<>(() -> {
            ZoomableScrollPane.this.content.setScaleX(zoomX);
            ZoomableScrollPane.this.content.setScaleY(zoomY);
        });

        // if setContent() is used to update content, then adjust accordingly:
        contentProperty().addListener((c, o, n) -> {
            if (n != outerNode) {
                ZoomableScrollPane.this.content = n;
                zoomNode.getChildren().clear();
                if (n != null)
                    zoomNode.getChildren().add(n);
                setContent(outerNode); // scroll pane scrolls outer node
            }
        });
    }

     /**
     * this returns the node that is zoomable. This method should be used in place of ScrollPane.getContent()
     *
     * @return zoomable content
     */
    public Node getContentNode() {
        return content;
    }

    public Pane getOuterNode() {
        return outerNode;
    }

    public double getZoomFactorX() {
        return zoomFactorX;
    }

    public double getZoomFactorY() {
        return zoomFactorY;
    }

    public double getZoomX() {
        return zoomX;
    }

    public double getZoomY() {
        return zoomY;
    }

    private StackPane createOuterNode() {
        final StackPane outerNode = new StackPane();
        outerNode.setOnScroll(e -> {
            if (ZoomableScrollPane.this.isAllowZoom() && (!isRequireShiftOrControlToZoom() || e.isShiftDown() || e.isControlDown())) {
                e.consume();
                final double factorX;
                final double factorY;

                if ((Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY()))) {
                    factorX = (e.getDeltaX() > 0 ? mouseScrollZoomFactor : 1 / mouseScrollZoomFactor);
                    factorY = 1;
                } else {
                    factorX = 1;
                    factorY = (e.getDeltaY() > 0 ? mouseScrollZoomFactor : 1 / mouseScrollZoomFactor);
                }
                ZoomableScrollPane.this.doZoom(factorX, factorY, new Point2D(e.getX(), e.getY()));
            }
        });
        return outerNode;
    }

    public void updateScale() {
        updateScaleMethod.get().run();
    }

    public Runnable getUpdateScaleMethod() {
        return updateScaleMethod.get();
    }

    public ObjectProperty<Runnable> updateScaleMethodProperty() {
        return updateScaleMethod;
    }

    public void setUpdateScaleMethod(Runnable updateScaleMethod) {
        this.updateScaleMethod.set(updateScaleMethod);
    }

    private void doZoom(double factorX, double factorY, Point2D mousePoint) {
        if (lockAspectRatio.get()) {
            if (factorX != 1.0)
                //noinspection SuspiciousNameCombination
                factorY = factorX;
            else
                //noinspection SuspiciousNameCombination
                factorX = factorY;
        }

        zoomFactorX = factorX;
        zoomFactorY = factorY;

        zoomX *= zoomFactorX;
        zoomY *= zoomFactorY;

        final Bounds innerBounds = zoomNode.getLayoutBounds();
        final Bounds viewportBounds = getViewportBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        updateScale();

        this.layout(); // refresh ScrollPane scroll positions & target bounds


        // convert target coordinates to zoomTarget coordinates
        Point2D posInZoomTarget = content.parentToLocal(zoomNode.parentToLocal(mousePoint));

        posInZoomTarget = new Point2D(posInZoomTarget.getX() * (zoomFactorX - 1), posInZoomTarget.getY() * (zoomFactorY - 1));

        // calculate adjustment of scroll position (pixels)
        final Point2D adjustment = content.getLocalToParentTransform().deltaTransform(posInZoomTarget);

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        final Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
        setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
    }

    public boolean isLockAspectRatio() {
        return lockAspectRatio.get();
    }

    public BooleanProperty lockAspectRatioProperty() {
        return lockAspectRatio;
    }

    public void setLockAspectRatio(boolean lockAspectRatio) {
        this.lockAspectRatio.set(lockAspectRatio);
    }

    public boolean isAllowZoom() {
        return allowZoom.get();
    }

    public BooleanProperty allowZoomProperty() {
        return allowZoom;
    }

    public void setAllowZoom(boolean allowZoom) {
        this.allowZoom.set(allowZoom);
    }

    public void zoomBy(double zoomFactorX, double zoomFactorY) {
        if (isAllowZoom()) {
            doZoom(zoomFactorX, zoomFactorY, new Point2D(0.5 * getWidth(), 0.5 * getHeight())); // zoom to center
            updateScale();
        }
    }

    public void resetZoom() {
        zoomFactorX = 1 / zoomX;
        zoomFactorY = 1 / zoomY;
        zoomX = 1;
        zoomY = 1;
        updateScale();
    }

    public Group getContentGroup() {
        return zoomNode;
    }

    /**
     * ensure the node is showing
     *
     */
    public void ensureVisible(Node node) {
        if (node != null && getContent().getScene() != null) {
            final Bounds viewportBounds = getViewportBounds();
            final Bounds contentBounds = getContent().localToScene(getContent().getBoundsInLocal());
            Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());

            // this adjusts for the fact that the scrollpane might not fill out the whole scene:
            final double offsetH = (getContent().getScene().getWidth() - viewportBounds.getWidth());
            final double offsetV = (getContent().getScene().getHeight() - viewportBounds.getHeight());
            nodeBounds = new BoundingBox(nodeBounds.getMinX() - offsetH, nodeBounds.getMinY() - offsetV, nodeBounds.getWidth(), nodeBounds.getHeight());

            if (nodeBounds.getMaxX() < 0) {
                final double hValueDelta = (nodeBounds.getMinX() - viewportBounds.getWidth()) / contentBounds.getWidth();
                setHvalue(getHvalue() + hValueDelta);
            } else if (nodeBounds.getMinX() > viewportBounds.getWidth()) {
                final double hValueDelta = (nodeBounds.getMinX() + viewportBounds.getWidth()) / contentBounds.getWidth();
                setHvalue(getHvalue() + hValueDelta);
            }

            if (nodeBounds.getMaxY() < 0) {
                final double vValueDelta = (nodeBounds.getMinY() - viewportBounds.getHeight()) / contentBounds.getHeight();
                setVvalue(getVvalue() + vValueDelta);
            } else if (nodeBounds.getMinY() > viewportBounds.getHeight()) {
                final double vValueDelta = (nodeBounds.getMinY() + viewportBounds.getHeight()) / contentBounds.getHeight();
                setVvalue(getVvalue() + vValueDelta);
            }
        }
    }

    public boolean isRequireShiftOrControlToZoom() {
        return requireShiftOrControlToZoom.get();
    }

    public BooleanProperty requireShiftOrControlToZoomProperty() {
        return requireShiftOrControlToZoom;
    }

    public void setRequireShiftOrControlToZoom(boolean requireShiftOrControlToZoom) {
        this.requireShiftOrControlToZoom.set(requireShiftOrControlToZoom);
    }

    public double getMouseScrollZoomFactor() {
        return mouseScrollZoomFactor;
    }

    public void setMouseScrollZoomFactor(double mouseScrollZoomFactor) {
        this.mouseScrollZoomFactor = mouseScrollZoomFactor;
    }

    public ScrollBar getHorizontalScrollBar() {
        for (var node : lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) {
                var scrollBar = (ScrollBar) node;
                if (scrollBar.getOrientation() == Orientation.HORIZONTAL)
                    return scrollBar;
            }
        }
        return null;
    }

    public ScrollBar getVerticalScrollBar() {
        for (var node : lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) {
                var scrollBar = (ScrollBar) node;
                if (scrollBar.getOrientation() == Orientation.VERTICAL)
                    return scrollBar;
            }
        }
        return null;
    }
}