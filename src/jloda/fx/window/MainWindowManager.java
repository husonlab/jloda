/*
 * MainWindowManager.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.fx.window;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.stage.Stage;
import jloda.fx.util.ClosingLastDocument;
import jloda.util.IteratorUtils;
import jloda.util.ProgramProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * manages all open main windows
 * Daniel Huson, 1.2018
 */
public class MainWindowManager {
    private static int windowsCreated = 0;

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

        mainWindows.addListener((InvalidationListener) c -> {
            for (int i = 0; i < mainWindows.size(); i++) {
                for (int j = i + 1; j < mainWindows.size(); j++)
                    if (mainWindows.get(i) == mainWindows.get(j))
                        System.err.println("Duplicate: " + mainWindows.get(i));
            }
        });
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
        fireChanged();
    }

    /**
     * returns true if window was closed
     *
     * @param mainWindow
     * @return true, if closed
     */
    public boolean closeMainWindow(IMainWindow mainWindow) {
        if (MainWindowManager.getInstance().size() == 1) {
            if (!ClosingLastDocument.apply(mainWindow.getStage())) {
                if (!mainWindow.isEmpty()) {
                    createAndShowWindow(false);

                    mainWindow.getStage().close();
                    MainWindowManager.getInstance().closeAndRemoveAuxiliaryWindows(mainWindow);
                    mainWindows.remove(mainWindow);
                    fireChanged();

                }
                return false;
            }
        }
        ProgramProperties.put("WindowGeometry", (new WindowGeometry(mainWindow.getStage())).toString());
        // mainWindow.getStage().close();

        mainWindow.close();
        mainWindows.remove(mainWindow);
        closeAndRemoveAuxiliaryWindows(mainWindow);
        fireChanged();

        if (mainWindows.size() == 0) {
            ProgramProperties.store();
            Platform.exit();
            System.exit(0);
        }

        if (lastFocusedMainWindow == mainWindow) {
            lastFocusedMainWindow = mainWindows.get(mainWindows.size() - 1);
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
                    windowGeometry.setFromString(ProgramProperties.get("WindowGeometry", "50 50 800 800"));
                }
                final IMainWindow newWindow = getMainWindow(0).createNew();
                final Stage stage = new Stage();
                stage.setTitle("Untitled - " + ProgramProperties.getProgramName() + " [" + (++windowsCreated) + "]");
                stage.focusedProperty().addListener((c, o, n) -> {
                    if (n)
                        setLastFocusedMainWindow(newWindow);
                });

                newWindow.show(stage, windowGeometry.getX(), windowGeometry.getY(), windowGeometry.getWidth(), windowGeometry.getHeight());
                if (!mainWindows.contains(newWindow))
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
        fireChanged();
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
            fireChanged();
        }
    }

    public ReadOnlyLongProperty changedProperty() {
        return changed;
    }

    public void fireChanged() {
        changed.set(changed.get() + 1);
    }

    public ObservableList<IMainWindow> getMainWindows() {
        return mainWindows;
    }

    public ArrayList<Stage> getAuxiliaryWindows(IMainWindow mainWindow) {
        return mainWindows2AdditionalWindows.get(mainWindow);
    }

    public IMainWindow getLastFocusedMainWindow() {
        if (lastFocusedMainWindow != null)
            return lastFocusedMainWindow;
        else if (mainWindows.size() > 0)
            return mainWindows.get(0);
        else
            return null;
    }

    public void setLastFocusedMainWindow(IMainWindow lastFocusedMainWindow) {
        this.lastFocusedMainWindow = lastFocusedMainWindow;
    }

    public IntegerBinding sizeProperty() {
        return Bindings.size(mainWindows);
    }

    public static ChangeListener<Boolean> getUseDarkThemeListener(IMainWindow mainWindow) {
        return (v, o, n) -> {
            ProgramProperties.put("UseDarkTheme", n);
            for (var win : IteratorUtils.asSet(Collections.singletonList(mainWindow), MainWindowManager.getInstance().getMainWindows())) {
                win.getStage().getScene().getStylesheets().remove("jloda/resources/css/mondena_dark.css");
                if (n)
                    win.getStage().getScene().getStylesheets().add("jloda/resources/css/mondena_dark.css");
                for (var aux : MainWindowManager.getInstance().getAuxiliaryWindows(win)) {
                    aux.getScene().getStylesheets().remove("jloda/resources/css/mondena_dark.css");
                    if (n)
                        aux.getScene().getStylesheets().add("jloda/resources/css/mondena_dark.css");
                }
            }
        };
    }

    public static boolean isUseDarkTheme() {
        return ProgramProperties.get("UseDarkTheme", false);
    }
}
