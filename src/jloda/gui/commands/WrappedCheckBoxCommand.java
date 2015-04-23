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
 * a wrapped command
 * This is used for globally defined commands to ensure that they are given the correct dir, parent and viewer on execution
 * Daniel Huson, 4.2015
 */
public class WrappedCheckBoxCommand extends WrappedCommand implements ICheckBoxCommand {

    /**
     * constructor
     *
     * @param command
     */
    public WrappedCheckBoxCommand(ICheckBoxCommand command) {
        super(command);
    }

    /**
     * this is currently selected?
     *
     * @return selected
     */
    @Override
    public boolean isSelected() {
        return ((ICheckBoxCommand) command).isSelected();
    }

    /**
     * set the selected status of this command
     *
     * @param selected
     */
    @Override
    public void setSelected(boolean selected) {
        ((ICheckBoxCommand) command).setSelected(selected);
    }
}
