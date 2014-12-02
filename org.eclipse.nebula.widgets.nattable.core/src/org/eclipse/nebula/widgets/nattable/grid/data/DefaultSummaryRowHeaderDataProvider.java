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
package org.eclipse.nebula.widgets.nattable.grid.data;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;

/**
 * {@link IDataProvider} to use for the {@link RowHeaderLayer} if the
 * {@link SummaryRowLayer} is present in the body layer stack. This adds an
 * extra row to the row header for displaying the summary row.
 */
public class DefaultSummaryRowHeaderDataProvider extends
        DefaultRowHeaderDataProvider implements IDataProvider {

    public static final String DEFAULT_SUMMARY_ROW_LABEL = "Summary"; //$NON-NLS-1$
    private final String summaryRowLabel;

    public DefaultSummaryRowHeaderDataProvider(IDataProvider bodyDataProvider) {
        this(bodyDataProvider, DEFAULT_SUMMARY_ROW_LABEL);
    }

    /**
     * @param summaryRowLabel
     *            label to display in the row header for the Summary Row
     */
    public DefaultSummaryRowHeaderDataProvider(IDataProvider bodyDataProvider,
            String summaryRowLabel) {
        super(bodyDataProvider);
        this.summaryRowLabel = summaryRowLabel;
    }

    @Override
    public int getRowCount() {
        return super.getRowCount() + 1;
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        if (rowIndex == super.getRowCount()) {
            return this.summaryRowLabel;
        }
        return super.getDataValue(columnIndex, rowIndex);
    }
}
