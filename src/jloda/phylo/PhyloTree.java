/*
 * PhyloTree.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.graph.*;
import jloda.util.*;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Phylogenetic tree, with support for rooted phylogenetic network
 * Daniel Huson, 2003
 */
public class PhyloTree extends PhyloSplitsGraph {
	public static final boolean ALLOW_READ_RETICULATE = true;

	public static boolean SUPPORT_RICH_NEWICK = false; // only SplitsTree6 should set this to true

	public static boolean WARN_HAS_MULTILABELS = true;
	public static final String COLLAPSED_NODE_SUFFIX = "{+}";

	public boolean allowMultiLabeledNodes = true;

	private Node root = null;

	private boolean inputHasMultiLabels = false;
	private boolean hideCollapsedSubTreeOnWrite = false;


	private final boolean cleanLabelsOnWrite;

	private volatile EdgeSet reticulateEdges;
	private volatile NodeArray<List<Node>> lsaChildrenMap; // keep track of children in LSA tree in network
	private volatile EdgeSet transferAcceptorEdges;

	/**
	 * Construct a new empty phylogenetic tree.
	 */
	public PhyloTree() {
		super();
		cleanLabelsOnWrite = ProgramProperties.get("cleanTreeLabelsOnWrite", false);
	}

	/**
	 * copy constructor
	 */
	public PhyloTree(PhyloTree src) {
		this();
		copy(src);
	}

	/**
	 * Clears the tree.
	 */
	public void clear() {
		super.clear();
		setRoot(null);
		reticulateEdges = null;
		transferAcceptorEdges = null;
		lsaChildrenMap = null;
		inputHasMultiLabels = false;
	}

	/**
	 * copies a phylogenetic tree
	 *
	 * @param src original tree
	 * @return old node to new node lap
	 */
	public NodeArray<Node> copy(PhyloTree src) {
		NodeArray<Node> oldNode2NewNode = src.newNodeArray();
		copy(src, oldNode2NewNode, null);
		return oldNode2NewNode;
	}

	/**
	 * copies a phylogenetic tree
	 */
	public void copy(PhyloTree src, NodeArray<Node> oldNode2NewNode, EdgeArray<Edge> oldEdge2NewEdge) {
		setName(src.getName());
		if (oldEdge2NewEdge == null)
			oldEdge2NewEdge = new EdgeArray<>(src);
		oldNode2NewNode = super.copy(src, oldNode2NewNode, oldEdge2NewEdge);
		if (src.getRoot() != null) {
			var root = src.getRoot();
			setRoot(oldNode2NewNode.get(root));
		}
		if (src.lsaChildrenMap != null) {
			for (var v : src.nodes()) {
				var children = src.lsaChildrenMap.get(v);
				if (children != null) {
					var newChildren = new ArrayList<Node>();
					for (var w : children) {
						newChildren.add(oldNode2NewNode.get(w));
					}
					getLSAChildrenMap().put(oldNode2NewNode.get(v), newChildren);
				}
			}
		}
		if (src.reticulateEdges != null) {
			for (var e : src.reticulateEdges) {
				setReticulate(oldEdge2NewEdge.get(e), true);
			}
		}
		if (src.transferAcceptorEdges != null) {
			for (var e : src.transferAcceptorEdges) {
				setTransferAcceptor(oldEdge2NewEdge.get(e), true);
			}
		}
		setName(src.getName());
	}

	/**
	 * clones the current tree
	 *
	 * @return a clone of the current tree
	 */
	public Object clone() {
		var tree = new PhyloTree();
		tree.copy(this);
		return tree;
	}

	/**
	 * Produces a string representation of the tree in bracket notation.
	 *
	 * @return a string representation of the tree in bracket notation
	 */
	public String toBracketString() {
		try (var w = new StringWriter()) {
			write(w, true);
			return w.toString();
		} catch (Exception ex) {
			Basic.caught(ex);
			return "();";
		}
	}

	/**
	 * Produces a string representation of the tree in bracket notation.
	 *
	 * @return a string representation of the tree in bracket notation
	 */
	public String toBracketString(boolean showWeights) {
		if (SUPPORT_RICH_NEWICK)
			return toBracketString(new NewickOutputFormat(showWeights, false, hasEdgeConfidences(), hasEdgeProbabilities(), false));
		else
			return toBracketString(new NewickOutputFormat(showWeights, false, false, false, false));
	}

	/**
	 * Produces a string representation of the tree in bracket notation.
	 *
	 * @return a string representation of the tree in bracket notation
	 */
	public String toString(Map<String, String> translate) {
		try (var sw = new StringWriter()) {
			if (translate == null || translate.size() == 0) {
				this.write(sw, true);
			} else {
				var tmpTree = new PhyloTree();
				tmpTree.copy(this);
				for (var v : tmpTree.nodes()) {
					var key = tmpTree.getLabel(v);
					if (key != null) {
						var value = translate.get(key);
						if (value != null)
							tmpTree.setLabel(v, value);
					}
				}
				tmpTree.write(sw, true);
			}
			return sw.toString();
		} catch (Exception ex) {
			Basic.caught(ex);
			return "()";
		}
	}

	public String toBracketString(NewickOutputFormat format) {
		try (var sw = new StringWriter()) {
			write(sw, format);
			return sw.toString();
		} catch (Exception ex) {
			Basic.caught(ex);
			return "();";
		}
	}

	/**
	 * writes a tree
	 */
	public void write(final Writer writer, final boolean showWeights, final Function<Node, String> labeler) throws IOException {
		if (labeler == null) {
			this.write(writer, showWeights);
		} else {
			var tmpTree = new PhyloTree();
			tmpTree.copy(this);
			for (var v : tmpTree.nodes()) {
				var label = labeler.apply(v);
				if (label != null) {
					label = label.replaceAll("'", "_");
					if (StringUtils.intersects(label, " :(),"))
						tmpTree.setLabel(v, "'" + label + "'");
					else
						tmpTree.setLabel(v, label);
				}
			}
			tmpTree.write(writer, showWeights);
		}
	}

