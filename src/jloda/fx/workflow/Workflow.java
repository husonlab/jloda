/*
 *  Workflow.java Copyright (C) 2021 Daniel H. Huson
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

package jloda.fx.workflow;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * a work flow that consists of a DAG of algorithm and data nodes
 * Daniel Huson ,10.2021
 */
public class Workflow extends NamedBase {
	private final BooleanProperty busy = new SimpleBooleanProperty(false);
	private final ChangeListener<Boolean> nodeValidListener;

	private final ObservableList<WorkflowNode> nodes = FXCollections.observableArrayList();
	private int numberOfNodesCreated = 0;

	private final IntegerProperty numberOfEdges = new SimpleIntegerProperty(0);
	private final ListChangeListener<WorkflowNode> parentsChangedListener;

	private final BooleanProperty connected = new SimpleBooleanProperty(true);

	public Workflow() {
		parentsChangedListener = change -> {
			var count = 0;
			while (change.next()) {
				count += change.getAddedSize();
				count -= change.getRemovedSize();
			}
			numberOfEdges.set(numberOfEdges.get() + count);
		};

		nodeValidListener = (v, o, n) -> {
			if (n)
				busy.set(nodes.size() > 0 && !nodes.stream().allMatch(WorkflowNode::isValid));
			else
				busy.set(true);
		};

		nodes.addListener((ListChangeListener<? super WorkflowNode>) e -> {
			while (e.next()) {
				if (e.wasAdded()) {
					for (var node : e.getAddedSubList()) {
						node.validProperty().addListener(nodeValidListener);
						node.getParents().addListener(parentsChangedListener);
					}
				} else if (e.wasRemoved()) {
					for (var node : e.getRemoved()) {
						node.validProperty().removeListener(nodeValidListener);
					}
				}
			}
		});
	}

	public Iterable<WorkflowNode> nodes() {
		return nodes;
	}

	public Stream<WorkflowNode> nodeStream() {
		return nodes.stream();
	}

	public Iterable<WorkflowNode> roots() {
		return () -> nodeStream().filter(v -> v.getInDegree() == 0).iterator();
	}

	public Iterable<WorkflowNode> leaves() {
		return () -> nodeStream().filter(v -> v.getOutDegree() == 0).iterator();
	}

	public Iterable<? extends DataNode> dataNodes() {
		return () -> nodeStream().filter(v -> v instanceof DataNode).map(v -> (DataNode) v).iterator();
	}

	public Iterable<? extends AlgorithmNode> algorithmNodes() {
		return () -> nodeStream().filter(v -> v instanceof AlgorithmNode).map(v -> (AlgorithmNode) v).iterator();
	}

	public void addDataNode(DataNode v) {
		nodes.add(v);
		if (!isDAG())
			throw new IllegalArgumentException("AddNode(): not DAG");
		if (!isConnected())
			connected.set(determineIsConnected(v));
	}

	public void addAlgorithmNode(AlgorithmNode v) {
		nodes.add(v);
		if (!isDAG())
			throw new IllegalArgumentException("AddNode(): not DAG");
		if (!isConnected())
			connected.set(determineIsConnected(v));
	}

	public void deleteNode(WorkflowNode v) {
		checkOwner(v.getOwner());

		for (var w : v.getParents()) {
			w.getChildren().remove(v);
		}

		for (var w : v.getParents()) {
			w.getParents().remove(v);
		}

		nodes.remove(v);

		if (isConnected() && nodes.size() > 0)
			connected.set(determineIsConnected(nodes.get(0)));
	}

	public int getNumberOfNodes() {
		return nodes.size();
	}

	public int size() {
		return getNumberOfNodes();
	}

	public int getNumberOfEdges() {
		return numberOfEdges.get();
	}


	public boolean isConnected() {
		return connected.get();
	}

	public ReadOnlyBooleanProperty connectedProperty() {
		return connected;
	}

	public void checkOwner(Workflow owner) {
		assert owner != null : "Owner is null";
		assert owner == this : "Wrong owner";
	}

	public boolean getBusy() {
		return busy.get();
	}

	public ReadOnlyBooleanProperty busyProperty() {
		return busy;
	}

	public String toReportString() {
		var buf = new StringBuilder("Workflow (" + nodes.size() + " nodes):\n");
		var seen = new HashSet<WorkflowNode>();
		var queue = nodeStream().filter(n -> n.getInDegree() == 0).collect(Collectors.toCollection(LinkedList::new));
		while (queue.size() > 0) {
			var node = queue.pop();
			if (!seen.contains(node)) {
				seen.add(node);
				buf.append(node.toReportString(true));
				buf.append("\n");
				queue.addAll(node.getChildren().stream().filter(n -> !seen.contains(n)).collect(Collectors.toList()));
			}
		}
		return buf.toString();
	}

	public void clear() {
		// cancel all computations:
		for (var node : nodes()) {
			node.setValid(false);
		}
		nodes.clear();
	}

	public int getNodeUID() {
		return ++numberOfNodesCreated;
	}

	public boolean isDAG() {
		var discovered = new HashSet<WorkflowNode>();
		var departure = new HashMap<WorkflowNode, Integer>();

		var time = 0;
		for (var v : nodes()) {
			if (!discovered.contains(v))
				time = isDagRec(v, discovered, departure, time);
		}

		for (var u : nodes()) {
			for (var v : u.getChildren()) {
				if (departure.get(u) < departure.get(v))
					return false;
			}
		}
		return true;
	}

	private static int isDagRec(WorkflowNode v, Set<WorkflowNode> discovered, HashMap<WorkflowNode, Integer> departure, int time) {
		discovered.add(v);
		for (var u : v.getChildren()) {
			if (!discovered.contains(u)) {
				time = isDagRec(u, discovered, departure, time);
			}
		}
		departure.put(v, time++);
		return time;
	}

	private boolean determineIsConnected(WorkflowNode start) {
		var discovered = new HashSet<WorkflowNode>();
		var stack = new Stack<WorkflowNode>();
		stack.push(start);
		discovered.add(start);
		while (stack.size() > 0) {
			var v = stack.pop();
			for (var w : v.getParents()) {
				if (!discovered.contains(w)) {
					discovered.add(w);
					stack.push(w);
				}
			}
			for (var w : v.getChildren()) {
				if (!discovered.contains(w)) {
					discovered.add(w);
					stack.push(w);
				}
			}
		}
		return discovered.size() == nodes.size();
	}
}
