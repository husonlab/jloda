/*
 *  PQTree.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph.algorithms;

import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * implements the PQ-tree tree for consecutive ones (Booth and Lueker, 1976)
 * This is not linear time, however, as we use a full tree implementation, including all parent nodes
 */
public class PQTree {
	private static boolean verbose = false;

	private enum Type {P, Q, Leaf}

	private enum State {Full, Empty, Partial, DoublyPartial, PertinentRoot}

	private final BitSet all;
	private final PhyloTree tree;
	private final Map<Integer, Node> leafMap;

	private final Set<String> reductionsUsed = new TreeSet<>();

	/**
	 * constructor
	 */
	public PQTree() {
		leafMap = new HashMap<>();
		this.all = new BitSet();
		tree = new PhyloTree();
		var root = tree.newNode();
		setType(root, Type.P);
		tree.setRoot(root);
	}

	/**
	 * attempts to add the given cluster to the PQ-tree.
	 *
	 * @param set cluster
	 * @return true, if successfully added, false, if set not compatible with current PQ-tree
	 * @throws IllegalArgumentException if given set not contained in set of all elements represented by this PQ-tree
	 */
	public boolean accept(BitSet set) {
		if (set.cardinality() <= 1)
			return true;

		for (var t : BitSetUtils.members(set)) {
			if (!leafMap.containsKey(t)) {
				var v = tree.newNode();
				tree.addTaxon(v, t);
				setType(v, Type.Leaf);
				tree.newEdge(tree.getRoot(), v);
				all.set(t);
				leafMap.put(t, v);
			}
		}

		if (verbose) {
			System.err.println("======= set: " + StringUtils.toString(set));
			System.err.println("Tree: " + toBracketString());
		}

		if (!BitSetUtils.contains(all, set))
			throw new IllegalArgumentException("set not contained");

		// todo: copy only the pertinent tree
		var treeCopy = new PhyloTree(); // keep a copy in case we have to roll-back
		var leafMapCopy = new HashMap<Integer, Node>();
		{
			var oldNewMap = treeCopy.copy(tree);
			for (var entru : leafMap.entrySet()) {
				leafMapCopy.put(entru.getKey(), oldNewMap.get(entru.getValue()));
			}
		}

		var ok = new Single<>(true);
		var changed = new Single<>(false);

		try {
			final var belowMap = new HashMap<Node, Integer>();
			final var pertinentRoot = computePertinentRoot(set, belowMap);
			final var stateMap = new HashMap<Node, State>();

			tree.postorderTraversal(pertinentRoot,
					v -> ok.get() && belowMap.containsKey(v),
					v -> {
						if (ok.get()) { // need to check, as well
							if (verbose)
								System.err.println("v=" + (v == tree.getRoot() ? "root" : toBracketString(v)));

							if (isLeaf(v)) {
								reduceLeaf(v, stateMap);
							} else if (isP0(v, stateMap)) {
								reduceP0(v, stateMap);
							} else if (isP1(v, stateMap)) {
								reduceP1(v, stateMap);
							} else if (isP2(v, pertinentRoot, stateMap)) {
								reduceP2(v, stateMap);
								changed.set(true);
							} else if (isP3(v, pertinentRoot, stateMap)) {
								reduceP3(v, stateMap);
								changed.set(true);
							} else if (isP4_0(v, pertinentRoot, stateMap)) {
								reduceP4_0(v, stateMap);
								changed.set(true);
							} else if (isP4(v, pertinentRoot, stateMap)) {
								reduceP4(v, stateMap);
							} else if (isP5(v, pertinentRoot, stateMap)) {
								if (StringUtils.toString(set).equals("95,96"))
									System.err.println(set);

								reduceP5(v, stateMap);
								changed.set(true);
							} else if (isP6(v, pertinentRoot, stateMap)) {
								reduceP6(v, stateMap);
								changed.set(true);
							} else if (isQ0(v, stateMap)) {
								reduceQ0(v, stateMap);
								changed.set(true);
							} else if (isQ1(v, stateMap)) {
								reduceQ1(v, stateMap);
								changed.set(true);
							} else if (isQ2_0(v, stateMap)) {
								reduceQ2_0(v, stateMap);
								changed.set(true);
							} else if (isQ2_1(v, stateMap)) {
								reduceQ2_1(v, stateMap);
								changed.set(true);
							} else if (isQ3_0(v, pertinentRoot, stateMap)) {
								reduceQ3_0(v, stateMap);
								changed.set(true);
							} else if (isQ3_1a(v, pertinentRoot, stateMap)) {
								reduceQ3_1a(v, stateMap);
								changed.set(true);
							} else if (isQ3_1b(v, pertinentRoot, stateMap)) {
								reduceQ3_1b(v, stateMap);
								changed.set(true);
							} else if (isQ3_2(v, pertinentRoot, stateMap)) {
								reduceQ3_2(v, stateMap);
								changed.set(true);
							} else {
								ok.set(false);
							}
							if (verbose && !isLeaf(v))
								System.err.println(" -> " + toBracketString(v));

							if (true) {
								if (!IsTree.apply(tree)) {
									System.err.println("Not tree!");
								}
							}
						}
					});
			if (verbose) {
				System.err.println("result: " + ok.get());
				System.err.println("order:  " + StringUtils.toString(extractAnOrdering(), ", "));
			}
			return ok.get();
		} finally {
			if (ok.get() && !check(set)) {
				System.err.println("set: " + StringUtils.toString(set));
				System.err.println("Algorithms reports ok, but is not ok");
			} else if (!ok.get() && check(set)) {
				System.err.println("set: " + StringUtils.toString(set));
				System.err.println("Algorithms reports not ok, but is ok");
			}
			if (!ok.get() && changed.get()) { // roll back
				var oldNewMap = tree.copy(treeCopy);
				leafMap.clear();
				for (var entru : leafMapCopy.entrySet()) {
					leafMap.put(entru.getKey(), oldNewMap.get(entru.getValue()));
				}
			}
		}
	}

