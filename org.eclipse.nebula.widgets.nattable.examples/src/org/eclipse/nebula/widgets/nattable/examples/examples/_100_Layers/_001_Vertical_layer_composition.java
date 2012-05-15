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
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _001_Vertical_layer_composition extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _001_Vertical_layer_composition());
	}
	
	@Override
	public String getDescription() {
		return
				"NatTable encapsulates functionality into layers that can be stacked on top of each other to provide augmented behavior. " +
				"This example shows a basic DataLayer that has a SelectionLayer and ViewportLayer stacked on top of it. The " +
				"SelectionLayer tracks what cells are selected and enables those cells to be displayed using a different style according " +
				"to their selected state. The ViewportLayer enables the underlying layer to be scrolled. Just for the heck of it we are " +
				"scrolling over a 1,000,000 column by 1,000,000 row data layer.";
	}
	
	public Control createExampleControl(Composite parent) {
		ViewportLayer layer = new ViewportLayer(new SelectionLayer(new DataLayer(new DummyBodyDataProvider(1000000, 1000000))));
		layer.setRegionName(GridRegion.BODY);
		return new NatTable(parent, layer);
	}
	
}
