/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CellSelectionTest {
    private GridLayerFixture gridLayer;

    @BeforeEach
    public void setUp() {
        this.gridLayer = new GridLayerFixture();
        this.gridLayer.doCommand(new InitializeClientAreaCommandFixture());
    }

    @Test
    public void willSelectBodyCellAndShouldHaveColumnHeaderSelected() {
        // Select body cell
        // The cell position is a grid layer position
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 2, 2, false, false));

        // Get body layer cell corresponding to the selected body cell
        ILayer bodyLayer = this.gridLayer.getBodyLayer();
        // The column position is 1 because it takes into account the offset of
        // the row header
        ILayerCell cell = bodyLayer.getCellByPosition(1, 1);

        // Assert the cell is in selected state
        assertEquals(DisplayMode.SELECT, cell.getDisplayMode());
    }
}
