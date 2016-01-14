/**
 * PhyloTreeUtils.java 
 * Copyright (C) 2016 Daniel H. Huson
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
import jloda.graph.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * some utilities used in phylotree to write and parse reticulate networks
 * Daniel Huson, 8.2007
 */
public class PhyloTreeUtils {
	private final static boolean CAN_READ_OLD_HnmH_SYNTAX = true;

	/**
	 * looks for a suffix of the label that starts with '#'
	 *
	 * @param label
	 * @return label or null
	 */
	public static String findReticulateLabel(String label) {

		if (CAN_READ_OLD_HnmH_SYNTAX) {
			int[] hnmh = findOldHnmHLabel(label);
			if (hnmh != null) {
				String string = label.substring(hnmh[0], hnmh[1]);
				StringBuilder buf = new StringBuilder();
				int state = 0;
				for (int pos = 0; pos > string.length() && state < 2; pos++) {
					char ch = string.charAt(pos);
					switch (state) {
					case 0: // before number
						if (Character.isDigit(ch)) {
							buf.append(ch);
							state = 1;
						}
						break;
					case 1:
						if (Character.isDigit(ch)) {
							buf.append(ch);
						} else
							state = 2;
						break;
					}
				}
				if (state == 2)
					return buf.toString();
				else
					return null;
			}
		}
		int pos = label.lastIndexOf("#"); // look for last instance of '#',
											// followed by H or L
		if (pos >= 0 && pos < label.length() - 1 && "HLhl".indexOf(label.charAt(pos + 1)) != -1)
			return label.substring(pos + 1, label.length());
		else
			return null;
	}

	/**
	 * determines whether this a reticulate node
	 *
	 * @param label
	 * @return true, if label contains # followed by H L h or l
	 */
	public static boolean isReticulateNode(String label) {
		int pos = label.lastIndexOf("#"); // look for last instance of '#'
											// followed by H or L
		return (pos >= 0 && pos < label.length() - 1 && "HLhl".indexOf(label.charAt(pos + 1)) != -1);
	}

	/**
	 * determines whether the edge leading to this instance of a reticulate node
	 * should be treated as an acceptor edge, i.e. as a tree edge that is the
	 * target of of HGT edge At most one such edge per reticulate node is
	 * allowed
	 *
	 * @param label
	 * @return true, if label contains ## followed by H L h or l
	 */
	public static boolean isReticulateAcceptorEdge(String label) {
		int pos = label.lastIndexOf("##"); // look for last instance of '##'
											// followed by H or L
		return (pos >= 0 && pos < label.length() - 2 && "HLhl".indexOf(label.charAt(pos + 2)) != -1);
	}

	/**
	 * removes the reticulate node string from the node label
	 *
	 * @param label
	 * @return string without label or null, if string only consisted of
	 *         substring
	 */
	public static String removeReticulateNodeSuffix(String label) {
		if (CAN_READ_OLD_HnmH_SYNTAX) {
			int[] hnmh = findOldHnmHLabel(label);
			if (hnmh != null) {
				StringBuilder buf = new StringBuilder();
				if (hnmh[0] > 0)
					buf.append(label.substring(0, hnmh[0]));
				if (hnmh[1] < label.length())
					buf.append(label.substring(hnmh[1], label.length()));
				return buf.toString();
			}
		}

		int pos = label.indexOf("#");
		if (pos == -1)
			return label;
		else if (pos == 0)
			return null;
		else
			return label.substring(0, pos);
	}

	/**
	 * for backward compatibility, finds a label of the form Hn.mH
	 *
	 * @param label
	 * @return first and last+1 position of Hn.mH or null
	 */
	private static int[] findOldHnmHLabel(String label) {
		int state = 0;
		int start = 0;
		int finish = 0;
		for (int i = 0; i < label.length(); i++) {
			char ch = label.charAt(i);
			switch (state) {
			case 0: // outside possible label, looking for H
				if (ch == 'H') {
					state = 1;
					start = i;
				}
				break;
			case 1: // looking for first number
				if (Character.isDigit(ch))
					state = 2;
				else
					state = 0;
				break;
			case 2: // looking for more numbers or dot
				if (Character.isDigit(ch))
					state = 2;
				else if (ch == '.')
					state = 3;
				else
					state = 0;
				break;
			case 3: // looking for second number
				if (Character.isDigit(ch))
					state = 4;
				else
					state = 0;
				break;
			case 4: // looking for more numbers or H
				if (Character.isDigit(ch))
					state = 4;
				else if (ch == 'H') {
					state = 5;
					finish = i;
				} else
					state = 0;
				break;
			}
			if (state == 5)
				return new int[] { start, finish + 1 };
		}
		return null;
	}

