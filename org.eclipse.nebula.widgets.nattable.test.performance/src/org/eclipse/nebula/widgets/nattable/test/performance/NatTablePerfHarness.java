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
package org.eclipse.nebula.widgets.nattable.test.performance;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class NatTablePerfHarness extends Shell {
	
	public NatTablePerfHarness(String dataFileName, Display display, int style) {
		super(display, style);
		
		createContents(dataFileName);
		final GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);
	}
	
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	private void createContents(String dataFileName) {
		setText("NatTable Perf Harness");
		setSize(5000, 1000);

		final Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new FillLayout());
		
//		NatTableDataGenerator dataGenerator = new NatTableDataGenerator();
		long start, stop;
		start = System.currentTimeMillis();
		
		// FIXME hook up data provider to grid layer
		
//		IDataProvider dataProvider = new InternalDataProvider(dataGenerator.loadData(dataFileName));
//		stop = System.currentTimeMillis();
//		System.out.println((stop - start) + " milliseconds to load " + dataFileName);
//		start = System.currentTimeMillis();
//		DefaultRowHeaderConfig rowConfig = new DefaultRowHeaderConfig();
//		rowConfig.setRowHeaderColumnCount(1);
		
		/*DefaultNatTableModel natModel = new DefaultNatTableModel();
		natModel.setBodyConfig(new DefaultBodyConfig(dataProvider));
		natModel.getBodyConfig().getColumnWidthConfig().setDefaultSize(75);
		natModel.getBodyConfig().getRowHeightConfig().setDefaultSize(20);
		natModel.setRowHeaderConfig(rowConfig);
		natModel.setFullRowSelection(true);
		natModel.setEnableMoveColumn(true);
		natModel.getBodyConfig().getColumnWidthConfig().setResizableByDefault(true);*/
		new NatTable(composite);
		stop = System.currentTimeMillis();
		System.out.println((stop - start) + " milliseconds to load data into NatTable");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Missing input file.");
			System.exit(-1);
		}
		try {
			Display display = Display.getDefault();
			NatTablePerfHarness shell = new NatTablePerfHarness(args[0], display, SWT.SHELL_TRIM);
			shell.setLayout(new GridLayout());
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (	Exception e) {
			e.printStackTrace();

		}
	}
}
