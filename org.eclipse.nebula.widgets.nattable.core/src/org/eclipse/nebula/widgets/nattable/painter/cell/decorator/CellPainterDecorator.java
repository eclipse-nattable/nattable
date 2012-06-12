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
package org.eclipse.nebula.widgets.nattable.painter.cell.decorator;


import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Decorates a cell painter with another cell painter.
 */
public class CellPainterDecorator implements ICellPainter {

	private final ICellPainter baseCellPainter;
	private final CellEdgeEnum cellEdge;
	private final int spacing;
	private final ICellPainter decoratorCellPainter;
    private boolean interiorPainterToSpanFullWidth;
    private boolean paintDecorationDependent;

	public CellPainterDecorator(ICellPainter baseCellPainter, CellEdgeEnum cellEdge, ICellPainter decoratorCellPainter) {
		this(baseCellPainter, cellEdge, 2, decoratorCellPainter);
	}

	public CellPainterDecorator(ICellPainter baseCellPainter, CellEdgeEnum cellEdge, ICellPainter decoratorCellPainter, boolean paintDecorationDependent) {
		this(baseCellPainter, cellEdge, 2, decoratorCellPainter, paintDecorationDependent);
	}
	
	public CellPainterDecorator(ICellPainter baseCellPainter, CellEdgeEnum cellEdge, int spacing, ICellPainter decoratorCellPainter) {
		this(baseCellPainter, cellEdge, spacing, decoratorCellPainter, true);
	}
	
	public CellPainterDecorator(ICellPainter baseCellPainter, CellEdgeEnum cellEdge, int spacing, ICellPainter decoratorCellPainter, boolean paintDecorationDependent) {
		this.baseCellPainter = baseCellPainter;
		this.cellEdge = cellEdge;
		this.spacing = spacing;
		this.decoratorCellPainter = decoratorCellPainter;
		this.paintDecorationDependent = paintDecorationDependent;
	}

