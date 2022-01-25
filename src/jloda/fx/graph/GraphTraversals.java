/*
 *  GraphTraversals.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.graph;

import jloda.graph.Edge;
import jloda.graph.Node;

import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * basic graph traversals
 * Daniel Huson, 1.2022
 */
public class GraphTraversals {

	/**
	 * traverses all reachable nodes and applies a function to each
	 * @param start starting node
	 * @param edgePredicate cross edge only if this evaluates to true
	 * @param nodeFunction apply this once to each reached node
	 */
	public static void traverseReachable(Node start, Predicate<Edge> edgePredicate, Consumer<Node> nodeFunction) {
		var graph=start.getOwner();
		try(var scheduled=graph.newNodeSet()) {
			var stack=new Stack<Node>();
			stack.add(start);
			scheduled.add(start);
			while(stack.size()>0) {
				var v=stack.pop();
				nodeFunction.accept(v);
				for(var e:v.adjacentEdges()) {
					var w=e.getOpposite(v);
					if(!scheduled.contains(w) && edgePredicate.test(e)) {
						scheduled.add(w);
						stack.push(w);
					}
				}
			}
		}
	}
}
