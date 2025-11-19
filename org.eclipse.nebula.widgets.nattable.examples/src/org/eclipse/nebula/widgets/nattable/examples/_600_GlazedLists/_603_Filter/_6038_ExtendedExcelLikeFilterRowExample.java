/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._600_GlazedLists._603_Filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.ComboBoxFilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterUtils;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowCategoryValueMapper;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.menu.DebugMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

/**
 * Example showing how to add the filter row to the layer composition of a grid
 * that looks like the Excel filter with extended support for value collections.
 */
public class _6038_ExtendedExcelLikeFilterRowExample extends AbstractNatExample {

    private FilterRowCategoryValueMapper<String> foodCategoryMapper = new FilterRowCategoryValueMapper<String>() {
        @Override
        public List<String> valuesToCategories(List<String> values) {
            if (values == null || values.size() == 0 || !(values.get(0) instanceof String)) {
                return Collections.emptyList();
            }

            return values.stream().map(value -> {
                switch (value) {
                    case "Vegetables", "Prezels", "Donut" -> {
                        return "Vegetarian Dish";
                    }
                    case "Bacon", "Ham" -> {
                        return "Meat Dish";
                    }
                    case "Fish" -> {
                        return "Fish Dish";
                    }
                }
                return null;
            }).distinct().collect(Collectors.toList());
        }

        @Override
        public Collection<String> resolveCategories(Collection<String> valuesWithCategories) {
            return valuesWithCategories.stream().map(category -> {
                switch (category) {
                    case "Vegetarian Dish" -> {
                        return Arrays.asList("Vegetables", "Prezels", "Donut");
                    }
                    case "Meat Dish" -> {
                        return Arrays.asList("Bacon", "Ham");
                    }
                    case "Fish Dish" -> {
                        return Arrays.asList("Fish");
                    }
                }
                return Arrays.asList(category);
            }).flatMap(List::stream).distinct().collect(Collectors.toList());
        }

    };

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(1600, 800, new _6038_ExtendedExcelLikeFilterRowExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the filter row within a grid that looks like the Excel"
                + " filter row and some extended features like handling of value collections in cells.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();

        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName",
                "description", "gender",
                "married", "birthday", "address.street", "address.housenumber",
                "address.postalCode", "address.city", "favouriteFood",
                "favouriteDrinks" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("description", "Description");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");
        propertyToLabelMap.put("address.street", "Street");
        propertyToLabelMap.put("address.housenumber", "Housenumber");
        propertyToLabelMap.put("address.postalCode", "Postal Code");
        propertyToLabelMap.put("address.city", "City");
        propertyToLabelMap.put("favouriteFood", "Food");
        propertyToLabelMap.put("favouriteDrinks", "Drinks");

        IColumnPropertyAccessor<ExtendedPersonWithAddress> columnPropertyAccessor =
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        final BodyLayerStack<ExtendedPersonWithAddress> bodyLayerStack =
                new BodyLayerStack<>(
                        PersonService.getExtendedPersonsWithAddress(50),
                        columnPropertyAccessor);

        for (int i = 0; i < bodyLayerStack.filterList.size(); i++) {
            var person = bodyLayerStack.filterList.get(i);
            int modulo = i % 5;
            switch (modulo) {
                case 0 -> person.setDescription("One");
                case 1 -> person.setDescription("One,Two");
                case 2 -> person.setDescription("One,Two,Three");
                case 3 -> person.setDescription("One,Two,Three,Four");
                case 4 -> person.setDescription("One,Two,Three,Four,Five");
            }
        }

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        columnHeaderDataLayer,
                        bodyLayerStack,
                        bodyLayerStack.getSelectionLayer());

        ComboBoxFilterRowHeaderComposite<ExtendedPersonWithAddress> filterRowHeaderLayer =
                new ComboBoxFilterRowHeaderComposite<>(
                        bodyLayerStack.getFilterList(),
                        bodyLayerStack.getGlazedListsEventLayer(),
                        bodyLayerStack.getSortedList(),
                        columnPropertyAccessor,
                        columnHeaderLayer,
                        columnHeaderDataProvider,
                        configRegistry);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(
                        rowHeaderDataLayer,
                        bodyLayerStack,
                        bodyLayerStack.getSelectionLayer());

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(
                        columnHeaderDataProvider,
                        rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(
                        cornerDataLayer,
                        rowHeaderLayer,
                        filterRowHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(
                        bodyLayerStack,
                        filterRowHeaderLayer,
                        rowHeaderLayer,
                        cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        NatTable natTable = new NatTable(container, gridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
                return super.createCornerMenu(natTable)
                        .withStateManagerMenuItemProvider();
            }

            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                return super.createColumnHeaderMenu(natTable).withInspectLabelsMenuItem();
            }
        });

