/*****************************************************************************
 * Copyright (c) 2024 CEA LIST and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._800_Integration;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommand;
import org.eclipse.nebula.widgets.nattable.extension.poi.PoiExcelExporter;
import org.eclipse.nebula.widgets.nattable.extension.poi.XSSFExcelExporter;
import org.eclipse.nebula.widgets.nattable.fillhandle.FillHandleCompositeFreezeLayerPainter;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfiguration;
import org.eclipse.nebula.widgets.nattable.formula.FormulaDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.FormulaTooltipErrorReporter;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.formula.config.DefaultFormulaConfiguration;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultGridLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example that demonstrates the usage of formulas using the FormulaDataProvider
 * together with the freeze functionality.
 */
public class _819_FormulaDataWithFreezeExample extends AbstractNatExample {

    boolean evaluationEnabled = true;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _819_FormulaDataWithFreezeExample());
    }

    @Override
    public String getDescription() {
        return "This example demonstrates the usage of formulas to calculate cell values dynamically similar to spreadsheet applications.\n"
                + "Currently the following functions are supported: SUM, NEGATE, PRODUCT, QUOTIENT, MOD, POWER, SQRT and AVERAGE.\n"
                + "Additionally it supports the freeze functionality.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        // TODO add combo box and text field for editing formulas

        Composite gridPanel = new Composite(panel, SWT.NONE);
        gridPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);

        Composite buttonPanel = new Composite(panel, SWT.NONE);
        buttonPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        ConfigRegistry configRegistry = new ConfigRegistry();

        final FormulaGridLayer gridLayer = new FormulaGridLayer();

        final NatTable natTable = new NatTable(gridPanel, gridLayer, false);
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        final FormulaBodyLayerStack bodyLayer = gridLayer.getBodyLayer();

        natTable.addConfiguration(
                new FillHandleConfiguration(
                        bodyLayer.getSelectionLayer(),
                        bodyLayer.getCompositeFreezeLayerPainter()));

        // This is the formula specific configuration
        natTable.addConfiguration(
                new DefaultFormulaConfiguration(
                        bodyLayer.getFormulaDataProvider(),
                        bodyLayer.getSelectionLayer(),
                        natTable.getInternalCellClipboard(),
                        bodyLayer.getCompositeFreezeLayerPainter()));
        bodyLayer.getFormulaDataProvider().setErrorReporter(new FormulaTooltipErrorReporter(natTable, bodyLayer.getDataLayer()));

        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                // export to xlsx
                PoiExcelExporter exporter = new XSSFExcelExporter();
                exporter.setApplyBackgroundColor(false);
                exporter.setFormulaParser(bodyLayer.getFormulaDataProvider().getFormulaParser());
                configRegistry.registerConfigAttribute(
                        ExportConfigAttributes.EXPORTER,
                        exporter);
            }

            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                // change command to show the JFace ProgressDialog on export
                uiBindingRegistry.registerFirstKeyBinding(
                        new KeyEventMatcher(SWT.MOD1, 'e'),
                        (natTable, event) -> natTable.doCommand(new ExportCommand(natTable.getConfigRegistry(), natTable.getShell(), false, true)));
            }
        });

        natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                return new PopupMenuBuilder(natTable)
                        .withHideColumnMenuItem()
                        .withShowAllColumnsMenuItem()
                        .withAutoResizeSelectedColumnsMenuItem();
            }

        });

        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        final Button toggleFormulaButton = new Button(panel, SWT.PUSH);
        toggleFormulaButton.setText("Disable Formula Evaluation");
        toggleFormulaButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                _819_FormulaDataWithFreezeExample.this.evaluationEnabled = !_819_FormulaDataWithFreezeExample.this.evaluationEnabled;
                if (_819_FormulaDataWithFreezeExample.this.evaluationEnabled) {
                    natTable.doCommand(new EnableFormulaEvaluationCommand());
                    toggleFormulaButton.setText("Disable Formula Evaluation");
                } else {
                    natTable.doCommand(new DisableFormulaEvaluationCommand());
                    toggleFormulaButton.setText("Enable Formula Evaluation");
                }
            }
        });

        return panel;
    }

    /**
     * The body layer stack for the {@link _819_FormulaDataWithFreezeExample}.
     * Consists of
     * <ol>
     * <li>ViewportLayer</li>
     * <li>SelectionLayer</li>
     * <li>ColumnHideShowLayer</li>
     * <li>ColumnReorderLayer</li>
     * <li>DataLayer</li>
     * </ol>
     */
    class FormulaBodyLayerStack extends AbstractLayerTransform {

        private final FormulaDataProvider formulaDataProvider;
        private final DataLayer bodyDataLayer;
        private final ColumnReorderLayer columnReorderLayer;
        private final ColumnHideShowLayer columnHideShowLayer;
        private final SelectionLayer selectionLayer;
        private final ViewportLayer viewportLayer;
        private final CompositeFreezeLayer compositeFreezeLayer;
        private final FillHandleCompositeFreezeLayerPainter compositeFreezeLayerPainter;

        public FormulaBodyLayerStack() {
            IDataProvider dataProvider = new TwoDimensionalArrayDataProvider(new String[26][50]);
            this.formulaDataProvider = new FormulaDataProvider(dataProvider);
            this.bodyDataLayer = new DataLayer(this.formulaDataProvider);
            this.columnReorderLayer = new ColumnReorderLayer(this.bodyDataLayer);
            this.columnHideShowLayer = new ColumnHideShowLayer(this.columnReorderLayer);
            this.selectionLayer = new SelectionLayer(this.columnHideShowLayer);
            this.viewportLayer = new ViewportLayer(this.selectionLayer);

            FreezeLayer freezeLayer = new FreezeLayer(this.selectionLayer);
            this.compositeFreezeLayer =
                    new CompositeFreezeLayer(freezeLayer, this.viewportLayer, this.selectionLayer);
            this.compositeFreezeLayerPainter = new FillHandleCompositeFreezeLayerPainter(this.compositeFreezeLayer);
            this.compositeFreezeLayer.setLayerPainter(this.compositeFreezeLayerPainter);

            setUnderlyingLayer(this.compositeFreezeLayer);

            // enable formula result caching
            // this.formulaDataProvider.configureCaching(this.bodyDataLayer);
        }

        public FormulaDataProvider getFormulaDataProvider() {
            return this.formulaDataProvider;
        }

        public DataLayer getDataLayer() {
            return this.bodyDataLayer;
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public CompositeFreezeLayer getCompositeFreezeLayer() {
            return this.compositeFreezeLayer;
        }

        public FillHandleCompositeFreezeLayerPainter getCompositeFreezeLayerPainter() {
            return this.compositeFreezeLayerPainter;
        }
    }

    /**
     * The {@link GridLayer} used by the
     * {@link _819_FormulaDataWithFreezeExample}.
     */
    class FormulaGridLayer extends GridLayer {

        public FormulaGridLayer() {
            super(false);
            addConfiguration(new DefaultGridLayerConfiguration(this) {
                @Override
                protected void addAlternateRowColoringConfig(CompositeLayer gridLayer) {
                    // do nothing as we don't want to see alternate row coloring
                }
            });
            init();
        }

        private void init() {
            // Body
            FormulaBodyLayerStack bodyLayer = new FormulaBodyLayerStack();

            SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

            // Column header
            IDataProvider columnHeaderDataProvider = new LetterColumnHeaderDataProvider(26);
            ILayer columnHeaderLayer = new ColumnHeaderLayer(
                    new DefaultColumnHeaderDataLayer(columnHeaderDataProvider), bodyLayer, selectionLayer);

            // Row header
            IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(
                    bodyLayer.getDataLayer().getDataProvider());
            ILayer rowHeaderLayer = new RowHeaderLayer(
                    new DefaultRowHeaderDataLayer(rowHeaderDataProvider),
                    bodyLayer, selectionLayer);

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

        @Override
        public FormulaBodyLayerStack getBodyLayer() {
            return (FormulaBodyLayerStack) super.getBodyLayer();
        }

        public DataLayer getBodyDataLayer() {
            return getBodyLayer().getDataLayer();
        }

        public FormulaDataProvider getBodyDataProvider() {
            return getBodyLayer().getFormulaDataProvider();
        }
    }

    class LetterColumnHeaderDataProvider implements IDataProvider {

        private int columns = 0;

        public LetterColumnHeaderDataProvider(int columns) {
            this.columns = columns;
        }

        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return (char) (65 + columnIndex);
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getColumnCount() {
            return this.columns;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

    }

    class TwoDimensionalArrayDataProvider implements IDataProvider {

        private String[][] data;

        public TwoDimensionalArrayDataProvider(String[][] data) {
            this.data = data;
        }

        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return this.data[columnIndex][rowIndex];
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            this.data[columnIndex][rowIndex] = newValue != null ? newValue.toString() : null;
        }

        @Override
        public int getColumnCount() {
            return this.data.length;
        }

        @Override
        public int getRowCount() {
            return this.data[0] != null ? this.data[0].length : 0;
        }

    }
}
