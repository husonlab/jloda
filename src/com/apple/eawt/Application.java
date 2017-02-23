/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package com.apple.eawt;

/**
 * bare bones for compilation under winds
 * Created by huson on 2/23/17.
 */
public class Application {
    private static Application instance;

    private Application() {

    }

    public Application getApplication() {
        if (instance == null)
            instance = new Application();
        return instance;
    }

    public void setQuitHandler(Object ignored) {
    }

    public void setAboutHandler(Object ignored) {
    }

    public void setPreferencesHandler(Object ignored) {
    }

}
