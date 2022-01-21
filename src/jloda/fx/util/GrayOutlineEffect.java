/*
 * GrayOutlineEffect.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.util;

import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * gray outline
 * Daniel Huson, 7.2021
 */
public class GrayOutlineEffect extends DropShadow {
    private static GrayOutlineEffect instance;

    public static GrayOutlineEffect getInstance() {
        if (instance == null)
            instance = new GrayOutlineEffect();
        return instance;
    }

    private GrayOutlineEffect() {
        super(BlurType.THREE_PASS_BOX, Color.GRAY, 0.5, 0.5, 0, 0);
    }
}
