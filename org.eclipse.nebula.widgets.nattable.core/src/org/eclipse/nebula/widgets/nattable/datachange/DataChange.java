/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
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
