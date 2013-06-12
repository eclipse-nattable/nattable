/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;


/**
 * An {@link IConfigLabelAccumulator} that can aggregate labels from other <code>IConfigLabelAccumulator</code>s. 
 * All the labels provided by the aggregated accumulators are applied to the cell.
 */
public class AggregrateConfigLabelAccumulator implements IConfigLabelAccumulator {
    
    private List<IConfigLabelAccumulator> accumulators = new ArrayList<IConfigLabelAccumulator>();
    
    public void add(IConfigLabelAccumulator r) {
        if (r == null) throw new IllegalArgumentException("null"); //$NON-NLS-1$
        accumulators.add(r);
    }

    public void add(IConfigLabelAccumulator... r) {
    	if (r == null) throw new IllegalArgumentException("null"); //$NON-NLS-1$
    	accumulators.addAll(Arrays.asList(r));
    }

    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        for (IConfigLabelAccumulator accumulator : accumulators) {
        	accumulator.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
        }
    }

}
