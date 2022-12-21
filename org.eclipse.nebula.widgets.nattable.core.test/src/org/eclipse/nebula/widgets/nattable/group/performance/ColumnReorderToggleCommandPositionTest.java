/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.Test;

public class ColumnReorderToggleCommandPositionTest {

    // 5 columns 7 rows
    DataLayerFixture layer = new DataLayerFixture();

    @Test
    public void shouldToggleColumnReorderEndCommandCoordinateToLeftEdge() {
        ColumnReorderEndCommand command = new ColumnReorderEndCommand(this.layer, 2);
        assertEquals(2, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldToggleColumnReorderEndCommandCoordinateToRightEdge() {
        ColumnReorderEndCommand command = new ColumnReorderEndCommand(this.layer, 2);
        assertEquals(2, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(2, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldNotToggleColumnReorderEndCommandCoordinateToLeftEdgeOnLastColumn() {
        ColumnReorderEndCommand command = new ColumnReorderEndCommand(this.layer, 5);
        assertEquals(4, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(4, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldNotToggleColumnReorderEndCommandCoordinateToRightEdgeOnFirstColumn() {
        ColumnReorderEndCommand command = new ColumnReorderEndCommand(this.layer, 0);
        assertEquals(0, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(0, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldToggleColumnReorderCommandCoordinateToLeftEdge() {
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 0, 2);
        assertEquals(2, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldToggleColumnReorderCommandCoordinateToRightEdge() {
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 0, 2);
        assertEquals(2, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(2, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldNotToggleColumnReorderCommandCoordinateToLeftEdgeOnLastColumn() {
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 2, 5);
        assertEquals(4, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(4, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldNotToggleColumnReorderCommandCoordinateToRightEdgeOnFirstColumn() {
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 2, 0);
        assertEquals(0, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(0, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldToggleMultiColumnReorderCommandCoordinateToLeftEdge() {
        MultiColumnReorderCommand command = new MultiColumnReorderCommand(this.layer, Arrays.asList(0), 2);
        assertEquals(2, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldToggleMultiColumnReorderCommandCoordinateToRightEdge() {
        MultiColumnReorderCommand command = new MultiColumnReorderCommand(this.layer, Arrays.asList(0), 2);
        assertEquals(2, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(2, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldNotToggleMultiColumnReorderCommandCoordinateToLeftEdgeOnLastColumn() {
        MultiColumnReorderCommand command = new MultiColumnReorderCommand(this.layer, Arrays.asList(2), 5);
        assertEquals(4, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(4, command.getToColumnPosition());
        assertFalse(command.isReorderToLeftEdge());
    }

    @Test
    public void shouldNotToggleMultiColumnReorderCommandCoordinateToRightEdgeOnFirstColumn() {
        MultiColumnReorderCommand command = new MultiColumnReorderCommand(this.layer, Arrays.asList(2), 0);
        assertEquals(0, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());

        command.toggleCoordinateByEdge();
        assertEquals(0, command.getToColumnPosition());
        assertTrue(command.isReorderToLeftEdge());
    }
}