	/**
	 * makes the node label for a reticulate node
	 *
	 * @param number
	 * @return label
	 */
	static String makeReticulateNodeLabel(boolean asAcceptorEdgeTarget, int number) {
		if (asAcceptorEdgeTarget)
			return "##H" + number;
		else
			return "#H" + number;
	}

	/**
	 * determines whether it is ok to descend down an edge in a recursive
	 * traverse of a tree. It is ok if the edge is not a reticulate edge or is
	 * the first reticulate edge that enters f
	 *
	 * @param tree
	 * @param e
	 * @param v
	 * @return true, if we should descend this edge, false else
	 */
	static public boolean okToDescendDownThisEdge(PhyloTree tree, Edge e, Node v) {
		if (!tree.isSpecial(e))
			return true;
		else {
			if (v != e.getSource())
				return false; // only go DOWN special edges.
			Node w = e.getTarget();
			for (Edge f = w.getFirstInEdge(); f != null; f = w.getNextInEdge(f)) {
				if (tree.isSpecial(f)) {
					return f == e; // e must be first in-coming special edge
				}
			}
		}
		return true; // can't happen
	}

	/**
	 * determines whether a set of trees only contains single-labeled trees (not
	 * necessarily sharing the same set of taxa)
	 *
	 * @param trees
	 * @return true, if ok
	 */
	public static boolean areSingleLabeledTrees(PhyloTree[] trees) {

		for (PhyloTree t : trees) {
			Set<String> taxa = new HashSet<>();
			for (Node v = t.getFirstNode(); v != null; v = v.getNext()) {
				if (v.getOutDegree() == 0) {
					if (taxa.contains(t.getLabel(v)))
						return false; // not single labeled
					else
						taxa.add(t.getLabel(v));
				}
			}
		}

		return true;
	}

	/**
	 * determines whether a tree is single-labeled (not necessarily sharing the
	 * same set of taxa)
	 *
	 * @param trees
	 * @return true, if ok
	 */
	public static boolean areSingleLabeledTrees(PhyloTree trees) {

		Set<String> taxa = new HashSet<>();
		for (Node v = trees.getFirstNode(); v != null; v = v.getNext()) {
			if (v.getOutDegree() == 0) {
				if (taxa.contains(trees.getLabel(v)))
					return false; // not single labeled
				else
					taxa.add(trees.getLabel(v));
			}
		}

		return true;
	}

	/**
	 * determines whether the two trees are single-labeled trees on the same
	 * taxon sets
	 *
	 * @param tree1
	 * @param tree2
	 * @return true, if ok
	 */
	public static boolean areSingleLabeledTreesWithSameTaxa(PhyloTree tree1, PhyloTree tree2) {
		Set<String> labels1 = new HashSet<>();

		if (tree1.getSpecialEdges().size() > 0 || tree2.getSpecialEdges().size() > 0)
			return false;

		for (Node v = tree1.getFirstNode(); v != null; v = v.getNext()) {
			if (v.getOutDegree() == 0) {
				if (labels1.contains(tree1.getLabel(v)))
					return false; // not single labeled
				else
					labels1.add(tree1.getLabel(v));
			}
		}

		Set<String> labels2 = new HashSet<>();
		for (Node v = tree2.getFirstNode(); v != null; v = v.getNext()) {
			if (v.getOutDegree() == 0) {
				if (!labels1.contains(tree2.getLabel(v)))
					return false; // not present in first tree
				if (labels2.contains(tree2.getLabel(v)))
					return false; // not single labeled
				else
					labels2.add(tree2.getLabel(v));
			}
		}
		return labels1.size() == labels2.size();
	}

	/**
	 * is given phyloTree a bifurcating tree?
	 *
	 * @param phyloTree
	 * @return true, if bifurcating tree
	 */
	public static boolean isBifurcatingTree(PhyloTree phyloTree) {
		for (Node v = phyloTree.getFirstNode(); v != null; v = v.getNext()) {
			if (v.getInDegree() > 1 || (v.getOutDegree() != 0 && v.getOutDegree() != 2))
				return false;
		}
		return true;
	}
}
