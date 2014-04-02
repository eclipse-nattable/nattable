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
package org.eclipse.nebula.widgets.nattable.examples._500_Layers;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyModifiableBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _5013_PercentageSizingGridExample extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 850, new _5013_PercentageSizingGridExample());
	}

	@Override
	public String getDescription() {
		return "This example shows some examples for NatTable grid compositions that "
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
		
		Composite gridPanel = new Composite(panel, SWT.NONE);
		gridPanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);
		
		Composite buttonPanel = new Composite(panel, SWT.NONE);
		buttonPanel.setLayout(new RowLayout());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);
		
		final DummyModifiableBodyDataProvider dataProvider = new DummyModifiableBodyDataProvider(3, 2);
		
		//example for percentage calculation with default sizing in a grid
		//all columns will be same size while the NatTable itself will have 100%
		SimpleGridLayer gridLayer = new SimpleGridLayer();
		final DataLayer n4DataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		n4DataLayer.setColumnPercentageSizing(true);
		n4DataLayer.setRowPercentageSizing(true);
		//use different style bits to avoid rendering of inactive scrollbars for small table
		//when using percentage sizing, typically there should be no scrollbars, as the table
		//should take the available space
		//Note: The enabling/disabling and showing of the scrollbars is handled by the ViewportLayer.
		//		Without the ViewportLayer the scrollbars will always be visible with the default
		//		style bits of NatTable.
		final NatTable n4 = new NatTable(
				gridPanel, 
				SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, 
				gridLayer, false);
		n4.addConfiguration(new DefaultNatTableStyleConfiguration());
		n4.addConfiguration(new HeaderMenuConfiguration(n4));
		n4.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(n4);

		//example for fixed percentage sizing in a grid
		//ensure that the sum of column sizes is not greater than 100
		gridLayer = new SimpleGridLayer();
		final DataLayer n5DataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		n5DataLayer.setColumnWidthByPosition(0, 25);
		n5DataLayer.setColumnWidthByPosition(1, 25);
		n5DataLayer.setColumnWidthByPosition(2, 50);
		n5DataLayer.setColumnPercentageSizing(true);
		//use different style bits to avoid rendering of inactive scrollbars for small table
		//when using percentage sizing, typically there should be no scrollbars, as the table
		//should take the available space
		//Note: The enabling/disabling and showing of the scrollbars is handled by the ViewportLayer.
		//		Without the ViewportLayer the scrollbars will always be visible with the default
		//		style bits of NatTable.
		final NatTable n5 = new NatTable(
				gridPanel, 
				SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, 
				gridLayer, false);
		n5.addConfiguration(new DefaultNatTableStyleConfiguration());
		n5.addConfiguration(new HeaderMenuConfiguration(n5));
		n5.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(n5);
		
		//example for mixed percentage sizing in a grid
		//configure not every column with the exact percentage value, this way the columns for which
		//no exact values are set will use the remaining space
		gridLayer = new SimpleGridLayer();
		final DataLayer n6DataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		n6DataLayer.setColumnWidthByPosition(0, 20);
		n6DataLayer.setColumnWidthByPosition(2, 20);
		n6DataLayer.setColumnPercentageSizing(true);
		//use different style bits to avoid rendering of inactive scrollbars for small table
		//when using percentage sizing, typically there should be no scrollbars, as the table
		//should take the available space
		//Note: The enabling/disabling and showing of the scrollbars is handled by the ViewportLayer.
		//		Without the ViewportLayer the scrollbars will always be visible with the default
		//		style bits of NatTable.
		final NatTable n6 = new NatTable(
				gridPanel, 
				SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, 
				gridLayer, false);
		n6.addConfiguration(new DefaultNatTableStyleConfiguration());
		n6.addConfiguration(new HeaderMenuConfiguration(n6));
		n6.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(n6);

		//example for mixed fixed/percentage sizing in a grid
		//configure not every column with the exact percentage value, this way the columns for which
		//no exact values are set will use the remaining space
		gridLayer = new SimpleGridLayer();
		final DataLayer mixGridDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		mixGridDataLayer.setColumnPercentageSizing(true);
		mixGridDataLayer.setColumnPercentageSizing(0, false);
		mixGridDataLayer.setColumnPercentageSizing(1, false);
		mixGridDataLayer.setColumnWidthByPosition(0, 100);
		mixGridDataLayer.setColumnWidthByPosition(1, 100);
		//use different style bits to avoid rendering of inactive scrollbars for small table
		//when using percentage sizing, typically there should be no scrollbars, as the table
		//should take the available space
		//Note: The enabling/disabling and showing of the scrollbars is handled by the ViewportLayer.
		//		Without the ViewportLayer the scrollbars will always be visible with the default
		//		style bits of NatTable.
		final NatTable mixGrid = new NatTable(
				gridPanel, 
				SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, 
				gridLayer, false);
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
				
				n4DataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				n5DataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				n6DataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				mixGridDataLayer.setColumnWidthPercentageByPosition(dataProvider.getColumnCount()-1, 20);
				
				n4.refresh();
				n5.refresh();
				n6.refresh();
				mixGrid.refresh();
			}
		});
		
		return panel;
	}
	
	/**
	 * Simple grid implementation that doesn't contain a ViewportLayer in the body layer stack.
	 * This is because it is used for percentage sizing and we do not want to show scrollbars.
	 * 
	 * @author Dirk Fauth
	 *
	 */
	class SimpleGridLayer extends GridLayer {

		IDataProvider bodyDataProvider = new DummyModifiableBodyDataProvider(3, 2);
		DataLayer bodyDataLayer;
		
		SelectionLayer selectionLayer;
		
		protected SimpleGridLayer() {
			super(true);

			//create and set the body layer stack
			this.bodyDataLayer = new DataLayer(bodyDataProvider);
			ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
			ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
			selectionLayer = new SelectionLayer(columnHideShowLayer);
			setBodyLayer(selectionLayer);

			//create and set the column header layer stack
			IDataProvider columnHeaderDataProvider = new DummyColumnHeaderDataProvider(bodyDataProvider);
			DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
			ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, selectionLayer, selectionLayer);
			setColumnHeaderLayer(columnHeaderLayer);

			//create and set the row header layer stack
			IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
			DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
			ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, selectionLayer, selectionLayer);
			setRowHeaderLayer(rowHeaderLayer);

			//create and set the corner layer stack
			IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
			DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
			ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
			setCornerLayer(cornerLayer);
		}
		
		public IUniqueIndexLayer getBodyDataLayer() {
			return bodyDataLayer;
		}

	}
}