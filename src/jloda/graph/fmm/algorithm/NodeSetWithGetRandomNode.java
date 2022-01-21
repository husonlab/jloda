/*
 * NodeSetWithGetRandomNode.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph.fmm.algorithm;

import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.NodeIntArray;

import java.util.Random;

/**
 * implementation of the fast multilayer method (note: without multipole algorithm)
 * Original C++ author: Stefan Hachul, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class NodeSetWithGetRandomNode {
	final private Node[] array;
	private final NodeIntArray positionInArray;
	private final NodeIntArray massOfStar;
	private int lastSelectableIndexOfNode;
	final private Random random = new Random();

	public NodeSetWithGetRandomNode(Graph graph) {
		array = new Node[graph.getNumberOfNodes()];
		positionInArray = graph.newNodeIntArray();
		massOfStar = graph.newNodeIntArray();

		int i = 0;
		for (var v : graph.nodes()) {
			array[i] = v;
			positionInArray.put(v, i);
			i++;
			massOfStar.put(v, 1);
		}
		lastSelectableIndexOfNode = array.length - 1;
	}

	public NodeSetWithGetRandomNode(Graph graph, NodeArray<NodeAttributes> nodeAttributes) {
		array = new Node[graph.getNumberOfNodes()];
		positionInArray = graph.newNodeIntArray();
		massOfStar = graph.newNodeIntArray();

		int i = 0;
		for (var v : graph.nodes()) {
			array[i] = v;
			positionInArray.put(v, i);
			i++;
			massOfStar.put(v, nodeAttributes.get(v).getMass());
		}
		lastSelectableIndexOfNode = array.length - 1;

	}

	public boolean isEmpty() {
		return lastSelectableIndexOfNode < 0;
	}

	public boolean isDeleted(Node v) {
		return positionInArray.get(v) > lastSelectableIndexOfNode;
	}

	public void delete(Node v) {
		if (!isDeleted(v)) {
			var pos = positionInArray.get(v);
			var w = array[lastSelectableIndexOfNode];
			array[pos] = w;
			positionInArray.put(w, pos);
			array[lastSelectableIndexOfNode] = v;
			positionInArray.put(v, lastSelectableIndexOfNode);
			lastSelectableIndexOfNode--;
		}
	}

	public Node getRandomNode() {
		var v = array[random.nextInt(lastSelectableIndexOfNode + 1)];
		delete(v);
		return v;
	}

	public Node getRandomNodeWithLowestStarMass(int numberRandomTries) {
		int minMass = Integer.MAX_VALUE;
		Node randomNode = null;
		int last_trie_index = lastSelectableIndexOfNode;
		int i = 1;
		while (i <= numberRandomTries && last_trie_index >= 0) {
			var lastTrieNode = array[last_trie_index];
			var newRandomIndex = random.nextInt(last_trie_index + 1);
			var newRandomNode = array[newRandomIndex];
			array[last_trie_index] = newRandomNode;
			array[newRandomIndex] = lastTrieNode;
			positionInArray.put(newRandomNode, last_trie_index);
			positionInArray.put(lastTrieNode, newRandomIndex);

			if (massOfStar.get(array[last_trie_index]) < minMass) {
				randomNode = array[last_trie_index];
				minMass = massOfStar.get(randomNode);
			}
			i++;
			last_trie_index -= 1;
		}
		delete(randomNode);
		return randomNode;
	}

	public Node getRandomNodeWithHighestStarMass(int numberRandomTries) {
		int maxMass = 0;
		Node randomNode = null;
		int last_trie_index = lastSelectableIndexOfNode;
		int i = 1;
		while (i <= numberRandomTries && last_trie_index >= 0) {
			var lastTrieNode = array[last_trie_index];
			var newRandomIndex = random.nextInt(last_trie_index + 1);
			var newRandomNode = array[newRandomIndex];
			array[last_trie_index] = newRandomNode;
			array[newRandomIndex] = lastTrieNode;
			positionInArray.put(newRandomNode, last_trie_index);
			positionInArray.put(lastTrieNode, newRandomIndex);

			if (massOfStar.get(array[last_trie_index]) > maxMass) {
				randomNode = array[last_trie_index];
				maxMass = massOfStar.get(randomNode);
			}
			i++;
			last_trie_index -= 1;
		}

		delete(randomNode);
		return randomNode;
	}

	public void setSeed(int seed) {
		random.setSeed(seed);
	}
}
