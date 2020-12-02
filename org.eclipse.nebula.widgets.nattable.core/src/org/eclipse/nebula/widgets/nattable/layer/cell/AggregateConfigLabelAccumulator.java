/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 455949
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;

/**
 * An {@link IConfigLabelAccumulator} that can aggregate labels from other
 * <code>IConfigLabelAccumulator</code>s. All the labels provided by the
 * aggregated accumulators are applied to the cell.
 */
public class AggregateConfigLabelAccumulator implements IConfigLabelProvider {

    private List<IConfigLabelAccumulator> accumulators = new ArrayList<>();

    public void add(IConfigLabelAccumulator r) {
        if (r == null)
            throw new IllegalArgumentException("IConfigLabelAccumulator can not be null"); //$NON-NLS-1$
        this.accumulators.add(r);
    }

    public void add(IConfigLabelAccumulator... r) {
        if (r == null)
            throw new IllegalArgumentException("IConfigLabelAccumulator can not be null"); //$NON-NLS-1$
        this.accumulators.addAll(Arrays.asList(r));
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        for (IConfigLabelAccumulator accumulator : this.accumulators) {
            accumulator.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = new HashSet<>();
        for (IConfigLabelAccumulator accumulator : this.accumulators) {
            if (accumulator instanceof IConfigLabelProvider) {
                result.addAll(((IConfigLabelProvider) accumulator).getProvidedLabels());
            }
        }
        return result;
    }

}
