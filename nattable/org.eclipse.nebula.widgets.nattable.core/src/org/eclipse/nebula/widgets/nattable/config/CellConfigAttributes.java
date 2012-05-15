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
package org.eclipse.nebula.widgets.nattable.config;

import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.export.IExportFormatter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.IStyle;

public interface CellConfigAttributes {

	public static final ConfigAttribute<ICellPainter> CELL_PAINTER = new ConfigAttribute<ICellPainter>();
	
	public static final ConfigAttribute<IStyle> CELL_STYLE = new ConfigAttribute<IStyle>();
	
	public static final ConfigAttribute<IDisplayConverter> DISPLAY_CONVERTER = new ConfigAttribute<IDisplayConverter>();

	public static final ConfigAttribute<IExportFormatter> EXPORT_FORMATTER = new ConfigAttribute<IExportFormatter>();
	
}
