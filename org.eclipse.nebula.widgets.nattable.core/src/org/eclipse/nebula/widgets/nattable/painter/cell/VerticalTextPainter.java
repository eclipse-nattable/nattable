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
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * TextPainter that draws text into a cell vertically.
 * Can handle word wrapping and/or word cutting and/or automatic calculation and resizing of the
 * cell width and height if the text does not fit into the cell.
 */
public class VerticalTextPainter extends AbstractTextPainter {

	public VerticalTextPainter() {
		this(false, true);
	}

	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 */
	public VerticalTextPainter(boolean wrapText, boolean paintBg) {
		this(wrapText, paintBg, 0);
	}
	
	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param spacing
	 */
	public VerticalTextPainter(boolean wrapText, boolean paintBg, int spacing) {
		this(wrapText, paintBg, spacing, false);
	}

	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param calculate tells the text painter to calculate the cell border
	 * 			If wrapText is <code>true</code> the needed row height is calculated 
	 * 			to show the whole cell content.
	 * 			If wrapText is <code>false</code> the needed column width is calculated
	 * 			to show the whole cell content. 
	 */
	public VerticalTextPainter(boolean wrapText, boolean paintBg, boolean calculate) {
		this(wrapText, paintBg, 0, calculate);
	}
	
	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param spacing
	 * @param calculate tells the text painter to calculate the cell border
	 * 			If wrapText is <code>true</code> the needed row height is calculated 
	 * 			to show the whole cell content.
	 * 			If wrapText is <code>false</code> the needed column width is calculated
	 * 			to show the whole cell content. 
	 */
	public VerticalTextPainter(boolean wrapText, boolean paintBg, int spacing, boolean calculate) {
		super(wrapText, paintBg, spacing, calculate);
	}

	@Override
	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry){
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		return gc.textExtent(convertDataType(cell, configRegistry)).y;
	}

	@Override
	public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		return getLengthFromCache(gc, convertDataType(cell, configRegistry)) + (spacing*2) + 1;
	}

	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		if (paintBg) {
			super.paintCell(cell, gc, rectangle, configRegistry);
		}

		Rectangle originalClipping = gc.getClipping();
		gc.setClipping(rectangle.intersection(originalClipping));

		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		setupGCFromConfig(gc, cellStyle);
		
		boolean underline = renderUnderlined(cellStyle);
		boolean strikethrough = renderStrikethrough(cellStyle);

		String text = convertDataType(cell, configRegistry);

		//calculate the text to display, adds dots if the text is longer than the available
		//row height and adds new lines instead of spaces if word wrapping is enabled
		text = getTextToDisplay(cell, gc, rectangle.height, text);

		int numberOfNewLines = getNumberOfNewLines(text);
		
		//if the content width is bigger than the available column width
		//we're extending the column width (only if word wrapping is enabled)
		int fontHeight = gc.getFontMetrics().getHeight();
		int contentWidth = fontHeight * numberOfNewLines;
		int contentToCellDiff = (cell.getBounds().width - rectangle.width);
		
		if ((contentWidth > rectangle.width) && calculate) {
			ILayer layer = cell.getLayer();
			layer.doCommand(
					new ColumnResizeCommand(
							layer, 
							cell.getColumnPosition(), 
							contentWidth + (spacing*2) + contentToCellDiff));
		}
		
		if (text != null && text.length() > 0) {
			if (numberOfNewLines == 1) {
				int contentHeight = Math.min(getLengthFromCache(gc, text), rectangle.height);
				
				GraphicsUtils.drawVerticalText(
						text, 
						rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth) + spacing,
						rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight + spacing),
						underline,
						strikethrough,
						paintBg,
						gc, 
						SWT.UP);
			}
			else {
				//draw every line by itself because of the alignment, otherwise the whole text
				//is always aligned right
				int xStartPos = rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth);
				String[] lines = text.split("\n"); //$NON-NLS-1$
				for (String line : lines) {
					int lineContentWidth = Math.min(getLengthFromCache(gc, line), rectangle.width);
					
					GraphicsUtils.drawVerticalText(
							line,
							xStartPos + spacing,
							rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, lineContentWidth + spacing),
							underline,
							strikethrough,
							paintBg,
							gc,
							SWT.UP);
					
					//after every line calculate the x start pos new
					xStartPos += fontHeight;
				}
			}
		}

		gc.setClipping(originalClipping);
	}

	@Override
	protected void setNewMinLength(ILayerCell cell, int contentHeight) {
		int cellLength = cell.getBounds().height;
		if (cellLength < contentHeight) {

			ILayer layer = cell.getLayer();
			int row = layer.getRowIndexByPosition(cell.getRowPosition());
			layer.doCommand(new RowResizeCommand(layer, row, 
					contentHeight));
		}
	}

	@Override
	protected int calculatePadding(ILayerCell cell, int availableLength) {
		return cell.getBounds().height - availableLength;
	}
}