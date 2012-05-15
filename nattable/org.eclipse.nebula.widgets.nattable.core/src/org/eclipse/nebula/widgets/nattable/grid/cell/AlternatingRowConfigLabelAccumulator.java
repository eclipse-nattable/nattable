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
package org.eclipse.nebula.widgets.nattable.grid.cell;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

/**
 * Applies 'odd'/'even' labels to all the rows. These labels are
 * the used to apply color to alternate rows.
 *
 * @see DefaultRowStyleConfiguration
 */
public class AlternatingRowConfigLabelAccumulator implements IConfigLabelAccumulator {

	public static final String ODD_ROW_CONFIG_TYPE = "ODD_" + GridRegion.BODY; //$NON-NLS-1$

	public static final String EVEN_ROW_CONFIG_TYPE = "EVEN_" + GridRegion.BODY; //$NON-NLS-1$

	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		configLabels.addLabel((rowPosition % 2 == 0 ? EVEN_ROW_CONFIG_TYPE : ODD_ROW_CONFIG_TYPE));
	}
}
