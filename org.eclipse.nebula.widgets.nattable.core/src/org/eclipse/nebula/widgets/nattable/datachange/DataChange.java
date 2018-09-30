/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

/**
 * Interface that specifies a data change that is tracked by the
 * {@link DataChangeLayer}.
 *
 * @since 1.6
 */
public interface DataChange {

    /**
     * Discard the change.
     * 
     * @param layer
     *            The {@link DataChangeLayer} used to perform index-position
     *            transformations and to trigger the necessary commands to save.
     */
    void discard(DataChangeLayer layer);

    /**
     * Save the change.
     * 
     * @param layer
     *            The {@link DataChangeLayer} used to perform index-position
     *            transformations and to trigger the necessary commands to save.
     */
    void save(DataChangeLayer layer);

    /**
     *
     * @return The key used to identify the change in the backing data.
     */
    Object getKey();

    /**
     * Update the locally stored key. Used in case the key changed because of
     * structural changes.
     *
     * @param key
     *            The updated key.
     */
    void updateKey(Object key);
}