    public void setBaseCellPainterSpansWholeCell(boolean interiorPainterToSpanFullWidth) {
        this.interiorPainterToSpanFullWidth = interiorPainterToSpanFullWidth;
    }
	
	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		switch (cellEdge) {
		case TOP_LEFT:
		case TOP_RIGHT:
		case BOTTOM_LEFT:
		case BOTTOM_RIGHT:
		case BOTTOM:
		case TOP:
		    return spacing
				+ Math.max(
						baseCellPainter.getPreferredWidth(cell, gc, configRegistry),
						decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry)
				);
        default:
            break;
		}

		return baseCellPainter.getPreferredWidth(cell, gc, configRegistry)
				+ spacing
				+ decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry);
	}

	public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		switch (cellEdge) {
		case TOP_LEFT:
		case TOP_RIGHT:
		case BOTTOM_LEFT:
		case BOTTOM_RIGHT:
		case LEFT:
		case RIGHT:
			return spacing
				+ Math.max(
						baseCellPainter.getPreferredHeight(cell, gc, configRegistry),
						decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry)
				);
		default:
		    break;
		}

		return baseCellPainter.getPreferredHeight(cell, gc, configRegistry)
				+ spacing
				+ decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry);
	}

	public void paintCell(ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
		Rectangle baseCellPainterBounds = this.paintDecorationDependent ? 
				getBaseCellPainterBounds(cell, gc, adjustedCellBounds, configRegistry) : adjustedCellBounds;
		Rectangle decoratorCellPainterBounds = getDecoratorCellPainterBounds(cell, gc, adjustedCellBounds, configRegistry);
		
		Color originalBg = gc.getBackground();
		gc.setBackground(CellStyleUtil.getCellStyle(cell, configRegistry).getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		
		gc.fillRectangle(adjustedCellBounds);
		
		gc.setBackground(originalBg);
		
		baseCellPainter.paintCell(cell, gc, baseCellPainterBounds, configRegistry);
		decoratorCellPainter.paintCell(cell, gc, decoratorCellPainterBounds, configRegistry);
	}

	/**
	 * 
	 * @return The Rectangle which can be used by the base cell painter. 
	 */
	public Rectangle getBaseCellPainterBounds(ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
		int preferredDecoratorWidth = decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry);
		int preferredDecoratorHeight = decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry);
		
		// grab any extra space:
		int grabbedPreferredWidth = adjustedCellBounds.width - (interiorPainterToSpanFullWidth ? 0 : preferredDecoratorWidth) - spacing;
		int grabbedPreferredHeight = adjustedCellBounds.height - (interiorPainterToSpanFullWidth ? 0 : preferredDecoratorHeight) - spacing;
		
		switch (cellEdge) {
		case LEFT:
			return new Rectangle(
					adjustedCellBounds.x + preferredDecoratorWidth + spacing,
					adjustedCellBounds.y,
					grabbedPreferredWidth,
					adjustedCellBounds.height
					).intersection(adjustedCellBounds);
		case RIGHT:
			return new Rectangle(
					adjustedCellBounds.x,
					adjustedCellBounds.y,
					grabbedPreferredWidth,
					adjustedCellBounds.height
					).intersection(adjustedCellBounds);
		case TOP:
			return new Rectangle(
					adjustedCellBounds.x,
					adjustedCellBounds.y + preferredDecoratorHeight + spacing,
					adjustedCellBounds.width,
					grabbedPreferredHeight
					).intersection(adjustedCellBounds);
		case BOTTOM:
			return new Rectangle(
					adjustedCellBounds.x,
					adjustedCellBounds.y,
					adjustedCellBounds.width,
					grabbedPreferredHeight
					).intersection(adjustedCellBounds);
		case TOP_LEFT:
			return new Rectangle(
					adjustedCellBounds.x + preferredDecoratorWidth + spacing,
					adjustedCellBounds.y + preferredDecoratorHeight + spacing,
					grabbedPreferredWidth,
					grabbedPreferredHeight
					).intersection(adjustedCellBounds);
		case TOP_RIGHT:
			return new Rectangle(
					adjustedCellBounds.x,
					adjustedCellBounds.y + preferredDecoratorHeight + spacing,
					grabbedPreferredWidth,
					grabbedPreferredHeight
					).intersection(adjustedCellBounds);
		case BOTTOM_LEFT:
			return new Rectangle(
					adjustedCellBounds.x + preferredDecoratorWidth + spacing,
					adjustedCellBounds.y,
					grabbedPreferredWidth,
					grabbedPreferredHeight
					).intersection(adjustedCellBounds);
		case BOTTOM_RIGHT:
			return new Rectangle(
					adjustedCellBounds.x,
					adjustedCellBounds.y,
					grabbedPreferredWidth,
					grabbedPreferredHeight
					).intersection(adjustedCellBounds);
		case NONE:
		    break;
		}
		
		return null;
	}

	/**
	 * @return The Rectangle to paint the decoration.
	 */
	public Rectangle getDecoratorCellPainterBounds(ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
		int preferredDecoratorWidth = decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry);
		int preferredDecoratorHeight = decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry);
		
		switch (cellEdge) {
		case LEFT:
			return new Rectangle(
					adjustedCellBounds.x,
					adjustedCellBounds.y + ((adjustedCellBounds.height - preferredDecoratorHeight) / 2 ),
					preferredDecoratorWidth,
					preferredDecoratorHeight);
		case RIGHT:
			return new Rectangle(
					adjustedCellBounds.x + adjustedCellBounds.width - preferredDecoratorWidth,
					adjustedCellBounds.y + ((adjustedCellBounds.height - preferredDecoratorHeight) / 2 ),
					preferredDecoratorWidth,
					preferredDecoratorHeight);
		case TOP:
			return new Rectangle(
					adjustedCellBounds.x + ((adjustedCellBounds.width - preferredDecoratorWidth) / 2),
					adjustedCellBounds.y,
					preferredDecoratorWidth,
					preferredDecoratorHeight);
		case BOTTOM:
			return new Rectangle(
					adjustedCellBounds.x + ((adjustedCellBounds.width - preferredDecoratorWidth) / 2),
					adjustedCellBounds.y + adjustedCellBounds.height - preferredDecoratorHeight,
					preferredDecoratorWidth,
					preferredDecoratorHeight);
		case TOP_LEFT:
			return new Rectangle(
					adjustedCellBounds.x,
					adjustedCellBounds.y,
					preferredDecoratorWidth,
					preferredDecoratorHeight);
		case TOP_RIGHT:
			return new Rectangle(
					adjustedCellBounds.x + adjustedCellBounds.width - preferredDecoratorWidth,
					adjustedCellBounds.y,
					preferredDecoratorWidth,
					preferredDecoratorHeight);
		case BOTTOM_LEFT:
			return new Rectangle(
					adjustedCellBounds.x,
					adjustedCellBounds.y + adjustedCellBounds.height - preferredDecoratorHeight,
					preferredDecoratorWidth,
					preferredDecoratorHeight);
		case BOTTOM_RIGHT:
			return new Rectangle(
					adjustedCellBounds.x + adjustedCellBounds.width - preferredDecoratorWidth,
					adjustedCellBounds.y + adjustedCellBounds.height - preferredDecoratorHeight,
					preferredDecoratorWidth,
					preferredDecoratorHeight);
		case NONE:
		    break;
		}
		
		return null;
	}
	
	public ICellPainter getCellPainterAt(int x, int y, ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
		Rectangle decoratorCellPainterBounds = getDecoratorCellPainterBounds(cell, gc, adjustedCellBounds, configRegistry);
		if (decoratorCellPainterBounds.contains(x, y)) {
			return decoratorCellPainter.getCellPainterAt(x, y, cell, gc, decoratorCellPainterBounds, configRegistry);
		} else {
			Rectangle baseCellPainterBounds = getBaseCellPainterBounds(cell, gc, adjustedCellBounds, configRegistry);
			if (baseCellPainterBounds.contains(x, y)) {
				return baseCellPainter.getCellPainterAt(x, y, cell, gc, baseCellPainterBounds, configRegistry);
			}
		}
		return this;
	}
}
