/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._501_Data;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyModifiableBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _5012_PercentageSizingDataLayerExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 850,
                new _5012_PercentageSizingDataLayerExample());
    }

    @Override
    public String getDescription() {
        return "This example shows some examples for simple NatTable compositions that "
                + "are using percentage sizing.\n\n"
                + "First table:\tAll columns and all rows have the same size by calculating the size dependent on the available width\n"
                + "Second table:\tAll columns have fixed percentage values (25% / 25% / 50%)\n"
                + "Third table:\tColumn 1 and 3 are configured to take 40% of the available space each, column 2 will take the rest\n"
                + "Fourth table:\tColumn 1 and 2 are configured for 100 pixels width, column 3 will take the rest";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        Composite simplePanel = new Composite(panel, SWT.NONE);
        simplePanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(simplePanel);

        Composite buttonPanel = new Composite(panel, SWT.NONE);
        buttonPanel.setLayout(new RowLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        final DummyModifiableBodyDataProvider dataProvider =
                new DummyModifiableBodyDataProvider(3, 2);

        // example for percentage calculation with default sizing
        // all columns will be same size while the NatTable itself will have
        // 100%
        final DataLayer n1DataLayer = new DataLayer(dataProvider);
        n1DataLayer.setColumnPercentageSizing(true);
        n1DataLayer.setRowPercentageSizing(true);
        SelectionLayer layer = new SelectionLayer(n1DataLayer);
        layer.setRegionName(GridRegion.BODY);
        // use different style bits to avoid rendering of inactive scrollbars
        // for small table when using percentage sizing, typically there should
        // be no scrollbars, as the table should take the available space
        // Note: The enabling/disabling and showing of the scrollbars is handled
        // by the ViewportLayer. Without the ViewportLayer the scrollbars will
        // always be visible with the default style bits of NatTable.
        final NatTable n1 = new NatTable(
                simplePanel, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, layer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(n1);

        // example for fixed percentage sizing
        // ensure that the sum of column sizes is not greater than 100
        final DataLayer n2DataLayer = new DataLayer(dataProvider);
        n2DataLayer.setColumnWidthPercentageByPosition(0, 25);
        n2DataLayer.setColumnWidthPercentageByPosition(1, 25);
        n2DataLayer.setColumnWidthPercentageByPosition(2, 50);
        layer = new SelectionLayer(n2DataLayer);
        layer.setRegionName(GridRegion.BODY);
        // use different style bits to avoid rendering of inactive scrollbars
        // for small table when using percentage sizing, typically there should
        // be no scrollbars, as the table should take the available space
        // Note: The enabling/disabling and showing of the scrollbars is handled
        // by the ViewportLayer. Without the ViewportLayer the scrollbars will
        // always be visible with the default style bits of NatTable.
        final NatTable n2 = new NatTable(
                simplePanel, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, layer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(n2);

        // example for mixed percentage sizing
        // configure not every column with the exact percentage value, this way
        // the columns for which no exact values are set will use the remaining
        // space
        final DataLayer n3DataLayer = new DataLayer(dataProvider);
        n3DataLayer.setColumnPercentageSizing(true);
        n3DataLayer.setColumnWidthPercentageByPosition(0, 40);
        n3DataLayer.setColumnWidthPercentageByPosition(2, 40);
        layer = new SelectionLayer(n3DataLayer);
        layer.setRegionName(GridRegion.BODY);
        // use different style bits to avoid rendering of inactive scrollbars
        // for small table when using percentage sizing, typically there should
        // be no scrollbars, as the table should take the available space
        // Note: The enabling/disabling and showing of the scrollbars is handled
        // by the ViewportLayer. Without the ViewportLayer the scrollbars will
        // always be visible with the default style bits of NatTable.
        final NatTable n3 = new NatTable(
                simplePanel, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, layer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(n3);

        // example for mixed fixed/percentage sizing
        // configure not every column with the exact percentage value, this way
        // the columns for which no exact values are set will use the remaining
        // space
        final DataLayer mixDataLayer = new DataLayer(dataProvider);
        mixDataLayer.setColumnPercentageSizing(true);
        mixDataLayer.setColumnPercentageSizing(0, false);
        mixDataLayer.setColumnPercentageSizing(1, false);
        mixDataLayer.setColumnWidthByPosition(0, 100);
        mixDataLayer.setColumnWidthByPosition(1, 100);
        layer = new SelectionLayer(mixDataLayer);
        layer.setRegionName(GridRegion.BODY);
        // use different style bits to avoid rendering of inactive scrollbars
        // for small table when using percentage sizing, typically there should
        // be no scrollbars, as the table should take the available space
        // Note: The enabling/disabling and showing of the scrollbars is handled
        // by the ViewportLayer. Without the ViewportLayer the scrollbars will
        // always be visible with the default style bits of NatTable.
        final NatTable mix = new NatTable(
                simplePanel, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, layer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mix);

        Button addColumnButton = new Button(buttonPanel, SWT.PUSH);
        addColumnButton.setText("add column - no width");
        addColumnButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dataProvider.setColumnCount(dataProvider.getColumnCount() + 1);
                n1.refresh();
                n2.refresh();
                n3.refresh();
                mix.refresh();
            }
        });

        Button addColumnButton2 = new Button(buttonPanel, SWT.PUSH);
        addColumnButton2.setText("add column - 20 percent width");
        addColumnButton2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dataProvider.setColumnCount(dataProvider.getColumnCount() + 1);

                n1DataLayer.setColumnWidthPercentageByPosition(
                        dataProvider.getColumnCount() - 1, 20);
                n2DataLayer.setColumnWidthPercentageByPosition(
                        dataProvider.getColumnCount() - 1, 20);
                n3DataLayer.setColumnWidthPercentageByPosition(
                        dataProvider.getColumnCount() - 1, 20);
                mixDataLayer.setColumnWidthPercentageByPosition(
                        dataProvider.getColumnCount() - 1, 20);

                n1.refresh();
                n2.refresh();
                n3.refresh();
                mix.refresh();
            }
        });

        return panel;
    }

}