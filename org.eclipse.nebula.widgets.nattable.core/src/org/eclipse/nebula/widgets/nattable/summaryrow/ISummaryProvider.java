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
package org.eclipse.nebula.widgets.nattable.summaryrow;

/**
 * Summarizes the values in a column. Used by the {@link SummaryRowLayer} to
 * calculate summary values.
 */
public interface ISummaryProvider {

    public static final Object DEFAULT_SUMMARY_VALUE = "..."; //$NON-NLS-1$

    /**
     * @param columnIndex
     *            The column index of the column for which the summary should be
     *            calculated.
     * @return The calculated summary value for the column.
     */
    public Object summarize(int columnIndex);

    /**
     * Register this instance to indicate that a summary is not required. Doing
     * so avoids calls to the {@link ISummaryProvider} and is a performance
     * tweak.
     */
    public static final ISummaryProvider NONE = new ISummaryProvider() {
        @Override
        public Object summarize(int columnIndex) {
            return null;
        }
    };

    /**
     * This instance will always return
     * {@link ISummaryProvider#DEFAULT_SUMMARY_VALUE} and does not perform a
     * calculation.
     */
    public static final ISummaryProvider DEFAULT = new ISummaryProvider() {
        @Override
        public Object summarize(int columnIndex) {
            return DEFAULT_SUMMARY_VALUE;
        }
    };
}
