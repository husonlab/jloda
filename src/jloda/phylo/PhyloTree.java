/*
 * PhyloTree.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.phylo;

import jloda.graph.*;
import jloda.util.*;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Phylogenetic tree, with support for rooted phylogenetic network
 * Daniel Huson, 2003
 */
public class PhyloTree extends PhyloSplitsGraph {
    public static final boolean ALLOW_WRITE_RETICULATE = true;
    public static final boolean ALLOW_READ_RETICULATE = true;

    public boolean allowMultiLabeledNodes = true;

    Node root = null;
    boolean inputHasMultiLabels = false;
    static boolean warnMultiLabeled = true;
    private boolean hideCollapsedSubTreeOnWrite = false;
    public static final String COLLAPSED_NODE_SUFFIX = "{+}";

    private final boolean cleanLabelsOnWrite;

    private EdgeSet reticulatedEdges;
    protected NodeArray<List<Node>> lsaChildrenMap; // keep track of children in LSA tree in network

    /**
     * Construct a new empty phylogenetic tree.
     */
    public PhyloTree() {
        super();
        cleanLabelsOnWrite = ProgramProperties.get("cleanTreeLabelsOnWrite", false);
    }

    /**
     * copy constructor
     *
     * @param src
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
        if (reticulatedEdges != null)
            reticulatedEdges.clear();
        if (lsaChildrenMap != null)
            lsaChildrenMap.clear();
    }

    /**
     * copies a phylogenetic tree
     *
     * @param src original tree
     * @return mapping of old nodes to new nodes
     */
    public NodeArray<Node> copy(PhyloTree src) {
        NodeArray<Node> oldNode2NewNode = src.newNodeArray();
        copy(src, oldNode2NewNode, null);
        return oldNode2NewNode;
    }

