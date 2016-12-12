/*******************************************************************************
 * Copyright (c) 2013, 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._600_GlazedLists._603_Filter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.DataModelConstants;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsStaticFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowTextCellEditor;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
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
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemState;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * Simple example showing how to add the filter row to the layer composition of
 * a grid that is using GlazedLists FilterList for filtering. This example also
 * shows how to combine the filter row with static filtering.
 */
public class _6033_GlazedListsStaticFilterExample extends AbstractNatExample {

    private static final String ADD_FLANDERS_MENUITEM = "addFlanders";
    private static final String REMOVE_FLANDERS_MENUITEM = "removeFlanders";
    private static final String ADD_SIMPSON_MENUITEM = "addSimpson";
    private static final String REMOVE_SIMPSON_MENUITEM = "removeSimpson";

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _6033_GlazedListsStaticFilterExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the filter row within a grid"
                + " that is using GlazedLists FilterList for filtering. It also"
                + " shows how to combine the filter row with static filters."
                + " You can add static filters via the context menu on the corner region.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();

        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday",
                "address.street", "address.housenumber", "address.postalCode", "address.city" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");
        propertyToLabelMap.put("address.street", "Street");
        propertyToLabelMap.put("address.housenumber", "Housenumber");
        propertyToLabelMap.put("address.postalCode", "Postal Code");
        propertyToLabelMap.put("address.city", "City");

        IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor =
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        BodyLayerStack<PersonWithAddress> bodyLayerStack =
                new BodyLayerStack<>(
                        PersonService.getPersonsWithAddress(50),
                        columnPropertyAccessor);

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

        // add the ability to add static filters programmatically
        DefaultGlazedListsStaticFilterStrategy<PersonWithAddress> filterStrategy =
                new DefaultGlazedListsStaticFilterStrategy<>(
                        bodyLayerStack.getFilterList(),
                        columnPropertyAccessor,
                        configRegistry);

