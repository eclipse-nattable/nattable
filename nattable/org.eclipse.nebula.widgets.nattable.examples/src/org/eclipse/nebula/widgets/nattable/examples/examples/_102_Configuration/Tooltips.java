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
package org.eclipse.nebula.widgets.nattable.examples.examples._102_Configuration;


import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

public class Tooltips extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new Tooltips());
	}

	public Control createExampleControl(Composite parent) {
		NatTable natTable = new NatTable(parent, new DummyGridLayerStack(20, 100));
		attachToolTip(natTable);
		return natTable;
	}
	
	private void attachToolTip(NatTable natTable) {
		DefaultToolTip toolTip = new ExampleNatTableToolTip(natTable);
		toolTip.setBackgroundColor(natTable.getDisplay().getSystemColor(SWT.COLOR_RED));
		toolTip.setPopupDelay(500);
		toolTip.activate();
		toolTip.setShift(new Point(10, 10));
	}

	private class ExampleNatTableToolTip extends DefaultToolTip{

		private NatTable natTable;
		
		public ExampleNatTableToolTip(NatTable natTable) {
			super(natTable, ToolTip.NO_RECREATE, false);
			this.natTable = natTable;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.ToolTip#getToolTipArea(org.eclipse.swt.widgets.Event)
		 * 
		 * Implementation here means the tooltip is not redrawn unless mouse hover moves outside of the
		 * current cell (the combination of ToolTip.NO_RECREATE style and override of this method).
		 */
		protected Object getToolTipArea(Event event) {
			int col = natTable.getColumnPositionByX(event.x);
			int row = natTable.getRowPositionByY(event.y);
			
			return new Point(col, row);
		}
		
		protected String getText(Event event) {
			int col = natTable.getColumnPositionByX(event.x);
			int row = natTable.getRowPositionByY(event.y);
			
			return "Cell Position: ("+col+","+row+")";
		}
		
		protected Composite createToolTipContentArea(Event event, Composite parent) {
			// This is where you could get really creative with your tooltips...
			return super.createToolTipContentArea(event, parent);
		}
	}
}
