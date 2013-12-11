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
package org.eclipse.nebula.widgets.nattable.examples._300_Data;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.PersistentNatExampleWrapper;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyModifiableBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PersistentPercentageSizingDataLayerExample extends PersistentNatExampleWrapper {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 650, new PersistentPercentageSizingDataLayerExample());
	}
	
	public PersistentPercentageSizingDataLayerExample() {
		super(new Test());
	}
}

class Test extends AbstractNatExample {
	
	@Override
	public Control createExampleControl(Composite parent) {
		parent.setLayout(new GridLayout());
		
		final DummyModifiableBodyDataProvider dataProvider = new DummyModifiableBodyDataProvider(3, 2);
		
		DummyGridLayerStack gridLayer = new DummyGridLayerStack(dataProvider);
		final DataLayer n5DataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		n5DataLayer.setColumnWidthPercentageByPosition(0, 25);
		n5DataLayer.setColumnWidthPercentageByPosition(1, 25);
		n5DataLayer.setColumnWidthPercentageByPosition(2, 50);
		n5DataLayer.setColumnPercentageSizing(true);
		
		final NatTable n5 = new NatTable(parent, gridLayer, false);
		n5.addConfiguration(new DefaultNatTableStyleConfiguration());
		n5.addConfiguration(new HeaderMenuConfiguration(n5));
		n5.configure();
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(n5);
		
		//add buttons
		Button b1 = new Button(parent, SWT.PUSH);
		b1.setText("change");
		b1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				n5DataLayer.setColumnWidthPercentageByPosition(0, 50);
				n5DataLayer.setColumnWidthPercentageByPosition(1, 25);
				n5DataLayer.setColumnWidthPercentageByPosition(2, 25);
			}
		});
		Button b2 = new Button(parent, SWT.PUSH);
		b2.setText("default");
		b2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				n5DataLayer.setColumnWidthPercentageByPosition(0, 25);
				n5DataLayer.setColumnWidthPercentageByPosition(1, 25);
				n5DataLayer.setColumnWidthPercentageByPosition(2, 50);
			}
		});
		
		return n5;
	}
}
	