	/**
	 * Writes a tree in bracket notation
	 *
	 * @param w            the writer
	 * @param writeWeights write edge weights or not
	 */
	public void write(Writer w, boolean writeWeights) throws IOException {
		write(w, writeWeights, false);
	}

	/**
	 * Writes a tree in bracket notation
	 *
	 * @param w            the writer
	 * @param writeWeights write edge weights or not
	 */
	public void write(Writer w, boolean writeWeights, boolean writeEdgeLabelsAsComments) throws IOException {
		if (SUPPORT_RICH_NEWICK) {
			write(w, new NewickOutputFormat(writeWeights, false, hasEdgeConfidences(), hasEdgeProbabilities(), writeEdgeLabelsAsComments), null, null);
		} else {
			write(w, new NewickOutputFormat(writeWeights, false, false, false, writeEdgeLabelsAsComments), null, null);
		}
	}

	public void write(Writer w, NewickOutputFormat newickOutputFormat) throws IOException {
		write(w, newickOutputFormat, null, null);
	}

	private int outputNodeNumber = 0;
	private int outputEdgeNumber = 0;
	private NodeIntArray outputNodeReticulationNumberMap;  // global number of the reticulate nodes
	private int outputReticulationNumber;

	private static final String punctuationCharacters = "),;:";

	/**
	 * Writes a tree in bracket notation. Uses extended bracket notation to write reticulate network
	 *
	 * @param w             the writer
	 * @param nodeId2Number if non-null, will contain node-id to number mapping after call
	 * @param edgeId2Number if non-null, will contain edge-id to number mapping after call
	 */
	public void write(Writer w, NewickOutputFormat format, Map<Integer, Integer> nodeId2Number, Map<Integer, Integer> edgeId2Number) throws IOException {
		outputNodeNumber = 0;
		outputEdgeNumber = 0;

		if (hasReticulateEdges()) {
			// following two lines enable us to write reticulate networks in Newick format
			if (outputNodeReticulationNumberMap == null)
				outputNodeReticulationNumberMap = newNodeIntArray();
			else
				outputNodeReticulationNumberMap.clear();
			outputReticulationNumber = 0;
		}

		if (getNumberOfEdges() > 0) {
			if (getRoot() == null) {
				root = getFirstNode();
				for (Node v = root; v != null; v = v.getNext()) {
					if (v.getDegree() > root.getDegree())
						root = v;
				}
			}
			writeRec(w, root, null, format, nodeId2Number, edgeId2Number, getLabelForWriting(root));
		} else if (getNumberOfNodes() == 1) {
			w.write("(" + getLabelForWriting(getFirstNode()) + ");");
			if (nodeId2Number != null)
				nodeId2Number.put(getFirstNode().getId(), 1);
		} else
			w.write("();");

		if (outputNodeReticulationNumberMap != null)
			outputNodeReticulationNumberMap.clear();
	}

	/**
	 * Recursively writes a tree in bracket notation
	 */
	private void writeRec(Writer outs, Node v, Edge e, NewickOutputFormat format, Map<Integer, Integer> nodeId2Number, Map<Integer, Integer> edgeId2Number, String nodeLabel) throws IOException {
		if (nodeId2Number != null)
			nodeId2Number.put(v.getId(), ++outputNodeNumber);

		if (!isHideCollapsedSubTreeOnWrite() || getLabel(v) == null || !getLabel(v).endsWith(PhyloTree.COLLAPSED_NODE_SUFFIX)) {
			if (v.getOutDegree() > 0) {
				outs.write("(");
				boolean first = true;
				for (Edge f : v.outEdges()) {
					if (edgeId2Number != null)
						edgeId2Number.put(f.getId(), ++outputEdgeNumber);

					if (first)
						first = false;
					else
						outs.write(",");

					final Node w = f.getTarget();

					if (isReticulateEdge(f)) {
						boolean isAcceptorEdge = isTransferAcceptorEdge(e);

						if (outputNodeReticulationNumberMap.get(w) == null) {
							outputNodeReticulationNumberMap.set(w, ++outputReticulationNumber);
							final String label;
							if (getLabel(w) != null)
								label = getLabelForWriting(w) + PhyloTreeNetworkIOUtils.makeReticulateNodeLabel(isAcceptorEdge, outputNodeReticulationNumberMap.get(w));
							else
								label = PhyloTreeNetworkIOUtils.makeReticulateNodeLabel(isAcceptorEdge, outputNodeReticulationNumberMap.get(w));

							writeRec(outs, w, f, format, nodeId2Number, edgeId2Number, label);
						} else {
							String label;
							if (getLabel(w) != null)
								label = getLabelForWriting(w) + PhyloTreeNetworkIOUtils.makeReticulateNodeLabel(isAcceptorEdge, outputNodeReticulationNumberMap.get(w));
							else
								label = PhyloTreeNetworkIOUtils.makeReticulateNodeLabel(isAcceptorEdge, outputNodeReticulationNumberMap.get(w));

							outs.write(label);
							outs.write(getEdgeString(format, f));
						}
					} else
						writeRec(outs, w, f, format, nodeId2Number, edgeId2Number, getLabelForWriting(w));
				}
				outs.write(")");
			}
			if (nodeLabel != null && nodeLabel.length() > 0)
				outs.write(nodeLabel);
		}

		if (e != null) {
			outs.write(getEdgeString(format, e));
		}
	}

