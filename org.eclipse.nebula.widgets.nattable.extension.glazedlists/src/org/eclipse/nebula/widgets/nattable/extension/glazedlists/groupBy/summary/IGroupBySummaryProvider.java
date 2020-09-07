/*******************************************************************************
 * Copyright (c) 2013, 2020 Alexandre Pauzies and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexandre Pauzies <alexandre@pauzies.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary;

import java.util.List;

public interface IGroupBySummaryProvider<T> {

    public static final Object DEFAULT_SUMMARY_VALUE = "..."; //$NON-NLS-1$

    /**
     * @param columnIndex
     *            The column index of the column for which the summary should be
     *            calculated.
     * @return The calculated summary value for the column.
     */
    public Object summarize(int columnIndex, List<T> children);
}