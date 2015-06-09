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

package jloda.gui.director;

import jloda.gui.commands.CommandManager;

import javax.swing.*;

/**
 * A directable viewer is one that listens to the director updates and informs
 * on the uptodate status
 *
 * @author huson
 *         Date: 26-Nov-2003
 */
public interface IDirectableViewer extends IDirectorListener {
    /**
     * is viewer uptodate?
     *
     * @return uptodate
     */
    boolean isUptoDate();

    /**
     * return the frame associated with the viewer
     *
     * @return frame
     */
    JFrame getFrame();

    /**
     * gets the title
     *
     * @return title
     */
    String getTitle();

    /**
     * gets the associated command manager
     *
     * @return command manager
     */
    CommandManager getCommandManager();
}
