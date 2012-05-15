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
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public abstract class AbstractLayerPerformanceTest {

	public static final long DEFAULT_THRESHOLD = 100;
	
	private Shell shell;
	private GC gc;
	
	private long expectedTimeInMillis;
	
	protected ILayer layer;

	public Shell getShell() {
		return shell;
	}
	
	public GC getGC() {
		return gc;
	}
	
	public long getExpectedTimeInMillis() {
		return expectedTimeInMillis;
	}
	
	public void setExpectedTimeInMillis(long expectedTimeInMillis) {
		this.expectedTimeInMillis = expectedTimeInMillis;
	}
	
	@Before
	public void setup() {
		layer = null;
		expectedTimeInMillis = DEFAULT_THRESHOLD;
		
		shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setSize(2000, 1000);
		
		gc = new GC(shell);
	}
	
	@After
	public void tearDown() {
		Assert.assertNotNull("Layer was not set", layer);
		
		NatTable natTable = new NatTable(getShell(), layer);
		
		getShell().setVisible(true);
		
		// Start!
		long startTimeInMillis = System.currentTimeMillis();
		
		natTable.getLayerPainter().paintLayer(natTable, getGC(), 0, 0, new Rectangle(0, 0, natTable.getWidth(), natTable.getHeight()), natTable.getConfigRegistry());
		
		// Stop!
		long stopTimeInMillis = System.currentTimeMillis();
		long actualTimeInMillis = stopTimeInMillis - startTimeInMillis;
		
		gc.dispose();
		shell.dispose();
		
		System.out.println("duration = " + actualTimeInMillis + " milliseconds");
		Assert.assertTrue("Expected to take less than " + expectedTimeInMillis + " milliseconds but took " + actualTimeInMillis + " milliseconds", actualTimeInMillis < expectedTimeInMillis);
	}
	
}
