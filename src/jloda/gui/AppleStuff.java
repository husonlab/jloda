/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
