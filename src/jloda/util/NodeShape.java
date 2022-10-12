/*
 *  NodeShape.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.util;

/**
 * node shapes
 * Created by huson on 2/13/17.
 */
public enum NodeShape {
	None, Rectangle, Oval, Triangle, Diamond, TriangleDown, Star4, Star5, Pentagon, Star6, CrossPlus, CrossX, Hexagon, CirclePlus, CircleX, SquarePlus, SquareX;

	public static NodeShape valueOfIgnoreCase(String name) {
		if (name == null)
			return null;
		for (NodeShape shape : values()) {
			if (shape.toString().equalsIgnoreCase(name))
				return shape;
		}
		return null;
	}
}
