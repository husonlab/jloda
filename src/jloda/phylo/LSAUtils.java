/*
 * LSAUtils.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.phylo;

import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.util.IteratorUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * some utilities for working with the LSA tree associated with a rooted network
 * Daniel Huson, 12.2021
 */
public class LSAUtils {
	/**
	 * extracts the LSA tree from a rooted network
	 *
	 * @param tree the network
	 * @return lsa tree
	 */
	public static PhyloTree getLSATree(PhyloTree tree) {
		if (tree.isReticulated()) {
			var lsaTree = new PhyloTree(tree);
			for (var v : lsaTree.nodes()) {
				var lsaChildren = IteratorUtils.asList(lsaTree.lsaChildren(v));
				var edges = new ArrayList<Edge>();
				for (var w : lsaChildren) {
					if (v.getEdgeTo(w) == null) {
						var e = lsaTree.newEdge(v, w);
						lsaTree.setWeight(e, 1);
						edges.add(e);
					} else
						edges.add(v.getEdgeTo(w));
				}
				var toDelete = v.outEdgesStream(false).filter(e -> !edges.contains(e)).toList();
				toDelete.forEach(lsaTree::deleteEdge);
				v.rearrangeAdjacentEdges(edges);
			}
			return lsaTree;
		} else
			return tree;
	}

	/**
	 * performs a pre-order traversal at node v using the LSA tree, if defined. Makes sure that a node is visited only once
	 * its parents both in the LSA and in the network have been visited
	 *
	 * @param tree   the tree or network (with embedded LSA tree)
	 * @param v      the root node
	 * @param method method to apply
	 */
	public static void preorderTraversalLSA(PhyloTree tree, Node v, Consumer<Node> method) {
		if (!tree.isReticulated())
			tree.preorderTraversal(v, method);
		else {
			try (var visited = tree.newNodeSet()) {
				var queue = new LinkedList<Node>();
				queue.add(v);
				while (queue.size() > 0) {
					v = queue.pop();
					if (visited.containsAll(IteratorUtils.asList(v.parents()))) {
						method.accept(v);
						visited.add(v);
						tree.lsaChildren(v).forEach(queue::add);
					} else
						queue.add(v);
				}
			}
		}
	}

	/**
	 * performs a post-order traversal at node v, using the LSA tree, if defined
	 *
	 * @param tree   the tree or network
	 * @param v      the root node
	 * @param method method to apply
	 */
	public static void postorderTraversalLSA(PhyloTree tree, Node v, Consumer<Node> method) {
		if (!tree.isReticulated())
			tree.postorderTraversal(v, method);
		else {
			for (var w : tree.lsaChildren(v)) {
				postorderTraversalLSA(tree, w, method);
			}
			method.accept(v);
		}
	}

	public static void breathFirstTraversalLSA(PhyloTree tree, Node v, int level, BiConsumer<Integer, Node> method) {
		method.accept(level, v);
		for (var w : tree.lsaChildren(v)) {
			breathFirstTraversalLSA(tree, w, level + 1, method);
		}
	}

	/**
	 * given a reticulate network, returns a mapping of each LSA node to its children in the LSA tree
	 *
	 * @param tree             the tree
	 * @param reticulation2LSA is returned here
	 */
	public static NodeArray<List<Node>> computeLSAChildrenMap(PhyloTree tree, NodeArray<Node> reticulation2LSA) {
		final NodeArray<List<Node>> lsaChildrenMap = tree.newNodeArray();
		tree.getLSAChildrenMap().clear();

		if (tree.getRoot() != null) {
			// first we compute the reticulate node to lsa node mapping:
			computeReticulation2LSA(tree, reticulation2LSA);

			for (var v : tree.nodes()) {
				var children = v.outEdgesStream(false).filter(e -> !tree.isReticulateEdge(e))
						.map(Edge::getTarget).collect(Collectors.toList());
				tree.getLSAChildrenMap().put(v, children);
			}
			for (var v : tree.nodes()) {
				var lsa = reticulation2LSA.get(v);
				if (lsa != null)
					tree.getLSAChildrenMap().get(lsa).add(v);
			}
		}
		return lsaChildrenMap;
	}