	/**
	 * computes for each node, how many members are below it and determines the root of the pertinent tree
	 *
	 * @param set the current set
	 * @return pertinent tree
	 */
	private Node computePertinentRoot(BitSet set, Map<Node, Integer> belowMap) {
		Node pertinentRoot = null;

		for (var t : BitSetUtils.members(set)) {
			var v = leafMap.get(t);
			while (v != null) {
				var value = belowMap.getOrDefault(v, 0) + 1;
				belowMap.put(v, value);
				if (value < set.cardinality()) {
					v = v.getParent();
				} else {
					pertinentRoot = v;
					break;
				}
			}
		}

		if (pertinentRoot == null) {
			throw new RuntimeException("computePertinentRoot(): failed");
		}

		var v = pertinentRoot;
		while (v.getInDegree() > 0) {
			v = v.getParent();
			belowMap.remove(v);
		}

		if (verbose)
			System.err.println("Pertinent: " + (pertinentRoot == tree.getRoot() ? "root" : toBracketString(pertinentRoot)));
		return pertinentRoot;
	}

	private static boolean isLeaf(Node v) {
		return getType(v) == Type.Leaf;
	}

	private void reduceLeaf(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			//System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		stateMap.put(v, State.Full);
	}

	private static boolean isP0(Node v, HashMap<Node, State> stateMap) {
		return getType(v) == Type.P && getNonEmptyChildren(v, stateMap).size() == 0;
	}

	private void reduceP0(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		stateMap.put(v, State.Empty);

	}

	private static boolean isP1(Node v, HashMap<Node, State> stateMap) {
		return getType(v) == Type.P && getFullChildren(v, stateMap).size() == v.getOutDegree();
	}

	private void reduceP1(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		stateMap.put(v, State.Full);
	}

	private static boolean isP2(Node v, Node pertinentNode, HashMap<Node, State> stateMap) {
		return getType(v) == Type.P && v == pertinentNode
			   && hasFullChild(v, stateMap)
			   && hasEmptyChild(v, stateMap)
			   && !hasPartialChild(v, stateMap)
			   && !hasDoublePartialChild(v, stateMap);
	}

