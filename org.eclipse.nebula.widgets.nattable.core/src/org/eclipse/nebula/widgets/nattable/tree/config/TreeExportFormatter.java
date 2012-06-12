/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.config;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.excel.DefaultExportFormatter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;

public class TreeExportFormatter extends DefaultExportFormatter {

	private final ITreeRowModel<?> treeRowModel;
	
	public TreeExportFormatter(ITreeRowModel<?> treeRowModel) {
		this.treeRowModel = treeRowModel;
	}
	
	@Override
	public Object formatForExport(ILayerCell cell, IConfigRegistry configRegistry) {
		int index = cell.getLayer().getRowIndexByPosition(cell.getRowPosition());
		int depth = treeRowModel.depth(index);
		
		StringBuilder str = new StringBuilder();
		if (depth > 0) {
			for (int i = 0; i < depth; i++) {
				str.append("  "); //$NON-NLS-1$
			}
		}
		
		if (treeRowModel.isLeaf(index)) {
			str.append(super.formatForExport(cell, configRegistry));
		} else {
			str.append(treeRowModel.getObjectAtIndexAndDepth(index, depth));
		}
		
		return str.toString();
	}
	
}
