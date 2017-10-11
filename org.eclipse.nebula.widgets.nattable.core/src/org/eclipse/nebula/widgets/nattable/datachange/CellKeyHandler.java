/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

/**
 * Implementations of this interface are intended to generate a key for a cell
 * identified via column and row index. It can also calculate the column and row
 * index from the key again.
 *
 * @param <K>
 *            The type of the key.
 *
 * @since 1.6
 */
public interface CellKeyHandler<K> {

    /**
     * Creates the cell key based on the given column index and row index.
     *
     * @param columnIndex
     *            The column index of the cell whose key should be generated.
     * @param rowIndex
     *            The row index of the cell whose key should be generated.
     * @return The key of the cell with the given indexes.
     */
    K getKey(int columnIndex, int rowIndex);

    /**
     *
     * @param key
     *            The key from which the column index should be retrieved.
     * @return The column index for the cell key.
     */
    int getColumnIndex(K key);

    /**
     *
     * @param key
     *            The key from which the row index should be retrieved.
     * @return The row index for the cell key.
     */
    int getRowIndex(K key);

    /**
     *
     * @return <code>true</code> if the keys created by this handler need to be
     *         updated on horizontal structural changes, <code>false</code> if
     *         the keys update automatically.
     */
    boolean updateOnHorizontalStructuralChange();

    /**
     *
     * @return <code>true</code> if the keys created by this handler need to be
     *         updated on vertical structural changes, <code>false</code> if the
     *         keys update automatically.
     */
    boolean updateOnVerticalStructuralChange();
}
