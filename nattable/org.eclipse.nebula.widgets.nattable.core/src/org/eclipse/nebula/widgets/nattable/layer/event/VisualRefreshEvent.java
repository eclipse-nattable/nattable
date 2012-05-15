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
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.Arrays;
import java.util.Collection;


import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

public class VisualRefreshEvent implements IVisualChangeEvent {

	private ILayer layer;

	public VisualRefreshEvent(ILayer layer) {
		this.layer = layer;
	}
	
	protected VisualRefreshEvent(VisualRefreshEvent event) {
		this.layer = event.layer;
	}
	
	public ILayer getLayer() {
		return layer;
	}

	public boolean convertToLocal(ILayer localLayer) {
		layer = localLayer;
		
		return true;
	}
	
	public Collection<Rectangle> getChangedPositionRectangles() {
		return Arrays.asList(new Rectangle[] { new Rectangle(0, 0, layer.getColumnCount(), layer.getRowCount()) });
	}
	
	public ILayerEvent cloneEvent() {
		return new VisualRefreshEvent(this);
	}
	
}
