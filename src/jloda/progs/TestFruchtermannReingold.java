/*
 * TestNotificationManager.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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

package jloda.progs;

import javafx.application.Application;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import jloda.graph.*;
import jloda.graph.algorithms.FruchtermanReingoldLayoutNew;
import jloda.util.APoint2D;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgramProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

public class TestFruchtermannReingold extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        ProgramProperties.setUseGUI(true);
        ProgramProperties.setProgramName("TEST");

        Graph graph = setupGraph();
        EdgeFloatArray weights;
        weights = graph.newEdgeFloatArray();
        for (var e : graph.edges()) {
            var id1 = e.getSource().getId();
            var id2 = e.getTarget().getId();
            if (id1 == 1 || id2 == 1)
                weights.set(e, 1000);
            else if (id1 >= 4 && id1 <= 9 && id2 >= 4 && id2 <= 9)
                weights.set(e, 1);
            else
                weights.set(e, 100);
        }


        NodeArray<APoint2D<?>> points = graph.newNodeArray();

        var mainPane = new StackPane();

        CheckBox weightsCBox = new CheckBox("Weights");
        TextField iterationsTF = new TextField("1000");
        TextField gravityTF = new TextField("1");
        TextField speedTF = new TextField("1");
        TextField areaFT = new TextField("600");
        Button apply = new Button("Apply");

        ToolBar toolBar = new ToolBar(weightsCBox,
                new Label("Iters:"), iterationsTF,
                new Label("Gravity:"), gravityTF,
                new Label("Speed:"), speedTF,
                new Label("Area:"), areaFT,
                apply);


        Runnable updater = () -> {
            try {
                runAlgorithm(graph, weights, points, weightsCBox.isSelected(), Basic.parseInt(iterationsTF.getText()),
                        Basic.parseDouble(gravityTF.getText()), Basic.parseDouble(speedTF.getText()), Basic.parseFloat(areaFT.getText()));
            } catch (IOException ex) {
                Basic.caught(ex);
            }
            fitToBox(points, new BoundingBox(0, 0, 600, 600));
            var all = computeGraphView(graph, points, weights);
            mainPane.getChildren().setAll(all);
        };

        apply.setOnAction(e -> updater.run());

        updater.run();


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolBar);
        borderPane.setCenter(mainPane);
        stage.setScene(new Scene(borderPane));
        stage.setX(100);
        stage.setY(100);
        stage.setWidth(600);
        stage.setHeight(600);
        stage.show();
    }

    public void runAlgorithm(Graph graph, EdgeFloatArray weights, NodeArray<APoint2D<?>> points, boolean useWeights, int iterations, double gravity, double speed, float area) throws CanceledException {
        var algorithm = new FruchtermanReingoldLayoutNew(graph);
        if (useWeights)
            algorithm.setWeights(weights);
        algorithm.setOptionGravity(gravity);
        algorithm.setOptionArea(area);
        algorithm.setOptionSpeed(speed);
        algorithm.apply(iterations, points, null);
    }

    public Group computeGraphView(Graph graph, NodeArray<APoint2D<?>> points, EdgeFloatArray weights) {
        NodeArray<Circle> node2circle = graph.newNodeArray();

        var nodesGroup = new Group();
        var nodeLabelsGroup = new Group();

        for (var v : graph.nodes()) {
            var circle = new Circle(2);
            circle.setCenterX(points.get(v).getX());
            circle.setCenterY(points.get(v).getY());
            nodesGroup.getChildren().add(circle);
            node2circle.put(v, circle);

            if (v.getLabel() == null)
                v.setLabel("V" + v.getId());

            if (v.getLabel() != null) {
                Label label = new Label(v.getLabel());
                label.layoutXProperty().bind(circle.centerXProperty().add(5));
                label.layoutYProperty().bind(circle.centerYProperty().add(-5));
                nodeLabelsGroup.getChildren().add(label);
            }
        }


        var edgesGroup = new Group();
        var edgeLabelsGroup = new Group();

        for (var e : graph.edges()) {
            var line = new Line();
            var start = node2circle.get(e.getSource());
            var end = node2circle.get(e.getTarget());

            line.startXProperty().bind(start.centerXProperty());
            line.startYProperty().bind(start.centerYProperty());

            line.endXProperty().bind(end.centerXProperty());
            line.endYProperty().bind(end.centerYProperty());
            edgesGroup.getChildren().add(line);

            if (weights != null && weights.get(e) != null) {
                Label label = new Label("e" + e.getId() + ": " + weights.get(e));
                label.layoutXProperty().bind((line.startXProperty().add(line.endXProperty())).multiply(0.5));
                label.layoutYProperty().bind((line.startYProperty().add(line.endYProperty())).multiply(0.5));
                edgeLabelsGroup.getChildren().add(label);

            }
        }

        var all = new Group();
        all.getChildren().addAll(edgesGroup);
        all.getChildren().addAll(nodesGroup);
        all.getChildren().addAll(edgeLabelsGroup);
        all.getChildren().addAll(nodeLabelsGroup);
        return all;
    }

    private Graph setupGraph() {
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();

        return TestGraph.createGraph(nodes, edges);
    }

    public static void fitToBox(NodeArray<APoint2D<?>> points, Bounds tar) {
        var src = computeBBox(points);

        Function<Double, Double> mapX = x -> (x - src.getMinX()) / (src.getWidth()) * tar.getWidth() + tar.getMinX();
        Function<Double, Double> mapY = y -> (y - src.getMinY()) / (src.getHeight()) * tar.getHeight() + tar.getMinY();

        for (var v : points.keys()) {
            var point = points.get(v);
            points.put(v, new APoint2D<>(mapX.apply(point.getX()), mapY.apply(point.getY()), point.getUserData()));
        }
    }

    /**
     * computes the bounding box of all locations
     *
     * @param node2location
     * @return bounding box
     */
    private static Bounds computeBBox(NodeArray<APoint2D<?>> node2location) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (var point : node2location.values()) {
            minX = Math.min(minX, point.getX());
            maxX = Math.max(maxX, point.getX());
            minY = Math.min(minY, point.getY());
            maxY = Math.max(maxY, point.getY());
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }


}
