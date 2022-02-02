/*
 * MultiLevel.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.graph.*;
import jloda.graph.fmm.FastMultiLayerMethodOptions;
import jloda.graph.fmm.geometry.DPoint;
import jloda.util.Counter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;


/**
 * implementation of the fast multilayer method (note: without multipole algorithm)
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class MultiLevel {
	private static final Random random = new Random();

	/**
	 * creates the multi-level representation
	 *
	 * @return the number of levels
	 */
	public static int createMultiLevelRepresentations(FastMultiLayerMethodOptions options, Graph graph, NodeArray<NodeAttributes> nodeAttributes,
													  EdgeArray<EdgeAttributes> edgeAttributes,
													  Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes,
													  EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes) {
		multiLevelGraph[0] = graph;
		multiLevelNodeAttributes[0] = nodeAttributes;
		multiLevelEdgeAttributes[0] = edgeAttributes;

		var badEdgeNrCounter = new Counter(0);
		var activeLevel = 0;
		var activeGraph = multiLevelGraph[0];

		while (activeGraph.getNumberOfNodes() > options.getMinGraphSize() && edgeNumberSumOfAllLevelsIsLinear(multiLevelGraph, activeLevel, badEdgeNrCounter)) {
			var newGraph = new Graph();
			NodeArray<NodeAttributes> newNodeAttributes = newGraph.newNodeArray();
			EdgeArray<EdgeAttributes> newEdgeAttributes = newGraph.newEdgeArray();
			multiLevelGraph[activeLevel + 1] = newGraph;
			multiLevelNodeAttributes[activeLevel + 1] = newNodeAttributes;
			multiLevelEdgeAttributes[activeLevel + 1] = newEdgeAttributes;

			partitionGalaxyIntoSolarSystems(options, multiLevelGraph, multiLevelNodeAttributes, multiLevelEdgeAttributes, activeLevel);
			collapseSolarSystems(multiLevelGraph, multiLevelNodeAttributes, multiLevelEdgeAttributes, activeLevel);

			activeLevel++;
			activeGraph = multiLevelGraph[activeLevel];
		}
		return activeLevel;
	}

	/**
	 * determines whether edge number of sum of all levels can be considered linear
	 *
	 * @return true, if linear
	 */
	private static boolean edgeNumberSumOfAllLevelsIsLinear(Graph[] multiLevelGraph, int activeLevel, Counter badEdgeNrCounter) {
		if (activeLevel == 0)
			return true;
		else if (multiLevelGraph[activeLevel].getNumberOfEdges() <= 0.8 * (multiLevelGraph[activeLevel - 1].getNumberOfEdges()))
			return true;
		else if (badEdgeNrCounter.get() < 5) {
			badEdgeNrCounter.increment();
			return true;
		} else
			return false;
	}

	private static void partitionGalaxyIntoSolarSystems(FastMultiLayerMethodOptions options, Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes, int level) {
		createSunsAndPlanets(options, multiLevelGraph, multiLevelNodeAttributes, multiLevelEdgeAttributes, level);
		createMoonNodesAndPMNodes(multiLevelGraph, multiLevelNodeAttributes, multiLevelEdgeAttributes, level);
	}

	/**
	 * create suns and planets
	 *
	 */
	private static void createSunsAndPlanets(FastMultiLayerMethodOptions options, Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes, int level) {
		if (level == 0) {
			multiLevelNodeAttributes[level].values().forEach(ba -> ba.setMass(1));
		}

		final NodeSetWithGetRandomNode nodeSet;
		if (options.getGalaxyChoice() == FastMultiLayerMethodOptions.GalaxyChoice.UniformProb) {
			nodeSet = new NodeSetWithGetRandomNode(multiLevelGraph[level]);
		} else {
			nodeSet = new NodeSetWithGetRandomNode(multiLevelGraph[level], multiLevelNodeAttributes[level]);
		}
		nodeSet.setSeed(options.getRandSeed());

		var sunNodes = new ArrayList<Node>();

		while (!nodeSet.isEmpty()) { // randomly select a sun node
            Node sunNode;
            switch (options.getGalaxyChoice()) {
				default -> sunNode = nodeSet.getRandomNode();

				case NonUniformProbLowerMass -> sunNode = nodeSet.getRandomNodeWithLowestStarMass(options.getNumberRandomTries());

				case NonUniformProbHigherMass -> sunNode = nodeSet.getRandomNodeWithHighestStarMass(options.getNumberRandomTries());

			}
            sunNodes.add(sunNode);

            //create new node at higher level that represents the collapsed solar_system
            var newNode = multiLevelGraph[level + 1].newNode();
            {
                var na = new NodeAttributes();
                na.initMultiLevelValues();
                multiLevelNodeAttributes[level + 1].put(newNode, na);
            }

			//update information for sun_node
			{
				var sa = multiLevelNodeAttributes[level].get(sunNode);
				sa.setHigherLevelNode(newNode);
				sa.setType(NodeAttributes.Type.Sun);
				sa.setDedicatedSunNode(sunNode);
				sa.setDedicatedSunDistance(0);
			}

			//update information for planet_nodes
			var planetNodes = new ArrayList<Node>();
			for (var sunEdge : sunNode.adjacentEdges()) {
				double distanceToSun = multiLevelEdgeAttributes[level].get(sunEdge).getLength();
				final Node planetNode = sunEdge.getOpposite(sunNode);
				{
					var na = multiLevelNodeAttributes[level].get(planetNode);
					na.setType(NodeAttributes.Type.Planet);
					na.setDedicatedSunNode(sunNode);
					na.setDedicatedSunDistance(distanceToSun);
				}
				planetNodes.add(planetNode);
			}

			//delete all planet_nodes nodeSet
			for (var v : planetNodes) {
				if (!nodeSet.isDeleted(v))
					nodeSet.delete(v);
			}

			// determine possible moons:
			for (var v : planetNodes) {
				for (var e : v.adjacentEdges()) {
					var possibleMoonNode = e.getOpposite(v);
					var na = multiLevelNodeAttributes[level].get(possibleMoonNode);
					if (na.getType() == NodeAttributes.Type.Unspecified) {
						nodeSet.delete(possibleMoonNode);
					}
				}
			}
		}

		for (var sunNode : sunNodes) {
			var sna = multiLevelNodeAttributes[level].get(sunNode);
			var newNode = sna.getHigherLevelNode();
			var newNodeAttribute = new NodeAttributes(sna.getWidth(), sna.getHeight(), sna.getPosition(), sunNode, null);
			newNodeAttribute.setMass(0);
			multiLevelNodeAttributes[level + 1].put(newNode, newNodeAttribute);
		}
	}

	/**
	 * create moon nodes and possibly moon nodes
	 *
	 */
	private static void createMoonNodesAndPMNodes(Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes, int level) {
		for (var v : multiLevelGraph[level].nodes()) {
			if (multiLevelNodeAttributes[level].get(v).getType() == NodeAttributes.Type.Unspecified) { // possible moon node
				double distanceToNearestNeighbor = 0;
				Node nearestNeighborNode = null;
				Edge moonEdge = null;
				for (var e : v.adjacentEdges()) {
					var neighborNode = e.getOpposite(v);
					var neighborType = multiLevelNodeAttributes[level].get(neighborNode).getType();
					if (neighborType == NodeAttributes.Type.Planet || neighborType == NodeAttributes.Type.PlanetWithMoons) {
						var ea = multiLevelEdgeAttributes[level].get(e);
						if (moonEdge == null || distanceToNearestNeighbor > ea.getLength()) {
							moonEdge = e;
							distanceToNearestNeighbor = ea.getLength();
							nearestNeighborNode = neighborNode;
						}
					}
				}
				if (moonEdge != null)
					multiLevelEdgeAttributes[level].get(moonEdge).makeMoonEdge();
				if (nearestNeighborNode != null) {
					var nna = multiLevelNodeAttributes[level].get(nearestNeighborNode);
					nna.setType(NodeAttributes.Type.PlanetWithMoons);
					nna.getMoons().add(v);
					{
						var va = multiLevelNodeAttributes[level].get(v);
						va.setType(NodeAttributes.Type.Moon);
						va.setDedicatedSunNode(nna.getDedicatedSunNode());
						va.setDedicatedSunDistance(nna.getDedicatedSunDistance());
						va.setDedicatedPMNode(nearestNeighborNode);
					}
				}
			}
		}
	}

	/**
	 * collapse solar systems
	 *
	 */
	private static void collapseSolarSystems(Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes, int level) {
		calculateMassOfCollapsedNodes(multiLevelGraph, multiLevelNodeAttributes, level);
		var newEdgeLengths = createEdgesEdgeDistancesAndLambdaLists(multiLevelGraph, multiLevelNodeAttributes, multiLevelEdgeAttributes, level);
		deleteParallelEdgesAndUpdateEdgeLength(multiLevelGraph, multiLevelEdgeAttributes, newEdgeLengths, level);
	}

	/**
	 * calculate mass of collapsed nodes
	 *
	 */
	private static void calculateMassOfCollapsedNodes(Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, int level) {
		for (var v : multiLevelGraph[level].nodes()) {
			var dedicatedSun = multiLevelNodeAttributes[level].get(v).getDedicatedSunNode();
			var highestLevelNode = multiLevelNodeAttributes[level].get(dedicatedSun).getHigherLevelNode();
			multiLevelNodeAttributes[level + 1].get(highestLevelNode).setMass(multiLevelNodeAttributes[level + 1].get(highestLevelNode).getMass() + 1);

		}
	}

	/**
	 * create edge distances and lambda lists, also sets the edge attributes for the next level
	 *
	 */
	private static EdgeDoubleArray createEdgesEdgeDistancesAndLambdaLists(Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes, int level) {
		var interSolarSystemEdges = new ArrayList<Edge>();

		multiLevelEdgeAttributes[level + 1] = multiLevelGraph[level + 1].newEdgeArray();
		var newEdgeLengths = multiLevelGraph[level + 1].newEdgeDoubleArray();


		for (var e : multiLevelGraph[level].edges()) {
			var sourceSun = multiLevelNodeAttributes[level].get(e.getSource()).getDedicatedSunNode();
			var targetSun = multiLevelNodeAttributes[level].get(e.getTarget()).getDedicatedSunNode();
			if (sourceSun != targetSun) {
				var highLevelSourceSun = multiLevelNodeAttributes[level].get(sourceSun).getHigherLevelNode();
				var highLevelTargetSun = multiLevelNodeAttributes[level].get(targetSun).getHigherLevelNode();

				var newEdge = multiLevelGraph[level + 1].newEdge(highLevelSourceSun, highLevelTargetSun);
				{
					var ea = new EdgeAttributes();
					ea.initMultiLevelValues();
					multiLevelEdgeAttributes[level + 1].put(newEdge, ea);
				}
				multiLevelEdgeAttributes[level].get(e).setHigherLevelEdge(newEdge);
				interSolarSystemEdges.add(e);
			}
		}

		for (var e : interSolarSystemEdges) {
			var sourceSun = multiLevelNodeAttributes[level].get(e.getSource()).getDedicatedSunNode();
			var targetSun = multiLevelNodeAttributes[level].get(e.getTarget()).getDedicatedSunNode();

			var eLength = multiLevelEdgeAttributes[level].get(e).getLength();
			var sEdgeLength = multiLevelNodeAttributes[level].get(e.getSource()).getDedicatedSunDistance();
			var tEdgeLength = multiLevelNodeAttributes[level].get(e.getTarget()).getDedicatedSunDistance();
			var newLength = sEdgeLength + eLength + tEdgeLength;
			var eNew = multiLevelEdgeAttributes[level].get(e).getHigherLevelEdge();
			newEdgeLengths.put(eNew, newLength);

			multiLevelNodeAttributes[level].get(e.getSource()).getLambdas().add(sEdgeLength / newLength);
			multiLevelNodeAttributes[level].get(e.getTarget()).getLambdas().add(tEdgeLength / newLength);

			multiLevelNodeAttributes[level].get(e.getSource()).getNeighborSunNodes().add(targetSun);
			multiLevelNodeAttributes[level].get(e.getTarget()).getNeighborSunNodes().add(sourceSun);

		}
		return newEdgeLengths;
	}

	/**
	 * delete parallel edges and update edge lengths
	 *
	 */
	private static void deleteParallelEdgesAndUpdateEdgeLength(Graph[] multiLevelGraph, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes, EdgeDoubleArray newEdgeLengths, int level) {
		var nextGraph = multiLevelGraph[level + 1];

		var sortedEdges = nextGraph.getEdgesAsList();
		sortedEdges.sort(Comparator.comparingInt(a -> Math.max(a.getSource().getId(), a.getTarget().getId())));
		sortedEdges.sort(Comparator.comparingInt(a -> Math.min(a.getSource().getId(), a.getTarget().getId())));

		Edge prev = null;
		int counter = 1;
		for (var e : sortedEdges) {
			if (prev == null)
				prev = e;
			else {
				if (e.getSource() == prev.getSource() && e.getTarget() == prev.getTarget() || e.getSource() == prev.getTarget() && e.getTarget() == prev.getSource()) {
					newEdgeLengths.put(prev, newEdgeLengths.get(prev) + newEdgeLengths.get(e));
					nextGraph.deleteEdge(e);
					counter++;
				} else {
					if (counter > 1) {
						newEdgeLengths.put(prev, newEdgeLengths.get(prev) / counter);
						counter = 1;
					}
					prev = e;
				}
			}
		}
		if (counter > 1)
			newEdgeLengths.put(prev, newEdgeLengths.get(prev) / counter);

		for (var e : nextGraph.edges()) {
			multiLevelEdgeAttributes[level + 1].get(e).setLength(newEdgeLengths.get(e));
		}
	}

	public static void findInitialPlacementForLevel(int level, FastMultiLayerMethodOptions options, Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes) {
		setInitialPositionsOfSunNodes(level, multiLevelGraph, multiLevelNodeAttributes);
		var pmNodes = new ArrayList<Node>();
		setInitialPositionsOfPlanetAndModeNodes(level, options, multiLevelGraph, multiLevelNodeAttributes, multiLevelEdgeAttributes, pmNodes);
		setInitialPositionsOfPMNodes(level, options, multiLevelNodeAttributes, multiLevelEdgeAttributes, pmNodes);
	}

	private static void setInitialPositionsOfSunNodes(int level, Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes) {
		//multiLevelNodeAttributes[level+1]=multiLevelGraph[level+1].newNodeArray();
		for (var vHigh : multiLevelGraph[level + 1].nodes()) {
			var na = multiLevelNodeAttributes[level + 1].get(vHigh);
			var vAct = na.getLowerLevelNode();
			multiLevelNodeAttributes[level].get(vAct).setPosition(na.getPosition());
			multiLevelNodeAttributes[level].get(vAct).placed();
		}
	}

	private static void setInitialPositionsOfPlanetAndModeNodes(int level, FastMultiLayerMethodOptions options, Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes, ArrayList<Node> pmNodes) {
		final var list = new ArrayList<DPoint>();

		createAllPlacementSectors(multiLevelGraph, multiLevelNodeAttributes, multiLevelEdgeAttributes, level);

		for (var v : multiLevelGraph[level].nodes()) {
			var va = multiLevelNodeAttributes[level].get(v);
			var nodeType = va.getType();
			if (nodeType == NodeAttributes.Type.PlanetWithMoons) {
				pmNodes.add(v);
			} else {
				list.clear();
				var dedicatedSunPosition = multiLevelNodeAttributes[level].get(va.getDedicatedSunNode()).getPosition();

				if (options.getInitialPlacementMult() == FastMultiLayerMethodOptions.InitialPlacementMultiLayer.Advanced) {
					for (var e : v.adjacentEdges()) {
						var adj = e.getOpposite(v);
						var aa = multiLevelNodeAttributes[level].get(adj);

						if (va.getDedicatedSunNode() == aa.getDedicatedSunNode() && aa.getType() != NodeAttributes.Type.Sun && aa.isPlaced()) {
							var newPosition = calculatePosition(dedicatedSunPosition, aa.getPosition(),
									va.getDedicatedSunDistance(), multiLevelEdgeAttributes[level].get(e).getLength());
							list.add(newPosition);
						}
					}
				}
				if (va.getLambdas().size() == 0) {
					if (list.size() == 0) {
						var newPosition = createRandomPosition(dedicatedSunPosition, va.getDedicatedSunDistance(), va.getAngle1(), va.getAngle2());
						list.add(newPosition);
					}
				} else {
					var lambdaPos = 0;

					for (var adjSun : va.getNeighborSunNodes()) {
						var lambda = va.getLambdas().get(lambdaPos);
						var adjSunPosition = multiLevelNodeAttributes[level].get(adjSun).getPosition();
						var newPosition = getWaggledInbetweenPosition(dedicatedSunPosition, adjSunPosition, lambda);
						list.add(newPosition);
						lambdaPos = (lambdaPos + 1 < va.getLambdas().size() ? lambdaPos + 1 : 0);
					}
				}
				va.setPosition(DPoint.computeBarycenter(list));
				va.placed();
			}
		}
	}

	private static void createAllPlacementSectors(Graph[] multiLevelGraph, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes, int level) {
		var adjPositions = new ArrayList<DPoint>();

		for (var vHigh : multiLevelGraph[level + 1].nodes()) {
			var vha = multiLevelNodeAttributes[level + 1].get(vHigh);
			adjPositions.clear();

			var vHighPosition = vha.getPosition();
			for (var eHigh : vHigh.adjacentEdges()) {
				if (multiLevelEdgeAttributes[level + 1].get(eHigh).isExtraEdge()) {
					var wHigh = eHigh.getOpposite(vHigh);
					var wa = multiLevelNodeAttributes[level + 1].get(wHigh);
					var wHighPosition = new DPoint(wa.getX(), wa.getY());
					adjPositions.add(wHighPosition);
				}
			}
			double angle_1;
			double angle_2;
			if (adjPositions.size() == 0) {
				angle_1 = 0;
				angle_2 = 6.2831853;
			} else if (adjPositions.size() == 1) //special case
			{
				//create angle_1
				var start_pos = adjPositions.get(0);
				var xParallelPosition = new DPoint(vHighPosition.getX() + 1, vHighPosition.getY());
				angle_1 = DPoint.angle(vHighPosition, xParallelPosition, start_pos);
				//create angle_2
				angle_2 = angle_1 + Math.PI;
			} else { //usual case
				int MAX = 10; //the biggest of at most MAX random selected sectors is choosen
				int steps = 1;
				var i = 0;

				angle_1 = 0;
				angle_2 = 0;
				double act_angle_1;
				double act_angle_2;

				do {
					//create act_angle_1
					var startPos = adjPositions.get(i++);
					var xParallelPosition = new DPoint(vHighPosition.getX() + 1, vHighPosition.getY());
					act_angle_1 = DPoint.angle(vHighPosition, xParallelPosition, startPos);
					//create act_angle_2
					boolean first_angle = true;
					double min_next_angle = 0;

					for (int j = 0; j < adjPositions.size(); j++) {
						var nextAngle = DPoint.angle(vHighPosition, startPos, adjPositions.get(j));
						if (j != i && (first_angle || nextAngle < min_next_angle)) {
							min_next_angle = nextAngle;
							first_angle = false;
						}
					}
					act_angle_2 = act_angle_1 + min_next_angle;
					if (i == 0 || ((act_angle_2 - act_angle_1) > (angle_2 - angle_1))) {
						angle_1 = act_angle_1;
						angle_2 = act_angle_2;
					}
					i++;
					steps++;
				}
				while ((steps <= MAX) && i < adjPositions.size());

				if (angle_1 == angle_2)
					angle_2 = angle_1 + Math.PI;
			}

			var sunNode = vha.getLowerLevelNode();
			multiLevelNodeAttributes[level].get(sunNode).setAngle1(angle_1);
			multiLevelNodeAttributes[level].get(sunNode).setAngle2(angle_2);
		} // for all nodes

		//import the angle values from the values of the dedicated sun nodes
		for (var v : multiLevelGraph[level].nodes()) {
			var va = multiLevelNodeAttributes[level].get(v);
			var dedicatedSun = va.getDedicatedSunNode();
			va.setAngle1(multiLevelNodeAttributes[level].get(dedicatedSun).getAngle1());
			va.setAngle2(multiLevelNodeAttributes[level].get(dedicatedSun).getAngle2());
		}
	}

	private static DPoint calculatePosition(DPoint s, DPoint t, double dist_s, double dist_t) {
		var dist_st = s.distance(t);
		var lambda = (dist_s + (dist_st - dist_s - dist_t) / 2) / dist_st;

		if (Double.isNaN(lambda))
			System.err.println("NaN");
		return getWaggledInbetweenPosition(s, t, lambda);
	}

	private static DPoint getWaggledInbetweenPosition(DPoint s, DPoint t, double lambda) {
		final var WAGGLEFACTOR = 0.05;
		var inbetweenPoint = new DPoint(s.getX() + lambda * (t.getX() - s.getX()), s.getY() + lambda * (t.getY() - s.getY()));
		var dist_st = Math.sqrt((s.getX() - t.getX()) * (s.getX() - t.getX()) + (s.getY() - t.getY()) * (s.getY() - t.getY()));
		var radius = WAGGLEFACTOR * dist_st;
		var rand_radius = radius * random.nextDouble();
		return createRandomPosition(inbetweenPoint, rand_radius, 0, 2 * Math.PI);
	}

	private static DPoint createRandomPosition(DPoint center, double radius, double angle1, double angle2) {
		var rnd_angle = angle1 + (angle2 - angle1) * random.nextDouble();
		var dx = Math.cos(rnd_angle) * radius;
		var dy = Math.sin(rnd_angle) * radius;
		return new DPoint(center.getX() + dx, center.getY() + dy);
	}

	private static void setInitialPositionsOfPMNodes(int level, FastMultiLayerMethodOptions options, NodeArray<NodeAttributes>[] multiLevelNodeAttributes, EdgeArray<EdgeAttributes>[] multiLevelEdgeAttributes, ArrayList<Node> pmNodes) {
		var list = new ArrayList<DPoint>();

		for (var v : pmNodes) {
			list.clear();
			var va = multiLevelNodeAttributes[level].get(v);

			var sunNode = va.getDedicatedSunNode();
			var sunDist = va.getDedicatedSunDistance();
			var sunPos = multiLevelNodeAttributes[level].get(sunNode).getPosition();

			if (options.getInitialPlacementMult() == FastMultiLayerMethodOptions.InitialPlacementMultiLayer.Advanced) {
				for (var e : v.adjacentEdges()) {
					var adj = e.getOpposite(v);
					var aa = multiLevelNodeAttributes[level].get(adj);
					var ea = multiLevelEdgeAttributes[level].get(e);

					if (!ea.isMoonEdge() && va.getDedicatedSunNode() == aa.getDedicatedSunNode() && aa.getType() != NodeAttributes.Type.Sun && aa.isPlaced()) {
						var newPosition = calculatePosition(sunPos, aa.getPosition(), sunDist, ea.getLength());
						list.add(newPosition);
					}
				}
			}
			for (var moon : va.getMoons()) {
				var ma = multiLevelNodeAttributes[level].get(moon);
				var moonPos = ma.getPosition();
				var moonDist = ma.getDedicatedSunDistance();
				var lambda = sunDist / moonDist;
				var newPosition = getWaggledInbetweenPosition(sunPos, moonPos, lambda);
				list.add(newPosition);
			}

			if (va.getLambdas().size() > 0) {
				int i = 0;
				for (var adjSun : va.getNeighborSunNodes()) {
					var lambda = va.getLambdas().get(i);
					var adjSunPos = multiLevelNodeAttributes[level].get(adjSun).getPosition();
					var newPosition = getWaggledInbetweenPosition(sunPos, adjSunPos, lambda);
					list.add(newPosition);
					if (i + 1 < va.getLambdas().size() - 1)
						i++;
				}
			}
			va.setPosition(DPoint.computeBarycenter(list));
			va.placed();
		}
	}
}
