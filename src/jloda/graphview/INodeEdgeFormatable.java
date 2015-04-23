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

import jloda.graph.EdgeSet;
import jloda.graph.NodeSet;

import javax.swing.*;
import java.awt.*;

/**
 * used to format nodes and edges
 * Daniel Huson, 7.2010
 */
public interface INodeEdgeFormatable {
    Color getColorSelectedNodes();

    Color getBackgroundColorSelectedNodes();

    Color getLabelColorSelectedNodes();

    Color getLabelBackgroundColorSelectedNodes();

    boolean setColorSelectedNodes(Color a);

    boolean setBackgroundColorSelectedNodes(Color a);

    boolean setLabelColorSelectedNodes(Color a);

    boolean setLabelBackgroundColorSelectedNodes(Color a);

    Font getFontSelected();

    int getWidthSelectedNodes();

    int getHeightSelectedNodes();

    int getLineWidthSelectedNodes();

    byte getShapeSelectedNodes();

    boolean setFontSelectedEdges(String family, int bold, int italics, int size);

    boolean setFontSelectedNodes(String family, int bold, int italics, int size);


    void setWidthSelectedNodes(byte a);

    void setHeightSelectedNodes(byte a);

    void setLineWidthSelectedNodes(byte a);

    void setShapeSelectedNodes(byte a);


    Color getColorSelectedEdges();

    Color getLabelColorSelectedEdges();

    Color getLabelBackgroundColorSelectedEdges();

    boolean setColorSelectedEdges(Color a);

    boolean setLabelBackgroundColorSelectedEdges(Color a);

    boolean setLabelColorSelectedEdges(Color a);

    int getLineWidthSelectedEdges();

    int getDirectionSelectedEdges();

    byte getShapeSelectedEdges();

    void setLineWidthSelectedEdges(byte a);

    void setDirectionSelectedEdges(byte a);

    void setShapeSelectedEdges(byte a);

    void addNodeActionListener(NodeActionListener nal);

    void removeNodeActionListener(NodeActionListener nal);

    void addEdgeActionListener(EdgeActionListener eal);

    void removeEdgeActionListener(EdgeActionListener eal);

    boolean hasSelectedNodes();

    boolean hasSelectedEdges();

    void setLabelVisibleSelectedNodes(boolean visible);

    boolean hasLabelVisibleSelectedNodes();

    void setLabelVisibleSelectedEdges(boolean visible);

    boolean hasLabelVisibleSelectedEdges();

    void repaint();

    boolean getLockXYScale();

    void rotateLabelsSelectedNodes(int percent);

    void rotateLabelsSelectedEdges(int percent);

    JPanel getPanel();

    JScrollPane getScrollPane();

    void setRandomColorsSelectedNodes(boolean foreground, boolean background, boolean labelforeground, boolean labelbackgrond);

    void setRandomColorsSelectedEdges(boolean foreground, boolean labelforeground, boolean labelbackgrond);

    NodeSet getSelectedNodes();

    EdgeSet getSelectedEdges();

    boolean selectAllNodes(boolean select);

    boolean selectAllEdges(boolean select);

    JFrame getFrame();

}
