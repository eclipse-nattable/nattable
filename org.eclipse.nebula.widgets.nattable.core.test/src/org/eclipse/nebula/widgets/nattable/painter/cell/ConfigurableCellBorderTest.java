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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigurableCellBorderTest {

	private NatTable natTable;
	private ConfigRegistry configRegistry;

    @Before
    public void setUp() throws Exception {
        natTable = new NatTableFixture();
        configRegistry = (ConfigRegistry)natTable.getConfigRegistry();
    }

    @Test
    public void shouldReturnASelectedCellWithDottedLineStyling() {
        Style cellStyle = new Style();
		final BorderStyle defaultBorderStyle = new BorderStyle(13, GUIHelper.COLOR_YELLOW, LineStyleEnum.DOTTED);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, defaultBorderStyle);

		// Register line styling for body cells in selection mode
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT, SelectionStyleLabels.SELECTION_ANCHOR_STYLE);

		// Select and access cell
		natTable.doCommand(new SelectCellCommand(natTable, 2, 2, false, false));
		ILayerCell cell = natTable.getCellByPosition(2, 2);
		Assert.assertEquals(DisplayMode.SELECT, cell.getDisplayMode());

		// Check for line styling
		Assert.assertEquals(defaultBorderStyle, configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, cell.getDisplayMode(), cell.getConfigLabels().getLabels()).getAttributeValue(CellStyleAttributes.BORDER_STYLE));
    }

}
