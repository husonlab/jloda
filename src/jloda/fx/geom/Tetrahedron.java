/*
 * Tetrahedron.java Copyright (C) 2022 Daniel H. Huson
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
 * a basic Tetrahedron
 * Daniel Huson, 9.2015
 */
public class Tetrahedron extends MeshView {

    /**
     * constructor
     *
	 */
    public Tetrahedron(float length) {
        final TriangleMesh mesh = new TriangleMesh();
        final float p1 = 0.0f;
        final float p2 = (float) (length * Math.sqrt(2.0) / 2.0f);

        mesh.getPoints().addAll(
                length, p1, -p2,
                -length, p1, -p2,
                p1, length, p2,
                p1, -length, p2
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
                0, 0, 1, 1, 2, 2,
                1, 1, 0, 0, 3, 3,
                2, 2, 1, 1, 3, 4,
                0, 0, 2, 2, 3, 5
        );
        mesh.getFaceSmoothingGroups().addAll(0, 2, 4, 8);
        setMesh(mesh);
    }
}
