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
package org.eclipse.nebula.widgets.nattable.export;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.swt.widgets.Shell;

public interface ILayerExporter {

	public static final ConfigAttribute<ILayerExporter> CONFIG_ATTRIBUTE = new ConfigAttribute<ILayerExporter>();

	OutputStream getOutputStream(Shell shell);
	
	/**
	 * Should be called only once at the beginning of an export operation. If the exporter supports exporting multiple layers as part of a single
	 * export operation, this method will be called only once before any layers are exported.
	 * @param outputStream
	 * @throws IOException
	 */
	void exportBegin(OutputStream outputStream) throws IOException;
	
	/**
	 * Should be called only once at the end of an export operation. If the exporter supports exporting multiple layers as part of a single
	 * export operation, this method will be called only once after all layers are exported.
	 * @param outputStream
	 * @throws IOException
	 */
	void exportEnd(OutputStream outputStream) throws IOException;
	
	void exportLayerBegin(OutputStream outputStream, String layerName) throws IOException;
	
	void exportLayerEnd(OutputStream outputStream, String layerName) throws IOException;
	
	void exportRowBegin(OutputStream outputStream, int rowPosition) throws IOException;
	
	void exportRowEnd(OutputStream outputStream, int rowPosition) throws IOException;
	
	void exportCell(OutputStream outputStream, Object exportDisplayValue, ILayerCell cell, IConfigRegistry configRegistry) throws IOException;
	
}
