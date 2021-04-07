/*
 * Tetrahedron.java Copyright (C) 2021. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *  No usage, copying or distribution without explicit permission.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
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
     * @param length
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
