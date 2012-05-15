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
package org.eclipse.nebula.widgets.nattable.summaryrow;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class SummationSummaryProvider implements ISummaryProvider {

	private final IDataProvider dataProvider;
	private final boolean strict;
	
	public SummationSummaryProvider(IDataProvider dataProvider) {
		this(dataProvider, true);
	}

	/**
	 * @param strict If strict is true and one or more of the values in the
	 * column is not a number, then DEFAULT_SUMMARY_VALUE is returned. If
	 * strict is false, this method will return the sum of all the
	 * values in the column that are numbers, ignoring the non-numeric values.
	 */
	public SummationSummaryProvider(IDataProvider dataProvider, boolean strict) {
		this.dataProvider = dataProvider;
		this.strict = strict;
	}

	/**
	 * @return sum of all the numbers in the column (as Floats), or
	 * 	DEFAULT_SUMMARY_VALUE for non-numeric columns if strict is set to true
	 */
	public Object summarize(int columnIndex) {
		int rowCount = dataProvider.getRowCount();
		float summaryValue = 0;
		
		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			Object dataValue = dataProvider.getDataValue(columnIndex, rowIndex);

			if (dataValue instanceof Number) {
				summaryValue = summaryValue + Float.parseFloat(dataValue.toString());
			} else if (strict) {
				return DEFAULT_SUMMARY_VALUE;
			}
		}
			
		return summaryValue;
	}
}

