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
package org.eclipse.nebula.widgets.nattable.style;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StyleInheritanceTest {
	
	private static final FontData FONT_DATA = new FontData("Arial", 10, SWT.BOLD | SWT.ITALIC);

	private NatTable natTable;
	private IStyle evenCellStyle;
	private IStyle oddCellStyle;
	private Style superCellStyle;
	private ConfigRegistry configRegistry;
	private Color defaultBackgroundColor;
	private final Font font = GUIHelper.getFont(FONT_DATA);
	
	@Before
	public void setUp() throws Exception {
		natTable = new NatTableFixture();
		superCellStyle = new Style();
		superCellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
		defaultBackgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		superCellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, defaultBackgroundColor);
		superCellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.TOP);
		
		configRegistry = (ConfigRegistry) natTable.getConfigRegistry();
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, superCellStyle);
		
		// Setup even row style
		evenCellStyle = new Style() {{
				setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,  Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
				setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,  Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		}};
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, evenCellStyle, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
		
		// Setup odd row style
		oddCellStyle = new Style() {{
			setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,  Display.getDefault().getSystemColor(SWT.COLOR_RED));
		}};
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, oddCellStyle, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
	}
	
	@Test
	public void shouldFallBackToSuperTypeAttributesForEvenCell() {
		ILayerCell cell = natTable.getCellByPosition(2, 2);
		
		// Test cell even attributes
		final IStyle cellInstanceStyle = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		Assert.assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_BLACK), cellInstanceStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		Assert.assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_GRAY), cellInstanceStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		
		// Test super cell attributes
		StyleProxy cellProxy = new CellStyleProxy(configRegistry, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		final Font fontAttribute = cellProxy.getAttributeValue(CellStyleAttributes.FONT);
		Assert.assertEquals(FONT_DATA.getName(), fontAttribute.getFontData()[0].getName());
		Assert.assertEquals(FONT_DATA.getStyle(), fontAttribute.getFontData()[0].getStyle());
	}
	
	@Test
	public void shouldFallBackToSuperTypeAttributesForOddCell() {
		ILayerCell cell = natTable.getCellByPosition(2, 3);
		
		// Test cell odd attributes
		final IStyle cellInstanceStyle = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		Assert.assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_RED), cellInstanceStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		
		// Test super odd attributes
		StyleProxy cellProxy = new CellStyleProxy(configRegistry, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		final Color fontAttributeValue = cellProxy.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		Assert.assertEquals(defaultBackgroundColor, fontAttributeValue);
	}
}
