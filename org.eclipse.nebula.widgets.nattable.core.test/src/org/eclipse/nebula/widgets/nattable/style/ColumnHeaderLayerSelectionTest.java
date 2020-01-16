/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommand;
import org.junit.Before;
import org.junit.Test;

public class ColumnHeaderLayerSelectionTest {

    private GridLayerFixture gridLayer;

    @Before
    public void setUp() {
        this.gridLayer = new GridLayerFixture();
        this.gridLayer.doCommand(new InitializeClientAreaCommandFixture());
    }

    @Test
    public void willSelectBodyCellAndShouldHaveColumnHeaderSelected() {
        // Select body cell
        // The column position is a grid layer position
        this.gridLayer.doCommand(new SelectCellCommand(this.gridLayer, 2, 2, false, false));

        // Get column header cell corresponding to the selected body cell
        ColumnHeaderLayer columnHeaderLayer = this.gridLayer.getColumnHeaderLayer();
        // The column position is 1 because it takes into account the offset of
        // the row header
        ILayerCell cell = columnHeaderLayer.getCellByPosition(1, 0);

        // Assert the cell is in selected state
        assertEquals(DisplayMode.SELECT, cell.getDisplayMode());
    }

    @Test
    public void shouldReturnFullySelectedStyle() {
        // Select full column
        this.gridLayer.doCommand(new ViewportSelectColumnCommand(this.gridLayer, 2, false, false));

        ColumnHeaderLayer columnHeaderLayer = this.gridLayer.getColumnHeaderLayer();

        // Since I selected using grid coordinates, the column position should
        // be 1 rather than 2
        int columnPosition = this.gridLayer.localToUnderlyingColumnPosition(2);
        LabelStack labelStack = columnHeaderLayer.getConfigLabelsByPosition(columnPosition, 0);

        assertTrue(labelStack.hasLabel(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));

        columnPosition = this.gridLayer.localToUnderlyingColumnPosition(3);
        labelStack = columnHeaderLayer.getConfigLabelsByPosition(columnPosition, 0);
        assertFalse(labelStack.hasLabel(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
    }

    @Test
    public void shouldReturnAdditionalLabels() {
        ColumnHeaderLayer columnHeaderLayer = this.gridLayer.getColumnHeaderLayer();

        columnHeaderLayer.setConfigLabelAccumulator(new IConfigLabelProvider() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                if (columnPosition == 2) {
                    configLabels.addLabel("test");
                }
            }

            @Override
            public Collection<String> getProvidedLabels() {
                Set<String> result = new HashSet<>();
                result.add("test");
                return result;
            }
        });

        LabelStack labelStack = this.gridLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(2, labelStack.getLabels().size());
        assertTrue(labelStack.hasLabel("test"));
        assertTrue(labelStack.hasLabel(GridRegion.COLUMN_HEADER));
    }

    @Test
    public void shouldReturnProvidedLabels() {
        ColumnHeaderLayer columnHeaderLayer = this.gridLayer.getColumnHeaderLayer();

        columnHeaderLayer.setConfigLabelAccumulator(new IConfigLabelProvider() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                if (columnPosition == 2) {
                    configLabels.addLabel("test");
                }
            }

            @Override
            public Collection<String> getProvidedLabels() {
                Set<String> result = new HashSet<String>();
                result.add("test");
                return result;
            }
        });

        Collection<String> labels = columnHeaderLayer.getProvidedLabels();
        assertEquals(1, labels.size());
        assertEquals("test", labels.iterator().next());
    }
}