	public String getEdgeString(NewickOutputFormat format, Edge e) {
		var buf = new StringBuilder();
		var colons = 0;
		if (format.weights() && getWeight(e) != -1.0) {
			if (getEdgeWeights().containsKey(e)) {
				buf.append(StringUtils.removeTrailingZerosAfterDot(String.format(":%.8f", getWeight(e))));
				colons++;
			}
		}
		if (format.confidenceUsingColon() && hasEdgeConfidences() && getEdgeConfidences().containsKey(e)) {
			while (colons < 2) {
				buf.append(":");
				colons++;
			}
			buf.append(StringUtils.removeTrailingZerosAfterDot(String.format("%.8f", getConfidence(e))));
		}
		if (format.probabilityUsingColon() && hasEdgeProbabilities() && getEdgeProbabilities().containsKey(e)) {
			while (colons < 3) {
				buf.append(":");
				colons++;
			}
			buf.append(StringUtils.removeTrailingZerosAfterDot(String.format("%.8f", getProbability(e))));
		}
		if (format.edgeLabelsAsComments() && getLabel(e) != null) {
			buf.append("[").append(getLabelForWriting(e)).append("]");
		}
		return buf.toString();
	}

	/**
	 * get the label to be used for writing. Will have single quotes, if label contains punctuation character or white space
	 */
	public String getLabelForWriting(Node v) {
		var label = cleanLabelsOnWrite ? StringUtils.getCleanLabelForNewick(getLabel(v)) : getLabel(v);
		if (label != null) {
			for (int i = 0; i < label.length(); i++) {
				if (punctuationCharacters.indexOf(label.charAt(i)) != -1 || Character.isWhitespace(label.charAt(i)))
					return "'" + label + "'";
			}
		}
		return label;
	}

	/**
	 * get the label to be used for writing. Will have single quotes, if label contains punctuation character or white space
	 */
	public String getLabelForWriting(Edge e) {
		var label = cleanLabelsOnWrite ? StringUtils.getCleanLabelForNewick(getLabel(e)) : getLabel(e);
		if (label != null) {
			for (int i = 0; i < label.length(); i++) {
				if (punctuationCharacters.indexOf(label.charAt(i)) != -1 || Character.isWhitespace(label.charAt(i)))
					return "'" + label + "'";
			}
		}
		return label;
	}

	/**
	 * Given a string representation of a tree, returns the tree.
	 *
	 * @param str String
	 * @return tree PhyloTree
	 */
	static public PhyloTree valueOf(String str) throws IOException {
		var tree = new PhyloTree();
		tree.parseBracketNotation(str, true);
		return tree;
	}

	/**
	 * reads a line and then parses it as a rooted tree or network in Newick format
	 *
	 * @param r the reader
	 */
	public void read(Reader r) throws IOException {
		final BufferedReader br;
		if (r instanceof BufferedReader)
			br = (BufferedReader) r;
		else
			br = new BufferedReader(r);
		parseBracketNotation(br.readLine(), true, true);
	}

	/**
	 * Parses a tree or network in Newick notation, and sets the root, if desired
	 */
	public void parseBracketNotation(String str, boolean rooted) throws IOException {
		parseBracketNotation(str, rooted, true);
	}


	/**
	 * Parses a tree or network in Newick notation, and sets the root, if desired
	 */
	public void parseBracketNotation(String str, boolean rooted, boolean doClear) throws IOException {
		if (doClear)
			clear();
		inputHasMultiLabels = false;

		var seen = new HashMap<String, Node>();

		try {
			parseBracketNotationRec(seen, 0, null, 0, str);
		} catch (IOException ex) {
			System.err.println(str);
			throw ex;
		}
		if (getNumberOfNodes() > 0) {
			final var v = getFirstNode();
			if (rooted) {
				setRoot(v);
				if (false && !hasEdgeWeights() && isUnlabeledDiVertex(v)) {
					setWeight(v.getFirstAdjacentEdge(), 0.5);
					setWeight(v.getLastAdjacentEdge(), 0.5);
				}
			} else {
				if (isUnlabeledDiVertex(v))
					setRoot(delDivertex(v).getSource());
				else setRoot(v);
			}
		}

		// post process any reticulate nodes
		postProcessReticulate();

		// if all internal nodes are labeled with numbers, then these are interpreted as confidence values and put on the edges
		// In dendroscope and splitstree5, these are left on the nodes
		if (SUPPORT_RICH_NEWICK) {
			if (nodeStream().filter(v -> !v.isLeaf() && v.getInDegree() == 1).allMatch(v -> NumberUtils.isDouble(getLabel(v)))) {
				nodeStream().filter(v -> !v.isLeaf() && v.getInDegree() == 1)
						.forEach(v -> {
							setConfidence(v.getFirstInEdge(), NumberUtils.parseDouble(getLabel(v)));
							setLabel(v, null);
						});
				var maxValue = nodeStream().filter(v -> !v.isLeaf() && v.getInDegree() == 1)
						.mapToDouble(v -> NumberUtils.parseDouble(getLabel(v))).max();
				if (maxValue.isPresent()) {
					double leafValue = (maxValue.getAsDouble() > 1 && maxValue.getAsDouble() <= 100 ? 100 : 1);
					nodeStream().filter(v -> v.isLeaf() && v.getInDegree() == 1)
							.forEach(v -> setConfidence(v.getFirstInEdge(), leafValue));
				}
			}
		}

		// System.err.println("Multi-labeled nodes detected: " + isInputHasMultiLabels());

		if (false) {
			System.err.println("has acceptor edges: " + hasTransferAcceptorEdges());

			System.err.println("has edge weights: " + hasEdgeWeights());
			System.err.println("has edge confidences: " + hasEdgeConfidences());
			System.err.println("has edge probabilities: " + hasEdgeProbabilities());
			System.err.println(toBracketString(new NewickOutputFormat(true, true, true, true, true)));
		}
	}


