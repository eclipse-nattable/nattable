/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth and others.
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
     * Creates a new cell key based on the given key with an updated column
     * index.
     *
     * @param oldKey
     *            The key that should be copied.
     * @param columnIndex
     *            The column index that should be updated in the existing key.
     * @return The key of the cell with the previous row identifier and the
     *         updated column identifier.
     */
    K getKeyWithColumnUpdate(K oldKey, int columnIndex);

    /**
     * Creates a new cell key based on the given key with an updated row index.
     *
     * @param oldKey
     *            The key that should be copied.
     * @param rowIndex
     *            The row index that should be updated in the existing key.
     * @return The key of the cell with the previous column identifier and the
     *         updated row identifier.
     */
    K getKeyWithRowUpdate(K oldKey, int rowIndex);

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
