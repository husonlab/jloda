/*
 * RubberBandSelection.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.selection.rubberband;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jloda.fx.util.SelectionEffect;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shows a rubber band and calls a handler
 * Daniel Huson, 1.2018
 */
public class RubberBandSelection {
    private static ExecutorService service;

    @FunctionalInterface
    public interface Handler {
        /**
         * handle a rubber band selection
         *
         * @param rectangle       in scene coordinates
         * @param extendSelection true if shift key down
         * @param service         use this service for computations outside of the FX thread
         */
        void handle(Rectangle2D rectangle, boolean extendSelection, ExecutorService service);
    }

    private final Rectangle rectangle;
    private Point2D start;
    private Point2D end;
    private final Handler handler;

    private boolean stillDownWithoutMoving;
    private boolean inWait;
    private final BooleanProperty inRubberBand = new SimpleBooleanProperty();
    private final BooleanProperty inDrag = new SimpleBooleanProperty();

    /**
     * constructor
     *
     * @param pane       node on which mouse can be clicked and dragged to show rubber band
     * @param scrollPane if non-null, will implement panning
     * @param group      group into which rubber band should be temporarily added so that it appears in the scene
     * @param handler    this is called when rubber band is released
     */
    public RubberBandSelection(final Pane pane, final ScrollPane scrollPane, final Group group, final Handler handler) {
        if (service == null)
            service = Executors.newSingleThreadExecutor();

        this.handler = handler;
        rectangle = new Rectangle();

        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(SelectionEffect.getInstance().getColor());

        inRubberBand.addListener((c, o, n) -> {
            pane.setCursor(n ? Cursor.CROSSHAIR : Cursor.DEFAULT);
            if (n)
                group.getChildren().add(rectangle);
            else
                group.getChildren().remove(rectangle);
        });

        pane.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            stillDownWithoutMoving = true;
            inRubberBand.set(false);

            start = group.screenToLocal(e.getScreenX(), e.getScreenY());
            if (start != null) {
                end = null;
                rectangle.setX(start.getX());
                rectangle.setY(start.getY());
                rectangle.setWidth(0);
                rectangle.setHeight(0);

                if (e.isShiftDown()) {
                    inRubberBand.set(true);
                } else if (!inWait) {
                    service.execute(() -> {
                        try {
                            inWait = true;
                            synchronized (this) {
                                Thread.sleep(500);
                            }
                        } catch (InterruptedException ignored) {
                        }
                        if (stillDownWithoutMoving) {
                            Platform.runLater(() -> inRubberBand.set(true));
                        }
                        inWait = false;
                    });
                }
                if (pane.getCursor() == Cursor.CROSSHAIR)
                    e.consume();
            }
        });

        pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, (e) -> {
            stillDownWithoutMoving = false;
            if (inRubberBand.get()) {
                if (start != null) {
                    end = group.screenToLocal(e.getScreenX(), e.getScreenY());
                    rectangle.setX(Math.min(start.getX(), end.getX()));
                    rectangle.setY(Math.min(start.getY(), end.getY()));
                    rectangle.setWidth(Math.abs(end.getX() - start.getX()));
                    rectangle.setHeight(Math.abs(end.getY() - start.getY()));
                    if (pane.getCursor() == Cursor.CROSSHAIR)
                        e.consume();
                }
            } else if (scrollPane != null && start != null) {
                inDrag.set(true);
                double deltaX = e.getScreenX() - start.getX();
                double deltaY = e.getScreenY() - start.getY();
                // todo: determine the correct amount to scroll by
                if (deltaX > 5)
                    scrollPane.setHvalue(scrollPane.getHvalue() - 0.01 * scrollPane.getHmax());
                else if (deltaX < -5)
                    scrollPane.setHvalue(scrollPane.getHvalue() + 0.01 * scrollPane.getHmax());
                if (deltaY > 5)
                    scrollPane.setVvalue(scrollPane.getVvalue() - 0.01 * scrollPane.getVmax());
                else if (deltaY < -5)
                    scrollPane.setVvalue(scrollPane.getVvalue() + 0.01 * scrollPane.getVmax());
                if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5)
                    start = new Point2D(e.getScreenX(), e.getScreenY());
            }
        });

        pane.addEventHandler(MouseEvent.MOUSE_RELEASED, (e) -> {
            stillDownWithoutMoving = false;
            if (inRubberBand.get()) {
                if (start != null) {
                    start = null;
                    if (pane.getCursor() == Cursor.CROSSHAIR)
                        e.consume();
                    if (this.handler != null && rectangle.getWidth() > 0 && rectangle.getHeight() > 0) {
                        Point2D min = group.localToScene(rectangle.getX(), rectangle.getY());
                        Point2D max = group.localToScene(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight());
                        this.handler.handle(new Rectangle2D(min.getX(), min.getY(), max.getX() - min.getX(), max.getY() - min.getY()), e.isShiftDown(), service);
                    }
                }
                inRubberBand.set(false);
            }
            inDrag.set(false);
        });
    }

    public BooleanProperty inRubberBandProperty() {
        return inRubberBand;
    }
}
