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
package org.eclipse.nebula.widgets.nattable.extension.builder.configuration;


import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableStyle;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.graphics.Image;

public class ColumnHeaderConfiguration extends DefaultColumnHeaderLayerConfiguration {

	private final TableStyle tableStyle;

	public ColumnHeaderConfiguration(TableStyle tableStyle) {
		this.tableStyle = tableStyle;
	}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		addNormalModeStyling(configRegistry);
		addSelectedModeStyling(configRegistry);
	}

	private void addSelectedModeStyling(IConfigRegistry configRegistry) {
		Image selectedBgImage = tableStyle.columnHeaderSelectedBgImage;
		ICellPainter sortHeaderPainter;

		if(ObjectUtils.isNotNull(selectedBgImage)){
			TextPainter txtPainter = new TextPainter(false, false);
			ICellPainter selectedCellPainter = new BackgroundImagePainter(txtPainter, selectedBgImage, GUIHelper.getColor(192, 192, 192));
			sortHeaderPainter = new SortableHeaderTextPainter(selectedCellPainter, false, false);
		} else {
			sortHeaderPainter = new SortableHeaderTextPainter(new BeveledBorderDecorator(new TextPainter()), false, false);
		}

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, sortHeaderPainter, DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
	}

	private void addNormalModeStyling(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, tableStyle.columnHeaderBGColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, tableStyle.columnHeaderFGColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, tableStyle.columnHeaderFont);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.CORNER);

		// Gradient painter
		Image bgImage = tableStyle.columnHeaderBgImage;

		if (ObjectUtils.isNotNull(bgImage)) {
			TextPainter txtPainter = new TextPainter(false, false);
			ICellPainter cellPainter = new BackgroundImagePainter(txtPainter, bgImage, GUIHelper.getColor(192, 192, 192));
			SortableHeaderTextPainter sortHeaderPainter = new SortableHeaderTextPainter(cellPainter, false, false);

			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, sortHeaderPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, sortHeaderPainter, DisplayMode.NORMAL, GridRegion.CORNER);
		} else {
			SortableHeaderTextPainter sortHeaderPainter = new SortableHeaderTextPainter(new BeveledBorderDecorator(new TextPainter()), false, false);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, sortHeaderPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, sortHeaderPainter, DisplayMode.NORMAL, GridRegion.CORNER);
		}
	}
}
