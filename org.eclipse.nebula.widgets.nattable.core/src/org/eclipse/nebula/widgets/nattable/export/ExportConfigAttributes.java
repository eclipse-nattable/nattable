/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.export;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

/**
 * Configuration attributes that are used to configure the export functionality.
 * 
 * @author Dirk Fauth
 *
 */
public interface ExportConfigAttributes {

	/**
	 * The configuration attribute for specifying the concrete implementation instance of
	 * ILayerExporter that should be used for exporting the NatTable.
	 */
	ConfigAttribute<ILayerExporter> EXPORTER = new ConfigAttribute<ILayerExporter>();
	/**
	 * The configuration attribute for specifying a formatter that should be used to
	 * format the values for the export.
	 */
	ConfigAttribute<IExportFormatter> EXPORT_FORMATTER = new ConfigAttribute<IExportFormatter>();
	/**
	 * The configuration attribute for specifying the format that should be used for exporting
	 * Calendar or Date objects appropriately.
	 */
	ConfigAttribute<String> DATE_FORMAT = new ConfigAttribute<String>();

}
