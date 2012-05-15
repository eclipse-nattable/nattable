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


import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class TableStyle {
	public static final Font DEFAULT_TABLE_FONT = GUIHelper.getFont(new FontData("Arial", 8, SWT.NORMAL));
	public static final Font DEFAULT_SELECTION_FONT = GUIHelper.getFont(new FontData("Arial", 8, SWT.BOLD));

	// Table style
	public Color tableBgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
	public Font tableFont = DEFAULT_TABLE_FONT;
	public Font editingFont = DEFAULT_TABLE_FONT;
	public Color evenRowColor = GUIHelper.getColor(238, 248, 255);
	public Color oddRowColor = GUIHelper.getColor(255, 255, 255);

	public int defaultRowHeight = 18;
	public int defaultColumnWidth = 100;

	// Columns/column header style
	public Font columnHeaderFont = DEFAULT_SELECTION_FONT;
	public Image columnHeaderBgImage = new Image(Display.getCurrent(), this.getClass().getResourceAsStream("header_bg.png"));
	public Image columnHeaderSelectedBgImage = new Image(Display.getCurrent(), this.getClass().getResourceAsStream("header_bg_selected.png"));
	public Color columnHeaderBGColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
	public Color columnHeaderFGColor = GUIHelper.getColor(6, 47, 83);
	public int columnHeaderHeight = 20;

	// Filter row
	public Color filterRowBGColor = GUIHelper.getColor(255, 255, 204);
	public Color filterRowFGColor = GUIHelper.COLOR_BLACK;
	public Font filterRowFont = DEFAULT_TABLE_FONT;

	// Column group header
	public int columnGroupHeaderHeight = 20;

	// Rows/row header style
	public Font rowHeaderFont = DEFAULT_TABLE_FONT;
	public Color rowHeaderBGColor = GUIHelper.getColor(201, 232, 253);
	public Color rowHeaderFGColor = GUIHelper.getColor(6, 47, 83);
	public int rowHeaderWidth = 40;

	public VerticalAlignmentEnum defaultVerticalAlign = VerticalAlignmentEnum.MIDDLE;
	public HorizontalAlignmentEnum defaultHorizontalAlign = HorizontalAlignmentEnum.CENTER;

	// Styles applied to selected elements
	public Color headerSelectionFgColor = GUIHelper.COLOR_BLACK;
	public Color headerSelectionBgColor = GUIHelper.getColor(new RGB(234, 247, 255));
	public Font headerSelectionFont = DEFAULT_SELECTION_FONT;

	public Color fullySelectedHeaderFgColor;
	public Color fullySelectedHeaderBgColor;
	public Font fullySelectedHeaderFont;

	public Color anchorSelectionFgColor = GUIHelper.COLOR_BLACK;
	public Color anchorSelectionBgColor = GUIHelper.getColor(new RGB(184, 232, 255));
	public Font anchorSelectionFont = DEFAULT_SELECTION_FONT;

	public Color evenRowCellSelectionBgColor = GUIHelper.getColor(230, 225, 240);
	public Color evenRowCellSelectionFgColor = GUIHelper.COLOR_BLACK;
	public Color oddRowCellSelectionBgColor = GUIHelper.getColor(new RGB(210, 210, 240));
	public Color oddRowCellSelectionFgColor = GUIHelper.COLOR_BLACK;
	public Font cellSelectionFont;
}
