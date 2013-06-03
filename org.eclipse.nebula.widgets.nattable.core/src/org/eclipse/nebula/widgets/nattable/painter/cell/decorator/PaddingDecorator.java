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
package org.eclipse.nebula.widgets.nattable.painter.cell.decorator;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;


public class PaddingDecorator extends CellPainterWrapper {
	
	private final int topPadding;
	private final int rightPadding;
	private final int bottomPadding;
	private final int leftPadding;

	public PaddingDecorator(ICellPainter interiorPainter) {
		this(interiorPainter, 2);
	}
	
	public PaddingDecorator(ICellPainter interiorPainter, int padding) {
		this(interiorPainter, padding, padding, padding, padding);
	}
	
	public PaddingDecorator(ICellPainter interiorPainter, int topPadding, int rightPadding, int bottomPadding, int leftPadding) {
		super(interiorPainter);
		this.topPadding = topPadding;
		this.rightPadding = rightPadding;
		this.bottomPadding = bottomPadding;
		this.leftPadding = leftPadding;
	}

	@Override
	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return leftPadding + super.getPreferredWidth(cell, gc, configRegistry) + rightPadding;
	}
	
	@Override
	public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return topPadding + super.getPreferredHeight(cell, gc, configRegistry) + bottomPadding;
	}

	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
		Rectangle interiorBounds = getInteriorBounds(adjustedCellBounds);
		
		Color originalBg = gc.getBackground();
		Color cellStyleBackground = getBackgroundColor(cell, configRegistry);
        gc.setBackground(cellStyleBackground != null ? cellStyleBackground : originalBg);
		gc.fillRectangle(adjustedCellBounds);
		gc.setBackground(originalBg);
		
		if (interiorBounds.width > 0 && interiorBounds.height > 0) {
			super.paintCell(cell, gc, interiorBounds, configRegistry);
		}
	}
	
	public Rectangle getInteriorBounds(Rectangle adjustedCellBounds) {
		return new Rectangle(
				adjustedCellBounds.x + leftPadding,
				adjustedCellBounds.y + topPadding,
				adjustedCellBounds.width - leftPadding - rightPadding,
				adjustedCellBounds.height - topPadding - bottomPadding
		);
	}
	
	protected Color getBackgroundColor(ILayerCell cell, IConfigRegistry configRegistry) {
		return CellStyleUtil.getCellStyle(cell, configRegistry).getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);		
	}
	
	@Override
	public ICellPainter getCellPainterAt(int x, int y, ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
		//need to take the alignment into account
		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		
		HorizontalAlignmentEnum horizontalAlignment = cellStyle.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);
		int horizontalAlignmentPadding = 0;
		switch (horizontalAlignment) {
			case LEFT: horizontalAlignmentPadding = leftPadding;
						break;
			case CENTER: horizontalAlignmentPadding = leftPadding/2;
						break;
		}

		VerticalAlignmentEnum verticalAlignment = cellStyle.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);
		int verticalAlignmentPadding = 0;
		switch (verticalAlignment) {
			case TOP: verticalAlignmentPadding = topPadding;
						break;
			case MIDDLE: verticalAlignmentPadding = topPadding/2;
						break;
		}

		return super.getCellPainterAt(x - horizontalAlignmentPadding, y - verticalAlignmentPadding, cell, gc, adjustedCellBounds,
				configRegistry);
	}
}
