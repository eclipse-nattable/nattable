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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;

public class ExcelCellStyleAttributes {

	private final Color fg;
	private final Color bg;
	private final FontData fontData;
	private final String dataFormat;
	private final int hAlign;
	private final int vAlign;
	private final boolean vertical;

	public ExcelCellStyleAttributes(Color fg, Color bg, FontData fontData, String dataFormat, int hAlign, int vAlign, boolean vertical) {
		this.fg = fg;
		this.bg = bg;
		this.fontData = fontData;
		this.dataFormat = dataFormat;
		this.hAlign = hAlign;
		this.vAlign = vAlign;
		this.vertical = vertical;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof ExcelCellStyleAttributes == false) {
			return false;
		}
		
		ExcelCellStyleAttributes that = (ExcelCellStyleAttributes) obj;
		
		return new EqualsBuilder()
			.append(this.fg, that.fg)
			.append(this.bg, that.bg)
			.append(this.fontData, that.fontData)
			.append(this.dataFormat, that.dataFormat)
			.append(this.hAlign, that.hAlign)
			.append(this.vAlign, that.vAlign)
			.append(this.vertical, that.vertical)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(59, 187)
			.append(fg)
			.append(bg)
			.append(fontData)
			.append(dataFormat)
			.append(hAlign)
			.append(vAlign)
			.append(vertical)
			.toHashCode();
	}
	
}
