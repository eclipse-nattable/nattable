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
package org.eclipse.nebula.widgets.nattable.layer.config;


import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Sets up column header styling.
 * Added by {@link DefaultColumnHeaderLayerConfiguration}
 */
public class DefaultColumnHeaderStyleConfiguration extends AbstractRegistryConfiguration {

	public Font font = GUIHelper.getFont(new FontData("Verdana", 10, SWT.NORMAL)); //$NON-NLS-1$
	public Color bgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
	public Color fgColor = GUIHelper.COLOR_WIDGET_FOREGROUND;
	public Color gradientBgColor = GUIHelper.COLOR_WHITE;
	public Color gradientFgColor = GUIHelper.getColor(136, 212, 215);
	public HorizontalAlignmentEnum hAlign = HorizontalAlignmentEnum.CENTER;
	public VerticalAlignmentEnum vAlign = VerticalAlignmentEnum.MIDDLE;
	public BorderStyle borderStyle = null;

	public ICellPainter cellPainter = new BeveledBorderDecorator(new TextPainter());

	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.CORNER);

		// Normal
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_BACKGROUND_COLOR, gradientBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.GRADIENT_FOREGROUND_COLOR, gradientFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.CORNER);
	}
}
