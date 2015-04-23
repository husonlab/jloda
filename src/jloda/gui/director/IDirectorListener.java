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

/**
 * director events that viewers listen to
 * @author huson
 * 11.03
 */
package jloda.gui.director;

import jloda.util.CanceledException;

/**
 * director events that viewers listen to
 *
 * @author huson
 *         11.03
 */
public interface IDirectorListener extends IUpdateableView {
    /**
     * ask view to update itself. This is method is wrapped into a runnable object
     * and put in the swing event queue to avoid concurrent modifications.
     *
     * @param what what should be updated? Possible values: Director.ALL or Director.TITLE
     */
    public void updateView(String what);

    /**
     * ask view to prevent user input
     */
    public void lockUserInput();

    /**
     * ask view to allow user input
     */
    public void unlockUserInput();

    /**
     * is viewer currently locked?
     *
     * @return true, if locked
     */
    public boolean isLocked();

    /**
     * ask view to destroy itself
     */
    public void destroyView() throws CanceledException;

    /**
     * set uptodate state
     *
     * @param flag
     */
    public void setUptoDate(boolean flag);
}
