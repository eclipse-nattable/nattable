/*******************************************************************************
 * Copyright (c) 2022 Dirk Fauth and others.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.DataModelConstants;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.ComboBoxFilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.ComboBoxGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.GlazedListsFilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowRegularExpressionConverter;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowTextCellEditor;
import org.eclipse.nebula.widgets.nattable.filterrow.IFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.action.ClearFilterAction;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterIconPainter;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.filterrow.event.ClearFilterIconMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
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
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.theme.IThemeExtension;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemState;
import org.eclipse.nebula.widgets.nattable.ui.menu.MenuItemProviders;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * Example showing how to add the filter row to the layer composition of a grid
 * that contains Excel like filters, text filters and combobox filters.
 */
public class _6037_MixedFilterRowExample extends AbstractNatExample {

    private static final String EXCLUDE_LABEL = "EXCLUDE";

    private ArrayList<Serializable> filterExcludes = new ArrayList<>();

    private boolean regexFilterActive = false;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _6037_MixedFilterRowExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the filter row within a grid that has"
                + " Excel-like multi-select combobox filters, free text filters and"
                + " single-selection combobox filters in the filter row";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();

        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "birthday", "address.street", "address.housenumber",
                "address.postalCode", "address.city" };

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

        IRowIdAccessor<PersonWithAddress> rowIdAccessor = new IRowIdAccessor<PersonWithAddress>() {

            @Override
            public Serializable getRowId(PersonWithAddress rowObject) {
                return rowObject.getId();
            }
        };

        final BodyLayerStack<PersonWithAddress> bodyLayerStack =
                new BodyLayerStack<>(
                        PersonService.getPersonsWithAddress(50),
                        columnPropertyAccessor);

        // add some null and empty values to verify the correct handling
        bodyLayerStack.getBodyDataLayer().setDataValue(0, 3, "");
        bodyLayerStack.getBodyDataLayer().setDataValue(0, 5, null);
        bodyLayerStack.getBodyDataLayer().setDataValue(1, 2, "");
        bodyLayerStack.getBodyDataLayer().setDataValue(1, 6, null);

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

        // Create a customized GlazedListsFilterRowComboBoxDataProvider that
        // distincts the empty string and null from the collected values. This
        // way null and "" entries in the collection are treated the same way
        // and there is only a single "empty" entry in the dropdown.
        GlazedListsFilterRowComboBoxDataProvider<PersonWithAddress> filterRowComboBoxDataProvider =
                new GlazedListsFilterRowComboBoxDataProvider<PersonWithAddress>(
                        bodyLayerStack.getGlazedListsEventLayer(),
                        bodyLayerStack.getSortedList(),
                        columnPropertyAccessor) {

                    @Override
                    protected List<?> collectValues(int columnIndex) {
                        List<?> result = super.collectValues(columnIndex);
                        result = result.stream()
                                .map(x -> (x instanceof String && ((String) x).isEmpty()) ? null : x)
                                .distinct()
                                .collect(Collectors.toList());

                        return result;
                    }
                };

        ComboBoxGlazedListsWithExcludeFilterStrategy<PersonWithAddress> filterStrategy =
                new ComboBoxGlazedListsWithExcludeFilterStrategy<>(
                        filterRowComboBoxDataProvider,
                        bodyLayerStack.getFilterList(),
                        columnPropertyAccessor,
                        configRegistry);

        // create the ComboBoxFilterRowHeaderComposite without the default
        // configuration
        ComboBoxFilterRowHeaderComposite<PersonWithAddress> filterRowHeaderLayer =
                new ComboBoxFilterRowHeaderComposite<>(
                        filterStrategy,
                        filterRowComboBoxDataProvider,
                        columnHeaderLayer,
                        columnHeaderDataProvider,
                        configRegistry,
                        false);

        // add a default ComboBoxFilterRowConfiguration with an updated editor
        // that shows a filter icon if a filter is applied
        IComboBoxDataProvider comboBoxDataProvider = filterRowHeaderLayer.getComboBoxDataProvider();
        FilterRowComboBoxCellEditor filterEditor = new FilterRowComboBoxCellEditor(comboBoxDataProvider, 10);
        filterEditor.setShowDropdownFilter(true);
        filterRowHeaderLayer.addConfiguration(
                new ComboBoxFilterRowConfiguration(
                        filterEditor,
                        new ComboBoxFilterIconPainter(comboBoxDataProvider)) {

                    @Override
                    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                        // TODO 2.1 move this hack to the
                        // ComboBoxFilterRowConfiguration
                        // TODO 2.1 create dedicated matcher
                        // ComboBoxClearFilterIconMouseEventMatcher
                        ICellPainter filterRowPainter = configRegistry.getConfigAttribute(
                                CellConfigAttributes.CELL_PAINTER,
                                DisplayMode.NORMAL,
                                GridRegion.FILTER_ROW);
                        uiBindingRegistry.registerFirstSingleClickBinding(
                                new ClearFilterIconMouseEventMatcher((FilterRowPainter) filterRowPainter) {
                                    @SuppressWarnings("rawtypes")
                                    @Override
                                    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
                                        boolean matches = super.matches(natTable, event, regionLabels);
                                        if (matches) {
                                            ILayerCell cell = natTable.getCellByPosition(
                                                    natTable.getColumnPositionByX(event.x),
                                                    natTable.getRowPositionByY(event.y));
                                            Object cellData = cell.getDataValue();
                                            matches = (!EditConstants.SELECT_ALL_ITEMS_VALUE.equals(cellData)
                                                    && !(cellData instanceof Collection
                                                            && ((Collection) cellData).size() == comboBoxDataProvider.getValues(cell.getColumnIndex(), 0).size()));
                                        }
                                        return matches;
                                    }
                                },
                                new ClearFilterAction());
                    };
                });

        // add the specialized configuration to the
        // ComboBoxFilterRowHeaderComposite
        filterRowHeaderLayer.addConfiguration(new FilterRowConfiguration());

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

        // edit configuration
        natTable.addConfiguration(new EditConfiguration());

        // header menu configuration
        natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {

            private static final String ACTIVATE_REGEX_MENU_ID = "ACTIVATE_REGEX";
            private static final String DEACTIVATE_REGEX_MENU_ID = "DEACTIVATE_REGEX";

            @Override
            protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
                return super.createCornerMenu(natTable)
                        .withStateManagerMenuItemProvider();
            }

            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                return super.createColumnHeaderMenu(natTable)
                        .withMenuItemProvider(ACTIVATE_REGEX_MENU_ID, new IMenuItemProvider() {

                            @Override
                            public void addMenuItem(NatTable natTable, Menu popupMenu) {
                                MenuItem excludeRow = new MenuItem(popupMenu, SWT.PUSH);
                                excludeRow.setText("Activate RegEx filter");
                                excludeRow.setEnabled(true);

                                excludeRow.addSelectionListener(new SelectionAdapter() {
                                    @Override
                                    public void widgetSelected(SelectionEvent event) {
                                        _6037_MixedFilterRowExample.this.regexFilterActive = true;
                                        natTable.getConfigRegistry().registerConfigAttribute(
                                                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                                                TextMatchingMode.REGULAR_EXPRESSION,
                                                DisplayMode.NORMAL,
                                                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 0);
                                    }
                                });
                            }
                        })
                        .withVisibleState(ACTIVATE_REGEX_MENU_ID, new IMenuItemState() {

                            @Override
                            public boolean isActive(NatEventData natEventData) {
                                int columnPosition = natEventData.getColumnPosition();
                                int columnIndex = natTable.getColumnIndexByPosition(columnPosition);
                                return columnIndex == 0 && !_6037_MixedFilterRowExample.this.regexFilterActive;
                            }
                        })
                        .withMenuItemProvider(DEACTIVATE_REGEX_MENU_ID, new IMenuItemProvider() {

                            @Override
                            public void addMenuItem(NatTable natTable, Menu popupMenu) {
                                MenuItem excludeRow = new MenuItem(popupMenu, SWT.PUSH);
                                excludeRow.setText("Deactivate RegEx filter");
                                excludeRow.setEnabled(true);

                                excludeRow.addSelectionListener(new SelectionAdapter() {
                                    @Override
                                    public void widgetSelected(SelectionEvent event) {
                                        _6037_MixedFilterRowExample.this.regexFilterActive = false;
                                    }
                                });
                            }
                        })
                        .withVisibleState(DEACTIVATE_REGEX_MENU_ID, new IMenuItemState() {

                            @Override
                            public boolean isActive(NatEventData natEventData) {
                                int columnPosition = natEventData.getColumnPosition();
                                int columnIndex = natTable.getColumnIndexByPosition(columnPosition);
                                return columnIndex == 0 && _6037_MixedFilterRowExample.this.regexFilterActive;
                            }
                        });
            };
        });

        // body menu configuration
        natTable.addConfiguration(new BodyMenuConfiguration<PersonWithAddress>(
                natTable,
                bodyLayerStack,
                rowIdAccessor,
                filterStrategy,
                filterRowHeaderLayer.getFilterRowDataLayer().getFilterRowDataProvider()));

        natTable.configure();

        // The painter instances in a theme configuration are created on demand
        // to avoid unnecessary painter instances in memory. To change the
        // default filter row cell painter with the one for the excel like
        // filter row, we get the painter from the ConfigRegistry after
        // natTable#configure() and override createPainterInstances() of the
        // theme configuration.
        ModernNatTableThemeConfiguration themeConfiguration = new ModernNatTableThemeConfiguration() {
            @Override
            public void createPainterInstances() {
                super.createPainterInstances();
                this.filterRowCellPainter = configRegistry.getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        DisplayMode.NORMAL,
                        GridRegion.FILTER_ROW);
            }
        };

        // configure the filter exclude support
        configureFilterExcludes(rowIdAccessor, filterStrategy, bodyLayerStack, themeConfiguration);

        natTable.setTheme(themeConfiguration);

        natTable.registerCommandHandler(
                new DisplayPersistenceDialogCommandHandler(natTable));

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        return container;
    }

    /**
     * This method is used to configure the filter exclude support. This means
     * it creates and applies an exclude {@link Matcher}, configures an
     * {@link IConfigLabelAccumulator} and registers the styling via
     * {@link IThemeExtension}.
     *
     * @param rowIdAccessor
     * @param filterStrategy
     * @param bodyLayerStack
     * @param themeConfiguration
     */
    private void configureFilterExcludes(
            IRowIdAccessor<PersonWithAddress> rowIdAccessor,
            ComboBoxGlazedListsWithExcludeFilterStrategy<PersonWithAddress> filterStrategy,
            BodyLayerStack<PersonWithAddress> bodyLayerStack,
            ThemeConfiguration themeConfiguration) {

        // register the Matcher to the
        // ComboBoxGlazedListsWithExcludeFilterStrategy
        Matcher<PersonWithAddress> idMatcher = new Matcher<PersonWithAddress>() {
            @Override
            public boolean matches(PersonWithAddress item) {
                return _6037_MixedFilterRowExample.this.filterExcludes.contains(rowIdAccessor.getRowId(item));
            }
        };
        filterStrategy.addExcludeFilter(idMatcher);

        // register the IConfigLabelAccumulator to the body DataLayer
        bodyLayerStack.getBodyDataLayer().setConfigLabelAccumulator(new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                if (idMatcher.matches(bodyLayerStack.getBodyDataProvider().getRowObject(rowPosition))) {
                    configLabels.add(EXCLUDE_LABEL);
                }
            }
        });

        // extend the ThemeConfiguration to add styling for the EXCLUDE_LABEL
        themeConfiguration.addThemeExtension(new IThemeExtension() {

            @Override
            public void unregisterStyles(IConfigRegistry configRegistry) {
                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        DisplayMode.NORMAL,
                        EXCLUDE_LABEL);
            }

            @Override
            public void registerStyles(IConfigRegistry configRegistry) {
                Style style = new Style();
                style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WIDGET_LIGHT_SHADOW);
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        style,
                        DisplayMode.NORMAL,
                        EXCLUDE_LABEL);
            }
        });
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

        private final ListDataProvider<T> bodyDataProvider;
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
            this.bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

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

        public ListDataProvider<T> getBodyDataProvider() {
            return this.bodyDataProvider;
        }

        public DataLayer getBodyDataLayer() {
            return this.bodyDataLayer;
        }

        public GlazedListsEventLayer<T> getGlazedListsEventLayer() {
            return this.glazedListsEventLayer;
        }
    }

    // TODO 2.1 move this class to the GlazedLists extension
    /**
     * Specialized {@link ComboBoxGlazedListsFilterStrategy} that can be used to
     * exclude items from filtering. This means you can register a
     * {@link Matcher} that avoids that matching items get filtered by the
     * filterrow.
     *
     * @param <T>
     */
    class ComboBoxGlazedListsWithExcludeFilterStrategy<T> extends ComboBoxGlazedListsFilterStrategy<T> {

        private final CompositeMatcherEditor<T> compositeMatcherEditor;

        protected Map<Matcher<T>, MatcherEditor<T>> excludeMatcherEditor = new HashMap<>();

        public ComboBoxGlazedListsWithExcludeFilterStrategy(
                FilterRowComboBoxDataProvider<T> comboBoxDataProvider,
                FilterList<T> filterList,
                IColumnAccessor<T> columnAccessor,
                IConfigRegistry configRegistry) {
            super(comboBoxDataProvider, filterList, columnAccessor, configRegistry);

            // The default MatcherEditor is created and stored as member in the
            // DefaultGlazedListsFilterStrategy. That MatcherEditor is used for
            // the default filter operations. To exclude entries from filtering,
            // we create another CompositeMatcherEditor with an OR mode and set
            // that one on the FilterList.
            this.compositeMatcherEditor = new CompositeMatcherEditor<>();
            this.compositeMatcherEditor.setMode(CompositeMatcherEditor.OR);

            this.compositeMatcherEditor.getMatcherEditors().add(getMatcherEditor());

            this.filterList.setMatcherEditor(this.compositeMatcherEditor);
        }

        /**
         * Add a exclude filter to this filter strategy which will always be
         * applied additionally to any other filter to exclude from filtering.
         *
         * @param matcher
         *            the exclude filter to add
         */
        public void addExcludeFilter(final Matcher<T> matcher) {
            // create a new MatcherEditor
            MatcherEditor<T> matcherEditor = GlazedLists.fixedMatcherEditor(matcher);
            addExcludeFilter(matcherEditor);
        }

        /**
         * Add a exclude filter to this filter strategy which will always be
         * applied additionally to any other filter to exclude items from
         * filtering.
         *
         * @param matcherEditor
         *            the exclude filter to add
         */
        public void addExcludeFilter(final MatcherEditor<T> matcherEditor) {
            // add the new MatcherEditor to the CompositeMatcherEditor
            this.filterLock.writeLock().lock();
            try {
                this.compositeMatcherEditor.getMatcherEditors().add(matcherEditor);
            } finally {
                this.filterLock.writeLock().unlock();
            }

            this.excludeMatcherEditor.put(matcherEditor.getMatcher(), matcherEditor);
        }

        /**
         * Remove the exclude filter from this filter strategy.
         *
         * @param matcher
         *            the filter to remove
         */
        public void removeExcludeFilter(final Matcher<T> matcher) {
            MatcherEditor<T> removed = this.excludeMatcherEditor.remove(matcher);
            if (removed != null) {
                this.filterLock.writeLock().lock();
                try {
                    this.compositeMatcherEditor.getMatcherEditors().remove(removed);
                } finally {
                    this.filterLock.writeLock().unlock();
                }
            }
        }

        /**
         * Remove the exclude filter from this filter strategy.
         *
         * @param matcherEditor
         *            the filter to remove
         */
        public void removeExcludeFilter(final MatcherEditor<T> matcherEditor) {
            removeExcludeFilter(matcherEditor.getMatcher());
        }

        /**
         * Removes all applied exclude filters from this filter strategy.
         */
        public void clearExcludeFilter() {
            Collection<MatcherEditor<T>> excludeMatcher = this.excludeMatcherEditor.values();
            if (!excludeMatcher.isEmpty()) {
                this.filterLock.writeLock().lock();
                try {
                    this.compositeMatcherEditor.getMatcherEditors().removeAll(excludeMatcher);
                } finally {
                    this.filterLock.writeLock().unlock();
                }
                this.excludeMatcherEditor.clear();
            }
        }
    }

    /**
     * The configuration to enable editing of {@link PersonWithAddress} objects.
     */
    class EditConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.ALWAYS_EDITABLE);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultIntegerDisplayConverter(),
                    DisplayMode.NORMAL,
                    ColumnLabelAccumulator.COLUMN_LABEL_PREFIX
                            + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DisplayConverter() {

                        @Override
                        public Object canonicalToDisplayValue(Object canonicalValue) {
                            return (canonicalValue != null) ? canonicalValue.toString() : "";
                        }

                        @Override
                        public Object displayToCanonicalValue(Object displayValue) {
                            try {
                                return Gender.valueOf(displayValue.toString());
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        }

                    },
                    DisplayMode.NORMAL,
                    ColumnLabelAccumulator.COLUMN_LABEL_PREFIX
                            + DataModelConstants.GENDER_COLUMN_POSITION);
        }
    }

    /**
     * The configuration to specialize the combobox filter row to mix it with
     * default filters like free text.
     */
    class FilterRowConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {

            // #####
            // Free Edit Text Filter for Firstname column that supports
            // wildcards
            // #####

            // register the FilterRowTextCellEditor in the first column which
            // immediately commits on key press
            FilterRowTextCellEditor editor = new FilterRowTextCellEditor();
            SimpleContentProposalProvider contentProposalProvider =
                    new SimpleContentProposalProvider(
                            CustomFilterRowRegularExpressionConverter.EMPTY_LITERAL,
                            CustomFilterRowRegularExpressionConverter.NOT_EMPTY_LITERAL);
            contentProposalProvider.setFiltering(true);
            KeyStroke keystroke = null;
            try {
                keystroke = KeyStroke.getInstance("Ctrl+Space");
            } catch (ParseException e) {
                // should not happen as the string is correct
            }
            char[] autoActivationChars = ("<").toCharArray();

            editor.enableContentProposal(
                    new TextContentAdapter(),
                    contentProposalProvider,
                    keystroke,
                    autoActivationChars);

            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    editor,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.FIRSTNAME_COLUMN_POSITION);

            // register the FilterRowPainter in the first column to visualize
            // the free edit filter field as the ComboBoxFilterRowConfiguration
            // registers the ComboBoxFilterIconPainter as default
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    new PaddingDecorator(new FilterRowPainter(), 0, 0, 0, 5),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.FIRSTNAME_COLUMN_POSITION);

            // register the FilterRowRegularExpressionConverter in the first
            // column that converts simple expressions like wildcards to valid
            // regular expressions
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new CustomFilterRowRegularExpressionConverter(configRegistry),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.FIRSTNAME_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                    new DefaultDisplayConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.FIRSTNAME_COLUMN_POSITION);

            // #####
            // Fixed value single-selection combobox filter for Gender column
            // #####

            // register a combo box cell editor for the gender column in the
            // filter row the label is set automatically to the value of
            // FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + column
            // position
            ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(
                    CustomFilterRowRegularExpressionConverter.EMPTY_REGEX,
                    CustomFilterRowRegularExpressionConverter.NOT_EMPTY_REGEX,
                    Gender.FEMALE.toString(),
                    Gender.MALE.toString()));
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    comboBoxCellEditor,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.GENDER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    new PaddingDecorator(new FilterRowPainter(), 0, 0, 0, 5),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.GENDER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new CustomFilterRowRegularExpressionConverter(configRegistry),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.GENDER_COLUMN_POSITION);

            // #####
            // Free Edit Text Filter for Housenumber column that supports
            // expressions like greater, lesser, equals
            // #####

            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new TextCellEditor(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    new PaddingDecorator(new FilterRowPainter(), 0, 0, 0, 5),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                    new DefaultIntegerDisplayConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);

            // need to register the DefaultDisplayConverter for the housenumber
            // column as we register a custom display converter to show a label
            // for the empty entry in the multi-select combobox filter
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDisplayConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);

            // register a display converter on the filter row in general that
            // shows a value for an empty entry in the dropdown
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDisplayConverter() {
                        @Override
                        public Object canonicalToDisplayValue(Object sourceValue) {
                            return (sourceValue != null && !sourceValue.toString().isEmpty())
                                    ? sourceValue.toString()
                                    : CustomFilterRowRegularExpressionConverter.EMPTY_LITERAL;
                        }
                    },
                    DisplayMode.NORMAL,
                    GridRegion.FILTER_ROW);

            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.TEXT_DELIMITER, "[&\\|]"); //$NON-NLS-1$
        }
    }

    /**
     * Specialization of the {@link FilterRowRegularExpressionConverter} that
     * additionally parses special literals to corresponding regular
     * expressions.
     */
    class CustomFilterRowRegularExpressionConverter extends FilterRowRegularExpressionConverter {

        static final String EMPTY_LITERAL = "<empty>";
        static final String EMPTY_REGEX = "^$";
        static final String NOT_EMPTY_LITERAL = "<not_empty>";
        static final String NOT_EMPTY_REGEX = "^(?!\\s*$).+";

        private IConfigRegistry configRegistry;

        public CustomFilterRowRegularExpressionConverter(IConfigRegistry configRegistry) {
            this.configRegistry = configRegistry;
        }

        @Override
        public Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue) {
            if (displayValue != null) {
                // first convert the wildcards
                displayValue = super.displayToCanonicalValue(displayValue);

                if (displayValue.toString().contains(EMPTY_LITERAL)
                        || displayValue.toString().contains(NOT_EMPTY_LITERAL)) {

                    this.configRegistry.registerConfigAttribute(
                            FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                            TextMatchingMode.REGULAR_EXPRESSION,
                            DisplayMode.NORMAL,
                            FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + cell.getColumnIndex());
                } else {

                    if (!_6037_MixedFilterRowExample.this.regexFilterActive) {
                        // only switch to CONTAINS if RegEx filtering is not
                        // activated
                        this.configRegistry.registerConfigAttribute(
                                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                                TextMatchingMode.CONTAINS,
                                DisplayMode.NORMAL,
                                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + cell.getColumnIndex());
                    }
                }

                displayValue = displayValue.toString().replace(EMPTY_LITERAL, EMPTY_REGEX);
                displayValue = displayValue.toString().replace(NOT_EMPTY_LITERAL, NOT_EMPTY_REGEX);

                return displayValue;
            }
            return displayValue;
        }

        @Override
        public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue) {
            if (canonicalValue != null) {
                canonicalValue = super.canonicalToDisplayValue(canonicalValue);

                canonicalValue = canonicalValue.toString().replace(EMPTY_REGEX, EMPTY_LITERAL);
                canonicalValue = canonicalValue.toString().replace(NOT_EMPTY_REGEX, NOT_EMPTY_LITERAL);

                return canonicalValue;
            }
            return canonicalValue;
        }
    }

    /**
     * Menu configuration that adds a menu on the body with menu items to
     * exclude/include items from filtering.
     *
     * @param <T>
     */
    class BodyMenuConfiguration<T> extends AbstractUiBindingConfiguration {

        private static final String EXCLUDE_MENU_ID = "exclude";
        private static final String INCLUDE_MENU_ID = "include";

        private final Menu bodyMenu;

        private BodyMenuConfiguration(
                NatTable natTable,
                BodyLayerStack<T> bodyLayerStack,
                IRowIdAccessor<T> rowIdAccessor,
                IFilterStrategy<T> filterStrategy,
                FilterRowDataProvider<T> filterRowDataProvider) {

            this.bodyMenu = new PopupMenuBuilder(natTable)
                    .withMenuItemProvider(EXCLUDE_MENU_ID, new IMenuItemProvider() {

                        @Override
                        public void addMenuItem(NatTable natTable, Menu popupMenu) {
                            MenuItem excludeRow = new MenuItem(popupMenu, SWT.PUSH);
                            excludeRow.setText("Exclude from filter");
                            excludeRow.setEnabled(true);

                            excludeRow.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent event) {
                                    int rowPosition = MenuItemProviders.getNatEventData(event).getRowPosition();
                                    int rowIndex = natTable.getRowIndexByPosition(rowPosition);
                                    T rowObject = bodyLayerStack.getBodyDataProvider().getRowObject(rowIndex);
                                    Serializable rowId = rowIdAccessor.getRowId(rowObject);
                                    _6037_MixedFilterRowExample.this.filterExcludes.add(rowId);
                                    natTable.refresh(false);
                                }
                            });
                        }
                    })
                    .withVisibleState(EXCLUDE_MENU_ID, new IMenuItemState() {

                        @Override
                        public boolean isActive(NatEventData natEventData) {
                            int rowPosition = natEventData.getRowPosition();
                            int rowIndex = natTable.getRowIndexByPosition(rowPosition);
                            T rowObject = bodyLayerStack.getBodyDataProvider().getRowObject(rowIndex);
                            Serializable rowId = rowIdAccessor.getRowId(rowObject);
                            return !_6037_MixedFilterRowExample.this.filterExcludes.contains(rowId);
                        }
                    })
                    .withMenuItemProvider(INCLUDE_MENU_ID, new IMenuItemProvider() {

                        @Override
                        public void addMenuItem(NatTable natTable, Menu popupMenu) {
                            MenuItem excludeRow = new MenuItem(popupMenu, SWT.PUSH);
                            excludeRow.setText("Include to filter");
                            excludeRow.setEnabled(true);

                            excludeRow.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent event) {
                                    int rowPosition = MenuItemProviders.getNatEventData(event).getRowPosition();
                                    int rowIndex = natTable.getRowIndexByPosition(rowPosition);
                                    T rowObject = bodyLayerStack.getBodyDataProvider().getRowObject(rowIndex);
                                    Serializable rowId = rowIdAccessor.getRowId(rowObject);
                                    _6037_MixedFilterRowExample.this.filterExcludes.remove(rowId);
                                    filterStrategy.applyFilter(filterRowDataProvider.getFilterIndexToObjectMap());
                                }
                            });
                        }
                    })
                    .withVisibleState(INCLUDE_MENU_ID, new IMenuItemState() {

                        @Override
                        public boolean isActive(NatEventData natEventData) {
                            int rowPosition = natEventData.getRowPosition();
                            int rowIndex = natTable.getRowIndexByPosition(rowPosition);
                            T rowObject = bodyLayerStack.getBodyDataProvider().getRowObject(rowIndex);
                            Serializable rowId = rowIdAccessor.getRowId(rowObject);
                            return _6037_MixedFilterRowExample.this.filterExcludes.contains(rowId);
                        }
                    })
                    .build();
        }

        @Override
        public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
            uiBindingRegistry.registerMouseDownBinding(
                    new MouseEventMatcher(
                            SWT.NONE,
                            GridRegion.BODY,
                            MouseEventMatcher.RIGHT_BUTTON),
                    new PopupMenuAction(this.bodyMenu));
        }
    }
}