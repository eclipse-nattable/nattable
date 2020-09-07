/*****************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
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
     * @since 2.0
     */
    void hideColumnPositions(int... columnPositions);

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
     * Hide the columns with the given indexes.
     *
     * @param columnIndexes
     *            The indexes of the columns to hide.
     * @since 2.0
     */
    void hideColumnIndexes(int... columnIndexes);

    /**
     * Hide the columns with the given indexes.
     *
     * @param columnIndexes
     *            The indexes of the columns to hide.
     * @since 2.0
     */
    void hideColumnIndexes(Collection<Integer> columnIndexes);

    /**
     * Show the columns with the given indexes again if they are hidden by this
     * layer. Note that the indexes are needed and not the positions. This is
     * because a user is not able to select the hidden column in the NatTable
     * and therefore the position is not available anymore.
     *
     * @param columnIndexes
     *            The column indexes to show again.
     * @since 2.0
     */
    void showColumnIndexes(int... columnIndexes);

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
     * Show the column(s) that are hidden next to the given column position.
     *
     * @param columnPosition
     *            The column position whose neighbors should be shown again.
     * @param showToLeft
     *            Whether the column positions to the left or the right of the
     *            given column position should be shown again.
     * @param showAll
     *            Whether all hidden adjacent columns should be shown again or
     *            only the single direct adjacent column.
     */
    void showColumnPosition(int columnPosition, boolean showToLeft, boolean showAll);

    /**
     * Show all hidden columns again.
     */
    void showAllColumns();

    /**
     * Returns all indexes of the columns that are hidden in this layer.
     * <p>
     * <b>Note:</b> It does not include the column indexes of hidden columns
     * from underlying layers. This would cause issues on calculating positions
     * as every layer is responsible for those calculations itself.
     * </p>
     *
     * @return The indexes of the columns that are hidden in this layer.
     */
    Collection<Integer> getHiddenColumnIndexes();

    /**
     * Will collect and return all indexes of the columns that are hidden in
     * this layer.
     * <p>
     * <b>Note:</b> It is not intended that it also collects the column indexes
     * of underlying layers. This would cause issues on calculating positions,
     * as every layer is responsible for those calculations itself.
     * </p>
     *
     * @return All column indexes that are hidden in this layer.
     *
     * @since 2.0
     */
    int[] getHiddenColumnIndexesArray();
}
