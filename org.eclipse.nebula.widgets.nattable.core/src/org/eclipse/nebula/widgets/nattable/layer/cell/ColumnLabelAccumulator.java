/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;

/**
 * Accumulator for column labels allowing to configure cells by their column
 * position.
 *
 * The label of a column is {@link #COLUMN_LABEL_PREFIX} + column position.
 */
public class ColumnLabelAccumulator implements IConfigLabelProvider {

    private IDataProvider dataProvider;

    /**
     * Create a {@link ColumnLabelAccumulator}. Creating it via this constructor
     * won't add support for CSS styling because it is not calculatable which
     * labels are created by this instance.
     *
     * @see ColumnLabelAccumulator#ColumnLabelAccumulator(IDataProvider)
     */
    public ColumnLabelAccumulator() {}

    /**
     * Create a {@link ColumnLabelAccumulator} which can be used in conjunction
     * with CSS styling, because the labels that are added to the cells are
     * predictable.
     *
     * @param dataProvider
     *            The {@link IDataProvider} that should be used to calculate
     *            which columns are added by this instance.
     * @since 1.4
     */
    public ColumnLabelAccumulator(IDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     * The common prefix of column labels (value is {@value} ).
     */
    public static final String COLUMN_LABEL_PREFIX = "COLUMN_"; //$NON-NLS-1$

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        configLabels.addLabel(COLUMN_LABEL_PREFIX + columnPosition);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = new HashSet<String>();
        if (this.dataProvider != null) {
            for (int i = 0; i < this.dataProvider.getColumnCount(); i++) {
                result.add(COLUMN_LABEL_PREFIX + i);
            }
        }
        return result;
    }

}