        natTable.addConfiguration(new DebugMenuConfiguration(natTable));

        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        IEditableRule.ALWAYS_EDITABLE);
            }

        });

        natTable.configure();

        natTable.registerCommandHandler(
                new DisplayPersistenceDialogCommandHandler(natTable));

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Composite buttonPanel = new Composite(container, SWT.NONE);
        buttonPanel.setLayout(new RowLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        Button flattenDescriptionButton = new Button(buttonPanel, SWT.CHECK);
        flattenDescriptionButton.setSelection(false);
        flattenDescriptionButton.setText("Flatten Description");
        flattenDescriptionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isFlatten = flattenDescriptionButton.getSelection();
                // apply a custom map function when flattening or remove it if
                // not
                filterRowHeaderLayer.getComboBoxDataProvider().setCustomMapper(2,
                        isFlatten ? ComboBoxFilterUtils.DEFAULT_LIST_VALUE_MAP_FUNCTION : null);
                // toggle flattening
                filterRowHeaderLayer.getComboBoxDataProvider().setFlattenCollectionValues(2, isFlatten);
            }
        });

        Button flattenFoodDrinksButton = new Button(buttonPanel, SWT.CHECK);
        flattenFoodDrinksButton.setSelection(false);
        flattenFoodDrinksButton.setText("Flatten Food && Drinks");
        flattenFoodDrinksButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isFlatten = flattenFoodDrinksButton.getSelection();
                filterRowHeaderLayer.getComboBoxDataProvider().setFlattenCollectionValues(10, isFlatten);
                filterRowHeaderLayer.getComboBoxDataProvider().setFlattenCollectionValues(11, isFlatten);
            }
        });

        Button categorizeFoodButton = new Button(buttonPanel, SWT.CHECK);
        Button addCategoriesButton = new Button(buttonPanel, SWT.CHECK);
        categorizeFoodButton.setSelection(false);
        categorizeFoodButton.setText("Categorize Food");
        categorizeFoodButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selected = categorizeFoodButton.getSelection();
                filterRowHeaderLayer.getComboBoxDataProvider().setCategoryValueMapper(10,
                        selected ? _6038_ExtendedExcelLikeFilterRowExample.this.foodCategoryMapper : null);

                addCategoriesButton.setEnabled(selected);
            }
        });

        addCategoriesButton.setSelection(false);
        addCategoriesButton.setEnabled(false);
        addCategoriesButton.setText("Replace Food Values with Categories");
        addCategoriesButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selected = addCategoriesButton.getSelection();
                filterRowHeaderLayer.getComboBoxDataProvider().setCategoriesOnly(10, selected);
            }
        });

        return container;
    }

    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     *
     * @param <T>
     */
    class BodyLayerStack<T> extends AbstractLayerTransform {

        private final SortedList<T> sortedList;
        private final FilterList<T> filterList;

        private final IDataProvider bodyDataProvider;
        private final DataLayer bodyDataLayer;
        private final GlazedListsEventLayer<T> glazedListsEventLayer;

        private final SelectionLayer selectionLayer;

        public BodyLayerStack(List<T> values, IColumnPropertyAccessor<T> columnPropertyAccessor) {
            // wrapping of the list to show into GlazedLists
            // see http://publicobject.com/glazedlists/ for further information
            EventList<T> eventList = GlazedLists.eventList(values);
            TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

            // use the SortedList constructor with 'null' for the Comparator
            // because the Comparator will be set by configuration
            this.sortedList = new SortedList<>(rowObjectsGlazedList, null);
            // wrap the SortedList with the FilterList
            this.filterList = new FilterList<>(this.sortedList);

            this.bodyDataProvider =
                    new ListDataProvider<>(this.filterList, columnPropertyAccessor);
            this.bodyDataLayer = new DataLayer(getBodyDataProvider());

            // layer for event handling of GlazedLists and PropertyChanges
            this.glazedListsEventLayer =
                    new GlazedListsEventLayer<>(this.bodyDataLayer, this.filterList);

            this.selectionLayer = new SelectionLayer(getGlazedListsEventLayer());
            ViewportLayer viewportLayer = new ViewportLayer(getSelectionLayer());

            FreezeLayer freezeLayer = new FreezeLayer(this.selectionLayer);
            CompositeFreezeLayer compositeFreezeLayer =
                    new CompositeFreezeLayer(freezeLayer, viewportLayer, this.selectionLayer);

            setUnderlyingLayer(compositeFreezeLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public SortedList<T> getSortedList() {
            return this.sortedList;
        }

        public FilterList<T> getFilterList() {
            return this.filterList;
        }

        public IDataProvider getBodyDataProvider() {
            return this.bodyDataProvider;
        }

        public DataLayer getBodyDataLayer() {
            return this.bodyDataLayer;
        }

        public GlazedListsEventLayer<T> getGlazedListsEventLayer() {
            return this.glazedListsEventLayer;
        }
    }

}