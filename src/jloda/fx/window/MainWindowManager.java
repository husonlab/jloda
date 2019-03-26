/*
 *  Copyright (C) 2019. Daniel H. Huson
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

/*
 *  Copyright (C) 2018. Daniel H. Huson
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
package jloda.fx.window;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.stage.Stage;
import jloda.fx.util.ClosingLastDocument;
import jloda.fx.util.ProgramPropertiesFX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * manages all open main windows
 * Daniel Huson, 1.2018
 */
public class MainWindowManager {
    private final ObservableList<IMainWindow> mainWindows;
    private final Map<IMainWindow, ArrayList<Stage>> mainWindows2AdditionalWindows;
    private IMainWindow lastFocusedMainWindow;

    private final LongProperty changed = new SimpleLongProperty(0);

    private static final ObservableSet<String> previousSelection = FXCollections.observableSet();

    private static MainWindowManager instance;

    /**
     * constructor
     */
    private MainWindowManager() {
        mainWindows = FXCollections.observableArrayList();
        mainWindows2AdditionalWindows = new HashMap<>();
    }

    /**
     * get the instance
     *
     * @return instance
     */
    public static MainWindowManager getInstance() {
        if (instance == null)
            instance = new MainWindowManager();
        return instance;
    }

    public int size() {
        return mainWindows.size();
    }

    /**
     * get the previous selection list
     *
     * @return previous selection
     */
    public static ObservableSet<String> getPreviousSelection() {
        return previousSelection;
    }

    public void addMainWindow(IMainWindow mainWindow) {
        mainWindows.add(mainWindow);
        mainWindows2AdditionalWindows.put(mainWindow, new ArrayList<>());
        changed.set(changed.get() + 1);
    }

    /**
     * returns true if window was closed
     *
     * @param mainWindow
     * @return true, if closed
     */
    public boolean closeMainWindow(IMainWindow mainWindow) {
        if (mainWindows.size() == 1) {
            if (MainWindowManager.getInstance().size() == 1) {
                if (!ClosingLastDocument.apply(mainWindow.getStage())) {
                    if (!mainWindow.isEmpty()) {
                        final IMainWindow newWindow = mainWindow.createNew();
                        final WindowGeometry windowGeometry = new WindowGeometry(mainWindow.getStage());
                        newWindow.show(null, windowGeometry.getX(), windowGeometry.getY(), windowGeometry.getWidth(), windowGeometry.getHeight());
                    }
                    return false;
                }
            }
        }
        ProgramPropertiesFX.put("WindowGeometry", (new WindowGeometry(mainWindow.getStage())).toString());
        mainWindow.getStage().close();

        mainWindows.remove(mainWindow);
        closeAndRemoveAuxiliaryWindows(mainWindow);
        changed.set(changed.get() + 1);


        if (mainWindows.size() == 0) {
            Platform.exit();
        }

        if (lastFocusedMainWindow == mainWindow) {
            if (mainWindows.size() > 0)
                lastFocusedMainWindow = mainWindows.get(mainWindows.size() - 1);
            else
                lastFocusedMainWindow = null;
        }
        return true;
    }

    public IMainWindow createAndShowWindow(boolean useExistingEmpty) {
        final IMainWindow mainWindow = getLastFocusedMainWindow();
        if (useExistingEmpty && mainWindow.isEmpty())
            return mainWindow;
        else {
            try {
                final WindowGeometry windowGeometry = new WindowGeometry();

                if (mainWindow != null) {
                    windowGeometry.setFromStage(mainWindow.getStage());
                    windowGeometry.setX(windowGeometry.getX() + 50);
                    windowGeometry.setY(windowGeometry.getY() + 50);
                } else {
                    windowGeometry.setFromString(ProgramPropertiesFX.get("WindowGeometry", "50 50 800 800"));
                }
                final IMainWindow newWindow = getMainWindow(0).createNew();
                newWindow.show(null, windowGeometry.getX(), windowGeometry.getY(), windowGeometry.getWidth(), windowGeometry.getHeight());
                addMainWindow(newWindow);
                return newWindow;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public IMainWindow getMainWindow(int index) {
        return mainWindows.get(index);
    }

    public void addAuxiliaryWindow(IMainWindow mainWindow, Stage stage) {
        mainWindows2AdditionalWindows.get(mainWindow).add(stage);
        changed.set(changed.get() + 1);
    }

    public void closeAndRemoveAuxiliaryWindows(IMainWindow mainWindow) {
        if (mainWindows2AdditionalWindows.containsKey(mainWindow)) {
            for (Stage stage : mainWindows2AdditionalWindows.get(mainWindow))
                stage.close();
            mainWindows2AdditionalWindows.remove(mainWindow);
        }
    }

    public void removeAuxiliaryWindow(IMainWindow mainWindow, Stage stage) {
        if (mainWindows2AdditionalWindows.containsKey(mainWindow)) {
            mainWindows2AdditionalWindows.get(mainWindow).remove(stage);
            changed.set(changed.get() + 1);
        }
    }

    public ReadOnlyLongProperty changedProperty() {
        return changed;
    }

    public ObservableList<IMainWindow> getMainWindows() {
        return mainWindows;
    }

    public ArrayList<Stage> getAuxiliaryWindows(IMainWindow mainWindow) {
        return mainWindows2AdditionalWindows.get(mainWindow);
    }

    public IMainWindow getLastFocusedMainWindow() {
        return lastFocusedMainWindow;
    }

    public void setLastFocusedMainWindow(IMainWindow lastFocusedMainWindow) {
        this.lastFocusedMainWindow = lastFocusedMainWindow;
    }

    public IntegerBinding sizeProperty() {
        return Bindings.size(mainWindows);
    }
}
