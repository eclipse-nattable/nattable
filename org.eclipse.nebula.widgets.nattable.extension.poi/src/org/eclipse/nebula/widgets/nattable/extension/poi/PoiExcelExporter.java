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
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.ILayerExporter;
import org.eclipse.nebula.widgets.nattable.export.IOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleProxy;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.swt.SWT;
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

	private boolean applyBackgroundColor = true;
	
	public PoiExcelExporter(IOutputStreamProvider outputStreamProvider) {
		this.outputStreamProvider = outputStreamProvider;
	}
	
	@Override
	public OutputStream getOutputStream(Shell shell) {
		return outputStreamProvider.getOutputStream(shell);
	}
	
	@Override
	public void exportBegin(OutputStream outputStream) throws IOException {
		xlCellStyles = new HashMap<ExcelCellStyleAttributes, CellStyle>();
		xlWorkbook = createWorkbook();
	}
	
	@Override
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
		
		CellStyleProxy cellStyle = new CellStyleProxy(configRegistry, DisplayMode.NORMAL, cell.getConfigLabels().getLabels());
		Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		org.eclipse.swt.graphics.Font font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);
		FontData fontData = font.getFontData()[0];
		String dataFormat = null;
		
		int hAlign = HorizontalAlignmentEnum.getSWTStyle(cellStyle);
		int vAlign = VerticalAlignmentEnum.getSWTStyle(cellStyle);
		
		if (exportDisplayValue == null) exportDisplayValue = ""; //$NON-NLS-1$
		
		if (exportDisplayValue instanceof Boolean) {
			xlCell.setCellValue((Boolean) exportDisplayValue);
		} else if (exportDisplayValue instanceof Calendar) {
			dataFormat = getDataFormatString(cell, configRegistry);
			xlCell.setCellValue((Calendar) exportDisplayValue);
		} else if (exportDisplayValue instanceof Date) {
			dataFormat = getDataFormatString(cell, configRegistry);
			xlCell.setCellValue((Date) exportDisplayValue);
		} else if (exportDisplayValue instanceof Number) {
			xlCell.setCellValue(((Number) exportDisplayValue).doubleValue());
		} else {
			xlCell.setCellValue(exportDisplayValue.toString());
		}

		CellStyle xlCellStyle = getExcelCellStyle(fg, bg, fontData, dataFormat, hAlign, vAlign);
		xlCell.setCellStyle(xlCellStyle);
	}

	private CellStyle getExcelCellStyle(
			Color fg, Color bg, FontData fontData, String dataFormat, int hAlign, int vAlign) {
		
		CellStyle xlCellStyle = xlCellStyles.get(
				new ExcelCellStyleAttributes(fg, bg, fontData, dataFormat, hAlign, vAlign));
		
		if (xlCellStyle == null) {
			xlCellStyle = xlWorkbook.createCellStyle();
			
			if (applyBackgroundColor) {
				// Note: xl fill foreground = background
				setFillForegroundColor(xlCellStyle, bg);
				xlCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			}
			
			Font xlFont = xlWorkbook.createFont();
			setFontColor(xlFont, fg);
			xlFont.setFontName(fontData.getName());
			xlFont.setFontHeightInPoints((short) fontData.getHeight());
			xlCellStyle.setFont(xlFont);

			switch (hAlign) {
				case SWT.CENTER:	xlCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
								 	break;
				case SWT.LEFT: 		xlCellStyle.setAlignment(CellStyle.ALIGN_LEFT);
									break;
				case SWT.RIGHT: 	xlCellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
									break;
			}
			switch (vAlign) {
				case SWT.TOP:		xlCellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
								 	break;
				case SWT.CENTER: 	xlCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
									break;
				case SWT.BOTTOM: 	xlCellStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
									break;
			}
			
			if (dataFormat != null) {
				CreationHelper createHelper = xlWorkbook.getCreationHelper();
				xlCellStyle.setDataFormat(
						createHelper.createDataFormat().getFormat(dataFormat));
			}

			xlCellStyles.put(
					new ExcelCellStyleAttributes(fg, bg, fontData, dataFormat, hAlign, vAlign), xlCellStyle);
		}
		return xlCellStyle;
	}

	/**
	 * 
	 * @param cell The cell for which the date format needs to be determined.
	 * @param configRegistry The ConfigRegistry needed to retrieve the configuration.
	 * @return The date format that should be used to format Date or Calendar values
	 * 			in the export.
	 */
	protected String getDataFormatString(ILayerCell cell, IConfigRegistry configRegistry) {
		String dataFormat = configRegistry.getConfigAttribute(
				ExportConfigAttributes.DATE_FORMAT, DisplayMode.NORMAL, cell.getConfigLabels().getLabels());
		if (dataFormat == null) {
			dataFormat = "m/d/yy h:mm"; //$NON-NLS-1$
		}
		return dataFormat;
	}
	
	/**
	 * 
	 * @param applyBackgroundColor <code>true</code> to apply the background color set in the NatTable
	 * 			to the exported Excel. This also includes white background and header background color.
	 * 			<code>false</code> if the background color should not be set on export.
	 */
	public void setApplyBackgroundColor(boolean applyBackgroundColor) {
		this.applyBackgroundColor = applyBackgroundColor;
	}
	
	protected abstract Workbook createWorkbook();
	
	protected abstract void setFillForegroundColor(CellStyle xlCellStyle, Color swtColor);
	
	protected abstract void setFontColor(Font xlFont, Color swtColor);
	
}
