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
package org.eclipse.nebula.widgets.nattable.tree.painter;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class IndentedTreeImagePainter extends CellPainterWrapper {

	private final ITreeRowModel<?> treeRowModel;
	private final int treeIndent;
	
	public IndentedTreeImagePainter(ITreeRowModel<?> treeRowModel) {
		this(treeRowModel, 10, new TreeImagePainter(treeRowModel));
	}
	
	public IndentedTreeImagePainter(ITreeRowModel<?> treeRowModel, int treeIndent, ICellPainter treeImagePainter) {
		this.treeRowModel = treeRowModel;
		this.treeIndent = treeIndent;
		
		setWrappedPainter(treeImagePainter);
	}
	
	public ICellPainter getTreeImagePainter() {
		return getWrappedPainter();
	}

	@Override
	public Rectangle getWrappedPainterBounds(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		int depth = getDepth(cell);
		int indent = getIndent(depth);
		
		return new Rectangle(bounds.x + indent, bounds.y, bounds.width - indent, bounds.height);
	}

	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		super.paintCell(cell, gc, getWrappedPainterBounds(cell, gc, bounds, configRegistry), configRegistry);
	}
	
	@Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        int depth = getDepth(cell);
        int indent = getIndent(depth);
		return indent + super.getPreferredWidth(cell, gc, configRegistry);
    }
	
	protected int getIndent(int depth) {
		return this.treeIndent * depth;
	}
	
	private int getDepth(ILayerCell cell){
        int index = cell.getLayer().getRowIndexByPosition(cell.getRowPosition());
        return this.treeRowModel.depth(index);
    }
	
}