	private void reduceP2(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		var fullParent = tree.newNode();
		setType(fullParent, Type.P);
		var fullChildren = getFullChildren(v, stateMap);
		for (var child : fullChildren) {
			tree.deleteEdge(child.getFirstInEdge());
			tree.newEdge(fullParent, child);
		}
		tree.newEdge(v, fullParent);
		stateMap.put(fullParent, State.Full);
		// v is pertinent root, no need to set its state
	}

	private static boolean isP3(Node v, Node pertinentNode, HashMap<Node, State> stateMap) {
		return getType(v) == Type.P && v != pertinentNode
			   && hasFullChild(v, stateMap)
			   && hasEmptyChild(v, stateMap)
			   && !hasPartialChild(v, stateMap)
			   && !hasDoublePartialChild(v, stateMap);
	}

	private void reduceP3(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		var fullChildren = getFullChildren(v, stateMap);
		Node fullParent;
		if (fullChildren.size() == 1) {
			fullParent = fullChildren.get(0);
		} else {
			fullParent = tree.newNode();
			setType(fullParent, Type.P);
			for (var child : fullChildren) {
				tree.newEdge(fullParent, child);
			}
			stateMap.put(fullParent, State.Full);
		}
		var emptyChildren = getEmptyChildren(v, stateMap);
		Node emptyParent;
		if (emptyChildren.size() == 1) {
			emptyParent = emptyChildren.get(0);
		} else {
			emptyParent = tree.newNode();
			setType(emptyParent, Type.P);
			for (var child : emptyChildren) {
				tree.newEdge(emptyParent, child);
			}
			stateMap.put(emptyParent, State.Empty);
		}
		var list = List.of(emptyParent, fullParent);
		replaceChildren(v, list);
		stateMap.put(v, State.Partial);
		setType(v, Type.Q);
	}

	private static boolean isP4_0(Node v, Node pertinentNode, HashMap<Node, State> stateMap) {
		return getType(v) == Type.P && v == pertinentNode && getEmptyChildren(v, stateMap).size() == 0
			   && hasFullChild(v, stateMap)
			   && getPartialChildren(v, stateMap).size() == 1
			   && !hasDoublePartialChild(v, stateMap);
	}

	private void reduceP4_0(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}

		Node partialChild = null;
		var fullChildren = new ArrayList<Node>();
		for (var child : v.children()) {
			if (stateMap.get(child) == State.Partial)
				partialChild = child;
			else
				fullChildren.add(child);
		}

		var list = new ArrayList<Node>();

		assert (partialChild != null);

		var below = IteratorUtils.asList(partialChild.children());
		if (isEmpty(below.get(0), stateMap))
			list.addAll(below);
		else
			list.addAll(CollectionUtils.reverse(below));
		tree.deleteNode(partialChild);

		if (fullChildren.size() == 1)
			list.add(fullChildren.get(0));
		else if (fullChildren.size() > 1) {
			var full = tree.newNode();
			setType(full, Type.P);
			stateMap.put(full, State.Full);
			for (var child : fullChildren) {
				tree.newEdge(full, child);
			}
			list.add(full);
		}
		replaceChildren(v, list);

