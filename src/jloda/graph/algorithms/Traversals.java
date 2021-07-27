/*
 * TreeTraversal.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.graph.Graph;
import jloda.graph.Node;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * tree and graph traversals
 * Daniel Huson, 6.2021
 */
public class Traversals {

    /**
     * performs a pre-order tree traversal, applying the method to all nodes
     *
     * @param root   root node
     * @param method method to be applied
     */
    public static void preOrderTreeTraversal(Node root, Consumer<Node> method) {
        method.accept(root);
        for (var child : root.children()) {
            preOrderTreeTraversal(child, method);
        }
    }

    /**
     * performs a pre-order tree traversal, applying the method to all nodes
     *
     * @param root   root node
     * @param ok     only visit nodes for which this evaluates to true
     * @param method method to be applied
     */
    public static void preOrderTreeTraversal(Node root, Function<Node, Boolean> ok, Consumer<Node> method) {
        if (ok.apply(root)) {
            method.accept(root);
            for (var child : root.children()) {
                preOrderTreeTraversal(child, ok, method);
            }
        }
    }

    /**
     * performs a post-order tree traversal, applying the method to all nodes
     *
     * @param root   root node
     * @param method method to be applied
     */
    public static void postOrderTreeTraversal(Node root, Consumer<Node> method) {
        for (var child : root.children()) {
            postOrderTreeTraversal(child, method);
        }
        method.accept(root);
    }

    /**
     * performs a post-order tree traversal, applying the method to all nodes
     *
     * @param root   root node
     * @param ok     only visit nodes for which this evaluates to true
     * @param method method to be applied
     */
    public static void postOrderTreeTraversal(Node root, Function<Node, Boolean> ok, Consumer<Node> method) {
        if (ok.apply(root)) {
            for (var child : root.children()) {
                postOrderTreeTraversal(child, ok, method);
            }
            method.accept(root);
        }
    }

    /**
     * performs a pre-order graph traversal, applying the method to all nodes
     *
     * @param graph  the graph
     * @param method method to be applied
     */
    public static void preOrderGraphTraversal(Graph graph, Consumer<Node> method) {
        var visited = graph.newNodeSet();
        Function<Node, Boolean> check = v -> {
            if (visited.contains(v))
                return false;
            else {
                visited.add(v);
                return true;
            }
        };
        for (var v : graph.nodes()) {
            if (check.apply(v)) {
                preOrderGraphTraversalRec(v, check, method);
            }
        }
    }

    /**
     * performs a pre-order graph traversal, applying the method to all nodes
     *
     * @param graph  the graph
     * @param ok     only visit nodes for which this evaluates to true
     * @param method method to be applied
     */
    public static void preOrderGraphTraversal(Graph graph, Function<Node, Boolean> ok, Consumer<Node> method) {
        var visited = graph.newNodeSet();
        Function<Node, Boolean> check = v -> {
            if (!ok.apply(v) || visited.contains(v))
                return false;
            else {
                visited.add(v);
                return true;
            }
        };
        for (var v : graph.nodes()) {
            if (check.apply(v)) {
                preOrderGraphTraversalRec(v, check, method);
            }
        }
    }

    private static void preOrderGraphTraversalRec(Node v, Function<Node, Boolean> check, Consumer<Node> method) {
        method.accept(v);
        for (var w : v.adjacentNodes()) {
            if (check.apply(w)) {
                preOrderGraphTraversalRec(w, check, method);
            }
        }
    }

    /**
     * performs a pre-order traversal of a connected component, applying the method to all nodes
     *
     * @param v      a node in the component
     * @param ok     only visit nodes for which this evaluates to true
     * @param method method to be applied
     */
    public static void preOrderComponentTraversal(Node v, Function<Node, Boolean> ok, Consumer<Node> method) {
        var visited = v.getOwner().newNodeSet();
        Function<Node, Boolean> check = w -> {
            if (!ok.apply(w) || visited.contains(w))
                return false;
            else {
                visited.add(w);
                return true;
            }
        };
        if (check.apply(v)) {
            preOrderGraphTraversalRec(v, check, method);
        }
    }

    /**
     * performs a post-order graph traversal, applying the method to all nodes
     *
     * @param graph  the graph
     * @param method method to be applied
     */
    public static void postOrderGraphTraversal(Graph graph, Consumer<Node> method) {
        var visited = graph.newNodeSet();
        Function<Node, Boolean> check = v -> {
            if (visited.contains(v))
                return false;
            else {
                visited.add(v);
                return true;
            }
        };
        for (var v : graph.nodes()) {
            if (check.apply(v)) {
                postOrderGraphTraversalRec(v, check, method);
            }
        }
    }

    /**
     * performs a post-order traversal of a connected component, applying the method to all nodes
     *
     * @param v      a node in the component
     * @param ok     only visit nodes for which this evaluates to true
     * @param method method to be applied
     */
    public static void postOrderComponentTraversal(Node v, Function<Node, Boolean> ok, Consumer<Node> method) {
        var visited = v.getOwner().newNodeSet();
        Function<Node, Boolean> check = w -> {
            if (!ok.apply(w) || visited.contains(w))
                return false;
            else {
                visited.add(w);
                return true;
            }
        };
        if (check.apply(v)) {
            postOrderGraphTraversalRec(v, check, method);
        }
    }

    /**
     * performs a post-order graph traversal, applying the method to all nodes
     *
     * @param graph  the graph
     * @param ok     only visit nodes for which this evaluates to true
     * @param method method to be applied
     */
    public static void postOrderGraphTraversal(Graph graph, Function<Node, Boolean> ok, Consumer<Node> method) {
        var visited = graph.newNodeSet();
        Function<Node, Boolean> check = v -> {
            if (!ok.apply(v) || visited.contains(v))
                return false;
            else {
                visited.add(v);
                return true;
            }
        };
        for (var v : graph.nodes()) {
            if (check.apply(v)) {
                postOrderGraphTraversalRec(v, check, method);
            }
        }
    }

    private static void postOrderGraphTraversalRec(Node v, Function<Node, Boolean> check, Consumer<Node> method) {
        for (var w : v.adjacentNodes()) {
            if (check.apply(w)) {
                postOrderGraphTraversalRec(w, check, method);
            }
        }
        method.accept(v);
    }
}
