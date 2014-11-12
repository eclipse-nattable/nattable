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
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.NumberValues;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.DefaultSummaryRowConfiguration;
import org.eclipse.nebula.widgets.nattable.summaryrow.ISummaryProvider;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummationSummaryProvider;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example that demonstrates the usage of the SummaryRow in NatTable and how to
 * position the summary row at fixed locations in a layer composition.
 */
public class _5123_SummaryRowPositionExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _5123_SummaryRowPositionExample());
    }

    @Override
    public String getDescription() {
        return "This example demonstrates how to add a summary row to fixed locations in the table.\n"
                + "\n"
                + "1. position the summary row at the top\n"
                + "2. position the summary row at the bottom.";
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

        IColumnPropertyAccessor<NumberValues> cpa =
                new ReflectiveColumnPropertyAccessor<NumberValues>(propertyNames);
        IDataProvider dataProvider =
                new ListDataProvider<NumberValues>(createNumberValueList(), cpa);

        ConfigRegistry configRegistry = new ConfigRegistry();

        // Summary row fixed on top
        DataLayer dataLayer = new DataLayer(dataProvider);

        ViewportLayer viewportLayer = new ViewportLayer(dataLayer);

        // Plug in the SummaryRowLayer
        SummaryRowLayer summaryRowLayer = new SummaryRowLayer(dataLayer, configRegistry, false);
        // configure the SummaryRowLayer to be rendered standalone
        summaryRowLayer.setStandalone(true);

        CompositeLayer composite = new CompositeLayer(1, 2);
        composite.setChildLayer("SUMMARY", summaryRowLayer, 0, 0);
        composite.setChildLayer(GridRegion.BODY, viewportLayer, 0, 1);

        NatTable natTable = new NatTable(panel, composite, false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // Configure custom summary formula for a column
        natTable.addConfiguration(new ExampleSummaryRowConfiguration(dataProvider));
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.setConfigRegistry(configRegistry);
        natTable.configure();

        // Summary row fixed at the bottom
        dataLayer = new DataLayer(dataProvider);
        // IMPORTANT:
        // since the summary row layer is to the bottom of the viewport layer
        // we need to configure a GridLineCellLayerPainter that clips the top
        // cell. This means the body data layer is clipped at the bottom since
        // the painter is used globally
        dataLayer.setLayerPainter(new GridLineCellLayerPainter(false, true));

        viewportLayer = new ViewportLayer(dataLayer);

        // Plug in the SummaryRowLayer
        summaryRowLayer = new SummaryRowLayer(dataLayer, configRegistry, false);
        // configure the SummaryRowLayer to be rendered standalone
        summaryRowLayer.setStandalone(true);

        composite = new CompositeLayer(1, 2);
        composite.setChildLayer(GridRegion.BODY, viewportLayer, 0, 0);
        composite.setChildLayer("SUMMARY", summaryRowLayer, 0, 1);

        natTable = new NatTable(panel, composite, false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // Configure custom summary formula for a column
        natTable.addConfiguration(new ExampleSummaryRowConfiguration(dataProvider));
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.setConfigRegistry(configRegistry);
        natTable.configure();

        return panel;
    }

    private List<NumberValues> createNumberValueList() {
        List<NumberValues> result = new ArrayList<NumberValues>();

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

        for (int i = 0; i < 10; i++) {
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

    class ExampleSummaryRowConfiguration extends DefaultSummaryRowConfiguration {

        private final IDataProvider dataProvider;

        public ExampleSummaryRowConfiguration(IDataProvider dataProvider) {
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
                int rowCount = ExampleSummaryRowConfiguration.this.dataProvider.getRowCount();

                for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                    Object dataValue = ExampleSummaryRowConfiguration.this.dataProvider.getDataValue(columnIndex,
                            rowIndex);
                    total = total + Double.parseDouble(dataValue.toString());
                }
                return "Avg: " + total / rowCount;
            }
        }

    }
}
