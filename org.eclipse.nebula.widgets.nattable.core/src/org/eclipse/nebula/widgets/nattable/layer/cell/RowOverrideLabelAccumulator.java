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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.io.Serializable;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;


/**
 * @see ColumnOverrideLabelAccumulator
 * @param <T> type of the bean used as the data source for a row 
 */
public class RowOverrideLabelAccumulator<T> extends AbstractOverrider {
	
	private IRowDataProvider<T> dataProvider;
	private IRowIdAccessor<T> idAccessor;

	public RowOverrideLabelAccumulator(IRowDataProvider<T> dataProvider, IRowIdAccessor<T> idAccessor) {
		this.dataProvider = dataProvider;
		this.idAccessor = idAccessor;
	}
	
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		T rowObject = dataProvider.getRowObject(rowPosition);
		Serializable rowId = idAccessor.getRowId(rowObject);
		List<String> overrides = getOverrides(rowId);
		if (overrides != null) {
			for (String configLabel : overrides) {
				configLabels.addLabel(configLabel);
			}
		}
	}

	public void registerOverrides(int rowIndex, String...configLabels) {
		Serializable id = idAccessor.getRowId(dataProvider.getRowObject(rowIndex));
		registerOverrides(id, configLabels);
	}
	
}
