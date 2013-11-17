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

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

/**
 * Command handler for the TreeCollapseAllCommand.
 * <p>
 * Will search over the whole tree structure in the associated TreeLayer to identify 
 * collapsible nodes and collapse them one after the other.
 * 
 * @author Dirk Fauth
 *
 * @see TreeLayer
 * @see TreeCollapseAllCommand
 */
public class TreeCollapseAllCommandHandler implements ILayerCommandHandler<TreeCollapseAllCommand> {

	/**
	 * The TreeLayer to which this command handler is connected.
	 */
	private final TreeLayer treeLayer;

	/**
	 * 
	 * @param treeLayer The TreeLayer to which this command handler should be connected.
	 */
	public TreeCollapseAllCommandHandler(TreeLayer treeLayer) {
		this.treeLayer = treeLayer;
	}
	
	@Override
	public boolean doCommand(ILayer targetLayer, TreeCollapseAllCommand command) {
		ITreeRowModel<?> treeModel = this.treeLayer.getModel();
		for (int i = (treeLayer.getRowCount()-1); i >= 0; i--) {
			/*
			 * Checks if the node at the given visible index has children and is collapsible.
			 * If it is it will be collapsed otherwise skipped.
			 * This backwards searching and collapsing mechanism is necessary to ensure to
			 * really get every collapsible node in the whole tree structure.
			 */
			if (treeModel.hasChildren(i) && !treeModel.isCollapsed(i)) {
				treeLayer.collapseTreeRow(i);
			}
		}
		
		return true;
	}
	
	@Override
	public Class<TreeCollapseAllCommand> getCommandClass() {
		return TreeCollapseAllCommand.class;
	}

}
