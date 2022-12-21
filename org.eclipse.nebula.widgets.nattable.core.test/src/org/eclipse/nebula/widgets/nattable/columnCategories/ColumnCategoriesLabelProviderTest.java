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
package org.eclipse.nebula.widgets.nattable.columnCategories;

import static org.eclipse.nebula.widgets.nattable.test.fixture.ColumnCategoriesModelFixture.CATEGORY_B1_LABEL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.columnCategories.Node.Type;
import org.eclipse.nebula.widgets.nattable.columnCategories.gui.ColumnCategoriesLabelProvider;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.nebula.widgets.nattable.test.fixture.ColumnEntriesFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnCategoriesLabelProviderTest {

    private List<ColumnEntry> hiddenEntries;
    private ColumnCategoriesLabelProvider labelProvider;

    @BeforeEach
    public void setup() {
        this.hiddenEntries = ColumnEntriesFixture.getEntriesWithEvenIndexes();
        this.labelProvider = new ColumnCategoriesLabelProvider(this.hiddenEntries);
    }

    @Test
    public void shouldReturnLabelForCategoriesFromTheModel() throws Exception {
        assertEquals(CATEGORY_B1_LABEL, this.labelProvider.getText(new Node(
                CATEGORY_B1_LABEL, Type.CATEGORY)));
        assertEquals(Messages.getString("Unknown"),
                this.labelProvider.getText(new Node("2")));
    }

    @Test
    public void shouldReturnLabelsFromIndexesFromTheColumnEntry()
            throws Exception {
        assertEquals("Index2",
                this.labelProvider.getText(new Node("2", Type.COLUMN)));
        assertEquals("11", this.labelProvider.getText(new Node("11", Type.COLUMN)));
    }

}
