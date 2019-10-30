/*******************************************************************************
 * Copyright (c) 2013, 2019 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.Collection;

/**
 * Interface to define a layer that is able to deal with hidden rows.
 *
 * @since 1.6
 */
public interface IRowHideShowLayer {

    /**
     * Hide the rows at the given positions.
     *
     * @param rowPositions
     *            The positions of the rows to hide.
     * @since 2.0
     */
    void hideRowPositions(int... rowPositions);

    /**
     * Hide the rows at the given positions.
     *
     * @param rowPositions
     *            The positions of the rows to hide.
     * @since 2.0
     */
    void hideRowPositions(Collection<Integer> rowPositions);

    /**
     * Hide the rows with the given indexes.
     *
     * @param rowIndexes
     *            The indexes of the rows to hide.
     * @since 2.0
     */
    void hideRowIndexes(int... rowIndexes);

    /**
     * Hide the rows with the given indexes.
     *
     * @param rowIndexes
     *            The indexes of the rows to hide.
     * @since 2.0
     */
    void hideRowIndexes(Collection<Integer> rowIndexes);

    /**
     * Show the rows with the given indexes again.
     *
     * @param rowIndexes
     *            The indexes of the rows that should be showed again.
     * @since 2.0
     */
    void showRowIndexes(int... rowIndexes);

    /**
     * Show the rows with the given indexes again.
     *
     * @param rowIndexes
     *            The indexes of the rows that should be showed again.
     * @since 2.0
     */
    void showRowIndexes(Collection<Integer> rowIndexes);

    /**
     * Show the row(s) that are hidden next to the given row position.
     *
     * @param rowPosition
     *            The row position whose neighbors should be shown again.
     * @param showToTop
     *            Whether the row positions to the top or the bottom of the
     *            given row position should be shown again.
     * @param showAll
     *            Whether all hidden adjacent rows should be shown again or only
     *            the single direct adjacent row.
     */
    void showRowPosition(int rowPosition, boolean showToTop, boolean showAll);

    /**
     * Show all rows that where previously hidden.
     *
     * @since 2.0
     */
    void showAllRows();

}
