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
package org.eclipse.nebula.widgets.nattable.extension.builder.layers;

import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableRow;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;


public class CornerLayerStack<T extends TableRow> extends AbstractLayerTransform {


	public CornerLayerStack(ColumnHeaderLayerStack<T> columnHeaderLayer, RowHeaderLayerStack<T> rowHeaderLayer) {
		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(
				columnHeaderLayer.getDataProvider(),
				rowHeaderLayer.getDataProvider());

		CornerLayer cornerLayer = new CornerLayer(new DataLayer(cornerDataProvider), rowHeaderLayer, columnHeaderLayer);

		setUnderlyingLayer(cornerLayer);
	}
}