	/**
	 * recursively do the work
	 *
	 * @param seen  set of seen labels
	 * @param depth distance from root
	 * @param v     parent node
	 * @param pos   current position in string
	 * @param str   string
	 * @return new current position
	 */
	private int parseBracketNotationRec(Map<String, Node> seen, int depth, Node v, int pos, String str) throws IOException {
		for (pos = StringUtils.skipSpaces(str, pos); pos < str.length(); pos = StringUtils.skipSpaces(str, pos + 1)) {
			var w = newNode();
			String label = null;
			if (str.charAt(pos) == '(') {
				pos = parseBracketNotationRec(seen, depth + 1, w, pos + 1, str);
				if (str.charAt(pos) != ')')
					throw new IOException("Expected ')' at position " + pos);
				pos = StringUtils.skipSpaces(str, pos + 1);
				while (pos < str.length() && punctuationCharacters.indexOf(str.charAt(pos)) == -1) {
					var pos0 = pos;
					var buf = new StringBuilder();
					var inQuotes = false;
					while (pos < str.length() && (inQuotes || punctuationCharacters.indexOf(str.charAt(pos)) == -1)) {
						if (str.charAt(pos) == '\'')
							inQuotes = !inQuotes;
						else
							buf.append(str.charAt(pos));
						pos++;
					}
					label = buf.toString().trim();

					if (label.length() > 0) {
						if (!getAllowMultiLabeledNodes() && seen.containsKey(label) && PhyloTreeNetworkIOUtils.findReticulateLabel(label) == null)
						// if label already used, make unique, unless this is a reticulate node
						{
							if (label.startsWith("'") && label.endsWith("'") && label.length() > 1)
								label = label.substring(1, label.length() - 1);
							// give first occurrence of this label the suffix .1
							final Node old = seen.get(label);
							if (old != null) // change label of node
							{
								setLabel(old, label + ".1");
								seen.put(label, null); // keep label in, but null indicates has changed
								seen.put(label + ".1", old);
								inputHasMultiLabels = true;
							}

							var t = 1;
							String labelt;
							do {
								labelt = label + "." + (++t);
							} while (seen.containsKey(labelt));
							label = labelt;
						}
						seen.put(label, w);
					}
					setLabel(w, label);
					if (label.length() == 0)
						throw new IOException("Expected label at position " + pos0);
				}
			} else // everything to next ) : or , is considered a label:
			{
				if (getNumberOfNodes() == 1)
					throw new IOException("Expected '(' at position " + pos);
				var pos0 = pos;
				final var buf = new StringBuilder();
				boolean inQuotes = false;
				while (pos < str.length() && (inQuotes || punctuationCharacters.indexOf(str.charAt(pos)) == -1)) {
					if (str.charAt(pos) == '\'')
						inQuotes = !inQuotes;
					else
						buf.append(str.charAt(pos));
					pos++;
				}
				label = buf.toString().trim();

				if (label.startsWith("'") && label.endsWith("'") && label.length() > 1)
					label = label.substring(1, label.length() - 1).trim();

				if (label.length() > 0) {
					if (!getAllowMultiLabeledNodes() && seen.containsKey(label) && PhyloTreeNetworkIOUtils.findReticulateLabel(label) == null) {
						// give first occurrence of this label the suffix .1
						var old = seen.get(label);
						if (old != null) // change label of node
						{
							setLabel(old, label + ".1");
							seen.put(label, null); // keep label in, but null indicates has changed
							seen.put(label + ".1", old);
							inputHasMultiLabels = true;
							if (WARN_HAS_MULTILABELS)
								System.err.println("multi-label: " + label);
						}

						var t = 1;
						String labelt;
						do {
							labelt = label + "." + (++t);
						} while (seen.containsKey(labelt));
						label = labelt;
					}
					seen.put(label, w);
				}
				setLabel(w, label);
				if (label.length() == 0)
					throw new IOException("Expected label at position " + pos0);
			}
			Edge e = null;
			if (v != null)
				e = newEdge(v, w);

			// detect and read embedded bootstrap values:
			pos = StringUtils.skipSpaces(str, pos);

			// read edge weights
			var didReadWeight = false;

			for (var which = 0; which < 3; which++) {
				if (pos < str.length() && str.charAt(pos) == ':') { // edge weight is following
					pos = StringUtils.skipSpaces(str, pos + 1);
					if (pos < str.length() && str.charAt(pos) == ':') {
						continue;
					}
					var pos0 = pos;
					var numberStr = StringUtils.getStringUptoDelimiter(str, pos0, punctuationCharacters);
					if (!NumberUtils.isDouble(numberStr))
						throw new IOException("Expected number at position " + pos0 + " (got: '" + numberStr + "')");
					pos = pos0 + numberStr.length();
					var value = Math.max(0, Double.parseDouble(numberStr));
					switch (which) {
						case 0 -> {
							if (e != null) {
								setWeight(e, value);
								didReadWeight = true;
							}
						}
						case 1 -> {
							if (e != null) {
								setConfidence(e, value);
							}
						}
						case 2 -> {
							if (e != null) {
								setProbability(e, value);
							}
						}
					}

				}
				if (!SUPPORT_RICH_NEWICK)
					break; // don't allow confidence or probability
			}

			// adjust edge weights for reticulate edges
			if (e != null) {
				if (SUPPORT_RICH_NEWICK) {
					if (label != null && PhyloTreeNetworkIOUtils.isReticulateNode(label)) {
						if (PhyloTreeNetworkIOUtils.isReticulateAcceptorEdge(label)) {
							setTransferAcceptor(e, true);
						} else {
							setReticulate(e, true);
						}
					}
				} else {
					try {
						if (label != null && PhyloTreeNetworkIOUtils.isReticulateNode(label)) {
							// if an instance of a reticulate node is marked ##, then we will set the weight of the edge to the node to a number >0
							// to indicate that edge should be drawn as a tree edge
							if (PhyloTreeNetworkIOUtils.isReticulateAcceptorEdge(label)) {
								if (!didReadWeight || getWeight(e) <= 0) {
									setWeight(e, 0.000001);
								}
							} else {
								if (getWeight(e) > 0)
									setWeight(e, 0.0);
							}
						}
					} catch (IllegalSelfEdgeException e1) {
						Basic.caught(e1);
					}
				}
			}

			// now i should be pointing to a ',', a ')' or '[' (for a label)
			if (pos >= str.length()) {
				if (depth == 0)
					return pos; // finished parsing tree
				else
					throw new IOException("Unexpected end of line");
			}
			if (str.charAt(pos) == '[') // edge label
			{
				int x = str.indexOf('[', pos + 1);
				int j = str.indexOf(']', pos + 1);
				if (j == -1 || (x != -1 && x < j))
					throw new IOException("Error in edge label at position: " + pos);
				setLabel(e, str.substring(pos + 1, j));
				pos = j + 1;
			}
			if (str.charAt(pos) == ';' && depth == 0)

				return pos; // finished parsing tree
			else if (str.charAt(pos) == ')')
				return pos;
			else if (str.charAt(pos) != ',')
				throw new IOException("Unexpected '" + str.charAt(pos) + "' at position " + pos);
		}
		return -1;
	}

