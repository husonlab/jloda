/*
 * NodeShapeIcon.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.swing.graphview;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * a node shape icon
 * Created by huson on 2/13/17.
 */
public class NodeShapeIcon extends ImageIcon {
    /**
     * constructor
     *
     * @param shape
     * @param bgColor
     */
    public NodeShapeIcon(NodeShape shape, int size, Color bgColor) {
        final DefaultNodeDrawer drawer = new DefaultNodeDrawer(null);
        final NodeView nodeView = new NodeView();
        nodeView.setLocation(size / 2, size / 2);
        nodeView.setBackgroundColor(bgColor);
        nodeView.setColor(Color.BLACK);
        nodeView.setNodeShape(shape);
        nodeView.setWidth(size);
        nodeView.setHeight(size);
        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        drawer.setup(null, g);
        drawer.draw(nodeView);
        g.dispose();
        setImage(image);

    }
}
