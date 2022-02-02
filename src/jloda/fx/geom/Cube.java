/*
 * Cube.java Copyright (C) 2022 Daniel H. Huson
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
package jloda.fx.geom;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * a basic cube
 * Daniel Huson, 9.2015
 */
public class Cube extends MeshView {

    /**
     * constructor
     *
	 */
    public Cube(float width, float height, float depth) {
        final TriangleMesh mesh = new TriangleMesh();
        final float w = width / 2.0f;
        final float h = height / 2.0f;
        final float d = depth / 2.0f;

        mesh.getPoints().addAll(
                w, h, d,
                w, h, -d,
                w, -h, d,
                w, -h, -d,
                -w, h, d,
                -w, h, -d,
                -w, -h, d,
                -w, -h, -d
        );
        mesh.getTexCoords().addAll(
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f
        );
        mesh.getFaces().addAll(
                0, 2, 1, 1, 5, 0,
                0, 2, 5, 0, 4, 3,
                0, 2, 4, 1, 6, 0,
                0, 2, 6, 0, 2, 3,
                0, 2, 2, 1, 3, 0,
                0, 2, 3, 0, 1, 3,
                7, 2, 3, 1, 2, 0,
                7, 2, 2, 0, 6, 3,
                7, 2, 6, 1, 4, 0,
                7, 2, 4, 0, 5, 3,
                7, 2, 5, 1, 1, 0,
                7, 2, 1, 0, 3, 3
        );
        mesh.getFaceSmoothingGroups().addAll(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        setMesh(mesh);
    }
}
