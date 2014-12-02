/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

/**
 * Interface for listeners that listen to updates to the value cache of the
 * FilterRowComboBoxDataProvider.
 *
 * @author Dirk Fauth
 *
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
