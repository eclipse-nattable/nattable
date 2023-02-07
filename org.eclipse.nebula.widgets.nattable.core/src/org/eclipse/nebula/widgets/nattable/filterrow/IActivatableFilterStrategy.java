/*******************************************************************************
 * Copyright (c) 2023 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow;

/**
 * Extended {@link IFilterStrategy} that supports the activating and
 * deactivating the filter logic. Useful for special filter logic like static or
 * exclude filters.
 *
 * @param <T>
 *            type of objects in the FilterList
 *
 * @since 2.1
 */
public interface IActivatableFilterStrategy<T> extends IFilterStrategy<T> {

    /**
     * Activate additional filter logic so it gets applied on the next filter
     * operation, e.g. static or exclude filters. By default does nothing.
     */
    void activateFilterStrategy();

    /**
     * Deactivate additional filter logic so it does not get applied on the next
     * filter operation, e.g. static or exclude filters. By default does
     * nothing.
     */
    void deactivateFilterStrategy();

    /**
     *
     * @return <code>true</code> if the additional filter logic provided by this
     *         {@link IFilterStrategy} is active, <code>false</code> if not.
     */
    boolean isActive();
}
