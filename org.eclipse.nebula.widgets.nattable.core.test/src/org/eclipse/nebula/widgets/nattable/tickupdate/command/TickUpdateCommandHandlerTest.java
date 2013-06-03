/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tickupdate.command;

import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.EDIT;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;


import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.TickUpdateHandlerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.tickupdate.TickUpdateConfigAttributes;
import org.eclipse.nebula.widgets.nattable.tickupdate.command.TickUpdateCommand;
import org.eclipse.nebula.widgets.nattable.tickupdate.command.TickUpdateCommandHandler;
import org.junit.Before;
import org.junit.Test;

public class TickUpdateCommandHandlerTest {

	private SelectionLayer selectionLayer;
	private ConfigRegistry testConfigRegistry;
	private TickUpdateCommandHandler commandHandler;
	private ColumnOverrideLabelAccumulator columnLabelAccumulator;

	@Before
	public void setup(){
		DataLayerFixture bodyDataLayer = new DataLayerFixture();
		selectionLayer = new SelectionLayer(bodyDataLayer);
		selectionLayer.setSelectedCell(1, 1);
		
		testConfigRegistry = new ConfigRegistry();
		testConfigRegistry.registerConfigAttribute(TickUpdateConfigAttributes.UPDATE_HANDLER,	
				new TickUpdateHandlerFixture());
		testConfigRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, 
				IEditableRule.ALWAYS_EDITABLE);

		registerCellStyleAccumulators(bodyDataLayer);
		commandHandler = new TickUpdateCommandHandler(selectionLayer);
	}
	
	private void registerCellStyleAccumulators(DataLayer bodyDataLayer) {
		AggregrateConfigLabelAccumulator aggregrateConfigLabelAccumulator = new AggregrateConfigLabelAccumulator();
		columnLabelAccumulator = new ColumnOverrideLabelAccumulator(new DataLayerFixture());
		aggregrateConfigLabelAccumulator.add(columnLabelAccumulator, new AlternatingRowConfigLabelAccumulator());
		bodyDataLayer.setConfigLabelAccumulator(aggregrateConfigLabelAccumulator);
	}

	@Test
	public void shouldIncrementCellValue() throws Exception {
		assertEquals("[1, 1]", selectionLayer.getDataValueByPosition(1, 1));
		commandHandler.doCommand(new TickUpdateCommand(testConfigRegistry, true));
		assertEquals("[1, 1]up", selectionLayer.getDataValueByPosition(1, 1));
	}

	@Test
	public void shouldDecrementCellValue() throws Exception {
		assertEquals("[1, 1]", selectionLayer.getDataValueByPosition(1, 1));
		commandHandler.doCommand(new TickUpdateCommand(testConfigRegistry, false));
		assertEquals("[1, 1]down", selectionLayer.getDataValueByPosition(1, 1));
	}

	@Test
	public void shouldNotUpdateAnUneditableCell() throws Exception {
		testConfigRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE);

		assertEquals("[1, 1]", selectionLayer.getDataValueByPosition(1, 1));

		// Increment
		commandHandler.doCommand(new TickUpdateCommand(testConfigRegistry, true));
		assertEquals("[1, 1]", selectionLayer.getDataValueByPosition(1, 1));

		// Decrement
		commandHandler.doCommand(new TickUpdateCommand(testConfigRegistry, false));
		assertEquals("[1, 1]", selectionLayer.getDataValueByPosition(1, 1));
	}
	
	@Test
	public void shouldUpdateMultipleCellsInSelection() throws Exception {
		selectionLayer.selectCell(1, 2, false, true);
		selectionLayer.selectCell(1, 5, false, true);
		
		assertEquals("[1, 1]", selectionLayer.getDataValueByPosition(1, 1));
		assertEquals("[1, 2]", selectionLayer.getDataValueByPosition(1, 2));
		assertEquals("[1, 5]", selectionLayer.getDataValueByPosition(1, 5));

		// Increment
		commandHandler.doCommand(new TickUpdateCommand(testConfigRegistry, true));
		
		assertEquals("[1, 1]up", selectionLayer.getDataValueByPosition(1, 1));
		assertEquals("[1, 2]up", selectionLayer.getDataValueByPosition(1, 2));
		assertEquals("[1, 3]", selectionLayer.getDataValueByPosition(1, 3));
		assertEquals("[1, 5]up", selectionLayer.getDataValueByPosition(1, 5));
	}
	
	@Test
	public void shouldUpdateOnlyIfAllCellsHaveTheSameEditor() throws Exception {
		columnLabelAccumulator.registerColumnOverrides(1, "COMBO_BOX_EDITOR_LABEL");
		columnLabelAccumulator.registerColumnOverrides(2, "TEXT_BOX_EDITOR_LABEL");
		
		testConfigRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new ComboBoxCellEditor(Arrays.asList("")), EDIT, "COMBO_BOX_EDITOR_LABEL");
		testConfigRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new TextCellEditor(), EDIT, "TEXT_BOX_EDITOR_LABEL");
		
		assertEquals("[1, 1]", selectionLayer.getDataValueByPosition(1, 1));
		assertEquals("[2, 1]", selectionLayer.getDataValueByPosition(2, 1));

		// Select cells in column 1 and 2
		selectionLayer.selectCell(1, 1, false, false);
		selectionLayer.selectCell(2, 1, false, true);	//Select with Ctrl key
		
		// Increment
		commandHandler.doCommand(new TickUpdateCommand(testConfigRegistry, true));
		
		// Should not increment - editors different
		assertEquals("[1, 1]", selectionLayer.getDataValueByPosition(1, 1));
		assertEquals("[2, 1]", selectionLayer.getDataValueByPosition(2, 1));
		
		// Select cells in column 1 Only
		selectionLayer.selectCell(1, 1, false, false);
		selectionLayer.selectCell(1, 2, false, true);	//Select with Ctrl key
		
		// Increment
		commandHandler.doCommand(new TickUpdateCommand(testConfigRegistry, true));

		// Should increment - same editor
		assertEquals("[1, 1]up", selectionLayer.getDataValueByPosition(1, 1));
		assertEquals("[1, 2]up", selectionLayer.getDataValueByPosition(1, 2));
	}
	
}
