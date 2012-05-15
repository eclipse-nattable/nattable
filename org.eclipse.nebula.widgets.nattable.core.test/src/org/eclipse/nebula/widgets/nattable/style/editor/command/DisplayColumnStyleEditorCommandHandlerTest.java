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
package org.eclipse.nebula.widgets.nattable.style.editor.command;

import static org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes.CELL_STYLE;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.NORMAL;
import static org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler.USER_EDITED_STYLE_LABEL;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;


import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.editor.ColumnStyleEditorDialog;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommand;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler;
import org.eclipse.nebula.widgets.nattable.test.fixture.CellStyleFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.PropertiesFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class DisplayColumnStyleEditorCommandHandlerTest {

	private ColumnOverrideLabelAccumulator labelAccumulatorFixture;
	private NatTableFixture natTableFixture;
	private DisplayColumnStyleEditorCommand commandFixture;
	private DisplayColumnStyleEditorCommandHandler handlerUnderTest;
	private IConfigRegistry configRegistryFixture;
	
	@Before
	public void setup() {
		labelAccumulatorFixture = new ColumnOverrideLabelAccumulator(new DataLayerFixture());
		natTableFixture = new NatTableFixture();
		configRegistryFixture = natTableFixture.getConfigRegistry();
		commandFixture = new DisplayColumnStyleEditorCommand(natTableFixture, natTableFixture.getConfigRegistry(), 1, 1);

		final SelectionLayer selectionLayer = ((DummyGridLayerStack)natTableFixture.getLayer()).getBodyLayer().getSelectionLayer(); 
		handlerUnderTest = new DisplayColumnStyleEditorCommandHandler(selectionLayer, labelAccumulatorFixture, configRegistryFixture);
	}

	@Test
	public void doCommand() throws Exception {
		handlerUnderTest.dialog = new ColumnStyleEditorDialog(new Shell(), new CellStyleFixture());
		handlerUnderTest.applySelectedStyleToColumns(commandFixture, new int[]{0});

		Style selectedStyle = (Style) configRegistryFixture.getConfigAttribute(CELL_STYLE, NORMAL, handlerUnderTest.getConfigLabel(0));

		assertEquals(CellStyleFixture.TEST_BG_COLOR, selectedStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		assertEquals(CellStyleFixture.TEST_FG_COLOR, selectedStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));

		List<String> columnLableOverrides = handlerUnderTest.columnLabelAccumulator.getOverrides(Integer.valueOf(0));
		assertEquals(1, columnLableOverrides.size());
		assertEquals(USER_EDITED_STYLE_LABEL + "0", columnLableOverrides.get(0));
	}

	@Test
	public void parseColumnIndexFromKey() throws Exception {
		int i = handlerUnderTest.parseColumnIndexFromKey(".BODY.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_3.horizontalAlignment");
		assertEquals(3, i);

		i = handlerUnderTest.parseColumnIndexFromKey(".BODY.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_12.horizontalAlignment");
		assertEquals(12, i);
	}

	@Test
	public void saveStateForMultipleLabels() throws Exception {
		CellStyleFixture style1 = new CellStyleFixture(HorizontalAlignmentEnum.LEFT);
		CellStyleFixture style2 = new CellStyleFixture(HorizontalAlignmentEnum.RIGHT);

		handlerUnderTest.stylesToPersist.put("label1", style1);
		handlerUnderTest.stylesToPersist.put("label2", style2);
		
		PropertiesFixture propertiesFixture = new PropertiesFixture();
		handlerUnderTest.saveState("prefix", propertiesFixture);
		
		assertEquals(HorizontalAlignmentEnum.LEFT.name(), propertiesFixture.getProperty("prefix.userDefinedColumnStyle.label1.style.horizontalAlignment"));
		assertEquals(HorizontalAlignmentEnum.RIGHT.name(), propertiesFixture.getProperty("prefix.userDefinedColumnStyle.label2.style.horizontalAlignment"));
	}
	
	@Test
	public void shouldRemoveLabelFromPersistenceIfStyleIsCleared() throws Exception {
		handlerUnderTest.dialog = new ColumnStyleEditorDialog(new Shell(), null);
		handlerUnderTest.applySelectedStyleToColumns(commandFixture, new int[]{0});

		Style selectedStyle = (Style) configRegistryFixture.getConfigAttribute(CELL_STYLE, NORMAL, handlerUnderTest.getConfigLabel(0));

		DefaultNatTableStyleConfiguration defaultStyle = new DefaultNatTableStyleConfiguration();
		assertEquals(defaultStyle.bgColor, selectedStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		assertEquals(defaultStyle.fgColor, selectedStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));

		Properties properties = new Properties();
		handlerUnderTest.saveState("prefix", properties);
		
		assertEquals(0, properties.size());
	}
	
	@Test
	public void loadStateForMultipleLabels() throws Exception {
		PropertiesFixture propertiesFixture = new PropertiesFixture()
			.addStyleProperties("prefix.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_0")
			.addStyleProperties("prefix.userDefinedColumnStyle.USER_EDITED_STYLE_FOR_INDEX_1");

		handlerUnderTest.loadState("prefix", propertiesFixture);
		
		Style style = (Style) configRegistryFixture.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL, "USER_EDITED_STYLE_FOR_INDEX_0");
		assertEquals(HorizontalAlignmentEnum.LEFT, style.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT));

		style = (Style) configRegistryFixture.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL, "USER_EDITED_STYLE_FOR_INDEX_1");
		assertEquals(VerticalAlignmentEnum.TOP, style.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT));
		
	}
}
