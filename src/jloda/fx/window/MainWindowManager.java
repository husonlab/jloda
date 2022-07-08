/*
 * MainWindowManager.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.window;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.stage.Stage;
import jloda.fx.util.ClosingLastDocument;
import jloda.util.IteratorUtils;
import jloda.util.ProgramProperties;
import jloda.util.Single;
import jloda.util.StringUtils;

import java.util.ArrayList;
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

    private static BooleanProperty useDarkTheme = null;
    private static MainWindowManager instance;

    private static WindowGeometry defaultGeometry;

    private static Single<Point2D> previousLocation = new Single<>(null);


    /**
     * constructor
     */
    private MainWindowManager() {
        if (false) {
            previousSelection.addListener((InvalidationListener) e -> {
                System.err.println("previousSelection: " + StringUtils.toString(previousSelection, " "));
            });
        }

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
        MainWindowManager.ensureDarkTheme(mainWindow);
        mainWindows.add(mainWindow);
        mainWindows2AdditionalWindows.put(mainWindow, new ArrayList<>());
        fireChanged();
    }

    /**
     * returns true if window was closed
     *
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
        return createAndShowWindow(useExistingEmpty ? getLastFocusedMainWindow() : null);
    }

    /**
     * create and show window
     *
     * @param existingWindow an existing window or null
     * @return the new window, or the existing window, if it is non-null and empty
     */
    public IMainWindow createAndShowWindow(IMainWindow existingWindow) {
        if (existingWindow != null && existingWindow.isEmpty()) {
            return existingWindow;
        } else {
            final IMainWindow newWindow = getMainWindow(0).createNew();
            final Stage stage = new Stage();
            stage.setTitle("Untitled - " + ProgramProperties.getProgramName() + " [" + (++windowsCreated) + "]");
            stage.focusedProperty().addListener((c, o, n) -> {
                if (n)
                    setLastFocusedMainWindow(newWindow);
            });

            WindowGeometry.setToStage(stage);

            previousLocation.setIfCurrentValueIsNull(new Point2D(stage.getX(), stage.getY()));
            previousLocation.set(new Point2D(previousLocation.get().getX() + 20, previousLocation.get().getY() + 20));
            stage.setX(previousLocation.get().getX());
            stage.setY(previousLocation.get().getY());

            WindowGeometry.listenToStage(stage);

            newWindow.show(stage, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());

            if (!mainWindows.contains(newWindow))
                addMainWindow(newWindow);

            return newWindow;
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
        return mainWindows2AdditionalWindows.computeIfAbsent(mainWindow, k -> new ArrayList<>());
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

    public static boolean isUseDarkTheme() {
        return useDarkThemeProperty().get();
    }

    public static BooleanProperty useDarkThemeProperty() {
        if (useDarkTheme == null) {
            useDarkTheme = new SimpleBooleanProperty(false);
            useDarkTheme.addListener((v, o, n) -> {
                ProgramProperties.put("UseDarkTheme", n);
                for (var win : IteratorUtils.asSet(MainWindowManager.getInstance().getMainWindows())) {
                    ensureDarkTheme(win);
                }
            });
            useDarkTheme.set(ProgramProperties.get("UseDarkTheme", false));
        }
        return useDarkTheme;
    }

    public static void ensureDarkTheme(IMainWindow window) {
        if (window != null) {
            window.getStage().getScene().getStylesheets().remove("jloda/resources/css/dark.css");
            if (isUseDarkTheme())
                window.getStage().getScene().getStylesheets().add("jloda/resources/css/dark.css");
            for (var aux : MainWindowManager.getInstance().getAuxiliaryWindows(window)) {
                aux.getScene().getStylesheets().remove("jloda/resources/css/dark.css");
                if (isUseDarkTheme())
                    aux.getScene().getStylesheets().add("jloda/resources/css/dark.css");
            }
        }
    }

    public static void setUseDarkTheme(boolean useDarkTheme) {
        useDarkThemeProperty().set(useDarkTheme);
    }

    public static WindowGeometry getDefaultGeometry() {
        if (defaultGeometry == null)
            defaultGeometry = WindowGeometry.loadFromProperties();
        return defaultGeometry;
    }
}
