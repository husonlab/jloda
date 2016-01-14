/**
 * AppleStuff.java 
 * Copyright (C) 2016 Daniel H. Huson
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
package jloda.gui;


import com.apple.eawt.*;

import javax.swing.*;

/**
 * Apple specific stuff
 * Daniel Huson, 3.2014
 */
public class AppleStuff {
    static private AppleStuff instance;
    private final Application application;
    private boolean isQuitDefined;
    private boolean isAboutDefined;
    private boolean isPreferencesDefined;

    /**
     * constructor
     */
    private AppleStuff() {
        application = Application.getApplication();
    }

    /**
     * get instance
     *
     * @return instance
     */
    public static AppleStuff getInstance() {
        if (instance == null)
            instance = new AppleStuff();
        return instance;
    }

    /**
     * sets the quit action
     *
     * @param action
     */
    public void setQuitAction(final Action action) {
        isQuitDefined = true;
        application.setQuitHandler(new QuitHandler() {
            @Override
            public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse) {
                action.actionPerformed(null);
                quitResponse.cancelQuit();
            }
        });
    }

    /**
     * set the about action
     *
     * @param action
     */
    public void setAboutAction(final Action action) {
        isAboutDefined = true;
        application.setAboutHandler(new AboutHandler() {
            @Override
            public void handleAbout(AppEvent.AboutEvent aboutEvent) {
                action.actionPerformed(null);
            }
        });
    }

    public void setPreferencesAction(final Action action) {
        isPreferencesDefined = true;
        application.setPreferencesHandler(new PreferencesHandler() {
            @Override
            public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
                action.actionPerformed(null);
            }
        });
    }

    public boolean isQuitDefined() {
        return isQuitDefined;
    }

    public boolean isAboutDefined() {
        return isAboutDefined;
    }

    public boolean isPreferencesDefined() {
        return isPreferencesDefined;
    }

}
