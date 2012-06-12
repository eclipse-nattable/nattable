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
package org.eclipse.nebula.widgets.nattable.export.excel;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.FileOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.export.ILayerExporter;
import org.eclipse.nebula.widgets.nattable.export.IOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleProxy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;

public class ExcelExporter implements ILayerExporter {

	private static final String EXCEL_HEADER_FILE = "excelExportHeader.txt"; //$NON-NLS-1$
	
	private final IOutputStreamProvider outputStreamProvider;

	public ExcelExporter() {
		this(new FileOutputStreamProvider("table_export.xls", new String[] { "Excel Workbok (*.xls)" }, new String[] { "*.xls" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public ExcelExporter(IOutputStreamProvider outputStreamProvider) {
		this.outputStreamProvider = outputStreamProvider;
	}
	
	public OutputStream getOutputStream(Shell shell) {
		return outputStreamProvider.getOutputStream(shell);
	}
	
	public void exportBegin(OutputStream outputStream) throws IOException {
	}

	public void exportEnd(OutputStream outputStream) throws IOException {
	}

	public void exportLayerBegin(OutputStream outputStream, String layerName) throws IOException {
		writeHeader(outputStream);
		outputStream.write(asBytes("<body><table border='1'>")); //$NON-NLS-1$
	}

	private void writeHeader(OutputStream outputStream) throws IOException {
		InputStream headerStream = null;
		try {
			headerStream = this.getClass().getResourceAsStream(EXCEL_HEADER_FILE);
			int c;
			while ((c = headerStream.read()) != -1) {
				outputStream.write(c);
			}
		} catch (Exception e) {
			logError(e);
		} finally {
			if (isNotNull(headerStream)) {
				headerStream.close();
			}
		}
	}

	public void exportLayerEnd(OutputStream outputStream, String layerName) throws IOException {
		outputStream.write(asBytes("</table></body></html>")); //$NON-NLS-1$
	}

	public void exportRowBegin(OutputStream outputStream, int rowPosition) throws IOException {
		outputStream.write(asBytes("<tr>\n")); //$NON-NLS-1$
	}

	public void exportRowEnd(OutputStream outputStream, int rowPosition) throws IOException {
		outputStream.write(asBytes("</tr>\n")); //$NON-NLS-1$
	}

	public void exportCell(OutputStream outputStream, Object exportDisplayValue, ILayerCell cell, IConfigRegistry configRegistry) throws IOException {
		CellStyleProxy cellStyle = new CellStyleProxy(configRegistry, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		Font font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);

		String htmlAttributes = String.format("style='color: %s; background-color: %s; %s;'", //$NON-NLS-1$
		                     getColorInCSSFormat(fg),
		                     getColorInCSSFormat(bg),
		                     getFontInCSSFormat(font));
		
		String htmlText = exportDisplayValue != null ? exportDisplayValue.toString() : ""; //$NON-NLS-1$
		
		if (htmlText.startsWith(" ")) { //$NON-NLS-1$
			htmlAttributes += " x:str=\"'" + htmlText + "\";"; //$NON-NLS-1$ //$NON-NLS-2$
			htmlText = htmlText.replaceFirst("^(\\ *)", "<span style='mso-spacerun:yes'>$1</span>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		outputStream.write(asBytes(String.format("\t<td %s>%s</td>\n", htmlAttributes, htmlText))); //$NON-NLS-1$
	}

	private byte[] asBytes(String string) {
		return string.getBytes();
	}

	private void logError(Exception e) {
		System.err.println("Excel Exporter failed: " + e.getMessage()); //$NON-NLS-1$
		e.printStackTrace(System.err);
	}
	
	private String getFontInCSSFormat(Font font) {
		FontData fontData = font.getFontData()[0];
		String fontName = fontData.getName();
		int fontStyle = fontData.getStyle();
		String HTML_STYLES[] = new String[] { "NORMAL", "BOLD", "ITALIC" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return String.format("font: %s; font-family: %s", //$NON-NLS-1$
		                     fontStyle <= 2 ? HTML_STYLES[fontStyle] : HTML_STYLES[0],
		                     fontName);
	}

	private String getColorInCSSFormat(Color color) {
		return String.format("rgb(%d,%d,%d)", //$NON-NLS-1$
		                     Integer.valueOf(color.getRed()),
		                     Integer.valueOf(color.getGreen()),
		                     Integer.valueOf(color.getBlue()));
	}
	
}
