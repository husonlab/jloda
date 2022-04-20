/*
 *  PrintUtils.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.util;

import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

/**
 * print utilities
 * Daniel Huson, 4.2022
 */
public class PrintUtils {
	/**
	 * creates a printable or copyable image, which is 4 times as large as the original for better resolution
	 *
	 * @param region              the region to print
	 * @param containedScrollPane a scrollpane contained in the region, used to crop scrollbars, if visible
	 * @return image view
	 */
	public static ImageView createImage(Region region, ScrollPane containedScrollPane) {
		var parameters = new SnapshotParameters();
		parameters.setTransform(new Scale(4, 4));
		var right = (containedScrollPane != null && BasicFX.isScrollBarVisible(containedScrollPane, Orientation.VERTICAL) ? 64 : 8);
		var bottom = (containedScrollPane != null && BasicFX.isScrollBarVisible(containedScrollPane, Orientation.HORIZONTAL) ? 64 : 8);
		parameters.setViewport(new Rectangle2D(4, 4, Math.max(100, 4 * region.getWidth() - right), Math.max(100, 4 * region.getHeight() - bottom)));
		return new ImageView(region.snapshot(parameters, null));
	}

}
