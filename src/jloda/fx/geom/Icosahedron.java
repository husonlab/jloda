/*
 * Icosahedron.java Copyright (C) 2022 Daniel H. Huson
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
 * a basic Icosahedron
 * Daniel Huson, 9.2015
 */
public class Icosahedron extends MeshView {

    /**
     * constructor
     *
	 */
    public Icosahedron(float length) {
        final TriangleMesh mesh = new TriangleMesh();
        final float p0 = 0.5f * length;
        final float p1 = 0.0f;
        final float p2 = (float) (length * (Math.sqrt(5.0) + 1.0) / 4.0f);

        mesh.getPoints().addAll(
                p0, p1, p2,
                p0, p1, -p2,
                -p0, p1, p2,
                -p0, p1, -p2,
                p2, p0, p1,
                p2, -p0, p1,
                -p2, p0, p1,
                -p2, -p0, p1,
                p1, p2, p0,
                p1, p2, -p0,
                p1, -p2, p0,
                p1, -p2, -p0
        );
        mesh.getTexCoords().addAll(
                0.50f, 1.00f,
                0.75f, (float) (1.0 - Math.sqrt(3.0) / 4.0f),
                0.25f, (float) (1.0 - Math.sqrt(3.0) / 4.0f),
                1.00f, 1.00f,
                0.50f, (float) (1.0 - Math.sqrt(3.0) / 2.0f),
                0.00f, 1.00f
        );
        mesh.getFaces().addAll(
                0, 0, 2, 1, 10, 2,
                0, 1, 10, 0, 5, 3,
                0, 2, 5, 1, 4, 4,
                0, 0, 4, 2, 8, 5,
                0, 1, 8, 0, 2, 3,
                2, 2, 8, 1, 6, 4,
                2, 0, 6, 2, 7, 5,
                2, 0, 7, 1, 10, 2,
                10, 2, 7, 1, 11, 4,
                10, 0, 11, 2, 5, 5,
                5, 0, 11, 1, 1, 2,
                5, 1, 1, 0, 4, 3,
                4, 0, 1, 2, 9, 5,
                4, 0, 9, 1, 8, 2,
                8, 1, 9, 0, 6, 3,
                6, 2, 9, 1, 3, 4,
                6, 0, 3, 1, 7, 2,
                7, 1, 3, 0, 11, 3,
                11, 2, 3, 1, 1, 4,
                1, 0, 3, 2, 9, 5
        );
        mesh.getFaceSmoothingGroups().addAll(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        );

        setMesh(mesh);
    }
}
