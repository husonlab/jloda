/**
 * PhyloTree.java 
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

/**
 * @version $Id: PhyloTree.java,v 1.87 2010-05-01 09:37:58 huson Exp $
 *
 * Phylogenetic tree
 *
 * @author Daniel Huson
 */

import jloda.graph.*;
import jloda.util.Basic;
import jloda.util.NotOwnerException;
import jloda.util.Pair;
import jloda.util.ProgramProperties;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PhyloTree extends PhyloGraph {
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

    private String name = null;

    protected final NodeArray<List<Node>> node2GuideTreeChildren; // keep track of children in LSA tree in network

    /**
     * Construct a new empty phylogenetic tree.
     */
    public PhyloTree() {
        super();
        cleanLabelsOnWrite = ProgramProperties.get("cleanTreeLabelsOnWrite", false);
        node2GuideTreeChildren = new NodeArray<>(this);
    }

    /**
     * Clears the tree.
     */
    public void clear() {
        super.clear();
        setRoot((Node) null);
    }

    /**
     * copies a phylogenetic tree
     *
     * @param src original tree
     * @return mapping of old nodes to new nodes
     */
    public NodeArray<Node> copy(PhyloTree src) {
        NodeArray<Node> oldNode2NewNode = super.copy(src);

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
                getNode2GuideTreeChildren().set(oldNode2NewNode.get(v), newChildren);
            }
        }

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
        super.copy(src, oldNode2NewNode, oldEdge2NewEdge);
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
                getNode2GuideTreeChildren().set(oldNode2NewNode.get(v), newChildren);
            }
        }
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
        edgeLabels.set(e, a);
    }

    /**
     * Produces a string representation of the tree in bracket notation.
     *
     * @return a string representation of the tree in bracket notation
     */
    public String toBracketString() {
        StringWriter sw = new StringWriter();
        try {
            write(sw, true);
        } catch (Exception ex) {
            Basic.caught(ex);
            return "();";
        }
        return sw.toString();
    }

    /**
     * Produces a string representation of the tree in bracket notation.
     *
     * @return a string representation of the tree in bracket notation
     */
    public String toBracketString(boolean showWeights) {
        StringWriter sw = new StringWriter();
        try {
            write(sw, showWeights);
        } catch (Exception ex) {
            Basic.caught(ex);
            return "();";
        }
        return sw.toString();
    }

    /**
     * gets the string representation of this tree
     *
     * @return tree
     */
    public String toString() {
        return toBracketString();
    }

    /**
     * Produces a string representation of the tree in bracket notation.
     *
     * @return a string representation of the tree in bracket notation
     */
    public String toString(Map translate) {
        StringWriter sw = new StringWriter();
        try {
            if (translate == null || translate.size() == 0) {
                this.write(sw, true);

            } else {
                PhyloTree tmpTree = new PhyloTree();
                tmpTree.copy(this);
                for (Node v = tmpTree.getFirstNode(); v != null; v = v.getNext()) {
                    String key = tmpTree.getLabel(v);
                    if (key != null) {
                        String value = (String) translate.get(key);
                        if (value != null)
                            tmpTree.setLabel(v, value);
                    }
                }
                tmpTree.write(sw, true);
            }
        } catch (Exception ex) {
            Basic.caught(ex);
            return "()";
        }
        return sw.toString();
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
     * Read a tree in newick notation as unrooted tree
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
        Node v = getFirstNode();
        if (v != null) {
            if (rooted) {
                setRoot(v);
                if (!hasWeights && isUnlabeledDiVertex(v)) {
                    setWeight(v.getFirstAdjacentEdge(), 0.5);
                    setWeight(v.getLastAdjacentEdge(), 0.5);
                }
            } else {
                setRoot((Node) null);
                if (isUnlabeledDiVertex(v))
                    delDivertex(v);
            }
        }
        if (ALLOW_READ_RETICULATE)
            postProcessReticulate();

        // System.err.println("Bootstrap values detected:    " + getInputHasBootstrapValuesOnNodes());
        // System.err.println("Multi-labeled nodes detected: " + getInputHasMultiLabels());
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
            for (i = Basic.skipSpaces(str, i); i < str.length(); i = Basic.skipSpaces(str, i + 1)) {
                Node w = newNode();
                String label = null;
                if (str.charAt(i) == '(') {
                    i = parseBracketNotationRecursively(seen, depth + 1, w, i + 1, str);
                    if (str.charAt(i) != ')')
                        throw new IOException("Expected ')' at position " + i);
                    i = Basic.skipSpaces(str, i + 1);
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
                i = Basic.skipSpaces(str, i);

                // read edge weights

                if (i < str.length() && str.charAt(i) == ':') // edge weight is following
                {
                    i = Basic.skipSpaces(str, i + 1);
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

                // adjust edge weights for reticulate edges
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
                                    setWeight(e, 0);
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
     * deletes artificial divertex
     *
     * @param v Node
     * @return the new edge
     */
    public Edge delDivertex(Node v) {
        if (v.getDegree() != 2)
            throw new RuntimeException("v not divertex, degree is: " + v.getDegree());

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
        if (edgeWeights.get(e) != null && edgeWeights.get(f) != null)
            setWeight(g, getWeight(e) + getWeight(f));
        if (root == v)
            root = null;
        deleteNode(v);
        return g;
    }

    /**
     * post processes a tree that really describes a reticulate network
     *
     * @return number of reticulate nodes
     */
    public int postProcessReticulate() {
        int count = 0;

        // determine all the groups of reticulate ndoes
        Map<String, List<Node>> reticulateNumber2Nodes = new HashMap<>(); // maps each reticulate-node prefix to the set of all nodes that have it

        for (Node v = getFirstNode(); v != null; v = v.getNext()) {
            String label = getLabel(v);
            if (label != null && label.length() > 0) {
                String reticulateLabel = PhyloTreeUtils.findReticulateLabel(label);
                if (reticulateLabel != null) {
                    setLabel(v, PhyloTreeUtils.removeReticulateNodeSuffix(label));
                    List<Node> list = reticulateNumber2Nodes.get(reticulateLabel);
                    if (list == null) {
                        list = new LinkedList<>();
                        reticulateNumber2Nodes.put(reticulateLabel, list);
                    }
                    list.add(v);
                }
            }
        }

        // collapse all instances of a reticulate node into one node
        for (String reticulateNumber : reticulateNumber2Nodes.keySet()) {
            List<Node> list = reticulateNumber2Nodes.get(reticulateNumber);
            if (list.size() > 0) {
                count++;
                Node u = newNode();  // all edges leading to a reticulate node will be redirected to this node
                for (Node v : list) {
                    if (getLabel(v) != null) {
                        if (getLabel(u) == null)
                            setLabel(u, getLabel(v));
                        else if (!getLabel(u).equals(getLabel(v)))
                            setLabel(u, getLabel(u) + "," + getLabel(v));
                    }

                    for (Edge e = v.getFirstAdjacentEdge(); e != null; e = v.getNextAdjacentEdge(e)) {
                        Node w = v.getOpposite(e);
                        Edge f;
                        if (w == e.getSource()) {
                            f = newEdge(w, u);
                            setSpecial(f, true);
                        } else {
                            f = newEdge(u, w);
                        }
                        setSplit(f, getSplit(e));
                        setWeight(f, getWeight(e));
                        setLabel(f, getLabel(e));
                    }
                    deleteNode(v);
                }
                boolean hasReticulateAcceptorEdge = false;
                for (Edge e = u.getFirstInEdge(); e != null; e = u.getNextInEdge(e)) {
                    if (getWeight(e) > 0)
                        if (!hasReticulateAcceptorEdge)
                            hasReticulateAcceptorEdge = true;
                        else {
                            setWeight(e, 0);
                            System.err.println("Warning: node has more than one reticulate-acceptor edge, will only use first");
                        }
                }
                if (hasReticulateAcceptorEdge) {
                    for (Edge e = u.getFirstInEdge(); e != null; e = u.getNextInEdge(e)) {
                        if (getWeight(e) == 0)
                            setWeight(e, -1);
                    }
                }
            }
        }
        return count;
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
    private NodeIntegerArray node2reticulateNumber;  // global number of the reticulate node
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
            node2reticulateNumber = new NodeIntegerArray(this);
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
    private void writeRec(Writer outs, Node v, Edge e, boolean writeEdgeWeights, boolean writeEdgeLabels, Map<Integer, Integer> nodeId2Number, Map<Integer, Integer> edgeId2Number, String nodeLabel)
            throws IOException {
        if (nodeId2Number != null)
            nodeId2Number.put(v.getId(), ++nodeNumber);
        // todo: need to change all code so that trees are always directed away from the root.
        // todo: must do this in splitstree first!

        int outDegree = 0;
        if (e == null)
            outDegree = getDegree(v);
        else if (isSpecial(e)) {
            for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f))
                if (!isSpecial(f))
                    outDegree++;
        } else
            outDegree = getDegree(v) - 1;
        if ((outDegree > 0 || e == null) && (!isHideCollapsedSubTreeOnWrite() || getLabel(v) == null || !getLabel(v).endsWith(PhyloTree.COLLAPSED_NODE_SUFFIX))) {
            outs.write("(");
            boolean first = true;
            for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
                if (f != e) {
                    if (node2reticulateNumber.getInt(v) > 0 && isSpecial(f))
                        continue; // don't climb back up a special edge

                    if (edgeId2Number != null)
                        edgeId2Number.put(f.getId(), ++edgeNumber);

                    if (first)
                        first = false;
                    else
                        outs.write(",");

                    Node w = v.getOpposite(f);
                    boolean inEdgeHasWeight = (getWeight(f) > 0);


                    if (isSpecial(f)) {
                        if (node2reticulateNumber.getInt(w) == 0) {
                            node2reticulateNumber.set(w, ++reticulateNodeNumber);
                            String label;
                            if (getLabel(w) != null)
                                label = getLabelForWriting(w) + PhyloTreeUtils.makeReticulateNodeLabel(inEdgeHasWeight, node2reticulateNumber.getInt(w));
                            else
                                label = PhyloTreeUtils.makeReticulateNodeLabel(inEdgeHasWeight, node2reticulateNumber.getInt(w));

                            writeRec(outs, w, f, writeEdgeWeights, writeEdgeLabels, nodeId2Number, edgeId2Number, label);
                        } else {
                            String label;
                            if (getLabel(w) != null)
                                label = getLabelForWriting(w) + PhyloTreeUtils.makeReticulateNodeLabel(inEdgeHasWeight, node2reticulateNumber.getInt(w));
                            else
                                label = PhyloTreeUtils.makeReticulateNodeLabel(inEdgeHasWeight, node2reticulateNumber.getInt(w));
                            outs.write(label);
                            if (writeEdgeWeights) {
                                if (getWeight(f) >= 0)
                                    outs.write(":" + (float) (getWeight(f)));
                                if (writeEdgeLabels && getLabel(f) != null) {
                                    outs.write("[" + getLabelForWriting(f) + "]");
                                }
                            }
                        }
                    } else
                        writeRec(outs, w, f, writeEdgeWeights, writeEdgeLabels, nodeId2Number, edgeId2Number,
                                getLabelForWriting(w));
                }
            }
            outs.write(")");
        }
        if (nodeLabel != null && nodeLabel.length() > 0)
            outs.write(nodeLabel);
        else if (outDegree == 0)
            outs.write("?");
        if (writeEdgeWeights && e != null) {
            if (getWeight(e) >= 0)
                outs.write(":" + (float) (getWeight(e)));
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
            label = label.replaceAll("[ \\[\\]\\(\\),:;]+", "_");
            if (label.length() > 0)
                return label;
            else
                return "_";
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
        int nodes = nodeNumberEdgeNumber.getFirstInt() + 1;
        nodeNumberEdgeNumber.setFirst(nodes);
        nodeId2Number.put(v.getId(), nodes);
        for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f))
            if (f != e) {
                int edges = nodeNumberEdgeNumber.getSecondInt() + 1;
                nodeNumberEdgeNumber.setSecond(edges);
                edgeId2Number.put(v.getId(), edges);
                if (PhyloTreeUtils.okToDescendDownThisEdge(this, f, v))
                    setId2NumberMapsRec(f.getOpposite(v), f, nodeNumberEdgeNumber, nodeId2Number, edgeId2Number);
            }
    }


    /**
     * sets the number 2 node and number 2 edge maps
     *
     * @param num2node
     * @param num2edge
     */
    public void setNum2NodeEdgeArray(Num2NodeArray num2node, Num2EdgeArray num2edge) {
        num2node.clear(getNumberOfNodes());
        num2edge.clear(getNumberOfEdges());
        setNum2NodeEdgeArrayRec(getRoot(), null, new Pair<>(0, 0), num2node, num2edge);
    }

    /**
     * recursively do the work
     *
     * @param v
     * @param e
     * @param nodeNumberEdgeNumber
     * @param num2node
     * @param num2edge
     */
    private void setNum2NodeEdgeArrayRec(Node v, Edge e, Pair<Integer, Integer> nodeNumberEdgeNumber, Num2NodeArray num2node, Num2EdgeArray num2edge) {
        int nodes = nodeNumberEdgeNumber.getFirstInt() + 1;
        nodeNumberEdgeNumber.setFirst(nodes);
        num2node.put(nodes, v);
        for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f))
            if (f != e) {
                int edges = nodeNumberEdgeNumber.getSecondInt() + 1;
                nodeNumberEdgeNumber.setSecond(edges);
                num2edge.put(edges, f);
                if (PhyloTreeUtils.okToDescendDownThisEdge(this, f, v))
                    setNum2NodeEdgeArrayRec(f.getOpposite(v), f, nodeNumberEdgeNumber, num2node, num2edge);
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
        if (root != null && root.getDegree() == 2 && (getNode2Taxa(root) == null || getNode2Taxa(root).size() == 0)) {
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
            edgeLabels.set(vu, edgeLabels.get(e));
            edgeLabels.set(uw, edgeLabels.get(e));
        }

        deleteEdge(e);
        setRoot(u);
    }

    /**
     * erase the current root. If it has out-degree two and is not node-labeled, then two out edges will be replaced by single ege
     *
     * @param edgeLabels if non-null and root has two out edges, will try to copy one of the edge labels to the new edge
     */
    public void eraseRoot(EdgeArray<String> edgeLabels) {
        final Node oldRoot = getRoot();
        setRoot((Node) null);
        if (oldRoot != null) {
            if (getOutDegree(oldRoot) == 2 && getLabel(oldRoot) == null) {
                if (edgeLabels != null) {
                    String label = null;
                    for (Edge e = oldRoot.getFirstOutEdge(); e != null; e = oldRoot.getNextOutEdge(e)) {
                        if (label == null && edgeLabels.get(e) != null)
                            label = edgeLabels.get(e);
                        edgeLabels.set(e, null);
                    }
                    final Edge e = delDivertex(oldRoot);
                    edgeLabels.set(e, label);
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
     * compute the cycle for this tree and then return it
     *
     * @return cycle for this tree
     */
    public int[] getCycle(Node v) {
        computeCycleRec(v, null, 0);

        return super.getCycle();
    }

    /**
     * recursively compute a cycle
     *
     * @param v
     * @param e
     * @param pos
     */
    private int computeCycleRec(Node v, Edge e, int pos) {
        final List<Integer> taxa = node2taxa.get(v);
        if (taxa != null) {
            for (Integer t : taxa) {
                setTaxon2Cycle(t, ++pos);
            }
        }
        for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
            if (f != e && PhyloTreeUtils.okToDescendDownThisEdge(this, f, v))
                pos = computeCycleRec(f.getOpposite(v), f, pos);
        }
        return pos;
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
     * redirect edges away from root. Assumes that special edges already point away from root
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
        for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
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
            label = label.replaceAll("[ \\[\\]\\(\\),:]+", "_");
            if (label.length() > 0)
                return label;
            else
                return "_";
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * returns the number of nodes with outdegree 0
     *
     * @return number of out-degree 0 nodes
     */
    public int getNumberOfLeaves() {
        int count = 0;
        for (Node v = getFirstNode(); v != null; v = v.getNext())
            if (v.getOutDegree() == 0)
                count++;
        return count;
    }


    /**
     * gets the node-2-guide-tree-children array
     *
     * @return array
     */
    public NodeArray<List<Node>> getNode2GuideTreeChildren() {
        return node2GuideTreeChildren;
    }
}

// EOF
