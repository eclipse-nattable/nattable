/*******************************************************************************
 * Copyright (c) 2012 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Special {@link TextPainter} that will always calculate the row height of the cell dependent
 * to the content shown in the cell. It uses word wrapping and calculation of the cell height
 * to support showing long texts in a single cell. It will grow/shrink the row height on
 * resizing so always the optimal height is used for the row the cell resides.
 * 
 * <p>This {@link TextPainter} should preferably be used for tables that use percentage sizing
 * so the calculated row heights for example will grow/shrink correctly when resizing the
 * composite that contains the table.</p>
 * 
 * <p>It shouldn't be used for large tables that can be scrolled as the growing/shrinking on
 * scrolling can cause some side effects, like jumping layouts on scrolling.</p>
 * 
 * @author Dirk Fauth
 *
 * @see TextPainter
 * @see DataLayer#setColumnPercentageSizing(boolean)
 */
public class AutomaticRowHeightTextPainter extends TextPainter {

	public AutomaticRowHeightTextPainter() {
		super(true, true, true);
	}

	public AutomaticRowHeightTextPainter(int spacing) {
		super(true, true, spacing, true);
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
			int fontHeight = gc.getFontMetrics().getHeight();
			String text = convertDataType(cell, configRegistry);
	
			// Draw Text
			text = getTextToDisplay(cell, gc, rectangle.width, text);
	
			int numberOfNewLines = getNumberOfNewLines(text);
			
			//if the content height is bigger than the available row height
			//we're extending the row height (only if word wrapping is enabled)
			int contentHeight = fontHeight * numberOfNewLines;
			int contentToCellDiff = (cell.getBounds().height - rectangle.height);
	
			//this code differs from nattable original and needs to be preserved for dlfp
			if ((contentHeight != rectangle.height) && calculate) {
				ILayer layer = cell.getLayer();
				layer.doCommand(
						new RowResizeCommand(
								layer, 
								cell.getRowPosition(), 
								contentHeight + (spacing*2) + contentToCellDiff));
			}
			
			if (numberOfNewLines == 1) {
				int contentWidth = Math.min(getLengthFromCache(gc, text), rectangle.width);
				
				gc.drawText(
						text,
						rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth + spacing),
						rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight + spacing),
						SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER
				);
			}
			else {
				//draw every line by itself because of the alignment, otherwise the whole text
				//is always aligned right
				int yStartPos = rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight);
				String[] lines = text.split("\n"); //$NON-NLS-1$
				for (String line : lines) {
					int lineContentWidth = Math.min(getLengthFromCache(gc, line), rectangle.width);
					
					gc.drawText(
							line,
							rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, lineContentWidth + spacing),
							yStartPos + spacing,
							SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER
					);
					
					//after every line calculate the y start pos new
					yStartPos += fontHeight;
				}
			}
	
			gc.setClipping(originalClipping);
		}
	}

}