	/**
	 * post processes a tree that really describes a reticulate network
	 */
	public void postProcessReticulate() {
		// determine all the groups of reticulate nodes
		final var reticulateNumber2Nodes = new HashMap<String, List<Node>>(); // maps each reticulate-node prefix to the set of all nodes that have it

		for (var v : nodes()) {
			var label = getLabel(v);
			if (label != null && label.length() > 0) {
				var reticulateLabel = PhyloTreeNetworkIOUtils.findReticulateLabel(label);
				if (reticulateLabel != null) {
					setLabel(v, PhyloTreeNetworkIOUtils.removeReticulateNodeSuffix(label));
					var list = reticulateNumber2Nodes.computeIfAbsent(reticulateLabel, k -> new ArrayList<>());
					list.add(v);
				}
			}
		}

		// collapse all instances of a reticulate node into one node
		for (var reticulateNumber : reticulateNumber2Nodes.keySet()) {
			final var list = reticulateNumber2Nodes.get(reticulateNumber);
			if (list.size() > 0) {
				Node u = null;

				for (var v : list) {
					if (u == null) {
						u = v;
					} else {
						if (getLabel(v) != null) {
							if (getLabel(u) == null)
								setLabel(u, getLabel(v));
							else if (!getLabel(u).equals(getLabel(v)))
								setLabel(u, getLabel(u) + "," + getLabel(v));
						}

						for (var e : v.adjacentEdges()) {
							final Edge f;
							if (e.getSource() == v) { /// attach child of v below u
								f = newEdge(u, e.getTarget());
							} else { // attach parent of v above u
								f = newEdge(e.getSource(), u);
							}
							if (hasEdgeWeights() && getEdgeWeights().containsKey(e))
								setWeight(f, getWeight(e));
							if (hasEdgeConfidences() && getEdgeConfidences().containsKey(e))
								setConfidence(f, getConfidence(e));
							if (hasEdgeProbabilities() && getEdgeProbabilities().containsKey(e))
								setProbability(f, getProbability(e));
							if (isTransferAcceptorEdge(e)) {
								setTransferAcceptor(f, true);
							}
							setReticulate(f, true);
							setLabel(f, getLabel(e));
						}
						deleteNode(v);
					}
				}

				if (!SUPPORT_RICH_NEWICK) {
					var transferAcceptorEdge = new Single<Edge>();
					for (var e : u.inEdges()) {
						setReticulate(e, true);
						if (getWeight(e) > 0) {
							if (transferAcceptorEdge.isNull())
								transferAcceptorEdge.set(e);
							else {
								setWeight(e, 0.0);
								System.err.println("Warning: node has more than one transfer-acceptor edge, will only use first");
							}
						}
					}
					if (transferAcceptorEdge.isNotNull()) {
						u.inEdgesStream(false).filter(e -> e != transferAcceptorEdge.get()).forEach(e -> setWeight(e, -1.0));
					}
				}
			}
		}
	}

	/**
	 * is v an unlabeled node of degree 2?
	 *
	 * @return true, if v is an unlabeled node of degree 2
	 */
	private boolean isUnlabeledDiVertex(Node v) {
		return v.getDegree() == 2 && (getLabel(v) == null || getLabel(v).length() == 0);
	}

	/**
	 * deletes divertex
	 *
	 * @param v Node
	 * @return the new edge
	 */
	public Edge delDivertex(Node v) {
		if (v.getDegree() != 2)
			throw new RuntimeException("v not di-vertex, degree is: " + v.getDegree());

		var e = getFirstAdjacentEdge(v);
		var f = getLastAdjacentEdge(v);

		var x = getOpposite(v, e);
		var y = getOpposite(v, f);

		Edge g = null;
		try {
			if (x == e.getSource())
				g = newEdge(x, y);
			else
				g = newEdge(y, x);
		} catch (IllegalSelfEdgeException e1) {
			Basic.caught(e1);
		}
		if (getWeight(e) != Double.NEGATIVE_INFINITY && getWeight(f) != Double.NEGATIVE_INFINITY)
			setWeight(g, getWeight(e) + getWeight(f));
		if (hasEdgeConfidences())
			setConfidence(g, Math.min(getConfidence(e), getConfidence(f)));
		if (hasEdgeProbabilities())
			setProbability(g, Math.min(getProbability(e), getProbability(f)));
		if (root == v)
			root = null;
		deleteNode(v);
		return g;
	}

	public boolean isInputHasMultiLabels() {
		return inputHasMultiLabels;
	}

	/**
	 * gets the root node if set, or null
	 *
	 * @return root or null
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * sets the root node
	 */
	public void setRoot(Node root) {
		this.root = root;
	}

	/**
	 * sets the root node in the middle of this edge
	 */
	public void setRoot(Edge e, EdgeArray<String> edgeLabels) {
		setRoot(e, getWeight(e) * 0.5, getWeight(e) * 0.5, edgeLabels);
	}

