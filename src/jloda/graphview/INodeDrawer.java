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

package jloda.graphview;

import jloda.graph.Node;

import java.awt.*;

/**
 * interface for drawing nodes
 * Daniel Huson, 1.2012
 */
public interface INodeDrawer {
    /**
     * setup data
     *
     * @param graphView
     * @param gc
     */
    void setup(GraphView graphView, Graphics2D gc);

    /**
     * draw the node
     *
     * @param selected
     */
    void draw(Node v, boolean selected);

    /**
     * draw the label of the node
     *
     * @param selected
     */
    void drawLabel(Node v, boolean selected);

    /**
     * draw the node and the label
     *
     * @param selected
     */
    void drawNodeAndLabel(Node v, boolean selected);


}
