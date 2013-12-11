/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._100_Layers._101_Header;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class RowHeaderSelectionDataLayerExample extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new RowHeaderSelectionDataLayerExample());
	}

	public Control createExampleControl(Composite parent) {
		DummyBodyDataProvider bodyDataProvider = new DummyBodyDataProvider(1000000, 1000000);
		SelectionLayer selectionLayer = new SelectionLayer(new DataLayer(bodyDataProvider));
		
		DataLayer rowHeaderDataLayer = new DataLayer(new DefaultRowHeaderDataProvider(bodyDataProvider));
		rowHeaderDataLayer.setDefaultColumnWidth(40);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, selectionLayer, selectionLayer);
		
		CompositeLayer compositeLayer = new CompositeLayer(2, 1);
		compositeLayer.setChildLayer(GridRegion.ROW_HEADER, rowHeaderLayer, 0, 0);
		compositeLayer.setChildLayer(GridRegion.BODY, selectionLayer, 1, 0);
		
		return new NatTable(parent, compositeLayer);
	}
	
}
