/*****************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.Collection;

/**
 * Interface to define a layer that is able to deal with hidden columns.
 *
 * @since 1.6
 */
public interface IColumnHideShowLayer {

    /**
     * Hide the columns at the specified positions. Note that the positions are
     * required as this method might get called via user interaction in a
     * transformed table (e.g. reordered).
     *
     * @param columnPositions
     *            The column positions to hide.
     */
    void hideColumnPositions(Integer... columnPositions);

    /**
     * Hide the columns at the specified positions. Note that the positions are
     * required as this method might get called via user interaction in a
     * transformed table (e.g. reordered).
     *
     * @param columnPositions
     *            The column positions to hide.
     */
    void hideColumnPositions(Collection<Integer> columnPositions);

    /**
     * Show the columns with the given indexes again if they are hidden by this
     * layer. Note that the indexes are needed and not the positions. This is
     * because a user is not able to select the hidden column in the NatTable
     * and therefore the position is not available anymore.
     *
     * @param columnIndexes
     *            The column indexes to show again.
     */
    void showColumnIndexes(Integer... columnIndexes);

    /**
     * Show the columns with the given indexes again if they are hidden by this
     * layer. Note that the indexes are needed and not the positions. This is
     * because a user is not able to select the hidden column in the NatTable
     * and therefore the position is not available anymore.
     *
     * @param columnIndexes
     *            The column indexes to show again.
     */
    void showColumnIndexes(Collection<Integer> columnIndexes);

    /**
     * Show all hidden columns again.
     */
    void showAllColumns();
}
