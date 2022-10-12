/*
 * ShapeFactory.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import jloda.util.NodeShape;

import java.util.Random;

/**
 * simple shape factory
 * Daniel Huson, 9.2015
 */
public class ShapeFactory {
    private static Random random;

    public enum Type {Ball, Cube, Octahedron, Tetrahedron, Dodecahedron, Icosahedron, Random}

    /**
     * creates a shape
     *
	 */
    public static Shape3D makeShape(float size, Type type) {
        Shape3D shape;
        switch (type) {
            default /* case Ball */ -> shape = new Sphere(size);
            case Cube -> shape = new Cube(1.7f * size, 1.7f * size, 1.7f * size);
            case Octahedron -> shape = new Octahedron(1.4f * size);
            case Tetrahedron -> shape = new Tetrahedron(1.4f * size);
            case Dodecahedron -> shape = new Dodecahedron(1.4f * size);
            case Icosahedron -> shape = new Icosahedron(1.4f * size);
            case Random -> {
                if (random == null)
                    random = new Random();
                shape = makeShape(size, Type.values()[random.nextInt(Type.values().length - 1)]); // length-1 so that we don't choose "Random"
            }
        }
        return shape;
    }

    public static Shape3D makeShape(float size, NodeShape nodeShape) {
        return switch (nodeShape) {
            case Diamond -> makeShape(size, Type.Octahedron);
            case Triangle -> makeShape(size, Type.Tetrahedron);
            case Rectangle -> makeShape(size, Type.Cube);
            default /* case Oval */ -> makeShape(size, Type.Ball);
        };
    }
}
