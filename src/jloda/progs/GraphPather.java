/*
 * GraphPather.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.progs;

import jloda.graph.*;
import jloda.swing.util.CommandLineOptions;
import jloda.util.UsageException;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * greedily finds paths in a distance graph
 * Daniel Huson, 5.2011
 */
public class GraphPather {
    static public void main(String[] args) throws UsageException, IOException {
        CommandLineOptions options = new CommandLineOptions(args);
        options.setDescription("GraphPather - finds paths in the graph of a distance matrix");

        String infileName = options.getMandatoryOption("-i", "Input file", "");
        String outFileName = options.getOption("-o", "Output file (or stdout)", "");
        double threshold = options.getOption("-t", "max distance threshold", Float.MAX_VALUE);
        options.done();

        BufferedReader r = new BufferedReader(new FileReader(infileName));
        int n = 0;
        int lineNumber = 0;


        BufferedWriter out;
        if (outFileName.length() > 0)
            out = new BufferedWriter(new FileWriter(outFileName));
        else
            out = new BufferedWriter(new OutputStreamWriter(System.out));


        final Graph graph = new Graph();
        Node[] id2node = null;

        int a = 0;

        System.err.println("Parsing distances:");
        String aLine;
        while ((aLine = r.readLine()) != null) {
            lineNumber++;
            aLine = aLine.trim();
            if (aLine.length() > 0 && !aLine.startsWith("#")) {
                String[] tokens = aLine.split(" ");

                if (n == 0) {
                    n = tokens.length;
                    id2node = new Node[n];
                    for (int b = 0; b < n; b++) {
                        id2node[b] = graph.newNode();
                        id2node[b].setInfo(b);
                    }
                } else if (tokens.length != n) {
                    throw new IOException("Line " + lineNumber + ": wrong number of tokens: " + tokens.length);
                }
                for (int b = 0; b < n; b++) {
                    if (a != b) {
                        float value = Float.parseFloat(tokens[b]);
                        if (value <= threshold) {
                            graph.newEdge(id2node[a], id2node[b], value);
                        }
                    }
                }
                a++;
            }
            if (a > n)
                throw new IOException("Line " + lineNumber + ": too many lines");

        }
        if (a < n)
            throw new IOException("Line " + lineNumber + ": too few lines");

        System.err.println("done (" + n + " x " + n + ")");

        System.err.println("Sorting edges:");

        Edge[] edges = new Edge[graph.getNumberOfEdges()];

        int count = 0;
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            edges[count++] = e;
        }

        Arrays.sort(edges,(edge1, edge2) -> {
            if ((Integer)edge1.getInfo() <(Integer)edge2.getInfo())
                return -1;
            else if ((Integer)edge1.getInfo() > (Integer)edge2.getInfo())
                return 1;
            else return Integer.compare(edge1.getId(), edge2.getId());
        });
        System.err.println("done (" + edges.length + ")");

        System.err.println("Selecting edges:");

        NodeArray<Node> other = new NodeArray<>(graph);
        NodeArray<Integer> degree = new NodeArray<>(graph, 0);

        EdgeSet selected = new EdgeSet(graph);

        for (Edge e : edges) {
            Node v = e.getSource();
            Node w = e.getTarget();

            if (degree.get(v) == 0 && degree.get(w) == 0) {
                selected.add(e);
                degree.put(v, 1);
                degree.put(w, 1);
                other.put(v, w);
                other.put(w, v);
            } else if (degree.get(v) == 0 && degree.get(w) == 1) {
                selected.add(e);
                degree.put(v, 1);
                degree.put(w, 2);
                Node u = other.get(w);
                other.put(u, v);
                other.put(v, u);
            } else if (degree.get(v) == 1 && degree.get(w) == 0) {
                selected.add(e);
                degree.put(v, 2);
                degree.put(w, 1);
                Node u = other.get(v);
                other.put(u, w);
                other.put(w, u);
            } else if (degree.get(v) == 1 && degree.get(w) == 1 && other.get(v) != w) {
                selected.add(e);
                degree.put(v, 2);
                degree.put(w, 2);
                Node uv = other.get(v);
                Node uw = other.get(w);
                other.put(uv, uw);
                other.put(uw, uv);
            }
        }
        List<Edge> toDelete = new LinkedList<>();
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            if (!selected.contains(e))
                toDelete.add(e);
        }
        for (Edge e : toDelete) {
            graph.deleteEdge(e);
        }

        System.err.println("done (" + selected.size() + ")");


        System.err.println("Building paths:");

        NodeSet used = new NodeSet(graph);

        int countPaths = 0;

        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            if (v.getDegree() == 1 && !used.contains(v)) {
                countPaths++;
                Edge e = null;
                do {
                    out.write(" " + ((Integer) v.getInfo() + 1));
                    Edge f = v.getFirstAdjacentEdge();
                    if (f == e) {
                        f = v.getLastAdjacentEdge();
                    }
                    if (f != e) {
                        v = f.getOpposite(v);
                        e = f;
                    } else
                        e = null;
                    used.add(v);
                }
                while (e != null);
                out.write("\n");
            }
        }
        out.close();
        System.err.println("done (" + countPaths + ")");

    }
}
