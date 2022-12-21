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

import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.Test;

public class RowReorderToggleCommandPositionTest {

    // 5 columns 7 rows
    DataLayerFixture layer = new DataLayerFixture();

    @Test
    public void shouldToggleRowReorderEndCommandCoordinateToTopEdge() {
        RowReorderEndCommand command = new RowReorderEndCommand(this.layer, 2);
        assertEquals(2, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());
    }

    @Test
    public void shouldToggleRowReorderEndCommandCoordinateToBottomEdge() {
        RowReorderEndCommand command = new RowReorderEndCommand(this.layer, 2);
        assertEquals(2, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(2, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());
    }

    @Test
    public void shouldNotToggleRowReorderEndCommandCoordinateToTopEdgeOnLastRow() {
        RowReorderEndCommand command = new RowReorderEndCommand(this.layer, 7);
        assertEquals(6, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(6, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());
    }

    @Test
    public void shouldNotToggleRowReorderEndCommandCoordinateToBottomEdgeOnFirstRow() {
        RowReorderEndCommand command = new RowReorderEndCommand(this.layer, 0);
        assertEquals(0, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(0, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());
    }

    @Test
    public void shouldToggleRowReorderCommandCoordinateToTopEdge() {
        RowReorderCommand command = new RowReorderCommand(this.layer, 0, 2);
        assertEquals(2, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());
    }

    @Test
    public void shouldToggleRowReorderCommandCoordinateToBottomEdge() {
        RowReorderCommand command = new RowReorderCommand(this.layer, 0, 2);
        assertEquals(2, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(2, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());
    }

    @Test
    public void shouldNotToggleRowReorderCommandCoordinateToTopEdgeOnLastRow() {
        RowReorderCommand command = new RowReorderCommand(this.layer, 2, 7);
        assertEquals(6, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(6, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());
    }

    @Test
    public void shouldNotToggleRowReorderCommandCoordinateToBottomEdgeOnFirstRow() {
        RowReorderCommand command = new RowReorderCommand(this.layer, 2, 0);
        assertEquals(0, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(0, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());
    }

    @Test
    public void shouldToggleMultiRowReorderCommandCoordinateToTopEdge() {
        MultiRowReorderCommand command = new MultiRowReorderCommand(this.layer, Arrays.asList(0), 2);
        assertEquals(2, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());
    }

    @Test
    public void shouldToggleMultiRowReorderCommandCoordinateToBottomEdge() {
        MultiRowReorderCommand command = new MultiRowReorderCommand(this.layer, Arrays.asList(0), 2);
        assertEquals(2, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(1, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(2, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());
    }

    @Test
    public void shouldNotToggleMultiRowReorderCommandCoordinateToTopEdgeOnLastRow() {
        MultiRowReorderCommand command = new MultiRowReorderCommand(this.layer, Arrays.asList(2), 7);
        assertEquals(6, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(6, command.getToRowPosition());
        assertFalse(command.isReorderToTopEdge());
    }

    @Test
    public void shouldNotToggleMultiRowReorderCommandCoordinateToBottomEdgeOnFirstRow() {
        MultiRowReorderCommand command = new MultiRowReorderCommand(this.layer, Arrays.asList(2), 0);
        assertEquals(0, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());

        command.toggleCoordinateByEdge();
        assertEquals(0, command.getToRowPosition());
        assertTrue(command.isReorderToTopEdge());
    }
}
