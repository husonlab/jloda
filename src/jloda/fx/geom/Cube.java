/*
 * Cube.java Copyright (C) 2020. Daniel H. Huson
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
 * a basic cube
 * Daniel Huson, 9.2015
 */
public class Cube extends MeshView {

    /**
     * constructor
     *
     * @param width
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
