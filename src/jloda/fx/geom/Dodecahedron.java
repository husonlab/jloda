/*
 * Dodecahedron.java Copyright (C) 2020. Daniel H. Huson
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
 * a basic Dodecahedron
 * Daniel Huson, 9.2015
 */
public class Dodecahedron extends MeshView {

    /**
     * constructor
     *
     * @param length
     */
    public Dodecahedron(float length) {
        final TriangleMesh mesh = new TriangleMesh();
        final float p0 = (float) (length / Math.sqrt(3.0f));
        final float p1 = (float) (length * Math.sqrt((3.0 - Math.sqrt(5.0)) / 6.0));
        final float p2 = (float) (length * Math.sqrt((3.0 + Math.sqrt(5.0)) / 6.0));
        final float p3 = 0.0f;

        mesh.getPoints().addAll(
                p0, p0, p0,
                p0, p0, -p0,
                p0, -p0, p0,
                p0, -p0, -p0,
                -p0, p0, p0,
                -p0, p0, -p0,
                -p0, -p0, p0,
                -p0, -p0, -p0,
                p1, p2, p3,
                -p1, p2, p3,
                p1, -p2, p3,
                -p1, -p2, p3,
                p2, p3, p1,
                p2, p3, -p1,
                -p2, p3, p1,
                -p2, p3, -p1,
                p3, p1, p2,
                p3, -p1, p2,
                p3, p1, -p2,
                p3, -p1, -p2
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
                0, 2, 8, 1, 9, 4,
                0, 2, 9, 1, 4, 4,
                0, 2, 4, 1, 16, 4,
                0, 2, 12, 1, 13, 4,
                0, 2, 13, 1, 1, 4,
                0, 2, 1, 1, 8, 4,
                0, 2, 16, 1, 17, 4,
                0, 2, 17, 1, 2, 4,
                0, 2, 2, 1, 12, 4,
                8, 2, 1, 1, 18, 4,
                8, 2, 18, 1, 5, 4,
                8, 2, 5, 1, 9, 4,
                12, 2, 2, 1, 10, 4,
                12, 2, 10, 1, 3, 4,
                12, 2, 3, 1, 13, 4,
                16, 2, 4, 1, 14, 4,
                16, 2, 14, 1, 6, 4,
                16, 2, 6, 1, 17, 4,
                9, 2, 5, 1, 15, 4,
                9, 2, 15, 1, 14, 4,
                9, 2, 14, 1, 4, 4,
                6, 2, 11, 1, 10, 4,
                6, 2, 10, 1, 2, 4,
                6, 2, 2, 1, 17, 4,
                3, 2, 19, 1, 18, 4,
                3, 2, 18, 1, 1, 4,
                3, 2, 1, 1, 13, 4,
                7, 2, 15, 1, 5, 4,
                7, 2, 5, 1, 18, 4,
                7, 2, 18, 1, 19, 4,
                7, 2, 11, 1, 6, 4,
                7, 2, 6, 1, 14, 4,
                7, 2, 14, 1, 15, 4,
                7, 2, 19, 1, 3, 4,
                7, 2, 3, 1, 10, 4,
                7, 2, 10, 1, 11, 4
        );
        mesh.getFaceSmoothingGroups().addAll(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        );
        setMesh(mesh);
    }
}
