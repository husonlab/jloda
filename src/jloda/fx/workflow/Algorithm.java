/*
 * Algorithm.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.util.progress.ProgressListener;

import java.util.Collection;

/**
 * algorithm
 * Daniel Huson, 10.2021
 */
public abstract class Algorithm extends NamedBase {
	public abstract void compute(ProgressListener progress, Collection<DataBlock> inputData, Collection<DataBlock> outputData);

	public boolean isApplicable(Collection<DataNode> inputNodes) {
		return inputNodes.stream().allMatch(WorkflowNode::isValid);
	}

	public void clear() {
	}
}
