/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

/**
 * Listener interface to get notified on changes in the {@link GroupByModel}.
 *
 * @since 2.5
 */
public interface GroupByModelListener {

    /**
     * Handle changes in the {@link GroupByModel}.
     *
     * @param groupByModel
     *            The {@link GroupByModel} that has changed.
     */
    void handleGroupByModelChange(GroupByModel groupByModel);
}
