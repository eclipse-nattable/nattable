/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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

/**
 * Implementation of CellPainterWrapper that is used to render tree structures in NatTable.
 * It puts indentation to tree nodes to visualize the tree structure and adds expand/collapse
 * icons corresponding to the state if a tree node has children.
 */
public class IndentedTreeImagePainter extends CellPainterWrapper {

	/**
	 * The ITreeRowModel that is needed to get information about tree states like depth, children and expand/collapse.
	 */
	private final ITreeRowModel<?> treeRowModel;
	/**
	 * The number of pixels to indent per depth.
	 */
	private final int treeIndent;
	
	/**
	 * Creates an IndentedTreeImagePainter based on the given ITreeRowModel.
	 * Will use 10 pixels for indentation per depth and a default TreeImagePainter for rendering
	 * the icons in the tree.
	 * @param treeRowModel The ITreeRowModel that is needed to get information about tree states like 
	 * 			depth, children and expand/collapse.
	 */
	public IndentedTreeImagePainter(ITreeRowModel<?> treeRowModel) {
		this(treeRowModel, 10, new TreeImagePainter(treeRowModel));
	}
	
	/**
	 * Creates an IndentedTreeImagePainter based on the given ITreeRowModel.
	 * Will use the given number of pixels for indentation per depth and a default TreeImagePainter for 
	 * rendering the icons in the tree.
	 * @param treeRowModel The ITreeRowModel that is needed to get information about tree states like 
	 * 			depth, children and expand/collapse.
	 * @param treeIndent The number of pixels to indent per depth.
	 */
	public IndentedTreeImagePainter(ITreeRowModel<?> treeRowModel, int treeIndent) {
		this(treeRowModel, treeIndent, new TreeImagePainter(treeRowModel));
	}
	
	/**
	 * Creates an IndentedTreeImagePainter based on the given ITreeRowModel, indentation per depth
	 * and ICellPainter for painting the icons in the tree.
	 * @param treeRowModel The ITreeRowModel that is needed to get information about tree states like 
	 * 			depth, children and expand/collapse.
	 * @param treeIndent The number of pixels to indent per depth.
	 * @param treeImagePainter The ICellPainter that should be used to paint the images in the tree. 
	 * 			Usually it is some type	of TreeImagePainter that paints expand/collapse/leaf icons regarding 
	 * 			the node state.
	 */
	public IndentedTreeImagePainter(ITreeRowModel<?> treeRowModel, int treeIndent, ICellPainter treeImagePainter) {
		this.treeRowModel = treeRowModel;
		this.treeIndent = treeIndent;
		
		setWrappedPainter(treeImagePainter);
	}
	
	/**
	 * @return The ICellPainter that is used to paint the images in the tree. Usually it is some type
	 * 			of TreeImagePainter that paints expand/collapse/leaf icons regarding the node state.
	 */
	public ICellPainter getTreeImagePainter() {
		return getWrappedPainter();
	}

	/**
	 * @param cellPainter The ICellPainter that should be used to paint the images in the tree. 
	 * 			Usually it is some type	of TreeImagePainter that paints expand/collapse/leaf icons regarding 
	 * 			the node state.
	 */
	public void setTreeImagePainter(ICellPainter cellPainter) {
		setWrappedPainter(cellPainter);
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
	
	/**
	 * @param depth The depth/level in the tree structure for which the indent is requested.
	 * @return The number of pixels the content should be indented.
	 */
	protected int getIndent(int depth) {
		return this.treeIndent * depth;
	}
	
	/**
	 * @param cell The cell for which the depth/level in the tree structure is requested.
	 * @return The depth/level in the tree structure the given cell is located.
	 */
	private int getDepth(ILayerCell cell) {
        int index = cell.getLayer().getRowIndexByPosition(cell.getRowPosition());
        return this.treeRowModel.depth(index);
    }
	
}
