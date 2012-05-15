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

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;

/**
 * Adds the Java class name of the cell's data value as a label.   
 */
public class ClassNameConfigLabelAccumulator implements IConfigLabelAccumulator {

	private IRowDataProvider<?> dataProvider;
	
	public ClassNameConfigLabelAccumulator(IRowDataProvider<?> dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	public void accumulateConfigLabels(LabelStack configLabel, int columnPosition, int rowPosition) {
		Object value = dataProvider.getDataValue(columnPosition, rowPosition);
		if (value != null) {
			configLabel.addLabel(value.getClass().getName());
		}
	}

}