        // Note: The column header layer is wrapped in a filter row composite.
        // This plugs in the filter row functionality
        FilterRowHeaderComposite<PersonWithAddress> filterRowHeaderLayer =
                new FilterRowHeaderComposite<>(
                        filterStrategy,
                        columnHeaderLayer,
                        columnHeaderDataLayer.getDataProvider(),
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
        NatTable natTable = new NatTable(parent, gridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        // add filter row configuration
        natTable.addConfiguration(new FilterRowConfiguration());

        natTable.addConfiguration(new StaticFilterHeaderMenu(natTable, filterStrategy));

        natTable.configure();

        return natTable;
    }

    /**
     * Specialized {@link HeaderMenuConfiguration} that adds menu items to add
     * and remove static filters via the corner header menu.
     */
    private class StaticFilterHeaderMenu extends HeaderMenuConfiguration {

        private DefaultGlazedListsStaticFilterStrategy<PersonWithAddress> filterStrategy;

        private boolean flandersMatcherActive = false;
        private boolean simpsonMatcherActive = false;

        private Matcher<PersonWithAddress> flandersMatcher = new Matcher<PersonWithAddress>() {
            @Override
            public boolean matches(PersonWithAddress person) {
                return !(person.getLastName() != null && person.getLastName().equals("Flanders"));
            }
        };

        private Matcher<PersonWithAddress> simpsonMatcher = new Matcher<PersonWithAddress>() {
            @Override
            public boolean matches(PersonWithAddress person) {
                return !(person.getLastName() != null && person.getLastName().equals("Simpson"));
            }
        };

        private StaticFilterHeaderMenu(
                NatTable natTable, DefaultGlazedListsStaticFilterStrategy<PersonWithAddress> filterStrategy) {
            super(natTable);
            this.filterStrategy = filterStrategy;
        }

        @Override
        protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
            return super.createCornerMenu(natTable)
                    .withMenuItemProvider(ADD_FLANDERS_MENUITEM, new IMenuItemProvider() {
                        @Override
                        public void addMenuItem(NatTable natTable, Menu popupMenu) {
                            MenuItem clearItem = new MenuItem(popupMenu, SWT.PUSH);
                            clearItem.setText("Add Flanders filter");
                            clearItem.setEnabled(true);

                            clearItem.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent e) {
                                    StaticFilterHeaderMenu.this.filterStrategy.addStaticFilter(StaticFilterHeaderMenu.this.flandersMatcher);
                                    StaticFilterHeaderMenu.this.flandersMatcherActive = true;
                                }
                            });
                        }
                    })
                    .withVisibleState(ADD_FLANDERS_MENUITEM, new IMenuItemState() {

                        @Override
                        public boolean isActive(NatEventData natEventData) {
                            return !StaticFilterHeaderMenu.this.flandersMatcherActive;
                        }
                    })
                    .withMenuItemProvider(REMOVE_FLANDERS_MENUITEM, new IMenuItemProvider() {
                        @Override
                        public void addMenuItem(NatTable natTable, Menu popupMenu) {
                            MenuItem clearItem = new MenuItem(popupMenu, SWT.PUSH);
                            clearItem.setText("Remove Flanders filter");
                            clearItem.setEnabled(true);

                            clearItem.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent e) {
                                    StaticFilterHeaderMenu.this.filterStrategy.removeStaticFilter(StaticFilterHeaderMenu.this.flandersMatcher);
                                    StaticFilterHeaderMenu.this.flandersMatcherActive = false;
                                }
                            });
                        }
                    })
                    .withVisibleState(REMOVE_FLANDERS_MENUITEM, new IMenuItemState() {

                        @Override
                        public boolean isActive(NatEventData natEventData) {
                            return StaticFilterHeaderMenu.this.flandersMatcherActive;
                        }
                    })
                    .withMenuItemProvider(ADD_SIMPSON_MENUITEM, new IMenuItemProvider() {
                        @Override
                        public void addMenuItem(NatTable natTable, Menu popupMenu) {
                            MenuItem clearItem = new MenuItem(popupMenu, SWT.PUSH);
                            clearItem.setText("Add Simpson filter");
                            clearItem.setEnabled(true);

                            clearItem.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent e) {
                                    StaticFilterHeaderMenu.this.filterStrategy.addStaticFilter(StaticFilterHeaderMenu.this.simpsonMatcher);
                                    StaticFilterHeaderMenu.this.simpsonMatcherActive = true;
                                }
                            });
                        }
                    })
                    .withVisibleState(ADD_SIMPSON_MENUITEM, new IMenuItemState() {

                        @Override
                        public boolean isActive(NatEventData natEventData) {
                            return !StaticFilterHeaderMenu.this.simpsonMatcherActive;
                        }
                    })
                    .withMenuItemProvider(REMOVE_SIMPSON_MENUITEM, new IMenuItemProvider() {
                        @Override
                        public void addMenuItem(NatTable natTable, Menu popupMenu) {
                            MenuItem clearItem = new MenuItem(popupMenu, SWT.PUSH);
                            clearItem.setText("Remove Simpson filter");
                            clearItem.setEnabled(true);

                            clearItem.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent e) {
                                    StaticFilterHeaderMenu.this.filterStrategy.removeStaticFilter(StaticFilterHeaderMenu.this.simpsonMatcher);
                                    StaticFilterHeaderMenu.this.simpsonMatcherActive = false;
                                }
                            });
                        }
                    })
                    .withVisibleState(REMOVE_SIMPSON_MENUITEM, new IMenuItemState() {

                        @Override
                        public boolean isActive(NatEventData natEventData) {
                            return StaticFilterHeaderMenu.this.simpsonMatcherActive;
                        }
                    })
                    .withMenuItemProvider(new IMenuItemProvider() {
                        @Override
                        public void addMenuItem(NatTable natTable, Menu popupMenu) {
                            MenuItem clearItem = new MenuItem(popupMenu, SWT.PUSH);
                            clearItem.setText("Clear Static Filters");
                            clearItem.setEnabled(true);

                            clearItem.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent e) {
                                    StaticFilterHeaderMenu.this.filterStrategy.clearStaticFilter();
                                    StaticFilterHeaderMenu.this.flandersMatcherActive = false;
                                    StaticFilterHeaderMenu.this.simpsonMatcherActive = false;
                                }
                            });
                        }
                    });
        }
    }

    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     *
     * @param <T>
     */
    class BodyLayerStack<T> extends AbstractLayerTransform {

        private final FilterList<T> filterList;

        private final IDataProvider bodyDataProvider;

        private final SelectionLayer selectionLayer;

        public BodyLayerStack(List<T> values, IColumnPropertyAccessor<T> columnPropertyAccessor) {
            // wrapping of the list to show into GlazedLists
            // see http://publicobject.com/glazedlists/ for further information
            EventList<T> eventList = GlazedLists.eventList(values);
            TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

            // use the SortedList constructor with 'null' for the Comparator
            // because the Comparator will be set by configuration
            SortedList<T> sortedList = new SortedList<>(rowObjectsGlazedList, null);
            // wrap the SortedList with the FilterList
            this.filterList = new FilterList<>(sortedList);

            this.bodyDataProvider =
                    new ListDataProvider<>(this.filterList, columnPropertyAccessor);
            DataLayer bodyDataLayer = new DataLayer(getBodyDataProvider());

            // layer for event handling of GlazedLists and PropertyChanges
            GlazedListsEventLayer<T> glazedListsEventLayer =
                    new GlazedListsEventLayer<>(bodyDataLayer, this.filterList);

            this.selectionLayer = new SelectionLayer(glazedListsEventLayer);
            ViewportLayer viewportLayer = new ViewportLayer(getSelectionLayer());

            setUnderlyingLayer(viewportLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public FilterList<T> getFilterList() {
            return this.filterList;
        }

        public IDataProvider getBodyDataProvider() {
            return this.bodyDataProvider;
        }
    }

    /**
     * The configuration to enable the edit mode for the grid and additional
     * edit configurations like converters and validators.
     */
    class FilterRowConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {

            // register the FilterRowTextCellEditor in the first column which
            // immediately commits on key press
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new FilterRowTextCellEditor(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.FIRSTNAME_COLUMN_POSITION);

            // register a combo box cell editor for the gender column in the
            // filter row the label is set automatically to the value of
            // FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + column
            // position
            ICellEditor comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(Gender.FEMALE, Gender.MALE));
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR, comboBoxCellEditor,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.GENDER_COLUMN_POSITION);

            // register a combo box cell editor for the married column in the
            // filter row the label is set automatically to the value of
            // FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + column
            // position
            comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(
                    Boolean.TRUE, Boolean.FALSE));
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    comboBoxCellEditor,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.MARRIED_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                    new DefaultIntegerDisplayConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                    TextMatchingMode.EXACT,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.GENDER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                    TextMatchingMode.REGULAR_EXPRESSION,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.TEXT_DELIMITER, "&"); //$NON-NLS-1$

        }

    }
}
