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
package org.eclipse.nebula.widgets.nattable.examples.examples._101_Data;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Big_data extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(800, 600, new Big_data());
	}
	
	@Override
	public String getDescription() {
		return
				"NatTable is designed to support very large data sets. This example shows a table with 500 columns and 1,000,000 rows. " +
				"NatTable tries hard to only ask for and render data that it currently needs to display.";
	}

	public Control createExampleControl(Composite parent) {
		DummyGridLayerStack layer = new DummyGridLayerStack(500, 1000000);
		
		// Widen row header so that all the row numbers are visible
		DataLayer rowHeaderDataLayer = (DataLayer) layer.getRowHeaderDataLayer();
		rowHeaderDataLayer.setColumnWidthByPosition(0, 80);
		
		return new NatTable(parent, layer);
	}
	
}
