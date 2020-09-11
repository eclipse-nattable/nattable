/*******************************************************************************
 * Copyright (c) 2016, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.examples._400_Configuration._440_Editing;

import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.nebula.cdatetime.CDateTimeCellEditor;
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
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

/**
 * Example that demonstrates the usage of a date editor based on Nebula
 * CDateTime.
 */
public class _448_CDateTimeEditorExample extends AbstractNatExample {

    public static final String COLUMN_ONE_LABEL = "ColumnOneLabel";
    public static final String COLUMN_TWO_LABEL = "ColumnTwoLabel";
    public static final String COLUMN_THREE_LABEL = "ColumnThreeLabel";
    public static final String COLUMN_FOUR_LABEL = "ColumnFourLabel";
    public static final String COLUMN_FIVE_LABEL = "ColumnFiveLabel";
    public static final String COLUMN_SIX_LABEL = "ColumnSixLabel";

    private EventList<DateValues> valuesToShow = GlazedLists.eventList(new ArrayList<DateValues>());

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _448_CDateTimeEditorExample());
    }

    @Override
    public String getDescription() {
        return "This example demonstrates the usage of the Nebula CDateTime control for editing date and time values in NatTable.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        Composite gridPanel = new Composite(panel, SWT.NONE);
        gridPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);

        Composite buttonPanel = new Composite(panel, SWT.NONE);
        buttonPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        // property names of the DateValues class
        String[] propertyNames = { "columnOneDate", "columnTwoDate",
                "columnThreeDate", "columnFourDate", "columnFiveDate", "columnSixCalendar" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("columnOneDate", "Date/Time");
        propertyToLabelMap.put("columnTwoDate", "Date");
        propertyToLabelMap.put("columnThreeDate", "Time");
        propertyToLabelMap.put("columnFourDate", "Time Discrete");
        propertyToLabelMap.put("columnFiveDate", "Date/Time text only");
        propertyToLabelMap.put("columnSixCalendar", "Calendar");

        this.valuesToShow.add(createDateValues());
        this.valuesToShow.add(createDateValues());

        ConfigRegistry configRegistry = new ConfigRegistry();

        DateGridLayer gridLayer =
                new DateGridLayer(this.valuesToShow, configRegistry, propertyNames, propertyToLabelMap);
        DataLayer bodyDataLayer = gridLayer.getBodyDataLayer();

        final ColumnOverrideLabelAccumulator columnLabelAccumulator =
                new ColumnOverrideLabelAccumulator(bodyDataLayer);
        bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
        registerColumnLabels(columnLabelAccumulator);

        final NatTable natTable = new NatTable(gridPanel, gridLayer, false);
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DateEditConfiguration());
        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        return panel;
    }

    private void registerColumnLabels(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
        columnLabelAccumulator.registerColumnOverrides(0, COLUMN_ONE_LABEL);
        columnLabelAccumulator.registerColumnOverrides(1, COLUMN_TWO_LABEL);
        columnLabelAccumulator.registerColumnOverrides(2, COLUMN_THREE_LABEL);
        columnLabelAccumulator.registerColumnOverrides(3, COLUMN_FOUR_LABEL);
        columnLabelAccumulator.registerColumnOverrides(4, COLUMN_FIVE_LABEL);
        columnLabelAccumulator.registerColumnOverrides(5, COLUMN_SIX_LABEL);
    }

    private DateValues createDateValues() {
        DateValues dv = new DateValues();
        dv.setColumnOneDate(new Date());
        dv.setColumnTwoDate(new Date());
        dv.setColumnThreeDate(new Date());
        dv.setColumnFourDate(new Date());
        dv.setColumnFiveDate(new Date());
        dv.setColumnSixCalendar(Calendar.getInstance());
        return dv;
    }

    /**
     * The body layer stack for the {@link _448_CDateTimeEditorExample}.
     * Consists of
     * <ol>
     * <li>ViewportLayer</li>
     * <li>SelectionLayer</li>
     * <li>ColumnHideShowLayer</li>
     * <li>ColumnReorderLayer</li>
     * <li>DataLayer</li>
     * </ol>
     */
    class DateBodyLayerStack extends AbstractLayerTransform {

        private final DataLayer bodyDataLayer;
        private final ColumnReorderLayer columnReorderLayer;
        private final ColumnHideShowLayer columnHideShowLayer;
        private final SelectionLayer selectionLayer;
        private final ViewportLayer viewportLayer;

        public DateBodyLayerStack(EventList<DateValues> valuesToShow, final String[] propertyNames, ConfigRegistry configRegistry) {
            IDataProvider dataProvider =
                    new ListDataProvider<>(valuesToShow, new ReflectiveColumnPropertyAccessor<>(propertyNames));
            this.bodyDataLayer = new DataLayer(dataProvider);
            this.columnReorderLayer = new ColumnReorderLayer(this.bodyDataLayer);
            this.columnHideShowLayer = new ColumnHideShowLayer(this.columnReorderLayer);
            this.selectionLayer = new SelectionLayer(this.columnHideShowLayer);
            this.viewportLayer = new ViewportLayer(this.selectionLayer);
            setUnderlyingLayer(this.viewportLayer);

            registerCommandHandler(new CopyDataCommandHandler(this.selectionLayer));
        }

        public DataLayer getDataLayer() {
            return this.bodyDataLayer;
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }
    }

    /**
     * The {@link GridLayer} used by the {@link _448_CDateTimeEditorExample}.
     */
    class DateGridLayer extends GridLayer {

        public DateGridLayer(
                EventList<DateValues> valuesToShow,
                ConfigRegistry configRegistry,
                final String[] propertyNames,
                Map<String, String> propertyToLabelMap) {

            super(true);
            init(valuesToShow, configRegistry, propertyNames, propertyToLabelMap);
        }

        private void init(
                EventList<DateValues> valuesToShow,
                ConfigRegistry configRegistry,
                final String[] propertyNames,
                Map<String, String> propertyToLabelMap) {

            // Body
            DateBodyLayerStack bodyLayer = new DateBodyLayerStack(valuesToShow, propertyNames, configRegistry);

            SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

            // Column header
            IDataProvider columnHeaderDataProvider =
                    new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
            ILayer columnHeaderLayer =
                    new ColumnHeaderLayer(
                            new DefaultColumnHeaderDataLayer(columnHeaderDataProvider), bodyLayer, selectionLayer);

            // Row header
            IDataProvider rowHeaderDataProvider =
                    new DefaultRowHeaderDataProvider(bodyLayer.getDataLayer().getDataProvider());
            ILayer rowHeaderLayer =
                    new RowHeaderLayer(
                            new DefaultRowHeaderDataLayer(rowHeaderDataProvider), bodyLayer, selectionLayer);

            // Corner
            ILayer cornerLayer = new CornerLayer(
                    new DataLayer(
                            new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)),
                    rowHeaderLayer,
                    columnHeaderLayer);

            setBodyLayer(bodyLayer);
            setColumnHeaderLayer(columnHeaderLayer);
            setRowHeaderLayer(rowHeaderLayer);
            setCornerLayer(cornerLayer);
        }

        public DataLayer getBodyDataLayer() {
            return ((DateBodyLayerStack) getBodyLayer()).getDataLayer();
        }
    }

    /**
     * Configuration for enabling and configuring edit behaviour.
     */
    class DateEditConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE,
                    IEditableRule.ALWAYS_EDITABLE);

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDateDisplayConverter(),
                    DisplayMode.NORMAL,
                    COLUMN_ONE_LABEL);

            String datePattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                    FormatStyle.MEDIUM,
                    null,
                    IsoChronology.INSTANCE,
                    Locale.getDefault());
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDateDisplayConverter(datePattern),
                    DisplayMode.NORMAL,
                    COLUMN_TWO_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDateDisplayConverter("HH:mm"),
                    DisplayMode.NORMAL,
                    COLUMN_THREE_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDateDisplayConverter("HH:mm"),
                    DisplayMode.NORMAL,
                    COLUMN_FOUR_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDateDisplayConverter(),
                    DisplayMode.NORMAL,
                    COLUMN_FIVE_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDateDisplayConverter() {
                        @Override
                        public Object canonicalToDisplayValue(Object canonicalValue) {
                            Calendar canonical = null;
                            if (canonicalValue != null) {
                                canonical = (Calendar) canonicalValue;
                            }
                            return super.canonicalToDisplayValue(canonical != null ? canonical.getTime() : null);
                        }

                        @Override
                        public Object displayToCanonicalValue(Object displayValue) {
                            if (displayValue != null && !displayValue.toString().isEmpty()) {
                                Calendar result = Calendar.getInstance();
                                result.setTime((Date) super.displayToCanonicalValue(displayValue));
                                return result;
                            }
                            return super.displayToCanonicalValue(displayValue);
                        }
                    },
                    DisplayMode.NORMAL,
                    COLUMN_SIX_LABEL);

            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new CDateTimeCellEditor(),
                    DisplayMode.EDIT,
                    COLUMN_ONE_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new CDateTimeCellEditor(false, CDT.DROP_DOWN | CDT.DATE_SHORT),
                    DisplayMode.EDIT,
                    COLUMN_TWO_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new CDateTimeCellEditor(false, CDT.DROP_DOWN | CDT.TIME_SHORT),
                    DisplayMode.EDIT,
                    COLUMN_THREE_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new CDateTimeCellEditor(false, CDT.DROP_DOWN | CDT.TIME_SHORT | CDT.CLOCK_DISCRETE),
                    DisplayMode.EDIT,
                    COLUMN_FOUR_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    new CDateTimeCellEditor(false, CDT.DATE_SHORT | CDT.TIME_SHORT),
                    DisplayMode.EDIT,
                    COLUMN_FIVE_LABEL);

            // specify an editor that deals with Calendar
            CDateTimeCellEditor editor = new CDateTimeCellEditor();
            editor.setProvideCalendar(true);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR,
                    editor,
                    DisplayMode.EDIT,
                    COLUMN_SIX_LABEL);
        }
    }

    public static class DateValues {
        private Date columnOneDate;
        private Date columnTwoDate;
        private Date columnThreeDate;
        private Date columnFourDate;
        private Date columnFiveDate;
        private Calendar columnSixCalendar;

        public DateValues() {
        }

        public Date getColumnOneDate() {
            return this.columnOneDate;
        }

        public void setColumnOneDate(Date columnOneDate) {
            this.columnOneDate = columnOneDate;
        }

        public Date getColumnTwoDate() {
            return this.columnTwoDate;
        }

        public void setColumnTwoDate(Date columnTwoDate) {
            this.columnTwoDate = columnTwoDate;
        }

        public Date getColumnThreeDate() {
            return this.columnThreeDate;
        }

        public void setColumnThreeDate(Date columnThreeDate) {
            this.columnThreeDate = columnThreeDate;
        }

        public Date getColumnFourDate() {
            return this.columnFourDate;
        }

        public void setColumnFourDate(Date columnFourDate) {
            this.columnFourDate = columnFourDate;
        }

        public Date getColumnFiveDate() {
            return this.columnFiveDate;
        }

        public void setColumnFiveDate(Date columnFiveDate) {
            this.columnFiveDate = columnFiveDate;
        }

        public Calendar getColumnSixCalendar() {
            return this.columnSixCalendar;
        }

        public void setColumnSixCalendar(Calendar columnSixCalendar) {
            this.columnSixCalendar = columnSixCalendar;
        }

    }
}
