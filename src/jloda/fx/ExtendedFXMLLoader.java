/*
 *  Copyright (C) 2019 Daniel H. Huson and David J. Bryant
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

package jloda.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * extended FXLoader
 * Daniel Huson, 12/2016
 */
public class ExtendedFXMLLoader<C> {
    private final FXMLLoader fxmlLoader;
    private final Parent root;
    private final C controller;

    /**
     * load the FXML from the fxml file associated with a class
     * For example, if the path for clazz is splitstree5.gui.TaxaFilterView or splitstree5.gui.TaxaFilterViewController, parses the file splitstree5.gui.TaxaFilterView.fxml
     *
     * @param clazz
     * @throws IOException
     */
    public ExtendedFXMLLoader(Class clazz) {
        try {
            fxmlLoader = new FXMLLoader();
            String path = clazz.getCanonicalName().replaceAll("Controller$", "").replaceAll("\\.", "/") + ".fxml";
            final URL url = clazz.getClassLoader().getResource(path);
            // System.err.println("path: " + path + " URL: " + url);
            if (url == null)
                throw new IOException("Failed to get resource: " + path);

            try (final InputStream ins = url.openStream()) {
                if (ins == null)
                    throw new IOException("Failed to open input stream for URL: " + url);

                root = fxmlLoader.load(ins);
            }
            controller = fxmlLoader.getController();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * get the loader
     *
     * @return loader
     */
    public FXMLLoader getFxmlLoader() {
        return fxmlLoader;
    }

    /**
     * get the root node
     *
     * @return root node
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * get the controller object
     *
     * @return controller
     */
    public C getController() {
        return controller;
    }
}
