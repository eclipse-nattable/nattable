/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._100_Layers;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.config.DefaultFreezeGridBindings;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This fixture creates a simple, minimal 3x3 table with resizable columns whose
 * cells are of the form 'Row X, Col Y'.
 */
public class FreezableGridExample extends AbstractNatExample {
	
	@Override
	public String getDescription() {
		return
				"This example demonstrates the column and row freezing functionality of NatTable.\n" +
				"\n" +
				"* FREEZE COLUMNS AND ROWS by selecting a cell and using ctrl-shift-f. The columns to the left and the rows to the right " +
				"of the selected cell will be frozen such that they will always remain on screen even when the viewport is scrolled.\n" +
				"* UNFREEZE COLUMNS AND ROWS with ctrl-shift-u.";
	}
	
	public Control createExampleControl(Composite parent) {
		// Body
		final DummyBodyDataProvider bodyDataProvider = new DummyBodyDataProvider(20, 1000);
		final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		final DefaultBodyLayerStack bodyLayer = new DefaultBodyLayerStack(bodyDataLayer);
		final SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();
		final FreezeLayer freezeLayer = new FreezeLayer(selectionLayer);
	    final CompositeFreezeLayer compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, bodyLayer.getViewportLayer(), selectionLayer);
	    
	    // Column header
		final IDataProvider columnHeaderDataProvider = new DummyColumnHeaderDataProvider(bodyDataProvider);
		final ILayer columnHeaderLayer = new ColumnHeaderLayer(new DefaultColumnHeaderDataLayer(columnHeaderDataProvider), compositeFreezeLayer, selectionLayer);
		
		// Row header
		final IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		final ILayer rowHeaderLayer = new RowHeaderLayer(new DefaultRowHeaderDataLayer(rowHeaderDataProvider), compositeFreezeLayer, selectionLayer);
	    
		// Corner
		final DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		final CornerLayer cornerLayer = new CornerLayer(new DataLayer(cornerDataProvider), rowHeaderLayer, columnHeaderLayer);
		
	    // Grid
	    final GridLayer gridLayer = new GridLayer(compositeFreezeLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);
	    
		NatTable natTable = new NatTable(parent, gridLayer, false);

		// Configuration
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
		natTable.addConfiguration(new DefaultFreezeGridBindings());
		
		natTable.configure();
		
		return natTable;
	}
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new FreezableGridExample());
	}
	
}
