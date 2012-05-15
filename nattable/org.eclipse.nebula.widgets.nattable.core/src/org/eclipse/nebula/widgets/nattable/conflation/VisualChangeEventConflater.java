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
package org.eclipse.nebula.widgets.nattable.conflation;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;

/**
 * Gathers all the VisualChangeEvents. When its run, it refreshes/repaints the table. 
 *
 */
public class VisualChangeEventConflater extends AbstractEventConflater { 

	private final NatTable natTable;

	public VisualChangeEventConflater(NatTable ownerLayer) {
		natTable = ownerLayer;
	}

	@Override
	public void addEvent(ILayerEvent event) {
		if(event instanceof IVisualChangeEvent){
			super.addEvent(event);
		}
	}
	
	@Override
	public Runnable getConflaterTask() {
		return new Runnable() {

			public void run() {
				if (queue.size() > 0) {
					natTable.getDisplay().asyncExec(new Runnable() {
						public void run() {
							natTable.updateResize();
						}
					});

					clearQueue();
				}
			}
		};
	}

}
