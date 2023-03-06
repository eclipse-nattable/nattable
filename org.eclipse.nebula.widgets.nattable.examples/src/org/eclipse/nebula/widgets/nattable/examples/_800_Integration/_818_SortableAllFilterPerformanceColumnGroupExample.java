/*******************************************************************************
 * Copyright (c) 2023 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.examples._800_Integration;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommandHandler;
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
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.ComboBoxFilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.ComboBoxGlazedListsWithExcludeFilterStrategy;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.FilterRowUtils;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.GlazedListsFilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.MarkupDisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RegexMarkupValue;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RichTextCellPainter;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RichTextConfigAttributes;
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
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterAppliedEvent;
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
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.action.ClearHoverStylingAction;
import org.eclipse.nebula.widgets.nattable.hover.config.ColumnHeaderResizeHoverBindings;
import org.eclipse.nebula.widgets.nattable.hover.config.RowHeaderResizeHoverBindings;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
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
import org.eclipse.nebula.widgets.nattable.ui.scaling.ScalingUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

public class _818_SortableAllFilterPerformanceColumnGroupExample extends AbstractNatExample {

    private static final String EXCLUDE_LABEL = "EXCLUDE";

    private ArrayList<Serializable> filterExcludes = new ArrayList<>();

    private List<ExtendedPersonWithAddress> mixedPersons = PersonService.getExtendedPersonsWithAddress(1000);
    // private List<ExtendedPersonWithAddress> mixedPersons = createPersons(0);
    private List<ExtendedPersonWithAddress> alternativePersons = createAlternativePersons();

    private AtomicBoolean alternativePersonsActive = new AtomicBoolean(false);

    public static void main(String[] args) {
        StandaloneNatExampleRunner.run(new _818_SortableAllFilterPerformanceColumnGroupExample());
    }

    @Override
    public String getDescription() {
        return "This example shows a complex setup that combines multiple filter features like"
                + " the mixed filter row within a grid that has Excel-like multi-select combobox filters, free text filters and single-selection combobox filters,"
                + " an additional single-line filter,"
                + " a configuration that allows to exclude rows from filtering"
                + " and a configuration so that the filter comboboxes only show items for currently visible rows in the table.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(container);

        Text input = new Text(container, SWT.SINGLE | SWT.SEARCH | SWT.ICON_CANCEL);
        input.setMessage("type filter text");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(input);

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
                "address.city",
                "age", "money", "description", "favouriteFood", "favouriteDrinks" };

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
        propertyToLabelMap.put("age", "Age");
        propertyToLabelMap.put("money", "Money");
        propertyToLabelMap.put("description", "Description");
        propertyToLabelMap.put("favouriteFood", "Food");
        propertyToLabelMap.put("favouriteDrinks", "Drinks");

        IColumnPropertyAccessor<ExtendedPersonWithAddress> columnPropertyAccessor =
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        IRowIdAccessor<ExtendedPersonWithAddress> rowIdAccessor = ExtendedPersonWithAddress::getId;

        final BodyLayerStack<ExtendedPersonWithAddress> bodyLayerStack =
                new BodyLayerStack<>(
                        this.mixedPersons,
                        columnPropertyAccessor);

        // add some null and empty values to verify the correct handling
        bodyLayerStack.getBodyDataLayer().setDataValue(0, 3, "");
        bodyLayerStack.getBodyDataLayer().setDataValue(0, 5, null);
        bodyLayerStack.getBodyDataLayer().setDataValue(1, 2, "");
        bodyLayerStack.getBodyDataLayer().setDataValue(1, 6, null);
        bodyLayerStack.getBodyDataLayer().setDataValue(2, 3, null);
        bodyLayerStack.getBodyDataLayer().setDataValue(2, 5, null);

        // build the column header layer
        ColumnHeaderLayerStack<ExtendedPersonWithAddress> columnHeaderLayerStack =
                new ColumnHeaderLayerStack<ExtendedPersonWithAddress>(
                        propertyNames,
                        propertyToLabelMap,
                        columnPropertyAccessor,
                        bodyLayerStack,
                        configRegistry);

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
                        columnHeaderLayerStack.getColumnHeaderDataProvider(),
                        rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(
                        cornerDataLayer,
                        rowHeaderLayer,
                        columnHeaderLayerStack);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(
                        bodyLayerStack,
                        columnHeaderLayerStack,
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

        natTable.addConfiguration(new SingleClickSortConfiguration());

        // add a ui binding to clear the hover in the column header also if you
        // move over a column group or filter row cell
        natTable.addConfiguration(new AbstractUiBindingConfiguration() {

            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerFirstMouseMoveBinding((natTable, event, regionLabels) -> ((natTable != null && regionLabels == null) || regionLabels != null
                        && (regionLabels.hasLabel(GridRegion.BODY) || regionLabels.hasLabel(GridRegion.COLUMN_GROUP_HEADER) || regionLabels.hasLabel(GridRegion.FILTER_ROW))),
                        new ClearHoverStylingAction());
            }
        });

        // header menu configuration
        natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {

            @Override
            protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
                return super.createCornerMenu(natTable)
                        .withStateManagerMenuItemProvider()
                        .withClearAllFilters();
            }

            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                return new PopupMenuBuilder(natTable)
                        .withHideColumnMenuItem()
                        .withShowAllColumnsMenuItem()
                        .withColumnChooserMenuItem()
                        .withCreateColumnGroupMenuItem()
                        .withUngroupColumnsMenuItem()
                        .withAutoResizeSelectedColumnsMenuItem()
                        .withColumnStyleEditor()
                        .withColumnRenameDialog()
                        .withClearAllFilters()
                        .withFreezeColumnMenuItem();
            }
        });

        // Column group header menu
        final Menu columnGroupHeaderMenu = new PopupMenuBuilder(natTable)
                .withRenameColumnGroupMenuItem()
                .withRemoveColumnGroupMenuItem()
                .build();

        natTable.addConfiguration(new AbstractUiBindingConfiguration() {
            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerFirstMouseDownBinding(
                        new MouseEventMatcher(
                                SWT.NONE,
                                GridRegion.COLUMN_GROUP_HEADER,
                                MouseEventMatcher.RIGHT_BUTTON),
                        new PopupMenuAction(columnGroupHeaderMenu));
            }
        });

        // body menu configuration
        natTable.addConfiguration(new BodyMenuConfiguration<ExtendedPersonWithAddress>(
                natTable,
                bodyLayerStack,
                rowIdAccessor,
                columnHeaderLayerStack.getFilterStrategy(),
                columnHeaderLayerStack.getFilterRowHeaderLayer().getFilterRowDataLayer().getFilterRowDataProvider()));

        natTable.addConfiguration(new ScalingUiBindingConfiguration(natTable));

        // Register column chooser
        DisplayColumnChooserCommandHandler columnChooserCommandHandler =
                new DisplayColumnChooserCommandHandler(
                        bodyLayerStack.getColumnHideShowLayer(),
                        columnHeaderLayerStack.getColumnHeaderLayer(),
                        columnHeaderLayerStack.getColumnHeaderDataLayer(),
                        columnHeaderLayerStack.getColumnGroupHeaderLayer(),
                        false);

        bodyLayerStack.registerCommandHandler(columnChooserCommandHandler);

        natTable.configure();

        // The painter instances in a theme configuration are created on demand
        // to avoid unnecessary painter instances in memory. To change the
        // default filter row cell painter with the one for the excel like
        // filter row, we get the painter from the ConfigRegistry after
        // natTable#configure() and override createPainterInstances() of the
        // theme configuration.
        // Additionally we override the default body painter and data to be able
        // to highlight the single field filter expression
        ModernNatTableThemeConfiguration themeConfiguration = new ModernNatTableThemeConfiguration() {
            @Override
            public void createPainterInstances() {
                super.createPainterInstances();
                this.filterRowCellPainter = configRegistry.getConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        DisplayMode.NORMAL,
                        GridRegion.FILTER_ROW);

                this.defaultCellPainter = new BackgroundPainter(
                        new PaddingDecorator(new RichTextCellPainter(false, false), 0, 5, 0, 5, false));

            }
        };

        // add the style configuration for hover
        themeConfiguration.addThemeExtension(new IThemeExtension() {

            @Override
            public void unregisterStyles(IConfigRegistry configRegistry) {
                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        DisplayMode.HOVER,
                        GridRegion.COLUMN_HEADER);
                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        DisplayMode.SELECT_HOVER,
                        GridRegion.COLUMN_HEADER);

                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        DisplayMode.HOVER,
                        GridRegion.ROW_HEADER);
                configRegistry.unregisterConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        DisplayMode.SELECT_HOVER,
                        GridRegion.ROW_HEADER);
            }

            @Override
            public void registerStyles(IConfigRegistry configRegistry) {
                Style style = new Style();
                style.setAttributeValue(
                        CellStyleAttributes.BACKGROUND_COLOR,
                        GUIHelper.getColor(173, 216, 230));
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        style,
                        DisplayMode.HOVER,
                        GridRegion.COLUMN_HEADER);

                style = new Style();
                style.setAttributeValue(
                        CellStyleAttributes.BACKGROUND_COLOR,
                        GUIHelper.getColor(0, 71, 171));
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        style,
                        DisplayMode.SELECT_HOVER,
                        GridRegion.COLUMN_HEADER);

                style = new Style();
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
        configureFilterExcludes(rowIdAccessor, columnHeaderLayerStack.getFilterStrategy(), bodyLayerStack, themeConfiguration);

        // configure the single field filter support
        configureSingleFieldFilter(input, columnHeaderLayerStack.getFilterStrategy(), bodyLayerStack, themeConfiguration, natTable, columnHeaderLayerStack.getFilterRowHeaderLayer());

        natTable.setTheme(themeConfiguration);

        natTable.registerCommandHandler(
                new DisplayPersistenceDialogCommandHandler(natTable));

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Label rowCount = new Label(container, SWT.NONE);
        rowCount.setText(bodyLayerStack.getFilterList().size() + " / " + bodyLayerStack.getSortedList().size());

        natTable.addLayerListener(new ILayerListener() {

            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof RowStructuralRefreshEvent) {
                    rowCount.setText(bodyLayerStack.getFilterList().size() + " / " + bodyLayerStack.getSortedList().size());
                }
            }
        });

        Composite buttonPanel = new Composite(container, SWT.NONE);
        buttonPanel.setLayout(new RowLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        Button addRowButton = new Button(buttonPanel, SWT.PUSH);
        addRowButton.setText("Add Row");
        addRowButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Address address = new Address();
                address.setStreet("Some Street");
                address.setHousenumber(42);
                address.setPostalCode(12345);
                address.setCity("In the clouds");
                ExtendedPersonWithAddress person = new ExtendedPersonWithAddress(42, "Ralph",
                        "Wiggum", Gender.MALE, false, new Date(), address,
                        "password", "I am Ralph", 0.00, Arrays.asList("Chocolate", "Booger"), Arrays.asList("Saft"));

                bodyLayerStack.getFilterList().add(person);

                // as the GlazedListsEventLayer listens on list changes of the
                // FilterList, but the new entry will be filtered, there will be
                // no ListChangeEvent. Therefore we need to fire a row
                // structural refresh event manually to trigger a combobox cache
                // update
                bodyLayerStack.getGlazedListsEventLayer().fireLayerEvent(
                        new RowStructuralRefreshEvent(bodyLayerStack.getBodyDataLayer()));
            }
        });

        Button toggleComboContentButton = new Button(buttonPanel, SWT.PUSH);
        toggleComboContentButton.setText("Disable Dynamic Filter ComboBox Content");
        toggleComboContentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String buttonText = toggleComboContentButton.getText();
                if (buttonText.startsWith("Disable")) {
                    columnHeaderLayerStack.getFilterRowComboBoxDataProvider().setFilterCollection(null, null);
                    toggleComboContentButton.setText("Enable Dynamic Filter ComboBox Content");
                } else {
                    columnHeaderLayerStack.getFilterRowComboBoxDataProvider().setFilterCollection(bodyLayerStack.getFilterList(), columnHeaderLayerStack.getFilterRowHeaderLayer());
                    toggleComboContentButton.setText("Disable Dynamic Filter ComboBox Content");
                }
            }
        });

        Button replaceContentButton = new Button(buttonPanel, SWT.PUSH);
        replaceContentButton.setText("Replace Table Content");
        replaceContentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                bodyLayerStack.getSortedList().getReadWriteLock().writeLock().lock();
                try {
                    // deactivate
                    bodyLayerStack.getGlazedListsEventLayer().deactivate();
                    columnHeaderLayerStack.getFilterRowComboBoxDataProvider().deactivate();

                    // clear
                    bodyLayerStack.getSortedList().clear();

                    // addall
                    if (_818_SortableAllFilterPerformanceColumnGroupExample.this.alternativePersonsActive.compareAndSet(true, false)) {
                        bodyLayerStack.getSortedList().addAll(_818_SortableAllFilterPerformanceColumnGroupExample.this.mixedPersons);
                    } else {
                        _818_SortableAllFilterPerformanceColumnGroupExample.this.alternativePersonsActive.set(true);
                        bodyLayerStack.getSortedList().addAll(_818_SortableAllFilterPerformanceColumnGroupExample.this.alternativePersons);
                        // bodyLayerStack.getSortedList().addAll(PersonService.getPersonsWithAddress(200));
                    }
                } finally {
                    bodyLayerStack.getSortedList().getReadWriteLock().writeLock().unlock();
                    // activate
                    bodyLayerStack.getGlazedListsEventLayer().activate();
                    columnHeaderLayerStack.getFilterRowComboBoxDataProvider().activate();
                }
            }
        });

        Button reapplyContentButton = new Button(buttonPanel, SWT.PUSH);
        reapplyContentButton.setText("Re-Apply Table Content");
        reapplyContentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                bodyLayerStack.getSortedList().getReadWriteLock().writeLock().lock();
                try {
                    // deactivate
                    bodyLayerStack.getGlazedListsEventLayer().deactivate();
                    columnHeaderLayerStack.getFilterRowComboBoxDataProvider().deactivate();

                    // clear
                    bodyLayerStack.getSortedList().clear();

                    // addall
                    if (_818_SortableAllFilterPerformanceColumnGroupExample.this.alternativePersonsActive.get()) {
                        bodyLayerStack.getSortedList().addAll(_818_SortableAllFilterPerformanceColumnGroupExample.this.alternativePersons);
                    } else {
                        bodyLayerStack.getSortedList().addAll(_818_SortableAllFilterPerformanceColumnGroupExample.this.mixedPersons);
                    }
                } finally {
                    bodyLayerStack.getSortedList().getReadWriteLock().writeLock().unlock();
                    // activate
                    bodyLayerStack.getGlazedListsEventLayer().activate();
                    columnHeaderLayerStack.getFilterRowComboBoxDataProvider().activate();
                }
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
            IRowIdAccessor<ExtendedPersonWithAddress> rowIdAccessor,
            ComboBoxGlazedListsWithExcludeFilterStrategy<ExtendedPersonWithAddress> filterStrategy,
            BodyLayerStack<ExtendedPersonWithAddress> bodyLayerStack,
            ThemeConfiguration themeConfiguration) {

        // register the Matcher to the
        // ComboBoxGlazedListsWithExcludeFilterStrategy
        Matcher<ExtendedPersonWithAddress> idMatcher = item -> _818_SortableAllFilterPerformanceColumnGroupExample.this.filterExcludes.contains(rowIdAccessor.getRowId(item));
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
     * This method is used to configure the single field filter support.
     *
     * @param input
     * @param filterStrategy
     * @param bodyLayerStack
     * @param themeConfiguration
     * @param natTable
     */
    private void configureSingleFieldFilter(
            Text input,
            ComboBoxGlazedListsWithExcludeFilterStrategy<ExtendedPersonWithAddress> filterStrategy,
            BodyLayerStack<ExtendedPersonWithAddress> bodyLayerStack,
            ThemeConfiguration themeConfiguration,
            NatTable natTable,
            ILayer columnHeaderLayer) {

        // define a TextMatcherEditor and add it as static filter
        TextMatcherEditor<ExtendedPersonWithAddress> matcherEditor = new TextMatcherEditor<>(new TextFilterator<ExtendedPersonWithAddress>() {

            @Override
            public void getFilterStrings(List<String> baseList, ExtendedPersonWithAddress element) {
                // add all values that should be included in filtering
                // Note:
                // if special converters are involved in rendering,
                // consider using them for adding the String values
                baseList.add(element.getFirstName());
                baseList.add(element.getLastName());
                baseList.add("" + element.getGender());
                baseList.add("" + element.isMarried());
                baseList.add("" + element.getBirthday());
                baseList.add(element.getAddress().getStreet());
                baseList.add("" + element.getAddress().getHousenumber());
                baseList.add("" + element.getAddress().getPostalCode());
                baseList.add(element.getAddress().getCity());
            }
        });
        matcherEditor.setMode(TextMatcherEditor.CONTAINS);

        filterStrategy.addStaticFilter(matcherEditor);

        RegexMarkupValue regexMarkup = new RegexMarkupValue("",
                "<span style=\"background-color:rgb(255, 255, 0)\">",
                "</span>");

        // markup for highlighting
        MarkupDisplayConverter markupConverter = new MarkupDisplayConverter();
        markupConverter.registerMarkup("highlight", regexMarkup);
        // register markup display converter to be able to combine the markup
        // with other body display content provider
        natTable.getConfigRegistry().registerConfigAttribute(
                RichTextConfigAttributes.MARKUP_DISPLAY_CONVERTER,
                markupConverter,
                DisplayMode.NORMAL,
                GridRegion.BODY);

        // connect the input field with the matcher
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
                    String text = input.getText();
                    matcherEditor.setFilterText(new String[] { text });
                    regexMarkup.setRegexValue(text.isEmpty() ? "" : "(" + text + ")");
                    natTable.refresh(false);
                    columnHeaderLayer.fireLayerEvent(new FilterAppliedEvent(columnHeaderLayer));
                }
            }
        });

        input.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String text = input.getText();
                if (text == null || text.isEmpty()) {
                    matcherEditor.setFilterText(new String[] {});
                    regexMarkup.setRegexValue("");
                    natTable.refresh(false);
                    columnHeaderLayer.fireLayerEvent(new FilterAppliedEvent(columnHeaderLayer, true));
                }
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

        private final ColumnHideShowLayer columnHideShowLayer;

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
            this.columnHideShowLayer = new ColumnHideShowLayer(reorderLayer);

            ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer =
                    new ColumnGroupExpandCollapseLayer(this.columnHideShowLayer);

            this.selectionLayer = new SelectionLayer(columnGroupExpandCollapseLayer);
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

        public ColumnHideShowLayer getColumnHideShowLayer() {
            return this.columnHideShowLayer;
        }
    }

    /**
     * The column header layer stack wrapped in a class for better
     * encapsulation.
     *
     * @param <T>
     */
    class ColumnHeaderLayerStack<T> extends AbstractLayerTransform {
        private final IDataProvider columnHeaderDataProvider;
        private final DataLayer columnHeaderDataLayer;
        private final ColumnHeaderLayer columnHeaderLayer;
        private final ColumnGroupHeaderLayer columnGroupHeaderLayer;

        private final GlazedListsFilterRowComboBoxDataProvider<T> filterRowComboBoxDataProvider;
        private final ComboBoxGlazedListsWithExcludeFilterStrategy<T> filterStrategy;
        private final ComboBoxFilterRowHeaderComposite<T> filterRowHeaderLayer;

        public ColumnHeaderLayerStack(
                String[] propertyNames, Map<String, String> propertyToLabelMap,
                IColumnPropertyAccessor<T> columnPropertyAccessor,
                BodyLayerStack<T> bodyLayerStack, IConfigRegistry configRegistry) {

            // build the column header layer
            this.columnHeaderDataProvider =
                    new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
            this.columnHeaderDataLayer =
                    new DefaultColumnHeaderDataLayer(this.columnHeaderDataProvider);

            HoverLayer columnHoverLayer =
                    new HoverLayer(this.columnHeaderDataLayer, false);

            this.columnHeaderLayer =
                    new ColumnHeaderLayer(
                            columnHoverLayer,
                            bodyLayerStack,
                            bodyLayerStack.getSelectionLayer(),
                            false);

            this.columnHeaderLayer.addConfiguration(
                    new ColumnHeaderResizeHoverBindings(columnHoverLayer));

            SortHeaderLayer<T> sortHeaderLayer =
                    new SortHeaderLayer<>(
                            this.columnHeaderLayer,
                            new GlazedListsSortModel<>(
                                    bodyLayerStack.getSortedList(),
                                    columnPropertyAccessor,
                                    configRegistry,
                                    this.columnHeaderDataLayer));

            this.columnGroupHeaderLayer = new ColumnGroupHeaderLayer(
                    sortHeaderLayer,
                    bodyLayerStack.getSelectionLayer());
            this.columnGroupHeaderLayer.setCalculateHeight(true);

            this.columnGroupHeaderLayer.addGroup("Person", 0, 4);

            // Create a customized FilterRowComboBoxDataProvider that
            // distincts the empty string and null from the collected values.
            // This way null and "" entries in the collection are treated the
            // same way and there is only a single "empty" entry in the
            // dropdown.
            this.filterRowComboBoxDataProvider =
                    new GlazedListsFilterRowComboBoxDataProvider<>(
                            bodyLayerStack.getGlazedListsEventLayer(),
                            bodyLayerStack.getSortedList(),
                            columnPropertyAccessor);
            this.filterRowComboBoxDataProvider.setDistinctNullAndEmpty(true);
            this.filterRowComboBoxDataProvider.setCachingEnabled(true);
            // this.filterRowComboBoxDataProvider.disableUpdateEvents();

            this.filterStrategy =
                    new ComboBoxGlazedListsWithExcludeFilterStrategy<>(
                            this.filterRowComboBoxDataProvider,
                            bodyLayerStack.getFilterList(),
                            columnPropertyAccessor,
                            configRegistry);

            // create the ComboBoxFilterRowHeaderComposite without the default
            // configuration
            this.filterRowHeaderLayer =
                    new ComboBoxFilterRowHeaderComposite<>(
                            this.filterStrategy,
                            this.filterRowComboBoxDataProvider,
                            this.columnGroupHeaderLayer,
                            this.columnHeaderDataProvider,
                            configRegistry,
                            false);

            this.filterRowComboBoxDataProvider.setFilterCollection(bodyLayerStack.getFilterList(), this.filterRowHeaderLayer);

            // add a default ComboBoxFilterRowConfiguration with an updated
            // editor that shows a filter icon if a filter is applied
            FilterRowComboBoxCellEditor filterEditor = new FilterRowComboBoxCellEditor(this.filterRowComboBoxDataProvider, 10);
            filterEditor.configureDropdownFilter(true, true);
            this.filterRowHeaderLayer.addConfiguration(
                    new ComboBoxFilterRowConfiguration(
                            filterEditor,
                            new ComboBoxFilterIconPainter(this.filterRowComboBoxDataProvider),
                            this.filterRowComboBoxDataProvider));

            // add the specialized configuration to the
            // ComboBoxFilterRowHeaderComposite
            this.filterRowHeaderLayer.addConfiguration(new FilterRowConfiguration());

            this.filterRowHeaderLayer
                    .getFilterRowDataLayer()
                    .getFilterRowDataProvider()
                    .setFilterRowComboBoxDataProvider(this.filterRowComboBoxDataProvider);

            setUnderlyingLayer(this.filterRowHeaderLayer);
        }

        /**
         * We override this method to redirect to the underlying layer instead
         * of the default implementation in AbstractLayer. Otherwise the
         * rendering of spanned cells with a CompositeFreezeLayer is incorrect
         * as the cell bound calculation is performed at the wrong layer.
         */
        @Override
        public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
            return this.underlyingLayer.getBoundsByPosition(columnPosition, rowPosition);
        }

        public IDataProvider getColumnHeaderDataProvider() {
            return this.columnHeaderDataProvider;
        }

        public DataLayer getColumnHeaderDataLayer() {
            return this.columnHeaderDataLayer;
        }

        public ColumnHeaderLayer getColumnHeaderLayer() {
            return this.columnHeaderLayer;
        }

        public ColumnGroupHeaderLayer getColumnGroupHeaderLayer() {
            return this.columnGroupHeaderLayer;
        }

        public GlazedListsFilterRowComboBoxDataProvider<T> getFilterRowComboBoxDataProvider() {
            return this.filterRowComboBoxDataProvider;
        }

        public ComboBoxGlazedListsWithExcludeFilterStrategy<T> getFilterStrategy() {
            return this.filterStrategy;
        }

        public ComboBoxFilterRowHeaderComposite<T> getFilterRowHeaderLayer() {
            return this.filterRowHeaderLayer;
        }
    }

    /**
     * The configuration to enable editing of {@link ExtendedPersonWithAddress}
     * objects.
     */
    class EditConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.ALWAYS_EDITABLE);

            DefaultIntegerDisplayConverter housenumberConverter = new DefaultIntegerDisplayConverter();
            DecimalFormat housenumberFormat = new DecimalFormat();
            housenumberFormat.setGroupingUsed(false);
            housenumberConverter.setNumberFormat(housenumberFormat);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    housenumberConverter,
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
                                    _818_SortableAllFilterPerformanceColumnGroupExample.this.filterExcludes.add(rowId);
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
                            return !_818_SortableAllFilterPerformanceColumnGroupExample.this.filterExcludes.contains(rowId);
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
                                    _818_SortableAllFilterPerformanceColumnGroupExample.this.filterExcludes.remove(rowId);
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
                            return _818_SortableAllFilterPerformanceColumnGroupExample.this.filterExcludes.contains(rowId);
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

    private List<ExtendedPersonWithAddress> createAlternativePersons() {
        List<ExtendedPersonWithAddress> result = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Address address = new Address();
            address.setStreet("Evergreen Terrace");
            address.setHousenumber(732);
            address.setPostalCode(54321);
            address.setCity("Springfield");
            result.add(new ExtendedPersonWithAddress(i,
                    "Ralph", "Wiggum", Gender.MALE, false, new Date(),
                    address,
                    "password", "I am Ralph", 0.00, Arrays.asList("Chocolate", "Booger"), Arrays.asList("Saft")));
            result.add(new ExtendedPersonWithAddress(i,
                    "Clancy", "Wiggum", Gender.MALE, true, new Date(),
                    address,
                    "password", "Stop Police", 0.00, Arrays.asList("Donuts", "Burger", "Hot Dogs"), Arrays.asList("Milk", "Juice", "Lemonade")));
            result.add(new ExtendedPersonWithAddress(i,
                    "Sarah", "Wiggum", Gender.FEMALE, true, new Date(),
                    address,
                    "password", "Where is Ralphie", 0.00, Arrays.asList("Salad", "Veggie"), Arrays.asList("Water")));
        }

        for (int i = 400; i < 500; i++) {
            Address address = new Address();
            address.setStreet("Fish Smell Drive");
            address.setHousenumber(19);
            address.setPostalCode(54321);
            address.setCity("Springfield");
            result.add(new ExtendedPersonWithAddress(i,
                    "Nelson", "Muntz", Gender.MALE, false, new Date(),
                    address,
                    "GotCha", "Ha Ha", 0.00, Arrays.asList("Fish", "Cheese"), Arrays.asList("Water, Whiskey")));
        }

        return result;
    }

    private static List<ExtendedPersonWithAddress> createPersons(int startId) {
        List<ExtendedPersonWithAddress> result = new ArrayList<>();

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

        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 1, "Homer", "Simpson", Gender.MALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 2, "Homer", "Simpson", Gender.MALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 3, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 4, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 5, "Marge", "Simpson", Gender.FEMALE, true, new Date(), null),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 6, "Ned", null, Gender.MALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 7, "Maude", null, Gender.FEMALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));

        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 8, "Homer", "Simpson", Gender.MALE, true, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 9, "Homer", "Simpson", Gender.MALE, true, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 10, "Homer", "Simpson", Gender.MALE, true, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 11, "Bart", "Simpson", Gender.MALE, false, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 12, "Bart", "Simpson", Gender.MALE, false, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 13, "Bart", "Simpson", Gender.MALE, false, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 14, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 15, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 16, "Lisa", "Simpson", Gender.FEMALE, false, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 17, "Lisa", "Simpson", Gender.FEMALE, false, new Date()),
                south,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));

        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 18, "Ned", "Flanders", Gender.MALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 19, "Ned", "Flanders", Gender.MALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 20, "Maude", "Flanders", Gender.FEMALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 21, "Maude", "Flanders", Gender.FEMALE, true, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 22, "Rod", "Flanders", Gender.MALE, false, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 23, "Rod", "Flanders", Gender.MALE, false, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 24, "Tod", "Flanders", Gender.MALE, false, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 25, "Tod", "Flanders", Gender.MALE, false, new Date()),
                evergreen,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));

        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 26, "Lenny", "Leonard", Gender.MALE, false, new Date()),
                main,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 27, "Lenny", "Leonard", Gender.MALE, false, new Date()),
                main,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));

        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 28, "Carl", "Carlson", Gender.MALE, false, new Date()),
                main,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 29, "Carl", "Carlson", Gender.MALE, false, new Date()),
                main,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));

        result.add(new ExtendedPersonWithAddress(
                new Person(startId + 30, "Timothy", "Lovejoy", Gender.MALE, false, new Date()),
                main,
                "password", "Dough", 0.00, Arrays.asList("Burger", "Fries", "Donuts"), Arrays.asList("Beer")));
        return result;

    }

}