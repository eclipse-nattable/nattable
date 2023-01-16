/*******************************************************************************
 * Copyright (c) 2022, 2023 Dirk Fauth and others.
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.ContextualDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.Address;
import org.eclipse.nebula.widgets.nattable.dataset.person.DataModelConstants;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.ComboBoxFilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.ComboBoxGlazedListsWithExcludeFilterStrategy;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.FilterRowUtils;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.GlazedListsFilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowTextCellEditor;
import org.eclipse.nebula.widgets.nattable.filterrow.IFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterIconPainter;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
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
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.action.ClearHoverStylingAction;
import org.eclipse.nebula.widgets.nattable.hover.config.ColumnHeaderResizeHoverBindings;
import org.eclipse.nebula.widgets.nattable.hover.config.RowHeaderResizeHoverBindings;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
 * Example showing how to add the filter row to the layer composition of a grid
 * that contains Excel like filters, text filters and combobox filters.
 */
public class _6037_MixedFilterRowExample extends AbstractNatExample {

    private static final String EXCLUDE_LABEL = "EXCLUDE";

    private ArrayList<Serializable> filterExcludes = new ArrayList<>();

    public static void main(String[] args) {
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
        String[] propertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday",
                "address.street",
                "address.housenumber",
                "address.postalCode",
                "address.city" };

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

        IRowIdAccessor<PersonWithAddress> rowIdAccessor = PersonWithAddress::getId;

        final BodyLayerStack<PersonWithAddress> bodyLayerStack =
                new BodyLayerStack<>(
                        PersonService.getPersonsWithAddress(50),
                        // createPersons(0),
                        columnPropertyAccessor);

