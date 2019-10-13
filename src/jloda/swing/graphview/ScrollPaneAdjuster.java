/*
 * ScrollPaneAdjuster.java Copyright (C) 2019. Daniel H. Huson
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
import java.awt.geom.Point2D;

/**
 * this is used when zooming or rotating the graph in a graphview to adjust the
 * scroll pane so that the content in the middle of the window stays fixed
 * Daniel Huson, 12.2006
 */
public class ScrollPaneAdjuster {
    private final JScrollBar scrollBarX;
    private final JScrollBar scrollBarY;
    private final Transform trans;
    private final Point centerDC;
    private final Point2D centerWC;

    /**
     * construct object and "remember" how scrollpane is currently centered around middle of screen
     *
     * @param scrollPane
     * @param trans
     */
    public ScrollPaneAdjuster(JScrollPane scrollPane, Transform trans) {
        this(scrollPane, trans, null);
    }

    /**
     * construct object and "remember" how scrollpane is currently centered
     *
     * @param scrollPane
     * @param trans
     * @param centerDC   center point in device coordinates
     */
    public ScrollPaneAdjuster(JScrollPane scrollPane, Transform trans, Point centerDC) {
        this.trans = trans;
        scrollBarX = scrollPane.getHorizontalScrollBar();
        scrollBarY = scrollPane.getVerticalScrollBar();

        if (centerDC == null) // if no point given, center on window
            centerDC = new Point(scrollBarX.getValue() + scrollBarX.getVisibleAmount() / 2,
                    scrollBarY.getValue() + scrollBarY.getVisibleAmount() / 2);
        this.centerDC = centerDC;

        // save world coordinates of center
        if (trans != null)
            centerWC = trans.d2w(this.centerDC);
        else {
            centerWC = (Point) centerDC.clone(); // todo: this is broken for the chartviewer
        }
    }

    /**
     * adjusts the scroll bars to recenter on world coordinates that were previously in
     * center of window
     *
     * @param horizontal adjust horizontally
     * @param vertical   adjust vertically
     */
    public void adjust(boolean horizontal, boolean vertical) {
        Point newPosDC;
        if (trans != null) {
            boolean useMagnifier = trans.getMagnifier().isActive();
            trans.getMagnifier().setActive(false);
            newPosDC = trans.w2d(centerWC);
            trans.getMagnifier().setActive(useMagnifier);
        } else {
            newPosDC = centerDC; // todo: fix this
        }
        if (horizontal) {
            int diff = (int) Math.round(newPosDC.getX()) - centerDC.x;
            diff -= 1;
            scrollBarX.setValue(scrollBarX.getValue() + diff);
        }
        if (vertical) {
            int diff = (int) Math.round(newPosDC.getY()) - centerDC.y;
            if (trans != null && trans.getFlipV())
                diff = -diff;
            scrollBarY.setValue(scrollBarY.getValue() + diff);
        }
    }
}
