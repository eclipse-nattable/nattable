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
package org.eclipse.nebula.widgets.nattable.extension.builder.model;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.columnCategories.ColumnCategoriesModel;
import org.eclipse.nebula.widgets.nattable.columnCategories.Node;
import org.eclipse.nebula.widgets.nattable.extension.builder.util.ColumnCategoriesModelAssembler;
import org.junit.Test;

public class ColumnCategoriesModelAssemblerTest {

    @Test
    public void assembleModel() throws Exception {
        TableColumn[] columnProps = new TableColumn[] {
                new TableColumn(0, "a"),
                new TableColumn(1, "b").setCategory("C1"),
                new TableColumn(2, "c").setCategory("C1"),
                new TableColumn(3, "d").setCategory("C2"),
                new TableColumn(4, "e").setCategory("C3"),
                new TableColumn(5, "f") };

        ColumnCategoriesModel model = ColumnCategoriesModelAssembler
                .setupColumnCategories(columnProps);

        Node rootCategory = model.getRootCategory().getChildren().get(0);
        assertEquals(5, rootCategory.getNumberOfChildren());

        Node c1 = rootCategory.getChildren().get(2);
        assertEquals("C1", c1.getData());
        assertEquals(2, c1.getChildren().size());

        Node c2 = rootCategory.getChildren().get(3);
        assertEquals("C2", c2.getData());
        assertEquals(1, c2.getChildren().size());

        Node c3 = rootCategory.getChildren().get(4);
        assertEquals("C3", c3.getData());
        assertEquals(1, c3.getChildren().size());
    }

    @Test
    public void assembleModelAddingAllToRoot() throws Exception {
        TableColumn[] columnProps = new TableColumn[] {
                new TableColumn(0, "a"), new TableColumn(1, "b"),
                new TableColumn(2, "c"), new TableColumn(3, "d"),
                new TableColumn(4, "e"), new TableColumn(5, "f") };

        ColumnCategoriesModel model = ColumnCategoriesModelAssembler
                .setupColumnCategories(columnProps);

        Node rootCategory = model.getRootCategory().getChildren().get(0);
        assertEquals(6, rootCategory.getNumberOfChildren());
    }

}