    /**
     * copies a phylogenetic tree
     *
     * @param src
     * @param oldNode2NewNode
     * @param oldEdge2NewEdge
     */
    public void copy(PhyloTree src, NodeArray<Node> oldNode2NewNode, EdgeArray<Edge> oldEdge2NewEdge) {
        if (oldEdge2NewEdge == null)
            oldEdge2NewEdge = new EdgeArray<>(src);
        oldNode2NewNode = super.copy(src, oldNode2NewNode, oldEdge2NewEdge);
        // super.copy(src, oldNode2NewNode, oldEdge2NewEdge);
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
        if (src.reticulatedEdges != null) {
            for (var e : src.reticulatedEdges) {
                setReticulated(oldEdge2NewEdge.get(e), true);
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
     * Sets the label of an edge.
     *
     * @param e Edge
     * @param a String
     */
    public void setLabel(Edge e, String a) throws NotOwnerException {
        super.setLabel(e, a);
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
        try (var sw = new StringWriter()) {
            write(sw, showWeights);
            return sw.toString();
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
     * Given a string representation of a tree, returns the tree.
     *
     * @param str      String
     * @param keepRoot Boolean. Set the root as the top level node and remove deg 2 nodes
     * @return tree PhyloTree
     */
    static public PhyloTree valueOf(String str, boolean keepRoot) throws IOException {
        var tree = new PhyloTree();
        tree.parseBracketNotation(str, keepRoot);
        return tree;
    }

    /**
     * Read a tree in Newick notation as unrooted tree
     *
     * @param r the reader
     */
    public void read(Reader r) throws IOException {
        read(r, false);

    }

    /**
     * Read a tree in Newick notation, as rooted tree, if desired
     *
     * @param r      the reader
     * @param rooted read as rooted tree
     */
    public void read(final Reader r, final boolean rooted) throws IOException {
        final BufferedReader br;
        if (r instanceof BufferedReader)
            br = (BufferedReader) r;
        else
            br = new BufferedReader(r);
        parseBracketNotation(br.readLine(), rooted);
    }

    /**
     * parse a tree in newick format, as a rooted tree, if desired.
     */
    public void parseBracketNotation(String str, boolean rooted) throws IOException {
        parseBracketNotation(str, rooted, true);
    }


    /**
     * parse a tree in newick format, as a rooted tree, if desired.
     */
    public void parseBracketNotation(String str, boolean rooted, boolean doClear) throws IOException {
        if (doClear)
            clear();
        setInputHasMultiLabels(false);
        var seen = new HashMap<String, Node>();

        var hasWeights = new Single<>(false);
        try {
            parseBracketNotationRecursively(seen, 0, null, 0, str, hasWeights);
        } catch (IOException ex) {
            System.err.println(str);
            throw ex;
        }
        final var v = getFirstNode();
        if (v != null) {
            if (rooted) {
                setRoot(v);
                if (!hasWeights.get() && isUnlabeledDiVertex(v)) {
                    setWeight(v.getFirstAdjacentEdge(), 0.5);
                    setWeight(v.getLastAdjacentEdge(), 0.5);
                }
            } else {
                if (isUnlabeledDiVertex(v))
                    setRoot(delDivertex(v).getSource());
                else setRoot(v);
            }
        }

        if (ALLOW_READ_RETICULATE)
            postProcessReticulate();

        // System.err.println("Bootstrap values detected:    " + getInputHasBootstrapValuesOnNodes());
        // System.err.println("Multi-labeled nodes detected: " + getInputHasMultiLabels());
    }

    private static final String punctuationCharacters = "),;:";

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
    private int parseBracketNotationRecursively(Map<String, Node> seen, int depth, Node v, int pos, String str, Single<Boolean> hasWeights) throws IOException {
        for (pos = StringUtils.skipSpaces(str, pos); pos < str.length(); pos = StringUtils.skipSpaces(str, pos + 1)) {
            var w = newNode();
            String label = null;
            if (str.charAt(pos) == '(') {
                pos = parseBracketNotationRecursively(seen, depth + 1, w, pos + 1, str, hasWeights);
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
                            // give first occurence of this label the suffix .1
                            final Node old = seen.get(label);
                            if (old != null) // change label of node
                            {
                                setLabel(old, label + ".1");
                                seen.put(label, null); // keep label in, but null indicates has changed
                                seen.put(label + ".1", old);
                                setInputHasMultiLabels(true);
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
                            setInputHasMultiLabels(true);
                            if (getWarnMultiLabeled())
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

            if (pos < str.length() && str.charAt(pos) == ':') { // edge weight is following
                pos = StringUtils.skipSpaces(str, pos + 1);
                var pos0 = pos;
                var buf = new StringBuilder();
                while (pos < str.length() && (punctuationCharacters.indexOf(str.charAt(pos)) == -1 && str.charAt(pos) != '['))
                    buf.append(str.charAt(pos++));
                var number = buf.toString().trim();
                try {
                    var weight = Math.max(0, Double.parseDouble(number));
                    if (e != null)
                        setWeight(e, weight);
                    didReadWeight = true;
                    hasWeights.set(true);
                } catch (Exception ex) {
                    throw new IOException("Expected number at position " + pos0 + " (got: '" + number + "')");
                }
            }

            // adjust edge weights for reticulate edges
            if (e != null) {
                try {
                    if (label != null && PhyloTreeNetworkIOUtils.isReticulateNode(label)) {
                        // if an instance of a reticulate node is marked ##, then we will set the weight of the edge to the node to a number >0
                        // to indicate that edge should be drawn as a tree edge
                        if (PhyloTreeNetworkIOUtils.isReticulateAcceptorEdge(label)) {
                            if (!didReadWeight || getWeight(e) <= 0)
                                setWeight(e, 0.000001);
                        } else {
                            if (getWeight(e) > 0)
                                setWeight(e, 0.0);
                        }
                    }
                } catch (IllegalSelfEdgeException e1) {
                    Basic.caught(e1);
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
     * is v an unlabeled node of degree 2?
     *
     * @param v
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

        Edge e = getFirstAdjacentEdge(v);
        Edge f = getLastAdjacentEdge(v);

        Node x = getOpposite(v, e);
        Node y = getOpposite(v, f);

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
        if (root == v)
            root = null;
        deleteNode(v);
        return g;
    }

    /**
     * Writes a tree in bracket notation
     *
     * @param w            the writer
     * @param writeWeights write edge weights or not
     */
    public void write(Writer w, boolean writeWeights) throws IOException {
        write(w, writeWeights, false, null, null);
    }

    /**
     * Writes a tree in bracket notation
     *
     * @param w            the writer
     * @param writeWeights write edge weights or not
     */
    public void write(Writer w, boolean writeWeights, boolean writeEdgeLabels) throws IOException {
        write(w, writeWeights, writeEdgeLabels, null, null);
    }

    private int outputNodeNumber = 0;
    private int outputEdgeNumber = 0;
    private NodeIntArray outputNodeReticulationNumberMap;  // global number of the reticulate node
    private int outputReticulationNumber;

    /**
     * Writes a tree in bracket notation. Uses extended bracket notation to write reticulate network
     *
     * @param w                the writer
     * @param writeEdgeWeights write edge weights or not
     * @param nodeId2Number    if non-null, will contain node-id to number mapping after call
     * @param edgeId2Number    if non-null, will contain edge-id to number mapping after call
     */
    public void write(Writer w, boolean writeEdgeWeights, boolean writeEdgeLabels, Map<Integer, Integer> nodeId2Number, Map<Integer, Integer> edgeId2Number) throws IOException {
        outputNodeNumber = 0;
        outputEdgeNumber = 0;
        if (ALLOW_WRITE_RETICULATE) {
            // following two lines enable us to write cluster networks and reticulate networks in Newick format
            if (outputNodeReticulationNumberMap == null)
                outputNodeReticulationNumberMap = newNodeIntArray();
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
            writeRec(w, root, null, writeEdgeWeights, writeEdgeLabels, nodeId2Number, edgeId2Number, getLabelForWriting(root));
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
    private void writeRec(Writer outs, Node v, Edge e, boolean writeEdgeWeights, boolean writeEdgeLabels, Map<Integer, Integer> nodeId2Number, Map<Integer, Integer> edgeId2Number, String nodeLabel) throws IOException {
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
                    boolean inEdgeHasWeight = (getWeight(f) > 0);

                    if (isReticulatedEdge(f)) {
                        if (outputNodeReticulationNumberMap.get(w) == null) {
                            outputNodeReticulationNumberMap.set(w, ++outputReticulationNumber);
                            final String label;
                            if (getLabel(w) != null)
                                label = getLabelForWriting(w) + PhyloTreeNetworkIOUtils.makeReticulateNodeLabel(inEdgeHasWeight, outputNodeReticulationNumberMap.get(w));
                            else
                                label = PhyloTreeNetworkIOUtils.makeReticulateNodeLabel(inEdgeHasWeight, outputNodeReticulationNumberMap.get(w));

                            writeRec(outs, w, f, writeEdgeWeights, writeEdgeLabels, nodeId2Number, edgeId2Number, label);
                        } else {
                            String label;
                            if (getLabel(w) != null)
                                label = getLabelForWriting(w) + PhyloTreeNetworkIOUtils.makeReticulateNodeLabel(inEdgeHasWeight, outputNodeReticulationNumberMap.get(w));
                            else
                                label = PhyloTreeNetworkIOUtils.makeReticulateNodeLabel(inEdgeHasWeight, outputNodeReticulationNumberMap.get(w));

                            outs.write(label);
                            if (writeEdgeWeights && getWeight(f) != 1.0 && getWeight(f) != -1.0) {
                                outs.write(StringUtils.removeTrailingZerosAfterDot(String.format(":%.8f", getWeight(f))));
                            }
                            if (writeEdgeLabels && getLabel(f) != null) {
                                outs.write("[" + getLabelForWriting(f) + "]");
                            }
                        }
                    } else
                        writeRec(outs, w, f, writeEdgeWeights, writeEdgeLabels, nodeId2Number, edgeId2Number, getLabelForWriting(w));
                }
                outs.write(")");
            }
            if (nodeLabel != null && nodeLabel.length() > 0)
                outs.write(nodeLabel);
        }

        if (writeEdgeWeights && e != null && (!isReticulatedEdge(e) || getWeight(e) != 1.0 && getWeight(e) != -1.0)) {
            outs.write(StringUtils.removeTrailingZerosAfterDot(String.format(":%.8f", getWeight(e))));
            if (writeEdgeLabels && getLabel(e) != null) {
                outs.write("[" + getLabelForWriting(e) + "]");
            }
        }
    }

    /**
     * get the label to be used for writing. Will have single quotes, if label contains punctuation character or white space
     *
     * @param v
     * @return
     */
    public String getLabelForWriting(Node v) {
        String label = cleanLabelsOnWrite ? getCleanLabel(v) : getLabel(v);
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
     *
     * @param e
     * @return
     */
    public String getLabelForWriting(Edge e) {
        String label = cleanLabelsOnWrite ? getCleanLabel(e) : getLabel(e);
        if (label != null) {
            for (int i = 0; i < label.length(); i++) {
                if (punctuationCharacters.indexOf(label.charAt(i)) != -1 || Character.isWhitespace(label.charAt(i)))
                    return "'" + label + "'";
            }
        }
        return label;
    }

    /**
     * gets a clean version of the label. This is a label that can be printed in a Newick string
     *
     * @return clean label
     */
    private String getCleanLabel(Node v) {
        var label = getLabel(v);
        if (label == null)
            return null;
        else {
            label = getLabel(v).trim();
            label = label.replaceAll("[ \\[\\](),:;]+", "_");
            if (label.length() > 0)
                return label;
            else
                return "_";
        }
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
                    List<Node> list = reticulateNumber2Nodes.computeIfAbsent(reticulateLabel, k -> new LinkedList<>());
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
                            if (e.getSource() == v) { /// move child of v to u
                                f = newEdge(u, e.getTarget());
                            } else { // move parent of v to u
                                f = newEdge(e.getSource(), u);
                            }
                            setSplit(f, getSplit(e));
                            setWeight(f, getWeight(e));
                            setLabel(f, getLabel(e));
                        }
                        deleteNode(v);
                    }
                }
                var transferAcceptorEdge = new Single<Edge>();
                for (var e : u.inEdges()) {
                    setReticulated(e, true);
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
     *
     * @param root
     */
    public void setRoot(Node root) {
        this.root = root;
    }

    /**
     * sets the root node in the middle of this edge
     *
     * @param e
     */
    public void setRoot(Edge e, EdgeArray<String> edgeLabels) {
        setRoot(e, getWeight(e) * 0.5, getWeight(e) * 0.5, edgeLabels);
    }

    /**
     * sets the root node in the middle of this edge
     *
     * @param e
     * @param weightToSource weight for new edge adjacent to source of e
     * @param weightToTarget weight for new adjacent to target of e
     */
    public void setRoot(Edge e, double weightToSource, double weightToTarget, EdgeArray<String> edgeLabels) {
        final var root = getRoot();
        if (root != null && root.getDegree() == 2 && (getTaxa(root) == null || getNumberOfTaxa(root) == 0)) {
            if (root == e.getSource()) {
                var f = (root.getFirstAdjacentEdge() != e ? root.getFirstAdjacentEdge() : root.getLastAdjacentEdge());
                setWeight(e, weightToSource);
                setWeight(f, weightToTarget);
                return; // root stays root
            } else if (root == e.getTarget()) {
                var f = (root.getFirstAdjacentEdge() != e ? root.getFirstAdjacentEdge() : root.getLastAdjacentEdge());
                setWeight(e, weightToTarget);
                setWeight(f, weightToSource);
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
     * @throws Exception
     */
    public void print(PrintStream out, boolean wgts) throws Exception {
        StringWriter st = new StringWriter();
        write(st, wgts);
        out.println(st.toString());
    }


    /**
     * was last read tree multi-labeled? If so, the parser replaces instances of the same label
     * by label.1, label.2 ...
     *
     * @return true, if input was multi labeled
     */
    public boolean getInputHasMultiLabels() {
        return inputHasMultiLabels;
    }

    private void setInputHasMultiLabels(boolean inputHasMultiLabels) {
        this.inputHasMultiLabels = inputHasMultiLabels;
    }


    /**
     * returns true if string contains a bootstrap value
     *
     * @param label
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
     *
     * @return
     */
    public boolean getAllowMultiLabeledNodes() {
        return allowMultiLabeledNodes;
    }

    /**
     * allow different nodes to have the same names
     *
     * @param allowMultiLabeledNodes
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
     * warn about multi-labeled trees in input?
     *
     * @return true, if warnings are given
     */
    static public boolean getWarnMultiLabeled() {
        return warnMultiLabeled;
    }

    /**
     * warn about multi-labeled trees in input?
     *
     * @param warnMultiLabeled
     */
    static public void setWarnMultiLabeled(boolean warnMultiLabeled) {
        PhyloTree.warnMultiLabeled = warnMultiLabeled;
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
            for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
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
        if (e != null && v != e.getTarget() && !isReticulatedEdge(e))
            e.reverse();
        for (var f : IteratorUtils.asList(v.adjacentEdges())) {
            if (f != e && this.okToDescendDownThisEdgeInTraversal(f, v))
                redirectEdgesAwayFromRootRec(f.getOpposite(v), f);
        }
    }

    /**
     * gets a clean version of the label. This is a label that can be printed in a Newick string
     */
    private String getCleanLabel(Edge v) {
        var label = getLabel(v);
        if (label == null)
            return null;
        else {
            label = getLabel(v).trim();
            label = label.replaceAll("[ \\[\\](),:]+", "_");
            if (label.length() > 0)
                return label;
            else
                return "_";
        }
    }

    /**
     * gets the LSA-to-children map
     *
     * @return children of a node in the LSA tree
     */
    public NodeArray<List<Node>> getLSAChildrenMap() {
        if (lsaChildrenMap == null)
            lsaChildrenMap = newNodeArray();

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

    /**
     * iterates over all nodes of degree 1
     */
    public Iterable<Node> leaves() {
        return () -> new Iterator<>() {
            private Node v = getFirstNode();

            {
                while (v != null && v.getDegree() > 1) {
                    v = v.getNext();
                }
            }

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public Node next() {
                final var result = v;
                {
                    v = v.getNext();
                    while (v != null) {
                        if (v.getDegree() == 1)
                            break;
                        else
                            v = v.getNext();
                    }
                }
                return result;
            }
        };
    }

    /**
     * counts all nodes of degree 1
     */
    public int countLeaves() {
        return IteratorUtils.count(leaves());
    }

    /**
     * returns a new tree in which all edges of length < min length have been contracted
     *
     * @param minLength
     * @return true, if anything contracted
     */
    public boolean contractShortEdges(double minLength) {
        return contractEdges(edgeStream().filter(e -> getWeight(e) < minLength).collect(Collectors.toSet()), null);
    }

    /**
     * returns a new tree in which all edges of length < min length have been contracted
     *
     * @return true, if anything contracted
     */
    public boolean contractEdges(Set<Edge> edgesToContract, Single<Boolean> selfEdgeEncountered) {
        boolean hasContractedOne = edgesToContract.size() > 0;

        while (edgesToContract.size() > 0) {
            final var e = edgesToContract.iterator().next();
            edgesToContract.remove(e);

            final var v = e.getSource();
            final var w = e.getTarget();

            for (Edge f : v.adjacentEdges()) { // will remove e from edgesToContract here
                if (f != e) {
                    final var u = f.getOpposite(v);
                    final var needsContracting = edgesToContract.contains(f);
                    if (needsContracting)
                        edgesToContract.remove(f);

                    if (u != w) {
                        final Edge z;
                        if (u == f.getSource())
                            z = newEdge(u, w);
                        else
                            z = newEdge(w, u);
                        setWeight(z, getWeight(f));
                        setConfidence(z, getConfidence(f));
                        setLabel(z, getLabel(z));
                        if (needsContracting) {
                            edgesToContract.add(z);
                        }
                    } else if (selfEdgeEncountered != null)
                        selfEdgeEncountered.set(true);
                }
            }
            for (var taxon : getTaxa(v))
                addTaxon(w, taxon);

            if (getRoot() == v)
                setRoot(w);

            deleteNode(v);
        }

        return hasContractedOne;
    }

    public boolean isTreeEdge(Edge e) {
        return !isReticulatedEdge(e);
    }

    /**
     * determines whether edge represents a transfer.
     * This is the case if the edge is a reticulate edge and has non-positive weight
     *
     * @return true if transfer edge
     */
    public boolean isTransferEdge(Edge e) {
        return isReticulatedEdge(e) && getWeight(e) < 0.0;
    }

    public boolean isTransferAcceptorEdge(Edge e) {
        return isReticulatedEdge(e) && getWeight(e) > 0;
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
    public boolean isReticulatedEdge(Edge e) {
        return reticulatedEdges != null && reticulatedEdges.contains(e);
    }

    /**
     * mark as reticulated or not
     *
     * @param e          edge
     * @param reticulate is reticulate
     */
    public void setReticulated(Edge e, boolean reticulate) {
        if (reticulatedEdges == null) {
            if (!reticulate)
                return;
            reticulatedEdges = newEdgeSet();
        }
        if (reticulate)
            reticulatedEdges.add(e);
        else
            reticulatedEdges.remove(e);
    }

    /**
     * gets the number of reticulate edges
     *
     * @return number of reticulate edges
     */
    public int getNumberReticulateEdges() {
        return reticulatedEdges == null ? 0 : reticulatedEdges.size();
    }

    /**
     * iterable over all reticulate edges
     */
    public Iterable<Edge> reticulatedEdges() {
        return reticulatedEdges != null ? reticulatedEdges : Collections.emptySet();
    }

    /**
     * determines whether it is ok to descend an edge in a recursive
     * traverse of a tree. Use this to ensure that each node is visited only once
     *
     * @return true, if we should descend this edge, false else
     */
    public boolean okToDescendDownThisEdgeInTraversal(Edge e, Node v) {
        if (!isReticulatedEdge(e))
            return true;
        else {
            if (v != e.getSource())
                return false; // only go DOWN reticulate edges.
            return e == e.getTarget().inEdgesStream(false).filter(this::isReticulatedEdge).findFirst().orElse(null);
        }
    }

    /**
     * determines whether it is ok to descend an edge in a recursive
     * traverse of a tree. Use this to ensure that each node is visited only once
     */
    public boolean okToDescendDownThisEdgeInTraversal(Edge e) {
        if (!isReticulatedEdge(e))
            return true;
        else {
            return e == e.getTarget().inEdgesStream(false).filter(this::isReticulatedEdge).findFirst().orElse(null);
        }
    }
}

// EOF
