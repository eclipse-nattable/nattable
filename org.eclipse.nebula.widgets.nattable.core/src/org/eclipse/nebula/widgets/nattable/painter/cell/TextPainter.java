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
 * TextPainter that draws text into a cell horizontally.
 * Can handle word wrapping and/or word cutting and/or automatic calculation and resizing of the
 * cell width and height if the text does not fit into the cell.
 */
public class TextPainter extends AbstractTextPainter {

	public TextPainter() {
		this(false, true);
	}

	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 */
	public TextPainter(boolean wrapText, boolean paintBg) {
		this(wrapText, paintBg, 0);
	}
	
	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param spacing The space between text and cell border
	 */
	public TextPainter(boolean wrapText, boolean paintBg, int spacing) {
		this(wrapText, paintBg, spacing, false);
	}
	
	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param calculate tells the text painter to calculate the cell borders regarding the content
	 */
	public TextPainter(boolean wrapText, boolean paintBg, boolean calculate) {
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
	public TextPainter(boolean wrapText, boolean paintBg, 
			boolean calculateByTextLength, boolean calculateByTextHeight) {
		this(wrapText, paintBg, 0, calculateByTextLength, calculateByTextHeight);
	}
	
	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 * @param spacing The space between text and cell border
	 * @param calculate tells the text painter to calculate the cell borders regarding the content
	 */
	public TextPainter(boolean wrapText, boolean paintBg, int spacing, boolean calculate) {
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
	public TextPainter(boolean wrapText, boolean paintBg, int spacing, 
			boolean calculateByTextLength, boolean calculateByTextHeight) {
		super(wrapText, paintBg, spacing, calculateByTextLength, calculateByTextHeight);
	}
	
	@Override
	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry){
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		return getLengthFromCache(gc, convertDataType(cell, configRegistry)) + (spacing*2) + 1;
	}

	@Override
	public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		return gc.textExtent(convertDataType(cell, configRegistry)).y + (spacing*2) + 1;
	}


	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		if (paintBg) {
			super.paintCell(cell, gc, rectangle, configRegistry);
		}

		if (paintFg) {
			Rectangle originalClipping = gc.getClipping();
			gc.setClipping(rectangle.intersection(originalClipping));
	
			IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
			setupGCFromConfig(gc, cellStyle);
			
			boolean underline = renderUnderlined(cellStyle);
			boolean strikethrough = renderStrikethrough(cellStyle);
			
			int fontHeight = gc.getFontMetrics().getHeight();
			String text = convertDataType(cell, configRegistry);
	
			// Draw Text
			text = getTextToDisplay(cell, gc, rectangle.width, text);
	
			int numberOfNewLines = getNumberOfNewLines(text);
			
			//if the content height is bigger than the available row height
			//we're extending the row height (only if word wrapping is enabled)
			int contentHeight = (fontHeight * numberOfNewLines) + (spacing*2);
			int contentToCellDiff = (cell.getBounds().height - rectangle.height);
	
			if (performRowResize(contentHeight, rectangle)) {
				ILayer layer = cell.getLayer();
				layer.doCommand(
						new RowResizeCommand(
								layer, 
								cell.getRowPosition(), 
								contentHeight + contentToCellDiff));
			}
			
			if (numberOfNewLines == 1) {
				int contentWidth = Math.min(getLengthFromCache(gc, text), rectangle.width);
				
				gc.drawText(
						text,
						rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth) + spacing,
						rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight) + spacing,
						SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER
				);
				
				if (underline || strikethrough) {
					//start x of line = start x of text
					int x = rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth) + spacing;
					//y = start y of text
					int y = rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight) + spacing; 
					
					//check and draw underline and strikethrough separately so it is possible to combine both
					if (underline) {
						//y = start y of text + font height 
						// - half of the font descent so the underline is between the baseline and the bottom
						int underlineY = y + fontHeight - (gc.getFontMetrics().getDescent() / 2);
						gc.drawLine(
								x, 
								underlineY, 
								x + gc.textExtent(text).x, 
								underlineY);
					}
					
					if (strikethrough) {
						//y = start y of text + half of font height + ascent so lower case characters are
						//also strikethrough
						int strikeY = y + (fontHeight / 2) + (gc.getFontMetrics().getLeading() / 2);
						gc.drawLine(
								x, 
								strikeY, 
								x + gc.textExtent(text).x, 
								strikeY);
					}
				}
			}
			else {
				//draw every line by itself because of the alignment, otherwise the whole text
				//is always aligned right
				int yStartPos = rectangle.y
						+ CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight);
				String[] lines = text.split("\n"); //$NON-NLS-1$
				for (String line : lines) {
					int lineContentWidth = Math.min(getLengthFromCache(gc, line), rectangle.width);
					
					gc.drawText(
							line,
							rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, lineContentWidth) + spacing,
							yStartPos + spacing,
							SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER
					);
					
					if (underline || strikethrough) {
						//start x of line = start x of text
						int x = rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, lineContentWidth) + spacing;
						//y = start y of text
						int y = yStartPos + spacing; 
								
						
						//check and draw underline and strikethrough separately so it is possible to combine both
						if (underline) {
							//y = start y of text + font height 
							// - half of the font descent so the underline is between the baseline and the bottom
							int underlineY = y + fontHeight - (gc.getFontMetrics().getDescent() / 2);
							gc.drawLine(
									x, 
									underlineY, 
									x + gc.textExtent(line).x, 
									underlineY);
						}
						
						if (strikethrough) {
							//y = start y of text + half of font height + ascent so lower case characters are
							//also strikethrough
							int strikeY = y + (fontHeight / 2) + (gc.getFontMetrics().getLeading() / 2);
							gc.drawLine(
									x, 
									strikeY, 
									x + gc.textExtent(line).x, 
									strikeY);
						}
					}
					
					//after every line calculate the y start pos new
					yStartPos += fontHeight;
				}
			}
	
			gc.setClipping(originalClipping);
		}
	}


	@Override
	protected void setNewMinLength(ILayerCell cell, int contentWidth) {
		int cellLength = cell.getBounds().width;
		if (cellLength < contentWidth) {
			//execute ColumnResizeCommand
			ILayer layer = cell.getLayer();
			layer.doCommand(new ColumnResizeCommand(layer, cell.getColumnPosition(), 
					contentWidth));
		}
	}

	@Override
	protected int calculatePadding(ILayerCell cell, int availableLength) {
		return cell.getBounds().width - availableLength;
	}
	
	/**
	 * Checks if a row resize needs to be triggered.
	 * @param contentHeight The necessary height to show the content completely
	 * @param rectangle The available rectangle to render to
	 * @return <code>true</code> if a row resize needs to be performed, <code>false</code> if not
	 */
	protected boolean performRowResize(int contentHeight, Rectangle rectangle) {
		return ((contentHeight > rectangle.height) && calculateByTextHeight);
	}
}
