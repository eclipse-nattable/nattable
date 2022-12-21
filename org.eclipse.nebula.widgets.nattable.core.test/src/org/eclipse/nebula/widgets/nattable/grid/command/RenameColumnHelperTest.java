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
package org.eclipse.nebula.widgets.nattable.grid.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.columnRename.RenameColumnHelper;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHeaderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.integration.PersistenceIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RenameColumnHelperTest {

    private RenameColumnHelper helper;
    private Properties properties;

    private static String PREFIX = PersistenceIntegrationTest.TEST_PERSISTENCE_PREFIX;
    private static String KEY = RenameColumnHelper.PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS;

    @BeforeEach
    public void setup() {
        ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayerFixture();
        this.properties = new Properties();
        this.helper = new RenameColumnHelper(columnHeaderLayer);
    }

    @Test
    public void shouldRenameColumn() throws Exception {
        assertTrue(this.helper.renameColumnPosition(1, "one"));
        assertEquals("one", this.helper.getRenamedColumnLabel(1));

        // Invalid position
        assertFalse(this.helper.renameColumnPosition(-1, "badone"));
        assertEquals("one", this.helper.getRenamedColumnLabel(1));

        // new name is null
        assertTrue(this.helper.renameColumnPosition(1, null));
        assertEquals(null, this.helper.getRenamedColumnLabel(1));
    }

    @Test
    public void doNotSaveStateIfNoColumnAreRenamed() throws Exception {
        this.helper.saveState(PREFIX, this.properties);
        assertNull(this.properties.get(KEY));
    }

    @Test
    public void saveRenamedColumnInProperties() throws Exception {
        this.helper.renameColumnPosition(2, "Renamed 2");
        this.helper.saveState(PREFIX, this.properties);

        assertEquals("2:Renamed 2|", this.properties.get(PREFIX + KEY));

        this.helper.renameColumnPosition(1, "Renamed 1");
        this.helper.saveState(PREFIX, this.properties);

        assertEquals("1:Renamed 1|2:Renamed 2|", this.properties.get(PREFIX + KEY));
    }

    @Test
    public void loadStateWhenNoPrepertiesArePersisted() throws Exception {
        this.helper.loadState(PREFIX, this.properties);
        assertFalse(this.helper.isAnyColumnRenamed());
    }

    @Test
    public void loadStateFromProperties() throws Exception {
        this.properties.put(PREFIX + KEY, "2:Renamed 2|1:Renamed 1|");
        this.helper.loadState(PREFIX, this.properties);

        assertTrue(this.helper.isAnyColumnRenamed());
        assertEquals("Renamed 2", this.helper.getRenamedColumnLabel(2));
        assertEquals("Renamed 1", this.helper.getRenamedColumnLabel(1));
    }

}
