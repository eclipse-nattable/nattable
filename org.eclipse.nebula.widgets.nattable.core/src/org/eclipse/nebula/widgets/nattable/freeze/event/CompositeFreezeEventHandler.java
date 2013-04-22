/*******************************************************************************
 * Copyright (c) Apr 22, 2013 esp and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    esp - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze.event;

import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class CompositeFreezeEventHandler implements ILayerEventHandler<IStructuralChangeEvent> {

	private FreezeLayer freezeLayer;
	private ViewportLayer viewportLayer;

	public CompositeFreezeEventHandler(FreezeLayer freezeLayer, ViewportLayer viewportLayer) {
		this.freezeLayer = freezeLayer;
		this.viewportLayer = viewportLayer;
	}
	
	@Override
	public Class<IStructuralChangeEvent> getLayerEventClass() {
		return IStructuralChangeEvent.class;
	}

	@Override
	public void handleLayerEvent(IStructuralChangeEvent event) {
		viewportLayer.setOriginX(freezeLayer.getWidth());
		viewportLayer.setOriginY(freezeLayer.getHeight());
	}

}
