/*
 * Octahedron.java Copyright (C) 2021. Daniel H. Huson
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
 * a basic octahedron
 * Daniel Huson, 9.2015
 */
public class Octahedron extends MeshView {

    /**
     * constructor
     *
     * @param length
     */
    public Octahedron(float length) {
        final TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(
                length, 0.0f, 0.0f,
                -length, 0.0f, 0.0f,
                0.0f, length, 0.0f,
                0.0f, -length, 0.0f,
                0.0f, 0.0f, length,
                0.0f, 0.0f, -length
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
                4, 0, 0, 1, 2, 2,
                4, 1, 2, 0, 1, 3,
                4, 2, 1, 1, 3, 4,
                4, 0, 3, 2, 0, 5,
                5, 0, 2, 2, 0, 5,
                5, 2, 1, 1, 2, 4,
                5, 1, 3, 0, 1, 3,
                5, 0, 0, 1, 3, 2
        );
        mesh.getFaceSmoothingGroups().addAll(
                0, 2, 4, 8, 16, 32, 64, 128
        );
        setMesh(mesh);
    }
}
