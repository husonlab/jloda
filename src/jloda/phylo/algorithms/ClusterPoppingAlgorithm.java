/*
 *   ClusterPoppingAlgorithm.java Copyright (C) 2022 Daniel H. Huson
 *
 *   (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.phylo.algorithms;

import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;
import jloda.util.BitSetUtils;
import jloda.util.IteratorUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * runs the cluster popping algorithm to create a rooted tree or network from a set of clusters
 * Daniel Huson, 8.2022
 */
public class ClusterPoppingAlgorithm {
	/**
	 * runs the cluster popping algorithm to create a rooted tree or network from a set of clusters
	 *
	 * @param clusters0      input clusters
	 * @param weightFunction weights for clusters
	 * @param network        the resulting network
	 */
	public static void apply(Collection<BitSet> clusters0, Function<BitSet, Double> weightFunction, PhyloTree network) {
		network.clear();

		if (clusters0.size() > 0) {
			var clusters = new ArrayList<>(clusters0);
			clusters.sort((a, b) -> -Integer.compare(a.cardinality(), b.cardinality()));

			var taxa = BitSetUtils.union(clusters);

			try (NodeArray<BitSet> nodeClusterMap = network.newNodeArray(); var visited = network.newNodeSet()) {
				network.setRoot(network.newNode());
				nodeClusterMap.put(network.getRoot(), taxa);

				for (var cluster : clusters) {
					var clusterNode = network.newNode();
					nodeClusterMap.put(clusterNode, cluster);
					visited.clear();

					if (network.getNumberOfNodes() > 1 || cluster.cardinality() < taxa.cardinality()) { // skip first cluster if it contains all taxa
						var stack = new Stack<Node>();
						stack.push(network.getRoot());
						while (stack.size() > 0) {
							var v = stack.pop();
							var isBelowAChild = false;
							for (var w : v.children()) {
								var clusterW = nodeClusterMap.get(w);
								if (BitSetUtils.contains(clusterW, cluster)) {
									isBelowAChild = true;
									if (!visited.contains(w))
										stack.push(w);
								}
							}
							if (!isBelowAChild)
								network.newEdge(v, clusterNode);
						}
					}
				}

				// make sure no node has indegree>1 and outdegree>1
				for (var v : network.nodeStream().filter(v -> v.getInDegree() > 1 && v.getOutDegree() > 1).collect(Collectors.toList())) {
					var above = network.newNode();
					for (var inEdge : IteratorUtils.asList(v.inEdges())) {
						network.newEdge(inEdge.getSource(), above);
						network.deleteEdge(inEdge);
					}
					network.newEdge(above, v);
				}

				// make sure we have all leaf edges:
				for (var v : IteratorUtils.asList(network.leaves())) {
					var cluster = nodeClusterMap.get(v);
					if (cluster.cardinality() == 1) {
						network.addTaxon(v, cluster.nextSetBit(1));
					} else {
						for (var t : BitSetUtils.members(cluster)) {
							var w = network.newNode();
							network.newEdge(v, w);
							network.addTaxon(w, t);
						}
					}
				}

				// set edge weights:
				network.nodeStream().filter(v -> v.getInDegree() == 1)
						.forEach(v -> network.setWeight(v.getFirstInEdge(), weightFunction.apply(nodeClusterMap.get(v))));
			}
		}
	}
}
