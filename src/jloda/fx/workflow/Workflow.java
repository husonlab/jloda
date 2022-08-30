/*
 * Workflow.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.workflow;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import jloda.util.IteratorUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * a work flow that consists of a DAG of algorithm and data nodes
 * Daniel Huson ,10.2021
 */
public class Workflow extends WorkerBase implements Worker<Boolean> {
	private final BooleanProperty valid = new SimpleBooleanProperty(true);
	private final ChangeListener<Boolean> nodeValidChangeListener;

	private final InvalidationListener nodeStateChangeListener;

	private final ObservableList<WorkflowNode> nodes = FXCollections.observableArrayList();
	private int numberOfNodesCreated = 0;

	private final IntegerProperty numberOfNodes = new SimpleIntegerProperty(0);
	private final IntegerProperty numberOfDataNodes = new SimpleIntegerProperty(0);
	private final IntegerProperty numberOfAlgorithmNodes = new SimpleIntegerProperty(0);

	private final IntegerProperty numberOfEdges = new SimpleIntegerProperty(0);
	private final ListChangeListener<WorkflowNode> parentsChangedListener;

	private final BooleanProperty connected = new SimpleBooleanProperty(true);

	public Workflow() {
		this("Workflow");
	}

	public Workflow(String title) {
		setTitle(title);
		parentsChangedListener = c -> {
			var count = 0;
			while (c.next()) {
				if (c.wasAdded())
					count += c.getAddedSize();
				if (c.wasRemoved())
					count -= c.getRemovedSize();
			}
			numberOfEdges.set(numberOfEdges.get() + count);
			if (!isConnected())
				connected.set(determineIsConnected(nodes.get(0)));
			// todo: need to check whether DAG here.
		};

		nodeValidChangeListener = (v, o, n) -> {
			if (n)
				valid.set(nodes.size() > 0 && nodes.stream().allMatch(WorkflowNode::isValid));
			else
				valid.set(true);
		};

		nodeStateChangeListener = (e) -> {
			var currentStates = getAllCurrentStates();

			if (currentStates.contains(State.FAILED)) {
				setState(State.FAILED);
				if (getException() == null) {
					IteratorUtils.asStream(algorithmNodes()).filter(a -> a.getService().getState() == State.FAILED)
							.findFirst().ifPresent(a -> setException(a.getService().getException()));
				}
			} else {
				setException(null);
				if (currentStates.contains(State.CANCELLED)) {
					setState(State.CANCELLED);
				} else if (currentStates.contains(State.RUNNING)) {
					setState(State.RUNNING);
				} else if (currentStates.contains(State.SCHEDULED)) {
					setState(State.SCHEDULED);
				} else if (currentStates.contains(State.READY)) {
					setState(State.READY);
				} else if (currentStates.contains(State.SUCCEEDED)) {
					if (getMessage() == null)
						IteratorUtils.asStream(algorithmNodes()).filter(a -> a.getService().getState() == State.SCHEDULED)
								.findFirst().ifPresent(a -> setMessage(a.getService().getMessage()));
					setState(State.SUCCEEDED);
				}
			}
			if (!currentStates.contains(State.SUCCEEDED))
				setMessage(null);
			// setRunning(currentStates.contains(State.SCHEDULED) || currentStates.contains(State.READY) || currentStates.contains(State.RUNNING));
			setRunning(!currentStates.contains(State.CANCELLED) && (currentStates.contains(State.SCHEDULED) || currentStates.contains(State.RUNNING)));
		};

		stateProperty().addListener((v, o, n) -> {
			if (n == State.SUCCEEDED || n == State.READY)
				setException(null);
		});

		nodes.addListener((ListChangeListener<? super WorkflowNode>) e -> {
			while (e.next()) {
				if (e.wasAdded()) {
					for (var node : e.getAddedSubList()) {
						node.validProperty().addListener(nodeValidChangeListener);
						node.getParents().addListener(parentsChangedListener);
						if (node instanceof AlgorithmNode) {
							numberOfAlgorithmNodes.set(numberOfAlgorithmNodes.get() + 1);
							setTotalWork(getTotalWork() + 1);
							((AlgorithmNode) node).getService().stateProperty().addListener(nodeStateChangeListener);
						} else if (node instanceof DataNode) {
							numberOfDataNodes.set(numberOfDataNodes.get() + 1);
						}
					}
				} else if (e.wasRemoved()) {
					for (var node : e.getRemoved()) {
						node.validProperty().removeListener(nodeValidChangeListener);
						if (node instanceof AlgorithmNode) {
							numberOfAlgorithmNodes.set(numberOfAlgorithmNodes.get() - 1);
							((AlgorithmNode) node).getService().stateProperty().removeListener(nodeStateChangeListener);
							setTotalWork(getTotalWork() - 1);
						} else if (node instanceof DataNode) {
							numberOfDataNodes.set(numberOfDataNodes.get() - 1);
						}
					}
				}
			}
		});

		numberOfNodes.bind(Bindings.size(nodes));
	}

	@Override
	public boolean cancel() {
		var canceled = 0;
		for (var v : IteratorUtils.asStream(algorithmNodes()).filter(a -> a.getService().getState() == State.RUNNING).toList()) {
			if (v.getService().cancel())
				canceled++;
		}
		return canceled > 0;
	}

	public void reset() {
		setException(null);
		IteratorUtils.asStream(algorithmNodes()).forEach(a -> a.getService().reset());
	}

