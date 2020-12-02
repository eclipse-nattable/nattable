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
package org.eclipse.nebula.widgets.nattable.datachange.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.datachange.CellKeyHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;

/**
 * Extension of the {@link RowInsertEvent} that additionally carries the keys of
 * the inserted objects. Used for example in combination with the
 * DataChangeLayer to be able to revert an insert operation in a sorted or
 * filtered state.
 *
 * @since 1.6
 */
public class KeyRowInsertEvent extends RowInsertEvent {

    private final List<Object> keys = new ArrayList<>();
    private final CellKeyHandler<?> keyHandler;

    /**
     * Creates a {@link KeyRowInsertEvent} for one inserted row object.
     *
     * @param layer
     *            The layer to which the row index is matching.
     * @param rowIndex
     *            The index of the row that was inserted.
     * @param key
     *            The key of the inserted row object.
     * @param keyHandler
     *            The {@link CellKeyHandler} that was used to create the key.
     */
    public KeyRowInsertEvent(ILayer layer, int rowIndex, Object key, CellKeyHandler<?> keyHandler) {
        super(layer, rowIndex);
        this.keys.add(key);
        this.keyHandler = keyHandler;
    }

    /**
     * Creates a {@link KeyRowInsertEvent} for multiple inserted row objects.
     *
     * @param layer
     *            The layer to which the row indexes are matching.
     * @param rowPositionRange
     *            The position range of the inserted rows.
     * @param keys
     *            The keys of the inserted rows.
     * @param keyHandler
     *            The {@link CellKeyHandler} that was used to create the key.
     */
    public KeyRowInsertEvent(ILayer layer, Range rowPositionRange, Collection<Object> keys, CellKeyHandler<?> keyHandler) {
        super(layer, rowPositionRange);
        this.keys.addAll(keys);
        this.keyHandler = keyHandler;
    }

    /**
     * Creates a {@link KeyRowInsertEvent} for multiple inserted row objects.
     *
     * @param layer
     *            The layer to which the row indexes are matching.
     * @param rowPositionRanges
     *            The position ranges of the inserted rows.
     * @param keys
     *            The keys of the inserted rows.
     * @param keyHandler
     *            The {@link CellKeyHandler} that was used to create the key.
     */
    public KeyRowInsertEvent(ILayer layer, Collection<Range> rowPositionRanges, Collection<Object> keys, CellKeyHandler<?> keyHandler) {
        super(layer, rowPositionRanges);
        this.keys.addAll(keys);
        this.keyHandler = keyHandler;
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The event to clone.
     */
    protected KeyRowInsertEvent(KeyRowInsertEvent event) {
        super(event);
        this.keys.addAll(event.keys);
        this.keyHandler = event.keyHandler;
    }

    /**
     *
     * @return The keys of the inserted rows.
     */
    public List<Object> getKeys() {
        return this.keys;
    }

    /**
     *
     * @return The {@link CellKeyHandler} that was used to create the key.
     */
    public CellKeyHandler<?> getKeyHandler() {
        return this.keyHandler;
    }

    @Override
    public KeyRowInsertEvent cloneEvent() {
        return new KeyRowInsertEvent(this);
    }
}
