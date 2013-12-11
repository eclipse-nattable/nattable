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
package org.eclipse.nebula.widgets.nattable.examples.examples._100_Layers._900_test.elemental;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyModifiableBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PercentageSizingDataLayerExample extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 850, new PercentageSizingDataLayerExample());
	}

	@Override
	public Control createExampleControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);
		
		Composite simplePanel = new Composite(panel, SWT.NONE);
		simplePanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(simplePanel);
		
		Composite gridPanel = new Composite(panel, SWT.NONE);
		gridPanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);
		
		Composite buttonPanel = new Composite(panel, SWT.NONE);
		buttonPanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonPanel);
		
		final DummyModifiableBodyDataProvider dataProvider = new DummyModifiableBodyDataProvider(3, 2);
		
		//example for percentage calculation with default sizing
		//all columns will be same size while the NatTable itself will have 100%
		final DataLayer n1DataLayer = new DataLayer(dataProvider);
		n1DataLayer.setColumnPercentageSizing(true);
		n1DataLayer.setRowPercentageSizing(true);
		ViewportLayer layer = new ViewportLayer(new SelectionLayer(n1DataLayer));
		layer.setRegionName(GridRegion.BODY);
		final NatTable n1 = new NatTable(simplePanel, layer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(n1);
		
		//example for fixed percentage sizing
		//ensure that the sum of column sizes is not greater than 100
		final DataLayer n2DataLayer = new DataLayer(dataProvider);
		n2DataLayer.setColumnWidthPercentageByPosition(0, 25);
		n2DataLayer.setColumnWidthPercentageByPosition(1, 25);
		n2DataLayer.setColumnWidthPercentageByPosition(2, 50);
		layer = new ViewportLayer(new SelectionLayer(n2DataLayer));
		layer.setRegionName(GridRegion.BODY);
		final NatTable n2 = new NatTable(simplePanel, layer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(n2);

		//example for mixed percentage sizing
		//configure not every column with the exact percentage value, this way the columns for which
		//no exact values are set will use the remaining space
		final DataLayer n3DataLayer = new DataLayer(dataProvider);
		n3DataLayer.setColumnPercentageSizing(true);
		n3DataLayer.setColumnWidthPercentageByPosition(0, 40);
		n3DataLayer.setColumnWidthPercentageByPosition(2, 40);
		layer = new ViewportLayer(new SelectionLayer(n3DataLayer));
		layer.setRegionName(GridRegion.BODY);
		final NatTable n3 = new NatTable(simplePanel, layer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(n3);

		//example for mixed fixed/percentage sizing
		//configure not every column with the exact percentage value, this way the columns for which
		//no exact values are set will use the remaining space
		final DataLayer mixDataLayer = new DataLayer(dataProvider);
		mixDataLayer.setColumnPercentageSizing(true);
		mixDataLayer.setColumnPercentageSizing(0, false);
		mixDataLayer.setColumnPercentageSizing(1, false);
		mixDataLayer.setColumnWidthByPosition(0, 100);
		mixDataLayer.setColumnWidthByPosition(1, 100);
		layer = new ViewportLayer(new SelectionLayer(mixDataLayer));
		layer.setRegionName(GridRegion.BODY);
		final NatTable mix = new NatTable(simplePanel, layer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mix);
		
		//example for percentage calculation with default sizing in a grid
		//all columns will be same size while the NatTable itself will have 100%
		DummyGridLayerStack gridLayer = new DummyGridLayerStack(dataProvider);
		final DataLayer n4DataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		n4DataLayer.setColumnPercentageSizing(true);
		n4DataLayer.setRowPercentageSizing(true);
		final NatTable n4 = new NatTable(gridPanel, gridLayer, false);
		n4.addConfiguration(new DefaultNatTableStyleConfiguration());
		n4.addConfiguration(new HeaderMenuConfiguration(n4));
		n4.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(n4);

		//example for fixed percentage sizing in a grid
		//ensure that the sum of column sizes is not greater than 100
		gridLayer = new DummyGridLayerStack(dataProvider);
		final DataLayer n5DataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		n5DataLayer.setColumnWidthByPosition(0, 25);
		n5DataLayer.setColumnWidthByPosition(1, 25);
		n5DataLayer.setColumnWidthByPosition(2, 50);
		n5DataLayer.setColumnPercentageSizing(true);
		final NatTable n5 = new NatTable(gridPanel, gridLayer, false);
		n5.addConfiguration(new DefaultNatTableStyleConfiguration());
		n5.addConfiguration(new HeaderMenuConfiguration(n5));
		n5.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(n5);
		
		//example for mixed percentage sizing in a grid
		//configure not every column with the exact percentage value, this way the columns for which
		//no exact values are set will use the remaining space
		gridLayer = new DummyGridLayerStack(dataProvider);
		final DataLayer n6DataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		n6DataLayer.setColumnWidthByPosition(0, 20);
		n6DataLayer.setColumnWidthByPosition(2, 20);
		n6DataLayer.setColumnPercentageSizing(true);
		final NatTable n6 = new NatTable(gridPanel, gridLayer, false);
		n6.addConfiguration(new DefaultNatTableStyleConfiguration());
		n6.addConfiguration(new HeaderMenuConfiguration(n6));
		n6.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(n6);

		//example for mixed fixed/percentage sizing in a grid
		//configure not every column with the exact percentage value, this way the columns for which
		//no exact values are set will use the remaining space
		gridLayer = new DummyGridLayerStack(dataProvider);
		final DataLayer mixGridDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		mixGridDataLayer.setColumnPercentageSizing(true);
		mixGridDataLayer.setColumnPercentageSizing(0, false);
		mixGridDataLayer.setColumnPercentageSizing(1, false);
		mixGridDataLayer.setColumnWidthByPosition(0, 100);
		mixGridDataLayer.setColumnWidthByPosition(1, 100);
		final NatTable mixGrid = new NatTable(gridPanel, gridLayer, false);
		mixGrid.addConfiguration(new DefaultNatTableStyleConfiguration());
		mixGrid.addConfiguration(new HeaderMenuConfiguration(mixGrid));
		mixGrid.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mixGrid);
		
		
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
				n4.refresh();
				n5.refresh();
				n6.refresh();
				mixGrid.refresh();
			}
		});
		
		Button addColumnButton2 = new Button(buttonPanel, SWT.PUSH);
		addColumnButton2.setText("add column - 20 percent width");
		addColumnButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dataProvider.setColumnCount(dataProvider.getColumnCount() + 1);
				
				n1DataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				n2DataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				n3DataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				mixDataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				n4DataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				n5DataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				n6DataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				mixGridDataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				
				n1.refresh();
				n2.refresh();
				n3.refresh();
				mix.refresh();
				n4.refresh();
				n5.refresh();
				n6.refresh();
				mixGrid.refresh();
			}
		});
		
		return panel;
	}
	
}