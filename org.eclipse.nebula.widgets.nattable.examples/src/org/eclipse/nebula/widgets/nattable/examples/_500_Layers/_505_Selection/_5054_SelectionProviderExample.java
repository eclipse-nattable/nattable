/*******************************************************************************
 * Copyright (c) 2013, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._505_Selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.NatTableBorderOverlayPainter;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Example showing how to use JFace ISelectionProvider with a NatTable grid
 * composition.
 */
public class _5054_SelectionProviderExample extends AbstractNatExample {

    public static final String ACTIVE_LABEL = "ACTIVE";

    private boolean isFirstSelectionProvider = true;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _5054_SelectionProviderExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to use JFace ISelectionProvider mechanism with a NatTable grid composition."
                + " For this the RowSelectionProvider adapter class is used which also allows switching the"
                + " NatTable instance that provides the selection at runtime.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(2, true));

        // property names of the Person class
        String[] propertyNames = { "lastName", "firstName" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("firstName", "Firstname");

        IColumnPropertyAccessor<Person> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);

        IRowIdAccessor<Person> rowIdAccessor = new IRowIdAccessor<Person>() {
            @Override
            public Serializable getRowId(Person rowObject) {
                return rowObject.getId();
            }
        };

        // create the first table
        // create the body layer stack
        final IRowDataProvider<Person> firstBodyDataProvider =
                new ListDataProvider<>(getSimpsonsList(), columnPropertyAccessor);
        final DataLayer firstBodyDataLayer =
                new DataLayer(firstBodyDataProvider);
        final SelectionLayer firstSelectionLayer =
                new SelectionLayer(firstBodyDataLayer);
        ViewportLayer firstViewportLayer =
                new ViewportLayer(firstSelectionLayer);

        // use a RowSelectionModel that will perform row selections and is able
        // to identify a row via unique ID
        firstSelectionLayer.setSelectionModel(
                new RowSelectionModel<>(
                        firstSelectionLayer,
                        firstBodyDataProvider,
                        rowIdAccessor));

        // create the column header layer stack
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer firstColumnHeaderDataLayer =
                new DataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer firstColumnHeaderLayer =
                new ColumnHeaderLayer(
                        firstColumnHeaderDataLayer,
                        firstViewportLayer,
                        firstSelectionLayer);

