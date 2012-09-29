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
package org.eclipse.nebula.widgets.nattable.extension.poi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.ILayerExporter;
import org.eclipse.nebula.widgets.nattable.export.IOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleProxy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;

public abstract class PoiExcelExporter implements ILayerExporter {

	private final IOutputStreamProvider outputStreamProvider;
	
	private Map<ExcelCellStyleAttributes, CellStyle> xlCellStyles;
	
	protected Workbook xlWorkbook;
	protected int sheetNumber;
	protected Sheet xlSheet;
	protected Row xlRow;

	public PoiExcelExporter(IOutputStreamProvider outputStreamProvider) {
		this.outputStreamProvider = outputStreamProvider;
	}
	
	@Override
	public OutputStream getOutputStream(Shell shell) {
		return outputStreamProvider.getOutputStream(shell);
	}
	
	public void exportBegin(OutputStream outputStream) throws IOException {
		xlCellStyles = new HashMap<ExcelCellStyleAttributes, CellStyle>();
		xlWorkbook = createWorkbook();
	}
	
	public void exportEnd(OutputStream outputStream) throws IOException {
		xlWorkbook.write(outputStream);
		
		xlCellStyles = null;
		xlWorkbook = null;
		sheetNumber = 0;
		xlSheet = null;
		xlRow = null;
	}

	@Override
	public void exportLayerBegin(OutputStream outputStream, String layerName) throws IOException {
		sheetNumber++;
		if (layerName == null || layerName.length() == 0) {
			layerName = "Sheet" + sheetNumber; //$NON-NLS-1$
		}
		xlSheet = xlWorkbook.createSheet(layerName);
	}

	@Override
	public void exportLayerEnd(OutputStream outputStream, String layerName) throws IOException {
	}

	@Override
	public void exportRowBegin(OutputStream outputStream, int rowPosition) throws IOException {
		xlRow = xlSheet.createRow(rowPosition);
	}

	@Override
	public void exportRowEnd(OutputStream outputStream, int rowPosition) throws IOException {
	}

	@Override
	public void exportCell(OutputStream outputStream, Object exportDisplayValue, ILayerCell cell, IConfigRegistry configRegistry) throws IOException {
		int columnPosition = cell.getColumnPosition();
		int rowPosition = cell.getRowPosition();
		
		if (columnPosition != cell.getOriginColumnPosition() || rowPosition != cell.getOriginRowPosition()) {
			return;
		}
		
		Cell xlCell = xlRow.createCell(columnPosition);
		
		int columnSpan = cell.getColumnSpan();
		int rowSpan = cell.getRowSpan();
		if (columnSpan > 1 || rowSpan > 1) {
			int lastRow = rowPosition + rowSpan - 1;
			int lastColumn = columnPosition + columnSpan - 1;
			xlSheet.addMergedRegion(new CellRangeAddress(rowPosition, lastRow, columnPosition, lastColumn));
		}
		
		CellStyleProxy cellStyle = new CellStyleProxy(configRegistry, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		org.eclipse.swt.graphics.Font font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);
		FontData fontData = font.getFontData()[0];
		
		xlCell.setCellStyle(getExcelCellStyle(fg, bg, fontData));
		
		if (exportDisplayValue == null) exportDisplayValue = ""; //$NON-NLS-1$
		
		if (exportDisplayValue instanceof Boolean) {
			xlCell.setCellValue((Boolean) exportDisplayValue);
		} else if (exportDisplayValue instanceof Calendar) {
			xlCell.setCellValue((Calendar) exportDisplayValue);
		} else if (exportDisplayValue instanceof Date) {
			xlCell.setCellValue((Date) exportDisplayValue);
		} else if (exportDisplayValue instanceof Number) {
			xlCell.setCellValue(((Number) exportDisplayValue).doubleValue());
		} else {
			xlCell.setCellValue(exportDisplayValue.toString());
		}
	}

	private CellStyle getExcelCellStyle(Color fg, Color bg, FontData fontData) {
		CellStyle xlCellStyle = xlCellStyles.get(new ExcelCellStyleAttributes(fg, bg, fontData));
		
		if (xlCellStyle == null) {
			xlCellStyle = xlWorkbook.createCellStyle();
			// Note: xl fill foreground = background
			setFillForegroundColor(xlCellStyle, bg);
			xlCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			
			Font xlFont = xlWorkbook.createFont();
			setFontColor(xlFont, fg);
			xlFont.setFontName(fontData.getName());
			xlFont.setFontHeightInPoints((short) fontData.getHeight());
			xlCellStyle.setFont(xlFont);
			
			xlCellStyles.put(new ExcelCellStyleAttributes(fg, bg, fontData), xlCellStyle);
		}
		return xlCellStyle;
	}

	protected abstract Workbook createWorkbook();
	
	protected abstract void setFillForegroundColor(CellStyle xlCellStyle, Color swtColor);
	
	protected abstract void setFontColor(Font xlFont, Color swtColor);
	
}
