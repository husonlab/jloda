/*
 * FileOpenManager.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.util.RecentFilesManager;

import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * file open manager
 * Daniel Huson, 5.2019
 */
public class FileOpenManager {
    private static final ObjectProperty<Consumer<String>> fileOpener = new SimpleObjectProperty<>();
    private static final ObjectProperty<Collection<FileChooser.ExtensionFilter>> extensions = new SimpleObjectProperty<>();

    public static EventHandler<ActionEvent> createOpenFileEventHandler(Stage stage) {
        return event ->
        {
            File previousDir = new File(ProgramProperties.get("OpenFileDir", ""));

            final FileChooser fileChooser = new FileChooser();
            if (previousDir.isDirectory())
                fileChooser.setInitialDirectory(previousDir);
            if (ProgramProperties.getProgramVersion() != null)
                fileChooser.setTitle("Open File - " + ProgramProperties.getProgramVersion());
            else
                fileChooser.setTitle("Open File");

            if (getExtensions() != null)
                fileChooser.getExtensionFilters().addAll(getExtensions());
            final File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null && getFileOpener() != null) {
                ProgramProperties.put("OpenFileDir", selectedFile.getParent());
                getFileOpener().accept(selectedFile.getPath());
                RecentFilesManager.getInstance().insertRecentFile(selectedFile.getPath());
            }
        };
    }

    public static Consumer<String> getFileOpener() {
        return fileOpener.get();
    }

    public static ObjectProperty<Consumer<String>> fileOpenerProperty() {
        return fileOpener;
    }

    public static void setFileOpener(Consumer<String> fileOpener) {
        FileOpenManager.fileOpener.set(fileOpener);
    }

    public static Collection<FileChooser.ExtensionFilter> getExtensions() {
        return extensions.get();
    }

    public static ObjectProperty<Collection<FileChooser.ExtensionFilter>> extensionsProperty() {
        return extensions;
    }

    public static void setExtensions(Collection<FileChooser.ExtensionFilter> extensions) {
        FileOpenManager.extensions.set(extensions);
    }
}
