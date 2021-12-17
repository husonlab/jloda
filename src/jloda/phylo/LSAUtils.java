/*
 *  LSAUtils.java Copyright (C) 2021 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.phylo;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.util.IteratorUtils;

import java.util.ArrayList;
import java.util.LinkedList;
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
		if (tree.isRootedNetwork()) {
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
				var toDelete = v.outEdgesStream(false).filter(e -> !edges.contains(e)).collect(Collectors.toList());
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
		if (!tree.isRootedNetwork())
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
		if (!tree.isRootedNetwork())
			tree.postorderTraversal(v, method);
		else {
			for (var w : tree.lsaChildren(v)) {
				var e = v.getEdgeTo(w);
				if (e == null || !tree.isSpecial(e))
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
}
