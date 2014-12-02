/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._800_Integration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommandHandler;
import org.eclipse.nebula.widgets.nattable.export.config.DefaultExportBindings;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.print.command.PrintCommandHandler;
import org.eclipse.nebula.widgets.nattable.print.config.DefaultPrintBindings;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

/**
 * Example showing a NatTable that contains a column header and a body layer. It
 * adds sorting, filtering, editing, copy, print and export features.
 *
 * @author Dirk Fauth
 *
 */
public class _801_VerticalCompositionWithFeaturesExample extends
        AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400,
                new _801_VerticalCompositionWithFeaturesExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to assemble a table that consists of a column header and a body layer.\n"
                + "This example also shows how to configure such a composition by adding sorting,"
                + " filtering, editing, copy, print and export features.\n"
                + "Additionally it contains example code for modifying the table content with actions "
                + "outside the NatTable context.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        ConfigRegistry configRegistry = new ConfigRegistry();

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        panel.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        Composite gridPanel = new Composite(panel, SWT.NONE);
        gridPanel.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);

        Composite buttonPanel = new Composite(panel, SWT.NONE);
        buttonPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(false, false).applyTo(buttonPanel);

        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<String, String>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        IColumnPropertyAccessor<Person> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<Person>(
                propertyNames);

        List<Person> values = PersonService.getPersons(10);
        final EventList<Person> eventList = GlazedLists.eventList(values);
        TransformedList<Person, Person> rowObjectsGlazedList = GlazedLists
                .threadSafeList(eventList);

        // use the SortedList constructor with 'null' for the Comparator because
        // the Comparator
        // will be set by configuration
        SortedList<Person> sortedList = new SortedList<Person>(
                rowObjectsGlazedList, null);
        // wrap the SortedList with the FilterList
        FilterList<Person> filterList = new FilterList<Person>(sortedList);

        IRowDataProvider<Person> bodyDataProvider = new ListDataProvider<Person>(
                filterList, columnPropertyAccessor);
        final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        GlazedListsEventLayer<Person> eventLayer = new GlazedListsEventLayer<Person>(
                bodyDataLayer, filterList);
        final SelectionLayer selectionLayer = new SelectionLayer(eventLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(
                propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer = new DataLayer(
                columnHeaderDataProvider);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer,
                viewportLayer, selectionLayer);

        // add sorting
        SortHeaderLayer<Person> sortHeaderLayer = new SortHeaderLayer<Person>(
                columnHeaderLayer, new GlazedListsSortModel<Person>(sortedList,
                        columnPropertyAccessor, configRegistry,
                        columnHeaderDataLayer), false);

        // add the filter row functionality
        final FilterRowHeaderComposite<Person> filterRowHeaderLayer = new FilterRowHeaderComposite<Person>(
                new DefaultGlazedListsFilterStrategy<Person>(filterList,
                        columnPropertyAccessor, configRegistry),
                sortHeaderLayer, columnHeaderDataProvider, configRegistry);

        // set the region labels to make default configurations work, e.g.
        // editing, selection
        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER,
                filterRowHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, viewportLayer, 0, 1);

        // add edit configurations
        compositeLayer.addConfiguration(new DefaultEditConfiguration());
        compositeLayer.addConfiguration(new DefaultEditBindings());

        // add print support
        compositeLayer.registerCommandHandler(new PrintCommandHandler(
                compositeLayer));
        compositeLayer.addConfiguration(new DefaultPrintBindings());

        // add excel export support
        compositeLayer.registerCommandHandler(new ExportCommandHandler(
                compositeLayer));
        compositeLayer.addConfiguration(new DefaultExportBindings());

        final NatTable natTable = new NatTable(gridPanel, compositeLayer, false);

        natTable.setConfigRegistry(configRegistry);

        // adding this configuration adds the styles and the painters to use
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        // add sorting configuration
        natTable.addConfiguration(new SingleClickSortConfiguration());

        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        IEditableRule.ALWAYS_EDITABLE);

                // birthday is never editable
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        IEditableRule.NEVER_EDITABLE, DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);

                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITOR,
                        new ComboBoxCellEditor(Arrays.asList(Gender.FEMALE,
                                Gender.MALE)), DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        getGenderBooleanConverter(), DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);

                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITOR,
                        new ComboBoxCellEditor(Arrays.asList(Boolean.TRUE,
                                Boolean.FALSE)), DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3);
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        new DefaultBooleanDisplayConverter(),
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3);

            }
        });

        natTable.configure();

        final RowSelectionProvider<Person> selectionProvider = new RowSelectionProvider<Person>(
                selectionLayer, bodyDataProvider, false);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Button button = new Button(buttonPanel, SWT.PUSH);
        button.setText("Remove selected item");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // ensure any active cell editor is closed prior actions outside
                // the NatTable context
                if (natTable.getActiveCellEditor() != null) {
                    natTable.commitAndCloseActiveCellEditor();
                }
                Person item = (Person) ((IStructuredSelection) selectionProvider
                        .getSelection()).getFirstElement();
                eventList.remove(item);
            }
        });

        return panel;
    }

    /**
     * @return Returns a simple converter for the gender of a Person.
     *         {@link Gender#MALE} will be interpreted as <code>true</code>
     *         while {@link Gender#FEMALE} will be interpreted as
     *         <code>false</code>
     */
    private IDisplayConverter getGenderBooleanConverter() {
        return new DisplayConverter() {

            @Override
            public Object canonicalToDisplayValue(Object canonicalValue) {
                if (canonicalValue instanceof Gender) {
                    return canonicalValue.toString();
                }
                return null;
            }

            @Override
            public Object displayToCanonicalValue(Object displayValue) {
                Boolean displayBoolean = Boolean.valueOf(displayValue
                        .toString());
                return displayBoolean ? Gender.MALE : Gender.FEMALE;
            }

        };
    }

}
