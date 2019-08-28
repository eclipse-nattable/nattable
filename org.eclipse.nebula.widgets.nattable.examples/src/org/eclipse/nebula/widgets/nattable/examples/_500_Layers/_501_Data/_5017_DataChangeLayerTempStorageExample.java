/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._501_Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.datachange.DataChangeLayer;
import org.eclipse.nebula.widgets.nattable.datachange.PointKeyHandler;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.SaveDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.DateCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Example showing the usage of the {@link DataChangeLayer} that temporarily
 * stores data changes instead of applying it directly to the underlying data
 * model.
 */
public class _5017_DataChangeLayerTempStorageExample extends AbstractNatExample {

    private static String COLUMN_ONE_LABEL = "ColumnOneLabel";
    private static String COLUMN_TWO_LABEL = "ColumnTwoLabel";
    private static String COLUMN_THREE_LABEL = "ColumnThreeLabel";
    private static String COLUMN_FOUR_LABEL = "ColumnFourLabel";
    private static String COLUMN_FIVE_LABEL = "ColumnFiveLabel";

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _5017_DataChangeLayerTempStorageExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the DataChangeLayer. It can be used to highlight"
                + " cells with changed content. In this example the data model is not modified directly."
                + " Instead the changes are stored locally until a save operation is performed.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        IColumnPropertyAccessor<Person> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);

        final List<Person> data = PersonService.getPersons(10);

        // create the body layer stack
        IDataProvider bodyDataProvider =
                new ListDataProvider<>(data, columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        final ColumnOverrideLabelAccumulator columnLabelAccumulator =
                new ColumnOverrideLabelAccumulator(bodyDataLayer);
        bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
        registerColumnLabels(columnLabelAccumulator);

        // add a DataChangeLayer that temporarily tracks data changes without
        // updating the underlying data model
        DataChangeLayer dataChangeLayer = new DataChangeLayer(bodyDataLayer, new PointKeyHandler(), true);
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(dataChangeLayer);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
        SelectionLayer selectionLayer = new SelectionLayer(columnHideShowLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // create the column header layer stack
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(
                new DataLayer(
                        columnHeaderDataProvider),
                viewportLayer,
                selectionLayer);

        // create the row header layer stack
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(
                new DataLayer(rowHeaderDataProvider, 40, 20),
                viewportLayer,
                selectionLayer);

        // create the corner layer stack
        ILayer cornerLayer = new CornerLayer(
                new DataLayer(
                        new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)),
                rowHeaderLayer,
                columnHeaderLayer);

        // create the grid layer composed with the prior created layer stacks
        GridLayer gridLayer =
                new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);

        NatTable natTable = new NatTable(parent, gridLayer, false);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new EditorConfiguration());

        natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
                return super.createCornerMenu(natTable)
                        .withMenuItemProvider(new IMenuItemProvider() {

                            @Override
                            public void addMenuItem(NatTable natTable, Menu popupMenu) {
                                MenuItem export = new MenuItem(popupMenu, SWT.PUSH);
                                export.setText("Discard changes");
                                export.setEnabled(true);

                                export.addSelectionListener(new SelectionAdapter() {
                                    @Override
                                    public void widgetSelected(SelectionEvent e) {
                                        natTable.doCommand(new DiscardDataChangesCommand());
                                    }
                                });

                            }
                        })
                        .withMenuItemProvider(new IMenuItemProvider() {

                            @Override
                            public void addMenuItem(NatTable natTable, Menu popupMenu) {
                                MenuItem export = new MenuItem(popupMenu, SWT.PUSH);
                                export.setText("Save changes");
                                export.setEnabled(true);

                                export.addSelectionListener(new SelectionAdapter() {
                                    @Override
                                    public void widgetSelected(SelectionEvent e) {
                                        natTable.doCommand(new SaveDataChangesCommand());
                                    }
                                });

                            }
                        });
            }
        });

        natTable.configure();

        return natTable;
    }

    private void registerColumnLabels(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
        columnLabelAccumulator.registerColumnOverrides(0, COLUMN_ONE_LABEL);
        columnLabelAccumulator.registerColumnOverrides(1, COLUMN_TWO_LABEL);
        columnLabelAccumulator.registerColumnOverrides(2, COLUMN_THREE_LABEL);
        columnLabelAccumulator.registerColumnOverrides(3, COLUMN_FOUR_LABEL);
        columnLabelAccumulator.registerColumnOverrides(4, COLUMN_FIVE_LABEL);
    }

    class EditorConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.ALWAYS_EDITABLE);

            // add a special style to highlight the modified cells
            Style style = new Style();
            style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_YELLOW);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    style,
                    DisplayMode.NORMAL,
                    DataChangeLayer.DIRTY);

            // register a CheckBoxCellEditor for column three
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new CheckBoxCellEditor(),
                    DisplayMode.EDIT,
                    COLUMN_THREE_LABEL);

            // if you want to use the CheckBoxCellEditor, you should also
            // consider using the corresponding CheckBoxPainter to show the
            // content like a checkbox in your NatTable
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    new CheckBoxPainter(),
                    DisplayMode.NORMAL,
                    COLUMN_THREE_LABEL);

            // using a CheckBoxCellEditor also needs a Boolean conversion to
            // work correctly
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DisplayConverter() {

                        @Override
                        public Object canonicalToDisplayValue(Object canonicalValue) {
                            if (canonicalValue instanceof Gender) {
                                return ((Gender) canonicalValue) == Gender.MALE;
                            }
                            return null;
                        }

                        @Override
                        public Object displayToCanonicalValue(Object displayValue) {
                            Boolean displayBoolean = Boolean.valueOf(displayValue.toString());
                            return displayBoolean ? Gender.MALE : Gender.FEMALE;
                        }

                    },
                    DisplayMode.NORMAL,
                    COLUMN_THREE_LABEL);

            // register a CheckBoxCellEditor for column three
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new CheckBoxCellEditor(),
                    DisplayMode.EDIT,
                    COLUMN_FOUR_LABEL);

            // if you want to use the CheckBoxCellEditor, you should also
            // consider using the corresponding CheckBoxPainter to show the
            // content like a checkbox in your NatTable
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    new CheckBoxPainter(),
                    DisplayMode.NORMAL,
                    COLUMN_FOUR_LABEL);

            // using a CheckBoxCellEditor also needs a Boolean conversion to
            // work correctly
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultBooleanDisplayConverter(),
                    DisplayMode.NORMAL,
                    COLUMN_FOUR_LABEL);

            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new DateCellEditor(),
                    DisplayMode.EDIT,
                    COLUMN_FIVE_LABEL);

            DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            String pattern = ((SimpleDateFormat) formatter).toPattern();

            // using a DateCellEditor also needs a Date conversion to work
            // correctly
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDateDisplayConverter(pattern),
                    DisplayMode.NORMAL,
                    COLUMN_FIVE_LABEL);

        }
    }
}
