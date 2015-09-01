/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - added missing generic type arguments
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * This class implements "last row" caching for much faster column value access
 * on the table. As normally the default implementation would fetch the row
 * object for each cell in a column, we know what the last row we fetched was,
 * and if it's a re-request of the same row as last time, we simply return the
 * last cached row object (assuming the list didn't change in some important
 * way, as then we clear out our cache).
 *
 * @author Emil Crumhorn
 *
 * @deprecated Use a default {@link ListDataProvider} instead as the performance
 *             boost can not be verified with current Java and GlazedLists
 *             implementations and as this implementation is not thread-safe it
 *             introduces more issues than it tries to solve.
 */
@Deprecated
public class GlazedListsDataProvider<T> extends ListDataProvider<T> {

    private int lastRowIndex = -1;
    private T lastRowObject = null;

    public GlazedListsDataProvider(EventList<T> list, IColumnAccessor<T> columnAccessor) {
        super(list, columnAccessor);

        // As we cache the last row object for much faster access, we need to
        // tell that "tiny cache" that the input changed in any way, so that it
        // doesn't use the last row object anymore, as that will cause same rows
        // as last to be updated with the old object until the entire table has
        // either refreshed twice (for multi-row tables) or it will never
        // refresh (single entry tables).
        // thus, if it's a delete, we update completely. if it's a modification
        // of the current row we have cached, we update as well, inserts we
        // don't need to as they are new items and the index will never be the
        // same anyway.
        list.addListEventListener(new ListEventListener<T>() {
            @Override
            public void listChanged(ListEvent<T> event) {
                while (event.next()) {
                    int sourceIndex = event.getIndex();
                    int changeType = event.getType();

                    if (changeType == ListEvent.DELETE
                            || sourceIndex == GlazedListsDataProvider.this.lastRowIndex) {
                        inputChanged();
                        break;
                    }
                }
            }
        });
    }

    public void inputChanged() {
        this.lastRowIndex = -1;
        this.lastRowObject = null;
    }

    @Override
    public T getRowObject(int rowIndex) {
        if (rowIndex != this.lastRowIndex || this.lastRowObject == null) {
            ((EventList<T>) this.list).getReadWriteLock().readLock().lock();
            try {
                return super.getRowObject(rowIndex);
            } finally {
                ((EventList<T>) this.list).getReadWriteLock().readLock().unlock();
            }
        }

        return this.lastRowObject;
    }

    @Override
    public Object getDataValue(int colIndex, int rowIndex) {
        // new row to cache
        if (rowIndex != this.lastRowIndex || this.lastRowObject == null) {
            this.lastRowIndex = rowIndex;
            ((EventList<T>) this.list).getReadWriteLock().readLock().lock();
            try {
                this.lastRowObject = this.list.get(rowIndex);
            } finally {
                ((EventList<T>) this.list).getReadWriteLock().readLock().unlock();
            }
        }

        // same row as last, use its object as it's way faster than a
        // list.get(row);
        return this.columnAccessor.getDataValue(this.lastRowObject, colIndex);
    }
}
