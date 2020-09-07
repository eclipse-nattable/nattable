/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 455949
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tickupdate.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.TickUpdateHandlerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.tickupdate.TickUpdateConfigAttributes;
import org.junit.Before;
import org.junit.Test;

public class TickUpdateCommandHandlerTest {

    private SelectionLayer selectionLayer;
    private ConfigRegistry testConfigRegistry;
    private TickUpdateCommandHandler commandHandler;
    private ColumnOverrideLabelAccumulator columnLabelAccumulator;

    @Before
    public void setup() {
        DataLayerFixture bodyDataLayer = new DataLayerFixture();
        this.selectionLayer = new SelectionLayer(bodyDataLayer);
        this.selectionLayer.setSelectedCell(1, 1);

        this.testConfigRegistry = new ConfigRegistry();
        this.testConfigRegistry.registerConfigAttribute(
                TickUpdateConfigAttributes.UPDATE_HANDLER,
                new TickUpdateHandlerFixture());
        this.testConfigRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.ALWAYS_EDITABLE);

        registerCellStyleAccumulators(bodyDataLayer);
        this.commandHandler = new TickUpdateCommandHandler(this.selectionLayer);
    }

    private void registerCellStyleAccumulators(DataLayer bodyDataLayer) {
        AggregateConfigLabelAccumulator aggregrateConfigLabelAccumulator = new AggregateConfigLabelAccumulator();
        this.columnLabelAccumulator = new ColumnOverrideLabelAccumulator(new DataLayerFixture());
        aggregrateConfigLabelAccumulator.add(this.columnLabelAccumulator, new AlternatingRowConfigLabelAccumulator());
        bodyDataLayer.setConfigLabelAccumulator(aggregrateConfigLabelAccumulator);
    }

    @Test
    public void shouldIncrementCellValue() throws Exception {
        assertEquals("[1, 1]", this.selectionLayer.getDataValueByPosition(1, 1));
        this.commandHandler.doCommand(new TickUpdateCommand(this.testConfigRegistry, true));
        assertEquals("[1, 1]up", this.selectionLayer.getDataValueByPosition(1, 1));
    }

    @Test
    public void shouldDecrementCellValue() throws Exception {
        assertEquals("[1, 1]", this.selectionLayer.getDataValueByPosition(1, 1));
        this.commandHandler.doCommand(new TickUpdateCommand(this.testConfigRegistry, false));
        assertEquals("[1, 1]down", this.selectionLayer.getDataValueByPosition(1, 1));
    }

    @Test
    public void shouldNotUpdateAnUneditableCell() throws Exception {
        this.testConfigRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.NEVER_EDITABLE);

        assertEquals("[1, 1]", this.selectionLayer.getDataValueByPosition(1, 1));

        // Increment
        this.commandHandler.doCommand(new TickUpdateCommand(this.testConfigRegistry, true));
        assertEquals("[1, 1]", this.selectionLayer.getDataValueByPosition(1, 1));

        // Decrement
        this.commandHandler.doCommand(new TickUpdateCommand(this.testConfigRegistry, false));
        assertEquals("[1, 1]", this.selectionLayer.getDataValueByPosition(1, 1));
    }

    @Test
    public void shouldUpdateMultipleCellsInSelection() throws Exception {
        this.selectionLayer.selectCell(1, 2, false, true);
        this.selectionLayer.selectCell(1, 5, false, true);

        assertEquals("[1, 1]", this.selectionLayer.getDataValueByPosition(1, 1));
        assertEquals("[1, 2]", this.selectionLayer.getDataValueByPosition(1, 2));
        assertEquals("[1, 5]", this.selectionLayer.getDataValueByPosition(1, 5));

        // Increment
        this.commandHandler.doCommand(new TickUpdateCommand(this.testConfigRegistry, true));

        assertEquals("[1, 1]up", this.selectionLayer.getDataValueByPosition(1, 1));
        assertEquals("[1, 2]up", this.selectionLayer.getDataValueByPosition(1, 2));
        assertEquals("[1, 3]", this.selectionLayer.getDataValueByPosition(1, 3));
        assertEquals("[1, 5]up", this.selectionLayer.getDataValueByPosition(1, 5));
    }

    @Test
    public void shouldUpdateOnlyIfAllCellsHaveTheSameEditor() throws Exception {
        this.columnLabelAccumulator.registerColumnOverrides(1, "COMBO_BOX_EDITOR_LABEL");
        this.columnLabelAccumulator.registerColumnOverrides(2, "TEXT_BOX_EDITOR_LABEL");

        this.testConfigRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITOR,
                new ComboBoxCellEditor(Arrays.asList("")),
                DisplayMode.EDIT,
                "COMBO_BOX_EDITOR_LABEL");
        this.testConfigRegistry.registerConfigAttribute(
                EditConfigAttributes.CELL_EDITOR,
                new TextCellEditor(),
                DisplayMode.EDIT,
                "TEXT_BOX_EDITOR_LABEL");

        assertEquals("[1, 1]", this.selectionLayer.getDataValueByPosition(1, 1));
        assertEquals("[2, 1]", this.selectionLayer.getDataValueByPosition(2, 1));

        // Select cells in column 1 and 2
        this.selectionLayer.selectCell(1, 1, false, false);
        this.selectionLayer.selectCell(2, 1, false, true); // Select with Ctrl
                                                           // key

        // Increment
        this.commandHandler.doCommand(new TickUpdateCommand(this.testConfigRegistry, true));

        // Should not increment - editors different
        assertEquals("[1, 1]", this.selectionLayer.getDataValueByPosition(1, 1));
        assertEquals("[2, 1]", this.selectionLayer.getDataValueByPosition(2, 1));

        // Select cells in column 1 Only
        this.selectionLayer.selectCell(1, 1, false, false);
        this.selectionLayer.selectCell(1, 2, false, true); // Select with Ctrl
                                                           // key

        // Increment
        this.commandHandler.doCommand(new TickUpdateCommand(this.testConfigRegistry, true));

        // Should increment - same editor
        assertEquals("[1, 1]up", this.selectionLayer.getDataValueByPosition(1, 1));
        assertEquals("[1, 2]up", this.selectionLayer.getDataValueByPosition(1, 2));
    }

    @Test
    public void shouldNotThrowExceptionOnNoSelection() {
        // if no exception occurs this test succeeds
        // we also check that no update has been triggered
        this.selectionLayer.clear();

        this.selectionLayer.addLayerListener(new ILayerListener() {

            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof CellVisualChangeEvent) {
                    fail("update triggered");
                }
            }
        });
        this.commandHandler.doCommand(new TickUpdateCommand(this.testConfigRegistry, true));
    }
}
