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
	 * @param spacing The space between text and cell border
	 */
	public VerticalTextPainter(boolean wrapText, boolean paintBg, int spacing) {
		this(wrapText, paintBg, spacing, false);
	}

	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param calculate tells the text painter to calculate the cell borders regarding the content
	 */
	public VerticalTextPainter(boolean wrapText, boolean paintBg, boolean calculate) {
		this(wrapText, paintBg, 0, calculate);
	}

	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param calculateByTextLength tells the text painter to calculate the cell border by containing
	 * 			text length. For horizontal text rendering, this means the width of the cell is calculated
	 * 			by content, for vertical text rendering the height is calculated
	 * @param calculateByTextHeight tells the text painter to calculate the cell border by containing
	 * 			text height. For horizontal text rendering, this means the height of the cell is calculated
	 * 			by content, for vertical text rendering the width is calculated
	 */
	public VerticalTextPainter(boolean wrapText, boolean paintBg, 
			boolean calculateByTextLength, boolean calculateByTextHeight) {
		this(wrapText, paintBg, 0, calculateByTextLength, calculateByTextHeight);
	}
	
	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param spacing The space between text and cell border
	 * @param calculate tells the text painter to calculate the cell borders regarding the content
	 */
	public VerticalTextPainter(boolean wrapText, boolean paintBg, int spacing, boolean calculate) {
		super(wrapText, paintBg, spacing, calculate);
	}

	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param spacing The space between text and cell border
	 * @param calculateByTextLength tells the text painter to calculate the cell border by containing
	 * 			text length. For horizontal text rendering, this means the width of the cell is calculated
	 * 			by content, for vertical text rendering the height is calculated
	 * @param calculateByTextHeight tells the text painter to calculate the cell border by containing
	 * 			text height. For horizontal text rendering, this means the height of the cell is calculated
	 * 			by content, for vertical text rendering the width is calculated
	 */
	public VerticalTextPainter(boolean wrapText, boolean paintBg, int spacing, 
			boolean calculateByTextLength, boolean calculateByTextHeight) {
		super(wrapText, paintBg, spacing, calculateByTextLength, calculateByTextHeight);
	}

	@Override
	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry){
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		return gc.textExtent(convertDataType(cell, configRegistry)).y + (spacing*2);
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
		int contentWidth = (fontHeight * numberOfNewLines) + (spacing*2);
		int contentToCellDiff = (cell.getBounds().width - rectangle.width);
		
		if ((contentWidth > rectangle.width) && calculateByTextHeight) {
			ILayer layer = cell.getLayer();
			layer.doCommand(
					new ColumnResizeCommand(
							layer, 
							cell.getColumnPosition(), 
							contentWidth + contentToCellDiff));
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
			layer.doCommand(new RowResizeCommand(layer, cell.getRowPosition(), 
					contentHeight));
		}
	}

	@Override
	protected int calculatePadding(ILayerCell cell, int availableLength) {
		return cell.getBounds().height - availableLength;
	}
}