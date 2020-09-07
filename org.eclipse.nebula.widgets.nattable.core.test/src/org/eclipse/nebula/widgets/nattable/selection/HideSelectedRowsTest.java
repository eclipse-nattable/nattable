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
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class HideSelectedRowsTest {

    private SelectionLayer selectionLayer;
    private RowHideShowLayer rowHideShowLayer;

    @Before
    public void setUp() {
        this.rowHideShowLayer = new RowHideShowLayer(new DataLayerFixture());
        this.selectionLayer = new SelectionLayer(this.rowHideShowLayer);
    }

    @Test
    public void shouldAlsoHideRowWhichIsNotSelectedButHasAMouseOverIt() {
        this.selectionLayer.doCommand(new MultiRowHideCommand(this.selectionLayer, 2));
        assertTrue(this.rowHideShowLayer.isRowIndexHidden(2));
    }

    @Test
    public void shouldHideRowForSelectedCell() {
        // Select cell in row we want to hide
        this.selectionLayer.setSelectedCell(3, 0);

        // Hide selection
        this.selectionLayer.doCommand(new RowHideCommand(this.selectionLayer, 3));

        // The previously selected row should be hidden
        assertTrue(this.rowHideShowLayer.isRowIndexHidden(3));
        assertEquals(6, this.selectionLayer.getRowCount());
    }

    @Test
    public void shouldHideSelectedRow() {
        // Select row to hide
        new SelectRowCommandHandler(this.selectionLayer).selectRows(0, new int[] { 2 }, false, false, 2);

        // Hide row
        this.selectionLayer.doCommand(new MultiRowHideCommand(this.selectionLayer, 2));

        // The previously selected row should be hidden
        assertTrue(this.rowHideShowLayer.isRowIndexHidden(2));
        assertEquals(6, this.selectionLayer.getRowCount());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldHideSelectedRowViaCollection() {
        // Select row to hide
        new SelectRowCommandHandler(this.selectionLayer).selectRows(0,
                Arrays.asList(Integer.valueOf(2)), false, false, 2);

        // Hide row
        this.selectionLayer.doCommand(new MultiRowHideCommand(this.selectionLayer, 2));

        // The previously selected row should be hidden
        assertTrue(this.rowHideShowLayer.isRowIndexHidden(2));
        assertEquals(6, this.selectionLayer.getRowCount());
    }

    @Test
    public void shouldHideAllSelectedRows() {
        // Select cells and rows
        new SelectRowCommandHandler(this.selectionLayer).selectRows(0, new int[] { 2 }, false, false, 2);
        this.selectionLayer.selectCell(0, 1, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);

        // Hide selection
        this.selectionLayer.doCommand(new MultiRowHideCommand(this.selectionLayer, 2, 0, 4));

        // Previously selected rows should be hidden
        assertTrue(this.rowHideShowLayer.isRowIndexHidden(2));
        assertTrue(this.rowHideShowLayer.isRowIndexHidden(0));
        assertTrue(this.rowHideShowLayer.isRowIndexHidden(4));
    }
}
