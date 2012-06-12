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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Implementations are responsible for painting a cell.<br/>
 * 
 * Custom {@link ICellPainter} can be registered in the {@link IConfigRegistry}.
 * This is a mechanism for plugging in custom cell painting.
 * 
 * @see PercentageBarCellPainter
 */
public interface ICellPainter {
	
	/**
	 * 
	 * @param gc SWT graphics context used to draw the cell
	 * @param rectangle cell bounds
	 * @param natTable :-)
	 * @param cellRenderer
	 * @param rowIndex of the cell to paint
	 * @param colIndex of the cell to paint
	 * @param selected is the cell selected ?
	 */
	public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry);

	/**
	 * Get the preferred width of the cell when rendered by this painter. Used for auto-resize.
	 * @param cell
	 * @param gc
	 * @param configRegistry
	 * @return
	 */
	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry);

	/**
	 * Get the preferred height of the cell when rendered by this painter. Used for auto-resize.
	 * @param cell
	 * @param gc
	 * @param configRegistry
	 * @return
	 */
	public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry);
	
	public ICellPainter getCellPainterAt(int x, int y, ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry);
	
}
