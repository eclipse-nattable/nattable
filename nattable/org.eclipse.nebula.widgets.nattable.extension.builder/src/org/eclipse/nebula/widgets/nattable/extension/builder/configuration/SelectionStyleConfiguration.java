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

import static org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes.CELL_STYLE;
import static org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE;
import static org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE;
import static org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes.BACKGROUND_COLOR;
import static org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes.FONT;
import static org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes.FOREGROUND_COLOR;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.SELECT;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableStyle;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;


public class SelectionStyleConfiguration extends DefaultSelectionStyleConfiguration {

	private final TableStyle tableStyle;
	private final Style evenRowStyle;
	private final Style oddRowStyle;

	public SelectionStyleConfiguration(TableStyle tableStyle) {
		this.tableStyle = tableStyle;

		// Anchor style
		anchorBorderColor = tableStyle.anchorSelectionBgColor;
		anchorBgColor = tableStyle.anchorSelectionBgColor;
		anchorFgColor = tableStyle.anchorSelectionFgColor;

		// Selected headers style
		selectedHeaderBgColor = tableStyle.headerSelectionBgColor;
		selectedHeaderFgColor = tableStyle.headerSelectionFgColor;
		selectedHeaderFont = tableStyle.headerSelectionFont;

		// Even/row row sensitive selection style
		evenRowStyle = new Style();
		evenRowStyle.setAttributeValue(BACKGROUND_COLOR, tableStyle.evenRowCellSelectionBgColor);
		evenRowStyle.setAttributeValue(FOREGROUND_COLOR, tableStyle.evenRowCellSelectionFgColor);
		evenRowStyle.setAttributeValue(FONT, tableStyle.cellSelectionFont);

		oddRowStyle = new Style();
		oddRowStyle.setAttributeValue(BACKGROUND_COLOR, tableStyle.oddRowCellSelectionBgColor);
		oddRowStyle.setAttributeValue(FOREGROUND_COLOR, tableStyle.oddRowCellSelectionFgColor);
		oddRowStyle.setAttributeValue(FONT, tableStyle.cellSelectionFont);
	}

	@Override
	protected void configureHeaderFullySelectedStyle(IConfigRegistry configRegistry) {
		Style fullySelectedStyle = new Style();
		fullySelectedStyle.setAttributeValue(BACKGROUND_COLOR, tableStyle.fullySelectedHeaderBgColor);
		fullySelectedStyle.setAttributeValue(FOREGROUND_COLOR, tableStyle.fullySelectedHeaderFgColor);
		fullySelectedStyle.setAttributeValue(FONT, tableStyle.fullySelectedHeaderFont);

		configRegistry.registerConfigAttribute(CELL_STYLE, fullySelectedStyle, SELECT, SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
		configRegistry.registerConfigAttribute(CELL_STYLE, fullySelectedStyle, SELECT, SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);

		configRegistry.registerConfigAttribute(CELL_STYLE, evenRowStyle, SELECT, EVEN_ROW_CONFIG_TYPE);
		configRegistry.registerConfigAttribute(CELL_STYLE, oddRowStyle, SELECT, ODD_ROW_CONFIG_TYPE);
	}

}
