/*
 * INodeDrawer.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.swing.graphview;

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
	 */
    void setup(GraphView graphView, Graphics2D gc);

    /**
     * draw the node
     *
	 */
    void draw(Node v, boolean selected);

    /**
     * draw the label of the node
     *
	 */
    void drawLabel(Node v, boolean selected);

    /**
     * draw the node and the label
     *
	 */
    void drawNodeAndLabel(Node v, boolean selected);


}
