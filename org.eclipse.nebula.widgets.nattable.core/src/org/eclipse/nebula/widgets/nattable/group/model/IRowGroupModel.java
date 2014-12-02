/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.model;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.group.RowGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;

/**
 * The model behind the {@link RowGroupExpandCollapseLayer} contains
 * {@link IRowGroup}s which in turn contain lists of row objects of type T.
 */
public interface IRowGroupModel<T> extends IPersistable {

    /**
     * <p>
     * Adds a group into the model.
     * </p>
     * <p>
     * A notification should be sent to any {@link IRowGroupModelListener}s to
     * indicate a change in the model has occurred.
     * </p>
     *
     * @param rowGroup
     *            The {@link IRowGroup} to be added.
     * @return false if the group wasn't added.
     */
    boolean addRowGroup(final IRowGroup<T> rowGroup);

    /**
     * <p>
     * Adds multiple row groups into the model and only fires off a single
     * change notification.
     * </p>
     *
     * @param rowGroups
     *            A list of {@link IRowGroup}s to add.
     */
    void addRowGroups(final List<IRowGroup<T>> rowGroups);

    /**
     * <p>
     * Removes the group from the model.
     * </p>
     * <p>
     * A notification should be sent to any {@link IRowGroupModelListener}s to
     * indicate a change in the model has occurred.
     * </p>
     *
     * @param rowGroup
     *            The {@link IRowGroup} to remove.
     * @return true if the group was present and removed, false if the group was
     *         not present.
     */
    boolean removeRowGroup(final IRowGroup<T> rowGroup);

    /**
     * @return an unmodifiable {@link List} of {@link IRowGroup} in the model.
     */
    List<IRowGroup<T>> getRowGroups();

    /**
     * Returns an the {@link IRowGroup} with the specified group name.
     *
     * @param groupName
     *            The unique name assigned to an {@link IRowGroup}.
     * @return An {@link IRowGroup} or null if there is no group with the
     *         specified name.
     */
    IRowGroup<T> getRowGroupForName(final String groupName);

    /**
     * Returns the first {@link IRowGroup} found containing the specified row.
     *
     * @param row
     *            The row object.
     * @return An {@link IRowGroup} or null if there is no group with the
     *         specified row.
     */
    IRowGroup<T> getRowGroupForRow(final T row);

    /**
     * @return true if there are no {@link IRowGroup}s in the model. Note: if
     *         there are groups but there are no rows, then true is still
     *         returned.
     */
    boolean isEmpty();

    /**
     * <p>
     * Wipes all groups from the model.
     * </p>
     * <p>
     * A notification should be sent to any {@link IRowGroupModelListener}s to
     * indicate a change in the model has occurred.
     * </p>
     */
    void clear();

    /**
     * <p>
     * Notify any {@link IRowGroupModelListener}s that something in the model
     * has changed.
     * </p>
     */
    void notifyListeners();

    /**
     * Registers a listener to the model to receive notification of any changes.
     *
     * @param listener
     *            an {@link IRowGroupModelListener}.
     */
    void registerRowGroupModelListener(final IRowGroupModelListener listener);

    /**
     * Unregisters the listener from the model.
     *
     * @param listener
     *            an {@link IRowGroupModelListener}.
     */
    void unregisterRowGroupModelListener(final IRowGroupModelListener listener);

    /**
     * Retrieves a row T by it's index from our cache. It will be added to the
     * cache if not present.
     */
    T getRowFromIndexCache(final int rowIndex);

    /**
     * Retrieves a row's index by from our cache. It will be added to the cache
     * if not present.
     */
    int getIndexFromRowCache(final T row);

    /**
     * Required for the index-to-row cache to populate itseld.
     */
    void setDataProvider(IRowDataProvider<T> dataProvider);

    /**
     * @return an {@link IRowDataProvider} used for the index-to-row cache.
     */
    IRowDataProvider<T> getDataProvider();

    void invalidateIndexCache();

}
