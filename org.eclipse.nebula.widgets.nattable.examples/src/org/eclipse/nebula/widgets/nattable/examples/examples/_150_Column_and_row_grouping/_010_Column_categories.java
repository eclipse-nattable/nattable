/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._150_Column_and_row_grouping;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnCategories.ChooseColumnsFromCategoriesCommandHandler;
import org.eclipse.nebula.widgets.nattable.columnCategories.ColumnCategoriesModel;
import org.eclipse.nebula.widgets.nattable.columnCategories.Node;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.GlazedListsGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.GlazedLists;

public class _010_Column_categories extends AbstractNatExample {

    private GlazedListsGridLayer<RowDataFixture> gridLayer;

    public static void main(String[] args) {
        StandaloneNatExampleRunner.run(800, 600, new _010_Column_categories());
    }

    @Override
    public String getDescription() {
        return "This example demonstrates an alternative column chooser.\n"
                + "\n"
                + "- Right click on the column header.\n"
                + "- Select the Last option 'Choose columns'\n"
                + "- Hide some columns using the dialog\n"
                + "\n"
                + "This column chooser allows you to group the available columns into 'Categories'. Categories are a read "
                + "only concept and cannot be edited. The intent is to make it easier for the users to choose columns "
                + "when a large number of columns are available.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        ConfigRegistry configRegistry = new ConfigRegistry();

        String[] propertyNames = new String[20];
        for (int i = 0; i < 20; i++) {
            propertyNames[i] = RowDataListFixture.getPropertyNames()[i];
        }
        this.gridLayer = new GlazedListsGridLayer<RowDataFixture>(
                GlazedLists.eventList(RowDataListFixture.getList()),
                propertyNames,
                RowDataListFixture.getPropertyToLabelMap(),
                configRegistry);

        NatTable natTable = new NatTable(parent, this.gridLayer, false);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                return super.createColumnHeaderMenu(natTable)
                        .withCategoriesBasedColumnChooser("Choose columns");
            }
        });

        configureColumnCategoriesInChooser();

        natTable.configure();
        return natTable;
    }

    private void configureColumnCategoriesInChooser() {
        DefaultBodyLayerStack bodyLayer = this.gridLayer.getBodyLayerStack();
        ColumnCategoriesModel model = new ColumnCategoriesModelFixture();

        bodyLayer.registerCommandHandler(
                new ChooseColumnsFromCategoriesCommandHandler(
                        bodyLayer.getColumnHideShowLayer(),
                        this.gridLayer.getColumnHeaderLayerStack().getColumnHeaderLayer(),
                        this.gridLayer.getColumnHeaderLayerStack().getDataLayer(),
                        model));
    }

    class ColumnCategoriesModelFixture extends ColumnCategoriesModel {

        private static final long serialVersionUID = 1001L;

        public static final String CATEGORY_A_LABEL = "a";
        public static final String CATEGORY_B_LABEL = "b";
        public static final String CATEGORY_B1_LABEL = "b1";
        public static final String CATEGORY_B2_LABEL = "b2";
        public static final String CATEGORY_C_LABEL = "c";

        public ColumnCategoriesModelFixture() {
            Node root = addRootCategory("Root");
            root.addChildColumnIndexes(17, 18, 19);

            // a
            Node A = addCategory(root, CATEGORY_A_LABEL);
            A.addChildColumnIndexes(0, 2, 3, 4, 5, 6);

            // b
            Node B = root.addChildCategory(CATEGORY_B_LABEL);
            B.addChildCategory(CATEGORY_B1_LABEL).addChildColumnIndexes(7, 8);
            B.addChildCategory(CATEGORY_B2_LABEL).addChildColumnIndexes(9, 10, 11);

            // c
            Node C = root.addChildCategory(CATEGORY_C_LABEL);
            addColumnsToCategory(C, 12, 13, 14, 15, 16);
        }

    }
}