	/**
	 * compute the reticulation-to-lsa mapping
	 *
	 * @param tree             the rooted network
	 * @param reticulation2LSA the reticulation to LSA mapping
	 */
	private static void computeReticulation2LSA(PhyloTree tree, NodeArray<Node> reticulation2LSA) {
		reticulation2LSA.clear();

		try (NodeArray<BitSet> ret2PathSet = tree.newNodeArray(); NodeArray<EdgeArray<BitSet>> ret2Edge2PathSet = tree.newNodeArray();
			 NodeArray<Set<Node>> node2below = tree.newNodeArray()) {
			computeReticulation2LSARec(tree, tree.getRoot(), ret2PathSet, ret2Edge2PathSet, node2below, reticulation2LSA);
		}
	}

	/**
	 * recursively compute the mapping of reticulate nodes to their lsa nodes
	 */
	private static void computeReticulation2LSARec(PhyloTree tree, Node v, NodeArray<BitSet> ret2PathSet, NodeArray<EdgeArray<BitSet>> ret2Edge2PathSet,
												   NodeArray<Set<Node>> node2below, NodeArray<Node> reticulation2LSA) {
		if (v.getInDegree() > 1) // this is a reticulate node, add paths to node and incoming edges
		{
			// setup new paths for this node:
			try (EdgeArray<BitSet> edge2PathSet = tree.newEdgeArray()) {
				ret2Edge2PathSet.put(v, edge2PathSet);
				var pathsForR = new BitSet();
				ret2PathSet.put(v, pathsForR);
				//  assign a different path number to each in-edge:
				var pathNum = 0;
				for (var e : v.inEdges()) {
					pathNum++;
					pathsForR.set(pathNum);
					var pathsForEdge = new BitSet();
					pathsForEdge.set(pathNum);
					edge2PathSet.put(e, pathsForEdge);
				}
			}
		}

		var reticulationsBelow = new HashSet<Node>(); // set of all reticulate nodes below v
		node2below.put(v, reticulationsBelow);

		// visit all children and determine all reticulations below this node
		for (var w : v.children()) {
			if (node2below.get(w) == null) // if we haven't processed child yet, do it:
				computeReticulation2LSARec(tree, w, ret2PathSet, ret2Edge2PathSet, node2below, reticulation2LSA);
			reticulationsBelow.addAll(node2below.get(w));
			if (w.getInDegree() > 1)
				reticulationsBelow.add(w);
		}

		// check whether this is the lsa for any of the reticulations below v
		// look at all reticulations below v:
		var toDelete = new ArrayList<Node>();
		for (var r : reticulationsBelow) {
			// determine which paths from the reticulation lead to this node
			var edge2PathSet = ret2Edge2PathSet.get(r);
			var paths = new BitSet();
			for (var f : v.outEdges()) {
				var eSet = edge2PathSet.get(f);
				if (eSet != null)
					paths.or(eSet);

			}
			var alive = ret2PathSet.get(r);
			if (paths.equals(alive)) // if the set of paths equals all alive paths, v is lsa of r
			{
				reticulation2LSA.put(r, v);
				toDelete.add(r); // we don't need to consider this reticulation any more
			}
		}
		// don't need to consider reticulations for which lsa has been found:
		for (var u : toDelete)
			reticulationsBelow.remove(u);

		// all paths are pulled up the first in-edge
		if (v.getInDegree() >= 1) {
			for (var r : reticulationsBelow) {
				// determine which paths from the reticulation lead to this node
				var edge2PathSet = ret2Edge2PathSet.get(r);

				var newSet = new BitSet();
				for (Edge e : v.outEdges()) {
					var pathSet = edge2PathSet.get(e);
					if (pathSet != null)
						newSet.or(pathSet);
				}
				edge2PathSet.put(v.getFirstInEdge(), newSet);
			}
		}
		// open new paths on all additional in-edges:
		if (v.getInDegree() >= 2) {
			for (var r : reticulationsBelow) {
				var existingPathsForR = ret2PathSet.get(r);

				var edge2PathSet = ret2Edge2PathSet.get(r);
				// start with the second in edge:
				var first = true;
				for (var e : v.inEdges()) {
					if (first)
						first = false;
					else {
						var pathsForEdge = new BitSet();
						var pathNum = existingPathsForR.nextClearBit(1);
						existingPathsForR.set(pathNum);
						pathsForEdge.set(pathNum);
						edge2PathSet.put(e, pathsForEdge);
					}
				}
			}
		}
	}
}
