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
package org.eclipse.nebula.widgets.nattable.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.columnRename.RenameColumnHeaderCommand;
import org.eclipse.nebula.widgets.nattable.columnRename.event.RenameColumnHeaderEvent;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyModifiableBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnInsertEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RenameColumnIntegrationTest {

    private static final String TEST_COLUMN_NAME = "Test column name";

    DummyModifiableBodyDataProvider provider = new DummyModifiableBodyDataProvider(20, 20);
    DummyGridLayerStack grid = new DummyGridLayerStack(this.provider);
    NatTableFixture natTableFixture;
    LayerListenerFixture listener;

    @BeforeEach
    public void setup() {
        this.natTableFixture = new NatTableFixture(this.grid);
        this.listener = new LayerListenerFixture();
        this.natTableFixture.addLayerListener(this.listener);
    }

    @Test
    public void shouldRenameColumnHeader() {
        String originalColumnHeader = this.natTableFixture.getDataValueByPosition(2, 0).toString();
        assertEquals("Column 2", originalColumnHeader);

        this.natTableFixture.doCommand(
                new RenameColumnHeaderCommand(
                        this.natTableFixture,
                        2,
                        TEST_COLUMN_NAME));
        String renamedColumnHeader = this.natTableFixture.getDataValueByPosition(2, 0).toString();
        assertEquals(TEST_COLUMN_NAME, renamedColumnHeader);

        assertEquals(1, this.listener.getEventsCount());
        RenameColumnHeaderEvent event = (RenameColumnHeaderEvent) this.listener.getReceivedEvent(RenameColumnHeaderEvent.class);
        assertEquals(new Range(2, 3), event.getColumnPositionRanges().iterator().next());
    }

    @Test
    public void shouldRenameColumnHeaderForReorderedColumn() {
        String originalColumnHeader = this.natTableFixture.getDataValueByPosition(2, 0).toString();
        assertEquals("Column 2", originalColumnHeader);

        this.natTableFixture.doCommand(new ColumnReorderCommand(this.natTableFixture, 1, 5));

        originalColumnHeader = this.natTableFixture.getDataValueByPosition(2, 0).toString();
        assertEquals("Column 3", originalColumnHeader);

        this.natTableFixture.doCommand(
                new RenameColumnHeaderCommand(
                        this.natTableFixture,
                        2,
                        TEST_COLUMN_NAME));
        String renamedColumnHeader = this.natTableFixture.getDataValueByPosition(2, 0).toString();
        assertEquals(TEST_COLUMN_NAME, renamedColumnHeader);

        assertEquals(2, this.listener.getEventsCount());
        RenameColumnHeaderEvent event = (RenameColumnHeaderEvent) this.listener.getReceivedEvent(RenameColumnHeaderEvent.class);
        assertEquals(new Range(2, 3), event.getColumnPositionRanges().iterator().next());
    }

    @Test
    public void shouldRenameColumnHeaderForReorderedColumnProgrammatically() {
        String originalColumnHeader = this.natTableFixture.getDataValueByPosition(2, 0).toString();
        assertEquals("Column 2", originalColumnHeader);

        this.natTableFixture.doCommand(new ColumnReorderCommand(this.natTableFixture, 1, 5));

        originalColumnHeader = this.natTableFixture.getDataValueByPosition(2, 0).toString();
        assertEquals("Column 3", originalColumnHeader);

        this.grid.getColumnHeaderLayer().renameColumnIndex(2, TEST_COLUMN_NAME);
        String renamedColumnHeader = this.natTableFixture.getDataValueByPosition(2, 0).toString();
        assertEquals(TEST_COLUMN_NAME, renamedColumnHeader);

        assertEquals(2, this.listener.getEventsCount());
        RenameColumnHeaderEvent event = (RenameColumnHeaderEvent) this.listener.getReceivedEvent(RenameColumnHeaderEvent.class);
        assertEquals(new Range(2, 3), event.getColumnPositionRanges().iterator().next());
    }

    @Test
    public void shouldUpdateRenamedColumnOnDeleteOneColumn() {
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

        this.natTableFixture.doCommand(
                new RenameColumnHeaderCommand(
                        this.natTableFixture,
                        5,
                        TEST_COLUMN_NAME));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(5, 0).toString());

        // simulate deletion of a column
        this.provider.setColumnCount(this.provider.getColumnCount() - 1);
        this.grid.getColumnHeaderDataLayer().fireLayerEvent(new ColumnDeleteEvent(this.grid.getBodyDataLayer(), 0));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(4, 0).toString());
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

    }

    @Test
    public void shouldUpdateRenamedColumnOnDeleteMultipleColumn() {
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

        this.natTableFixture.doCommand(
                new RenameColumnHeaderCommand(
                        this.natTableFixture,
                        5,
                        TEST_COLUMN_NAME));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(5, 0).toString());

        // simulate deletion of a column
        this.provider.setColumnCount(this.provider.getColumnCount() - 3);
        this.grid.getColumnHeaderDataLayer().fireLayerEvent(new ColumnDeleteEvent(this.grid.getBodyDataLayer(), new Range(1, 4)));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(2, 0).toString());
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

    }

    @Test
    public void shouldUpdateRenamedColumnOnDeleteMultipleColumnRanges() {
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

        this.natTableFixture.doCommand(
                new RenameColumnHeaderCommand(
                        this.natTableFixture,
                        5,
                        TEST_COLUMN_NAME));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(5, 0).toString());

        // simulate deletion of a column
        this.provider.setColumnCount(this.provider.getColumnCount() - 3);
        this.grid.getColumnHeaderDataLayer().fireLayerEvent(
                new ColumnDeleteEvent(this.grid.getBodyDataLayer(), new Range(1, 3), new Range(6, 7)));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(3, 0).toString());
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

    }

    @Test
    public void shouldUpdateRenamedColumnOnAddingOneColumn() {
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

        this.natTableFixture.doCommand(
                new RenameColumnHeaderCommand(
                        this.natTableFixture,
                        5,
                        TEST_COLUMN_NAME));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(5, 0).toString());

        // simulate deletion of a column
        this.provider.setColumnCount(this.provider.getColumnCount() + 1);
        this.grid.getColumnHeaderDataLayer().fireLayerEvent(new ColumnInsertEvent(this.grid.getBodyDataLayer(), 0));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(6, 0).toString());
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

    }

    @Test
    public void shouldUpdateRenamedColumnOnAddingMultipleColumn() {
        this.natTableFixture.doCommand(new TurnViewportOffCommand());

        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

        this.natTableFixture.doCommand(
                new RenameColumnHeaderCommand(
                        this.natTableFixture,
                        5,
                        TEST_COLUMN_NAME));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(5, 0).toString());

        // simulate deletion of a column
        this.provider.setColumnCount(this.provider.getColumnCount() + 3);
        this.grid.getColumnHeaderDataLayer().fireLayerEvent(new ColumnInsertEvent(this.grid.getBodyDataLayer(), new Range(1, 4)));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(8, 0).toString());
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

    }

    @Test
    public void shouldUpdateRenamedColumnOnAddingMultipleColumnRanges() {
        this.natTableFixture.doCommand(new TurnViewportOffCommand());

        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

        this.natTableFixture.doCommand(
                new RenameColumnHeaderCommand(
                        this.natTableFixture,
                        5,
                        TEST_COLUMN_NAME));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(5, 0).toString());

        // simulate deletion of a column
        this.provider.setColumnCount(this.provider.getColumnCount() + 3);
        this.grid.getColumnHeaderDataLayer().fireLayerEvent(
                new ColumnInsertEvent(this.grid.getBodyDataLayer(), new Range(1, 3), new Range(7, 8)));

        assertEquals(TEST_COLUMN_NAME, this.natTableFixture.getDataValueByPosition(7, 0).toString());
        assertEquals("Column 5", this.natTableFixture.getDataValueByPosition(5, 0).toString());

    }
}
