/*
 * FlowPaneDragAndDrop.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.fx.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import jloda.fx.util.SelectionEffect;

/**
 * flow pane drag and drop
 * Daniel Huson, 5.2019
 */
public class FlowPaneDragAndDrop {
    private final static String NODE_DRAG_KEY = "node";

    /**
     * setup drag and drop
     *
     * @param flowPane
     */
    public static void setup(FlowPane flowPane) {
        final ObjectProperty<Node> draggingNode = new SimpleObjectProperty<>();
        //flowPane.setUserData(draggingNode);

        for (Node node : flowPane.getChildren()) {
            setupDrag(flowPane, node, draggingNode);
        }

        flowPane.getChildren().addListener((ListChangeListener<Node>) (e) -> {
            while (e.next()) {
                for (Node node : e.getAddedSubList()) {
                    setupDrag(flowPane, node, draggingNode);
                }
            }
        });
    }

    private static void setupDrag(FlowPane flowPane, Node node, ObjectProperty<Node> draggingNode) {
        node.setOnDragOver(event -> {
            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString() && NODE_DRAG_KEY.equals(dragboard.getString()) && draggingNode.get() != null) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });

        node.setOnDragDetected(event -> {
            Dragboard dragboard = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(NODE_DRAG_KEY);
            dragboard.setContent(clipboardContent);
            draggingNode.set(node);
            final WritableImage imageView = new WritableImage((int) Math.round(node.getLayoutBounds().getWidth()), (int) Math.round(node.getLayoutBounds().getHeight()));
            node.snapshot(null, imageView);
            dragboard.setDragView(imageView);
            event.consume();
        });

        node.setOnDragEntered(event -> {
            final int index = flowPane.getChildren().indexOf(node);

            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString() && NODE_DRAG_KEY.equals(dragboard.getString()) && draggingNode.get() != null) {
                final Node draggedNode = draggingNode.get();

                final int oldIndex = flowPane.getChildren().indexOf(draggedNode);
                if (index != oldIndex) {
                    node.setEffect(SelectionEffect.getInstance());
                    event.consume();
                }
            }
        });

        node.setOnDragExited(event -> {
            node.setEffect(null);
            event.consume();
        });

        node.setOnDragDropped(event -> {
            final int index = flowPane.getChildren().indexOf(node);

            final Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString() && NODE_DRAG_KEY.equals(dragboard.getString()) && draggingNode.get() != null) {
                final Node draggedNode = draggingNode.get();

                final int oldIndex = flowPane.getChildren().indexOf(draggedNode);

                if (index != oldIndex) {
                    flowPane.getChildren().remove(draggedNode);
                    flowPane.getChildren().add(oldIndex > index ? index : index - 1, draggedNode);
                }
                event.setDropCompleted(true);
                draggingNode.set(null);
                event.consume();
            }
        });
    }
}