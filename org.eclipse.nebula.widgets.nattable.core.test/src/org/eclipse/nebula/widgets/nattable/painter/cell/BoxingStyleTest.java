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
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.PricingTypeBean;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BoxingStyleTest {

	private static final int ROW_HEADER_COLUMN_COUNT = 1;
	private NatTable natTable;
	private ConfigRegistry configRegistry;
	private Style cellStyle;
	private TextPainter cellPainter;
	private GC gc;

	@Before
	public void setUp() throws Exception {
		natTable = new NatTableFixture();
		configRegistry = (ConfigRegistry) natTable.getConfigRegistry();
		cellStyle = new Style();
		cellPainter = new TextPainter();

		gc = new GC(Display.getDefault());
	}

	@After
	public void tearDown() {
		gc.dispose();
	}

	// Background color
	@Test
	public void retrievedCellShouldHaveConfiguredBackground() {
		// Register background color for body cells in normal mode
		final Color backgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, backgroundColor);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

		// Check for background color styling
		ILayerCell cell = natTable.getCellByPosition(2, 2);
		IStyle cellStyle = configRegistry.getConfigAttribute(
				CellConfigAttributes.CELL_STYLE,
				cell.getDisplayMode(),
				cell.getConfigLabels().getLabels());
		Assert.assertEquals(backgroundColor, cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));

		// set up painter
		cellPainter.setupGCFromConfig(gc, cellStyle);
		Assert.assertEquals(backgroundColor, gc.getBackground());
	}

	// Foreground color
	@Test
	public void retrievedCellShouldHaveConfiguredForegroundColor() {
		// Register foreground color for body cells in normal mode
		final Color foregroundColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, foregroundColor);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

		// Check cell foreground color
		ILayerCell cell = natTable.getCellByPosition(2, 2);
		IStyle cellStyle = configRegistry.getConfigAttribute(
				CellConfigAttributes.CELL_STYLE,
				cell.getDisplayMode(),
				cell.getConfigLabels().getLabels());
		Assert.assertEquals(foregroundColor, cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));

		// set up painter
		cellPainter.setupGCFromConfig(gc, cellStyle);
		Assert.assertEquals(foregroundColor, gc.getForeground());
	}

	// Horizontal alignment
	@Test
	public void retreivedCellShouldHaveRightAlignment() {
		// Register horizontal alignment
		final HorizontalAlignmentEnum hAlignment = HorizontalAlignmentEnum.RIGHT;
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlignment);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

		// Check cell horizontal alignment
		ILayerCell cell = natTable.getCellByPosition(2, 2);
		Assert.assertEquals(hAlignment.name(), configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, cell.getDisplayMode(),
				cell.getConfigLabels().getLabels()).getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT).name());
	}

	// Vertical alignment
	@Test
	public void retreivedCellShouldHaveTopAlignment() {
		// Register vertical alignment
		final VerticalAlignmentEnum vAlignment = VerticalAlignmentEnum.TOP;
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlignment);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);

		// Check cell vertical alignment
		ILayerCell cell = natTable.getCellByPosition(2, 3);
		Assert.assertEquals(vAlignment.name(), configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, cell.getDisplayMode(),
				cell.getConfigLabels().getLabels()).getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT).name());
	}

	@Test
	public void retrievedCellShouldBeConvertedUsingTheDisplayConverter() throws Exception {
		IConfigRegistry configRegistry = new ConfigRegistry();
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, PricingTypeBean.getDisplayConverter());

		NatTableFixture natTableFixture = new NatTableFixture(new DefaultGridLayer(
												RowDataListFixture.getList(),
												RowDataListFixture.getPropertyNames(),
												RowDataListFixture.getPropertyToLabelMap()), false);
		natTableFixture.setConfigRegistry(configRegistry);
		natTableFixture.configure();

		int columnIndex = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.PRICING_TYPE_PROP_NAME);
		Object dataValue = natTableFixture.getDataValueByPosition(columnIndex + ROW_HEADER_COLUMN_COUNT, 2);

		// Verify displayed value
		ILayerCell cell = natTableFixture.getCellByPosition(columnIndex + ROW_HEADER_COLUMN_COUNT, 2);
		TextPainter cellPainter = new TextPainter();
		Assert.assertEquals("Automatic", cellPainter.convertDataType(cell, configRegistry));

		// Assert that the display value is converted to an Object
		Assert.assertTrue(dataValue instanceof PricingTypeBean);
	}

}
