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

package jloda.gui.find;

import javax.swing.*;
import java.awt.*;

/**
 * command interface for find dialog and find toolbar
 * Daniel Huson, 2.2012
 */
public interface IFindDialog {
    JFrame getFrame();

    boolean selectTarget(String name);

    void chooseTargetForFrame(Component parent);

    SearchActions getActions();

    void setMessage(String message);

    void clearMessage();

    void updateTargets();

    String getFindText();

    String getReplaceText();
}
