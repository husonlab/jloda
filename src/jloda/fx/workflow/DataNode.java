/*
 *  DataNode.java Copyright (C) 2021 Daniel H. Huson
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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import jloda.util.StringUtils;

import java.util.stream.Collectors;

/**
 * a workflow node that contains a data block
 * Daniel Huson, 10.2021
 */
public class DataNode extends WorkflowNode {
	private final ObjectProperty<DataBlock> dataBlock = new SimpleObjectProperty<>(null);

	public DataNode(Workflow owner) {
		super(owner);
	}

	@Override
	protected ChangeListener<Boolean> createParentValidListener() {
		return (v, o, n) -> setValid(n && getParents().stream().allMatch(WorkflowNode::isValid));
	}

	public DataBlock getDataBlock() {
		return dataBlock.get();
	}

	public ReadOnlyObjectProperty<DataBlock> dataBlockProperty() {
		return dataBlock;
	}

	public void setDataBlock(DataBlock dataBlock) {
		this.dataBlock.set(dataBlock);
	}

	public void addParent(WorkflowNode v) {
		if (!(v instanceof AlgorithmNode))
			throw new IllegalArgumentException("addParent(): must be AlgorithmNode");
		super.addParent(v);
	}

	public void addChild(WorkflowNode v) {
		if (!(v instanceof AlgorithmNode))
			throw new IllegalArgumentException("addChild(): must be AlgorithmNode");
		super.addChild(v);
	}

	@Override
	public String toReportString(boolean full) {
		if (full) return String.format("%s (%s); parents: %s children: %s",
				toReportString(false), isValid(),
				StringUtils.toString(getParents().stream().map(WorkflowNode::getId).collect(Collectors.toList()), ","),
				StringUtils.toString(getChildren().stream().map(WorkflowNode::getId).collect(Collectors.toList()), ","));
		else
			return String.format("%02d DataBlock '%s'", getId(),
					(getDataBlock() != null ? getDataBlock().getName() : getName()));
	}
}