        // register custom label styling to indicate if the table is active
        firstColumnHeaderDataLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {
            @Override
            public void accumulateConfigLabels(
                    LabelStack configLabels, int columnPosition, int rowPosition) {
                if (_5054_SelectionProviderExample.this.isFirstSelectionProvider) {
                    configLabels.addLabelOnTop(ACTIVE_LABEL);
                }
            }
        });

        // set the region labels to make default configurations work, e.g.
        // selection
        CompositeLayer firstCompositeLayer = new CompositeLayer(1, 2);
        firstCompositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, firstColumnHeaderLayer, 0, 0);
        firstCompositeLayer.setChildLayer(GridRegion.BODY, firstViewportLayer, 0, 1);

        final NatTable firstNatTable = new NatTable(panel, firstCompositeLayer, false);

        firstNatTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        firstNatTable.addConfiguration(new ActiveTableStyleConfiguration());
        firstNatTable.configure();

        // set the modern theme
        firstNatTable.setTheme(new ModernNatTableThemeConfiguration());

        // add overlay painter for full borders
        firstNatTable.addOverlayPainter(new NatTableBorderOverlayPainter());

        // create the second table
        // create the body layer stack
        final IRowDataProvider<Person> secondBodyDataProvider =
                new ListDataProvider<>(getFlandersList(), columnPropertyAccessor);
        final DataLayer secondBodyDataLayer =
                new DataLayer(secondBodyDataProvider);
        final SelectionLayer secondSelectionLayer =
                new SelectionLayer(secondBodyDataLayer);
        ViewportLayer secondViewportLayer =
                new ViewportLayer(secondSelectionLayer);

        // use a RowSelectionModel that will perform row selections and is able
        // to identify a row via unique ID
        secondSelectionLayer.setSelectionModel(
                new RowSelectionModel<>(
                        secondSelectionLayer,
                        secondBodyDataProvider,
                        rowIdAccessor));

        // create the column header layer stack
        DataLayer secondColumnHeaderDataLayer =
                new DataLayer(columnHeaderDataProvider);
        ILayer secondColumnHeaderLayer =
                new ColumnHeaderLayer(
                        secondColumnHeaderDataLayer,
                        secondViewportLayer,
                        secondSelectionLayer);

        // register custom label styling to indicate if the table is active
        secondColumnHeaderDataLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {
            @Override
            public void accumulateConfigLabels(
                    LabelStack configLabels, int columnPosition, int rowPosition) {
                if (!_5054_SelectionProviderExample.this.isFirstSelectionProvider) {
                    configLabels.addLabelOnTop(ACTIVE_LABEL);
                }
            }
        });

        // set the region labels to make default configurations work, e.g.
        // selection
        CompositeLayer secondCompositeLayer = new CompositeLayer(1, 2);
        secondCompositeLayer.setChildLayer(
                GridRegion.COLUMN_HEADER, secondColumnHeaderLayer, 0, 0);
        secondCompositeLayer.setChildLayer(
                GridRegion.BODY, secondViewportLayer, 0, 1);

        final NatTable secondNatTable =
                new NatTable(panel, secondCompositeLayer, false);

        secondNatTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        secondNatTable.addConfiguration(new ActiveTableStyleConfiguration());
        secondNatTable.configure();

        // set the modern theme
        secondNatTable.setTheme(new ModernNatTableThemeConfiguration());

        // add overlay painter for full borders
        secondNatTable.addOverlayPainter(new NatTableBorderOverlayPainter());

        // set ISelectionProvider
        final RowSelectionProvider<Person> selectionProvider =
                new RowSelectionProvider<>(
                        firstSelectionLayer,
                        firstBodyDataProvider);

        // add a listener to the selection provider, in an Eclipse application
        // you would do this e.g. getSite().getPage().addSelectionListener()
        selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                log("Selection changed:");

                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                @SuppressWarnings("rawtypes")
                Iterator it = selection.iterator();
                while (it.hasNext()) {
                    Person selected = (Person) it.next();
                    log("  "
                            + selected.getFirstName()
                            + " "
                            + selected.getLastName());
                }
            }

        });

        // layout widgets
        GridDataFactory.fillDefaults().grab(true, true).applyTo(firstNatTable);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(secondNatTable);

        // add a region for buttons
        Composite buttonArea = new Composite(panel, SWT.NONE);
        buttonArea.setLayout(new RowLayout());
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(buttonArea);

        // create a button to enable selection provider change
        Button button = new Button(buttonArea, SWT.PUSH);
        button.setText("Change selection provider");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                _5054_SelectionProviderExample.this.isFirstSelectionProvider =
                        !_5054_SelectionProviderExample.this.isFirstSelectionProvider;
                if (_5054_SelectionProviderExample.this.isFirstSelectionProvider) {
                    selectionProvider.updateSelectionProvider(firstSelectionLayer, firstBodyDataProvider);
                } else {
                    selectionProvider.updateSelectionProvider(secondSelectionLayer, secondBodyDataProvider);
                }

                // refresh both tables to update the active rendering in the
                // column header/ this is not necessary for updating the
                // selection provider
                firstNatTable.doCommand(new VisualRefreshCommand());
                secondNatTable.doCommand(new VisualRefreshCommand());
            }
        });

        // add a log area to the example to show the log entries
        Text output = setupTextArea(panel);
        GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(output);

        return panel;
    }

    private List<Person> getSimpsonsList() {
        List<Person> result = new ArrayList<>();

        result.add(new Person(1, "Homer", "Simpson", Gender.MALE, true, new Date()));
        result.add(new Person(2, "Marge", "Simpson", Gender.FEMALE, true, new Date()));
        result.add(new Person(3, "Bart", "Simpson", Gender.MALE, false, new Date()));
        result.add(new Person(4, "Lisa", "Simpson", Gender.FEMALE, false, new Date()));
        result.add(new Person(5, "Maggie", "Simpson", Gender.FEMALE, false, new Date()));

        return result;
    }

    private List<Person> getFlandersList() {
        List<Person> result = new ArrayList<>();

        result.add(new Person(6, "Ned", "Flanders", Gender.MALE, true, new Date()));
        result.add(new Person(7, "Maude", "Flanders", Gender.FEMALE, true, new Date()));
        result.add(new Person(8, "Rod", "Flanders", Gender.MALE, false, new Date()));
        result.add(new Person(9, "Todd", "Flanders", Gender.MALE, false, new Date()));

        return result;
    }

    class ActiveTableStyleConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            IStyle style = new Style();
            style.setAttributeValue(
                    CellStyleAttributes.BACKGROUND_COLOR,
                    GUIHelper.COLOR_BLUE);
            style.setAttributeValue(
                    CellStyleAttributes.FOREGROUND_COLOR,
                    GUIHelper.COLOR_WHITE);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    style,
                    DisplayMode.NORMAL,
                    ACTIVE_LABEL);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    style,
                    DisplayMode.SELECT,
                    ACTIVE_LABEL);
        }
    }
}
