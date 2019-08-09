/*
 *  Test.java Copyright (C) 2019 Daniel H. Huson
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

package jloda.progs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jloda.fx.window.NotificationManager;
import jloda.util.ProgramProperties;

import java.util.Random;

public class TestNotificationManager extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        ProgramProperties.setUseGUI(true);
        ProgramProperties.setProgramName("TEST");

        final Button click = new Button("Click me");

        click.setOnAction((e) -> {
            switch ((new Random()).nextInt(3)) {
                case 0:
                    NotificationManager.showError(stage, "Good morning! THis is a lot of stuff.\nI loaded one tree\nI did!");
                    break;
                case 1:
                    NotificationManager.showInformation(stage, "Good morning! THis is a lot of stuff. I loaded one tree");
                    break;
                case 2:
                    NotificationManager.showWarning(stage, "Good morning! THis is a lot of stuff. I loaded one tree");
                    break;
            }
        });

        stage.setScene(new Scene(new BorderPane(click)));
        stage.setX(100);
        stage.setY(100);
        stage.setWidth(600);
        stage.setHeight(600);
        stage.show();
    }
}
