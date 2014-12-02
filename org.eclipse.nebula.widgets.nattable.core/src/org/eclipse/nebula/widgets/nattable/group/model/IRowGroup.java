/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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

/**
 * Represents a collapseable group of rows (of type T) in the
 * {@link IRowGroupModel}.
 *
 * @author Stefan Bolton
 *
 * @param <T>
 */
public interface IRowGroup<T> {

    /**
     * @return A Unique name for the group - it must not conflict with any other
     *         group's name.
     */
    String getGroupName();

    /**
     * If this group is a nested child of another this will return the parent
     * group.
     *
     * @return The parent group if this group is a nested child.
     */
    IRowGroup<T> getParentGroup();

    /**
     * Set the specified group as the parent of this group.
     *
     * @param parentGroup
     */
    void setParentGroup(final IRowGroup<T> parentGroup);

    /**
     * @return Whether the group has been expanded or collapsed. A collapsed
     *         group will hide all members of the group apart from any 'static'
     *         rows.
     */
    boolean isCollapsed();

    /**
     * @return Whether the group can be expanded or collapsed or, false if it
     *         should be locked in it's current state.
     */
    boolean isCollapseable();

    /**
     * <p>
     * Updates the group's state to indicate it is collapsed.
     * </p>
     * <p>
     * A notification should be sent to any {@link IRowGroupModelListener}s to
     * indicate a change in the model has occurred.
     * </p>
     */
    void collapse();

    /**
     * <p>
     * Updates the group's state to indicate it is expanded.
     * </p>
     * <p>
     * A notification should be sent to any {@link IRowGroupModelListener}s to
     * indicate a change in the model has occurred.
     * </p>
     */
    void expand();

    /**
     * <p>
     * Adds the row into the group.
     * </p>
     *
     * @param row
     *            the row to be added
     */
    void addMemberRow(final T row);

    /**
     * <p>
     * Adds multiple rows into the group.
     * </p>
     *
     * @param rows
     *            A {@link List} of rows T to be added.
     */
    void addMemberRows(final List<T> rows);

    /**
     * <p>
     * Adds a static row into the group. A static row is one that is always
     * shown when the group is collapsed (summary data rows for example).
     * </p>
     *
     * @param row
     *            the static row T to be added.
     */
    void addStaticMemberRow(final T row);

    /**
     * <p>
     * Removes the row from the group whether it's static or non-static.
     * </p>
     * <p>
     * Existing positions in the model may be bumped up if the row was not the
     * last row in the model.
     * </p>
     * <p>
     * A notification should be sent to any {@link IRowGroupModelListener}s to
     * indicate a change in the model has occurred.
     * </p>
     *
     * @param row
     *            The row T to be removed.
     * @return true if the row existed and was removed.
     */
    boolean removeMemberRow(final T row);

    /**
     * <p>
     * Removes multiple rows from the group whether they are static or
     * non-static.
     * </p>
     * <p>
     * Existing positions in the model may be bumped up if the row was not the
     * last row in the model.
     * </p>
     * <p>
     * A notification should be sent to any {@link IRowGroupModelListener}s to
     * indicate a change in the model has occurred.
     * </p>
     *
     * @param rows
     *            A {@link List} of rows T to be added.
     */
    void removeMemberRows(final List<T> rows);

    /**
     * @param includeStaticRows
     *            true to include the static rows false to exclude them.
     * @return an unmodifiable {@link List} of the rows (T) in the
     *         {@link IRowGroup}.
     */
    List<T> getMemberRows(final boolean includeStaticRows);

    /**
     * @return an unmodifiable {@link List} of the static rows (T) in the
     *         {@link IRowGroup}.
     */
    List<T> getStaticMemberRows();

    /**
     * Removes all member and static member rows from the group.
     */
    void clear();

    /**
     * @return <code>true</code> if there are no rows (normal or static) in the
     *         group.
     */
    boolean isEmpty();

    /**
     * Adds a row-group into the group. Calls to getMemberRows and
     * getStaticMemberRows will recurse through nested groups and return a
     * flattened list of rows in all contained groups.
     *
     * @param rowGroup
     *            a IHierarchicalRowGroup.
     */
    void addRowGroup(final IRowGroup<T> rowGroup);

    /**
     * Returns the row-group child of this group.
     *
     * @param rowGroup
     *            a IHierarchicalRowGroup.
     * @return true if the group existed as a child and was removed.
     */
    boolean removeRowGroup(final IRowGroup<T> rowGroup);

    /**
     * @return an unmodifiable {@link List} of the groups which are direct
     *         children of this group.
     */
    List<IRowGroup<T>> getRowGroups();

    /**
     * Returns only the rows contained with this group. Not nested groups.
     *
     * @return a list of rows T.
     */
    List<T> getOwnMemberRows(final boolean includeStaticRows);

    /**
     * @return Static rows only in this group not in nested groups.
     */
    List<T> getOwnStaticMemberRows();

    /**
     * Retrieves the sub-group for a given row member.
     */
    IRowGroup<T> getRowGroupForRow(final T row);

    /**
     * Allows some arbitrary data to be tagged to an IRowGroup.
     *
     * @param data
     */
    void setData(final Object data);

    /**
     * Allows some arbitrary data to be tagged to an IRowGroup.
     *
     * @param data
     */
    void setData(final String key, final Object data);

    /**
     * Allows some arbitrary data to be retrieved from an IRowGroup.
     */
    Object getData();

    /**
     * Allows some arbitrary data to be retrieved from an IRowGroup.
     */
    Object getData(String key);

}
