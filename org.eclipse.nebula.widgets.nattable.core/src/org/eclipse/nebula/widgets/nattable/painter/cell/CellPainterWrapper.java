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

public abstract class CellPainterWrapper extends AbstractCellPainter {

	private ICellPainter wrappedPainter;

	public CellPainterWrapper() {}

	public CellPainterWrapper(ICellPainter painter) {
		this.wrappedPainter = painter;
	}

	public void setWrappedPainter(ICellPainter painter) {
		this.wrappedPainter = painter;
	}

	public ICellPainter getWrappedPainter() {
		return wrappedPainter;
	}

	public Rectangle getWrappedPainterBounds(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		return bounds;
	}

	@Override
	public ICellPainter getCellPainterAt(int x, int y, ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
		Rectangle wrappedPainterBounds = getWrappedPainterBounds(cell, gc, adjustedCellBounds, configRegistry);
		if (wrappedPainter != null && wrappedPainterBounds.contains(x, y)) {
			return getWrappedPainter().getCellPainterAt(x, y, cell, gc, wrappedPainterBounds, configRegistry);
		} else {
			return super.getCellPainterAt(x, y, cell, gc, adjustedCellBounds, configRegistry);
		}
	}

	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return wrappedPainter != null ? wrappedPainter.getPreferredWidth(cell, gc, configRegistry) : 0;
	}

	public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return wrappedPainter != null ? wrappedPainter.getPreferredHeight(cell, gc, configRegistry) : 0;
	}

	public void paintCell(ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
		if (wrappedPainter != null) {
			wrappedPainter.paintCell(cell, gc, adjustedCellBounds, configRegistry);
		}
	}

}
