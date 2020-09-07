/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._505_Selection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.DateCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.selection.EditTraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveCellSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _5055_SelectionTraversalExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 600,
                new _5055_SelectionTraversalExample());
    }

    @Override
    public String getDescription() {
        return "This example shows different traversal strategy configurations.\n\n"
                + "1. AXIS traversal - traversal happens on one axis without cycle, default\n"
                + "2. AXIS CYCLE traversal - traversal happens on one axis where moving over a "
                + "border means to move to the beginning of the same row/column\n"
                + "3. TABLE traversal - traversal happens on table basis where moving over a "
                + "border means to move to the beginning of the next/previous row/column\n"
                + "4. TABLE CYCLE traversal - traversal happens on table basis where moving over "
                + "a border means to move to the beginning of the next/previous row/column, but "
                + "moving over the table end/beginning moves to the opposite\n"
                + "5. mixed - this shows how to mix traversal strategies for left/right and up/down movements\n"
                + "6. editable traversal - shows how to use the editable traversal strategy.\n"
                + "The last three columns are not editable, and the traversal will always search for the next "
                + "editable cell.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday" };
        IColumnPropertyAccessor<Person> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);
        IRowDataProvider<Person> bodyDataProvider =
                new ListDataProvider<>(PersonService.getPersons(3), columnPropertyAccessor);

        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        // 1. AXIS traversal - NatTable default
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        NatTable natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // 2. AXIS CYCLE traversal
        bodyDataLayer = new DataLayer(bodyDataProvider);
        selectionLayer = new SelectionLayer(bodyDataLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        // register a MoveCellSelectionCommandHandler with
        // AXIS_CYCLE_TRAVERSAL_STRATEGY
        viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));

        natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // 3. TABLE traversal
        bodyDataLayer = new DataLayer(bodyDataProvider);
        selectionLayer = new SelectionLayer(bodyDataLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        // register a MoveCellSelectionCommandHandler with
        // TABLE_TRAVERSAL_STRATEGY
        viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));

        natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // 4. TABLE CYCLE traversal
        bodyDataLayer = new DataLayer(bodyDataProvider);
        selectionLayer = new SelectionLayer(bodyDataLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        // register a MoveCellSelectionCommandHandler with
        // TABLE_CYCLE_TRAVERSAL_STRATEGY
        viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));

        natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // 5. mixed traversal
        // on left/right we will use TABLE CYCLE
        // on up/down we will use AXIS CYCLE
        bodyDataLayer = new DataLayer(bodyDataProvider);
        selectionLayer = new SelectionLayer(bodyDataLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        // register a MoveCellSelectionCommandHandler with
        // TABLE_CYCLE_TRAVERSAL_STRATEGY for horizontal traversal
        // and AXIS_CYCLE_TRAVERSAL_STRATEGY for vertical traversal
        // NOTE:
        // You could achieve the same by registering a command handler
        // with TABLE_CYCLE_TRAVERSAL_STRATEGY and registering
        // MoveSelectionActions with a customized ITraversalStrategy, e.g.
        // AXIS_CYCLE_TRAVERSAL_STRATEGY
        viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(selectionLayer,
                        ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));

        natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // 6. edit traversal traversal
        // on left/right we will use TABLE CYCLE
        // on up/down we will use AXIS CYCLE
        bodyDataLayer = new DataLayer(bodyDataProvider);
        selectionLayer = new SelectionLayer(bodyDataLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        final ColumnOverrideLabelAccumulator columnLabelAccumulator =
                new ColumnOverrideLabelAccumulator(bodyDataLayer);
        bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
        registerColumnLabels(columnLabelAccumulator);

        // add some edit configuration
        viewportLayer.addConfiguration(new DefaultEditBindings());
        viewportLayer.addConfiguration(new DefaultEditConfiguration());
        viewportLayer.addConfiguration(new EditorConfiguration());

        natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // register a MoveCellSelectionCommandHandler with
        // TABLE_CYCLE_TRAVERSAL_STRATEGY for horizontal traversal
        // and AXIS_CYCLE_TRAVERSAL_STRATEGY for vertical traversal
        // NOTE:
        // You could achieve the same by registering a command handler
        // with TABLE_CYCLE_TRAVERSAL_STRATEGY and registering
        // MoveSelectionActions with a customized ITraversalStrategy, e.g.
        // AXIS_CYCLE_TRAVERSAL_STRATEGY
        viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(selectionLayer,
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, natTable),
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, natTable)));

        return panel;
    }

    public static String COLUMN_ONE_LABEL = "ColumnOneLabel";
    public static String COLUMN_TWO_LABEL = "ColumnTwoLabel";
    public static String COLUMN_THREE_LABEL = "ColumnThreeLabel";
    public static String COLUMN_FOUR_LABEL = "ColumnFourLabel";
    public static String COLUMN_FIVE_LABEL = "ColumnFiveLabel";

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

            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.NEVER_EDITABLE,
                    DisplayMode.EDIT,
                    COLUMN_THREE_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.NEVER_EDITABLE,
                    DisplayMode.EDIT,
                    COLUMN_FOUR_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.NEVER_EDITABLE,
                    DisplayMode.EDIT,
                    COLUMN_FIVE_LABEL);

            // configure to open the adjacent editor after commit
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.OPEN_ADJACENT_EDITOR,
                    Boolean.TRUE);

            registerEditors(configRegistry);
        }

        private void registerEditors(IConfigRegistry configRegistry) {
            // register a TextCellEditor for column 1 and 2 that moves on commit
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new TextCellEditor(true, true),
                    DisplayMode.NORMAL,
                    COLUMN_ONE_LABEL);

            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new TextCellEditor(true, true),
                    DisplayMode.NORMAL,
                    COLUMN_TWO_LABEL);

            registerColumnFourCheckbox(configRegistry);
            registerColumnFiveDateEditor(configRegistry);
        }

        /**
         * The following will register a default CheckBoxCellEditor for the
         * column that carries the married information.
         * <p>
         * To register a CheckBoxCellEditor, you need to
         * <ol>
         * <li>Register the editor</li>
         * <li>Register the painter corresponding to that editor</li>
         * <li>Register the needed converter</li>
         * </ol>
         *
         * @param configRegistry
         */
        private void registerColumnFourCheckbox(IConfigRegistry configRegistry) {
            // register a CheckBoxCellEditor for column four
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new CheckBoxCellEditor(),
                    DisplayMode.EDIT,
                    COLUMN_FOUR_LABEL);

            // if you want to use the CheckBoxCellEditor, you should also
            // consider using the corresponding CheckBoxPainter to show
            // the content like a checkbox in your NatTable
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
        }

        /**
         * Registers the date editor for the birthday column.
         *
         * @param configRegistry
         */
        private void registerColumnFiveDateEditor(IConfigRegistry configRegistry) {
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