	public void restart(AlgorithmNode algorithmNode) {
		algorithmNode.restart();
	}

	public Set<Worker.State> getAllCurrentStates() {
		return IteratorUtils.asStream(algorithmNodes()).filter(a -> !(a.isValid() && a.getService().getState() == State.READY)).map(a -> a.getService().getState()).collect(Collectors.toSet());
	}

	public ObservableList<WorkflowNode> nodes() {
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

	public void addNode(WorkflowNode node) {
		addNodes(Collections.singleton(node));
	}

	public void addNodes(Collection<WorkflowNode> toAdd) {
		WorkflowNode first = null;
		for (var node : toAdd) {
			if (first == null)
				first = node;
			checkOwner(node.getOwner());
			nodes.add(node);
		}
		if (!isConnected() && first != null)
			connected.set(determineIsConnected(first));
	}

	public void deleteNode(WorkflowNode v) {
		deleteNodes(Collections.singleton(v));
	}

	public void deleteNodes(Collection<WorkflowNode> toDelete) {
		for (var v : toDelete) {
			checkOwner(v.getOwner());

			var parents = new ArrayList<>(v.getParents());
			for (var w : parents) {
				w.getChildren().remove(v);
			}

			var children = new ArrayList<>(v.getChildren());
			for (var w : children) {
				w.getParents().remove(v);
			}
			nodes.remove(v);
		}

		if (isConnected() && nodes.size() > 0)
			connected.set(determineIsConnected(nodes.get(0)));
	}

	public int getNumberOfNodes() {
		return numberOfNodes.get();
	}

	public ReadOnlyIntegerProperty numberOfNodesProperty() {
		return numberOfNodes;
	}

	public int getNumberOfDataNodes() {
		return numberOfDataNodes.get();
	}

	public ReadOnlyIntegerProperty numberOfDataNodesProperty() {
		return numberOfDataNodes;
	}

	public int getNumberOfAlgorithmNodes() {
		return numberOfAlgorithmNodes.get();
	}

	public ReadOnlyIntegerProperty numberOfAlgorithmNodesProperty() {
		return numberOfAlgorithmNodes;
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

	public boolean isValid() {
		return valid.get();
	}

	public ReadOnlyBooleanProperty validProperty() {
		return valid;
	}

	protected void setValid(boolean valid) {
		this.valid.set(valid);
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
				queue.addAll(node.getChildren().stream().filter(n -> !seen.contains(n)).toList());
			}
		}
		return buf.toString();
	}

	public void clear() {
		// cancel all computations:
		for (var node : nodes()) {
			node.setValid(false);
		}
		for (var node : nodes) {
			node.getParents().clear();
			node.getChildren().clear();
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
		if (start == null)
			return true;
		else {
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

	public Collection<WorkflowNode> getAllDescendants(WorkflowNode node, boolean includeGivenNode) {
		var result = new ArrayList<WorkflowNode>();
		var seen = new HashSet<WorkflowNode>();
		if (includeGivenNode)
			result.add(node);
		var queue = new LinkedList<>(node.getChildren());
		while (queue.size() > 0) {
			node = queue.pop();
			if (!seen.contains(node)) {
				seen.add(node);
				result.add(node);
				queue.addAll(node.getChildren());
			}
		}
		return result;
	}

	public Collection<WorkflowNode> getAllAncestors(WorkflowNode node, boolean includeGivenNode) {
		var result = new ArrayList<WorkflowNode>();
		var seen = new HashSet<WorkflowNode>();
		if (includeGivenNode)
			result.add(node);
		var queue = new LinkedList<>(node.getParents());
		while (queue.size() > 0) {
			node = queue.pop();
			if (!seen.contains(node)) {
				seen.add(node);
				result.add(node);
				queue.addAll(node.getParents());
			}
		}
		return result;
	}

	public DataNode newDataNode(DataBlock dataBlock) {
		var node = new DataNode(this);
		nodes.add(node);
		node.setDataBlock(dataBlock);
		return node;
	}

	public AlgorithmNode newAlgorithmNode(Algorithm algorithm) {
		var node = new AlgorithmNode(this);
		nodes.add(node);
		node.setAlgorithm(algorithm);
		return node;
	}

	/**
	 * make a copy that is shallow in the sense that we reference the original datablocks and algorithms, rather than copy them
	 *
	 * @param src source to copy from
	 */
	public void shallowCopy(Workflow src) {
		clear();

		var nodeCopyNodeMap = new HashMap<WorkflowNode, WorkflowNode>();

		for (var node : src.nodes()) {
			var nodeCopy = nodeCopyNodeMap.get(node);
			if (nodeCopy == null) {
				if (node instanceof DataNode dataNode) {
					nodeCopyNodeMap.put(node, newDataNode(dataNode.getDataBlock()));
				} else if (node instanceof AlgorithmNode algorithmNode) {
					nodeCopyNodeMap.put(node, newAlgorithmNode(algorithmNode.getAlgorithm()));
				}
			}
		}

		for (var node : src.nodes()) {
			var nodeCopy = nodeCopyNodeMap.get(node);
			for (var parent : node.getParents()) {
				var parentCopy = nodeCopyNodeMap.get(parent);
				if (!nodeCopy.getParents().contains(parentCopy)) {
					nodeCopy.getParents().add(parentCopy);
				}
			}
		}
	}
}
