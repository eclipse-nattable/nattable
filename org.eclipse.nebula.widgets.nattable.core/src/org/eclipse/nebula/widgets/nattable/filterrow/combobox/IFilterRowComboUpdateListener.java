/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

/**
 * Interface for listeners that listen to updates to the value cache of the
 * FilterRowComboBoxDataProvider.
 */
public interface IFilterRowComboUpdateListener {

    /**
     * Handles the update of the value cache of the
     * FilterRowComboBoxDataProvider
     *
     * @param event
     *            The FilterRowComboUpdateEvent that should be handled
     */
    void handleEvent(FilterRowComboUpdateEvent event);
}
