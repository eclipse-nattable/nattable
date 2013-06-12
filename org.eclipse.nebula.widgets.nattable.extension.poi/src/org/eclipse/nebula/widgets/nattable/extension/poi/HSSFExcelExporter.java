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
package org.eclipse.nebula.widgets.nattable.extension.poi;

import java.util.ArrayList;
import java.util.List;


import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.nebula.widgets.nattable.export.FileOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.export.IOutputStreamProvider;
import org.eclipse.swt.graphics.Color;

public class HSSFExcelExporter extends PoiExcelExporter {

	private List<Color> colorIndex = new ArrayList<Color>();
	
	public HSSFExcelExporter() {
		super(new FileOutputStreamProvider("table_export.xls", new String[] { "Excel Workbook (*.xls)" }, new String[] { "*.xls" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public HSSFExcelExporter(IOutputStreamProvider outputStreamProvider) {
		super(outputStreamProvider);
	}
	
	@Override
	protected Workbook createWorkbook() {
		colorIndex = new ArrayList<Color>();
		return new HSSFWorkbook();
	}
	
	protected void setFillForegroundColor(CellStyle xlCellStyle, Color swtColor) {
		xlCellStyle.setFillForegroundColor(getColorIndex(swtColor));
	}
	
	protected void setFontColor(Font xlFont, Color swtColor) {
		xlFont.setColor(getColorIndex(swtColor));
	}

	/**
	 * Note: The Excel HSSF format only supports a maximum of 56 custom colors. If you have more than that number of colors,
	 * bad things will happen when you try to export.
	 */
	private short getColorIndex(Color swtColor) {
		if (!colorIndex.contains(swtColor)) {
			colorIndex.add(swtColor);
			
			HSSFPalette palette = ((HSSFWorkbook) xlWorkbook).getCustomPalette();
			
		    palette.setColorAtIndex((short) (55 - colorIndex.indexOf(swtColor)),
		            (byte) swtColor.getRed(),
		            (byte) swtColor.getGreen(),
		            (byte) swtColor.getBlue()
		    );
		}
		
		return (short) (55 - colorIndex.indexOf(swtColor));
	}

}
