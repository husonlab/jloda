/*
 * TestSplittableTabPane.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jloda.fx.shapes.SquareShape;
import jloda.util.ProgramProperties;
import jloda.util.Single;

public class TestSplittableTabPane extends Application {
    @Override
    public void init() throws Exception {
        ProgramProperties.setProgramName("Test");
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(ProgramProperties.getProgramName());


        final BorderPane borderPane = new BorderPane();

        final SplittableTabPane tabPane = new SplittableTabPane();
        borderPane.setCenter(tabPane);

        final Single<Integer> counter = new Single<>(0);

        final Tab first = new Tab("First");
        {
            final TextArea textArea = new TextArea("\n" + counter + "\n" + createText());
            first.setContent(textArea);
            first.setClosable(false);
        }

        tabPane.getTabs().add(first);
        tabPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, null)));


        final Button newTab = new Button("New tab");
        newTab.setOnAction((e) -> {
            counter.set(counter.get() + 1);
            final Tab tab = new Tab("Tab-" + counter.get());
            tab.setGraphic(new SquareShape(12));
            final TextArea textArea = new TextArea("\n" + counter + "\n" + createText());
            tab.setContent(textArea);
            tabPane.getTabs().add(tab);
        });
        newTab.setDefaultButton(true);

        final Label focus = new Label("Has Focus");
        focus.disableProperty().bind(tabPane.focusedTabPaneProperty().isNull());

        final Label selected = new Label("Has Selected");
        selected.disableProperty().bind(tabPane.getSelectionModel().selectedItemProperty().isNull());

        final Button closeTab = new Button("Close Tab");
        closeTab.setOnAction((e) ->
                tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedItem()));
        closeTab.disableProperty().bind(selected.disabledProperty().or(focus.disabledProperty()));

        final Button closeAux = new Button("Close Aux");
        closeAux.setOnAction((e) -> tabPane.redockAll());

        final Button quit = new Button("Quit");
        quit.setOnAction((e) -> Platform.exit());

        final ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(newTab, focus, selected, closeTab, closeAux, quit);
        borderPane.setTop(buttonBar);

        final StackPane root = new StackPane();
        root.getChildren().add(borderPane);
        final Scene scene = new Scene(root, 800, 800);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setX(400);
        primaryStage.setY(400);
        primaryStage.show();

        tabPane.prefWidthProperty().bind(root.widthProperty());
        tabPane.prefHeightProperty().bind(root.heightProperty());
    }

    public String createText() {
        return " final StackPane root=new StackPane();\n" +
                "        root.setPrefWidth(600);\n" +
                "        root.setPrefHeight(600);\n" +
                "        root.getChildren().add(borderPane);\n" +
                "        final Scene scene=new Scene(root,600,600);\n" +
                "        primaryStage.setScene(scene);\n" +
                "        primaryStage.sizeToScene();\n" +
                "        primaryStage.setX(400);\n" +
                "        primaryStage.setY(400);\n" +
                "        primaryStage.show();\n";
    }
}
