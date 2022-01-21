/*
 * GML.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph.io;

import jloda.graph.EdgeSet;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.util.parse.NexusStreamParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * i/o in GML
 * daniel huson, 3.2021
 */
public class GML {

    /**
     * write a graph in GML
     *
     * @param w
     * @param comment
     * @param directed
     * @param label
     * @param graphId
     * @throws IOException
     */
    public static void writeGML(Graph graph, Writer w, String comment, boolean directed, String label, int graphId, Map<String, NodeSet> label2nodes, Map<String, EdgeSet> label2edges) throws IOException {
        w.write("graph [\n");
        if (comment != null)
            w.write("\tcomment \"" + comment + "\"\n");
        w.write("\tdirected " + (directed ? 1 : 0) + "\n");
        w.write("\tid " + graphId + "\n");
        if (label != null)
            w.write("\tlabel \"" + label + "\"\n");
        boolean hasNodeLabels = (label2nodes != null && label2nodes.containsKey("label"));
        for (var v : graph.nodes()) {
            w.write("\tnode [\n");
            w.write("\t\tid " + v.getId() + "\n");
            if (label2nodes != null) {
                for (String aLabel : label2nodes.keySet()) {
                    var set = label2nodes.get(aLabel);
                    if (set != null && set.contains(v)) {
                        w.write("\t\t" + aLabel + "\"\n");
                        break;
                    }
                }
            }
            if (!hasNodeLabels)
                w.write("\t\tlabel \"" + (v.getInfo() != null ? v.getInfo().toString() : "null") + "\"\n");

            w.write("\t]\n");
        }
        boolean hasEdgeLabels = (label2edges != null && label2edges.containsKey("label"));

        for (var e : graph.edges()) {
            w.write("\tedge [\n");
            w.write("\t\tsource " + e.getSource().getId() + "\n");
            w.write("\t\ttarget " + e.getTarget().getId() + "\n");

            if (label2edges != null) {
                for (String aLabel : label2edges.keySet()) {
                    var set = label2edges.get(aLabel);
                    if (set != null && set.contains(e)) {
                        w.write("\t\t" + aLabel + "\"\n");
                    }
                }
            }
            if (!hasEdgeLabels)
                w.write("\t\tlabel \"" + (e.getInfo() != null ? e.getInfo().toString() : "null") + "\"\n");

            w.write("\t]\n");
        }
        w.write("]\n");
        w.flush();
    }

    public static void readGML(Reader r, Graph graph) throws IOException {
        readGML(r, graph, new GMLInfo());
    }

    /**
     * read a graph in GML for that was previously saved using writeGML. This is not a general parser.
     *
     * @param r
     */
    public static void readGML(Reader r, Graph graph, GMLInfo info) throws IOException {
        final NexusStreamParser np = new NexusStreamParser(r);
        np.setSquareBracketsSurroundComments(false);

        graph.clear();

        np.matchIgnoreCase("graph [");
        if (np.peekMatchIgnoreCase("comment")) {
            np.matchIgnoreCase("comment");
            info.setComment(NexusStreamParser.getQuotedString(np));
        }
        np.matchIgnoreCase("directed");
        info.setDirected(np.getInt(0, 1) == 1);

        np.matchIgnoreCase("id");
        info.setId(np.getInt());

        if (np.peekMatchIgnoreCase("label")) {
            np.matchIgnoreCase("label");
            info.setLabel(NexusStreamParser.getQuotedString(np));
        }

        Map<Integer, Node> id2node = new HashMap<>();
        while (np.peekMatchIgnoreCase("node")) {
            np.matchIgnoreCase("node [");
            np.matchIgnoreCase("id");
            int id = np.getInt();
            var v = graph.newNode(null);
            id2node.put(id, v);
            if (np.peekMatchIgnoreCase("label")) {
                np.matchIgnoreCase("label");
                v.setInfo(NexusStreamParser.getQuotedString(np));
            }
            np.matchIgnoreCase("]");
        }
        while (np.peekMatchIgnoreCase("edge")) {
            np.matchIgnoreCase("edge [");
            np.matchIgnoreCase("source");
            int sourceId = np.getInt();
            np.matchIgnoreCase("target");
            int targetId = np.getInt();
            if (!id2node.containsKey(sourceId))
                throw new IOException("Undefined node id: " + sourceId);
            if (!id2node.containsKey(targetId))
                throw new IOException("Undefined node id: " + targetId);
            var e = graph.newEdge(id2node.get(sourceId), id2node.get(targetId), null);
            if (np.peekMatchIgnoreCase("label")) {
                np.matchIgnoreCase("label");
                e.setInfo(NexusStreamParser.getQuotedString(np));
            }
            np.matchIgnoreCase("]");
        }
        np.matchIgnoreCase("]");
    }

    public static class GMLInfo {
        private String comment;
        private boolean directed;
        private int id;
        private String label;

        public void clear() {
            comment = null;
            directed = false;
            id = 0;
            label = null;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public boolean isDirected() {
            return directed;
        }

        public void setDirected(boolean directed) {
            this.directed = directed;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