        // add some null and empty values to verify the correct handling
        bodyLayerStack.getBodyDataLayer().setDataValue(0, 3, "");
        bodyLayerStack.getBodyDataLayer().setDataValue(0, 5, null);
        bodyLayerStack.getBodyDataLayer().setDataValue(1, 2, "");
        bodyLayerStack.getBodyDataLayer().setDataValue(1, 6, null);
        bodyLayerStack.getBodyDataLayer().setDataValue(2, 3, null);
        bodyLayerStack.getBodyDataLayer().setDataValue(2, 5, null);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);

        HoverLayer columnHoverLayer =
                new HoverLayer(columnHeaderDataLayer, false);

        ColumnHeaderLayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        columnHoverLayer,
                        bodyLayerStack,
                        bodyLayerStack.getSelectionLayer(),
                        false);

        columnHeaderLayer.addConfiguration(
                new ColumnHeaderResizeHoverBindings(columnHoverLayer));

        // Create a customized GlazedListsFilterRowComboBoxDataProvider that
        // distincts the empty string and null from the collected values. This
        // way null and "" entries in the collection are treated the same way
        // and there is only a single "empty" entry in the dropdown.
        FilterRowComboBoxDataProvider<PersonWithAddress> filterRowComboBoxDataProvider =
                new GlazedListsFilterRowComboBoxDataProvider<>(
                        bodyLayerStack.getGlazedListsEventLayer(),
                        bodyLayerStack.getSortedList(),
                        columnPropertyAccessor);
        filterRowComboBoxDataProvider.setDistinctNullAndEmpty(true);
        filterRowComboBoxDataProvider.setCachingEnabled(true);

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

        filterRowComboBoxDataProvider.setFilterCollection(bodyLayerStack.getFilterList(), filterRowHeaderLayer);

        // add a default ComboBoxFilterRowConfiguration with an updated editor
        // that shows a filter icon if a filter is applied
        FilterRowComboBoxCellEditor filterEditor = new FilterRowComboBoxCellEditor(filterRowComboBoxDataProvider, 10);
        filterEditor.setShowDropdownFilter(true);
        filterRowHeaderLayer.addConfiguration(
                new ComboBoxFilterRowConfiguration(
                        filterEditor,
                        new ComboBoxFilterIconPainter(filterRowComboBoxDataProvider),
                        filterRowComboBoxDataProvider));

        // add the specialized configuration to the
        // ComboBoxFilterRowHeaderComposite
        filterRowHeaderLayer.addConfiguration(new FilterRowConfiguration());

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);

        HoverLayer rowHoverLayer =
                new HoverLayer(rowHeaderDataLayer, false);
        RowHeaderLayer rowHeaderLayer =
                new RowHeaderLayer(
                        rowHoverLayer,
                        bodyLayerStack,
                        bodyLayerStack.getSelectionLayer(),
                        false);

        // add RowHeaderHoverLayerConfiguration to ensure that hover styling and
        // resizing is working together
        rowHeaderLayer.addConfiguration(
                new RowHeaderResizeHoverBindings(rowHoverLayer));

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

        natTable.addConfiguration(new AbstractUiBindingConfiguration() {

            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerMouseMoveBinding((natTable, event, regionLabels) -> ((natTable != null && regionLabels == null) || regionLabels != null
                        && regionLabels.hasLabel(GridRegion.BODY)), new ClearHoverStylingAction());

            }
        });

        // header menu configuration
        natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {

            @Override
            protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
                return super.createCornerMenu(natTable)
                        .withStateManagerMenuItemProvider();
            }
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

        // add the style configuration for hover
        themeConfiguration.addThemeExtension(new IThemeExtension() {

            Image bgImage = GUIHelper.getImageByURL("columnHeaderBg",
                    getClass().getResource("/org/eclipse/nebula/widgets/nattable/examples/resources/column_header_bg.png"));

            Image hoverBgImage = GUIHelper.getImageByURL("hoverColumnHeaderBg",
                    getClass().getResource("/org/eclipse/nebula/widgets/nattable/examples/resources/hovered_column_header_bg.png"));

            TextPainter txtPainter = new TextPainter(false, false);

            ICellPainter bgImagePainter =
                    new BackgroundImagePainter(this.txtPainter, this.bgImage, GUIHelper.getColor(192, 192, 192));

            ICellPainter hoveredHeaderPainter =
                    new BackgroundImagePainter(
                            this.txtPainter, this.hoverBgImage, GUIHelper.getColor(192, 192, 192));

            @Override
            public void unregisterStyles(IConfigRegistry configRegistry) {
                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        DisplayMode.NORMAL,
                        GridRegion.COLUMN_HEADER);

                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        DisplayMode.HOVER,
                        GridRegion.COLUMN_HEADER);
            }

            @Override
            public void registerStyles(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        this.bgImagePainter,
                        DisplayMode.NORMAL,
                        GridRegion.COLUMN_HEADER);

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        this.hoveredHeaderPainter,
                        DisplayMode.HOVER,
                        GridRegion.COLUMN_HEADER);

                Style style = new Style();
                style.setAttributeValue(
                        CellStyleAttributes.BACKGROUND_COLOR,
                        GUIHelper.COLOR_RED);

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        style,
                        DisplayMode.HOVER,
                        GridRegion.ROW_HEADER);

                style = new Style();
                style.setAttributeValue(
                        CellStyleAttributes.BACKGROUND_COLOR,
                        GUIHelper.COLOR_BLUE);

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        style,
                        DisplayMode.SELECT_HOVER,
                        GridRegion.ROW_HEADER);

            }
        });

        // configure the filter exclude support
        configureFilterExcludes(rowIdAccessor, filterStrategy, bodyLayerStack, themeConfiguration);

        natTable.setTheme(themeConfiguration);

        natTable.registerCommandHandler(
                new DisplayPersistenceDialogCommandHandler(natTable));

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        Button button = new Button(container, SWT.PUSH);
        button.setText("Add Row");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Address address = new Address();
                address.setStreet("Some Street");
                address.setHousenumber(42);
                address.setPostalCode(12345);
                address.setCity("In the clouds");
                PersonWithAddress person = new PersonWithAddress(42, "Ralph",
                        "Wiggum", Gender.MALE, false, new Date(), address);

                bodyLayerStack.getSortedList().add(person);
            }
        });

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
        Matcher<PersonWithAddress> idMatcher = item -> _6037_MixedFilterRowExample.this.filterExcludes.contains(rowIdAccessor.getRowId(item));
        filterStrategy.addExcludeFilter(idMatcher);

        // register the IConfigLabelAccumulator to the body DataLayer
        AggregateConfigLabelAccumulator aggregate = new AggregateConfigLabelAccumulator();
        aggregate.add(bodyLayerStack.getBodyDataLayer().getConfigLabelAccumulator());
        aggregate.add((IConfigLabelAccumulator) (configLabels, columnPosition, rowPosition) -> {
            if (idMatcher.matches(bodyLayerStack.getBodyDataProvider().getRowObject(rowPosition))) {
                configLabels.add(EXCLUDE_LABEL);
            }
        });
        bodyLayerStack.getBodyDataLayer().setConfigLabelAccumulator(aggregate);

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
    class BodyLayerStack<T> extends AbstractIndexLayerTransform {

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

            ColumnReorderLayer reorderLayer = new ColumnReorderLayer(this.glazedListsEventLayer);
            ColumnHideShowLayer hideShowLayer = new ColumnHideShowLayer(reorderLayer);

            this.selectionLayer = new SelectionLayer(hideShowLayer);
            ViewportLayer viewportLayer = new ViewportLayer(this.selectionLayer);

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

            // Gender

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

            // Married

            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new CheckBoxCellEditor(),
                    DisplayMode.EDIT,
                    ColumnLabelAccumulator.COLUMN_LABEL_PREFIX
                            + DataModelConstants.MARRIED_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    new CheckBoxPainter(),
                    DisplayMode.NORMAL,
                    ColumnLabelAccumulator.COLUMN_LABEL_PREFIX
                            + DataModelConstants.MARRIED_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultBooleanDisplayConverter(),
                    DisplayMode.NORMAL,
                    ColumnLabelAccumulator.COLUMN_LABEL_PREFIX
                            + DataModelConstants.MARRIED_COLUMN_POSITION);

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

            // register display converters for the first column to support an
            // unidirectional conversion of user friendly strings to complex
            // regular expressions.

            // CellConfigAttributes.DISPLAY_CONVERTER is needed for editing.
            // Using the DefaultDisplayConverter will simply take the entered
            // value to the data model. That means, the filter row contains
            // exactly the value that was entered by the user.
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDisplayConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.FIRSTNAME_COLUMN_POSITION);

            // FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER is used to
            // convert the value in the filter row to a filter string. It is
            // used for the unidirectional conversion of the user value to a
            // complex regular expression.
            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                    new CustomFilterRowRegularExpressionConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.FIRSTNAME_COLUMN_POSITION);

            // FilterRowConfigAttributes.FILTER_CONTENT_DISPLAY_CONVERTER is
            // needed to convert the body data. This is necessary as the filter
            // row does not know about the display converter in the body. If it
            // is not set it would use the FILTER_DISPLAY_CONVERTER, which would
            // cause issues in the further processing for the regular expression
            // conversion.
            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.FILTER_CONTENT_DISPLAY_CONVERTER,
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
                    CustomFilterRowRegularExpressionConverter.EMPTY_LITERAL,
                    CustomFilterRowRegularExpressionConverter.NOT_EMPTY_LITERAL,
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
                    new DefaultDisplayConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.GENDER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                    new CustomFilterRowRegularExpressionConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.GENDER_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.FILTER_CONTENT_DISPLAY_CONVERTER,
                    new DefaultDisplayConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.GENDER_COLUMN_POSITION);

            // #####
            // Fixed value single-selection combobox filter for Married column
            // #####

            comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(Boolean.TRUE, Boolean.FALSE));
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    comboBoxCellEditor,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.MARRIED_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    new PaddingDecorator(new FilterRowPainter(), 0, 0, 0, 5),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.MARRIED_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDisplayConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.MARRIED_COLUMN_POSITION);

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

            // register a default display converter to be able to use expression
            // characters
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

            // #####
            // Free Edit Text Filter for Birthday column that supports
            // expressions like greater, lesser, equals
            // #####

            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new TextCellEditor(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.BIRTHDAY_COLUMN_POSITION);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    new PaddingDecorator(new FilterRowPainter(), 0, 0, 0, 5),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.BIRTHDAY_COLUMN_POSITION);

            // register a default display converter to be able to use expression
            // characters
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DateThresholdConverter(),
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.BIRTHDAY_COLUMN_POSITION);

            // register a date converter for the birthday column
            DefaultDateDisplayConverter converter = new DefaultDateDisplayConverter("dd.MM.yyyy");
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    converter,
                    DisplayMode.NORMAL,
                    ColumnLabelAccumulator.COLUMN_LABEL_PREFIX
                            + DataModelConstants.BIRTHDAY_COLUMN_POSITION);

            // register the same converter in the filter row as content
            // converter to support text based filtering on formatted Date
            // objects (e.g. filter for "-08-" to get all birthdays in August)
            configRegistry.registerConfigAttribute(
                    FilterRowConfigAttributes.FILTER_CONTENT_DISPLAY_CONVERTER,
                    converter,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                            + DataModelConstants.BIRTHDAY_COLUMN_POSITION);

        }
    }

    /**
     * Special converter that is able to convert simple year based expressions
     * like &gt; 2000 to a threshold expression that returns all entries that
     * are after 31.12.2000.
     */
    class DateThresholdConverter extends DefaultDisplayConverter {
        @Override
        public Object displayToCanonicalValue(Object displayValue) {
            if (displayValue != null) {
                if (displayValue.toString().matches("(\\d){8}")) {
                    String ds = displayValue.toString();
                    displayValue = ds.substring(0, 2) + "." + ds.substring(2, 4) + "." + ds.substring(4);
                }

                ParseResult parse = FilterRowUtils.parseExpression(displayValue.toString());
                if (parse.getValueToMatch() != null && parse.getValueToMatch().matches("(\\d){4}")) {
                    switch (parse.getMatchOperation()) {
                        case GREATER_THAN:
                            displayValue = "> 31.12." + parse.getValueToMatch();
                            break;
                        case GREATER_THAN_OR_EQUAL:
                            displayValue = ">= 31.12." + (Integer.valueOf(parse.getValueToMatch()) - 1);
                            break;
                        case LESS_THAN:
                            displayValue = "< 01.01." + parse.getValueToMatch();
                            break;
                        case LESS_THAN_OR_EQUAL:
                            displayValue = "<= 01.01." + (Integer.valueOf(parse.getValueToMatch()) + 1);
                            break;
                        case NOT_EQUAL:
                            displayValue = "< 01.01." + parse.getValueToMatch() + " | > 31.12." + parse.getValueToMatch();
                            break;
                        default:
                            // equal or none
                            displayValue = ">= 01.01." + parse.getValueToMatch() + " & <= 31.12." + parse.getValueToMatch();
                            break;
                    }
                }

            }
            return super.displayToCanonicalValue(displayValue);
        }

        @Override
        public Object canonicalToDisplayValue(Object sourceValue) {
            if (sourceValue != null) {
                if (sourceValue.toString().matches("(\\d){2}\\.(\\d){2}\\.(\\d){4}")) {
                    sourceValue = sourceValue.toString().replace(".", "");
                } else {
                    String[] splitted = sourceValue.toString().split("[&\\\\|]");
                    if (splitted.length > 0) {
                        ParseResult parse = FilterRowUtils.parseExpression(splitted[0]);
                        if (parse.getValueToMatch() != null && parse.getValueToMatch().length() > 6) {
                            String year = parse.getValueToMatch().substring(6);
                            switch (parse.getMatchOperation()) {
                                case GREATER_THAN:
                                    sourceValue = "> " + year;
                                    break;
                                case GREATER_THAN_OR_EQUAL:
                                    if (splitted.length == 1) {
                                        sourceValue = ">= " + (Integer.valueOf(year) + 1);
                                    } else if (splitted.length == 2) {
                                        // equal
                                        sourceValue = "= " + year;
                                    }
                                    break;
                                case LESS_THAN:
                                    if (splitted.length == 1) {
                                        sourceValue = "< " + year;
                                    } else if (splitted.length == 2) {
                                        // not equal
                                        sourceValue = "<> " + year;
                                    }
                                    break;
                                case LESS_THAN_OR_EQUAL:
                                    sourceValue = "<= " + (Integer.valueOf(year) - 1);
                                    break;
                                default:
                                    sourceValue = year;
                                    break;
                            }
                        }
                    }
                }
            }
            return super.canonicalToDisplayValue(sourceValue);
        }
    }

    /**
     * Special implementation of a {@link ContextualDisplayConverter} that is
     * used to convert a filter string with special literals and wildcards
     * characters to a corresponding complex regular expression.
     */
    class CustomFilterRowRegularExpressionConverter extends ContextualDisplayConverter {

        static final String EMPTY_LITERAL = "<empty>";
        static final String EMPTY_REGEX = "^$";
        static final String NOT_EMPTY_LITERAL = "<not_empty>";
        static final String NOT_EMPTY_REGEX = "^(?!\\s*$).+";

        static final String IGNORE_CASE_MODE_FLAG = "(?i)";

        static final String NOT_EQUALS_LITERAL = "<>";
        static final String NOT_EQUALS_REGEX_PREFIX = "^((?!";
        static final String NOT_EQUALS_REGEX_SUFFIX = ").)*$";

        @Override
        public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue) {
            if (canonicalValue != null) {
                // first convert the wildcards
                if (canonicalValue != null) {
                    canonicalValue = canonicalValue.toString().replaceAll("\\*", "(.\\*)"); //$NON-NLS-1$ //$NON-NLS-2$
                    canonicalValue = canonicalValue.toString().replaceAll("\\?", "(.\\?)"); //$NON-NLS-1$ //$NON-NLS-2$
                }

                String cvString = canonicalValue.toString();

                if (cvString.contains(EMPTY_LITERAL)
                        || cvString.contains(NOT_EMPTY_LITERAL)
                        || cvString.contains("*")
                        || cvString.contains("?")) {

                    configRegistry.registerConfigAttribute(
                            FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                            TextMatchingMode.REGULAR_EXPRESSION,
                            DisplayMode.NORMAL,
                            FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + cell.getColumnIndex());

                    // add the ignore case flag to the regex
                    cvString = IGNORE_CASE_MODE_FLAG + cvString;
                } else {

                    if (cvString.startsWith("=")) {

                        configRegistry.registerConfigAttribute(
                                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                                TextMatchingMode.EXACT,
                                DisplayMode.NORMAL,
                                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + cell.getColumnIndex());

                        cvString = cvString.substring(1).trim();
                    } else if (cvString.startsWith(NOT_EQUALS_LITERAL)) {
                        configRegistry.registerConfigAttribute(
                                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                                TextMatchingMode.REGULAR_EXPRESSION,
                                DisplayMode.NORMAL,
                                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + cell.getColumnIndex());

                        cvString = IGNORE_CASE_MODE_FLAG + NOT_EQUALS_REGEX_PREFIX + cvString.substring(2).trim() + NOT_EQUALS_REGEX_SUFFIX;
                    } else {
                        // only switch to CONTAINS if RegEx filtering is not
                        // activated
                        configRegistry.registerConfigAttribute(
                                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                                TextMatchingMode.CONTAINS,
                                DisplayMode.NORMAL,
                                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + cell.getColumnIndex());
                    }
                }

                cvString = cvString.replace(EMPTY_LITERAL, EMPTY_REGEX);
                cvString = cvString.replace(NOT_EMPTY_LITERAL, NOT_EMPTY_REGEX);

                return cvString;
            }
            return canonicalValue;
        }

        @Override
        public Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue) {
            // empty as never called
            return null;
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
                            MenuItem includeRow = new MenuItem(popupMenu, SWT.PUSH);
                            includeRow.setText("Include to filter");
                            includeRow.setEnabled(true);

                            includeRow.addSelectionListener(new SelectionAdapter() {
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
                    .withInspectLabelsMenuItem()
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

    private static List<PersonWithAddress> createPersons(int startId) {
        List<PersonWithAddress> result = new ArrayList<>();

        Address evergreen = new Address();
        evergreen.setStreet("Evergreen Terrace");
        evergreen.setHousenumber(42);
        evergreen.setPostalCode(11111);
        evergreen.setCity("Springfield");

        Address south = new Address();
        south.setStreet("South Street");
        south.setHousenumber(23);
        south.setPostalCode(22222);
        south.setCity("Shelbyville");

        Address main = new Address();
        main.setStreet("Main Street");
        main.setHousenumber(4711);
        main.setPostalCode(33333);
        main.setCity("Ogdenville");

        result.add(new PersonWithAddress(
                new Person(startId + 1, "Homer", "Simpson", Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 2, "Homer", "Simpson", Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 3, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 4, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 5, "Marge", "Simpson", Gender.FEMALE, true, new Date(), null),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 6, "Ned", null, Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 7, "Maude", null, Gender.FEMALE, true, new Date()),
                evergreen));

        result.add(new PersonWithAddress(
                new Person(startId + 8, "Homer", "Simpson", Gender.MALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 9, "Homer", "Simpson", Gender.MALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 10, "Homer", "Simpson", Gender.MALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 11, "Bart", "Simpson", Gender.MALE, false, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 12, "Bart", "Simpson", Gender.MALE, false, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 13, "Bart", "Simpson", Gender.MALE, false, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 14, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 15, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 16, "Lisa", "Simpson", Gender.FEMALE, false, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 17, "Lisa", "Simpson", Gender.FEMALE, false, new Date()),
                south));

        result.add(new PersonWithAddress(
                new Person(startId + 18, "Ned", "Flanders", Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 19, "Ned", "Flanders", Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 20, "Maude", "Flanders", Gender.FEMALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 21, "Maude", "Flanders", Gender.FEMALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 22, "Rod", "Flanders", Gender.MALE, false, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 23, "Rod", "Flanders", Gender.MALE, false, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 24, "Tod", "Flanders", Gender.MALE, false, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 25, "Tod", "Flanders", Gender.MALE, false, new Date()),
                evergreen));

        result.add(new PersonWithAddress(
                new Person(startId + 26, "Lenny", "Leonard", Gender.MALE, false, new Date()),
                main));
        result.add(new PersonWithAddress(
                new Person(startId + 27, "Lenny", "Leonard", Gender.MALE, false, new Date()),
                main));

        result.add(new PersonWithAddress(
                new Person(startId + 28, "Carl", "Carlson", Gender.MALE, false, new Date()),
                main));
        result.add(new PersonWithAddress(
                new Person(startId + 29, "Carl", "Carlson", Gender.MALE, false, new Date()),
                main));

        result.add(new PersonWithAddress(
                new Person(startId + 30, "Timothy", "Lovejoy", Gender.MALE, false, new Date()),
                main));
        return result;

    }

}