		stateMap.put(v, State.Partial);
		setType(v, Type.Q);
	}

	private static boolean isP4(Node v, Node pertinentNode, HashMap<Node, State> stateMap) {
		return getType(v) == Type.P && v == pertinentNode && getPartialChildren(v, stateMap).size() == 1
			   && !hasDoublePartialChild(v, stateMap);

	}

	private void reduceP4(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		var partialChild = getPartialChildren(v, stateMap).get(0);
		var fullChildren = getFullChildren(v, stateMap);

		var list = new ArrayList<>(getEmptyChildren(v, stateMap));
		list.add(partialChild);

		if (fullChildren.size() > 0) {
			Node full;
			if (fullChildren.size() == 1) {
				full = fullChildren.get(0);
			} else {
				full = tree.newNode();
				setType(full, Type.P);
				for (var child : fullChildren) {
					tree.newEdge(full, child);
				}
				stateMap.put(full, State.Full);
			}
			var below = IteratorUtils.asList(partialChild.children());
			if (!isEmpty(below.get(0), stateMap)) {
				CollectionUtils.reverseInPlace(below);
			}
			below.add(full);
			replaceChildren(partialChild, below);
		}
		replaceChildren(v, list);
		stateMap.put(partialChild, State.Partial);
		stateMap.put(v, State.Partial);
	}

	private static boolean isP5(Node v, Node pertinentNode, HashMap<Node, State> stateMap) {
		return getType(v) == Type.P && v != pertinentNode && getPartialChildren(v, stateMap).size() == 1
			   && !hasDoublePartialChild(v, stateMap);
	}

	private void reduceP5(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		var partialChild = getPartialChildren(v, stateMap).get(0);
		var emptyChildren = getEmptyChildren(v, stateMap);
		var fullChildren = getFullChildren(v, stateMap);

		var list = new ArrayList<Node>();

		if (emptyChildren.size() > 0) {
			Node empty;
			if (emptyChildren.size() == 1) {
				empty = emptyChildren.get(0);
				tree.deleteEdge(empty.getFirstInEdge());
			} else {
				empty = tree.newNode();
				setType(empty, Type.P);
				for (var child : emptyChildren) {
					tree.deleteEdge(child.getFirstInEdge());
					tree.newEdge(empty, child);
				}
				stateMap.put(empty, State.Empty);
			}
			list.add(empty);
		}

		if (getType(partialChild) == Type.P) {
			list.addAll(getEmptyChildren(partialChild, stateMap));
			list.addAll(getFullChildren(partialChild, stateMap));
		} else {
			if (stateMap.get(partialChild.getFirstOutEdge().getTarget()) == State.Full)
				list.addAll(CollectionUtils.reverse(IteratorUtils.asList(partialChild.children())));
			else
				list.addAll(IteratorUtils.asList(partialChild.children()));
		}

		tree.deleteNode(partialChild);

		if (fullChildren.size() > 0) {
			Node full;
			if (fullChildren.size() == 1) {
				full = fullChildren.get(0);
				tree.deleteEdge(full.getFirstInEdge());
			} else {
				full = tree.newNode();
				setType(full, Type.P);
				for (var child : fullChildren) {
					tree.deleteEdge(child.getFirstInEdge());
					tree.newEdge(full, child);
				}
				stateMap.put(full, State.Full);
			}
			list.add(full);
		}
		replaceChildren(v, list);
		setType(v, Type.Q);
		stateMap.put(v, State.Partial);
	}

	private static boolean isP6(Node v, Node pertinentNode, HashMap<Node, State> stateMap) {
		return getType(v) == Type.P && v == pertinentNode && getPartialChildren(v, stateMap).size() == 2
			   && !hasDoublePartialChild(v, stateMap);
	}

	private void reduceP6(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}

		var list = new ArrayList<>(getEmptyChildren(v, stateMap));

		var partialChildren = getPartialChildren(v, stateMap);
		var partialChild1 = partialChildren.get(0);

		var below = new ArrayList<Node>();
		if (getType(partialChild1) == Type.P) {
			below.addAll(getEmptyChildren(partialChild1, stateMap));
			below.addAll(getFullChildren(partialChild1, stateMap));
		} else {
			if (stateMap.get(partialChild1.getFirstOutEdge().getTarget()) == State.Full)
				below.addAll(CollectionUtils.reverse(IteratorUtils.asList(partialChild1.children())));
			else
				below.addAll(IteratorUtils.asList(partialChild1.children()));
		}
		tree.deleteNode(partialChild1);

		var fullChildren = getFullChildren(v, stateMap);
		if (fullChildren.size() == 1) {
			var full = fullChildren.get(0);
			tree.deleteEdge(full.getFirstInEdge());
			below.add(full);
		} else if (fullChildren.size() > 1) {
			var full = tree.newNode();
			setType(full, Type.P);
			for (var child : fullChildren) {
				tree.deleteEdge(child.getFirstInEdge());
				tree.newEdge(full, child);
			}
			stateMap.put(full, State.Full);
			below.add(full);
		}

		var partialChild2 = partialChildren.get(1);
		if (getType(partialChild2) == Type.P) {
			below.addAll(getFullChildren(partialChild2, stateMap));
			below.addAll(getEmptyChildren(partialChild2, stateMap));
		} else {
			if (stateMap.get(partialChild2.getFirstOutEdge().getTarget()) == State.Full)
				below.addAll(IteratorUtils.asList(partialChild2.children()));
			else
				below.addAll(CollectionUtils.reverse(IteratorUtils.asList(partialChild2.children())));
		}
		tree.deleteNode(partialChild2);

		if (list.size() == 0) {
			replaceChildren(v, below);
			setType(v, Type.Q);
		} else {
			var doublyPartialChild = tree.newNode();
			below.forEach(w -> tree.newEdge(doublyPartialChild, w));
			setType(doublyPartialChild, Type.Q);
			stateMap.put(doublyPartialChild, State.DoublyPartial);
			list.add(doublyPartialChild);
			replaceChildren(v, list);
		}
		stateMap.put(v, State.DoublyPartial);
		// is pertinent root, no need to set state
	}

	private static boolean isQ0(Node v, HashMap<Node, State> stateMap) {
		return getType(v) == Type.Q && getNonEmptyChildren(v, stateMap).size() == 0;
	}

	private void reduceQ0(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		stateMap.put(v, State.Empty);
	}

	private static boolean isQ1(Node v, HashMap<Node, State> stateMap) {
		return getType(v) == Type.Q && getFullChildren(v, stateMap).size() == v.getOutDegree();
	}

	private void reduceQ1(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		stateMap.put(v, State.Full);
	}

	private static boolean isQ2_0(Node v, HashMap<Node, State> stateMap) {
		if (getType(v) != Type.Q)
			return false;
		if (hasPartialChild(v, stateMap) || hasDoublePartialChild(v, stateMap) ||
			!hasEmptyChild(v, stateMap) || !hasFullChild(v, stateMap))
			return false;

		var emptyFirst = isEmpty(v.getFirstOutEdge().getTarget(), stateMap);
		var after = false;
		for (var child : v.children()) {
			if (isEmpty(child, stateMap) == emptyFirst) {
				if (after)
					return false;
			} else {
				after = true;
			}
		}
		return true;
	}

	private void reduceQ2_0(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		stateMap.put(v, State.Partial);
	}

	private static boolean isQ2_1(Node v, HashMap<Node, State> stateMap) {
		if (getType(v) != Type.Q)
			return false;
		if (hasDoublePartialChild(v, stateMap))
			return false;
		var partialChildren = getPartialChildren(v, stateMap);
		if (partialChildren.size() != 1)
			return false;
		var partialChild = partialChildren.get(0);
		var after = false;

		var emptyFirst = isEmpty(v.getFirstOutEdge().getTarget(), stateMap);

		if (emptyFirst) {
			for (var child : v.children()) {
				if (isEmpty(child, stateMap)) {
					if (after)
						return false;
				} else {
					if (child == partialChild)
						after = true;
					if (!after)
						return false;
				}
			}
		} else { // empty last
			for (var child : v.children()) {
				if (isEmpty(child, stateMap)) {
					if (!after)
						return false;
				} else {
					if (after)
						return false;
					if (child == partialChild)
						after = true;
				}
			}
		}
		return true;
	}

	private void reduceQ2_1(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}

		var partialChild = getPartialChildren(v, stateMap).get(0);

		var list = new ArrayList<Node>();

		var emptyFirst = (isEmpty(v.getFirstOutEdge().getTarget(), stateMap));
		for (var child : v.children()) {
			if (child == partialChild) {
				var below = IteratorUtils.asList(child.children());
				if (emptyFirst == isEmpty(below.get(0), stateMap))
					list.addAll(below);
				else
					list.addAll(CollectionUtils.reverse(below));
			} else {
				list.add(child);
			}
		}
		tree.deleteNode(partialChild);
		replaceChildren(v, list);
		if (isEmpty(list.get(0), stateMap) != isEmpty(list.get(list.size() - 1), stateMap))
			stateMap.put(v, State.Partial);
		else
			stateMap.put(v, State.DoublyPartial);
	}

	private static boolean isQ3_0(Node v, Node pertinentRoot, HashMap<Node, State> stateMap) {
		if (getType(v) != Type.Q || v != pertinentRoot)
			return false;
		if (hasPartialChild(v, stateMap))
			return false;
		if (hasDoublePartialChild(v, stateMap))
			return false;

		var before = true;
		var during = false;
		var after = false;
		var hasEmptyBefore = false;
		var hasFullDuring = false;
		var hasEmptyAfter = false;

		for (var child : v.children()) {
			if (isEmpty(child, stateMap)) {
				if (before)
					hasEmptyBefore = true;
				if (during) {
					during = false;
					after = true;
				}
				if (after)
					hasEmptyAfter = true;
			} else {
				if (before) {
					before = false;
					during = true;
					hasFullDuring = true;
				} else if (after) {
					return false;
				}
			}
		}
		return hasEmptyBefore && hasFullDuring && hasEmptyAfter;
	}

	private void reduceQ3_0(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		stateMap.put(v, State.DoublyPartial);
	}

	private static boolean isQ3_1a(Node v, Node pertinentRoot, HashMap<Node, State> stateMap) {
		if (getType(v) != Type.Q || v != pertinentRoot)
			return false;
		if (hasDoublePartialChild(v, stateMap))
			return false;
		var partialChildren = getPartialChildren(v, stateMap);
		if (partialChildren.size() != 1)
			return false;
		var partialChild = partialChildren.get(0);
		var during = false;
		for (var child : v.children()) {
			if (child == partialChild) {
				during = true;
			}
			if (isEmpty(child, stateMap)) {
				if (during) {
					during = false;
				}
			} else {
				if (!during)
					return false;
			}
		}
		return true;
	}

	private void reduceQ3_1a(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		var partialChild = getPartialChildren(v, stateMap).get(0);
		var list = new ArrayList<Node>();
		for (var child : v.children()) {
			if (child != partialChild) {
				list.add(child);
			} else {
				var below = IteratorUtils.asList(child.children());
				if (isEmpty(below.get(0), stateMap)) {
					list.addAll(below);
				} else
					list.addAll(CollectionUtils.reverse(below));
			}
		}
		tree.deleteNode(partialChild);
		replaceChildren(v, list);
		stateMap.put(v, State.PertinentRoot);
	}

	private static boolean isQ3_1b(Node v, Node pertinentRoot, HashMap<Node, State> stateMap) {
		if (getType(v) != Type.Q || v != pertinentRoot)
			return false;
		if (hasDoublePartialChild(v, stateMap))
			return false;
		var partialChildren = getPartialChildren(v, stateMap);
		if (partialChildren.size() != 1)
			return false;
		var partialChild = partialChildren.get(0);
		var before = false;
		var during = false;
		var past = false;
		for (var child : v.children()) {
			if (isEmpty(child, stateMap)) {
				if (during) {
					return false;
				}
				if (!before)
					before = true;
			} else {
				if (!during && !past) {
					before = false;
					during = true;
				}
				if (past)
					return false;
			}
			if (child == partialChild) {
				past = true;
				during = false;
			}
		}
		return true;
	}

	private void reduceQ3_1b(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		var partialChildren = getPartialChildren(v, stateMap);
		var partialChild = partialChildren.get(0);
		var list = new ArrayList<Node>();

		for (var child : v.children()) {
			if (child != partialChild) {
				list.add(child);
			} else {
				var below = IteratorUtils.asList(child.children());
				if (isEmpty(below.get(0), stateMap)) {
					list.addAll(CollectionUtils.reverse(below));
				} else
					list.addAll(below);
			}
		}
		tree.deleteNode(partialChild);
		replaceChildren(v, list);
		stateMap.put(v, State.PertinentRoot);
	}


	private static boolean isQ3_2(Node v, Node pertinentRoot, HashMap<Node, State> stateMap) {
		if (getType(v) != Type.Q || v != pertinentRoot)
			return false;
		if (hasDoublePartialChild(v, stateMap))
			return false;
		var partialChildren = getPartialChildren(v, stateMap);
		if (partialChildren.size() != 2)
			return false;
		var partialChild1 = partialChildren.get(0);
		var partialChild2 = partialChildren.get(1);
		var between = false;
		for (var child : v.children()) {
			if (child == partialChild1)
				between = true;
			if (isEmpty(child, stateMap)) {
				if (between)
					return false;
			} else {
				if (!between)
					return false;
			}
			if (child == partialChild2)
				between = false;
		}
		return true;
	}

	private void reduceQ3_2(Node v, HashMap<Node, State> stateMap) {
		if (verbose) {
			System.err.print(getMethodName() + ": " + toBracketString(v));
			reductionsUsed.add(getMethodName());
		}
		var partialChildren = getPartialChildren(v, stateMap);
		var partialChild1 = partialChildren.get(0);
		var partialChild2 = partialChildren.get(1);

		var list = new ArrayList<Node>();
		for (var child : v.children()) {
			if (child == partialChild1) {
				var below = IteratorUtils.asList(child.children());
				if (isEmpty(below.get(0), stateMap)) {
					list.addAll(below);
				} else
					list.addAll(CollectionUtils.reverse(below));
			} else if (child == partialChild2) {
				var below = IteratorUtils.asList(child.children());
				if (isEmpty(below.get(0), stateMap)) {
					list.addAll(CollectionUtils.reverse(below));
				} else
					list.addAll(below);
			} else {
				list.add(child);
			}
		}
		tree.deleteNode(partialChild1);
		tree.deleteNode(partialChild2);
		replaceChildren(v, list);

		stateMap.put(v, State.PertinentRoot);
	}

	private static ArrayList<Node> getFullChildren(Node v, HashMap<Node, State> stateMap) {
		return v.childrenStream().filter(w -> stateMap.getOrDefault(w, State.Empty) == State.Full).collect(Collectors.toCollection(ArrayList::new));
	}

	private static boolean hasFullChild(Node v, HashMap<Node, State> stateMap) {
		return v.childrenStream().anyMatch(w -> stateMap.getOrDefault(w, State.Empty) == State.Full);
	}

	private static ArrayList<Node> getEmptyChildren(Node v, HashMap<Node, State> stateMap) {
		return v.childrenStream().filter(w -> stateMap.getOrDefault(w, State.Empty) == State.Empty).collect(Collectors.toCollection(ArrayList::new));
	}

	private static ArrayList<Node> getNonEmptyChildren(Node v, HashMap<Node, State> stateMap) {
		return v.childrenStream().filter(w -> stateMap.getOrDefault(w, State.Empty) != State.Empty).collect(Collectors.toCollection(ArrayList::new));
	}

	private static boolean hasEmptyChild(Node v, HashMap<Node, State> stateMap) {
		return v.childrenStream().anyMatch(w -> stateMap.getOrDefault(w, State.Empty) == State.Empty);
	}

	private static ArrayList<Node> getPartialChildren(Node v, HashMap<Node, State> stateMap) {
		return v.childrenStream().filter(w -> stateMap.getOrDefault(w, State.Empty) == State.Partial).collect(Collectors.toCollection(ArrayList::new));
	}

	private static boolean hasPartialChild(Node v, HashMap<Node, State> stateMap) {
		return v.childrenStream().anyMatch(w -> stateMap.getOrDefault(w, State.Empty) == State.Partial);
	}

	private static boolean hasDoublePartialChild(Node v, HashMap<Node, State> stateMap) {
		return v.childrenStream().anyMatch(w -> stateMap.getOrDefault(w, State.Empty) == State.DoublyPartial);
	}

	private static void replaceChildren(Node v, Collection<Node> list) {
		for (var e : IteratorUtils.asList(v.outEdges())) {
			v.getOwner().deleteEdge(e);
		}
		list.forEach(w -> v.getOwner().newEdge(v, w));
	}

	private static boolean isEmpty(Node v, HashMap<Node, State> stateMap) {
		return stateMap.getOrDefault(v, State.Empty) == State.Empty;
	}

	/**
	 * extracts an ordering that is compatible with all accepted sets
	 *
	 * @return ordering
	 */
	public ArrayList<Integer> extractAnOrdering() {
		return extractAnOrdering(tree.getRoot());
	}

	/**
	 * extracts an ordering that is compatible with all accepted sets
	 *
	 * @return ordering
	 */
	public static ArrayList<Integer> extractAnOrdering(Node root) {
		var ordering = new ArrayList<Integer>();
		var tree = ((PhyloTree) root.getOwner());
		tree.postorderTraversal(root, v -> {
			if (getType(v) == Type.Leaf)
				ordering.add(tree.getTaxon(v));
		});
		return ordering;
	}

	public boolean check(BitSet set) {
		var ordering = extractAnOrdering();
		var min = Integer.MAX_VALUE;
		var max = Integer.MIN_VALUE;
		for (var t : BitSetUtils.members(set)) {
			var index = ordering.indexOf(t);
			min = Math.min(min, index);
			max = Math.max(max, index);
		}
		return (max - min + 1) == set.cardinality();
	}

	public void check(BitSet... sets) {
		System.err.println("PQTree: " + toBracketString());
		var ordering = extractAnOrdering();
		System.err.println("Ordering: " + ordering);
		for (var set : sets) {
			System.err.println(set + ": " + (check(set) ? "contained" : "NOT contained"));
		}
	}

	static Type getType(Node v) {
		return (Type) v.getData();
	}

	static void setType(Node v, Type type) {
		v.setData(type);
	}

	public String toBracketString() {
		return toBracketString(tree.getRoot());
	}

	public String toBracketString(Node root) {
		var buf = new StringBuilder();
		toBracketStringRec(root, buf);
		buf.append(";");
		return buf.toString();
	}

	private void toBracketStringRec(Node v, StringBuilder buf) {
		var type = getType(v);
		buf.append(switch (type) {
			case P -> '(';
			case Q -> '[';
			default -> '\'';
		});
		if (type == Type.P || type == Type.Q) {
			var first = true;
			for (var w : v.children()) {
				if (first)
					first = false;
				else
					buf.append(",");
				toBracketStringRec(w, buf);
			}
		} else
			buf.append(tree.getTaxon(v));
		buf.append(switch (type) {
			case P -> ')';
			case Q -> ']';
			default -> '\'';
		});
	}

	public String getMethodName() {
		var stackTrace = Thread.currentThread().getStackTrace();
		return stackTrace[2].getMethodName();
	}

	public static void test(Collection<BitSet> sets) {
		var pqTree = new PQTree();
		var fails = 0;
		for (var set : sets) {
			if (!pqTree.accept(set)) {
				System.err.println("pqTree: " + pqTree.toBracketString());
				System.err.println("Failed to accept: " + StringUtils.toString(set));
				fails++;
			}
		}
		System.err.println("pqTree test: " + fails + " fails");

		if (fails > 0 && pqTree.reductionsUsed.size() > 0) {
			pqTree.reportReductionsUsed();
		}
	}

	public void reportReductionsUsed() {
		System.err.println("Reductions used: " + StringUtils.toString(reductionsUsed, ", "));
	}

	public static void main(String[] args) {
		var a = BitSetUtils.asBitSet(1, 2, 3);
		var b = BitSetUtils.asBitSet(2, 3, 5);
		var c = BitSetUtils.asBitSet(4, 5, 6);
		var d = BitSetUtils.asBitSet(4, 8);
		var e = BitSetUtils.asBitSet(1, 7, 8, 9);
		var f = BitSetUtils.asBitSet(2, 3, 4, 5, 6);
		var g = BitSetUtils.asBitSet(1, 3, 7);
		var h = BitSetUtils.asBitSet(2, 3, 5);

		test(List.of(a, b, c, d, e, f, g, h));
	}
}
