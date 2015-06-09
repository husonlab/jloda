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

package jloda.gui.commands;

/**
 * Checkbox command
 * Daniel Huson, 5.2010
 */
public interface ICheckBoxCommand extends ICommand {

    /**
     * this is currently selected?
     *
     * @return selected
     */
    boolean isSelected();

    /**
     * set the selected status of this command
     *
     * @param selected
     */
    void setSelected(boolean selected);
}
