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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * this is used when zooming or rotating the graph in a graphview to adjust the
 * scroll pane so that the content in the middle of the window stays fixed
 * Daniel Huson, 12.2006
 */
public class ScrollPaneAdjuster {
    private final JScrollPane scrollPane;
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
        this.scrollPane = scrollPane;
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
