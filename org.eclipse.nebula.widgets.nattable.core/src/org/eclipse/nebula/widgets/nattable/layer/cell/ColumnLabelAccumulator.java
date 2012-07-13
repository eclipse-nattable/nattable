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

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;


/**
 * Accumulator for column labels allowing to configure cells by their column position.
 * 
 * The label of a column is {@link #COLUMN_LABEL_PREFIX} + column position.
 */
public class ColumnLabelAccumulator implements IConfigLabelAccumulator {

	/**
	 * The common prefix of column labels (value is {@value}).
	 */
	public static final String COLUMN_LABEL_PREFIX = "COLUMN_"; //$NON-NLS-1$


	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		configLabels.addLabel(COLUMN_LABEL_PREFIX + columnPosition);
	}

}
