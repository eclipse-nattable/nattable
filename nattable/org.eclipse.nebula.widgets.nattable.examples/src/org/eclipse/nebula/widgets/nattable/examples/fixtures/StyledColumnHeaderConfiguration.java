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
package org.eclipse.nebula.widgets.nattable.examples.fixtures;


import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.examples.examples._104_Styling._000_Styled_grid;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Extends the default column header style configuration to add custom painters for the column headers.
 * This has to be added to the table using the addConfiguration() method.
 *
 * @see _000_Styled_grid
 */
public class StyledColumnHeaderConfiguration extends DefaultColumnHeaderStyleConfiguration {

	public StyledColumnHeaderConfiguration() {
		font = GUIHelper.getFont(new FontData("Verdana", 10, SWT.BOLD));
	}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		super.configureRegistry(configRegistry);
		addNormalModeStyling(configRegistry);
		addSelectedModeStyling(configRegistry);
	}

	private void addSelectedModeStyling(IConfigRegistry configRegistry) {
		Image selectedBgImage = new Image(Display.getDefault(), getClass().getResourceAsStream("selected_column_header_bg.png"));

		TextPainter txtPainter = new TextPainter(false, false);
		ICellPainter selectedCellPainter = new BackgroundImagePainter(txtPainter, selectedBgImage, GUIHelper.getColor(192, 192, 192));
		// If sorting is enables we still want the sort icon to be drawn.
		SortableHeaderTextPainter selectedHeaderPainter = new SortableHeaderTextPainter(selectedCellPainter, false, true);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, selectedHeaderPainter, DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
	}

	private void addNormalModeStyling(IConfigRegistry configRegistry) {
		Image bgImage = new Image(Display.getDefault(), getClass().getResourceAsStream("column_header_bg.png"));

		TextPainter txtPainter = new TextPainter(false, false);
		ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter, bgImage, GUIHelper.getColor(192, 192, 192));
		SortableHeaderTextPainter headerPainter = new SortableHeaderTextPainter(bgImagePainter, false, true);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, headerPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, headerPainter, DisplayMode.NORMAL, GridRegion.CORNER);
	}
}
