/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.tree.command;

import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

/**
 * Command handler for the TreeExpandAllCommand.
 * <p>
 * Will search over the whole tree structure in the associated TreeLayer to identify 
 * expandable nodes and expand them one after the other.
 * 
 * @author Dirk Fauth
 * 
 * @see TreeLayer
 * @see TreeExpandAllCommand
 *
 */
public class TreeExpandAllCommandHandler implements ILayerCommandHandler<TreeExpandAllCommand> {

	/**
	 * The TreeLayer to which this command handler is connected.
	 */
	private final TreeLayer treeLayer;

	/**
	 * 
	 * @param treeLayer The TreeLayer to which this command handler should be connected.
	 */
	public TreeExpandAllCommandHandler(TreeLayer treeLayer) {
		this.treeLayer = treeLayer;
	}
	
	@Override
	public boolean doCommand(ILayer targetLayer, TreeExpandAllCommand command) {
		ITreeRowModel<?> treeModel = this.treeLayer.getModel();
		
		List<Integer> rootIndexes = treeModel.getRootIndexes();
		Collections.reverse(rootIndexes);
		
		for (int root : rootIndexes) {
			expandChild(root, treeModel);
		}
		
		return true;
	}

	/**
	 * Expands the child at the given visible index and tries to expand the child nodes 
	 * immediately after that.
	 * @param index The visible index of the node to expand.
	 * @param treeModel The ITreeRowModel which is necessary to check the collapsed
	 * 			state and children.
	 */
	private void expandChild(int index, ITreeRowModel<?> treeModel) {
		if (treeModel.hasChildren(index)) {
			if (treeModel.isCollapsed(index)) {
				treeLayer.expandTreeRow(index);
			}
			
			expandChilds(treeModel.getChildIndexes(index), treeModel);
		}
	}
	
	/**
	 * Will iterate over the given indexes and try to expand if the node at the given index
	 * has children and can be expanded. This will be done in backwards order so the found
	 * indexes in parent nodes don't need to be adjusted.
	 * @param childIndexes The indexes of the child nodes to expand.
	 * @param treeModel The ITreeRowModel which is necessary to check the collapsed
	 * 			state and children.
	 */
	private void expandChilds(List<Integer> childIndexes, ITreeRowModel<?> treeModel) {
		Collections.reverse(childIndexes);
		for (int index : childIndexes) {
			expandChild(index, treeModel);
		}
	}

	@Override
	public Class<TreeExpandAllCommand> getCommandClass() {
		return TreeExpandAllCommand.class;
	}

}
