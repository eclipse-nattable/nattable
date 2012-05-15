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
package org.eclipse.nebula.widgets.nattable.extension.builder.model;


import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class ColumnStyle {

	public Color bgColor = GUIHelper.COLOR_WHITE;
	public Color fgColor = GUIHelper.COLOR_BLACK;

	public HorizontalAlignmentEnum hAlign = HorizontalAlignmentEnum.LEFT;
	public VerticalAlignmentEnum vAlign = VerticalAlignmentEnum.TOP;

	public Font font = TableStyle.DEFAULT_TABLE_FONT;

	public BorderStyle borderStyle = new BorderStyle(0, GUIHelper.COLOR_BLACK, LineStyleEnum.SOLID);
}
