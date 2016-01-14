/**
 * INodeEdgeFormatable.java 
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
