/*
 * NodeAttributes.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph.fmm.algorithm;

import jloda.graph.Node;
import jloda.graph.fmm.FastMultiLayerMethodLayout;
import jloda.graph.fmm.geometry.DPoint;

import java.util.ArrayList;

/**
 * implementation of the fast multilayer method (note: without multipole algorithm)
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class NodeAttributes implements FastMultiLayerMethodLayout.Point {
	private DPoint position = new DPoint();

	private float width;
	private float height;

	private Node lowerLevelNode;
	private Node higherLevelNode;

	// multi-level step:
	private int mass;

	public enum Type {Unspecified, Sun, Planet, PlanetWithMoons, Moon}

	private Type type = Type.Unspecified; // 1: sun, 2: planet, 3: planet with moons, 4: moon
	private Node dedicatedSunNode;
	private double dedicatedSunDistance;
	private Node dedicatedPMNode;
	private ArrayList<Double> lambdas;
	private ArrayList<Node> neighborSunNodes;
	private ArrayList<Node> moons;
	private boolean placed = false;
	private double angle1 = 0;
	private double angle2 = 6.2831853;

	public NodeAttributes() {
	}

	public NodeAttributes(float width, float height, DPoint position, Node vLow, Node vHigh) {
		this.width = width;
		this.height = height;
		this.position = position;
		this.lowerLevelNode = vLow;
		this.higherLevelNode = vHigh;
	}


	public float getRadius() {
		return Math.max(width, height) / 2f;
	}

	public void setPosition(DPoint position) {
		this.position = position;
	}

	public void setPosition(double x, double y) {
		this.position = new DPoint(x, y);
	}

	public DPoint getPosition() {
		return position;
	}

	public double getX() {
		return position.getX();
	}

	public double getY() {
		return position.getY();
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public Node getOriginalNode() {
		return lowerLevelNode;
	}

	public void setOriginalNode(Node orig) {
		this.lowerLevelNode = orig;
	}

	public Node getCopyNode() {
		return higherLevelNode;
	}

	public void setCopyNode(Node copy) {
		this.higherLevelNode = copy;
	}

	public Node getLowerLevelNode() {
		return lowerLevelNode;
	}

	public void setLowerLevelNode(Node lowerLevelNode) {
		this.lowerLevelNode = lowerLevelNode;
	}

	public Node getHigherLevelNode() {
		return higherLevelNode;
	}

	public void setHigherLevelNode(Node higherLevelNode) {
		this.higherLevelNode = higherLevelNode;
	}

	public int getMass() {
		return mass;
	}

	public void setMass(int mass) {
		this.mass = mass;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Node getDedicatedSunNode() {
		return dedicatedSunNode;
	}

	public void setDedicatedSunNode(Node dedicatedSunNode) {
		this.dedicatedSunNode = dedicatedSunNode;
	}

	public double getDedicatedSunDistance() {
		return dedicatedSunDistance;
	}

	public void setDedicatedSunDistance(double dedicatedSunDistance) {
		this.dedicatedSunDistance = dedicatedSunDistance;
	}

	public Node getDedicatedPMNode() {
		return dedicatedPMNode;
	}

	public void setDedicatedPMNode(Node dedicatedPMNode) {
		this.dedicatedPMNode = dedicatedPMNode;
	}

	public ArrayList<Double> getLambdas() {
		if (lambdas == null)
			lambdas = new ArrayList<>();
		return lambdas;
	}

	public ArrayList<Node> getNeighborSunNodes() {
		if (neighborSunNodes == null)
			neighborSunNodes = new ArrayList<>();
		return neighborSunNodes;
	}

	public ArrayList<Node> getMoons() {
		if (moons == null)
			moons = new ArrayList<>();
		return moons;
	}

	public boolean isPlaced() {
		return placed;
	}

	public void placed() {
		this.placed = true;
	}

	public double getAngle1() {
		return angle1;
	}

	public void setAngle1(double angle1) {
		this.angle1 = angle1;
	}

	public double getAngle2() {
		return angle2;
	}

	public void setAngle2(double angle2) {
		this.angle2 = angle2;
	}

	public void initMultiLevelValues() {
		type = Type.Unspecified;
		dedicatedSunNode = null;
		dedicatedSunDistance = 0;
		dedicatedPMNode = null;
		lambdas = null;
		neighborSunNodes = null;
		moons = null;
		placed = false;
		angle1 = 0;
		angle2 = 6.2831853;
	}
}
