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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class ColumnHeaderLayerFixture extends ColumnHeaderLayer {

	//Viewport is 400px wide
	public static DataLayer dataLayer = new DataLayerFixture();
	public static ViewportLayer bodyLayer = new ViewportLayerFixture(new SelectionLayer(dataLayer));

	public ColumnHeaderLayerFixture() {
		super(dataLayer, bodyLayer, bodyLayer.getSelectionLayer());
	}

	public ColumnHeaderLayerFixture(ViewportLayer viewportLayer) {
		super(dataLayer, viewportLayer, viewportLayer.getSelectionLayer());
	}

	public static DataLayer getDataLayer() {
		return dataLayer;
	}
}
