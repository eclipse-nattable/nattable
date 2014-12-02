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

/**
 * Implementation of ISummaryProvider that summarizes all values in a column if
 * they are of type Number and return the sum as Double value.
 * <p>
 * If a column contains mixed values, e.g. Strings and Integers, the
 * SummationSummaryProvider will return
 * {@link ISummaryProvider#DEFAULT_SUMMARY_VALUE} by default, as a summation is
 * not possible this way. You are able to change that behaviour by creating the
 * SummationSummaryProvider with the parameter strict set to <code>false</code>.
 * In that case the non Number values will be ignored.
 */
public class SummationSummaryProvider implements ISummaryProvider {

    private final IDataProvider dataProvider;
    private final boolean strict;

    /**
     * Create a new {@link SummationSummaryProvider} by using the given
     * {@link IDataProvider}.
     * <p>
     * Using this constructor will set the {@link SummationSummaryProvider} in
     * strict mode which means that if a column contains non Number values,
     * {@link ISummaryProvider#DEFAULT_SUMMARY_VALUE} will be returned.
     *
     * @param dataProvider
     *            The {@link IDataProvider} that should be used to calculate the
     *            sum.
     */
    public SummationSummaryProvider(IDataProvider dataProvider) {
        this(dataProvider, true);
    }

    /**
     * Create a new {@link SummationSummaryProvider} by using the given
     * {@link IDataProvider} and strict mode configuration.
     * <p>
     * Using this constructor will set the {@link SummationSummaryProvider} in
     * strict mode which means that if a column contains non Number values,
     * {@link ISummaryProvider#DEFAULT_SUMMARY_VALUE} will be returned.
     *
     * @param dataProvider
     *            The {@link IDataProvider} that should be used to calculate the
     *            sum.
     * @param strict
     *            If strict is set to <code>true</code> and one or more of the
     *            values in the column is not of type Number, then
     *            {@link ISummaryProvider#DEFAULT_SUMMARY_VALUE} will be
     *            returned. If strict is set to <code>false</code>, this method
     *            will return the sum of all the values in the column that are
     *            of type Number, ignoring the non Number values.
     */
    public SummationSummaryProvider(IDataProvider dataProvider, boolean strict) {
        this.dataProvider = dataProvider;
        this.strict = strict;
    }

    /**
     * Calculates the sum of the values in the column.
     *
     * @return The sum of all Number values in the column as Double or
     *         {@link ISummaryProvider#DEFAULT_SUMMARY_VALUE} if the column
     *         contains non Number values and this SummationSummaryProvider is
     *         configured to be strict.
     */
    @Override
    public Object summarize(int columnIndex) {
        int rowCount = this.dataProvider.getRowCount();
        double summaryValue = 0;

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object dataValue = this.dataProvider.getDataValue(columnIndex, rowIndex);

            if (dataValue instanceof Number) {
                summaryValue += ((Number) dataValue).doubleValue();
            } else if (this.strict) {
                return DEFAULT_SUMMARY_VALUE;
            }
        }

        return summaryValue;
    }
}
