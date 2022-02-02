/*
 * TransformableGroup.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.TransformChangedEvent;
import javafx.scene.transform.Translate;

/**
 * Group with its own transformation
 * Original source: Getting started with JavaFX, MoleculeSampleApp
 * Daniel Huson, 9.2015
 */
public class TransformableGroup extends Group {

    public enum RotateOrder {
        XYZ, XZY, YXZ, YZX, ZXY, ZYX
    }

    private final Translate t = new Translate();
    private final Translate p = new Translate();

    private final Rotate rx = new Rotate();

    {
        rx.setAxis(Rotate.X_AXIS);
    }

    private final Rotate ry = new Rotate();

    {
        ry.setAxis(Rotate.Y_AXIS);
    }

    private final Rotate rz = new Rotate();

    {
        rz.setAxis(Rotate.Z_AXIS);
    }

    public final Scale s = new Scale();

    /**
     * constructor
     */
    public TransformableGroup() {
        super();
        getTransforms().addAll(t, rz, ry, rx, s);
    }

    /**
     * constructor
     *
	 */
    public TransformableGroup(RotateOrder rotateOrder) {
        super();
        // choose the order of rotations based on the rotateOrder
        switch (rotateOrder) {
            case XYZ -> getTransforms().addAll(t, p, rz, ry, rx, s);
            case XZY -> getTransforms().addAll(t, p, ry, rz, rx, s);
            case YXZ -> getTransforms().addAll(t, p, rz, rx, ry, s);
            case YZX -> getTransforms().addAll(t, p, rx, rz, ry, s);  // For Camera
            case ZXY -> getTransforms().addAll(t, p, ry, rx, rz, s);
            case ZYX -> getTransforms().addAll(t, p, rx, ry, rz, s);
        }
    }

    /**
     * set translation
     *
	 */
    public void setTranslate(double x, double y, double z) {
        t.setX(x);
        t.setY(y);
        t.setZ(z);
    }

    /**
     * set translation
     *
	 */
    public void setTranslate(double x, double y) {
        t.setX(x);
        t.setY(y);
    }

    // Cannot override these methods as they are final:
    // public void setTranslateX(double x) { t.setX(x); }
    // public void setTranslateY(double y) { t.setY(y); }
    // public void setTranslateZ(double z) { t.setZ(z); }
    // Use these methods instead:
    public void setTx(double x) {
        t.setX(x);
    }

    public void setTy(double y) {
        t.setY(y);
    }

    public void setTz(double z) {
        t.setZ(z);
    }

    public double getTx() {
        return t.getX();
    }

    public double getTy() {
        return t.getY();
    }

    public double getTz() {
        return t.getZ();
    }

    /**
     * set rotation
     *
	 */
    public void setRotate(double x, double y, double z) {
        rx.setAngle(x);
        ry.setAngle(y);
        rz.setAngle(z);
    }

    public void setRotateX(double x) {
        rx.setAngle(x);
    }

    public void setRotateY(double y) {
        ry.setAngle(y);
    }

    public void setRotateZ(double z) {
        rz.setAngle(z);
    }

    public void setRx(double x) {
        rx.setAngle(x);
    }

    public void setRy(double y) {
        ry.setAngle(y);
    }

    public void setRz(double z) {
        rz.setAngle(z);
    }

    public void setScale(double scaleFactor) {
        s.setX(scaleFactor);
        s.setY(scaleFactor);
        s.setZ(scaleFactor);
    }

    public void setScale(double x, double y, double z) {
        s.setX(x);
        s.setY(y);
        s.setZ(z);
    }

    // Cannot override these methods as they are final:
    // public void setScaleX(double x) { s.setX(x); }
    // public void setScaleY(double y) { s.setY(y); }
    // public void setScaleZ(double z) { s.setZ(z); }
    // Use these methods instead:
    public void setSx(double x) {
        s.setX(x);
    }

    public void setSy(double y) {
        s.setY(y);
    }

    public void setSz(double z) {
        s.setZ(z);
    }

    public double getSx() {
        return s.getX();
    }

    public double getSy() {
        return s.getY();
    }

    public double getSz() {
        return s.getZ();
    }

    public void setPivot(double x, double y, double z) {
        p.setX(x);
        p.setY(y);
        p.setZ(z);
    }

    /**
     * resetData all
     */
    public void reset() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        rx.setAngle(0.0);
        ry.setAngle(0.0);
        rz.setAngle(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
    }

    /**
     * resetData translation, scale and pivot
     */
    public void resetTSP() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
    }

    public void addTransformChangedEventHandler(EventHandler<Event> handler) {
        t.addEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        s.addEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        p.addEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        rx.addEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        ry.addEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        rz.addEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
    }

    public void removeTransformChangedEventHandler(EventHandler<Event> handler) {
        t.removeEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        s.removeEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        p.removeEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        rx.removeEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        ry.removeEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
        rz.removeEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, handler);
    }
}
