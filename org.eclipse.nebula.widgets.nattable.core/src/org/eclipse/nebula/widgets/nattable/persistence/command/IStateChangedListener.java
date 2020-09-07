/*******************************************************************************
 * Copyright (c) 2012, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.persistence.command;

/**
 * Listener interface to react on changes on NatTable states/view configurations
 * made by using the PersistenceDialog.
 *
 * @see StateChangeEvent
 */
public interface IStateChangedListener {

    /**
     * Handle the {@link StateChangeEvent} fired by the PersistenceDialog.
     *
     * @param event
     *            The {@link StateChangeEvent} to handle
     */
    void handleStateChange(final StateChangeEvent event);
}