	/**
	 * sets the root node in the middle of this edge
	 *
	 * @param weightToSource weight for new edge adjacent to source of e
	 * @param weightToTarget weight for new adjacent to target of e
	 */
	public void setRoot(Edge e, double weightToSource, double weightToTarget, EdgeArray<String> edgeLabels) {
		final var root = getRoot();
		if (root != null && root.getDegree() == 2 && (getTaxa(root) == null || getNumberOfTaxa(root) == 0)) {
			if (root == e.getSource()) {
				var f = (root.getFirstAdjacentEdge() != e ? root.getFirstAdjacentEdge() : root.getLastAdjacentEdge());
				setWeight(e, weightToTarget);
				setWeight(f, weightToSource);
				return; // root stays root
			} else if (root == e.getTarget()) {
				var f = (root.getFirstAdjacentEdge() != e ? root.getFirstAdjacentEdge() : root.getLastAdjacentEdge());
				setWeight(e, weightToSource);
				setWeight(f, weightToTarget);
				return; // root stays root
			}
			eraseRoot(edgeLabels);
		}
		var v = e.getSource();
		var w = e.getTarget();
		var u = newNode();
		var vu = newEdge(v, u);
		var uw = newEdge(u, w);
		setWeight(vu, weightToSource);
		setWeight(uw, weightToTarget);
		if (edgeLabels != null) {
			edgeLabels.put(vu, edgeLabels.get(e));
			edgeLabels.put(uw, edgeLabels.get(e));
		}
		if (hasEdgeConfidences() && getEdgeConfidences().containsKey(e)) {
			setConfidence(vu, getConfidence(e));
			setConfidence(uw, getConfidence(e));
		}
		if (hasEdgeProbabilities() && getEdgeProbabilities().containsKey(e)) {
			setProbability(vu, getProbability(e));
			setProbability(uw, getProbability(e));
		}

		deleteEdge(e);
		setRoot(u);
	}

	/**
	 * erase the current root. If it has out-degree two and is not node-labeled, then two out edges will be replaced by single edge
	 *
	 * @param edgeLabels if non-null and root has two out edges, will try to copy one of the edge labels to the new edge
	 */
	public void eraseRoot(EdgeArray<String> edgeLabels) {
		final Node oldRoot = getRoot();
		setRoot(null);
		if (oldRoot != null) {
			if (getOutDegree(oldRoot) == 2 && getLabel(oldRoot) == null) {
				if (edgeLabels != null) {
					String label = null;
					for (Edge e = oldRoot.getFirstOutEdge(); e != null; e = oldRoot.getNextOutEdge(e)) {
						if (label == null && edgeLabels.get(e) != null)
							label = edgeLabels.get(e);
						edgeLabels.put(e, null);
					}
					final Edge e = delDivertex(oldRoot);
					edgeLabels.put(e, label);
				} else
					delDivertex(oldRoot);
			}
		}
	}

	/**
	 * prints a tree
	 *
	 * @param out  the print stream
	 * @param wgts show weights?
	 */
	public void print(PrintStream out, boolean wgts) throws Exception {
		StringWriter st = new StringWriter();
		write(st, wgts);
		out.println(st);
	}


