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

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.builder.configuration.RowHeaderConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableModel;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableRow;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableStyle;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;


public class RowHeaderLayerStack<T extends TableRow> extends AbstractLayerTransform {


	private final DefaultRowHeaderDataProvider dataProvider;

	public RowHeaderLayerStack(BodyLayerStack<T> bodyLayer, TableModel tableModel) {
		TableStyle tableStyle = tableModel.tableStyle;

		dataProvider = new DefaultRowHeaderDataProvider(bodyLayer.getDataProvider());

		DataLayer rowHeaderDataLayer = new DataLayer(dataProvider, tableStyle.rowHeaderWidth, tableStyle.defaultRowHeight);
		rowHeaderDataLayer.setDefaultColumnWidth(tableModel.tableStyle.rowHeaderWidth);

		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());

		setUnderlyingLayer(rowHeaderLayer);

		// Style config
		rowHeaderLayer.addConfiguration(new RowHeaderConfiguration(tableStyle));
	}

	public IDataProvider getDataProvider() {
		return dataProvider;
	}
}
