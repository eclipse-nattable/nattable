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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.FileOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.export.ILayerExporter;
import org.eclipse.nebula.widgets.nattable.export.IOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleProxy;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;

/**
 * This class is used to export a NatTable to an Excel spreadsheet by using a 
 * XML format.
 */
public class ExcelExporter implements ILayerExporter {

	private static final Log log = LogFactory.getLog(ExcelExporter.class);
	
	private static final String EXCEL_HEADER_FILE = "excelExportHeader.txt"; //$NON-NLS-1$
	
	/**
	 * The IOutputStreamProvider that is used to create new OutputStreams on
	 * beginning new export operations.
	 */
	private final IOutputStreamProvider outputStreamProvider;

	/**
	 * Creates a new ExcelExporter using a FileOutputStreamProvider with default values.
	 */
	public ExcelExporter() {
		this(new FileOutputStreamProvider("table_export.xls", new String[] { "Excel Workbok (*.xls)" }, new String[] { "*.xls" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * Creates a new ExcelExporter that uses the given IOutputStreamProvider for retrieving
	 * the OutputStream to write the export to.
	 * @param outputStreamProvider The IOutputStreamProvider that is used to retrieve the 
	 * 			OutputStream to write the export to.
	 */
	public ExcelExporter(IOutputStreamProvider outputStreamProvider) {
		this.outputStreamProvider = outputStreamProvider;
	}
	
	@Override
	public OutputStream getOutputStream(Shell shell) {
		return outputStreamProvider.getOutputStream(shell);
	}
	
	@Override
	public void exportBegin(OutputStream outputStream) throws IOException {
	}

	@Override
	public void exportEnd(OutputStream outputStream) throws IOException {
	}

	@Override
	public void exportLayerBegin(OutputStream outputStream, String layerName) throws IOException {
		writeHeader(outputStream);
		outputStream.write(asBytes("<body><table border='1'>")); //$NON-NLS-1$
	}

	/**
	 * Writes the Excel header informations that are stored locally in the package
	 * structure.
	 * @throws IOException if an I/O error occurs on closing the stream to
	 * 			the header content file
	 */
	private void writeHeader(OutputStream outputStream) throws IOException {
		InputStream headerStream = null;
		try {
			headerStream = this.getClass().getResourceAsStream(EXCEL_HEADER_FILE);
			int c;
			while ((c = headerStream.read()) != -1) {
				outputStream.write(c);
			}
		} catch (Exception e) {
			log.error("Excel Exporter failed: " + e.getMessage(), e); //$NON-NLS-1$
		} finally {
			if (isNotNull(headerStream)) {
				headerStream.close();
			}
		}
	}

	@Override
	public void exportLayerEnd(OutputStream outputStream, String layerName) throws IOException {
		outputStream.write(asBytes("</table></body></html>")); //$NON-NLS-1$
	}

	@Override
	public void exportRowBegin(OutputStream outputStream, int rowPosition) throws IOException {
		outputStream.write(asBytes("<tr>\n")); //$NON-NLS-1$
	}

	@Override
	public void exportRowEnd(OutputStream outputStream, int rowPosition) throws IOException {
		outputStream.write(asBytes("</tr>\n")); //$NON-NLS-1$
	}

	@Override
	public void exportCell(OutputStream outputStream, Object exportDisplayValue, ILayerCell cell, IConfigRegistry configRegistry) throws IOException {
		CellStyleProxy cellStyle = new CellStyleProxy(configRegistry, DisplayMode.NORMAL, cell.getConfigLabels().getLabels());
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

	@Override
	public Object getResult() {
		return outputStreamProvider.getResult();
	}
	
}
