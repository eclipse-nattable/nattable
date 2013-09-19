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
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.IStyle;

public interface CellConfigAttributes {

	ConfigAttribute<ICellPainter> CELL_PAINTER = new ConfigAttribute<ICellPainter>();
	
	ConfigAttribute<IStyle> CELL_STYLE = new ConfigAttribute<IStyle>();
	
	ConfigAttribute<IDisplayConverter> DISPLAY_CONVERTER = new ConfigAttribute<IDisplayConverter>();
	
}
