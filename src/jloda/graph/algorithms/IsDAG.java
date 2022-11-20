/*
 * IsDAG.java Copyright (C) 2022. Daniel H. Huson
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
 *
 */

package jloda.graph.algorithms;

import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeIntArray;
import jloda.graph.NodeSet;
import jloda.util.Counter;

public class IsDAG {
	/**
	 * determines whether given graph is a DAG
	 *
	 * @param graph the graph
	 * @return true, if graph is DAG
	 */
	public static boolean apply(Graph graph) {
		var time = new Counter(0);
		try (var discovered = graph.newNodeSet(); var departure = graph.newNodeIntArray()) {
			for (var v : graph.nodes()) {
				if (!discovered.contains(v)) {
					applyRec(v, discovered, departure, time);
				}
			}

			for (var v : graph.nodes()) {
				for (var u : v.children()) {
					if (departure.get(v) <= departure.get(u))
						return false;
				}
			}
		}
		return true;
	}

	private static void applyRec(Node v, NodeSet discovered, NodeIntArray departure, Counter time) {
		discovered.add(v);

		for (var u : v.children()) {
			if (!discovered.contains(u))
				applyRec(u, discovered, departure, time);
		}
		departure.set(v, (int) time.getAndIncrement());
	}
}
