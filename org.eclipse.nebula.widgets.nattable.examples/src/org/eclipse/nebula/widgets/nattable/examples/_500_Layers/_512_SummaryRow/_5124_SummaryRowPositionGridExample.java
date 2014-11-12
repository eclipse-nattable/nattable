/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._512_SummaryRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.NumberValues;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.FixedSummaryRowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.DefaultSummaryRowConfiguration;
import org.eclipse.nebula.widgets.nattable.summaryrow.FixedGridSummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.ISummaryProvider;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummationSummaryProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.DebugMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example that demonstrates the usage of the SummaryRow in a NatTable grid and
 * how to position the summary row at fixed locations in a layer composition.
 */
public class _5124_SummaryRowPositionGridExample extends AbstractNatExample {

    static final String SUMMARY_REGION = "SUMMARY";

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400,
                new _5124_SummaryRowPositionGridExample());
    }

    @Override
    public String getDescription() {
        return "This example demonstrates how to add a summary row to the end of a grid.\n"
                + "\n"
                + "Features\n"
                + "	Different style can be applied to the whole row\n"
                + "	Different style can be applied to the individual cells in the summary row\n"
                + "	Plug-in your own summary formulas via ISummaryProvider interface (Default is summation)";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.nebula.widgets.nattable.examples.INatExample#createExampleControl
     * (org.eclipse.swt.widgets.Composite)
     */
    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 20;
        panel.setLayout(layout);

        // property names of the NumberValues class
        String[] propertyNames = { "columnOneNumber", "columnTwoNumber",
                "columnThreeNumber", "columnFourNumber", "columnFiveNumber" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<String, String>();
        propertyToLabelMap.put("columnOneNumber", "Column 1");
        propertyToLabelMap.put("columnTwoNumber", "Column 2");
        propertyToLabelMap.put("columnThreeNumber", "Column 3");
        propertyToLabelMap.put("columnFourNumber", "Column 4");
        propertyToLabelMap.put("columnFiveNumber", "Column 5");

        IColumnPropertyAccessor<NumberValues> cpa =
                new ReflectiveColumnPropertyAccessor<NumberValues>(propertyNames);
        IDataProvider dataProvider =
                new ListDataProvider<NumberValues>(createNumberValueList(), cpa);

        ConfigRegistry configRegistry = new ConfigRegistry();

        // Summary row on top
        // The summary row is within the grid so it can be placed BETWEEN column
        // header and body
        // The body itself is a CompositeLayer with 1 column and 2 rows
        // The first row is the SummaryRowLayer configured as standalone
        // The second row is the body layer stack
        // for correct rendering of the row header you should use the
        // FixedSummaryRowHeaderLayer
        // which is adding the correct labels and shows a special configurable
        // label for the summary row
        final SummaryRowGridLayer gridLayerWithSummary = new SummaryRowGridLayer(
                dataProvider, configRegistry, propertyNames, propertyToLabelMap,
                true);

        NatTable natTable = new NatTable(panel, gridLayerWithSummary, false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DebugMenuConfiguration(natTable));
        natTable.configure();

        // Summary row at bottom
        // The grid doesn't contain a summary row
        // Instead create a CompositeLayer 1 column 2 rows
        // first row = GridLayer
        // second row = FixedGridSummaryRowLayer
        final SummaryRowGridLayer gridLayer = new SummaryRowGridLayer(
                dataProvider, configRegistry, propertyNames, propertyToLabelMap, false);

        // since the summary row should stay at a fixed position we need to add
        // a new composite row
        // this is necessary as also the row header cell needs to be fixed

        // create a standalone summary row
        // for a grid this is the FixedGridSummaryRowLayer
        FixedGridSummaryRowLayer summaryRowLayer =
                new FixedGridSummaryRowLayer(gridLayer.getBodyDataLayer(), gridLayer, configRegistry, false);
        summaryRowLayer.addConfiguration(
                new ExampleSummaryRowGridConfiguration(gridLayer.getBodyDataLayer().getDataProvider()));
        summaryRowLayer.setSummaryRowLabel("\u2211");

        // create a composition that has the grid on top and the summary row on
        // the bottom
        CompositeLayer composite = new CompositeLayer(1, 2);
        composite.setChildLayer("GRID", gridLayer, 0, 0);
        composite.setChildLayer(SUMMARY_REGION, summaryRowLayer, 0, 1);

        natTable = new NatTable(panel, composite, false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // configure a painter that renders a line on top of the summary row
        // this is necessary because the CompositeLayerPainter does not render
        // lines on the top of a region
        natTable.addOverlayPainter(new IOverlayPainter() {

            @Override
            public void paintOverlay(GC gc, ILayer layer) {
                // render a line on top of the summary row
                Color beforeColor = gc.getForeground();
                gc.setForeground(GUIHelper.COLOR_GRAY);
                int gridBorderY = gridLayer.getHeight() - 1;
                gc.drawLine(0, gridBorderY, layer.getWidth() - 1, gridBorderY);
                gc.setForeground(beforeColor);
            }
        });

        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DebugMenuConfiguration(natTable));
        natTable.configure();

        return panel;
    }

    private List<NumberValues> createNumberValueList() {
        List<NumberValues> result = new ArrayList<NumberValues>();

        for (int i = 0; i < 25; i++) {
            NumberValues nv = new NumberValues();
            nv.setColumnOneNumber(5);
            nv.setColumnTwoNumber(4);
            nv.setColumnThreeNumber(3);
            nv.setColumnFourNumber(1);
            nv.setColumnFiveNumber(1);
            result.add(nv);

            nv = new NumberValues();
            nv.setColumnOneNumber(1);
            nv.setColumnTwoNumber(1);
            nv.setColumnThreeNumber(2);
            nv.setColumnFourNumber(2);
            nv.setColumnFiveNumber(3);
            result.add(nv);

            nv = new NumberValues();
            nv.setColumnOneNumber(1);
            nv.setColumnTwoNumber(2);
            nv.setColumnThreeNumber(2);
            nv.setColumnFourNumber(3);
            nv.setColumnFiveNumber(3);
            result.add(nv);

            nv = new NumberValues();
            nv.setColumnOneNumber(1);
            nv.setColumnTwoNumber(2);
            nv.setColumnThreeNumber(4);
            nv.setColumnFourNumber(4);
            nv.setColumnFiveNumber(3);
            result.add(nv);

            nv = new NumberValues();
            nv.setColumnOneNumber(5);
            nv.setColumnTwoNumber(4);
            nv.setColumnThreeNumber(4);
            nv.setColumnFourNumber(4);
            nv.setColumnFiveNumber(7);
            result.add(nv);
        }

        return result;
    }

    /**
     * The body layer stack for the {@link _5124_SummaryRowPositionGridExample}.
     * Consists of
     * <ol>
     * <li>CompositeLayer - SummaryRowLayer included on top or bottom</li>
     * <li>ViewportLayer</li>
     * <li>SelectionLayer</li>
     * <li>ColumnHideShowLayer</li>
     * <li>ColumnReorderLayer</li>
     * <li>DataLayer</li>
     * </ol>
     */
    class SummaryRowBodyLayerStack extends AbstractLayerTransform {

        private final DataLayer bodyDataLayer;
        // private final SummaryRowLayer summaryRowLayer;
        private final ColumnReorderLayer columnReorderLayer;
        private final ColumnHideShowLayer columnHideShowLayer;
        private final SelectionLayer selectionLayer;
        private final ViewportLayer viewportLayer;

        public SummaryRowBodyLayerStack(
                IDataProvider dataProvider, ConfigRegistry configRegistry, boolean summaryRowOnTop) {
            this.bodyDataLayer = new DataLayer(dataProvider);

            this.columnReorderLayer = new ColumnReorderLayer(this.bodyDataLayer);
            this.columnHideShowLayer = new ColumnHideShowLayer(this.columnReorderLayer);
            this.selectionLayer = new SelectionLayer(this.columnHideShowLayer);
            this.viewportLayer = new ViewportLayer(this.selectionLayer);

            if (summaryRowOnTop) {
                // create a standalone SummaryRowLayer
                SummaryRowLayer summaryRowLayer = new SummaryRowLayer(
                        this.bodyDataLayer, configRegistry, false);
                // configure the SummaryRowLayer to be rendered standalone
                summaryRowLayer.setStandalone(true);
                summaryRowLayer.addConfiguration(
                        new ExampleSummaryRowGridConfiguration(this.bodyDataLayer.getDataProvider()));

                CompositeLayer composite = new CompositeLayer(1, 2);
                composite.setChildLayer(SUMMARY_REGION, summaryRowLayer, 0, 0);
                composite.setChildLayer(GridRegion.BODY, this.viewportLayer, 0, 1);

                setUnderlyingLayer(composite);
            }
            else {
                setUnderlyingLayer(this.viewportLayer);
            }

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
     * The {@link GridLayer} used by the
     * {@link _5124_SummaryRowPositionGridExample}.
     */
    class SummaryRowGridLayer extends GridLayer {

        public SummaryRowGridLayer(
                IDataProvider dataProvider, ConfigRegistry configRegistry,
                final String[] propertyNames, Map<String, String> propertyToLabelMap,
                boolean summaryRowOnTop) {
            super(true);
            init(dataProvider, configRegistry, propertyNames, propertyToLabelMap, summaryRowOnTop);
        }

        private void init(
                IDataProvider dataProvider, ConfigRegistry configRegistry,
                final String[] propertyNames, Map<String, String> propertyToLabelMap,
                final boolean summaryRowOnTop) {
            // Body
            SummaryRowBodyLayerStack bodyLayer =
                    new SummaryRowBodyLayerStack(dataProvider, configRegistry, summaryRowOnTop);

            SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

            // Column header
            IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(
                    propertyNames, propertyToLabelMap);
            ILayer columnHeaderLayer = new ColumnHeaderLayer(
                    new DefaultColumnHeaderDataLayer(columnHeaderDataProvider),
                    bodyLayer, selectionLayer);

            // Row header
            IDataProvider rowHeaderDataProvider =
                    new DefaultRowHeaderDataProvider(bodyLayer.getDataLayer().getDataProvider());
            final DataLayer rowHeaderDataLayer =
                    new DefaultRowHeaderDataLayer(rowHeaderDataProvider) {

            };

            ILayer rowHeaderLayer = null;
            if (summaryRowOnTop) {
                // in case of a summary row on top in the body region we use a
                // specific FixedSummaryRowHeaderLayer
                rowHeaderLayer = new FixedSummaryRowHeaderLayer(rowHeaderDataLayer,
                        bodyLayer, selectionLayer);
                ((FixedSummaryRowHeaderLayer) rowHeaderLayer).setSummaryRowLabel("\u2211");
            }
            else {
                rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer,
                        bodyLayer, selectionLayer);
            }

            // Corner
            ILayer cornerLayer = new CornerLayer(new DataLayer(
                    new DefaultCornerDataProvider(columnHeaderDataProvider,
                            rowHeaderDataProvider)), rowHeaderLayer,
                            columnHeaderLayer);

            setBodyLayer(bodyLayer);
            setColumnHeaderLayer(columnHeaderLayer);
            setRowHeaderLayer(rowHeaderLayer);
            setCornerLayer(cornerLayer);
        }

        public DataLayer getBodyDataLayer() {
            return ((SummaryRowBodyLayerStack) getBodyLayer()).getDataLayer();
        }
    }

    class ExampleSummaryRowGridConfiguration extends DefaultSummaryRowConfiguration {

        private final IDataProvider dataProvider;

        public ExampleSummaryRowGridConfiguration(IDataProvider dataProvider) {
            this.dataProvider = dataProvider;
            this.summaryRowBgColor = GUIHelper.COLOR_BLUE;
            this.summaryRowFgColor = GUIHelper.COLOR_WHITE;
        }

        @Override
        public void addSummaryProviderConfig(IConfigRegistry configRegistry) {
            // Labels are applied to the summary row and cells by default to
            // make configuration easier.
            // See the Javadoc for the SummaryRowLayer

            // Default summary provider
            configRegistry.registerConfigAttribute(
                    SummaryRowConfigAttributes.SUMMARY_PROVIDER,
                    new SummationSummaryProvider(this.dataProvider), DisplayMode.NORMAL,
                    SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);

            // Average summary provider for column index 2
            configRegistry.registerConfigAttribute(
                    SummaryRowConfigAttributes.SUMMARY_PROVIDER,
                    new AverageSummaryProvider(), DisplayMode.NORMAL,
                    SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + 4);
        }

        /**
         * Custom summary provider which averages out the contents of the column
         */
        class AverageSummaryProvider implements ISummaryProvider {
            @Override
            public Object summarize(int columnIndex) {
                double total = 0;
                int rowCount = ExampleSummaryRowGridConfiguration.this.dataProvider.getRowCount();

                for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                    Object dataValue = ExampleSummaryRowGridConfiguration.this.dataProvider.getDataValue(columnIndex,
                            rowIndex);
                    total = total + Double.parseDouble(dataValue.toString());
                }
                return "Avg: " + total / rowCount;
            }
        }
    }

}
