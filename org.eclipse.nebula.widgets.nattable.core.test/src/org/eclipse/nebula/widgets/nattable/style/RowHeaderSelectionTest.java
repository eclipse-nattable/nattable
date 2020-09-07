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
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommand;
import org.junit.Before;
import org.junit.Test;

public class RowHeaderSelectionTest {

    private GridLayerFixture gridLayer;

    @Before
    public void setUp() {
        this.gridLayer = new GridLayerFixture();
        this.gridLayer.doCommand(new InitializeClientAreaCommandFixture());
    }

    @Test
    public void willSelectBodyCellAndShouldHaveColumnHeaderSelected() {
        // Select body cell
        // The row position is a grid layer position
        this.gridLayer.doCommand(new SelectCellCommand(this.gridLayer, 2, 2, false, false));

        // Get row header cell corresponding to the selected body cell
        RowHeaderLayer rowHeaderLayer = this.gridLayer.getRowHeaderLayer();
        // The column position is 0 because it takes into account the offset of
        // the row header
        ILayerCell cell = rowHeaderLayer.getCellByPosition(0, 1);

        // Assert the cell is in selected state
        assertEquals(DisplayMode.SELECT, cell.getDisplayMode());
    }

    @Test
    public void shouldReturnFullySelectedStyle() {
        // Select full column
        this.gridLayer.doCommand(new ViewportSelectRowCommand(this.gridLayer, 1, false, false));

        RowHeaderLayer rowHeaderLayer = this.gridLayer.getRowHeaderLayer();

        // Since I selected using grid coordinates, the column position should
        // be 1 rather than 2
        int rowPosition = this.gridLayer.localToUnderlyingRowPosition(1);
        LabelStack labelStack = rowHeaderLayer.getConfigLabelsByPosition(0, rowPosition);
        assertTrue(labelStack.hasLabel(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));

        rowPosition = this.gridLayer.localToUnderlyingRowPosition(4);
        labelStack = rowHeaderLayer.getConfigLabelsByPosition(0, rowPosition);
        assertFalse("Should not have returned fully selected style.", labelStack.hasLabel(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
    }
}
