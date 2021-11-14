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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Phylogenetic tree
 * Daniel Huson, 2003
 */
public class PhyloTree extends PhyloSplitsGraph {
    public static final boolean ALLOW_WRITE_RETICULATE = true;
    public static final boolean ALLOW_READ_RETICULATE = true;
    public static final boolean ALLOW_READ_WRITE_EDGE_LABELS = true;

    public boolean allowMultiLabeledNodes = true;

    Node root = null; // can be a node or edge
    boolean inputHasMultiLabels = false;
    static boolean warnMultiLabeled = true;
    private boolean hideCollapsedSubTreeOnWrite = false;
    public static final String COLLAPSED_NODE_SUFFIX = "{+}";

    private final boolean cleanLabelsOnWrite;

    private double weight = 1;

    protected NodeArray<List<Node>> node2GuideTreeChildren; // keep track of children in LSA tree in network

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
        node2GuideTreeChildren = null;
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
        oldNode2NewNode = super.copy(src, oldNode2NewNode, oldEdge2NewEdge);
        // super.copy(src, oldNode2NewNode, oldEdge2NewEdge);
        if (src.getRoot() != null) {
            Node root = src.getRoot();
            setRoot(oldNode2NewNode.get(root));
        }
        for (Node v = src.getFirstNode(); v != null; v = v.getNext()) {
            List<Node> children = src.getNode2GuideTreeChildren().get(v);
            if (children != null) {
                List<Node> newChildren = new LinkedList<>();
                for (Node w : children) {
                    newChildren.add(oldNode2NewNode.get(w));
                }
                getNode2GuideTreeChildren().put(oldNode2NewNode.get(v), newChildren);
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
        PhyloTree tree = new PhyloTree();
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
        try (StringWriter w = new StringWriter()) {
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
        try (StringWriter sw = new StringWriter()) {
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
        try (StringWriter sw = new StringWriter()) {
            if (translate == null || translate.size() == 0) {
                this.write(sw, true);
            } else {
                PhyloTree tmpTree = new PhyloTree();
                tmpTree.copy(this);
                for (Node v = tmpTree.getFirstNode(); v != null; v = v.getNext()) {
                    String key = tmpTree.getLabel(v);
                    if (key != null) {
                        String value = translate.get(key);
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
     *
     * @param writer
     * @param showWeights
     * @param labeler
     * @throws IOException
     */
    public void write(final Writer writer, final boolean showWeights, final Function<Node, String> labeler) throws IOException {
        if (labeler == null) {
            this.write(writer, showWeights);

        } else {
            PhyloTree tmpTree = new PhyloTree();
            tmpTree.copy(this);
            for (Node v = tmpTree.getFirstNode(); v != null; v = v.getNext()) {
                String label = labeler.apply(v);
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
        final PhyloTree tree = new PhyloTree();
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
     * parse a tree in Newick format, discarding the root
     *
     * @param str
     * @throws IOException
     */
    public void parseBracketNotation(String str) throws IOException {
        parseBracketNotation(str, false);
    }

    boolean hasWeights = false;

    /**
     * parse a tree in newick format, as a rooted tree, if desired.
     *
     * @param str
     * @param rooted maintain root, even if it has degree 2
     * @throws IOException
     */
    public void parseBracketNotation(String str, boolean rooted) throws IOException {
        parseBracketNotation(str, rooted, true);
    }


    /**
     * parse a tree in newick format, as a rooted tree, if desired.
     *
     * @param str
     * @param rooted   maintain root, even if it has degree 2
     * @param doClear: erase the existing tree?
     * @throws IOException
     */
    public void parseBracketNotation(String str, boolean rooted, boolean doClear) throws IOException {
        if (doClear)
            clear();
        setInputHasMultiLabels(false);
        Map<String, Node> seen = new HashMap<>();

        hasWeights = false;
        try {
            parseBracketNotationRecursively(seen, 0, null, 0, str);
        } catch (IOException ex) {
            System.err.println(str);
            throw ex;
        }
        final Node v = getFirstNode();
        if (v != null) {
            if (rooted) {
                setRoot(v);
                if (!hasWeights && isUnlabeledDiVertex(v)) {
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
    private static final String startOfNumber = "-.0123456789";

    /**
     * recursively do the work
     *
     * @param seen  set of seen labels
     * @param depth distance from root
     * @param v     parent node
     * @param i     current position in string
     * @param str   string
     * @return new current position
     * @throws IOException
     */
    private int parseBracketNotationRecursively(Map<String, Node> seen, int depth, Node v, int i, String str) throws IOException {
        try {
            for (i = StringUtils.skipSpaces(str, i); i < str.length(); i = StringUtils.skipSpaces(str, i + 1)) {
				Node w = newNode();
				String label = null;
				if (str.charAt(i) == '(') {
					i = parseBracketNotationRecursively(seen, depth + 1, w, i + 1, str);
					if (str.charAt(i) != ')')
						throw new IOException("Expected ')' at position " + i);
					i = StringUtils.skipSpaces(str, i + 1);
					while (i < str.length() && punctuationCharacters.indexOf(str.charAt(i)) == -1) {
						int i0 = i;
						StringBuilder buf = new StringBuilder();
                        boolean inQuotes = false;
                        while (i < str.length() && (inQuotes || punctuationCharacters.indexOf(str.charAt(i)) == -1)) {
                            if (str.charAt(i) == '\'')
                                inQuotes = !inQuotes;
                            else
                                buf.append(str.charAt(i));
                            i++;
                        }
                        label = buf.toString().trim();

                        if (label.length() > 0) {
                            if (!getAllowMultiLabeledNodes() && seen.containsKey(label) && PhyloTreeUtils.findReticulateLabel(label) == null)
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

                                int t = 1;
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
                            throw new IOException("Expected label at position " + i0);
                    }
                } else // everything to next ) : or , is considered a label:
                {
                    if (getNumberOfNodes() == 1)
                        throw new IOException("Expected '(' at position " + i);
                    int i0 = i;
                    final StringBuilder buf = new StringBuilder();
                    boolean inQuotes = false;
                    while (i < str.length() && (inQuotes || punctuationCharacters.indexOf(str.charAt(i)) == -1)) {
                        if (str.charAt(i) == '\'')
                            inQuotes = !inQuotes;
                        else
                            buf.append(str.charAt(i));
                        i++;
                    }
                    label = buf.toString().trim();

                    if (label.startsWith("'") && label.endsWith("'") && label.length() > 1)
                        label = label.substring(1, label.length() - 1).trim();


                    if (label.length() > 0) {
                        if (!getAllowMultiLabeledNodes() && seen.containsKey(label) && PhyloTreeUtils.findReticulateLabel(label) == null) {
                            // give first occurrence of this label the suffix .1
                            Node old = seen.get(label);
                            if (old != null) // change label of node
                            {
                                setLabel(old, label + ".1");
                                seen.put(label, null); // keep label in, but null indicates has changed
                                seen.put(label + ".1", old);
                                setInputHasMultiLabels(true);
                                if (getWarnMultiLabeled())
                                    System.err.println("multi-label: " + label);
                            }

                            int t = 1;
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
                        throw new IOException("Expected label at position " + i0);
                }
                Edge e = null;
                if (v != null)
                    e = newEdge(v, w);

                // detect and read embedded bootstrap values:
				i = StringUtils.skipSpaces(str, i);

                // read edge weights

                if (i < str.length() && str.charAt(i) == ':') // edge weight is following
                {
					i = StringUtils.skipSpaces(str, i + 1);
                    int i0 = i;
                    StringBuilder buf = new StringBuilder();
                    while (i < str.length() && (punctuationCharacters.indexOf(str.charAt(i)) == -1 && str.charAt(i) != '['))
                        buf.append(str.charAt(i++));
                    String number = buf.toString().trim();
                    try {
                        double weight = Math.max(0, Double.parseDouble(number));
                        if (e != null)
                            setWeight(e, weight);
                        if (!hasWeights)
                            hasWeights = true;
                    } catch (Exception ex) {
                        throw new IOException("Expected number at position " + i0 + " (got: '" + number + "')");
                    }
                }

                // adjust edge weights for reticulate adjacentEdges
                if (e != null) {
                    try {
                        if (label != null && PhyloTreeUtils.isReticulateNode(label)) {
                            // if an instance of a reticulate node is marked ##, then we will set the weight of the edge to the node to a number >0
                            // to indicate that edge should be drawn as a tree node
                            if (PhyloTreeUtils.isReticulateAcceptorEdge(label)) {
                                if (getWeight(e) <= 0)
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
                if (i >= str.length()) {
                    if (depth == 0)
                        return i; // finished parsing tree
                    else
                        throw new IOException("Unexpected end of line");
                }
                if (str.charAt(i) == '[') // edge label
                {
                    int x = str.indexOf('[', i + 1);
                    int j = str.indexOf(']', i + 1);
                    if (j == -1 || (x != -1 && x < j))
                        throw new IOException("Error in edge label at position: " + i);
                    setLabel(e, str.substring(i + 1, j));
                    i = j + 1;
                }
                if (str.charAt(i) == ';' && depth == 0)

                    return i; // finished parsing tree
                else if (str.charAt(i) == ')')
                    return i;
                else if (str.charAt(i) != ',')
                    throw new IOException("Unexpected '" + str.charAt(i) + "' at position " + i);
            }
        } catch (NotOwnerException ex) {
            throw new IOException(ex);
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
     * @param outs         the writer
     * @param writeWeights write edge weights or not
     */
    public void write(Writer outs, boolean writeWeights) throws IOException {
        write(outs, writeWeights, false, null, null);
    }

    /**
     * Writes a tree in bracket notation
     *
     * @param outs         the writer
     * @param writeWeights write edge weights or not
     */
    public void write(Writer outs, boolean writeWeights, boolean writeEdgeLabels) throws IOException {
        write(outs, writeWeights, writeEdgeLabels, null, null);
    }

    private int nodeNumber = 0;
    private int edgeNumber = 0;
    private NodeIntArray node2reticulateNumber;  // global number of the reticulate node
    private int reticulateNodeNumber;

    /**
     * Writes a tree in bracket notation. Uses extended bracket notation to write reticulate network
     *
     * @param w                the writer
     * @param writeEdgeWeights write edge weights or not
     * @param nodeId2Number    if non-null, will contain node-id to number mapping after call
     * @param edgeId2Number    if non-null, will contain edge-id to number mapping after call
     */
    public void write(Writer w, boolean writeEdgeWeights, boolean writeEdgeLabels, Map<Integer, Integer> nodeId2Number, Map<Integer, Integer> edgeId2Number) throws IOException {
        nodeNumber = 0;
        edgeNumber = 0;
        if (ALLOW_WRITE_RETICULATE) {
            // following two lines enable us to write cluster networks and reticulate networks in Newick format
            node2reticulateNumber = new NodeIntArray(this);
            reticulateNodeNumber = 0;
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
    }

    /**
     * Recursively writes a tree in bracket notation
     *
     * @param outs
     * @param v
     * @param e
     * @param writeEdgeWeights
     * @param writeEdgeLabels
     * @param nodeId2Number
     * @param edgeId2Number
     * @param nodeLabel
     * @throws IOException
     */
    private void writeRec(Writer outs, Node v, Edge e, boolean writeEdgeWeights, boolean writeEdgeLabels, Map<Integer, Integer> nodeId2Number, Map<Integer, Integer> edgeId2Number, String nodeLabel) throws IOException {
        if (nodeId2Number != null)
            nodeId2Number.put(v.getId(), ++nodeNumber);

        if (!isHideCollapsedSubTreeOnWrite() || getLabel(v) == null || !getLabel(v).endsWith(PhyloTree.COLLAPSED_NODE_SUFFIX)) {
            if (v.getOutDegree() > 0) {
                outs.write("(");
                boolean first = true;
                for (Edge f : v.outEdges()) {
                    if (edgeId2Number != null)
                        edgeId2Number.put(f.getId(), ++edgeNumber);

                    if (first)
                        first = false;
                    else
                        outs.write(",");

                    final Node w = f.getTarget();
                    boolean inEdgeHasWeight = (getWeight(f) > 0);

                    if (isSpecial(f)) {
                        if (node2reticulateNumber.get(w) == null) {
                            node2reticulateNumber.set(w, ++reticulateNodeNumber);
                            final String label;
                            if (getLabel(w) != null)
                                label = getLabelForWriting(w) + PhyloTreeUtils.makeReticulateNodeLabel(inEdgeHasWeight, node2reticulateNumber.get(w));
                            else
                                label = PhyloTreeUtils.makeReticulateNodeLabel(inEdgeHasWeight, node2reticulateNumber.get(w));

                            writeRec(outs, w, f, writeEdgeWeights, writeEdgeLabels, nodeId2Number, edgeId2Number, label);
                        } else {
                            String label;
                            if (getLabel(w) != null)
                                label = getLabelForWriting(w) + PhyloTreeUtils.makeReticulateNodeLabel(inEdgeHasWeight, node2reticulateNumber.get(w));
                            else
                                label = PhyloTreeUtils.makeReticulateNodeLabel(inEdgeHasWeight, node2reticulateNumber.get(w));

                            outs.write(label);
                            if (writeEdgeWeights) {
                                outs.write(":" + getWeight(f));
                                if (writeEdgeLabels && getLabel(f) != null) {
                                    outs.write("[" + getLabelForWriting(f) + "]");
                                }
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

        if (writeEdgeWeights && e != null) {
            outs.write(":" + (getWeight(e)));
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
     * @param v
     * @return clean label
     */
    private String getCleanLabel(Node v) {
        String label = getLabel(v);
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
     *
     * @return number of reticulate nodes
     */
    public void postProcessReticulate() {
        // determine all the groups of reticulate ndoes
        Map<String, List<Node>> reticulateNumber2Nodes = new HashMap<>(); // maps each reticulate-node prefix to the set of all nodes that have it

        for (Node v = getFirstNode(); v != null; v = v.getNext()) {
            String label = getLabel(v);
            if (label != null && label.length() > 0) {
                String reticulateLabel = PhyloTreeUtils.findReticulateLabel(label);
                if (reticulateLabel != null) {
                    setLabel(v, PhyloTreeUtils.removeReticulateNodeSuffix(label));
                    List<Node> list = reticulateNumber2Nodes.computeIfAbsent(reticulateLabel, k -> new LinkedList<>());
                    list.add(v);
                }
            }
        }

        // collapse all instances of a reticulate node into one node
        for (String reticulateNumber : reticulateNumber2Nodes.keySet()) {
            final List<Node> list = reticulateNumber2Nodes.get(reticulateNumber);
            if (list.size() > 0) {
                Node u = null;
                for (Node v : list) {
                    if (u == null) {
                        u = v;
                    } else {
                        if (getLabel(v) != null) {
                            if (getLabel(u) == null)
                                setLabel(u, getLabel(v));
                            else if (!getLabel(u).equals(getLabel(v)))
                                setLabel(u, getLabel(u) + "," + getLabel(v));
                        }

                        for (Edge e : v.adjacentEdges()) {
                            final Edge f;
                            if (e.getSource() == v) { /// transfer child of v to u
                                f = newEdge(u, e.getTarget());
                            } else { // transfer parent of v to u
                                f = newEdge(e.getSource(), u);
                            }
                            setSplit(f, getSplit(e));
                            setWeight(f, getWeight(e));
                            setLabel(f, getLabel(e));
                        }
                        deleteNode(v);
                    }
                }
                boolean hasReticulateAcceptorEdge = false;
                for (Edge e : u.inEdges()) {
                    setSpecial(e, true);
                    if (getWeight(e) > 0)
                        if (!hasReticulateAcceptorEdge)
                            hasReticulateAcceptorEdge = true;
                        else {
                            setWeight(e, 0.0);
                            System.err.println("Warning: node has more than one reticulate-acceptor edge, will only use first");
                        }
                }
                if (hasReticulateAcceptorEdge) {
                    for (Edge e : u.inEdges()) {
                        if (getWeight(e) == 0)
                            setWeight(e, -1.0);
                    }
                }
            }
        }
    }

    /**
     * computes mapping of node ids to numbers 1..numberOfNodes and of edge ids to numbers 1..numberOfEdges.
     * Proceeds recursively from the root of the tree
     *
     * @param nodeId2Number
     * @param edgeId2Number
     */
    public void setId2NumberMaps(Map<Integer, Integer> nodeId2Number, Map<Integer, Integer> edgeId2Number) {
        if (getRoot() != null)
            setId2NumberMapsRec(getRoot(), null, new Pair<>(0, 0), nodeId2Number, edgeId2Number);
    }

    /**
     * recursively does the work
     *
     * @param v
     * @param e
     * @param nodeNumberEdgeNumber
     * @param nodeId2Number
     * @param edgeId2Number
     */
    private void setId2NumberMapsRec(Node v, Edge e, Pair<Integer, Integer> nodeNumberEdgeNumber, Map<Integer, Integer> nodeId2Number, Map<Integer, Integer> edgeId2Number) {
        int nodes = nodeNumberEdgeNumber.getFirst() + 1;
        nodeNumberEdgeNumber.setFirst(nodes);
        nodeId2Number.put(v.getId(), nodes);
        for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f))
            if (f != e) {
                int edges = nodeNumberEdgeNumber.getSecond() + 1;
                nodeNumberEdgeNumber.setSecond(edges);
                edgeId2Number.put(v.getId(), edges);
                if (PhyloTreeUtils.okToDescendDownThisEdge(this, f, v))
                    setId2NumberMapsRec(f.getOpposite(v), f, nodeNumberEdgeNumber, nodeId2Number, edgeId2Number);
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
        final Node root = getRoot();
        if (root != null && root.getDegree() == 2 && (getTaxa(root) == null || getNumberOfTaxa(root) == 0)) {
            if (root == e.getSource()) {
                Edge f = (root.getFirstAdjacentEdge() != e ? root.getFirstAdjacentEdge() : root.getLastAdjacentEdge());
                setWeight(e, weightToSource);
                setWeight(f, weightToTarget);
                return; // root stays root
            } else if (root == e.getTarget()) {
                Edge f = (root.getFirstAdjacentEdge() != e ? root.getFirstAdjacentEdge() : root.getLastAdjacentEdge());
                setWeight(e, weightToTarget);
                setWeight(f, weightToSource);
                return; // root stays root
            }
            eraseRoot(edgeLabels);
        }
        Node v = e.getSource();
        Node w = e.getTarget();
        Node u = newNode();
        Edge vu = newEdge(v, u);
        Edge uw = newEdge(u, w);
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
     * erase the current root. If it has out-degree two and is not node-labeled, then two out adjacentEdges will be replaced by single edge
     *
     * @param edgeLabels if non-null and root has two out adjacentEdges, will try to copy one of the edge labels to the new edge
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
     * returns tree, if all nodes have degree <=3
     *
     * @return true, if binary
     */
    public boolean isBifurcating() {
        for (Node v = getFirstNode(); v != null; v = v.getNext())
            if (v.getDegree() > 3)
                return false;
        return true;
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
     *
     * @param src
     * @param collapsedNodes
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
     *
     * @param v
     * @param e
     * @param collapsedNodes
     * @param oldNode2newNode
     * @param toDelete
     */
    private void extractTreeRec(Node v, Edge e, NodeSet collapsedNodes, NodeArray<Node> oldNode2newNode, NodeSet toDelete) {
        toDelete.remove(oldNode2newNode.get(v));
        if (!collapsedNodes.contains(v)) {
            for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
                if (f != e && PhyloTreeUtils.okToDescendDownThisEdge(this, f, v)) {
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
     *
     * @param hideCollapsedSubTreeOnWrite
     */
    public void setHideCollapsedSubTreeOnWrite(boolean hideCollapsedSubTreeOnWrite) {
        this.hideCollapsedSubTreeOnWrite = hideCollapsedSubTreeOnWrite;
    }

    /**
     * redirect adjacentEdges away from root. Assumes that special adjacentEdges already point away from root
     */
    public void redirectEdgesAwayFromRoot() {
        redirectEdgesAwayFromRootRec(getRoot(), null);

    }

    /**
     * recursively does the work
     *
     * @param v
     * @param e
     */
    private void redirectEdgesAwayFromRootRec(Node v, Edge e) {
        if (e != null && v != e.getTarget() && !isSpecial(e))
            e.reverse();
		for (Edge f : IteratorUtils.asList(v.adjacentEdges())) {
			if (f != e && PhyloTreeUtils.okToDescendDownThisEdge(this, f, v))
				redirectEdgesAwayFromRootRec(f.getOpposite(v), f);
		}
    }

    /**
     * gets a clean version of the label. This is a label that can be printed in a Newick string
     *
     * @param v
     * @return clean label
     */
    private String getCleanLabel(Edge v) {
        String label = getLabel(v);
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
     * gets the node-2-guide-tree-children array
     *
     * @return array
     */
    public NodeArray<List<Node>> getNode2GuideTreeChildren() {
        if (node2GuideTreeChildren == null)
            node2GuideTreeChildren = newNodeArray();

        return node2GuideTreeChildren;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
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
            if (f != e && PhyloTreeUtils.okToDescendDownThisEdge(this, f, v))
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
                final Node result = v;
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
     * returns a new tree in which all adjacentEdges of length < min length have been contracted
     *
     * @param minLength
     * @return true, if anything contracted
     */
    public boolean contractShortEdges(double minLength) {
        return contractEdges(edgeStream().filter(e -> getWeight(e) < minLength).collect(Collectors.toSet()), null);
    }

    /**
     * returns a new tree in which all adjacentEdges of length < min length have been contracted
     *
     * @return true, if anything contracted
     */
    public boolean contractEdges(Set<Edge> edgesToContract, Single<Boolean> selfEdgeEncountered) {
        boolean hasContractedOne = edgesToContract.size() > 0;

        while (edgesToContract.size() > 0) {
            final Edge e = edgesToContract.iterator().next();
            edgesToContract.remove(e);

            final Node v = e.getSource();
            final Node w = e.getTarget();

            for (Edge f : v.adjacentEdges()) { // will remove e from edgesToContract here
                if (f != e) {
                    final Node u = f.getOpposite(v);
                    final boolean needsContracting = edgesToContract.contains(f);
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
            for (Integer taxon : getTaxa(v))
                addTaxon(w, taxon);

            if (getRoot() == v)
                setRoot(w);

            deleteNode(v);
        }

        return hasContractedOne;
    }


    /**
     * gets max distance from this node to any leaf
     * @param v
     * @return depth
     */
    public static int getDepth(Node v) {
        var max = v.outEdgesStream(true).mapToInt(e -> getDepth(e.getTarget()) + 1).max();
        if (max.isPresent())
            return max.getAsInt();
        else
            return 0;
    }


    /**
     * determines whether edge represents a transfer
     *
     * @return true if transfer edge
     */
    public boolean isTransferEdge(Edge e) {
        return isSpecial(e) && getWeight(e) == -1;
    }

    public int computeMaxDepth() {
        return computeMaxDepthRec(getRoot(), 0);
    }

    private int computeMaxDepthRec(Node v, int depth) {
        if (v.isLeaf())
            return depth;
        else {
            return v.childrenStream(false).mapToInt(w -> computeMaxDepthRec(w, depth + 1)).max().orElse(0);
        }
    }
}

// EOF
