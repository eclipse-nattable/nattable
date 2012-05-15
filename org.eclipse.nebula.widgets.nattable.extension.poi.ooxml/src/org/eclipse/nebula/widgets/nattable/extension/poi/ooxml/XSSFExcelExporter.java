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
package org.eclipse.nebula.widgets.nattable.extension.poi.ooxml;


import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.nebula.widgets.nattable.export.FileOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.export.IOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.extension.poi.PoiExcelExporter;
import org.eclipse.swt.graphics.Color;

public class XSSFExcelExporter extends PoiExcelExporter {

	public XSSFExcelExporter() {
		super(new FileOutputStreamProvider("table_export.xlsx", new String[] { "Excel Workbook (*.xlsx)" }, new String[] { "*.xlsx" }));
	}
	
	public XSSFExcelExporter(IOutputStreamProvider outputStreamProvider) {
		super(outputStreamProvider);
	}
	
	@Override
	protected Workbook createWorkbook() {
		return new XSSFWorkbook();
	}
	
	protected void setFillForegroundColor(CellStyle xlCellStyle, Color swtColor) {
		((XSSFCellStyle) xlCellStyle).setFillForegroundColor(new XSSFColor(new java.awt.Color(swtColor.getRed(), swtColor.getGreen(), swtColor.getBlue())));
	}
	
	protected void setFontColor(Font xlFont, Color swtColor) {
		((XSSFFont) xlFont).setColor(new XSSFColor(new java.awt.Color(swtColor.getRed(), swtColor.getGreen(), swtColor.getBlue())));
	}

}
