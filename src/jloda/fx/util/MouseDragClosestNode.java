/*
 * MouseDragClosestNode.java Copyright (C) 2020. Daniel H. Huson
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
 *
 */

package jloda.fx.util;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * maintains a draggable nod
 * Daniel Huson, 1.2020
 */
public class MouseDragClosestNode {
    private double mouseDownX=0;
    private double mouseDownY=0;

    private double mouseX = 0;
    private double mouseY = 0;
    private Node target;

    public static void setup (Node node, Node reference1,Node target1, Node reference2,Node target2, BiConsumer<Node,Point2D> totalTranslation) {
         new MouseDragClosestNode(node,reference1,target1,reference2,target2,totalTranslation);
        }

    /**
     * constructor
     */
    private MouseDragClosestNode(Node node, Node reference1,Node target1, Node reference2,Node target2, BiConsumer<Node,Point2D> totalTranslation2) {

        node.setOnMousePressed((e -> {
            mouseDownX=mouseX = e.getSceneX();
            mouseDownY=mouseY = e.getSceneY();
            e.consume();

            final double distance1=reference1.localToScene(reference1.getTranslateX(),reference1.getTranslateY()).distance(mouseX,mouseY);
            final double distance2=reference2.localToScene(reference2.getTranslateX(),reference2.getTranslateY()).distance(mouseX,mouseY);

            if(distance1<=distance2)
                target=target1;
           else
               target=target2;
        }));

        node.setOnMouseDragged(e -> {
            target.setTranslateX(target.getTranslateX()+(e.getSceneX() - mouseX));
            target.setTranslateY(target.getTranslateY()+(e.getSceneY() - mouseY));
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
            e.consume();
        });

        node.setOnMouseReleased(e-> totalTranslation2.accept(target,new Point2D(e.getSceneX()-mouseDownX,e.getSceneY()-mouseDownY)));
    }
}