	/**
	 * returns true if string contains a bootstrap value
	 *
	 * @return true, if label contains a non-negative float
	 */
	public static boolean isBootstrapValue(String label) {
		try {
			return Float.parseFloat(label) >= 0;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * allow different nodes to have the same names
	 */
	public boolean getAllowMultiLabeledNodes() {
		return allowMultiLabeledNodes;
	}

	/**
	 * allow different nodes to have the same names
	 */
	public void setAllowMultiLabeledNodes(boolean allowMultiLabeledNodes) {
		this.allowMultiLabeledNodes = allowMultiLabeledNodes;
	}

	/**
	 * is this bifurcating, do all nodes degree <=3?
	 *
	 * @return true, if binary
	 */
	public boolean isBifurcating() {
		return nodeStream().noneMatch(v -> v.getDegree() > 3);
	}

	/**
	 * is this a rooted network
	 *
	 * @return true, if is rooted network
	 */
	public boolean isReticulated() {
		return nodeStream().anyMatch(v -> v.getInDegree() >= 2);
	}

	/**
	 * compute weight of all edges
	 *
	 * @return sum of all none-negative edge weights
	 */
	public double computeTotalWeight() {
		return edgeStream().filter(e -> getWeight(e) > 0).mapToDouble(this::getWeight).sum();
	}

	/**
	 * given a rooted tree and a set of collapsed nodes, returns a tree that contains
	 * only the uncollapsed part of the tree
	 */
	public void extractTree(PhyloTree src, NodeSet collapsedNodes) {
		clear();
		if (src.getRoot() != null) {
			NodeArray<Node> oldNode2newNode = super.copy(src);

			if (getRoot() != null && oldNode2newNode != null) {
				setRoot(oldNode2newNode.get(src.getRoot()));
			}

			NodeSet toDelete = new NodeSet(this);
			toDelete.addAll();
			extractTreeRec(src.getRoot(), null, collapsedNodes, oldNode2newNode, toDelete);
			while (!toDelete.isEmpty()) {
				Node v = toDelete.getFirstElement();
				toDelete.remove(v);
				deleteNode(v);
			}
		}
	}

	/**
	 * recursively does the work
	 */
	private void extractTreeRec(Node v, Edge e, NodeSet collapsedNodes, NodeArray<Node> oldNode2newNode, NodeSet toDelete) {
		if (oldNode2newNode != null)
			toDelete.remove(oldNode2newNode.get(v));
		if (!collapsedNodes.contains(v)) {
			for (var f : v.adjacentEdges()) {
				if (f != e && this.okToDescendDownThisEdgeInTraversal(f, v)) {
					extractTreeRec(f.getOpposite(v), f, collapsedNodes, oldNode2newNode, toDelete);
				}
			}
		}
	}

	/**
	 * hide collapsed subtrees on write?
	 *
	 * @return true, if hidden
	 */
	public boolean isHideCollapsedSubTreeOnWrite() {
		return hideCollapsedSubTreeOnWrite;
	}

	/**
	 * hide collapsed subtrees on write?
	 */
	public void setHideCollapsedSubTreeOnWrite(boolean hideCollapsedSubTreeOnWrite) {
		this.hideCollapsedSubTreeOnWrite = hideCollapsedSubTreeOnWrite;
	}

	/**
	 * redirect edges away from root. Assumes that reticulate edges already point away from root
	 */
	public void redirectEdgesAwayFromRoot() {
		redirectEdgesAwayFromRootRec(getRoot(), null);

	}

	/**
	 * recursively does the work
	 */
	private void redirectEdgesAwayFromRootRec(Node v, Edge e) {
		if (e != null && v != e.getTarget() && !isReticulateEdge(e))
			e.reverse();
		for (var f : IteratorUtils.asList(v.adjacentEdges())) {
			if (f != e && this.okToDescendDownThisEdgeInTraversal(f, v))
				redirectEdgesAwayFromRootRec(f.getOpposite(v), f);
		}
	}

	/**
	 * gets the LSA-to-children map
	 *
	 * @return children of a node in the LSA tree
	 */
	public NodeArray<List<Node>> getLSAChildrenMap() {
		if (lsaChildrenMap == null) {
			synchronized (this) {
				if (lsaChildrenMap == null)
					lsaChildrenMap = newNodeArray();
			}
		}

		return lsaChildrenMap;
	}

	/**
	 * iterable over all children, if tree, or all children in LSA tree, if network
	 *
	 * @param v node
	 * @return iterable
	 */
	public Iterable<Node> lsaChildren(Node v) {
		if (lsaChildrenMap != null && lsaChildrenMap.get(v) != null)
			return lsaChildrenMap.get(v);
		else
			return v.children();
	}

	public boolean isLeaf(Node v) {
		return v.getOutDegree() == 0;
	}

	/**
	 * determines whether this node is a leaf in tree, if tree, or in the LSA tree, if network
	 *
	 * @param v node
	 * @return true, if leaf in LSA tree
	 */
	public boolean isLsaLeaf(Node v) {
		return v.isLeaf() || lsaChildrenMap != null && lsaChildrenMap.get(v) != null && lsaChildrenMap.get(v).size() == 0;
	}

	/**
	 * gets the first out edge in the LSA tree
	 *
	 * @param v node
	 * @return first edge
	 */
	public Node getFirstChildLSA(Node v) {
		for (var first : lsaChildren(v))
			return first;
		return null;
	}

	/**
	 * gets the last out edge in the LSA tree
	 *
	 * @param v node
	 * @return last edge
	 */
	public Node getLastChildLSA(Node v) {
		Node last = null;
		for (Node node : lsaChildren(v))
			last = node;
		return last;
	}

	/**
	 * compute the cycle for this tree and then return it
	 *
	 * @return cycle for this tree
	 */
	public int[] getCycle(Node v) {
		computeCycleRec(v, null, 0);
		return getCycle();
	}

	/**
	 * recursively compute a cycle
	 */
	private int computeCycleRec(Node v, Edge e, int pos) {
		for (Integer t : getTaxa(v)) {
			setTaxon2Cycle(t, ++pos);
		}
		for (var f : v.adjacentEdges()) {
			if (f != e && this.okToDescendDownThisEdgeInTraversal(f, v))
				pos = computeCycleRec(f.getOpposite(v), f, pos);
		}
		return pos;
	}


	public boolean isTreeEdge(Edge e) {
		return !isReticulateEdge(e);
	}

	/**
	 * determines whether edge represents a transfer.
	 * This is the case if the edge is a reticulate edge and has non-positive weight
	 *
	 * @return true if transfer edge
	 */
	public boolean isTransferEdge(Edge e) {
		if (SUPPORT_RICH_NEWICK)
			return isReticulateEdge(e) && !isTransferAcceptorEdge(e) && e.getTarget().inEdgesStream(false).anyMatch(this::isTransferAcceptorEdge);
		else
			return isReticulateEdge(e) && getWeight(e) < 0.0;
	}

	/**
	 * applies method to all nodes in preorder traversal
	 *
	 * @param method method to apply
	 */
	public void preorderTraversal(Consumer<Node> method) {
		preorderTraversal(getRoot(), method);
	}

	/**
	 * performs a pre-order traversal at node v. If rooted network, will visit some nodes more than once
	 *
	 * @param v      the root node
	 * @param method method to apply
	 */
	public void preorderTraversal(Node v, Consumer<Node> method) {
		method.accept(v);
		for (var e : v.outEdges()) {
			preorderTraversal(e.getTarget(), method);
		}
	}

	/**
	 * performs a pre-order traversal at node v. If rooted network, will visit some nodes more than once
	 *
	 * @param v         the root node
	 * @param condition must evaluate to true for node to be visited
	 * @param method    method to apply
	 */
	public void preorderTraversal(Node v, Function<Node, Boolean> condition, Consumer<Node> method) {
		if (condition.apply(v)) {
			method.accept(v);
			for (var e : v.outEdges()) {
				preorderTraversal(e.getTarget(), condition, method);
			}
		}
	}

	/**
	 * applies method to all nodes in postorder traversal. If rooted network, will visit some nodes more than once
	 *
	 * @param method method to apply
	 */
	public void postorderTraversal(Consumer<Node> method) {
		postorderTraversal(getRoot(), method);
	}

	/**
	 * performs a post-order traversal at node v. If rooted network, will visit some nodes more than once
	 *
	 * @param v      the root node
	 * @param method method to apply
	 */
	public void postorderTraversal(Node v, Consumer<Node> method) {
		for (var e : v.outEdges()) {
			postorderTraversal(e.getTarget(), method);
		}
		method.accept(v);
	}

	/**
	 * performs a post-order traversal at node v. If rooted network, will visit some nodes more than once
	 *
	 * @param v         the root node
	 * @param condition must evaluate to true for node to be visited
	 * @param method    method to apply
	 */
	public void postorderTraversal(Node v, Function<Node, Boolean> condition, Consumer<Node> method) {
		if (condition.apply(v)) {
			for (var e : v.outEdges()) {
				postorderTraversal(e.getTarget(), condition, method);
			}
			method.accept(v);
		}
	}

	public void breathFirstTraversal(BiConsumer<Integer, Node> method) {
		breathFirstTraversal(getRoot(), 1, method);
	}

	public void breathFirstTraversal(Node v, int level, BiConsumer<Integer, Node> method) {
		method.accept(level, v);
		for (var e : v.outEdges()) {
			breathFirstTraversal(e.getTarget(), level + 1, method);
		}
	}

	/**
	 * is this a reticulated edge?
	 *
	 * @param e edge
	 * @return true, if marked as reticulate
	 */
	public boolean isReticulateEdge(Edge e) {
		return e != null && reticulateEdges != null && reticulateEdges.contains(e);
	}

	/**
	 * mark as reticulated or not
	 *
	 * @param e          edge
	 * @param reticulate is reticulate
	 */
	public void setReticulate(Edge e, boolean reticulate) {
		if (reticulate)
			getReticulateEdges().add(e);
		else if (reticulateEdges != null)
			getReticulateEdges().remove(e);
	}

	public EdgeSet getReticulateEdges() {
		if (reticulateEdges == null) {
			synchronized (this) {
				if (reticulateEdges == null) {
					reticulateEdges = newEdgeSet();
				}
			}
		}
		return reticulateEdges;
	}

	public boolean hasReticulateEdges() {
		return reticulateEdges != null && reticulateEdges.size() > 0;
	}

	/**
	 * mark as acceptord or not
	 *
	 * @param e        edge
	 * @param acceptor is acceptor
	 */
	public void setTransferAcceptor(Edge e, boolean acceptor) {
		setReticulate(e, acceptor);
		if (acceptor)
			getTransferAcceptorEdges().add(e);
		else if (transferAcceptorEdges != null)
			getTransferAcceptorEdges().remove(e);
	}

	public boolean isTransferAcceptorEdge(Edge e) {
		if (SUPPORT_RICH_NEWICK)
			return transferAcceptorEdges != null && transferAcceptorEdges.contains(e);
		else
			return isReticulateEdge(e) && getWeight(e) > 0;
	}

	public EdgeSet getTransferAcceptorEdges() {
		if (transferAcceptorEdges == null) {
			synchronized (this) {
				if (transferAcceptorEdges == null) {
					transferAcceptorEdges = newEdgeSet();
				}
			}
		}
		return transferAcceptorEdges;
	}

	public boolean hasTransferAcceptorEdges() {
		return transferAcceptorEdges != null && transferAcceptorEdges.size() > 0;
	}

	/**
	 * gets the number of reticulate edges
	 *
	 * @return number of reticulate edges
	 */
	public int getNumberReticulateEdges() {
		return reticulateEdges == null ? 0 : reticulateEdges.size();
	}

	/**
	 * iterable over all reticulate edges
	 */
	public Iterable<Edge> reticulateEdges() {
		return reticulateEdges != null ? reticulateEdges : Collections.emptySet();
	}

	/**
	 * determines whether it is ok to descend an edge in a recursive
	 * traverse of a tree. Use this to ensure that each node is visited only once
	 *
	 * @return true, if we should descend this edge, false else
	 */
	public boolean okToDescendDownThisEdgeInTraversal(Edge e, Node v) {
		if (!isReticulateEdge(e))
			return true;
		else {
			if (v != e.getSource())
				return false; // only go DOWN reticulate edges.
			return e == e.getTarget().inEdgesStream(false).filter(this::isReticulateEdge).findFirst().orElse(null);
		}
	}

	/**
	 * determines whether it is ok to descend an edge in a recursive
	 * traverse of a tree. Use this to ensure that each node is visited only once
	 */
	public boolean okToDescendDownThisEdgeInTraversal(Edge e) {
		if (!isReticulateEdge(e))
			return true;
		else {
			return e == e.getTarget().inEdgesStream(false).filter(this::isReticulateEdge).findFirst().orElse(null);
		}
	}

	/**
	 * get edge that corresponds to the split
	 *
	 * @param partA one side of split
	 * @param partB other slide of split
	 * @return separating edge, if it exists, otherwise null
	 */
	public Edge getEdgeForSplit(BitSet partA, BitSet partB) {
		var e = getEdgeForCluster(partA);
		if (e == null)
			e = getEdgeForCluster(partB);
		return e;
	}

	/**
	 * get the edge that separates the given cluster from other taxa
	 *
	 * @param cluster the taxa to be separated
	 * @return separating edge or null
	 */
	public Edge getEdgeForCluster(BitSet cluster) {
		var pair = getEdgeForTaxaRec(getRoot(), cluster);
		return pair == null ? null : pair.getFirst();
	}

	private Pair<Edge, BitSet> getEdgeForTaxaRec(Node v, BitSet cluster) {
		var here = new BitSet();
		for (var t : getTaxa(v)) {
			here.set(t);
		}
		if (!BitSetUtils.contains(cluster, here))
			return null;

		var hasBadChild = false;
		for (var w : v.children()) {
			var pair = getEdgeForTaxaRec(w, cluster);
			if (pair == null)
				hasBadChild = true;
			else if (pair.getFirst() != null) // result contains seeked edge
				return pair;
			else
				here.or(pair.getSecond());
		}
		if (hasBadChild)
			return null;
		else if (here.equals(cluster))
			return new Pair<>(v.getFirstInEdge(), here);
		else
			return new Pair<>(null, here);
	}

	public record NewickOutputFormat(boolean weights, boolean confidenceAsNodeLabel, boolean confidenceUsingColon,
									 boolean probabilityUsingColon, boolean edgeLabelsAsComments) {
		}
}

// EOF
